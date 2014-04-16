/**
 *  FileScanner class
 *  tokenizes an input file given a delimiter based on 
 *  set of rules defined in
 *  @see FileScanner.flex
 *
 * @author  Krithiga Thangavelu, CEP, UNC CHAPEL HILL.
 * @version $Id: FileScanner.flex,v 1.1 2006/11/01 15:33:39 parthee Exp $
 **/

package gov.epa.mims.analysisengine.table;

import java_cup.runtime.Symbol;
import gov.epa.mims.analysisengine.table.TokenConstants;
import java.util.Vector;
import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

%%

%class FileScanner
%public
%unicode
%cup
%line
%column
%standalone
%type java_cup.runtime.Symbol

%{

/** a getter method for the line recently tokenized
 *  @return String the entire line
 */
  public String getLine() 
  {
      return line.toString();
  }

  /** a getter method for list of tokens separated by delimiter in a line
   *  @param char delimiter
   *  @return Symbol[] an array of tokens
   *  return value is null when end of file is reached
   **/
  public Symbol[] getTokensPerLine(char delimiter, boolean ignoreMultipleDelims) throws IOException 
  {
   
   Vector tokens = new Vector();
   int numTokensBetweenDelim = 0;
   int prevPositionOfDelim = -1;
   int lastType =0; // default - NULL_LITERAL
   line.setLength(0);
   Symbol modifiedToken;
   Symbol token = next_token();
   if(token.sym == TokenConstants.EOF) return null;
   while(token!=null) 
   {
      if(token.sym == delimiter || token.sym == TokenConstants.EOL || token.sym
== TokenConstants.EOF || (numTokensBetweenDelim == 0 && ignoreMultipleDelims))
      {
         if(numTokensBetweenDelim == 0) 
         {
            if(token.sym == delimiter && !ignoreMultipleDelims)
            {
               prevPositionOfDelim = token.right-1+token.value.toString().length();
               modifiedToken = symbol(TokenConstants.NULL_LITERAL, new String());
               tokens.add(modifiedToken);
            }
         } 
         else 
         {
            if(numTokensBetweenDelim > 1) 
            {
             String string = line.substring(prevPositionOfDelim+1, line.length()-1);
             line.setLength(prevPositionOfDelim+1);
             modifiedToken = symbol(TokenConstants.STRING_LITERAL, string);
             line.append(token.value);
             while(numTokensBetweenDelim-- != 0) tokens.removeElementAt(tokens.size()-1);
             tokens.add(modifiedToken);
            }
            else 
            {
             // for ease of retrieval.. change the value object to its type
             // say a Double literal..value converted to Double from String
             //exception null or missing value .. type unknown so store as string
             if(lastType != TokenConstants.NULL_LITERAL) 
             {
                modifiedToken = (Symbol)tokens.get(tokens.size()-1);
                tokens.set(tokens.size()-1, changeType(modifiedToken));
                lastType = TokenConstants.NULL_LITERAL;
             }
             if(token.sym == TokenConstants.EOF)
               break;
            }
             prevPositionOfDelim = token.right-1+token.value.toString().length();
             numTokensBetweenDelim = 0;
         }
         if( token.sym == TokenConstants.EOL || token.sym == TokenConstants.EOF) 
            break;
      } 
      else 
      {
         numTokensBetweenDelim++;
         tokens.add(token);
         lastType = token.sym;
      }
      token = next_token();
   }
   return (Symbol[])tokens.toArray(new Symbol[0]);
  }

  /**  changes the String to an object of corresponding type
   *   @param Symbol token(containing the object type and String equivalent of value)
   *   @return Symbol 
   **/
  private Symbol changeType(Symbol token) 
  {
   
   switch(token.sym) 
   {

     case 6:
      Boolean b = new Boolean((String)token.value);
      token.value = b;
      return token;
   
     case 1:
      token.value = ((String)(token.value)).trim();
      return token;

     case 2:
      Double d = new Double((String)token.value);
      token.value = d;
      return token;
      
     case 3:
       try 
       {
         Integer i = new Integer((String) token.value); 
         token.value = i;
       } 
       catch (NumberFormatException e) 
       {
            Double id = new Double((String) token.value);    
            token.value = id;
            token.sym = TokenConstants.DOUBLE_LITERAL;
       }
       return token;
   
     case 4:
     case 5:
         Date da = latest_date; //its easier to store last parsed date in Date format
         if(da != null)
            token.value = da;
         else 
            token.sym = TokenConstants.STRING_LITERAL;
         return token;
    }
   return token;
  }

  private StringBuffer string = new StringBuffer();
  private boolean stringInitialState = false;
  private StringBuffer line = new StringBuffer();
  private boolean needToAppendQuote = false;
  private Date latest_date;

  /** constructor for token
   * @param int type - type of the token string or double or integer or null or boolean
   * @param Object value - the actual token as an object
   * @return java_cup.runtime.Symbol
   **/
  protected Symbol symbol(int type, Object value) 
  {
    if(type != TokenConstants.NULL_LITERAL) line.append(value);
    if(needToAppendQuote) 
    {
      line.append("\"");
      needToAppendQuote = false;
    }
    return new Symbol(type, yyline, yycolumn, value);
  }

/** a utility method..given a type gives toString()
 *  @param int type - refer gov.epa.mims.analysisengine.table.sym
 *  @return String 
 **/
 public String printType (int type) 
 {
  switch(type) 
  {
   case -3:
      return  "COMMENT";
   case -2:
      return "EOL";
   case -1:
      return "EOF";
   case 0:
      return "NULL_LITERAL";
   case 1:
      return "STRING";
   case 2: 
      return "DOUBLE";
   case 3: return "INTEGER";
   case 4: return "DATE";
   case 5: return "DATE_TIME";
   case 6: return "BOOLEAN";
   }
   return String.valueOf((char)type);
  }

   boolean checkDate(String date) 
   {
      String[] splits = date.split("/");
      int month = Integer.parseInt(splits[0]);
      int day = Integer.parseInt(splits[1]);
      if(month==0 || month > 12)
         return false;
      if(day == 0 || day > 31)
         return false;
      return true;
   }

   boolean checkTime(String time) 
   {
      String[] splits = time.split(":");
      int hour = Integer.parseInt(splits[0]);
      int minutes = Integer.parseInt(splits[1]);
      if(hour<0 || hour > 24)
         return false;
      if(minutes < 0 || minutes > 59)
         return false;
      return true;
   }

   /** stores date in the class variable latest_date
    *  as a  java.util.Date object.
    *  @param String data (mm/dd/yyyy)
    *  @param String time (hh:mm)
    *  @return none
    */
   private void storeDate(String date, String time) 
   {
      SimpleDateFormat sdf;
      latest_date = null;

      if(date == null) {
         latest_date=null;
         return;
      }

      if(time!=null) 
      {
         date+=" ";
         date+=time;
         sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm");
      }
      else
          sdf = new SimpleDateFormat("MM/dd/yyyy");

      try 
      {
         latest_date =  sdf.parse(date);
      } 
      catch (ParseException pe) 
      { 
         latest_date = null; return;
      }

   }

%}

LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
WhiteSpace     = {LineTerminator} | [ \t\f]

/* comments */
Comment = {TraditionalComment} | {EndOfLineComment} | {DocumentationComment}

TraditionalComment   = "/*" [^*] ~"*/" | "/*" "*"+ "/"
EndOfLineComment     = "//" {InputCharacter}* {LineTerminator}
DocumentationComment = "/**" {CommentContent} "*"+ "/"
CommentContent       = ( [^*] | \*+ [^/*] )*

BooleanLiteral = true|false
DecIntegerLiteral = [-]*[0-9]+ 
Date = [0-1]*[0-9]\/[0-3]*[0-9]\/[1-9][0-9][0-9][0-9]
Time = [0-2]*[0-9]\:[0-5][0-9]
AlphaNumericString  = [0-9]+([a-z]|[A-Z]|_)+([a-z]|[A-Z]|[0-9]|_)*|([a-z]|[A-Z]|_)([a-z]|[A-Z]|[0-9]|_)* 
DoubleLiteral = {FLit1}|{FLit2}|{FLit3}

FLit1 = [ \t\f]*[-+]*[0-9]+ \. [0-9]* {Exponent}?
FLit2 = [ \t\f]*[-+]*\. [0-9]+ {Exponent}?
FLit3 = [ \t\f]*[-+]*[0-9]+  {Exponent}+


Exponent = [eE]+ [+\-]? [0-9]+
MissingValue = "NaN"
%state STRING DATE

%%

<YYINITIAL> {

  /* literals */
  {BooleanLiteral}      { return symbol(TokenConstants.BOOLEAN_LITERAL, yytext().toString()); }
  {MissingValue}        { return symbol(TokenConstants.DOUBLE_LITERAL, yytext().toString()); }
  {AlphaNumericString}  { return symbol(TokenConstants.STRING_LITERAL, yytext().toString()); } 
  {DecIntegerLiteral}   { return symbol(TokenConstants.INTEGER_LITERAL, yytext().toString()); } 
  {Date}                { string.setLength(0); string.append(yytext()); 
                           if(checkDate(string.toString()))
                              yybegin(DATE); 
                           else
                              return symbol(TokenConstants.STRING_LITERAL, yytext().toString());
                         }
  {DoubleLiteral}        { return symbol(TokenConstants.DOUBLE_LITERAL, yytext().toString()); }
 
   \"                    { 
                            line.append(yytext());
                            string.setLength(0); 
                            stringInitialState = true;
                            yybegin(STRING); 
                         }

  /* comments */
  {Comment}              { return symbol(TokenConstants.COMMENT, yytext().toString()); }

 <<EOF>>                 { return symbol(TokenConstants.EOF, null); }
 
 {LineTerminator}        { return symbol(TokenConstants.EOL, yytext().toString()); }


 /* possible separators */

   "("            { return symbol(TokenConstants.LPAREN, yytext().toString()); }
   ")"            { return symbol(TokenConstants.RPAREN, yytext().toString()); }
   "{"            { return symbol(TokenConstants.LBRACE, yytext().toString()); }
   "}"            { return symbol(TokenConstants.RBRACE, yytext().toString()); }
   "["            { return symbol(TokenConstants.LBRACK, yytext().toString()); }
   "]"            { return symbol(TokenConstants.RBRACK, yytext().toString()); }
   ":"            { return symbol(TokenConstants.COLON, yytext().toString()); }
   "|"            { return symbol(TokenConstants.OR, yytext().toString()); }
   ";"            { return symbol(TokenConstants.SEMICOLON, yytext().toString()); }
   ","            { return symbol(TokenConstants.COMMA, yytext().toString()); }
   "."            { return symbol(TokenConstants.DOT, yytext().toString()); }
   "="            { return symbol(TokenConstants.EQ, yytext().toString()); }
   ">"            { return symbol(TokenConstants.GT, yytext().toString()); }
   "<"            { return symbol(TokenConstants.LT, yytext().toString()); }
   "!"            { return symbol(TokenConstants.NOT, yytext().toString()); }
   "~"            { return symbol(TokenConstants.COMP, yytext().toString()); }
   "?"            { return symbol(TokenConstants.QUESTION, yytext().toString()); }
   "+"            { return symbol(TokenConstants.PLUS, yytext().toString()); }
   "-"            { return symbol(TokenConstants.MINUS, yytext().toString()); }
   "*"            { return symbol(TokenConstants.MULT, yytext().toString()); }
   "/"            { return symbol(TokenConstants.DIV, yytext().toString()); }
   "&"            { return symbol(TokenConstants.AND, yytext().toString()); }
   "^"            { return symbol(TokenConstants.XOR, yytext().toString()); }
   "%"            { return symbol(TokenConstants.MOD, yytext().toString()); }
   "#"            { return symbol(TokenConstants.HASH, yytext().toString()); }
   "\t"           { return symbol(TokenConstants.TAB, "\t"); }
   " "            { return symbol(TokenConstants.SPACE, " "); }

  /* whitespace */
  {WhiteSpace}    { line.append(yytext()); }


}

<DATE> {
   {WhiteSpace}   { 
                     string.append(yytext()); 
                  }
   {Time}         {
                      yybegin(YYINITIAL);
                      if(!checkTime(yytext().toString())) 
                      {
                        storeDate(string.toString(), (String)null);
                        yypushback(yytext().length());
                        return symbol(TokenConstants.DATE_LITERAL, string.toString());
                      }
                      storeDate(string.toString(), yytext().toString()); 
                      string.append(yytext()); 
                      return symbol(TokenConstants.DATE_TIME_LITERAL, string.toString());
                   }
   .|\n            { 
                       yybegin(YYINITIAL);
                       storeDate(string.toString(), (String)null);            
                       yypushback(yytext().length());
                       return symbol(TokenConstants.DATE_LITERAL, string.toString()); 
                   }
   }

<STRING> {

 {DecIntegerLiteral}\"          {
               yybegin(YYINITIAL);
               string.append(yytext());
               needToAppendQuote = true;
               if(stringInitialState)
                  return symbol(TokenConstants.INTEGER_LITERAL, string.substring(0, string.length()-1));
               else 
                 return symbol(TokenConstants.STRING_LITERAL, string.substring(0, string.length()-1));
            }
  {BooleanLiteral}\"      {
               yybegin(YYINITIAL);
               string.append(yytext());
               needToAppendQuote = true;
               if(stringInitialState)
                  return symbol(TokenConstants.BOOLEAN_LITERAL, string.substring(0, string.length()-1));
               else 
                 return symbol(TokenConstants.STRING_LITERAL, string.substring(0, string.length()-1));
            }
 {DoubleLiteral}\" | NaN\"   { 
               yybegin(YYINITIAL);
               string.append(yytext());
               needToAppendQuote = true;
               if(stringInitialState)
                 return symbol(TokenConstants.DOUBLE_LITERAL, string.substring(0, string.length()-1));
               else 
                 return symbol(TokenConstants.STRING_LITERAL, string.substring(0, string.length()-1));
            }
  
  {Date}\"  { 
              yybegin(YYINITIAL);
              needToAppendQuote = true;
              string.append(yytext());
              if(stringInitialState) 
              {
               storeDate(yytext().substring(0,((yytext()).length())-1), (String)null); 
               return symbol(TokenConstants.DATE_LITERAL, string.substring(0, string.length()-1));
              }
              else 
                 return symbol(TokenConstants.STRING_LITERAL, string.substring(0, string.length()-1));
            }

  {Date}{WhiteSpace}{Time}\"    {    
              yybegin(YYINITIAL);
              needToAppendQuote = true;
              string.append(yytext());
              if(stringInitialState) {
                String[] splits = yytext().substring(0, yytext().length()-1).split("[\r\n|\n|\r]|[ \t\f]");
                if(checkDate(splits[0]) && checkTime(splits[1])) 
                {
                  storeDate(splits[0], splits[1]); 
                  return symbol(TokenConstants.DATE_TIME_LITERAL, string.substring(0, string.length()-1));
                }
              }
                 return symbol(TokenConstants.STRING_LITERAL, string.substring(0, string.length()-1));
            }


  \"       { 
               yybegin(YYINITIAL); 
               needToAppendQuote = true;
               return symbol(TokenConstants.STRING_LITERAL, string.toString()); 
           }

      
  [^\n\r\"\\]+ {         
               // check why \" is in the matching condition in state String
               stringInitialState= false;
               string.append( yytext() ); 
               }

  \\\"      {
               stringInitialState = false;
               string.append("\\\""); 
            }

  .         { 
               stringInitialState = false; 
               string.append(yytext().toString()); 
            }

}

.           { 
               line.append(yytext()); 
            }
