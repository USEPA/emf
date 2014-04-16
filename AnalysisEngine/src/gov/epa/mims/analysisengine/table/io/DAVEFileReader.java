package gov.epa.mims.analysisengine.table.io;




/*
 * DAVEFileReader.java
 * This class is customized to read DAVE output file
 * Created on April 1, 2004, 12:31 PM
 * @author  Parthee Partheepan, CEP, UNC-CHAPEL HILL
 * @version $Id: DAVEFileReader.java,v 1.2 2006/10/30 21:43:51 parthee Exp $
 */

 public class DAVEFileReader extends FileParser
{
   
   public static final int  NO_OF_COLUMN_HEADER_ROWS = 1;
   
   /** Creates a new instance of DAVEFileReader */
   public DAVEFileReader(String fileName,String delimiter, boolean ignoreMultDelims) throws Exception
   {
      super(fileName, delimiter, NO_OF_COLUMN_HEADER_ROWS, ignoreMultDelims);
   }
   
}

