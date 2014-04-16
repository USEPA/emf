package gov.epa.mims.analysisengine.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import gov.epa.mims.analysisengine.tree.*;

/**
 * A base class with the Axis variables. The getter, setters and GUI components
 * for these variables will be made in this class.
 * It is up to sub-classes to layout the GUI elements and call the get and set
 * methods.
 * @author Daniel Gatti
 * @version $Id: AxisEditor.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 */

public class AxisEditor
extends OptionDialog
implements FontConstantsIfc
{
   /** The Axis that we are editing. */
   protected Axis axis = null;

   /** The GUI element for the axisColor in Axis. */
   protected ColorValuePanel axisColorPnl = null;

   /** The GUI element for the tickMarkLabelColor in Axis. */
   protected ColorValuePanel tickMarkLabelColorPnl = null;

   /** The font style for the tick marks. */
   protected StringChooserPanel tickMarkFontStylePnl = null;

   /** The editor for the Axis test. */
   protected TextValuePanel axisTextPnl = null;

   /** The button to bring up the cutom user tick marks. */
   protected JButton customTickMarkBtn = null;

   /** The GUI for drawing the tick mark labels. */
   protected BooleanValuePanel drawTickLabelsPnl = null;

   /** The GUI for drawTickMarkLabelsPerpendicularToAxis in Axis. */
   protected BooleanValuePanel tickMarkPerpPnl = null;

   /** The GUI for drawing tick marks. */
   protected BooleanValuePanel drawTickMarksPnl = null;

   /** The GUI for enableAxis in Axis. */
   protected BooleanValuePanel enableAxisPnl = null;

   /** The GUI for the axis position. */
   protected DoubleValuePanel positionPnl = null;

   /** The GUI for the tick mark label expansion. */
   protected DoubleValuePanel labelExpPnl =null;

   /** The String converter for the position algorithms. */
   protected PrettyOptionStringConverter posAlignConv = null;

   /** The GUI for the positioningAlgorithm in Axis. */
   protected StringChooserPanel algorithmPnl = null;

   /** The editor for the custom tick marks. */
   protected CustomTickMarkEditor tickMarkEditor = null;

   /** The tick mark subpanel that we need to expose to subclasses so that they
    * can add to the tick mark panel.*/
   protected JPanel tickSubPanel = null;

   /** The array for user display of position algorithm options. */
   protected String[] positionOptions =
   {
      "Default",
      "Lines Into Margin",
      "Data Coordinates"
   };

   /** The array for system position algorihtm values. */
   protected int[] positionInts =
   {
      Axis.DEFAULT_POSITIONING,
      Axis.LINES_INTO_MARGIN,
      Axis.USER_COORDINATES
   };

   /**
    * Constructor.
    * @param axis Axis that this GUI will represent to the user.
    */
   public AxisEditor(Axis axis)
   {
      super();
   }


   /**
    * Constructor need for class.newInstance
    */
   public AxisEditor()
   {
      this(null);
   }


   /**
    * Present a GUI to the user that will allow them to edit the custom tick marks.
    * @return
    */
   protected void editCustomLabels()
   {
      tickMarkEditor = new CustomTickMarkEditor(axis);
      tickMarkEditor.setVisible(true);
   } // editCustomLabels()



   /**
    * Get a panel with the Tick Mark components laid out for general use
    * by sub-classes.
    */
   protected JPanel getTickMarkPanel()
   {
      JPanel tickPanel = new JPanel();
      tickPanel.setLayout(new BoxLayout(tickPanel, BoxLayout.Y_AXIS));
      tickPanel.setBorder(BorderFactory.createTitledBorder(
      BorderFactory.createLoweredBevelBorder(), "Tick Marks",
      TitledBorder.LEFT, TitledBorder.TOP));

      JPanel tickTopPanel = new JPanel();
      tickTopPanel.setLayout(new BoxLayout(tickTopPanel, BoxLayout.X_AXIS));
      tickTopPanel.add(drawTickMarksPnl);
      tickTopPanel.add(drawTickLabelsPnl);

      JPanel tickColorTextPanel = new JPanel();
      tickColorTextPanel.setLayout(new BoxLayout(tickColorTextPanel, BoxLayout.X_AXIS));
      tickColorTextPanel.add(tickMarkLabelColorPnl);
      tickColorTextPanel.add(tickMarkFontStylePnl);

      JPanel tickPerpExpPanel = new JPanel();
      tickPerpExpPanel.setLayout(new BoxLayout(tickPerpExpPanel, BoxLayout.X_AXIS));
      tickPerpExpPanel.add(tickMarkPerpPnl);
      tickPerpExpPanel.add(labelExpPnl);

      tickSubPanel = new JPanel();
      tickSubPanel.setLayout(new BoxLayout(tickSubPanel, BoxLayout.Y_AXIS));
      tickSubPanel.setBorder(BorderFactory.createTitledBorder(
      BorderFactory.createEtchedBorder(), "Tick Mark Labels",
      TitledBorder.LEFT, TitledBorder.TOP));
      tickSubPanel.add(tickColorTextPanel);
      tickSubPanel.add(tickPerpExpPanel);

      JPanel customTickMarkPnl = new JPanel();
      customTickMarkPnl.add(customTickMarkBtn);

      tickPanel.add(tickTopPanel);
      tickPanel.add(tickSubPanel);
      tickPanel.add(customTickMarkPnl);

      drawTickLabelsPnl.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent ae)
         {
            boolean enable = drawTickLabelsPnl.getValue();
            tickMarkLabelColorPnl.setEnabled(enable);
            tickMarkFontStylePnl.setEnabled(enable);
            tickMarkPerpPnl.setEnabled(enable);
            labelExpPnl.setEnabled(enable);
            customTickMarkBtn.setEnabled(enable);
         }
      }
      );

      customTickMarkBtn.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent ae)
         {
            editCustomLabels();
         }
      }
      );

      return tickPanel;
   }


   /**
    * Create the GUI elements.
    */
   protected void initialize()
   {
      axisColorPnl = new ColorValuePanel("Color", false);
      axisColorPnl.setToolTipText("The color for the axis.");
      tickMarkLabelColorPnl = new ColorValuePanel("Color", false);
      tickMarkLabelColorPnl.setToolTipText("The color for the tick marks on the axis.");
      tickMarkLabelColorPnl.setToolTipText("The style of font for the tick mark labels.");
      tickMarkFontStylePnl = new StringChooserPanel("Font Style",
      false, AVAILABLE_FONTS);
      tickMarkFontStylePnl.setToolTipText("The font style for the tick mark labels.");
      axisTextPnl = new TextValuePanel("Text", false, null,true);
      axisTextPnl.setToolTipText("The text to place next to this axis.");
      customTickMarkBtn = new JButton("Edit Custom Tick Marks");
      customTickMarkBtn.setToolTipText("Use these if you would like to set your own custom tick marks and labels.");
      tickMarkPerpPnl = new BooleanValuePanel("Perpendicular to Axis?");
      tickMarkPerpPnl.setToolTipText("Should the tick marks be drawn perpendicular to this axis?");
      drawTickLabelsPnl = new BooleanValuePanel("Draw Tick Mark Labels?");
      drawTickLabelsPnl.setToolTipText("Draw the tick mark labels?");
      drawTickMarksPnl = new BooleanValuePanel("Draw Tick Marks?");
      drawTickMarksPnl.setToolTipText("Draw the tick marks on the axis?");
      enableAxisPnl = new BooleanValuePanel("Draw Axis?");
      enableAxisPnl.setToolTipText("Draw the axis?");
      positionPnl = new DoubleValuePanel("Axis Position", false);
      positionPnl.setToolTipText("The position of the axis based on the positioning method.");
      labelExpPnl = new DoubleValuePanel("Size", false);
      labelExpPnl.setToolTipText("The relative size of the tick mark labels. (1 = normal, 2 = twice as large, etc.)");
      posAlignConv = new PrettyOptionStringConverter();
      for (int i = 0; i < positionOptions.length; i++)
         posAlignConv.addPrettyOption(positionOptions[i],
         Integer.toString(positionInts[i]), false);
      algorithmPnl = new StringChooserPanel("Positioning Method", false,
      positionOptions);
      algorithmPnl.setToolTipText("The method for positioning the axis.");

   }



   /**
    * Set the values in the GUI to match the data model.
    */
   protected void initGUIFromModel()
   {
      axisColorPnl.setValue(axis.getAxisColor());
      tickMarkLabelColorPnl.setValue(axis.getTickMarkLabelColor());
      tickMarkFontStylePnl.setValue(axis.getTickMarkFont());
      if(axis.getAxisLabelText() != null)
      {
         axisTextPnl.setValue(axis.getAxisLabelText());
      }
      else
         axisTextPnl.setValue(new Text());
      tickMarkPerpPnl.setValue(axis.getDrawTickMarkLabelsPerpendicularToAxis());
      drawTickMarksPnl.setValue(axis.getDrawTickMarks());
      drawTickLabelsPnl.setValue(axis.getDrawTickMarkLabels());
      enableAxisPnl.setValue(axis.getEnableAxis());
      labelExpPnl.setValue(axis.getTickMarkLabelExpansion());
      positionPnl.setValue(axis.getPosition());
      String algStr = Integer.toString(axis.getPositioningAlgorithm());
      String userStr = posAlignConv.getPrettyOption(algStr);
      algorithmPnl.setValue(userStr);
      positionPnl.setValue(axis.getPosition());

      boolean enable = drawTickLabelsPnl.getValue();
      tickMarkLabelColorPnl.setEnabled(enable);
      tickMarkFontStylePnl.setEnabled(enable);
      tickMarkPerpPnl.setEnabled(enable);
      labelExpPnl.setEnabled(enable);
      customTickMarkBtn.setEnabled(enable);
   }


   /**
    * Get the values from the GUI and save them to the model.
    * @throws java.lang.Exception
    */
   protected void saveGUIValuesToModel() throws java.lang.Exception
   {
      axis.setAxisColor(axisColorPnl.getValue());
      axis.setTickMarkLabelColor(tickMarkLabelColorPnl.getValue());
      axis.setTickMarkFont(tickMarkFontStylePnl.getValue());
      Text txt = axisTextPnl.getValue();
      if (txt != null)
         axis.setAxisLabelText(txt);
      axis.setDrawTickMarkLabelsPerpendicularToAxis(tickMarkPerpPnl.getValue());
      axis.setDrawTickMarks(drawTickMarksPnl.getValue());
      axis.setDrawTickMarkLabels(drawTickLabelsPnl.getValue());
      axis.setEnableAxis(enableAxisPnl.getValue());
      axis.setTickMarkLabelExpansion(labelExpPnl.getValue());
      String userStr = algorithmPnl.getValue();
      String algStr = posAlignConv.getSystemOption(userStr);
      axis.setPosition(Integer.parseInt(algStr), positionPnl.getValue());
   } // saveGUIValuesToModel()


   /**
    * Set the Axis variable and initial the GUI from the model.
    * @param dataSource Object that this GUI represents.
    * @param optionName String
    */
   public void setDataSource(Object dataSource, String optionName)
   {
      this.axis = (Axis) dataSource;
      // we are storing this object twice
      super.setDataSource(dataSource, optionName);
      if (axis != null)
      {
         initGUIFromModel();
      }
      pack();
      this.repaint();
   }
   
   /**
    * Override this to specify the info shown in the table
    * @return String info to show in the options table
    */
   public String getInfoString()
   {
      Text t = axis.getAxisLabelText();
      if( t != null)
      {
         return t.toString();
      }
      else
      {
         return "";
      }
   }
   
} // class AxisEditor
