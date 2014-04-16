package gov.epa.mims.analysisengine.table.io;

import java.util.ArrayList;

/*
 * ARRFFileReader.java
 * This class is customized to read Monte Carlo inputfile
 * Created on April 1, 2004, 12:31 PM
 * @author  Parthee Partheepan, CEP, UNC-CHAPEL HILL
 * @version $Id: ARRFFileReader.java,v 1.2 2006/10/30 21:43:51 parthee Exp $
 */
public class ARRFFileReader extends FileParser
{
   public static final int  NO_OF_COLUMN_HEADER_ROWS = 0;
   public static final String DELIMITER  = ",";
   
   /** Creates a new instance of ARRFFileReader */
   public ARRFFileReader(String fileName) throws Exception
   {
      super( fileName,DELIMITER, NO_OF_COLUMN_HEADER_ROWS, false);
      customizeHeaderData();
   }
                                                                                                                                               
   protected void customizeHeaderData() {

      columnHeaderData = new ArrayList();
      String[] columnNames = new String[getColumnCount()];
      String header = getFileHeader();
      int numCols = getColumnCount();
      String dataToBeParsed = header.substring(header.indexOf("@attribute"));
      int index = dataToBeParsed.indexOf("\n");
      for(int i=0; i<numCols && index<=dataToBeParsed.length(); i++)
      {
         String[] lineTokens = dataToBeParsed.substring(0, index).split("[ \t]+");
         dataToBeParsed = dataToBeParsed.substring(index+1);
         index = dataToBeParsed.indexOf("\n");
         int j=0;
         if(lineTokens[j].equals("@attribute"))
         {
           while(lineTokens[++j].trim().length()==0);
           if(j < lineTokens.length)
           {
               columnNames[i]=(lineTokens[j]);
           }
         } 
     }
     columnHeaderData.add(columnNames);
     fileHeader = fileHeader.substring(0, fileHeader.indexOf("@attribute"));
	}
}

