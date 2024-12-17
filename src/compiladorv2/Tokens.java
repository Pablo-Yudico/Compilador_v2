package compiladorv2;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */

/**
 *
 * @author Omen
 */
public enum Tokens {
    id, //0 id de variable
    num, //1 numero entero
    tint, //2 int
    tfloat, //3 float
    tchar, //4 char
    coma, //5 ,
    pcoma, //6 ; 
    sum, //7 +
    res, //8 -
    mul, //9 *
    div, //10 /
    p_a, //11 (
    p_c, //12 ) 
    igual, //13 =
    terminador, //14 El inicio y fin marcado por el $
}
