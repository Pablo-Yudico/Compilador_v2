package compiladorv2;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Omen
 */
public class Token {
    
    private int tipo;
    private String lexema;
    private int linea;
    

    public Token( int tipo, String lexema, int linea) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.linea = linea;
    }
    
    public int Tipo()
    {
        return tipo;
    }
    
    public String Lexema()
    {
        return lexema;
    }
    
    public int Linea()
    {
        return linea;
    }
}
