package comparador;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import comum.Aluno;
import enem.index.OrganizadorBrent;
import sequencial.index.OrganizadorSequencial;

public class TestaLeitura {
	
	private OrganizadorSequencial sequencial;
	private OrganizadorBrent brent;
	
	public TestaLeitura() {
		
		long total_brent = 0,
			 total_seq	 = 0;
		String dbPath = System.getProperty( "user.home" ) + File.separator;
		File fSelected = new File( dbPath + "selected.db" );
		
		try {
			RandomAccessFile rf = new RandomAccessFile( fSelected, "r" );
			FileChannel in = rf.getChannel();
			
			sequencial = new OrganizadorSequencial( dbPath + "enem_aleat.db" );
			brent = new OrganizadorBrent( dbPath + "enem_brent.db" );
	
			for( int i = 0; i < fSelected.length(); i+= 8 ) {
				
				ByteBuffer buf = ByteBuffer.allocate( 8 );
				in.read( buf );
				long mat = buf.getLong( 0 );
				
				long inicio = System.currentTimeMillis();
				@SuppressWarnings("unused")
				Aluno aluno = sequencial.getAluno( mat );
				long fim = System.currentTimeMillis();
				total_seq += fim -inicio;
				
				System.out.println( "Laço: " + ( i / 8 ) );
				
				if( aluno  != null )
					System.out.println( "Aluno: " + mat  );
				else
					System.out.println( "Aluno: " + mat  + " não encontrado" );
				
				inicio = System.currentTimeMillis();
				brent.getAluno( mat );
				fim = System.currentTimeMillis();
				total_brent += fim -inicio;
			}
			
			brent.finish();
			sequencial.finish();
	
			in.close();
			rf.close();
		}
		catch( IOException e ) { e.printStackTrace(); }
		
		System.out.println( "Sequencial: " + total_seq + "\nBrent: " + total_brent );
	}

	public static void main( String[] args ) { new TestaLeitura();	}

}
