package comum;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class GenericOrganizador {
	
	protected static int MAX_RECORDS = 10000019;

	protected File file;
	protected FileChannel in,
				out;
	protected RandomAccessFile rfIn,
							 rfOut;
	protected ByteBuffer buffer;
	
	
	protected GenericOrganizador( String fileName ) {
		
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
	
	public Aluno getAlunoByPosition( long position ) {

		Aluno aluno = null;
		buffer.position( 0 );
		
		// Permite usar valores unitários para position, mas deslocar-se pelo arquivo de registro em registro
		try { in.read( buffer, position * buffer.capacity() ); }
		catch( IOException e ) { e.printStackTrace(); }
		
		aluno = new Aluno( buffer );
		
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