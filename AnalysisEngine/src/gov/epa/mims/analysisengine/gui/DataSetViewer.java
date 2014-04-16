package gov.epa.mims.analysisengine.gui;

import gov.epa.mims.analysisengine.tree.DataSetIfc;
import gov.epa.mims.analysisengine.tree.LabeledDataSetIfc;
import gov.epa.mims.analysisengine.tree.DateDataSetIfc;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.io.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.util.Date;
import java.util.GregorianCalendar;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;


/**
 * A window panel for viewing a set of DataSetIfc in a table with a print option
 *
 * To see an example, run main
 *
 * @author Steve Howard
 * @version $Id: DataSetViewer.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 *
 **/

public class DataSetViewer extends JPanel
{
  // array of DataSetIfc to view
  private DataSetIfc[] dataSets;
  private PrintableTable printTable;

  // parent window of DataSetViewer
  private Window parent;

  //  ToolBar and buttons
  private JToolBar toolBar;
  private JButton closeButton;
  private JButton printButton;
  private JButton exportButton;

  // tableView for viewing dataSets
  private JScrollPane tableView;

  // formatter for Double and Date labeled field
  private static DecimalFormat decFormatter = new DecimalFormat("0.0000E00");
  private static SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");


  // column 0 type
  private int columnZeroType;

  // define types of column 0 labels
  private final static int ROW_NUMBER_LABEL = 1;
  private final static int DATE_LABEL = 2;
  private final static int TEXT_LABEL = 3;


  /**
   * constructor
   *
   * @param parent Window of the parent
   *
   * @param dataSets is an Array of DataSetIfc[] to view
   *
   */
  public DataSetViewer(Window parent, DataSetIfc[] dataSets)
      throws Exception
  {
     super();
     this.setLayout(new BorderLayout());

     this.dataSets = dataSets;
     this.parent = parent;

     // try to open all dataSets
     for(int i=0; i<dataSets.length; i++)
     {
        try
        {
           dataSets[i].open();
        }
        catch(Exception ex)
        {
           String msg = "Exception on open for DataSet ["+dataSets[i].getName()+"], "+ex.getMessage();
           JOptionPane.showMessageDialog(this, msg, "Error Message", JOptionPane.ERROR_MESSAGE);
           throw new Exception(msg);
        }

        // check number of values in each set
        try
        {
           int count = dataSets[i].getNumElements();
        }
        catch(Exception ex)
        {
           String msg = "Exception on open for DataSet ["+dataSets[i].getName()+"], "+ex.getMessage();
           JOptionPane.showMessageDialog(this, msg, "Error Message", JOptionPane.ERROR_MESSAGE);
           throw new Exception(msg);
        }
     }

     // determine column Zero label type to use
     determineColumnZero();

     // create and add toolBar to panel
     createToolBar();
     this.add(toolBar, BorderLayout.NORTH);

     // create and add table to panel
     createTableView();
     this.add(tableView, BorderLayout.CENTER);
  }

 /**
  * private method to determine which column zero label type to use
  */
  private void determineColumnZero()
  {
     double dvalue;

     try
     {
        dvalue = dataSets[0].getElement(0);
     }
     catch(Exception ex)
     {
        dvalue = 0.0D;
     }

     //  determine type of first set
     if( dataSets[0] instanceof LabeledDataSetIfc && dvalue!=Double.MIN_VALUE )
     {
        columnZeroType = TEXT_LABEL;
     }
     else if( dataSets[0] instanceof DateDataSetIfc )
     {
        columnZeroType = DATE_LABEL;
     }
     else
     {
        columnZeroType = ROW_NUMBER_LABEL;
     }

     if( dataSets.length==1 || columnZeroType==ROW_NUMBER_LABEL ) return;

     // verify that remaining sets are of same type with matching labels
     for(int i=1; i<dataSets.length; i++)
     {
        if( columnZeroType==TEXT_LABEL && !(dataSets[i] instanceof LabeledDataSetIfc) )
        {
           columnZeroType = ROW_NUMBER_LABEL;
           return;
        }
        if( columnZeroType==DATE_LABEL && !(dataSets[i] instanceof DateDataSetIfc) )
        {
           columnZeroType = ROW_NUMBER_LABEL;
           return;
        }

        try
        {
           // verify each label
           for(int j=0; j<dataSets[0].getNumElements(); j++)
           {
              if(columnZeroType==TEXT_LABEL)
              {
                 String label0 = ((LabeledDataSetIfc)dataSets[0]).getLabel(j);
                 String labeli = ((LabeledDataSetIfc)dataSets[i]).getLabel(j);
                 if( !label0.equals(labeli) )
                 {
                    columnZeroType = ROW_NUMBER_LABEL;
                    return;
                 }
              }
              else if(columnZeroType==DATE_LABEL)
              {
                 Date date0 = ((DateDataSetIfc)dataSets[0]).getDate(j);
                 Date datei = ((DateDataSetIfc)dataSets[i]).getDate(j);
                 if( date0.getTime()!=datei.getTime() )
                 {
                    columnZeroType = ROW_NUMBER_LABEL;
                    return;
                 }
              }
           }
        }
        catch(Exception ex)
        {
           columnZeroType = ROW_NUMBER_LABEL;
           return;
        }
     }
     return;
   }

 /**
  * private method to create toolbar and it's buttons
  */
  private void createToolBar()
  {
     // create toolBar
     toolBar = new JToolBar( SwingConstants.HORIZONTAL );
     toolBar.setFloatable(false);

     // create close button
     closeButton = new JButton("Close");
     Action closeAction = new AbstractAction("Close")
     {
        public void actionPerformed(java.awt.event.ActionEvent evt)
        {
           parent.setVisible(false);
           parent.dispose();
        }
     };
     closeButton.setAction(closeAction);
     closeButton.setToolTipText("Close Viewer");
     toolBar.add(closeButton);

     // create print button
     printButton = new JButton("Print");
     Action printAction = new AbstractAction("Print")
     {
        public void actionPerformed(java.awt.event.ActionEvent evt)
        {
          // add printTable to offscreen frame so table will have Graphics
          JFrame jf = new JFrame();
          JScrollPane scrollPane = new JScrollPane(printTable);
          jf.getContentPane().add(scrollPane);
          jf.pack();

          RepaintManager.currentManager(printTable).setDoubleBufferingEnabled(false);
          PrinterJob prn = PrinterJob.getPrinterJob();
          prn.setPrintable( printTable );
          if( prn.printDialog() )
          {
             try
             {
                prn.print();
             }
             catch(Exception ex)
             {
                JOptionPane.showMessageDialog(parent, ex.getMessage(), "Printer Exception", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
             }
          }

          // remove offscreen frame
          jf.dispose();
        }
     };
     printButton.setAction(printAction);
     printButton.setToolTipText("Print table");
     toolBar.add(printButton);

     // create export button
     exportButton = new JButton("Export");
     Action exportAction = new AbstractAction("Export")
     {
        public void actionPerformed(java.awt.event.ActionEvent evt)
        {
           exportTable();
        }
     };
     exportButton.setAction(exportAction);
     exportButton.setToolTipText("Export data to file");
     toolBar.add(exportButton);

  }

/**
 * method to create a Table View for set of DataSetIfc (dataSets)
 */
 private void createTableView()
 {
    // determine width of column zero
    final int widthColumnZero = (columnZeroType==ROW_NUMBER_LABEL) ? 50 : 150;

    // create dataModel for table
    TableModel dataModel = new AbstractTableModel()
    {
        public int getColumnCount()
        {
           return dataSets.length+1;
        }
        public int getRowCount()
        {
           // return size of largest datasets
           int size = 0;
           for(int i=0; i<dataSets.length; i++)
           {
              int setSize = 0;
              try
              {
                 setSize = dataSets[i].getNumElements();
              }
              catch(Exception ex){}
              if( setSize > size ) size = setSize;
           }
           return size;
        }
        public Object getValueAt( int row, int col)
        {
           if( col==0 )  // return row number
           {
              String result;
              try
              {
                 if( columnZeroType==DataSetViewer.TEXT_LABEL )
                 {
                    result = ((LabeledDataSetIfc)dataSets[0]).getLabel(row);
                 }
                 else if( columnZeroType==DataSetViewer.DATE_LABEL  )
                 {
                    result = dateFormatter.format( ((DateDataSetIfc)dataSets[0]).getDate(row) );
                 }
                 else  // use row number
                 {
                    result = "item " + Integer.toString(row);
                 }
              }
              catch(Exception ex)
              {
                 result = null;
              }
              return result;
           }
           else if(col > 0)
           {
              String result;
              try
              {
                 double dvalue = dataSets[col-1].getElement(row);
                 if(dvalue != Double.MIN_VALUE )
                 {
                    result = decFormatter.format(dvalue);
                 }
                 else if( dataSets[col-1] instanceof LabeledDataSetIfc )
                 {
                    result = ((LabeledDataSetIfc)dataSets[col-1]).getLabel(row);
                 }
                 else
                 {
                    result = decFormatter.format(dvalue);
                 }
              }
              catch(Exception ex)
              {
                 result = "NaN";
              }
              //AME return new Double(dvalue);
              // If we use a string instead, then it uses scientific notation
              // for very large or very small values
              return result;
           }
           else
           {
              return null;
           }
        }
        public String getColumnName(int col)
        {
           if( col>0 )
           {
              return dataSets[col-1].getName() + " ("+dataSets[col-1].getUnits()+")";
           }
           else
           {
              return null;
           }
        }
        public Class getColumnClass(int col)
        {
              return String.class;
        }
        public boolean isCellEditable(int row, int col)
        {
           return false;
        }
        public void setValueAt( Object aValue, int row, int col ){}
      };

    // create a table for printing
    printTable = new PrintableTable(dataModel);

    // create column model for rowHeaders
    TableColumnModel rowHeaderModel = new DefaultTableColumnModel()
    {
       boolean first = true;
       public void addColumn(TableColumn tc)
       {
          if( first )
          {
             tc.setMaxWidth(widthColumnZero);
             tc.setPreferredWidth(widthColumnZero);
             super.addColumn(tc);
             first = false;
          }
       }
    };

    // create column model for data rows
    TableColumnModel columnModel = new DefaultTableColumnModel()
    {
       boolean first = true;
       public void addColumn(TableColumn tc)
       {
          if( first )
          {
             first = false;
          }
          else
          {
             tc.setMaxWidth(200);
             tc.setPreferredWidth(150);
             super.addColumn(tc);
          }
       }
    };

    // create the header table
    JTable headerTable = new JTable(dataModel, rowHeaderModel);
    headerTable.createDefaultColumnsFromModel();
    headerTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    headerTable.setMaximumSize(new Dimension(widthColumnZero,10000));
    headerTable.setBackground(Color.lightGray);

    // create the table
    JTable dataTable = new JTable(dataModel, columnModel);
    dataTable.createDefaultColumnsFromModel();
    dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    //share the same selectionModel between tables
    dataTable.setSelectionModel( headerTable.getSelectionModel() );

    // setup panel to hold header table
    JViewport viewport = new JViewport();
    viewport.setView(headerTable);
    viewport.setPreferredSize(headerTable.getMaximumSize());

    // create tableView ScrollPane
    tableView = new JScrollPane(dataTable);
    tableView.setRowHeader(viewport);

  }

 /**
  * method to export table to file
  */
  public void exportTable()
  {
     String crlf = System.getProperty("line.separator");
     String delimiter = ",";

     // open file dialog to select file for exporting
     JFileChooser fileChooser = new JFileChooser();
     fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
     fileChooser.setDialogTitle("Select File");
     fileChooser.setApproveButtonToolTipText("Select File to Export");
     fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
     fileChooser.setApproveButtonText("Export");
     fileChooser.setMultiSelectionEnabled(false);
     if(fileChooser.showDialog(this,"Export") == JFileChooser.APPROVE_OPTION)
     {
        File xFile = fileChooser.getSelectedFile();

        // try to export to file
        try
        {
           xFile.createNewFile();   // try to ensure we can write the file
           PrintWriter writer = new PrintWriter(new FileWriter(xFile));

           TableModel model = printTable.getModel();

           // print header line
           for(int c=0; c<model.getColumnCount(); c++)
           {
              String colName = model.getColumnName(c);
              if( colName==null ) colName = "";
              writer.print("\""+colName+"\"");
              writer.print( (c<model.getColumnCount()-1) ? delimiter : crlf );
           }

           // print rows
           for(int r=0; r<model.getRowCount(); r++)
           {
              for(int c=0; c<model.getColumnCount(); c++)
              {
                 Object field = model.getValueAt(r,c);
                 if( field==null ) field = "";
                 writer.print(field);
                 writer.print( (c<model.getColumnCount()-1) ? delimiter : crlf );
              }
           }

           writer.close();
        }
        catch(Exception ex)
        {

        }
     }
  }


 /**
  * main routine for running viewer in standalone mode
  */
  public static void main(String[] args)
  {
     JFrame frame = new JFrame("DataSet Viewer");

    // place frame in center of screen
    Dimension scrnSize = Toolkit.getDefaultToolkit().getScreenSize();
    int w = (int)(scrnSize.getWidth()/2.5);
    int h = (int)(scrnSize.getHeight()/1.5);
    int x = (int)((scrnSize.getWidth()-w)/2.0);
    int y = (int)((scrnSize.getHeight()-h)/2.0);
    frame.setBounds(x,y,w,h);
    frame.setResizable(true);

    DataSetIfc[] sets = new DataSetIfc[3];

    sets[0] = createDateDataSetIfc("set 1", "feet", 0.0, 1.0, 50);
    sets[1] = createDataSetIfc("set 2", "meters", 0.0, 0.3048, 45);
    sets[2] = createDataSetIfc("set 3", "inches", 0.0, 12.0, 40);

    try
    {
       frame.getContentPane().add( new DataSetViewer(frame, sets));
    }
    catch(Exception ex)
    {
       JOptionPane.showMessageDialog(frame, "Invalid dataSets", "Error Message", JOptionPane.ERROR_MESSAGE);
    }

    frame.addWindowListener(new WindowAdapter()
    {
       public void windowClosing(WindowEvent ev)
       {
          ev.getWindow().dispose();
          System.exit(0);
       }
    });

    frame.setVisible(true);
  }

 /**
  * inner class to create a printable table
  */
  private class PrintableTable extends JTable implements Printable
  {
     PrintableTable(TableModel dataModel)
     {
        super(dataModel);
     }

     PrintableTable(TableModel dataModel, TableColumnModel tableColumnModel)
     {
        super(dataModel,tableColumnModel);
     }

    /**
     * copies table to printer
     */
     public int print(Graphics g, PageFormat pageFormat, int pageIndex)
        throws PrinterException
     {
        Graphics2D  g2 = (Graphics2D) g;
        g2.setColor(Color.black);
          int fontHeight=g2.getFontMetrics().getHeight();
        int fontDesent=g2.getFontMetrics().getDescent();

        double pageHeight = pageFormat.getImageableHeight()-fontHeight;
        double pageWidth = pageFormat.getImageableWidth();
        double tableWidth = (double) getColumnModel().getTotalColumnWidth();
        double scale = 1;

        // set scale to go from table width to page width
        if (tableWidth >= pageWidth)
        {
           scale =  pageWidth / tableWidth;
        }

        double headerHeightOnPage = getTableHeader().getHeight()*scale;
        double tableWidthOnPage = tableWidth*scale;

        //   double oneRowHeight=(getRowHeight() + getRowMargin())*scale;  // margin is included in row height
        double oneRowHeight=(getRowHeight())*scale;
        int numRowsOnAPage = (int)((pageHeight-headerHeightOnPage)/oneRowHeight);
        double pageHeightForTable = oneRowHeight*numRowsOnAPage;
        int totalNumPages = (int)Math.ceil(((double)getRowCount())/numRowsOnAPage);

        if(pageIndex >= totalNumPages)
        {
           return NO_SUCH_PAGE;
        }

        g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

        // print page number if totalNumPages > 1
        if(totalNumPages > 1)
        {
           g2.drawString("Page: "+(pageIndex+1),
                       (int)pageWidth/2-35,
                       (int)(pageHeight+fontHeight-fontDesent));
        }

        g2.translate(0f,headerHeightOnPage);
        g2.translate(0f,-pageIndex*pageHeightForTable);

        g2.setClip(0, (int)(pageHeightForTable*pageIndex),(int)
            Math.ceil(tableWidthOnPage),
                 (int) Math.ceil(pageHeightForTable));

        g2.scale(scale,scale);
        paint(g2);

        g2.scale(1/scale,1/scale);
        g2.translate(0f,pageIndex*pageHeightForTable);
        g2.translate(0f, -headerHeightOnPage);
        g2.setClip(0, 0,(int) Math.ceil(tableWidthOnPage),
              (int)Math.ceil(headerHeightOnPage));
        g2.scale(scale,scale);
        getTableHeader().paint(g2);

        return Printable.PAGE_EXISTS;
     }
  }


  /**
   * method that returns a DataSet from a DoubleSeriesValue
   */
   private static DataSetIfc createDataSetIfc(String name, String units, double minVal, double stepVal, int count )
   {
      final String fname = name;
      final String funits = units;
      final double fminVal = minVal;
      final double fstepVal = stepVal;
      final int fcount = count;

      // create DataSetIfc
      DataSetIfc set = new DataSetIfc()
      {
         boolean isOpen = false;

         public String getName()
         {
            return fname;
         }

         public String getUnits()
         {
            return funits;
         }

         public int getNumElements() throws java.lang.Exception
         {
            if( fcount<0 ) throw new java.lang.Exception("Invalid number of Elements");
            return fcount;
         }

         public void open() throws Exception
         {
            if( isOpen ) throw new java.lang.Exception("Dataset is already open");
            isOpen = true;
         }

         public void close() throws Exception
         {
            if( isOpen ) throw new java.lang.Exception("Dataset has not been opened");
            isOpen = false;
         }

         public double getElement(int i) throws java.lang.Exception,
                               java.util.NoSuchElementException
         {
            if( !isOpen ) throw new java.lang.Exception("Dataset has not been opened");
            if( i < 0 ) throw new java.lang.Exception("Invalid index number");
            if( i >= fcount ) throw new java.util.NoSuchElementException("Invalid index number");

            return (fminVal + (i)*fstepVal);
         }

         public String getContentDescription()
         {
            return "Example of DataSetIfc for testing";
         }
      };

      return set;
   }



  /**
   * method that returns a Labeled DataSet for testing
   */
   private static LabeledDataSetIfc createLabeledDataSetIfc(String name, String units, double minVal, double stepVal, int count )
   {
      final String fname = name;
      final String funits = units;
      final double fminVal = minVal;
      final double fstepVal = stepVal;
      final int fcount = count;

      // create DataSetIfc
      LabeledDataSetIfc set = new LabeledDataSetIfc()
      {
         boolean isOpen = false;

         public String getName()
         {
            return fname;
         }

         public String getUnits()
         {
            return funits;
         }

         public int getNumElements() throws java.lang.Exception
         {
            if( fcount<0 ) throw new java.lang.Exception("Invalid number of Elements");
            return fcount;
         }

         public void open() throws Exception
         {
            if( isOpen ) throw new java.lang.Exception("Dataset is already open");
            isOpen = true;
         }

         public void close() throws Exception
         {
            if( isOpen ) throw new java.lang.Exception("Dataset has not been opened");
            isOpen = false;
         }

         public double getElement(int i) throws java.lang.Exception,
                               java.util.NoSuchElementException
         {
            if( !isOpen ) throw new java.lang.Exception("Dataset has not been opened");
            if( i < 0 ) throw new java.lang.Exception("Invalid index number");
            if( i >= fcount ) throw new java.util.NoSuchElementException("Invalid index number");

            return (fminVal + (i)*fstepVal);
         }

         public String getLabel(int i) throws java.lang.Exception,
                             java.util.NoSuchElementException
         {
            return "row "+Integer.toString(i);
         }

         public String getContentDescription()
         {
            return "Example of LabeledDataSetIfc for testing";
         }

      };

      return set;
   }

  /**
   * method that returns a DateDataSet for testing
   */
   private static DateDataSetIfc createDateDataSetIfc(String name, String units, double minVal, double stepVal, int count )
   {
      final String fname = name;
      final String funits = units;
      final double fminVal = minVal;
      final double fstepVal = stepVal;
      final int fcount = count;

      // create DataSetIfc
      DateDataSetIfc set = new DateDataSetIfc()
      {
         boolean isOpen = false;
         GregorianCalendar cal = new GregorianCalendar();

         public String getName()
         {
            return fname;
         }

         public String getUnits()
         {
            return funits;
         }

         public int getNumElements() throws java.lang.Exception
         {
            if( fcount<0 ) throw new java.lang.Exception("Invalid number of Elements");
            return fcount;
         }

         public void open() throws Exception
         {
            if( isOpen ) throw new java.lang.Exception("Dataset is already open");
            isOpen = true;
         }

         public void close() throws Exception
         {
            if( isOpen ) throw new java.lang.Exception("Dataset has not been opened");
            isOpen = false;
         }

         public double getElement(int i) throws java.lang.Exception,
                               java.util.NoSuchElementException
         {
            if( !isOpen ) throw new java.lang.Exception("Dataset has not been opened");
            if( i < 0 ) throw new java.lang.Exception("Invalid index number");
            if( i >= fcount ) throw new java.util.NoSuchElementException("Invalid index number");

            return (fminVal + (i)*fstepVal);
         }

         public Date getDate(int i) throws java.lang.Exception,
                             java.util.NoSuchElementException
         {
            Date startDate = dateFormatter.parse("07/01/2003 00:00:00");
            cal.setTime( startDate );

            // add i hours to date
            cal.add( java.util.Calendar.HOUR, i );

            return cal.getTime();
         }

         public String getContentDescription()
         {
            return "Example of DateDataSetIfc for testing";
         }
      };

      return set;
   }
}
