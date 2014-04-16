package gov.epa.mims.analysisengine.table.io;



import java.util.ArrayList;


/**
 * SMKReportFileReader.java
 * This class is customized to read SMK Report file
 * Created on March 31, 2004, 1:38 PM
 * @author  Parthee Partheepan, CEP, UNC-CHAPEL HILL
 * @version $Id: SMKReportFileReader.java,v 1.1 2006/11/01 15:13:33 parthee Exp $
 */

public class SMKReportFileReader extends FileParser {
   public static final int  NO_OF_COLUMN_HEADER_ROWS = 2;
   public static final String DELIMITER = ";";

   /** Creates a new instance of SMOKEFileReader */
   public SMKReportFileReader(String fileName, String delimiter, boolean ignoreMultDelims) throws Exception {
      super(fileName, delimiter, NO_OF_COLUMN_HEADER_ROWS, ignoreMultDelims);
      // AME: temporary - see if this works for most formats
      rowHeaderData = new ArrayList();
      rowHeaderData.add("Pollutant");
      rowHeaderData.add("Units");
      StringBuffer buffer = new StringBuffer(logger);
      int startpos = logger.indexOf("Non Data");
      if(startpos >= 0) {
      int endpos = logger.indexOf('\n', startpos);
      endpos = logger.indexOf('\n', endpos+1);
      buffer.delete(startpos, endpos);
      logger = buffer.toString();
      }
   }
  
}
