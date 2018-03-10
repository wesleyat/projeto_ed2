/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package comum;

import java.nio.ByteBuffer;

/**
 *
 * @author rafion
 * Atualizado por Wesley Alves Torres
 */
class ConversorAluno {

    static ByteBuffer toByteBuffer( Aluno a ) {
    	
        // O tamanho do buffer será sempre igual ao tamanho, em bytes, de todos os campos
    	// da classe Aluno somados
        ByteBuffer buf = ByteBuffer.allocate( TamanhoAluno.LENGTH );

        buf.putLong( a.getMatricula() );
        
        buf.put( a.getNome().getBytes() );
        buf.position( TamanhoAluno.LENGTH_MATRIC + TamanhoAluno.LENGTH_NOME );
        
        buf.put( a.getEndereco().getBytes() );
        buf.position( TamanhoAluno.LENGTH_MATRIC + TamanhoAluno.LENGTH_NOME + TamanhoAluno.LENGTH_ENDER );
        
        buf.put( a.getTelefone().getBytes() );
        buf.position( TamanhoAluno.LENGTH_MATRIC + TamanhoAluno.LENGTH_NOME + TamanhoAluno.LENGTH_ENDER + TamanhoAluno.LENGTH_FONE );
        
        buf.putShort( a.getCurso() );
        
        buf.put( a.getEmail().getBytes() );
        
        buf.position( TamanhoAluno.LENGTH );
        buf.flip();

        return buf;

    }

    static Aluno toAluno( ByteBuffer buf ) {

    	buf.position( 0 );
        long matricula = buf.getLong();

        byte[] b_nome = new byte[TamanhoAluno.LENGTH_NOME];
        buf.get( b_nome );
        String nome = new String( b_nome );

        byte[] b_endereco = new byte[TamanhoAluno.LENGTH_ENDER];
        buf.get( b_endereco );
        String endereco = new String( b_endereco );

        byte[] b_telefone = new byte[TamanhoAluno.LENGTH_FONE];
        buf.get( b_telefone );
        String telefone = new String( b_telefone );

        short curso = buf.getShort();

        byte[] b_email = new byte[TamanhoAluno.LENGTH_MAIL];
        buf.get( b_email );
        String email = new String( b_email );

        Aluno a = new Aluno( matricula, nome, endereco, curso );
        a.setTelefone( telefone );
        a.setEmail( email );

        return a;
    }
}