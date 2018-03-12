package sequencial.index;

import java.io.IOException;
import java.nio.ByteBuffer;

import comum.Aluno;
import comum.GenericOrganizador;
import comum.IFileOrganizer;

public class OrganizadorSequencial extends GenericOrganizador implements IFileOrganizer {
	
	public OrganizadorSequencial( String fileName ) { super( fileName ); }
	
	@Override
	public void addAluno( Aluno p ) {

		long position = getAlunoPosition( p.getMatricula() ); // Esta vari�vel est� sempre associada � posi��o no channel
		
		if( position < 0 ) {

			buffer = p.toByteBuffer();
			
			try {
				long matricula = p.getMatricula();
				
				do {
					ByteBuffer buf = ByteBuffer.allocate( Aluno.LENGTH );
					in.read( buf );
					position = in.position();
					long mat = buf.getLong( 0 ); // Explicitando a posi��o do in�cio da leitura porque, neste momento, position est� no final do buffer
					
					if( mat > matricula ) {
						
						out.write( buffer, position -buffer.capacity() ); // Escreve na posi��o do primeiro byte do registro
						matricula = mat; // Atualiza a vari�vel matricula para a pr�xima compara��o
						buf.flip();
						buffer = buf;
					}
				}
				while ( position < file.length() ); // Encerra o loop ao final do arquivo
				
				out.write( buffer ); 
				in.close();
				rfIn.close();
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
				in.position( position * buffer.capacity() );
				buffer.position( 0 );
				
				while( in.read( buffer, position +buffer.capacity() ) > -1 ) { // L� o pr�ximo registro, apartir do seu primeiro byte, sem alterar channel.position
				
					buffer.flip();
					out.write( buffer ); // Sobrescreve o conte�do de channel.position, avan�ando para o primeiro byte do pr�ximo registro
					buffer.flip();
					position = in.position();
				}
				
				in.truncate( position ); // Remove o �ltimo registro, que � igual ao pen�ltimo.
			}
			catch( IOException e ) { e.printStackTrace(); }
		}
		
		return aluno;
	}
	
	public long getAlunoPosition( long matricula ) {
		
		long pos = -1;
		long mat;
		
		try {
			do {
				buffer.position( 0 );
				in.read( buffer );
				mat = buffer.getLong( 0 );
				
				//if( mat > matricula ) // // Otimizando: como o arquivo � ordenado em ordem crescente, se mat > matricula n�o precisa mais procurar
				//	break;
				
				if( matricula == mat ) {
				
					pos = ( in.position() -( long )buffer.capacity() ) / ( long )buffer.capacity();
					break;
				}
			}
			while( mat > 0 );
		}
		catch( IOException e ) { e.printStackTrace(); }
		
		return pos;
	}
}