package sequencial.index;

import java.io.File;
import java.util.Scanner;

import comum.Aluno;

public class Main {

	private static final String DB_FILE_NAME = "alunos.db";
	private static final int MAX_NOME_LEN     = 39, // 40 - 1 caracteres
							 MAX_ENDERECO_LEN = 49, // 50 - 1 caracteres
							 MAX_TELEFONE_LEN = 9,  // 10 - 1 caracteres
							 MAX_EMAIL_LEN    = 44; // 45 - 1 caracteres
	private static OrganizadorSequencial org;
	private static String dbPath,
						  dbFullPath;
	
	
	public static void main( String[] args ) {
		
		int opcao = -1;
		dbPath = System.getProperty( "user.home" );
		dbFullPath = dbPath + File.separator + DB_FILE_NAME;
		org = new OrganizadorSequencial( dbFullPath );
		Aluno aluno;
		Scanner scan = new Scanner( System.in );
		
		while( opcao != 6 ) {
			
			long matricula;
			aluno = null;
			printMenu();
			opcao = scan.nextInt();
			
			switch( opcao ) {
			case 1:
				System.out.println(
						"\n================== CONSULTAR DADOS DO ALUNO ===================\n" +
						"Informe a matrícula: "
					);
				matricula = scan.nextLong();
				
				aluno = org.getAluno( matricula );
				
				if( aluno == null )
					System.out.println( "Aluno não encontrado!" );
				else {
					System.out.println(
							"Curso: " + aluno.getCurso() + "\n" +
							"Nome: " + aluno.getNome() + "\n" +
							"Endereço: " + aluno.getEndereco() + "\n" +
							"Telefone: " + aluno.getTelefone() + "\n" +
							"E-mail: " + aluno.getEmail() + "\n"
						);
				}
				
				
				break;
			case 2:
				System.out.println(
						"\n===================== NOVO ALUNO =====================\n" +
						"Matrícula: " 
					);
				matricula = scan.nextLong();
				System.out.print( "Curso: " );
				short curso = scan.nextShort();
				System.out.print( "Nome (até 40 caracteres): " );
				scan.nextLine();
				String nome = scan.nextLine();
				nome = nome.substring( 0, nome.length() > MAX_NOME_LEN ? MAX_NOME_LEN : nome.length() );
				System.out.print( "Endereço (até 50 caracteres): " );
				//scan.nextLine();
				String endereco = scan.nextLine();
				endereco = endereco.substring( 0, endereco.length() > MAX_ENDERECO_LEN ? MAX_ENDERECO_LEN : endereco.length() );
				System.out.print( "Telefone (até 10 caracteres): " );
				//scan.nextLine();
				String telefone = scan.nextLine();
				telefone = telefone.substring( 0, telefone.length() > MAX_TELEFONE_LEN ? MAX_TELEFONE_LEN : telefone.length() );
				System.out.print( "E-mail (até 45 caracteres): " );
				//scan.nextLine();
				String email = scan.nextLine();
				email = email.substring( 0, email.length() > MAX_EMAIL_LEN ? MAX_EMAIL_LEN : email.length() );
				
				aluno = new Aluno( matricula, curso, nome, endereco );
				aluno.setEmail( email );
				aluno.setTelefone( telefone );
				
				org.addAluno( aluno );
				break;
			case 3:
				System.out.println(
						"\n===================== EXCLUIR ALUNO =====================\n" +
						"Matrícula: "
					);
				matricula = scan.nextLong();
				aluno = org.delAluno( matricula );
				
				if( aluno == null)
					System.out.println( "Aluno não encontrado!" );
				else
					System.out.println( "Aluno " + aluno.getNome() + "removido com sucesso!" );
				break;
			case 4:
				System.out.print( "\nInforme o novo diretório: " );
				scan.nextLine();
				setDBPath( scan.nextLine() );
				break;
			case 5:
				System.out.println( "Registros salvos em " + getDBFullPath() );
			}
		}
		
		scan.close();
	}
	
	private static String getDBPath() { return dbPath;	}
	
	private static String getDBFullPath() { return dbFullPath; }
	
	private static void setDBPath( String newPath ) {
		
		dbPath = newPath;
		dbFullPath = dbPath + File.separator + DB_FILE_NAME;
		org.moveDatabase( dbFullPath );
	}
	
	private static void printMenu() {
		
		System.out.println(
				"======================= MENU ====================\n" +
				"1 - CONSULTAR ALUNO\n" +
				"2 - ADICIONAR ALUNO\n" +
				"3 - EXCLUIR ALUNO\n" +
				"4 - ALTERAR LOCAL DE ARMAZENAMENTO\n" +
				"5 - CONFERIR LOCAL DA DATABASE\n" +
				"6 - SAIR"
			);
	}
}
