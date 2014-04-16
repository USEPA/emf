package gov.epa.mims.analysisengine.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.tree.AnalysisOptions;
import gov.epa.mims.analysisengine.tree.Page;
import gov.epa.mims.analysisengine.tree.PageType;

/**
 * A panel that allows the user to edit options for a page.  This is similar
 * to PageTypeEditor.  PageTypeEditor should be updated to use this panel.
 *
 * @author Alison Eyth, Prashant Pai
 * @version $Id: PageOptionsPanel.java,v 1.3 2007/05/31 14:29:32 qunhe Exp $
 *
 **/
public class PageOptionsPanel
    extends JPanel
implements
gov.epa.mims.analysisengine.tree.AnalysisOptionConstantsIfc,
gov.epa.mims.analysisengine.tree.PageConstantsIfc
{
   /******************************************************
    *
    * fields
    *
    *****************************************************/

   /** a textfield to set the filename **/
   private JTextField fileNameField = null;

   /** a panel to set the file format **/
//   private StringChooserPanel formatPanel = null;

   /** a textfield to set the PDF reader **/
   //private JTextField pdfReaderField = null;

   /** the analysis options for the page */
   private AnalysisOptions pageOptions = null;

   /** the pagetype to be edited **/
   private PageType pageType = null;

   /** The file chooser for browsing for files. */
   private JFileChooser fileChooser = new JFileChooser();

   /** Hashtable of file filters for fast lookup. */
   private HashMap fileFilters = new HashMap();

   /** The PagePanel that contains this panel. Used to call createPlot
    * from the file chooser.  */
   protected TreeDialog parent = null;

   /** A constant String to indicate that no filename has been entered. */
   //public static final String NOT_SAVED = "< not saved >";

   /******************************************************
    *
    * methods
    *
    *****************************************************/

   /**
    * constructor that edits a page type
    * @param pageOptions the analysis options for the page
    */
   public PageOptionsPanel(AnalysisOptions pageOptions, TreeDialog parent)
   {
     super();
     this.pageOptions = pageOptions;
     this.parent = parent;
     initialize();
     setDataSource(pageType);
   }//PageTypeEditor(PageType)

   /**
    * constructor need for class.newInstance
    */
   public PageOptionsPanel()
   {
     this(null, null);
   }//PageTypeEditor()


   /**
    * This method is used when a new tree is read in from a serialized file
    * after the GUI is already up.
    * @param pageType PageType that is the new PageType to use.
    * @param pageOptions AnalysisOptions that are the new options to use.
    */
   public void setDataModel(PageType newPageType, AnalysisOptions newPageOptions)
   {
      this.pageOptions = newPageOptions;
      setDataSource(newPageType);
   }


   public void setDataSource(Object dataSource)
   {
      this.pageType = (PageType)dataSource;
      if (pageType != null)
      {
         initGUIFromModel();
      }
      //pack();
      this.repaint();
   }

   /**
    * a private method to initialize the GUI
    */
   private void initialize()
   {
      // Initialize the page options.
     if (pageOptions == null)
     {
        pageOptions = new AnalysisOptions();
        pageType = new PageType();
        pageOptions.addOption(PAGE_TYPE, pageType);
     }
     else
     {
        pageType = (PageType)pageOptions.getOption(PAGE_TYPE);
        pageType.setForm(SCREEN);
     }

     setDefaultPlotFileNameIfNeeded();
     setBorder(AxisNumericEditor.getCustomBorder("Page Options"));
     setLayout(new BorderLayout());

     fileNameField = new JTextField(20);

     // File Chooser set up.
     fileChooser.setMultiSelectionEnabled(false);
     fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

     // Add file filters to the hashtable
     fileFilters.put(PageType.JPG_EXT, new ImageFileFilter(PageType.JPG_EXT, "JPEG Files (*.jpg)"));
     fileFilters.put(PageType.PNG_EXT, new ImageFileFilter(PageType.PNG_EXT, "PNG Files (*.png)"));
     fileFilters.put(PageType.PDF_EXT, new ImageFileFilter(PageType.PDF_EXT, "Adobe PDF Files (*.pdf)"));
     fileFilters.put(PageType.PS_EXT, new ImageFileFilter(PageType.PS_EXT, "PostScript Files (*.ps)"));
     fileFilters.put(PageType.PTX_EXT, new ImageFileFilter(PageType.PTX_EXT, "LATEX Picture Files (*.ptx)"));

     // Remove all file filters from the file chooser.
     FileFilter[] oldFilters = fileChooser.getChoosableFileFilters();
     for (int i = 0; i < oldFilters.length; i++)
        fileChooser.removeChoosableFileFilter(oldFilters[i]);

     // Add our file filters to the file chooser.
     Collection c = fileFilters.values();
     Iterator iter = c.iterator();
     while (iter.hasNext())
        fileChooser.addChoosableFileFilter((FileFilter)iter.next());

     // Browse buttoon
     JButton browseButton = new JButton("Browse...");
     browseButton.addActionListener(new ActionListener()
     {
       public void actionPerformed(ActionEvent ae)
       {
          // Set the filename in the chooser.
          String filename = fileNameField.getText();
          if (filename != null)
             fileChooser.setSelectedFile(new File(filename));

          int returnVal = fileChooser.showSaveDialog(PageOptionsPanel.this);
          if (returnVal != JFileChooser.APPROVE_OPTION)
             return;

          filename = fileChooser.getSelectedFile().getAbsolutePath();
          // if there is no file name, we can't save it yet
          if (filename.length() == 0)
          {
            DefaultUserInteractor.get().notify(PageOptionsPanel.this,"Invalid filename",
                 "Please enter a non-empty file name.", UserInteractor.ERROR);
          }
          else
          {
             // Othrwise, add the correct file extension if it's not on the
             // filename.
             filename = PageType.addFileExtensionIfNeeded(filename,
                ((ImageFileFilter)fileChooser.getFileFilter()).getExtension());
             fileChooser.setSelectedFile(new File(filename));
             fileNameField.setText(filename);
             // false means write the plot to a file.
             parent.createPlotFromGUI(false);
         }
       }//actionPerformed()
     });

     JButton saveButton = new JButton("Save");
     saveButton.addActionListener(new ActionListener()
     {
        public void actionPerformed(ActionEvent ae)
        {
           {
              parent.createPlotFromGUI(false);
           }
        }
     }
     );

     JPanel fileNamePanel = new JPanel();
     fileNamePanel.setLayout(new BoxLayout(fileNamePanel, BoxLayout.X_AXIS));
     fileNamePanel.add(new JLabel("File Name:"));
     fileNamePanel.add(fileNameField);
     fileNamePanel.add(browseButton);
     fileNamePanel.add(saveButton);
     fileNamePanel.setToolTipText(
         "File Name is required is a file needs to be written out");

     JPanel mainPanel = new JPanel();
     mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
     mainPanel.add(fileNamePanel);
     add(mainPanel, BorderLayout.CENTER);
   }//initialize()


   /**
    * @return AnalysisOptions corresponding to the page
    */
   AnalysisOptions getOptions()
   {
      return pageOptions;
   }

   /**
    * @pre the object i.e. BarType is not null
    */
   protected void initGUIFromModel()
   {
     String fileName = pageType.getFilename();
     // For some reason, forward slashes were required by R, so switch back if
     // we're on a PC
     if (File.pathSeparatorChar == '\\')
     {
       if (fileName.indexOf('/') > 0)
       {
          fileName = fileName.replace('/','\\');
       }
     }
//System.out.println("fileName = " + fileName);
     fileNameField.setText(fileName);

   }//initGUIFromModel()


   /**
    * Get the GUI values and place them in the model.
    * @param onScreen boolean that is true if we are writing to the screen.
    * @throws Exception
    */
   protected void saveGUIValuesToModel(boolean onScreen) throws Exception
   {
//     pageType.setForm(formatPanel.getValue());

      // Don't check the filename if the user is plotting to the screen.
      if (onScreen)
         return;

      // Get the type from the filename extension.
      String filename = fileNameField.getText();
      int lastDot = filename.lastIndexOf('.');
      if (lastDot > 0)
      {
         String ext = filename.substring(lastDot+1);
         String format = PageType.getFormatForExtension(ext);
         if (format == null)
         {
            throw new Exception(
                  "Please enter a filename with one of the following extensions :" +
               "(*.jpg, *.pdf, *.png, *.ps, *.ptx).");
         }
         else
         {
            pageType.setForm(format);
         }
      } // if (lastDot > 0)
      else if (filename.length() > 0)
      {
         throw new Exception(
               "Please enter a filename with one of the following extensions :" +
               "(*.jpg, *.pdf, *.png, *.ps, *.ptx).");
      }

      pageType.setFilename(filename);
     //pageType.setPDFreader(pdfReaderField.getText());
   }//saveGUIValuesToModel()

   /**
    * To be called from higher level to notify that it is time to store
    * the GUI choices to the model
    * @param onScreen boolean that is true if the plot should be drawn on screen.
    * @throws Exception
    */
   protected void storeGUIValues(boolean onScreen) throws Exception
   {
      saveGUIValuesToModel(onScreen);
   }


   public static void main(String[] args)
   {
     DefaultUserInteractor.set(new GUIUserInteractor());
     try
     {
       PageTypeEditor pageTypeEditor = new PageTypeEditor(new PageType());
       pageTypeEditor.setVisible(true);
     }
     catch(Exception e)
     {
       e.printStackTrace();
     }
   }//main()


   /**
    * Set the default name for the plot if it i null.
    */
   private void setDefaultPlotFileNameIfNeeded()
   {
      if (pageType.getFilename() == null)
         pageType.setFilename("");
   }



   /**
    * A file filter that shows only certain files for use in the file chooser.
    */
   class ImageFileFilter extends FileFilter implements Serializable
   {
	   static final long serialVersionUID = 1;
	   
      /** The extension that this class will accept. */
      private String extension = null;

      /** The decription to return to the FileChooser. */
      private String description = null;

      /**
       * Constructor.
       */
      public ImageFileFilter(String ext, String desc)
      {
         extension = ext;
         description = desc;
      }


      /**
       * Return true for directories and files with the proper extension
       * (case insensitive).
       * @param file File whose extension should be checked.
       * @return true if the file is a directory or has an extension that
       *    matches the one for this instance.
       */
      public boolean accept(File file)
      {
         if (file.isDirectory())
            return true;

         String filename = file.getName();
         int lastDot = filename.lastIndexOf('.');
         if (lastDot > 0)
         {
            String fileExt = filename.substring(lastDot + 1);
            return (fileExt.equalsIgnoreCase(extension));
         }
         else
            return false;
      }

      /**
       * Return the file extension.
       * @return String that is the fiel extension.
       */
      public String getExtension()
      {
         return extension;
      }

      /**
       * Return a human readable description for these files.
       * @return String
       */
      public String getDescription()
      {
         return description;
      }
   } // class ImageFileFilter
}


