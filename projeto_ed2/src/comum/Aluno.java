package comum;

import java.nio.ByteBuffer;

/**
 * @author Wesley Alves Torres
 *
 */
public class Aluno implements Comparable<Object> {

	private long matric;
	private short curso;
	private String nome,
				   endereco,
				   telefone,
				   email;

	
	public Aluno( long matricula, String nome, String endereco, short curso ) {
		
		matric = matricula;
		this.nome = nome.trim();
		this.endereco = endereco.trim();
		this.curso = curso;
	}
	
	public Aluno( ByteBuffer buf ) {
		
		Aluno a = ConversorAluno.toAluno( buf );
		
		matric = a.matric;
		nome = a.nome;
		endereco = a.endereco;
		telefone = a.telefone;
		curso = a.curso;
		email = a.email;
	}
	
	public long getMatricula() { return matric; }
	
	public short getCurso() { return curso; }
	
	public String getNome() { return nome; }
	
	public String getEndereco() { return endereco; }
	
	public String getTelefone() { return telefone; }
	
	public String getEmail() { return email; }
	
	public void setCurso( short curso ) { this.curso = curso; }
	
	public void setNome( String nome ) { this.nome = nome.trim(); }
	
	public void setEndereco( String endereco ) { this.endereco = endereco.trim(); }
	
	public void setTelefone( String telefone ) { this.telefone = telefone.trim(); }
	
	public void setEmail( String email ) { this.email = email.trim(); }

	public boolean equals( Object outro ) {
		
		if( outro instanceof Aluno )
			return matric == ( ( Aluno )outro ).matric;
		
		return false;
	}
	
	@Override
	public int compareTo( Object outro ) throws IllegalArgumentException {
		
		if( !equals( outro ) )
			throw new IllegalArgumentException();
		
		if( matric < ( ( Aluno )outro ).matric ) return -1;
		
		if( matric > ( ( Aluno )outro ).matric ) return 1;
		
		return 0;
	}
	
	public ByteBuffer toByteByffer() { return ConversorAluno.toByteBuffer( this ); }
	
	public String toString() {
		
		return "Matrícula: " + matric + "; Nome: " + nome + "; Endereço: " + endereco + "\n" +
			   "Curso: " + curso + "; Telefone: " + telefone + "; E-mail: " + email;
	}
}