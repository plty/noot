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
Comment = {TraditionalComment} | {EndOfLineComment}

TraditionalComment = "/*" [^*] ~"*/" | "/*" "*"+ "/"
EndOfLineComment = "//" {InputCharacter}* {LineTerminator}?

Identifier = [a-z][a-zA-Z0-9_]*
ClassName = [A-Z][a-zA-Z0-9_]*
Integer = [0-9]+

%state STRING

%%
<YYINITIAL> {
  /* keywords */
  "class" { return symbol("class",sym.CLASS); }
  "main" { return symbol("main",sym.MAIN); }
  "Void" { return symbol("Void",sym.VOID); }

  /* separators */
  "(" { return symbol("(",sym.LPAREN); }
  ")" { return symbol(")",sym.RPAREN); }
  "{" { return symbol("{",sym.LBRACE); }
  "}" { return symbol("}",sym.RBRACE); }

  /* comments */
  {Comment} { /* ignore */ }

  /* whitespace */
  {WhiteSpace} { /* ignore */ }

  /* literals */
  {ClassName} { return symbol("cls", sym.CNAME, yytext()); }
}

<<EOF>> { return symbol("EOF",sym.EOF); }