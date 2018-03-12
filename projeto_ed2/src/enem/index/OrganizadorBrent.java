package enem.index;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import comum.Aluno;
import comum.IFileOrganizer;

public class OrganizadorBrent implements IFileOrganizer {
	
	private static int MAX_RECORDS = 10000019;
	
	private File file;
	private FileChannel in, 
						out;
	private RandomAccessFile rfIn,
							 rfOut;
	private ByteBuffer buffer;
	
	
	public OrganizadorBrent( String fileName ) {
		
		buffer = ByteBuffer.allocate( Aluno.LENGTH );
		file = new File( fileName );
		
		try {
			if( !file.exists() )
				file.createNewFile();
			
			rfIn = new RandomAccessFile( file, "r" );
			rfOut = new RandomAccessFile( file, "rw" );
			in = rfIn.getChannel();
			out = rfOut.getChannel();
		}
		catch( IOException e ) { e.printStackTrace(); }
	}

	@Override
	public Aluno getAluno( long matricula ) {
		
		long position = getAlunoPosition( matricula );
		
		if( position < 0 )
			return null;
		
		buffer.position( 0 );
		
		try { in.read( buffer, position ); }
		catch( IOException e ) { e.printStackTrace(); }
		
		return new Aluno( buffer );
	}
	
	private long getAlunoPosition( long matricula ) {
		
		long position = getNextPosition( matricula, -1, 0 ); // position é do arquivo, não confundir com o do buffer
		
		buffer.position( 0 );
		
		try {				
			while( in.read( buffer, position ) > -1 ) { // Se retornar -1, não existe dado na posição
				
				long mat = buffer.getLong( 0 );
				
				if( mat < 1 ) // Se mat == 0, então o registro é vazio, Se mat == -1, o registro foi excluído 
					break;
				
				if( matricula == mat )
					return position;
				
				position = getNextPosition( matricula, position, 0 );
				
				buffer.position( 0 );
			}
		}
		catch ( IOException e ) { e.printStackTrace();	}
		
		return -1;
	}
	
	private long getNextPosition( long matricula, long position, long saltos ) {
		
		long nxtPosition,
			 incremento = saltos > 0 ? inc( matricula ) *saltos : inc( matricula );
		
		if( position < 0 )
			nxtPosition = hash( matricula ) *( long )buffer.capacity(); // A multiplicação por capacity() é para evitar que um registro seja
		else{ 															// inserido no meio de outro
			saltos = saltos > -1 ? saltos : 0; 
			nxtPosition = position +( incremento *saltos *( long )buffer.capacity() );
		}
		
		if( nxtPosition > ( long )MAX_RECORDS *( long )buffer.capacity() ) {
			
			long diff = nxtPosition -( ( long )MAX_RECORDS *( long )buffer.capacity() );
			nxtPosition = diff;
		}
		
		return nxtPosition;
	}
	
	@Override
	public void addAluno( Aluno p ) {
		
		try {
			long matBuffer;
			
			buffer.position( 0 );
			long position = getNextPosition( p.getMatricula(), -1, 0 ),
				 matP = p.getMatricula();
			int qtdBytes = in.read( buffer, position );
			
			if( qtdBytes > -1 ) { // Verifica se existe registro na posição lida
				
				matBuffer = buffer.getLong( 0 );
				buffer.position( 0 );

				if( matBuffer > 0 ) { // Verifica se a posição lida não é de registro excluído
					
					if( matBuffer == matP ) // Verifica se não é tentativa de duplicata
						return;
					
					long custoP			= calculaCustoFuturoAcesso( p.getMatricula(), position ),
						 custoMatBuffer	= calculaCustoFuturoAcesso( matBuffer, position );
					
					if( custoP > custoMatBuffer )
						out.write( buffer, getNextPosition( matBuffer, position, custoMatBuffer -1 ) );
					else														 // É (custo -1) porque parte do custo já foi quitado com o cáculo da
						position = getNextPosition( matP, position, custoP -1 ); // primeira posição (a que já está ocupada)
				}
			}
							
			buffer = p.toByteBuffer();
			
			out.write( buffer, position );
		}
		catch( IOException e ) { e.printStackTrace(); }
	}

	private long calculaCustoFuturoAcesso( long matricula, long position ) {
		
		long qtdBytes = -1,
			 inc = inc( matricula ) * ( long )buffer.capacity();

		buffer.position( 0 );
		
		try { qtdBytes = in.read( buffer, position ); }
		catch ( IOException e ) { e.printStackTrace(); }
		
		if( qtdBytes < 0 || buffer.getLong( 0 ) < 1 ) // Verifica se existia algum registro não vazio naquela posição
			return 1L;
		
		return 1L + calculaCustoFuturoAcesso( matricula, position + inc );
	}
	
	@Override
	public Aluno delAluno( long matricula ) {
		
		Aluno aluno = getAluno( matricula );
		
		if( aluno != null ) {
			
			long position = getAlunoPosition( matricula );
			
			buffer.position( 0 );
			buffer.putLong( -1 );
			buffer.position( 0 );
			
			try { out.write( buffer, position ); }
			catch( IOException e ) { e.printStackTrace(); }
		}
		
		return aluno;
	}
	
	public void finish() {
		
		try {
			in.close();
			out.close();
			rfIn.close();
			rfOut.close();
		} 
		catch (IOException e) { e.printStackTrace(); }
	}
	
	public boolean hasDatabase() { return file.exists(); }
	
	private long hash( long chave ) { return chave % ( long )MAX_RECORDS; }
	
	private long inc( long chave ) { return ( chave % ( ( long )MAX_RECORDS -2L ) ) +1L; }
	
	public void moveDatabase( String newPath ) {
		
		file.renameTo( new File( newPath ) );
		file = new File( newPath );
		
		try {
			rfIn = new RandomAccessFile( file, "r" );
			rfOut = new RandomAccessFile( file, "rw" );
			in = rfIn.getChannel();
			out = rfOut.getChannel();
		}
		catch ( FileNotFoundException e ) { e.printStackTrace(); }
	}
}