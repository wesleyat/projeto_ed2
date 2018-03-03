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
	ByteBuffer buffer = ByteBuffer.allocate( 300 );
	
	
	public OrganizadorSequencial( String fileName ) {
		
		file = new File( fileName );
		
		try {
			if( !file.exists() ) file.createNewFile();
		}
		catch( IOException e ) { e.printStackTrace(); }
	}
	
	@Override
	public void addAluno( Aluno p ) {

		long position = getAlunoPosition( p.getMatricula() );
		
		if( position == -1 ) {
			
			buffer.putLong( p.getMatricula() );
			buffer.putShort( p.getCurso() );
			buffer.put( p.getNome().getBytes() );
			buffer.put( p.getEndereco().getBytes() );
			buffer.put( p.getTelefone().getBytes() );
			buffer.put( p.getEmail().getBytes() );
			buffer.flip();
			
			try {
				rf = new RandomAccessFile( file, "rw" );
				channel = rf.getChannel();
				channel.position( 0 );
				ByteBuffer buf = ByteBuffer.allocate( 300 );
				long mat;
				boolean fim;
				
				// Lê o primeiro registro com matrícula maior que a matrícula a ser inserida (position no registro sucessor), ou ao final do arquivo
				do {
					fim = channel.read( buf ) == -1; // Encerra o loop ao final do arquivo
					mat = buf.getLong();
				}
				while ( mat < p.getMatricula() | !fim );
				
				do {
					channel.write( buffer, ( channel.position() - 1 ) ); // É (position - 1) porque position é incrementado assim que o buffer é lido
					buffer = buf;
					fim = channel.read( buf ) == -1;
				}
				while( !fim );
				
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
		
		if( position == -1 ) return null;
		
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
				
				channel.position( position + 1 ); // Posiciona o channel na posição sucessora a do registro encontrado
				
				while( channel.read( buffer ) > -1 ) { // Repete enquanto não chegar ao final do arquivo
					
					channel.position( position - 2 ); // Voltando para a posição do registro encontrado para sobrescrevê-lo
					
					channel.write( buffer );
					channel.position( position + 1 ); // Quando escreve, position avança para o próximo registro, que é o mesmo que acabou 
				}									  // de ser escrito. Então deve-se avançar mais um registro para ler um novo registro.
				
				channel.truncate( channel.position() - 1 ); // Remove o último registro, que é igual ao penúltimo.
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
				pos = channel.read( buffer );
				long mat = buffer.getLong();
				
				if( matricula == mat ) break;
			}
			while( pos > -1 );
			
			channel.close();
			rf.close();
		}
		catch( IOException e ) { e.printStackTrace(); }
		
		return pos;
	}
	
	public Aluno getAlunoByPosition( long position ) {

		try {
			rf = new RandomAccessFile( file, "r" );
			channel = rf.getChannel();
			
			channel.position( position );
			channel.read( buffer );
			channel.close();
			rf.close();
		}
		catch( IOException e ) { e.printStackTrace(); }
		
		long matricula = buffer.getLong();
		short curso = buffer.getShort();
		byte[] nome = new byte[80];
		byte[] endereco = new byte[100];
		byte[] telefone = new byte[20];
		byte[] email = new byte[90];
		
		buffer.get( nome );
		if( buffer.remaining() == 20 || buffer.remaining() == 110 )
			buffer.get( endereco );
		
		if( buffer.remaining() == 90 )buffer.get( telefone );
		
		Aluno aluno = new Aluno( matricula,curso, new String( nome ), new String( endereco ) );
		aluno.setEmail( new String( email ) );
		aluno.setTelefone( new String( telefone ) );
		
		return aluno;
	}
	
	public boolean hasDatabase() { return file.exists(); }
	
	public void moveDatabase( String newPath ) {
		
		file.renameTo( new File( newPath ) );
		file = new File( newPath );
	}
	
	/*
	public void ManipuladorSequencial( String fileName, String mode ) {
		
		try {
			File file = new File( fileName );
			RandomAccessFile rf = new RandomAccessFile( file, mode );
			channel = rf.getChannel();
		}
		catch( FileNotFoundException e ) {
			
			e.printStackTrace();
		}
	}
	*/
}