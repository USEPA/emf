
package gov.epa.mims.analysisengine.gui;

import gov.epa.mims.analysisengine.tree.BoxType;
import gov.epa.mims.analysisengine.tree.BoxPlotConstantsIfc;
import gov.epa.mims.analysisengine.stats.Percentile;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.table.*;

/**
 * BoxTypeEditor.java
 * A Dialog to edit "properties" of a boxtype, which is one of the analysis
 * options for a boxplot.
 *This editor does not support three functionality that is supported by the BoxType:
 * - set Boxes Positions in category axis
 * - set Boxes widths
 * - set Boxes expansion factors
 *
 * @author Parthee Partheepan, CEP UNC
 * @version $Id: BoxTypeEditor.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 * @see BoxType.java
 * @see BoxPlot.java
 * @see AnalysisOptions.java
 */
public class BoxTypeEditor extends OptionDialog
{

   /** a data model for this gui */
   private BoxType boxType;

//   /** a check box to specify whether to transpose the axis */
//   private JCheckBox transposeAxisCB;
   /** a button for to specify the box plot to be horizontal */
   private JRadioButton horizontalRB;

   /** a button for to specify the box plot to be vertical */
   private JRadioButton verticalRB;

   /** a check box to specify whether to reverse the order of the plot */
   private JCheckBox reverseOrderCB;

   /** a check box to specify whether to have a notch for the box */
   private JCheckBox notchCB;

   /** a double value panel to specify the notch fraction */
   private DoubleValuePanel notchFractionDP;

   /** a color value panel to specify the border color for the box and whisker
    */
   private ColorValuePanel borderColorCP;

   /** a double value panel to specify the double value panel for the line width
    *for the border and whisker */
   private DoubleValuePanel linewidthDP;

//   /** a check box for specifying whether to plot the outliers
//    */
//   private JCheckBox outliersCB;
//
//   /** a double value panel to specify the range for the box */
//   private DoubleValuePanel rangeDP;

   /** whether box plot will have a fixed width or not */
   private JCheckBox fixedWidthCB;

   /** a color editable panel to specify the set of colors for the boxes */
   private ColorEditableTablePanel boxColorsEP;

   /** a check box to indicate whether custom labels are users */
   private JCheckBox customLabelCB;

   /** JTable for to specify custon settings for the box plot */
   private JTable table;

   /** constant to indicate the first column in the table panel */
   public static final int FIRST_COLUMN = 0;

   /** constant to indicate the second column in the table panel */
   public static final int SECOND_COLUMN = 1;

   /** default labels for the first colum */
   public static final String [] CUSTOM_LABELS = {BoxPlotConstantsIfc.LOWER_WHISKER,
      BoxPlotConstantsIfc.LOWER_HINGE, BoxPlotConstantsIfc.LOWER_NOTCH_EXTREME,
      BoxPlotConstantsIfc.MEDIAN, BoxPlotConstantsIfc.UPPER_NOTCH_EXTREME,
      BoxPlotConstantsIfc.UPPER_HINGE, BoxPlotConstantsIfc.UPPER_WHISKER};

   /** default valus for the second colum corresponding to the first col labels */
   public static final Double [] DEFAULT_VALUES = {new Double(0.00),new Double(0.20),
      new Double(0.25),new Double(0.50), new Double(0.70),new Double(0.75),
      new Double(1.00)};

   /** Creates a new instance of BoxTypeEditor */
   public BoxTypeEditor(BoxType aBoxType)
   {
      super();
      initialize();
      setDataSource(aBoxType, "");
   }//BoxTypeEditor

   /**
   * constructor need for class.newInstance
   */
  public BoxTypeEditor()
  {
    this(null);
  }

   public void setDataSource(Object dataSource, String optionName)
  {
     this.boxType = (BoxType)dataSource;

     super.setDataSource(dataSource, optionName);
     if (boxType != null)
     {
        initGUIFromModel();
     }
     pack();
     setLocation(ScreenUtils.getPointToCenter(this));
   }//setDataSource()

   private void initialize()
   {
      setModal(true);
      this.setTitle("Edit Box Type Properties");

      verticalRB = new JRadioButton("Vertical ");
      verticalRB.setHorizontalTextPosition(SwingConstants.LEFT);
      horizontalRB = new JRadioButton("Horizontal ");
      horizontalRB.setHorizontalTextPosition(SwingConstants.LEFT);
      ButtonGroup group = new ButtonGroup();
      group.add(verticalRB);
      group.add(horizontalRB);

//      outliersCB = new JCheckBox("Outliers ?");
//      outliersCB.setHorizontalTextPosition(SwingConstants.LEFT);
//      outliersCB.setToolTipText("Check if you want to plot the outliers.");
//      rangeDP = new DoubleValuePanel("Range",false,0.0, Double.MAX_VALUE);
//      rangeDP.setToolTipText("Range > 0.0");


      JPanel orientationPanel = new JPanel();
      orientationPanel.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createLineBorder(Color.black),"Orientation "));
      orientationPanel.setLayout(new BoxLayout(orientationPanel, BoxLayout.X_AXIS));
      orientationPanel.add(Box.createHorizontalGlue());
      orientationPanel.add(verticalRB);
      orientationPanel.add(Box.createHorizontalStrut(20));
      orientationPanel.add(horizontalRB);
      orientationPanel.add(Box.createHorizontalGlue());

      fixedWidthCB = new JCheckBox("Fixed Width? ");
      fixedWidthCB.setHorizontalTextPosition(SwingConstants.LEFT);
      fixedWidthCB.setToolTipText("Check = Fixed width for all the boxes, "+
         "Uncheck = Width is dependent on number of data point in a data set");
      reverseOrderCB = new JCheckBox("Reverse Order? ");
      reverseOrderCB.setToolTipText("Check = Reverse the order of the data in "+
      "category axis, Uncheck = Default order");
      reverseOrderCB.setHorizontalTextPosition(SwingConstants.LEFT);
      notchCB = new JCheckBox("Show Notches? ");
      notchCB.setToolTipText("Check = Show the notches in the box, Uncheck = Box " +
      "without the notches");
      notchCB.setHorizontalTextPosition(SwingConstants.LEFT);
      notchCB.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            notchFractionDP.setEnabled(notchCB.isSelected());
         }
      });
      notchFractionDP = new DoubleValuePanel("Notch Fraction",false,0.0, 1.0);
      notchFractionDP.setToolTipText("Fraction of the notch and values between 0 and 1.");
      borderColorCP = new ColorValuePanel("Outline Color",false);
      borderColorCP.setToolTipText("Color of the outline for the box and whisker");
      linewidthDP = new DoubleValuePanel("Line Width",false,0.0, Double.MAX_VALUE);
      linewidthDP.setToolTipText("Width of the outline for the box and whisker ");

//      JPanel boxWhiskerPanel1 = new JPanel();
//      boxWhiskerPanel1.setLayout(new GridLayout(2,3));
//      boxWhiskerPanel1.add(fixedWidthCB);
//      boxWhiskerPanel1.add(reverseOrderCB);
//      boxWhiskerPanel1.add(borderColorCP);
//      boxWhiskerPanel1.add(notchCB);
//      boxWhiskerPanel1.add(notchFractionDP);
//      boxWhiskerPanel1.add(linewidthDP);

      JPanel boxWhiskerPanel1 = new JPanel();
      boxWhiskerPanel1.setLayout(new BoxLayout(boxWhiskerPanel1, BoxLayout.X_AXIS));
      boxWhiskerPanel1.add(Box.createHorizontalStrut(10));
      boxWhiskerPanel1.add(fixedWidthCB);
      boxWhiskerPanel1.add(Box.createHorizontalGlue());
      boxWhiskerPanel1.add(reverseOrderCB);
      boxWhiskerPanel1.add(Box.createHorizontalGlue());
      boxWhiskerPanel1.add(borderColorCP);
      boxWhiskerPanel1.add(Box.createHorizontalGlue());

      JPanel boxWhiskerPanel2 = new JPanel();
      boxWhiskerPanel2.setLayout(new BoxLayout(boxWhiskerPanel2,BoxLayout.X_AXIS));
      boxWhiskerPanel2.add(Box.createHorizontalStrut(10));
      boxWhiskerPanel2.add(notchCB);
      boxWhiskerPanel2.add(Box.createHorizontalStrut(5));
      boxWhiskerPanel2.add(notchFractionDP);
      boxWhiskerPanel2.add(Box.createHorizontalStrut(15));
      boxWhiskerPanel2.add(linewidthDP);
      boxWhiskerPanel2.add(Box.createHorizontalGlue());

      boxColorsEP = new ColorEditableTablePanel("Box Colors");
      JPanel boxColorPanel = new JPanel(new BorderLayout());
//      boxColorPanel.setBorder(BorderFactory.createCompoundBorder(
//         BorderFactory.createTitledBorder(
//            BorderFactory.createLineBorder(Color.black),"Box Colors"),
//            BorderFactory.createLoweredBevelBorder()));
      boxColorPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.black),"Box Colors"));
      boxColorPanel.add(boxColorsEP);

      JPanel boxWhiskerPanel = new JPanel();
      boxWhiskerPanel.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createLineBorder(Color.black),"Box & Whisker"));
      boxWhiskerPanel.setLayout(new BoxLayout(boxWhiskerPanel,BoxLayout.Y_AXIS));
      boxWhiskerPanel.add(Box.createVerticalStrut(5));

      boxWhiskerPanel.add(boxWhiskerPanel1);
      boxWhiskerPanel.add(Box.createVerticalStrut(5));
      boxWhiskerPanel.add(boxWhiskerPanel2);
      boxWhiskerPanel.add(Box.createVerticalStrut(5));
//      boxWhiskerPanel.add(boxWhiskerPanel3);
//      boxWhiskerPanel.add(Box.createVerticalStrut(5));

      JPanel customPanel = createCustomPanel();
      JPanel cusAndColorPanel = new JPanel();
      cusAndColorPanel.setLayout(new BoxLayout(cusAndColorPanel,BoxLayout.X_AXIS));
      //boxWhiskerPanel.add(boxColorPanel);
      cusAndColorPanel.add(boxColorPanel);
      cusAndColorPanel.add(customPanel);

      boxWhiskerPanel.add(cusAndColorPanel);
      boxWhiskerPanel.add(Box.createVerticalStrut(5));

      JPanel mainPanel = new JPanel();
      mainPanel.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createEmptyBorder(4,4,4,4),
         BorderFactory.createCompoundBorder(
            BorderFactory.createLoweredBevelBorder(),
            BorderFactory.createEmptyBorder(4,4,4,4))));
      mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
      mainPanel.add(orientationPanel);
      mainPanel.add(boxWhiskerPanel);
      //mainPanel.add(createCustomPanel());

      Container container = getContentPane();
      container.setLayout(new BorderLayout());
      container.add(mainPanel,BorderLayout.CENTER);
      container.add(getButtonPanel(),BorderLayout.SOUTH);

   }//initialize

   /** a helper method to create custom panel */
   private JPanel createCustomPanel()
   {
        customLabelCB  = new JCheckBox("Custom Settings",false);
        customLabelCB.setHorizontalTextPosition(SwingConstants.LEFT);
        customLabelCB.addActionListener(new ActionListener()
        {
           public void actionPerformed(ActionEvent e)
           {
               boolean enable = customLabelCB.isSelected();
               table.setEnabled(enable);
           }
        });
        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.add(customLabelCB);

        //setting default values
        Object[][] data = new Object[CUSTOM_LABELS.length][2];
        for(int i=0; i<CUSTOM_LABELS.length; i++)
        {
           data[i][0] = CUSTOM_LABELS[i];
           data[i][1] = DEFAULT_VALUES[i];
        }//for(i)

        String [] columnNames = {"Plot Constants","Percentiles"};

        DefaultTableModel tableModel = new DefaultTableModel(data,columnNames)
        {
           public boolean isCellEditable(int row, int column)
           {
               return (column==SECOND_COLUMN);
           }

           public void setValueAt(Object o, int row, int column)
           {
               Double oldValue = (Double)getValueAt(row,column);
               if(column == SECOND_COLUMN)
               {
                  try
                  {
                     Double d = new Double((String)o);
                     double value = d.doubleValue();
                     if(value < 0.0 || value > 1.0)
                     {
                        DefaultUserInteractor.get().notify(BoxTypeEditor.this,"Error",
                           "Please enter a value between 0.0 and 1.0",
                           UserInteractor.ERROR);
                        return;
                     }
                     else
                     {
                        Vector rowVector = (Vector)dataVector.elementAt(row);
                        rowVector.setElementAt(d, column);
                        fireTableCellUpdated(row, column);
                     }
                  }
                  catch(Exception e)
                  {
                     DefaultUserInteractor.get().notify(BoxTypeEditor.this,"Unexpected Type",
                        "This column will only accep value of type Double but found "
                        + o.getClass(), UserInteractor.ERROR);

                     return;
                  }
               }
           }
        };

        table = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(table,
         JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
         JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
        scrollPane.setPreferredSize(new Dimension(200, 100));
        JPanel customPanel = new JPanel();
        customPanel.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createLineBorder(Color.black),"Custom Settings"));
        customPanel.setLayout(new BorderLayout());
        customPanel.add(checkBoxPanel,BorderLayout.NORTH);
        customPanel.add(scrollPane,BorderLayout.CENTER);

        return customPanel;
   }//createCustomPanel()

//   /** a helper method to set up a cell editor
//    * while will handle only double values with in range 0.0 and 1.0
//    */
//   protected void setUpTextFieldEditor()
//   {
//      // Set up the cell editor.
//       final JTextField txtField = new JTextField();
//
//       DefaultCellEditor doubleEditor = new DefaultCellEditor(txtField)
//          {
//            // Save the original value.
//            Object originalObjectValue = null;
//
//            // Override getTableCellEditorComponent() to save the original value
//            // in case the user enters an invalid value.
//            public Component getTableCellEditorComponent(JTable table,
//                  Object value, boolean isSelected, int row, int column)
//            {
//               originalObjectValue = table.getValueAt(row, column);
//
//               if (value instanceof Double)
//               {
//                  Double d = ((Double)value);
//                  txtField.setText(d.toString());
//                  txtField.selectAll();
//               }
//               else
//               {
//                  DefaultUserInteractor.get().notify("Unexpected object type",
//                     "Please enter a double value between 0 and 1",
//                     UserInteractor.ERROR);
//               }
//               return txtField;
//            }
//
//             public Object getCellEditorValue()
//             {
//               double enteredValue;
//
//                try
//                {
//                  enteredValue = Double.parseDouble(txtField.getText());
//                }
//                catch(Exception e)
//                {
//                  DefaultUserInteractor.get().notify("Error verifying input value",
//                     "Please enter a decimal number between 0.0 and 1.0.", UserInteractor.ERROR);
//                  return originalObjectValue;
//                }
//
//                if (enteredValue < 0.0 || enteredValue > 1.0)
//                {
//                   String message = "Please enter a decimal number between 0.0 and 1.0.";
//                   DefaultUserInteractor.get().notify("Error verifying input value",
//                     message, UserInteractor.ERROR);
//                   return originalObjectValue;
//                }//if (enteredValue < 0.0 || enteredValue > 1.0)
//
//                return new Double(enteredValue);
//              }//getCellEditorValue()
//          };
//
//      table.setDefaultEditor(Double.class, doubleEditor);
//      TableColumn tableColumn = table.getColumnModel().getColumn(SECOND_COLUMN);
//      doubleEditor.setClickCountToStart(2);
//      tableColumn.setCellEditor(doubleEditor);
//   } // setUpTextFieldEditor()

   protected void initGUIFromModel()
   {
      boolean isHorizontal = boxType.getHorizontal();
      horizontalRB.setSelected(isHorizontal);
      verticalRB.setSelected(!isHorizontal);

//      outliersCB.setSelected(boxType.getOutliers());
//      rangeDP.setValue(boxType.getRange());

      //NOTE in gui we refer whether it's fixed or not
      fixedWidthCB.setSelected(!boxType.getVarwidths());
      reverseOrderCB.setSelected(boxType.getReversePlotOrder());
      boolean isNotch = boxType.getNotch();
      notchCB.setSelected(isNotch);
      notchFractionDP.setEnabled(isNotch);

      notchFractionDP.setValue(boxType.getNotchFrac());
      borderColorCP.setValue(boxType.getBorderColor());
      linewidthDP.setValue(boxType.getLwd());
      boxColorsEP.setValue(boxType.getColor());
      int selOption = boxType.getProcessing();
      if(selOption == BoxPlotConstantsIfc.USE_R)
      {
        customLabelCB.setSelected(false);
        table.setEnabled(false);
      }//if
      else if(selOption == BoxPlotConstantsIfc.CUSTOM)
      {
        customLabelCB.setSelected(true);
        HashMap hashMap = boxType.getCustomPercentiles();
        Double value = null;
        for(int i=0; i< CUSTOM_LABELS.length; i++)
        {
            value = (Double)hashMap.get(CUSTOM_LABELS[i]);
//System.out.println("value.class=" + value);
            table.setValueAt(value, i, SECOND_COLUMN);
        }//for(i)
      }//else if
      else
      {
        DefaultUserInteractor.get().notify(this,"Error", "This option '" + selOption +
         "' is not valid", UserInteractor.ERROR);
      }
   }//initGUIFromModel

   protected void saveGUIValuesToModel() throws Exception
   {
      if(customLabelCB.isSelected())
      {
         //check for whether values are in the ascending order
         if(checkCustomTableValues())
         {
            boxType.setProcessing(BoxPlotConstantsIfc.CUSTOM);
            //check for double value between 0 and 1
            //have a custom editor and verify whether it's in the ascending order
            HashMap hashMap = new HashMap();
            Double value = null;
            for(int i=0; i < CUSTOM_LABELS.length ; i++)
            {
               value = (Double)table.getValueAt(i, SECOND_COLUMN);
               hashMap.put(CUSTOM_LABELS[i],value);
            }//for(i)
            boxType.setCustomPercentiles(hashMap);
         }
         else
         {
            shouldContinueClosing = false;
            return;
         }
      }//if(customLabelCB.isSelected())
      else
      {
        boxType.setProcessing(BoxPlotConstantsIfc.USE_R);
      }//else

      //NOTE in gui we refer whether it's fixed or not
      boxType.setHorizontal(horizontalRB.isSelected());
      boxType.setReversePlotOrder(reverseOrderCB.isSelected());
//      boxType.setOutliers(outliersCB.isSelected());
//      boxType.setRange(rangeDP.getValue());

      boxType.setVarwidths(!fixedWidthCB.isSelected());
      boxType.setNotch(notchCB.isSelected());
      boxType.setNotchFrac(notchFractionDP.getValue());
      boxType.setBorderColor(borderColorCP.getValue());
      boxType.setLwd(linewidthDP.getValue());
      boxType.setColor((Color[])boxColorsEP.getValue());


   }//saveGUIValuesToModel

   /** a helper method for checking whether values are in the ascending order
    */
   private boolean checkCustomTableValues()
   {
      Double d1 = null;
      Double d2 = null;
      for(int i=0; i<CUSTOM_LABELS.length-1; i++)
      {
         d1 = (Double)table.getValueAt(i, SECOND_COLUMN);
         d2 = (Double)table.getValueAt(i+1, SECOND_COLUMN);
         if(d1.doubleValue() >d2.doubleValue())
         {
            DefaultUserInteractor.get().notify(this,"Error", "In the custom settings table, the row"
               + (i+1) + " > row " + (i+2)+ ". The values in the table should be in an ascending order.",
               UserInteractor.ERROR);
            return false;
         }
      }//for(i)
      return true;
   }

   /**
    * @param args the command line arguments
    */
   public static void main(String[] args)
   {
      DefaultUserInteractor.set(new GUIUserInteractor());
      BoxTypeEditor boxTypeEditor = new BoxTypeEditor(new BoxType());
      boxTypeEditor.setVisible(true);
   }//main()



}
