/********************************************************************
 *  Class Name: gov.epa.mims.analysisengine.table.TokenConstants  
 *  Defines type values used by FileScanner to identify the token type.
 *
 *  IMPORTANT:
 *  Insert a token type variable in this class whenever you define a new token in FileScanner.flex
 *  Rename sym.EOF in FileScanner.java to TokenConstants.EOF whenever FileScanner.java is generated from FileScanner.flex
 *
 * @author  Krithiga Thangavelu, CEP, UNC CHAPEL HILL.
 * @version $Id: TokenConstants.java,v 1.1 2006/10/30 17:26:13 parthee Exp $

 */

package gov.epa.mims.analysisengine.table.io;
import java.util.Date;

public  class TokenConstants {
  // a mapping between type and corresponding Class Type
  static Class[] TypeToClass = {Object.class, String.class, Double.class, Integer.class, Date.class, Date.class, Boolean.class};  
  

  static int NULL_LITERAL = 0;
  static int STRING_LITERAL = 1;
  static int DOUBLE_LITERAL = 2;
  static int INTEGER_LITERAL = 3;
  static int DATE_LITERAL = 4;
  static int DATE_TIME_LITERAL = 5;
  static int BOOLEAN_LITERAL = 6;
  static int EOF = -1;
  static int EOL = -2;
  static int COMMENT = -3;
  static int  LPAREN ='{';
  static int  RPAREN ='}';
  static int  LBRACE ='(';
  static int  RBRACE =')';
  static int  LBRACK ='[';
  static int  RBRACK =']';
  static int  COLON = ':';
  static int  OR = '|';
  static int  SEMICOLON =';';
  static int  COMMA = ',';
  static int  DOT ='.';
  static int  EQ = '=';
  static int  GT = '>';
  static int  LT ='<';
  static int  NOT = '!';
  static int  COMP = '~';
  static int  QUESTION = '?';
  static int  PLUS ='+';
  static int  MINUS ='-';
  static int  MULT = '*';
  static int  DIV = '/';
  static int  AND = '&';
  static int  XOR = '^';
  static int  MOD = '%';
  static int HASH = '#';
  static int TAB = '\t';
  static int SPACE = ' ';
  
  /** a utility method..given a type gives toString()
 *  @param int type - refer gov.epa.mims.analysisengine.table.TokenConstants
 *  @return String
 **/
 public static String printType (int type) {
  switch(type) {
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
 
}
