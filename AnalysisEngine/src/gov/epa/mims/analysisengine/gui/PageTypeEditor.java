package gov.epa.mims.analysisengine.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import gov.epa.mims.analysisengine.tree.PageType;


/**
 * Editor for the page type
 *
 * @author Alison Eyth, Prashant Pai
 * @version $Id: PageTypeEditor.java,v 1.3 2005/09/21 14:19:48 parthee Exp $
 *
 **/
public class PageTypeEditor
    extends OptionDialog
{
   /******************************************************
    *
    * fields
    *
    *****************************************************/

   /** a textfield to set the filename **/
   private JTextField fileNameField = null;

   /** a panel to set the file format **/
   private StringChooserPanel formatPanel = null;

   /** a textfield to set the PDF reader **/
   //private JTextField pdfReaderField = null;

   /** the pagetype to be edited **/
   private PageType pageType = null;

   /******************************************************
    *
    * methods
    *
    *****************************************************/

   /**
    * constructor that edits a page type
    * @param aPageType the pagetype to be edited
    */
   public PageTypeEditor(PageType aPageType)
   {
     super();
     initialize();
     setDataSource(aPageType, "");
     setLocation(ScreenUtils.getPointToCenter(this));
   }//PageTypeEditor(PageType)

   /**
    * constructor need for class.newInstance
    */
   public PageTypeEditor()
   {
     this(null);
   }//PageTypeEditor()

   public void setDataSource(Object dataSource, String optionName)
   {
      this.pageType = (PageType)dataSource;
      super.setDataSource(dataSource, optionName);
      if (pageType != null)
      {
         initGUIFromModel();
      }
      pack();
      this.repaint();
   }

   /**
    * a private method to initialize the GUI
    */
   private void initialize()
   {
     this.setModal(true);
     Container contentPane = getContentPane();
     setTitle("Edit PageType Properties");

     contentPane.setLayout(new BorderLayout());
     JPanel mainPanel = new JPanel(new GridLayout(2, 1, 3, 3));
     contentPane.add(mainPanel, BorderLayout.CENTER);

     fileNameField = new JTextField(20);
     JPanel fileNamePanel = new JPanel();
 //    fileNamePanel.setBorder(AxisEditor.getCustomBorder(null));
     fileNamePanel.add(new JLabel("File Name:"));
     fileNamePanel.add(fileNameField);

     JButton browseButton = new JButton("Browse");
     browseButton.addActionListener(new ActionListener()
     {
       public void actionPerformed(ActionEvent ae)
       {
         JFileChooser fileChooser = new JFileChooser(fileNameField.getText());
         fileChooser.setMultiSelectionEnabled(false);
         fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
         int returnVal = fileChooser.showOpenDialog(PageTypeEditor.this);
         if (returnVal != JFileChooser.APPROVE_OPTION) return;
         fileNameField.setText(fileChooser.getSelectedFile().getAbsolutePath());
       }//actionPerformed()
     });
     fileNamePanel.add(browseButton);
     fileNamePanel.setToolTipText(
         "File Name is required is a file needs to be written out");

     formatPanel = new StringChooserPanel("Output Form:", false, new String[]
           {"PDF", "X11", "SCREEN"});
 //    formatPanel.setBorder(AxisEditor.getCustomBorder(null));

     mainPanel.add(formatPanel);
     mainPanel.add(fileNamePanel);

/*
     pdfReaderField = new JTextField(20);
     JPanel readerPanel = new JPanel();
     readerPanel.setAlignmentY(JPanel.CENTER_ALIGNMENT);
     readerPanel.add(new JLabel("PDF Reader:"));
     readerPanel.add(pdfReaderField);
     readerPanel.setBorder(AxisEditor.getCustomBorder(null));
     mainPanel.add(readerPanel);
*/
     // Create a new OK Cancel Panel with the OK and Cancel Action Listeners
     JPanel buttonPanel = new OKCancelPanel(
         new ActionListener()
         {
           public void actionPerformed(ActionEvent ae)
           {
             try
             {
               saveGUIValuesToModel();
               dispose();
             }
             catch(Exception e)
             {
               DefaultUserInteractor.get().notifyOfException(PageTypeEditor.this,"Error settting "
                   + "Page Type Properties", e, UserInteractor.ERROR);
             }
           }
         },
         new ActionListener()
         {
           public void actionPerformed(ActionEvent ae)
           {
             dispose();
             initGUIFromModel();
           }
         }, getRootPane());
     contentPane.add(buttonPanel, BorderLayout.SOUTH);
     //pack();
   }//initialize()

   /**
    * @pre the object i.e. BarType is not null
    */
   protected void initGUIFromModel()
   {
     fileNameField.setText(pageType.getFilename());
     formatPanel.setValue(pageType.getForm());
     //pdfReaderField.setText(pageType.getPDFreader());
   }//initGUIFromModel()

   protected void saveGUIValuesToModel() throws Exception
   {
     pageType.setForm(formatPanel.getValue());
     pageType.setFilename(fileNameField.getText());
     //pageType.setPDFreader(pdfReaderField.getText());
   }//saveGUIValuesToModel()


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

}

