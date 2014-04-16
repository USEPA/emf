package gov.epa.mims.analysisengine.table.io;

/**
 * Title:        Total Risk Integrated Methodology
 * Description:  Holds information about a file including it's name, column names, the
 *               start line for the data etc.
 * Copyright:    Copyright (c) 1998
 * Company:      MCNC-North Carolina Supercomputing Center
 * @author Daniel Gatti
 * @version $Id: FileAttributes.java,v 1.1 2006/11/01 15:33:39 parthee Exp $
 */
public class FileAttributes
{
   public String fileName  = null;
   public String delimiter = null;
   public boolean hasColumnNames = true;
   public int numValuesPerLine = -1;
   public int columnNameStartLine   = -1;
   public int columnNameEndLine   = -1;
   public int startDataLine    = -1;
   public int endDataLine      = -1;

} // class FileAttributes

