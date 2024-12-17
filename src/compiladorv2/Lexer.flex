package compiladorv2;
%%
%class Lexer //El nombre que le pondré a la clase
%type Token //El tipo de objeto que quiero regresar
L=[a-zA-Z_]
D=[0-9]+
espacio=[ \t\r]
salto=[\n]
%{
    public int linea = 1;
%}
%%
{espacio} { /* ignora espacios en blanco */ }
{salto} {linea++;}

"int" { return new Token(Tokens.tint.ordinal(), yytext(), linea); }
"float" { return new Token(Tokens.tfloat.ordinal(), yytext(), linea); }
"char" { return new Token(Tokens.tchar.ordinal(), yytext(), linea); }
"=" { return new Token(Tokens.igual.ordinal(), yytext(), linea); }
"+" { return new Token(Tokens.sum.ordinal(), yytext(), linea); }
"-" { return new Token(Tokens.res.ordinal(), yytext(), linea); }
"*" { return new Token(Tokens.mul.ordinal(), yytext(), linea); }
"/" { return new Token(Tokens.div.ordinal(), yytext(), linea); }
"(" { return new Token(Tokens.p_a.ordinal(), yytext(), linea); }
")" { return new Token(Tokens.p_c.ordinal(), yytext(), linea); }
"," { return new Token(Tokens.coma.ordinal(), yytext(), linea); }
";" { return new Token(Tokens.pcoma.ordinal(), yytext(), linea); }
{L}({L}|{D})* { return new Token(Tokens.id.ordinal(), yytext(), linea); }
[+-]?{D}+(\.{D}+)? { return new Token(Tokens.num.ordinal(), yytext(), linea); }


// Regla para manejar tokens desconocidos
. { 
    System.err.println("Error: Token desconocido '" + yytext() + "' en la línea " + linea); 
    System.exit(1); // Detiene la ejecución
}
