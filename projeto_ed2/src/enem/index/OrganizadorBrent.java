package enem.index;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import comum.Aluno;
import comum.IFileOrganizer;

public class OrganizadorBrent implements IFileOrganizer {
	
	private static int NUM_OF_REC = 10000019;
	
	private File file;
	private FileChannel channel;
	private RandomAccessFile rf;
	private ByteBuffer buffer;
	
	public OrganizadorBrent( String fileName ) {
		
		file = new File( fileName );
		
		try {
			if( !file.exists() )
				file.createNewFile();
		}
		catch( IOException e ) { e.printStackTrace(); }
	}

	@Override
	public void addAluno( Aluno p ) {
		
		long position = getAlunoPosition( p.getMatricula() );
		
		if( position < 0 ) { // Verifica se o registro já não existe
			
			try {
				rf = new RandomAccessFile( file, "rw" );
				channel = rf.getChannel();
				buffer = ByteBuffer.allocate( Aluno.LENGTH );
				position = hash( p.getMatricula() ) * ( long )buffer.capacity();
				int qtdBytes = channel.read( buffer, position );
				
				if( qtdBytes > -1 ) { // Verifica se existe registro na posição lida
					
					long matBuffer = buffer.getLong( 0 );

					if( matBuffer > 0 ) { // Verifica se a posição lida não é de registro excluído
						
						long custoP			= calculaCustoAcesso( p.getMatricula(), position ),
							custoMatBuffer	= calculaCustoAcesso( matBuffer, position );
						
						if( custoP > custoMatBuffer ) {
							
							buffer.position( 0 );
							channel.write( buffer, position + ( ( custoMatBuffer -1L ) * ( long )buffer.capacity() ) );
						}
						else
							position += ( custoP - 1L ) * ( long )buffer.capacity();
					}
				}
								
				buffer = p.toByteBuffer();
				
				channel.write( buffer, position );
				channel.close();
				rf.close();
			}
			catch( IOException e ) { e.printStackTrace(); }
		}
	}

	@Override
	public Aluno getAluno( long matricula ) {
		
		long position = getAlunoPosition( matricula );
		
		if( position < 0 )
			return null;
		
		try {
			rf = new RandomAccessFile( file, "r" );
			channel = rf.getChannel();
			
			buffer = ByteBuffer.allocate( Aluno.LENGTH );
			channel.read( buffer, position );
			channel.close();
			rf.close();
		}
		catch( IOException e ) { e.printStackTrace(); }
		
		Aluno aluno = new Aluno( buffer );
		
		return aluno;
	}

	@Override
	public Aluno delAluno( long matricula ) {
		
		Aluno aluno = getAluno( matricula );
		
		if( aluno != null ) {
			
			long position = getAlunoPosition( matricula );
			
			try {
				rf = new RandomAccessFile( file, "rw" );
				channel = rf.getChannel();	
				buffer = ByteBuffer.allocate( Aluno.LENGTH );
				
				buffer.putLong( -1 );
				buffer.position( 0 );
				channel.write( buffer, position );
				channel.close();
				rf.close();
			}
			catch( IOException e ) { e.printStackTrace(); }
		}
		
		return aluno;
	}
	
	private long hash( long chave ) { return chave % ( long )NUM_OF_REC; }
	
	private long inc( long chave ) { return ( chave % ( ( long )NUM_OF_REC -2L ) ) +1L; }
	
	private long getAlunoPosition( long matricula ) {
		
		long position = hash( matricula ) * ( long )Aluno.LENGTH, // position é do arquivo, não confundir com o do buffer
			 incremento = inc( matricula ) * ( long )Aluno.LENGTH; // A multiplicação por LENGTH é para evitar que um registro seja inserido no meio de outro
		
		try {
			rf = new RandomAccessFile( file, "r" );
			channel = rf.getChannel();
			buffer = ByteBuffer.allocate( Aluno.LENGTH );
				
			while( channel.read( buffer, position ) > -1 ) { // Se retornar -1, não existe dado na posição
				
				long mat = buffer.getLong( 0 );
				
				if( mat < 1 ) // Se mat == 0, então o registro é vazio, Se mat == -1, o registro foi excluído 
					break;
				
				if( matricula == mat )
					return position;
				
				position += incremento;
				
				if( position > ( long )NUM_OF_REC * ( long )Aluno.LENGTH ) {
					
					long diff = position -( ( long )NUM_OF_REC * ( long )Aluno.LENGTH );
					position = 0L + diff;
				}
				
				buffer.position( 0 );
			}
			
			channel.close();
			rf.close();
		}
		catch ( IOException e ) { e.printStackTrace();	}
		
		return -1;
	}
	
	private long calculaCustoAcesso( long matricula, long position ) {
		
		long qtdBytes = -1,
			 inc = inc( matricula ) * ( long )Aluno.LENGTH;
		
		try {
			RandomAccessFile rf = new RandomAccessFile( file, "r" );
			FileChannel channel = rf.getChannel();
			buffer = ByteBuffer.allocate( Aluno.LENGTH );
			qtdBytes = channel.read( buffer, position );
			
			channel.close();
			rf.close();
		} 
		catch ( IOException e ) { e.printStackTrace(); }
		
		if( qtdBytes < 0 || buffer.getLong( 0 ) < 1 ) // Verifica se existia algum registro não vazio naquela posição
			return 2; // Retorna 2 porque, se precisou calcular o custo, o registro já não vai ser inserido na primeira posição checada
		
		return 1L + calculaCustoAcesso( matricula, position + inc );
	}
	
	public boolean hasDatabase() { return file.exists(); }
	
	public void moveDatabase( String newPath ) {
		
		file.renameTo( new File( newPath ) );
		file = new File( newPath );
	}
}