package comum;

/**
 * @author Wesley Alves Torres
 *
 */
public class Aluno implements Comparable<Object> {

	private long matric;
	private short curso;
	private String nome = new String( new char[40] ),
				   endereco = new String( new char[50] ),
				   telefone = new String( new char[10] ),
				   email = new String( new char[45] );
				   
	public Aluno() {}
	
	public Aluno( long matricula, short curso, String nome, String endereco ) {
		
		this.matric = matricula;
		this.curso = curso;
		this.nome = nome;
		this.endereco = endereco;
	}
	
	public long getMatricula() { return matric; }
	
	public short getCurso() { return curso; }
	
	public String getNome() { return nome; }
	
	public String getEndereco() { return endereco; }
	
	public String getTelefone() { return telefone.isEmpty() ? "" : telefone; }
	
	public String getEmail() { return email.isEmpty() ? "" : email; }
	
	public void setCurso( short curso ) { this.curso = curso; }
	
	public void setNome( String nome ) { this.nome = nome; }
	
	public void setEndereco( String endereco ) { this.endereco = endereco; }
	
	public void setTelefone( String telefone ) { this.telefone = telefone; }
	
	public void setEmail( String email ) { this.email = email; }

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
