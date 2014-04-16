package gov.epa.mims.analysisengine.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import gov.epa.mims.analysisengine.tree.GridType;

/**
 * Editor for grid type
 *
 * @author Alison Eyth, Prashant Pai
 * @version $Id: GridTypeEditor.java,v 1.3 2005/09/21 14:16:01 parthee Exp $
 *
 **/
public class GridTypeEditor
    extends OptionDialog
{
   /******************************************************
    *
    * fields
    *
    *****************************************************/

   /** a panel for the boolean value "draw Grids?" **/
   //private BooleanValuePanel drawGridPanel = null;
   private JCheckBox drawGridPanel = null;

   /** a panel to set the line style for the grid **/
   private StringChooserPanel stylePanel = null;

   /** a panel to set the width of the lines for the grid **/
   private DoubleValuePanel widthPanel = null;

   /** a panel to set the color of the grid lines **/
   private ColorValuePanel colorPanel = null;

   /** a panel to set the number of cells in x direction **/
   private IntegerValuePanel xCellsPanel = null;

   /** a panel to set the number of cells in y direction **/
   private IntegerValuePanel yCellsPanel = null;

   /** the gridtype to be edited **/
   private GridType gridType = null;

   /******************************************************
    *
    * methods
    *
    *****************************************************/

   /**
    * constructor that edits a gridtype
    * @param aGridType the gridtype to be edited
    */
   public GridTypeEditor(GridType aGridType)
   {
     super();
     initialize();
     setDataSource(aGridType, "");
     setLocation(ScreenUtils.getPointToCenter(this));
   }//BarTypeEditor()

   /**
    * constructor need for class.newInstance
    */
   public GridTypeEditor()
   {
     this(null);
   }

   public void setDataSource(Object dataSource, String optionName)
   {
      this.gridType = (GridType)dataSource;
      super.setDataSource(dataSource, optionName);
      if (gridType != null)
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
     setTitle("Edit GridType Properties");

     contentPane.setLayout(new BorderLayout());
     JPanel topPanel = new JPanel();
     topPanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
     drawGridPanel = new JCheckBox("Draw grids?");
     drawGridPanel.addItemListener(new ItemListener(){
       public void itemStateChanged(ItemEvent ie)
       {
         boolean enable = (ie.getStateChange() == ItemEvent.SELECTED);
         colorPanel.setEnabled(enable);
         stylePanel.setEnabled(enable);
         widthPanel.setEnabled(enable);
         xCellsPanel.setEnabled(enable);
         yCellsPanel.setEnabled(enable);
       }
     });
     topPanel.add(drawGridPanel);
     contentPane.add(topPanel, BorderLayout.NORTH);

     //JPanel middlePanel = new JPanel(new GridLayout(2, 1, 3, 3));
     JPanel middlePanel = new JPanel();
     contentPane.add(middlePanel, BorderLayout.CENTER);

     JPanel linePanel = new JPanel();
     // AME: no more AxisEditor
     //linePanel.setBorder(AxisEditor.getCustomBorder("Lines"));
     middlePanel.add(linePanel);

     stylePanel = new StringChooserPanel("Style:", true,
         new String[]{"SOLID", "DASHED", "DOTTED", "DOTDASH", "LONGDASH", "TWODASH"});
     stylePanel.setToolTipText("The linestyle for grid lines");
     linePanel.add(stylePanel);

     widthPanel = new DoubleValuePanel("Width:", true, 0, Double.POSITIVE_INFINITY);
     widthPanel.setToolTipText("Width for grid lines should be greater than 0");
     linePanel.add(widthPanel);

     colorPanel = new ColorValuePanel("Color:", true);
     linePanel.add(colorPanel);

     JPanel noCellsPanel = new JPanel();
     // AME: no more AxisEditor
     //noCellsPanel.setBorder(AxisEditor.getCustomBorder("Number of Cells"));
     //middlePanel.add(noCellsPanel);

     xCellsPanel = new IntegerValuePanel("in X direction:", true, 0,
      Integer.MAX_VALUE);
     noCellsPanel.add(xCellsPanel);

     yCellsPanel = new IntegerValuePanel("in Y direction:", true, 0,
      Integer.MAX_VALUE);
     noCellsPanel.add(yCellsPanel);

     contentPane.add(getButtonPanel(), BorderLayout.SOUTH);
     //pack();
   }//initialize()

   /**
    * @pre the object i.e. gridType is not null
    */
   protected void initGUIFromModel()
   {
     drawGridPanel.setSelected(gridType.getDraw());
     stylePanel.setValue(gridType.getLineStyle());
     widthPanel.setValue(gridType.getLineWidth());
     colorPanel.setValue(gridType.getColor());
     xCellsPanel.setValue(gridType.getNumberXcells());
     yCellsPanel.setValue(gridType.getNumberYcells());
   }//initGUIFromModel()

   protected void saveGUIValuesToModel() throws Exception
   {
     gridType.setDraw(drawGridPanel.isSelected());
     if (gridType.getDraw())
     {
       gridType.setLineStyle(stylePanel.getValue());
       gridType.setLineWidth(widthPanel.getValue());
       if (!colorPanel.getBackground().equals(colorPanel.getValue()))
         gridType.setColor(colorPanel.getValue());
       gridType.setNumberXcells(xCellsPanel.getValue());
       gridType.setNumberYcells(yCellsPanel.getValue());
     }//if (gridType.getDraw())
   }//saveGUIValuesToModel()

   public static void main(String[] args)
   {
     DefaultUserInteractor.set(new GUIUserInteractor());
     GridTypeEditor gridTypeEditor = new GridTypeEditor(new GridType());
     gridTypeEditor.setVisible(true);
   }//main()

}

