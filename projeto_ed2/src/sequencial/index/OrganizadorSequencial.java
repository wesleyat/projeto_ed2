package sequencial.index;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import comum.Aluno;
import comum.IFileOrganizer;

public class OrganizadorSequencial implements IFileOrganizer {

	File file;
	FileChannel channel;
	RandomAccessFile rf;
	ByteBuffer buffer;
	
	
	public OrganizadorSequencial( String fileName ) {
		
		file = new File( fileName );
		
		try {
			if( !file.exists() ) file.createNewFile();
		}
		catch( IOException e ) { e.printStackTrace(); }
	}
	
	@Override
	public void addAluno( Aluno p ) {

		long position = getAlunoPosition( p.getMatricula() ); // Esta vari�vel est� sempre associada � posi��o no channel
		
		if( position < 0 ) {

			buffer = p.toByteByffer();
			
			try {
				rf = new RandomAccessFile( file, "rw" );
				channel = rf.getChannel();

				long matricula = p.getMatricula();
				
				do {
					ByteBuffer buf = ByteBuffer.allocate( p.size() );
					channel.read( buf );
					position = channel.position();
					long mat = buf.getLong( 0 ); // Explicitando a posi��o do in�cio da leitura porque, neste momento, position est� no final do buffer
					
					if( mat > matricula ) {
						
						channel.write( buffer, position -buffer.capacity() ); // Escreve na posi��o do primeiro byte do registro
						matricula = buf.getLong( 0 ); // Atualiza a vari�vel matricula para a pr�xima compara��o
						buf.position( 0 );
						buffer = buf;
					}
				}
				while ( position < file.length() ); // Encerra o loop ao final do arquivo
				
				channel.write( buffer ); 
				channel.close();
				rf.close();
			}
			catch( IOException e ) { e.printStackTrace(); }
		}
	}

	
	@Override
	public Aluno getAluno( long matricula ) {
		
		long position = getAlunoPosition( matricula );
		
		if( position < 0 ) return null;
		
		return getAlunoByPosition( position );
	}

	
	@Override
	public Aluno delAluno( long matricula ) {
		
		long position = getAlunoPosition( matricula );
		Aluno aluno = null;
		
		if( position > -1 ) {
		
			aluno = getAlunoByPosition( position );
			
			try {
				rf = new RandomAccessFile( file, "rw" );
				channel = rf.getChannel();
				channel.position( position );
				
				do {
					buffer = ByteBuffer.allocate( aluno.size() );
					
					if( channel.read( buffer, position +buffer.capacity() ) < 0 ) // L� o pr�ximo registro, apartir do seu primeiro byte, sem alterar channel.position
						break;
					
					buffer.position( 0 ); // N�o usei buffer.flip() porque ele pode mudar o tamanho do registro conforme encontre espa�os em branco nos cmapos
					
					channel.write( buffer ); // Sobrescreve o conte�do de channel.position, avan�ando para o primeiro byte do pr�ximo registro
					position = channel.position();
				}
				while ( position < file.length() ); // Encerra o loop ao final do arquivo
				
				channel.truncate( position ); // Remove o �ltimo registro, que � igual ao pen�ltimo.
				channel.close();
				rf.close();
			}
			catch( IOException e ) { e.printStackTrace(); }
		}
		
		return aluno;
	}

	public long getAlunoPosition( long matricula ) {
		
		long pos = -1;
		
		try {
			rf = new RandomAccessFile( file, "r" );
			channel = rf.getChannel();
		
			do {
				pos = drainChannel() -buffer.capacity(); // � a posi��o do primeiro byte do registro
				long mat = buffer.getLong();
				
				if( matricula == mat )
					break;
			}
			while( pos < file.length() );
			
			channel.close();
			rf.close();
		}
		catch( IOException e ) { e.printStackTrace(); }
		
		return pos;
	}
	
	public Aluno getAlunoByPosition( long position ) {

		Aluno aluno = null;
		
		try {
			rf = new RandomAccessFile( file, "r" );
			channel = rf.getChannel();
			
			channel.position( position * buffer.capacity() ); // Permite usar valores unit�rios para position, mas deslocar-se pelo arquivo de registro em registro
			drainChannel();
			channel.close();
			rf.close();
		}
		catch( IOException e ) { e.printStackTrace(); }
		
		aluno = new Aluno( buffer );
		
		return aluno;
	}
	
	public boolean hasDatabase() { return file.exists(); }
	
	public void moveDatabase( String newPath ) {
		
		file.renameTo( new File( newPath ) );
		file = new File( newPath );
	}
	
	private long drainChannel() throws IOException {
		
		buffer.position( 0 );
		channel.read( buffer );
		long pos = channel.position();
		buffer.position( 0 );
		
		return pos; // Retorna a posi��o do in�cio do pr�ximo registro
	}
}