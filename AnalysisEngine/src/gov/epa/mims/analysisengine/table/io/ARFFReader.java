package gov.epa.mims.analysisengine.table.io;


import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

/*
 * ARFFReader.java
 * This class is customized to read Monte Carlo inputfile
 * Created on April 1, 2004, 12:31 PM
 * @author  Parthee Partheepan, CEP, UNC-CHAPEL HILL
 * @version $Id: ARFFReader.java,v 1.2 2006/10/30 21:43:51 parthee Exp $
 */
public class ARFFReader extends FileParser
{
   public static final int  NO_OF_COLUMN_HEADER_ROWS = 0;
   public static final String DELIMITER  = ",";
   
   /** Creates a new instance of ARFFFileReader */
   public ARFFReader(String fileName) throws Exception
   {
      super();

      FileReader reader = new FileReader(fileName);
      BufferedReader bf = new BufferedReader(reader);
      StringBuffer header = new StringBuffer();
      String line;
      while((line = bf.readLine())!=null)
      {
         if((line.trim()).indexOf("%")==0 ||
           (line.trim()).indexOf("@attribute")==0 ||
           (line.trim()).indexOf("@relation") == 0 ||
           (line.trim()).indexOf("@data") == 0 ||
           ((line.trim()).length())==0 ||
           (line.trim()).indexOf("@ATTRIBUTE")==0 ||
           (line.trim()).indexOf("@RELATION") == 0 ||
           (line.trim()).indexOf("@DATA") == 0)
                header.append(line+"\n");
         else
            break;
      }
      if(line!=null)
         header.append(line+"\n");
   
      this.delimiter = DELIMITER;
      this.multipleOccurences = false;
      readAndStoreData(bf);
      fileHeader = new String((header.toString())+fileHeader);
      customizeHeaderData();
   }
                                                                                                                                               
   protected void customizeHeaderData() throws Exception {

      columnHeaderData = new ArrayList();
      String[] columnNames = new String[getColumnCount()];
      int numCols = getColumnCount();
      int index = fileHeader.indexOf("@attribute");
      if(index == -1)
          index = fileHeader.indexOf("@ATTRIBUTE");
      if(index == -1)
          throw new Exception(fileName+" is not an ARFF file");
      String dataToBeParsed = fileHeader.substring(index);
      index = dataToBeParsed.indexOf("\n");
      for(int i=0; i<numCols && index<=dataToBeParsed.length(); i++)
      {
         while (index==0) 
         {
             index++;
             index = dataToBeParsed.indexOf("\n");
         }
         String[] lineTokens = dataToBeParsed.substring(0, index).split("[ \t]+");
         dataToBeParsed = dataToBeParsed.substring(index+1);
         index = dataToBeParsed.indexOf("\n");
         int j=0;
         if(lineTokens[j].equalsIgnoreCase("@attribute"))
         {
           while(lineTokens[++j].trim().length()==0);
           if(j < lineTokens.length)
               columnNames[i]=(lineTokens[j].replace('"', ' ')).trim();
         } 
     }
     columnHeaderData.add(columnNames);
     index = fileHeader.indexOf("@relation");
     if(index == -1)
         index = fileHeader.indexOf("@RELATION");
     fileHeader = fileHeader.substring(0,index);
     StringBuffer header = new StringBuffer(fileHeader);
     if(fileHeader.length() > 0)
     {
        int startIndex = 1;
        if(header.charAt(startIndex-1)=='%')
           header.deleteCharAt(startIndex-1);
        while(header.substring(startIndex).indexOf("\n")!=-1)
        {
            startIndex += (header.substring(startIndex)).indexOf("\n") + 1;
            while(startIndex<header.length() && (header.charAt(startIndex)==' ' ||
header.charAt(startIndex) == '\t' || header.charAt(startIndex)=='\n')) startIndex++;
            if(startIndex<header.length() && header.charAt(startIndex)=='%')
               header.deleteCharAt(startIndex);
         }
         fileHeader = header.toString();
      }
   }
}

