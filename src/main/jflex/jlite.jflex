package jlite.lexer;

import java_cup.runtime.ComplexSymbolFactory.ComplexSymbol;
import java_cup.runtime.ComplexSymbolFactory.Location;
import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.Symbol;
import java_cup.runtime.SymbolFactory;

import jlite.parser.sym;

%%
%public
%class Scanner
%cup
%unicode
%line
%column

%{
  StringBuilder sb = new StringBuilder();
  ComplexSymbolFactory sf = new ComplexSymbolFactory();

  public Scanner(java.io.Reader in, ComplexSymbolFactory sf){
    this(in);
	this.sf = sf;
  }

  private Symbol symbol(String name, int sym) {
    return sf.newSymbol(name, sym);
  }

  private Symbol symbol(String name, int sym, Object val) {
    return sf.newSymbol(name, sym, val);
  }
%}

LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
WhiteSpace     = {LineTerminator} | [ \t\f]


/* comments */
Comment = {BlockComment} | {LineComment}

BlockComment = "/*" [^*] ~"*/" | "/*" "*"+ "/"
LineComment = "//" {InputCharacter}* {LineTerminator}?

Identifier = [a-z][a-zA-Z0-9_]*
ClassName = [A-Z][a-zA-Z0-9_]*
Integer = [0-9]+

%state STRING

%%
<YYINITIAL> {
  /* keywords */
  "class" { return symbol("class", sym.CLASS); }
  "main" { return symbol("main", sym.MAIN); }

  "Void" { return symbol("void", sym.VOID); }
  "Bool" { return symbol("bool", sym.BOOL); }
  "Int" { return symbol("int", sym.INT); }
  "String" { return symbol("string", sym.STRING); }

  "true" { return symbol("true", sym.TRUE); }
  "false" { return symbol("false", sym.FALSE); }

  "if" { return symbol("if", sym.IF); }
  "else" { return symbol("else", sym.ELSE); }
  "while" { return symbol("while", sym.WHILE); }
  "readln" { return symbol("readln", sym.READLN); }
  "println" { return symbol("println", sym.PRINTLN); }
  "return" { return symbol("return", sym.RETURN); }

  "this" { return symbol("this", sym.THIS); }
  "new" { return symbol("new", sym.NEW); }
  "null" { return symbol("null", sym.NULL); }

  /* separators */
  "(" { return symbol("(", sym.LPAREN); }
  ")" { return symbol(")", sym.RPAREN); }
  "{" { return symbol("{", sym.LBRACE); }
  "}" { return symbol("}", sym.RBRACE); }
  ";" { return symbol(";", sym.SEMI); }
  "," { return symbol(",", sym.COMMA); }
  "." { return symbol(".", sym.DOT); }

  /* operators */
  "+" { return symbol("+", sym.PLUS); }
  "-" { return symbol("-", sym.MINUS); }
  "*" { return symbol("*", sym.MUL); }
  "/" { return symbol("/", sym.DIV); }
  "<" { return symbol("<", sym.LT); }
  ">" { return symbol(">", sym.GT); }
  "<=" { return symbol("<=", sym.LEQ); }
  ">=" { return symbol(">=", sym.GEQ); }
  "==" { return symbol("==", sym.EQ); }
  "!=" { return symbol("!=", sym.NEQ); }
  "=" { return symbol("=", sym.ASSIGN); }
  "!" { return symbol("!", sym.NOT); }
  "||" { return symbol("||", sym.OR); }
  "&&" { return symbol("&&", sym.AND); }

  /* comments */
  {Comment} { /* ignore */ }

  /* whitespace */
  {WhiteSpace} { /* ignore */ }

  /* literals */
  {Integer} { return symbol("int_lit", sym.INT_LIT, new Integer(Integer.parseInt(yytext()))); }
  \" { yybegin(STRING); sb.setLength(0); }
  {Identifier} { return symbol("id", sym.ID, yytext()); }
  {ClassName} { return symbol("cname", sym.CNAME, yytext()); }
}

<STRING> {
  \" { yybegin(YYINITIAL); return symbol("string_lit", sym.STR_LIT, sb.toString()); }
  [^\n\r\"\\]+                   { sb.append( yytext() ); }
  "\\t"                          { sb.append('\t'); }
  "\\n"                          { sb.append('\n'); }

  "\\r"                          { sb.append('\r'); }
  "\\\""                         { sb.append('\"'); }
  "\\\\"                         { sb.append('\\'); }
  \\[0-3]?[0-7]?[0-7]            { char val = (char) Integer.parseInt(yytext().substring(1), 8); sb.append(val); }
  \\x[0-9a-f]?[0-9a-f]           { char val = (char) Integer.parseInt(yytext().substring(2), 16); sb.append(val); }

  /* error cases */
  \\. { throw new Error("Illegal character"); }
  {LineTerminator} { throw new Error("Unterminated string"); }
}


<<EOF>> { return symbol("EOF", sym.EOF); }