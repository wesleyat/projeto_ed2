package comum;

/**
 * @author Wesley Alves Torres
 *
 */
public class Aluno implements Comparable<Object> {

	private long matric;
	private short curso;
	private static final int MAX_NOME_LEN     = 80,
			 				 MAX_ENDERECO_LEN = 100,
			 				 MAX_TELEFONE_LEN = 20,
			 				 MAX_EMAIL_LEN    = 90;
	private String nome,
				   endereco,
				   telefone,
				   email;
				   
	public Aluno() {}
	
	public Aluno( long matricula, short curso, String nome, String endereco ) {
		
		this.matric = matricula;
		this.curso = curso;
		
		if( nome.length() > MAX_NOME_LEN )
			this.nome = nome.substring( 0, MAX_NOME_LEN -1 );
		else
			this.nome = nome + new String( new char[MAX_NOME_LEN - nome.length()] );
		
		if( endereco.length() > MAX_ENDERECO_LEN )
			this.endereco = endereco.substring( 0, MAX_ENDERECO_LEN -1 );
		else
			this.endereco = endereco + new String( new char[MAX_ENDERECO_LEN - endereco.length()] );
	}
	
	public long getMatricula() { return matric; }
	
	public short getCurso() { return curso; }
	
	public String getNome() { return nome; }
	
	public String getEndereco() { return endereco; }
	
	public String getTelefone() { return telefone.isEmpty() ? "" : telefone; }
	
	public String getEmail() { return email.isEmpty() ? "" : email; }
	
	public void setCurso( short curso ) { this.curso = curso; }
	
	public void setNome( String nome ) {
		
		if( nome.length() > MAX_NOME_LEN )
			this.nome = nome.substring( 0, MAX_NOME_LEN -1 );
		else
			this.nome = nome + new String( new char[MAX_NOME_LEN - nome.length()] ); 
	}
	
	public void setEndereco( String endereco ) { 
		
		if( endereco.length() > MAX_ENDERECO_LEN )
			this.endereco = endereco.substring( 0, MAX_ENDERECO_LEN -1 );
		else
			this.endereco = endereco + new String( new char[MAX_ENDERECO_LEN - endereco.length()] ); 
	}
	
	public void setTelefone( String telefone ) {
		
		if( telefone.length() > MAX_TELEFONE_LEN )
			this.telefone = telefone.substring( 0, MAX_TELEFONE_LEN -1 );
		else
			this.telefone = telefone + new String( new char[MAX_TELEFONE_LEN - telefone.length()] ); 
	}
	
	public void setEmail( String email ) { 
		
		if( email.length() > MAX_EMAIL_LEN )
			this.email = email.substring( 0, MAX_EMAIL_LEN -1 );
		else
			this.email = email + new String( new char[MAX_EMAIL_LEN - email.length()] );
	}

	public boolean equals( Object outro ) {
		
		if( outro instanceof Aluno ) return matric == ( ( Aluno )outro ).matric;
		
		return false;
	}
	
	@Override
	public int compareTo( Object outro ) throws IllegalArgumentException {
		
		if( !equals( outro ) ) throw new IllegalArgumentException();
		
		if( matric < ( ( Aluno )outro ).matric ) return -1;
		
		if( matric > ( ( Aluno )outro ).matric ) return 1;
		
		return 0;
	}
}
