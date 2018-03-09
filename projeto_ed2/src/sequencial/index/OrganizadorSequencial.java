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

		long position = getAlunoPosition( p.getMatricula() ); // Esta variável está sempre associada à posição no channel
		
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
					long mat = buf.getLong( 0 ); // Explicitando a posição do início da leitura porque, neste momento, position está no final do buffer
					
					if( mat > matricula ) {
						
						channel.write( buffer, position -buffer.capacity() ); // Escreve na posição do primeiro byte do registro
						matricula = buf.getLong( 0 ); // Atualiza a variável matricula para a próxima comparação
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
					
					if( channel.read( buffer, position +buffer.capacity() ) < 0 ) // Lê o próximo registro, apartir do seu primeiro byte, sem alterar channel.position
						break;
					
					buffer.position( 0 ); // Não usei buffer.flip() porque ele pode mudar o tamanho do registro conforme encontre espaços em branco nos cmapos
					
					channel.write( buffer ); // Sobrescreve o conteúdo de channel.position, avançando para o primeiro byte do próximo registro
					position = channel.position();
				}
				while ( position < file.length() ); // Encerra o loop ao final do arquivo
				
				channel.truncate( position ); // Remove o último registro, que é igual ao penúltimo.
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
				pos = drainChannel() -buffer.capacity(); // É a posição do primeiro byte do registro
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
			
			channel.position( position * buffer.capacity() ); // Permite usar valores unitários para position, mas deslocar-se pelo arquivo de registro em registro
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
		
		return pos; // Retorna a posição do início do próximo registro
	}
}