package gov.epa.mims.analysisengine.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import gov.epa.mims.analysisengine.tree.OutlineType;


/**
 * Editor for outline type
 *
 * @author Alison Eyth, Prashant Pai
 * @version $Id: OutlineTypeEditor.java,v 1.3 2005/09/21 14:19:48 parthee Exp $
 *
 **/
public class OutlineTypeEditor
extends OptionDialog
{
   /******************************************************
    *
    * fields
    *
    *****************************************************/

  /** a panel to set the properties plot outline **/
   private OutlineStylePanel plotPanel;

   /** a panel to set the properties figure outline **/
//   private OutlineStylePanel figurePanel;

   /** a panel to set the properties inner outline **/
   private OutlineStylePanel innerPanel;

   /** a panel to set the properties outer outline **/
   private OutlineStylePanel outerPanel;


   /** the outlinetype to be edited **/
   private OutlineType outlineType = null;

   /** image icon for the displaySizeHelp icons */
   private static ImageIcon helpImageIcon = MultipleEditableTablePanel.createImageIcon(
         "/gov/epa/mims/analysisengine/gui/icons/outlineHelp.jpeg");

   /** to view the image */
   private ImageViewer imageViewer;

   /******************************************************
    *
    * methods
    *
    *****************************************************/

   /**
    * constructor that edits a outline type
    * @param anOutlineType the outlinetype to be edited
    */
   public OutlineTypeEditor(OutlineType anOutlineType)
   {
      super();
      initialize();
      setDataSource(anOutlineType, "");
      setLocation(ScreenUtils.getPointToCenter(this));
   }//OutlineTypeEditor(OutlineType)

   /**
    * constructor need for class.newInstance
    */
   public OutlineTypeEditor()
   {
      this(null);
   }//OutlineTypeEditor()

   public void setDataSource(Object dataSource, String optionName)
   {
      this.outlineType = (OutlineType)dataSource;
      super.setDataSource(dataSource, optionName);
      if (outlineType != null)
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
      plotPanel = new OutlineStylePanel("Plot");
      //Decided to hide this from the user
      //figurePanel = new OutlineStylePanel("Figure");
      //figurePanel.setEnabled(false);
      //In tree package this is reffered as Inner Margin but here as Figure
      innerPanel = new OutlineStylePanel("Figure");
      innerPanel.setEnabled(false);
      //In tree package this is reffered as Outer Margin but here as Page
      outerPanel = new OutlineStylePanel("Page");
      outerPanel.setEnabled(false);

      JPanel titlePanel = new JPanel();
      JPanel enablePanel = new JPanel();
      JPanel stylePanel = new JPanel();
      JPanel colorPanel = new JPanel();
      JPanel widthPanel = new JPanel();

      titlePanel.setLayout(new GridLayout(5, 1));
      enablePanel.setLayout(new GridLayout(5, 1));
      stylePanel.setLayout(new GridLayout(5, 1));
      colorPanel.setLayout(new GridLayout(5, 1));
      widthPanel.setLayout(new GridLayout(5, 1));

      JComponent[] plotComps = plotPanel.getGUIComponents();
//      JComponent[] figureComps = figurePanel.getGUIComponents();
      JComponent[] innerComps = innerPanel.getGUIComponents();
      JComponent[] outerComps = outerPanel.getGUIComponents();

      titlePanel.add(new JLabel("Border"));
      titlePanel.add(plotComps[0]);
//      titlePanel.add(figureComps[0]);
      titlePanel.add(innerComps[0]);
      titlePanel.add(outerComps[0]);

      enablePanel.add(new JLabel("Draw?"));
      enablePanel.add(plotComps[1]);
//      enablePanel.add(figureComps[1]);
      enablePanel.add(innerComps[1]);
      enablePanel.add(outerComps[1]);

      stylePanel.add(new JLabel("Line Style", JLabel.CENTER));
      stylePanel.add(plotComps[2]);
//      stylePanel.add(figureComps[2]);
      stylePanel.add(innerComps[2]);
      stylePanel.add(outerComps[2]);

      colorPanel.add(new JLabel("Color"));
      colorPanel.add(plotComps[3]);
//      colorPanel.add(figureComps[3]);
      colorPanel.add(innerComps[3]);
      colorPanel.add(outerComps[3]);

      widthPanel.add(new JLabel("Line Width"));
      widthPanel.add(plotComps[4]);
//      widthPanel.add(figureComps[4]);
      widthPanel.add(innerComps[4]);
      widthPanel.add(outerComps[4]);

      JPanel middlePanel = new JPanel();
      middlePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLoweredBevelBorder(),
            BorderFactory.createEmptyBorder(3, 3, 3, 3)));
      middlePanel.setLayout(new BoxLayout(middlePanel,BoxLayout.X_AXIS));
      middlePanel.add(titlePanel);
      middlePanel.add(Box.createHorizontalStrut(20));
      middlePanel.add(enablePanel);
      middlePanel.add(Box.createHorizontalStrut(20));
      middlePanel.add(stylePanel);
      middlePanel.add(Box.createHorizontalStrut(20));
      middlePanel.add(colorPanel);
      middlePanel.add(Box.createHorizontalStrut(20));
      middlePanel.add(widthPanel);

      ActionListener helpListener = getHelpListener();
      JPanel buttonPanel = getButtonPanel(true,helpListener);

      Container contentPane = getContentPane();
      contentPane.setLayout(new BorderLayout());


      contentPane.add(middlePanel, BorderLayout.CENTER);
      contentPane.add(buttonPanel, BorderLayout.SOUTH);

      setModal(true);
      //pack();
   }//initialize()

   /** a helper method to create help listener
   */
   private ActionListener getHelpListener()
   {
      ActionListener helpListener = new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            imageViewer = new ImageViewer(OutlineTypeEditor.this, helpImageIcon);
            imageViewer.setVisible(true);
         }
      };
      return helpListener;
   }//getHelpListener()


   /**
    * @pre the object i.e. BarType is not null
    */
   protected void initGUIFromModel()
   {
      plotPanel.initModel(outlineType,OutlineType.PLOT);
//      figurePanel.initModel(outlineType,OutlineType.FIGURE);
      innerPanel.initModel(outlineType,OutlineType.INNER);
      outerPanel.initModel(outlineType,OutlineType.OUTER);

   }//initGUIFromModel()

   protected void saveGUIValuesToModel() throws Exception
   {
      plotPanel.saveModel(outlineType,OutlineType.PLOT);
//      figurePanel.saveModel(outlineType,OutlineType.FIGURE);
      innerPanel.saveModel(outlineType,OutlineType.INNER);
      outerPanel.saveModel(outlineType,OutlineType.OUTER);

   }//saveGUIValuesToModel()


   /**
    * A helper method to load the icons
    * @param path location of the image icon.
    * @return ImageIcon
    */
   public ImageIcon createImageIcon(String path)
   {
      java.net.URL imgURL = getClass().getResource(path);

      if (imgURL != null)
      {
         return new ImageIcon(imgURL);
      }
      else
      {
         System.err.println("Could not find file: " + path + " in classpath.");
         return null;
      }
   } // createImageIcon()


   public static void main(String[] args)
   {
      DefaultUserInteractor.set(new GUIUserInteractor());
      try
      {
         OutlineTypeEditor outlineTypeEditor = new OutlineTypeEditor(new OutlineType());
         outlineTypeEditor.setVisible(true);
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }//main()

}

