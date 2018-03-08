/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package comum;

/**
 *
 * @author Wesley Alves Torres
 */
public interface TamanhoAluno {
	
    public static final int LENGTH_MATRIC	= 8;
    public static final int LENGTH_NOME		= 80;
    public static final int LENGTH_ENDER	= 100;
    public static final int LENGTH_FONE		= 20;
    public static final int LENGTH_CURSO	= 2;
    public static final int LENGTH_MAIL		= 90;
    public static final int LENGTH_ALUNO	= LENGTH_MATRIC + LENGTH_NOME + LENGTH_ENDER + LENGTH_FONE + LENGTH_CURSO + LENGTH_MAIL;
}
