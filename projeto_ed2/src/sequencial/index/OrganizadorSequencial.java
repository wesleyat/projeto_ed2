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

			ByteBuffer buffer = p.toByteBuffer();
			
			try {
				rf = new RandomAccessFile( file, "rw" );
				channel = rf.getChannel();

				long matricula = p.getMatricula();
				
				do {
					ByteBuffer buf = ByteBuffer.allocate( Aluno.LENGTH );
					channel.read( buf );
					position = channel.position();
					long mat = buf.getLong( 0 ); // Explicitando a posição do início da leitura porque, neste momento, position está no final do buffer
					
					if( mat > matricula ) {
						
						channel.write( buffer, position -buffer.capacity() ); // Escreve na posição do primeiro byte do registro
						matricula = mat; // Atualiza a variável matricula para a próxima comparação
						buf.flip();
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
				channel.position( position * Aluno.LENGTH );
				ByteBuffer buffer = ByteBuffer.allocate( Aluno.LENGTH );
				
				while( channel.read( buffer, position +buffer.capacity() ) > -1 ) { // Lê o próximo registro, apartir do seu primeiro byte, sem alterar channel.position
				
					buffer.flip();
					channel.write( buffer ); // Sobrescreve o conteúdo de channel.position, avançando para o primeiro byte do próximo registro
					buffer.flip();
					position = channel.position();
				}
				
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
			long mat;
		
			do {
				ByteBuffer buffer = ByteBuffer.allocate( Aluno.LENGTH );
				channel.read( buffer );
				mat = buffer.getLong( 0 );
				
				//if( mat > matricula ) // // Otimizando: como o arquivo é ordenado em ordem crescente, se mat > matricula não precisa mais procurar
				//	break;
				
				if( matricula == mat ) {
				
					pos = ( channel.position() -( long )buffer.capacity() ) / ( long )buffer.capacity();
					break;
				}
			}
			while( mat > 0 );
			
			channel.close();
			rf.close();
		}
		catch( IOException e ) { e.printStackTrace(); }
		
		return pos;
	}
	
	public Aluno getAlunoByPosition( long position ) {

		Aluno aluno = null;
		ByteBuffer buffer = ByteBuffer.allocate( Aluno.LENGTH );
		
		try {
			rf = new RandomAccessFile( file, "r" );
			channel = rf.getChannel();
			
			channel.position( position * buffer.capacity() ); // Permite usar valores unitários para position, mas deslocar-se pelo arquivo de registro em registro
			channel.read( buffer );
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
}