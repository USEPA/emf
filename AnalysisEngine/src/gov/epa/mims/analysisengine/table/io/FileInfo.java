package gov.epa.mims.analysisengine.table.io;

import java.io.OutputStream;

/** FileInfo is a vector that stores all information required to import a file
 *
 * @author  Krithiga Thangavelu, CEP, UNC CHAPEL HILL.
 * @version $Id: FileInfo.java,v 1.1 2006/10/30 17:26:13 parthee Exp $
 */


public class FileInfo extends java.util.Vector {
   public String fileName;
   public String path;
   public String delim;
   public String fileType;
   public int numColHdrRows;
   
  FileInfo(String filename, String path, String delim, int numColHdrRows, String fileType) {
   this.fileName = filename;
   this.path = path;
   this.delim = delim;
   this.numColHdrRows = numColHdrRows;
   this.fileType = fileType;
   add(fileType);
   add(path);
   add(fileName);
   add(delim);
   add(new Integer(numColHdrRows));
  }

  public void print(OutputStream out) throws java.io.IOException 
  {
   String line = new String("\""+fileType+"\";\""+path+"\";\""+fileName+"\";\""+delim+"\";\""+numColHdrRows+"\"\n");
   out.write(line.getBytes(), 0, line.getBytes().length);
  }
}

