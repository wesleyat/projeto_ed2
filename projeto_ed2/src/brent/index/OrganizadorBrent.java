package brent.index;

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
	public void addAluno(Aluno p) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Aluno getAluno(long matric) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Aluno delAluno(long matric) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private int hash( long chave ) { return ( int )chave % NUM_OF_REC; }
	
	private int inc( long chave ) { return ( ( int )chave % ( NUM_OF_REC -2 ) ) +1; }
}