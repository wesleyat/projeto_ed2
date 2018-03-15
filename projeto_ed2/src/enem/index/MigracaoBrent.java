package enem.index;

import java.io.File;

import comum.Aluno;
import sequencial.index.OrganizadorSequencial;

public class MigracaoBrent {
	
	private OrganizadorSequencial sequencial;
	private OrganizadorBrent brent;
	
	public MigracaoBrent() {
		
		long total_brent = 0,
			 total_seq	 = 0;
		String dbPath = System.getProperty( "user.home" ) + File.separator;
		
		sequencial = new OrganizadorSequencial( dbPath + "enem_aleat.db" );
		brent = new OrganizadorBrent( dbPath + "enem_brent.db" );

		for( int i = 0; i < 7603290; i++ ) {			
			
			long inicio = System.currentTimeMillis();
			Aluno aluno = sequencial.getAlunoByPosition( i );
			long fim = System.currentTimeMillis();
			total_seq += fim -inicio;
			
			System.out.println( "Laço: " + i + "\nAluno: " + aluno.getMatricula() );
			
			inicio = System.currentTimeMillis();
			brent.addAluno( aluno );
			fim = System.currentTimeMillis();
			total_brent += fim -inicio;
		}
		
		brent.finish();
		sequencial.finish();
		
		System.out.println( "Tempo de leitura: " + total_seq + "\nTempo de escrita: " + total_brent );
	}

	public static void main( String[] args ) { new MigracaoBrent(); }
}