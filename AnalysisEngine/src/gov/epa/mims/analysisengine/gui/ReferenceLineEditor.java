package gov.epa.mims.analysisengine.gui;

import gov.epa.mims.analysisengine.tree.ReferenceLine;
import gov.epa.mims.analysisengine.tree.TextBorder;
import gov.epa.mims.analysisengine.tree.Text;
import java.awt.Container;
import java.util.ArrayList;
import java.util.EventObject;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.border.*;
import javax.swing.table.TableColumn;

/**
 * An editor for Reference Line
 * @author Parthee R Partheepan, Dan Gatti, Alyson Eyth
 * @version $Id: ReferenceLineEditor.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 */
public class ReferenceLineEditor extends OptionDialog

{
   /** The list of ReferenceLines that this GUi will edit. */
   protected ArrayList referenceLines = null;

   /** a panel to edit symbol sizes **/
   private MultipleEditableTablePanel tablePanel = null;

   /**an  interface to convert system strings into line style icons **/
   private PrettyOptionImageIconConverter lineStyleConverter;

   /** whether to draw the reference lines at all*/
   private JCheckBox enableTotalCheckBox = null;

   /** Label column number*/
   private final int LABEL_COLUMN_NUMBER = 1;

   /** Line Style column number*/
   private final int LINESTYLE_COLUMN_NUMBER = 2;

   /** Line Width column number*/
   private final int LINEWIDTH_COLUMN_NUMBER = 3;

   /** Color column number*/
   private final int COLOR_COLUMN_NUMBER = 4;

   /** Ref Line Parameter column number*/
   private final int ENABLE_REFLLINE_COLUMN_NUMBER = 5;

   /** Ref Line X1 column number*/
   private final int X1_COLUMN_NUMBER = 6;

   /** Ref Line Y1 column number*/
   private final int Y1_COLUMN_NUMBER = 7;

   /** Ref Line M column number*/
   private final int M_COLUMN_NUMBER = 8;

   /** Ref Line X2 column number*/
   private final int X2_COLUMN_NUMBER = 9;

   /** Ref Line Y2 column number*/
   private final int Y2_COLUMN_NUMBER = 10;

   /**
    * Consructor.
    */
   public ReferenceLineEditor(ArrayList refLines)
   {
      super();
      initialize();
      referenceLines = refLines;

      setDataSource(referenceLines, "");

   }

   public void setDataSource(Object dataSource, String optionName)
   {
      setTitle("Edit Reference Lines");
      super.setDataSource(dataSource, optionName);
      initGUIFromModel();

      pack();
      this.repaint();
   }

   /**
    * Build the GUI.
    */
   protected void initialize()
   {
      Container contentPane = getContentPane();
      contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

      //panel for enable refLineLabel
      JPanel enableRefLinePanel = new JPanel();
      enableTotalCheckBox = new JCheckBox("Enable Ref Lines",true);
      enableRefLinePanel.add(enableTotalCheckBox);
      enableTotalCheckBox.addItemListener(new ItemListener()
      {
         public void itemStateChanged(ItemEvent e)
         {

            if(((JCheckBox)e.getSource()).isSelected())
            {

               tablePanel.setEnabledAll(true);
            }
            else
            {
               tablePanel.setEnabledAll(false);
            }

         }
      });
      contentPane.add(enableRefLinePanel);

      //first column

      TextBorderTableColumn labelColumn = new TextBorderTableColumn(LABEL_COLUMN_NUMBER,"Label");
      labelColumn.setPreferredWidth(75);

      //setup the second  column: line styles
      lineStyleConverter = PrettyOptionImageIconConverter.getLineStyleConverter();
      ImageIcon[] lineStyleIcons  = lineStyleConverter.getAllPrettyOptions();
      ImageTableColumn lineStyleColumn = new ImageTableColumn(
      LINESTYLE_COLUMN_NUMBER, "LineStyle");
      lineStyleColumn.setPreferredWidth(100);
      lineStyleColumn.setInsertDefaultValue(true);
      lineStyleColumn.setValidChoices(lineStyleIcons);


      //setup the fourth column: width
      DoubleTableColumn widthColumn = new DoubleTableColumn(
      LINEWIDTH_COLUMN_NUMBER, "Width");
      widthColumn.setPreferredWidth(50);
      widthColumn.setBounds(0,10);


      //setup the fifth column: color
      ColorTableColumn colorColumn = new ColorTableColumn(
      COLOR_COLUMN_NUMBER, "Color");
      colorColumn.setPreferredWidth(50);


      //setup the sixth column
      BooleanTableColumn booleanColumn = new BooleanTableColumn(
      ENABLE_REFLLINE_COLUMN_NUMBER, "Enable");
      booleanColumn.setPreferredWidth(50);

      DoubleTableColumn x1Column = new DoubleTableColumn(X1_COLUMN_NUMBER, "X1");
      x1Column.setInsertDefaultValue(true);
      x1Column.setPreferredWidth(50);

      DoubleTableColumn y1Column = new DoubleTableColumn(Y1_COLUMN_NUMBER, "Y1");
      y1Column.setInsertDefaultValue(true);
      y1Column.setPreferredWidth(50);

      DoubleTableColumn mColumn = new DoubleTableColumn(M_COLUMN_NUMBER, "m");
      mColumn.setInsertDefaultValue(true);
      mColumn.setPreferredWidth(50);

      DoubleTableColumn x2Column = new DoubleTableColumn(M_COLUMN_NUMBER, "X2");
      x2Column.setInsertDefaultValue(true);
      x2Column.setPreferredWidth(50);

      DoubleTableColumn y2Column = new DoubleTableColumn(M_COLUMN_NUMBER, "Y2");
      y2Column.setInsertDefaultValue(true);
      y2Column.setPreferredWidth(50);

      try
      {
         lineStyleColumn.setDefaultValue(
            MultipleEditableTablePanel.createImageIcon(
            "/gov/epa/mims/analysisengine/gui/icons/lineStyles/solid.jpg"));
         widthColumn.setDefaultValue(new Double(1.0));
         colorColumn.setDefaultValue(Color.black);
         booleanColumn.setDefaultValue(new Boolean(true));
         x1Column.setDefaultValue(null);
         y1Column.setDefaultValue(null);
         mColumn.setDefaultValue(null);
         x2Column.setDefaultValue(null);
         y2Column.setDefaultValue(null);
      }
      catch(Exception e)
      {
         DefaultUserInteractor.get().notify(this, "Unexpected Object Type",e.getMessage(),
            UserInteractor.ERROR);
      }

      //creating a table consist of above columns
      TableColumn [] tableColumns =
      {labelColumn, lineStyleColumn, widthColumn, colorColumn, booleanColumn,
       x1Column, y1Column, mColumn, x2Column, y2Column};

       tablePanel = new MultipleEditableTablePanel(tableColumns);
       tablePanel.setRowHeight(20);
       tablePanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
       contentPane.add(tablePanel);

       contentPane.add(getButtonPanel());

       setModal(true);
       pack();
   }


   /**
    * Take all of the ReferenceLines in the referenceLines ArrayList and place
    * them in the GUI.
    */
   protected void initGUIFromModel()
   {

      int rowCount = tablePanel.getRowCount();
      for (int i=0; i< rowCount; i++)
      {
         tablePanel.removeRow(0);
      }

      if(referenceLines != null)
      {
         for(int i=0; i< referenceLines.size(); i++)
         {
            ReferenceLine line= (ReferenceLine)referenceLines.get(i);
            if(line != null)
            {
               tablePanel.insertRow(false);
               tablePanel.setValueAt((TextBorder)line.getLabel(),
               i, LABEL_COLUMN_NUMBER);
               tablePanel.setValueAt(lineStyleConverter.getPrettyOption(
               line.getLinestyle()), i, LINESTYLE_COLUMN_NUMBER);
               tablePanel.setValueAt(new Double(line.getLinewidth()), i,
               LINEWIDTH_COLUMN_NUMBER);
               tablePanel.setValueAt(line.getLinecolor(), i, COLOR_COLUMN_NUMBER);
               tablePanel.setValueAt(new Boolean(line.getEnable()),
               i, ENABLE_REFLLINE_COLUMN_NUMBER);
               if(!Double.isNaN(line.getX1()))
               {
                  tablePanel.setValueAt(new Double(line.getX1()), i,X1_COLUMN_NUMBER);
               }
               if(!Double.isNaN(line.getY1()))
               {
                  tablePanel.setValueAt(new Double(line.getY1()), i, Y1_COLUMN_NUMBER);
               }
               if(!Double.isNaN(line.getM()))
               {
                  tablePanel.setValueAt(new Double(line.getM()), i, M_COLUMN_NUMBER);
               }
               if(!Double.isNaN(line.getX2()))
               {
                  tablePanel.setValueAt(new Double(line.getX2()), i, X2_COLUMN_NUMBER);
               }
               if(!Double.isNaN(line.getY2()))
               {
                  tablePanel.setValueAt(new Double(line.getY2()), i, Y2_COLUMN_NUMBER);
               }
            }
         }
      }

   }




   /**
    * Take all of the ReferenceLines in the GUI and place them in the referenceLines
    * ArrayList to be returned.
    * @throws java.lang.Exception
    */
   protected void saveGUIValuesToModel() throws java.lang.Exception
   {
      if(referenceLines == null)
      {
         referenceLines = new ArrayList();
      }
      else
      {
         referenceLines.clear();
      }

      Object [] tempObjectsX1 = tablePanel.getValueAt(X1_COLUMN_NUMBER);
      Object [] tempObjectsY1 = tablePanel.getValueAt(Y1_COLUMN_NUMBER);
      Object [] tempObjectsM = tablePanel.getValueAt(M_COLUMN_NUMBER);
      Object [] tempObjectsX2 = tablePanel.getValueAt(X2_COLUMN_NUMBER);
      Object [] tempObjectsY2 = tablePanel.getValueAt(Y2_COLUMN_NUMBER);
      double [] doublesX1 = new double[tempObjectsX1.length];
      double [] doublesY1 = new double[tempObjectsY1.length];
      double [] doublesM = new double[tempObjectsM.length];
      double [] doublesX2 = new double[tempObjectsX2.length];
      double [] doublesY2 = new double[tempObjectsY2.length];

      for(int i=0; i<tempObjectsX1.length; i++)
      {

         if(((Double)tempObjectsX1[i] != null && (Double)tempObjectsY1[i] != null)
         && ( ((Double)tempObjectsM[i] != null && (Double)tempObjectsX2[i] == null
         && (Double)tempObjectsY2[i] == null) ||
         ((Double)tempObjectsM[i]==null && ((Double)tempObjectsX2[i] != null
         && (Double)tempObjectsY2[i] != null))))

         {
            //check for null in a cell
            ReferenceLine refLine = null;

            doublesX1[i] = ((Double)tempObjectsX1[i]).doubleValue();
            doublesY1[i] = ((Double)tempObjectsY1[i]).doubleValue();

            if((Double)tempObjectsX2[i] == null
            && (Double)tempObjectsY2[i] == null)
            {
               doublesM[i] = ((Double)tempObjectsM[i]).doubleValue();
               refLine = new ReferenceLine(doublesX1[i], doublesY1[i],doublesM[i]);
               referenceLines.add(refLine);
            }
            else if( (Double)tempObjectsM[i] == null)
            {
               doublesX2[i] = ((Double)tempObjectsX2[i]).doubleValue();
               doublesY2[i] = ((Double)tempObjectsY2[i]).doubleValue();
               refLine = new ReferenceLine(doublesX1[i], doublesY1[i],
               doublesX2[i], doublesY2[i]);
               referenceLines.add(refLine);
            }

            if(tablePanel.getValueAt(LABEL_COLUMN_NUMBER)!= null)
            {
               Object [] tempLabelObjects = tablePanel.getValueAt(LABEL_COLUMN_NUMBER);
               TextBorder txtBorder = (TextBorder)tempLabelObjects[i];
               ((ReferenceLine)referenceLines.get(i)).setLabel(txtBorder);
            }

            if(tablePanel.getValueAt(LINESTYLE_COLUMN_NUMBER)!= null)
            {
               Object [] tempLineStyleObjects = tablePanel.getValueAt(LINESTYLE_COLUMN_NUMBER);
               ImageIcon [] tempIcons = new ImageIcon[tempLineStyleObjects.length];
               lineStyleConverter = PrettyOptionImageIconConverter.getLineStyleConverter();

               tempIcons[i] = (ImageIcon)tempLineStyleObjects[i];
               ((ReferenceLine)referenceLines.get(i)).setLinestyle(
               lineStyleConverter.getSystemOption(tempIcons[i]));
            }
            if(tablePanel.getValueAt(LINEWIDTH_COLUMN_NUMBER)!= null)
            {
               Object [] tempLineWidthObjects = tablePanel.getValueAt(LINEWIDTH_COLUMN_NUMBER);
               double [] doubles = new double[tempLineWidthObjects.length];
               doubles[i] = ((Double)tempLineWidthObjects[i]).doubleValue();
               //System.out.println("line width : " + doubles[i]);
               ((ReferenceLine)referenceLines.get(i)).setLinewidth(doubles[i]);
            }
            if(tablePanel.getValueAt(COLOR_COLUMN_NUMBER)!= null)
            {
               Object [] tempLineColorObjects = tablePanel.getValueAt(COLOR_COLUMN_NUMBER);
               Color [] colors = new Color[tempLineColorObjects.length];
               colors[i] = ((Color)tempLineColorObjects[i]);
               ((ReferenceLine)referenceLines.get(i)).setLinecolor(colors[i]);

            }
            if(tablePanel.getValueAt(ENABLE_REFLLINE_COLUMN_NUMBER) != null)
            {
               Object [] tempEnableLineObjects = tablePanel.getValueAt(ENABLE_REFLLINE_COLUMN_NUMBER);
               boolean [] booleans = new boolean[tempEnableLineObjects.length];
               booleans[i] = ((Boolean)tempEnableLineObjects[i]).booleanValue();
               ((ReferenceLine)referenceLines.get(i)).setEnable(booleans[i]);

            }
         }
         else
         {
            DefaultUserInteractor.get().notify(this, "Reference Line Coordinates",
            "You can either specify x1, y1 and m  OR x1, y1, x2 and y2:"+
            " So Ref Line in row "+(i+1)+" was not created",
            UserInteractor.ERROR);
            shouldContinueClosing = false;
            referenceLines = null;
         }

      }
      if(!enableTotalCheckBox.isSelected() && referenceLines != null)
      {

         for (int i=0 ; i< referenceLines.size(); i++)
         {
            ReferenceLine line = (ReferenceLine)referenceLines.get(i);
            line.setEnable(false);
         }
      }



   }


   /**
    * Return a list of RefrenceLines if the user set any or null if there are none
    * or the user decided to disable them.
    * @return ArrayList that contains ReferenceLines from this GUI or null if
    * there are none.
    */
   protected ArrayList getReferenceLines()
   {
      return referenceLines;
   }


   /**
    * Set the ReferenceLines that will be edited by this GUI.
    *
    * @param referenceLines ArrayList that is the reference lines from the Axis
    * or null if there are none.
    */
   protected void setReferenceLines(ArrayList referenceLines)
   {
      this.referenceLines = referenceLines;
   }



   public static void main(String[] args)
   {
      ArrayList refLines = new ArrayList();

      ReferenceLine refLine = new ReferenceLine(2,1,.5);
      TextBorder textBorder = new TextBorder();
      textBorder.setTextString("Hello");

      refLine.setLabel(textBorder);
      refLine.setLinestyle("SOLID");
      refLine.setLinecolor(Color.black);
      refLines.add(refLine);

      DefaultUserInteractor.set(new GUIUserInteractor());
      try
      {
         ReferenceLineEditor referenceLineEditor = new ReferenceLineEditor(refLines);
         referenceLineEditor.setVisible(true);
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }//main()

   ///////////////////////////////
} // class ReferenceLineEditor
