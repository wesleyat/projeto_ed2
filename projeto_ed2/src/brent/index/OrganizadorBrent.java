package brent.index;

import java.io.File;
import java.io.FileNotFoundException;
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public Aluno getAluno( long matric ) {
		
		Aluno aluno = null;
		
		
		
		byte[] nome = new byte[80];
		byte[] endereco = new byte[100];
		byte[] telefone = new byte[20];
		byte[] email = new byte[90];
		
		long mat = buffer.getLong();
		short curso = buffer.getShort();
		buffer.get( nome );
		buffer.get( endereco );
		buffer.get( telefone );
		buffer.get( email );
		
		return null;
	}

	@Override
	public Aluno delAluno( long matric ) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private int hash( long chave ) { return ( int )chave % NUM_OF_REC; }
	
	private int inc( long chave ) { return ( ( int )chave % ( NUM_OF_REC -2 ) ) +1; }
	
	private int getAlunoPosition( int matric ) {
		
		int position = hash( matric );
		
		try {
			rf = new RandomAccessFile( file, "r" );
			channel = rf.getChannel();
			buffer.position( 0 );
			
			if( channel.read( buffer, position ) > -1 ) {
				
				long mat = buffer.getLong( 0 );
				
				if( matric == mat )
					return position;
				
				// TODO : implementar a busca pelo menor esforço
			}
		}
		catch ( IOException e ) { e.printStackTrace();	}
		
		return -1;
	}
}