package enem.index;

import java.io.File;

import comum.Aluno;
import sequencial.index.OrganizadorSequencial;

public class MigracaoBrent {
	
	OrganizadorSequencial sequencial;
	OrganizadorBrent brent;
	
	public MigracaoBrent() {
		
		String dbPath = System.getProperty( "user.home" ) + File.separator;
		
		sequencial = new OrganizadorSequencial( dbPath + "enem_aleat.db" );
		brent = new OrganizadorBrent( dbPath + "enem_brent.db" );

		for( int i = 0; i < 7603290; i++ ) {
			
			Aluno aluno = sequencial.getAlunoByPosition( i );
			brent.addAluno( aluno );
		}
	}

	public static void main( String[] args ) { new MigracaoBrent(); }
}