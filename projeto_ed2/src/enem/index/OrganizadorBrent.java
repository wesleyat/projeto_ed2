package enem.index;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import comum.Aluno;
import comum.IFileOrganizer;

public class OrganizadorBrent implements IFileOrganizer {
	
	private static int BUFF_SIZE = 300;
	private static int NUM_OF_REC = 10000019;
	
	private File file;
	private FileChannel channel;
	private RandomAccessFile rf;
	private ByteBuffer buffer = ByteBuffer.allocate( BUFF_SIZE );
	
	public OrganizadorBrent( String fileName ) {
		
		file = new File( fileName );
		buffer = ByteBuffer.allocate( BUFF_SIZE );
		
		try {
			if( !file.exists() )
				file.createNewFile();
		}
		catch( IOException e ) { e.printStackTrace(); }
	}

	@Override
	public void addAluno( Aluno p ) {
		
		int position = getAlunoPosition( p.getMatricula() );
		
		if( position < 0 ) { // Verifica se o registro já não existe
			
			try {
				rf = new RandomAccessFile( file, "rw" );
				channel = rf.getChannel();
				position = hash( p.getMatricula() );
				buffer.position( 0 );
				int qtdBytes = channel.read( buffer, position );
				
				if( qtdBytes > -1 ) { // Verifica se existe registro na posição lida
					
					long matBuffer = buffer.getLong( 0 );

					if( matBuffer > 0 ) { // Verifica se a posição lida não é de registro excluído
						
						int custoP			= calculaCustoAcesso( p.getMatricula(), position ),
							custoMatBuffer	= calculaCustoAcesso( matBuffer, position );
						
						if( custoP > custoMatBuffer ) {
							
							buffer.position( 0 );
							channel.write( buffer, position + ( custoMatBuffer - 1) );
						}
						else
							position += ( custoP - 1 );
					}
				}
								
				buffer.position( 0 );
				buffer.putLong( p.getMatricula() );
				buffer.putShort( p.getCurso() );
				buffer.put( p.getNome().getBytes() );
				buffer.put( p.getEndereco().getBytes() );
				buffer.put( p.getTelefone().getBytes() );
				buffer.put( p.getEmail().getBytes() );
				buffer.position( 0 );
				
				channel.write( buffer, position );
				channel.close();
				rf.close();
			}
			catch( IOException e ) { e.printStackTrace(); }
		}
	}

	@Override
	public Aluno getAluno( long matricula ) {
		
		int position = getAlunoPosition( matricula );
		
		if( position < 0 )
			return null;
		
		byte[] nome = new byte[80];
		byte[] endereco = new byte[100];
		byte[] telefone = new byte[20];
		byte[] email = new byte[90];
		
		try {
			rf = new RandomAccessFile( file, "r" );
			channel = rf.getChannel();
			
			buffer.position( 0 );
			channel.read( buffer, position );
			channel.close();
			rf.close();
		}
		catch( IOException e ) { e.printStackTrace(); }
		
		buffer.position( 8 ); // Pulando o campo da matrícula
		short curso = buffer.getShort();
		buffer.get( nome );
		buffer.get( endereco );
		buffer.get( telefone );
		buffer.get( email );
		
		Aluno aluno = new Aluno( matricula, new String( nome ), new String ( endereco ), curso );
		aluno.setEmail( new String( email ) );
		aluno.setTelefone( new String( telefone ) );
		
		return aluno;
	}

	@Override
	public Aluno delAluno( long matricula ) {
		
		Aluno aluno = getAluno( matricula );
		
		if( aluno != null ) {
			
			int position = getAlunoPosition( matricula );
			
			try {
				rf = new RandomAccessFile( file, "rw" );
				channel = rf.getChannel();
				
				buffer = ByteBuffer.allocate( BUFF_SIZE );
				buffer.putLong( -1 );
				channel.write( buffer, position );
				channel.close();
				rf.close();
			}
			catch( IOException e ) { e.printStackTrace(); }
		}
		
		return aluno;
	}
	
	private int hash( long chave ) { return ( int )chave % NUM_OF_REC; }
	
	private int inc( long chave ) { return ( ( int )chave % ( NUM_OF_REC -2 ) ) +1; }
	
	private int getAlunoPosition( long matricula ) {
		
		int position = hash( matricula ), // position é do arquivo, não confundir com o do buffer
			incremento = inc( matricula ); // A multiplicação por BUFF_SIZE é para evitar que um registro seja inserido no meio de outro
		
		try {
			rf = new RandomAccessFile( file, "r" );
			channel = rf.getChannel();
			buffer.position( 0 );
			channel.read( buffer, position );
				
			do {
				long mat;							// Não tenho como garantir que o meu hash não vai parar no meio de um registro existente,
				try { mat = buffer.getLong( 0 ); }  // então, se não conseguir ler um long, apenas parto para a próxima posição.
				catch( NullPointerException e ) { continue; }
				
				if( matricula == mat )
					return position;
				
				position += incremento; 
			}
			while( channel.read( buffer, position ) > 0 ); // Se retornar um negativo, não existia dado
														   // Se retornar zero, o dado era vazio
			channel.close();
			rf.close();
		}
		catch ( IOException e ) { e.printStackTrace();	}
		
		return -1;
	}
	
	private int calculaCustoAcesso( long matricula, int position ) {
		
		int qtdBytes = -1,
			inc = inc( matricula );
		
		try {
			rf = new RandomAccessFile( file, "r" );
			channel = rf.getChannel();
			buffer.position( 0 );
			qtdBytes = channel.read( buffer, position );
			channel.close();
			rf.close();
		} 
		catch ( IOException e ) { e.printStackTrace(); }
		
		if( qtdBytes < 0 || buffer.getLong( 0 ) < 0 ) // Verifica se existia algum registro não vazio naquela posição
			return 2;
		
		return 1 + calculaCustoAcesso( matricula, position + inc );
	}
	
	public boolean hasDatabase() { return file.exists(); }
	
	public void moveDatabase( String newPath ) {
		
		file.renameTo( new File( newPath ) );
		file = new File( newPath );
	}
}