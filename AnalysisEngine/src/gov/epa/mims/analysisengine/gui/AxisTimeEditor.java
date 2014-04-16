package gov.epa.mims.analysisengine.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import gov.epa.mims.analysisengine.tree.*;

/**
 * An Editor to display the AxisTime values.
 * @author Daniel Gatti
 * @version $Id: AxisTimeEditor.java,v 1.3 2005/09/21 14:22:48 parthee Exp $
 */
public class AxisTimeEditor extends AxisContinuousEditor
{
   /** GUI component for the final Date to allow grid to be drawn. */
   protected DateValuePanel finalPointPnl = null;

   /** GUI component for the desired location of first tick mark. */
   private DateValuePanel firstTickMarkPnl = null;

   /** GUI component for the initial point to start grid. */
   private DateValuePanel initialPointPnl = null;

   /** number of intervals in grid. */
   private IntegerValuePanel intervalCountPnl = null;

   /** GUI editor for increment for grid spacing. We will take this in hours
    * and convert to millieconds for R. */
   private DoubleValuePanel gridIncrementPnl = null;

   /** A set of Radio buttons to choose between interval count and increment. */
   protected JRadioButton rdoIncrement = null;
   protected JRadioButton rdoInterval = null;
   protected JRadioButton rdoDefault = null;

   /** The text for the Initial Point text field. We need this to add
    * (required) or (optional) to the labels when the radio buttons are changed. */
   protected String initialPointStr = "Initial Point";

   /** The text for the Final Point text field. We need this to add
    * (required) or (optional) to the labels when the radio buttons are changed. */
   protected String finalPointStr = "Final Point";

   /** The text for the Interval text field. We need this to add
    * (required) or (optional) to the labels when the radio buttons are changed. */
   protected String intervalStr = "Divisions";

   /** The text for the Increment Point text field. We need this to add
    * (required) or (optional) to the labels when the radio buttons are changed. */
   protected String incrementStr = "Increment";

   /** Tick label date format for displaying dates. */
   private StringChooserPanel tickLabelDateFormatPnl = null;
   
    /** Tick label time format for displaying dates. */
   private StringChooserPanel tickLabelTimeFormatPnl = null;

   /** GUI editor for format used to display the constant time label. */
   private TextValuePanel constantTimeLabelFormatPnl = null;

   /** GUI editor for the time zone. */
   private StringChooserPanel timeZonePnl = null;

   /** GUI editor for axis minimum.  */
   private DateValuePanel axisMinPnl = null;

   /** GUI editor for axis maximum.  */
   private DateValuePanel axisMaxPnl = null;

   /** GUI editor for the separation between tick marks in milliseconds. */
   private LongValuePanel tickIncrementPnl = null;

   /** a combo box to choose the unit for the increment */
   private JComboBox tickIncrementUnitComboBox = null;
   
   /* possible units option for tick increment */
   private String [] units = {"Minutes","Hours","Days"};

   /** to save the unit selecton */
   private String tickIncrementUnit = units[0];
   
   /** The list of DateValuePanels that need their date formats set when the
    * user change the date format. */
   protected DateValuePanel[] datePanels = null;

   /** a vector date format strings */
   static private Vector dateFormats = new Vector();
   
   /** a vector time format strings */
   static private Vector timeFormats = new Vector();

   /** The date format that the user has selected. */
   protected String chosenFormat = null;

   /** The default time/date format. */
   public static final String DEAFULT_DATE_FORMAT = "MM/dd/yyyy";
   
   /** empty date format */
   public static final String EMPTY_DATE_FORMAT = "";
   
   /** to store the previous date Format */
   private String prevDateFormat = DEAFULT_DATE_FORMAT;
   
   /** to store the previous default Time Format */
   private String prevTimeFormat = EMPTY_TIME_FORMAT;
   
   /** empty time format */
   public static final String EMPTY_TIME_FORMAT = "";

   /** The conversion from hours to milliseconds. */
   public static final long HOURS_TO_MILLISECONDS = 3600000;
   
   private static SimpleDateFormat simpleDateFormat =
      new SimpleDateFormat(DEAFULT_DATE_FORMAT);
   
   private static String [] commonTimeZones;

   static
   {
      dateFormats.add(DEAFULT_DATE_FORMAT);
      dateFormats.add("yyyy/MM/dd");
      dateFormats.add("yyyyMMdd");
      dateFormats.add("MMddyyyy");
      dateFormats.add("yyyyDDD");
      dateFormats.add("MMM yyyy");
      dateFormats.add(EMPTY_DATE_FORMAT);
      
      timeFormats.add(EMPTY_TIME_FORMAT);
      timeFormats.add("HH:mm:ss");
      timeFormats.add("HH:mm:ss zzz");
      timeFormats.add("HH:mm");
      timeFormats.add("HH");
      timeFormats.add("hh a");
      
      ArrayList tempList = new ArrayList();
      tempList.add("EST");
      tempList.add("PST");
      tempList.add("Ect/GMT");
      tempList.add("Ect/GMT+1");
      tempList.add("Ect/GMT+2");
      tempList.add("Ect/GMT+3");
      tempList.add("Ect/GMT+4");
      tempList.add("Ect/GMT+5");
      tempList.add("Ect/GMT+6");
      tempList.add("Ect/GMT+7");
      tempList.add("Ect/GMT+8");
      tempList.add("Ect/GMT+9");
      tempList.add("Ect/GMT+10");
      tempList.add("Ect/GMT+11");
      tempList.add("Ect/GMT+12");
      tempList.add("Ect/GMT-1");
      tempList.add("Ect/GMT-2");
      tempList.add("Ect/GMT-3");
      tempList.add("Ect/GMT-4");
      tempList.add("Ect/GMT-5");
      tempList.add("Ect/GMT-6");
      tempList.add("Ect/GMT-7");
      tempList.add("Ect/GMT-8");
      tempList.add("Ect/GMT-9");
      tempList.add("Ect/GMT-10");
      tempList.add("Ect/GMT-11");
      tempList.add("Ect/GMT-12");
      
      commonTimeZones = new String[tempList.size()];
      commonTimeZones = (String [])tempList.toArray(commonTimeZones);
   }//static


   /**
    * Consructor.
    */
   public AxisTimeEditor(AxisTime axis)
   {
      super(axis);
      initialize();
      setDataSource(axis, "Time Axis Editor");
      setLocation(ScreenUtils.getPointToCenter(this));
   }


   /**
   * Constructor need for class.newInstance
   */
   public AxisTimeEditor()
   {
      this(null);
   }


   /**
    * Contruct the GUI elements.
    *
    * @author Daniel Gatti
    */
   protected void initialize()
   {
      super.initialize();

      // Time Format Panel
      String[] formatStrs = new String[dateFormats.size()];
      dateFormats.copyInto(formatStrs);
      tickLabelDateFormatPnl = new StringChooserPanel("Date Format", false, formatStrs);
      tickLabelDateFormatPnl.setToolTipText("The date format for all times in this window and on the plot.");
      String[] formatTimeStrs = new String[timeFormats.size()];
      timeFormats.copyInto(formatTimeStrs);
      tickLabelTimeFormatPnl = new StringChooserPanel("Time Format", false, formatTimeStrs);
      tickLabelTimeFormatPnl.setToolTipText("The time format for all times in this window and on the plot.");
      timeZonePnl = new StringChooserPanel("Time Zone", false, commonTimeZones);//TimeZone.getAvailableIDs());
      timeZonePnl.setToolTipText("The time zone to use for all times in the plot.");
      constantTimeLabelFormatPnl = new TextValuePanel("Constant Time Label Format", false, null);
      
      JPanel timePanel1 = new JPanel();
      timePanel1.setLayout(new BoxLayout(timePanel1, BoxLayout.X_AXIS));
      timePanel1.add(tickLabelDateFormatPnl);
      timePanel1.add(tickLabelTimeFormatPnl);
      
      JPanel timePanel2 = new JPanel();
      timePanel2.add(timeZonePnl);
      
      JPanel timePanel3 = new JPanel();
      timePanel3.add(constantTimeLabelFormatPnl);
      

      JPanel timePanel = new JPanel();
      timePanel.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createLoweredBevelBorder(), "Date & Time Format ",
         TitledBorder.LEFT, TitledBorder.TOP));
      timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.Y_AXIS));
      timePanel.add(timePanel1);
      timePanel.add(timePanel2);
      timePanel.add(timePanel3);
      

      // Axis Panel
      axisMinPnl = new DateValuePanel("Min", false);
      axisMinPnl.setToolTipText("The lowest time to plot on the axis.");
      axisMaxPnl = new DateValuePanel("Max", false);
      axisMaxPnl.setToolTipText("The highest time to plot on the axis.");

      JPanel axisMinMaxPanel = new JPanel();
      axisMinMaxPanel.setLayout(new BoxLayout(axisMinMaxPanel, BoxLayout.X_AXIS));
      axisMinMaxPanel.add(axisMinPnl);
      axisMinMaxPanel.add(axisMaxPnl);

      JPanel axisTextPanel = new JPanel();
      axisTextPanel.setLayout(new BoxLayout(axisTextPanel, BoxLayout.X_AXIS));
      axisTextPanel.add(axisColorPnl);
      axisTextPanel.add(axisTextPnl);

      JPanel axisPosPnl = new JPanel();
      axisPosPnl.setLayout(new BoxLayout(axisPosPnl, BoxLayout.X_AXIS));
      axisPosPnl.add(algorithmPnl);
      axisPosPnl.add(positionPnl);

      JPanel axisSubPanel = new JPanel();
      axisSubPanel.setLayout(new BoxLayout(axisSubPanel, BoxLayout.Y_AXIS));
      axisSubPanel.setBorder(BorderFactory.createEtchedBorder());

      axisSubPanel.add(axisMinMaxPanel);
      axisSubPanel.add(axisTextPanel);
      axisSubPanel.add(axisPosPnl);

      JPanel axisPanel = new JPanel();
      axisPanel.setLayout(new BoxLayout(axisPanel, BoxLayout.Y_AXIS));
      axisPanel.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createLoweredBevelBorder(), "Axis",
         TitledBorder.LEFT, TitledBorder.TOP));
      axisPanel.add(enableAxisPnl);
      axisPanel.add(axisSubPanel);

      algorithmPnl.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            String s = posAlignConv.getSystemOption(algorithmPnl.getValue());
            int i = Integer.parseInt(s);
            positionPnl.setEnabled(i != Axis.DEFAULT_POSITIONING);
         }
      }
      );

      enableAxisPnl.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent ae)
         {

            enableWholeAxisPanel();

         }
      }
      );
      
      // Tick Marks
      JPanel tickPanel = getTickMarkPanel();
      drawTickLabelsPnl.setEnabled(true);
      firstTickMarkPnl = new DateValuePanel("First", false);
      firstTickMarkPnl.setPreferredSize(new Dimension(150, 30));
      firstTickMarkPnl.setToolTipText("The starting time at which to draw tick marks.");
      tickIncrementPnl = new LongValuePanel("Increment", false);
      tickIncrementPnl.setToolTipText("The increment between tick marks in hours.");
      tickIncrementUnitComboBox = new JComboBox(units);
      tickIncrementUnitComboBox.setSelectedItem(tickIncrementUnit);
      tickIncrementUnitComboBox.setToolTipText("Select a unit for the increment");
      tickIncrementUnitComboBox.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            long oldIncrementValue = tickIncrementPnl.getValue();
            String newUnit = (String)tickIncrementUnitComboBox.getSelectedItem();
            String oldUnit = tickIncrementUnit;
            long newIncrementValue = convertUnits(newUnit, oldUnit, oldIncrementValue);
            tickIncrementPnl.setValue(newIncrementValue);
            tickIncrementUnit = newUnit;
         }
      }
      );
      
      JPanel timeTickSubPanel = new JPanel();
      timeTickSubPanel.setLayout(new BoxLayout(timeTickSubPanel, BoxLayout.X_AXIS));

      timeTickSubPanel.add(firstTickMarkPnl);
      timeTickSubPanel.add(tickIncrementPnl);
      timeTickSubPanel.add(tickIncrementUnitComboBox);
      timeTickSubPanel.add(Box.createHorizontalGlue());
      
      tickSubPanel.add(timeTickSubPanel); // add to the parent class panel
//      tickSubPanel.add(timeZonePnl);

      // Grid panel
      finalPointPnl = new DateValuePanel(finalPointStr, true);
      finalPointPnl.setToolTipText("The final time at which to draw the grid.");
      initialPointPnl = new DateValuePanel(initialPointStr, true);
      initialPointPnl.setToolTipText("The starting time at which to draw the grid.");
      intervalCountPnl = new IntegerValuePanel(intervalStr, true, 1, Integer.MAX_VALUE);
      intervalCountPnl.setToolTipText("The number of divisions between the initial and final grid points.");
      // We need to set the minimum value to something greater than zero.
      gridIncrementPnl = new DoubleValuePanel(incrementStr, "hours", true, 0.01, Long.MAX_VALUE / HOURS_TO_MILLISECONDS);
      gridIncrementPnl.setToolTipText("The time increment between each grid line in hours.");
      String radioButtonToopTipText = "Set these to determine how the grid lines are drawn.";
      rdoIncrement    = new JRadioButton("Increment");
      rdoIncrement.setToolTipText(radioButtonToopTipText);
      rdoInterval     = new JRadioButton("Divisions");
      rdoInterval.setToolTipText(radioButtonToopTipText);
      rdoDefault      = new JRadioButton("Default");
      rdoDefault.setToolTipText(radioButtonToopTipText);
      ButtonGroup buttonGroup = new ButtonGroup();
      buttonGroup.add(rdoIncrement);
      buttonGroup.add(rdoInterval);
      buttonGroup.add(rdoDefault);

      JPanel gridColorStyleWidthPnl = new JPanel();
      //gridColorStyleWidthPnl.setBorder(BorderFactory.createEtchedBorder());
      gridColorStyleWidthPnl.setLayout(new BoxLayout(gridColorStyleWidthPnl, BoxLayout.X_AXIS));
      gridColorStyleWidthPnl.add(gridColorPnl);
      gridColorStyleWidthPnl.add(gridLineStylePnl);
      gridColorStyleWidthPnl.add(gridLineWidthPnl);

      JPanel gridTickMarksPnl = new JPanel();
      //gridTickMarksPnl.setBorder(BorderFactory.createEtchedBorder());
      gridTickMarksPnl.setLayout(new BoxLayout(gridTickMarksPnl, BoxLayout.X_AXIS));

      gridTickMarksPnl.add(gridTickmarkEnablePnl);
      gridTickMarksPnl.add(gridTickmarkLengthPnl);

      JPanel gridSpacingChoicePnl = new JPanel();
      gridSpacingChoicePnl.setLayout(new BoxLayout(gridSpacingChoicePnl, BoxLayout.Y_AXIS));
      gridSpacingChoicePnl.setBorder(BorderFactory.createEtchedBorder());
      gridSpacingChoicePnl.add(rdoDefault);
      gridSpacingChoicePnl.add(rdoIncrement);
      gridSpacingChoicePnl.add(rdoInterval);

      JPanel gridSpacingNumbersPnl = new JPanel(new GridLayout(2, 2));
      gridSpacingNumbersPnl.add(initialPointPnl);
      gridSpacingNumbersPnl.add(finalPointPnl);
      gridSpacingNumbersPnl.add(gridIncrementPnl);
      gridSpacingNumbersPnl.add(intervalCountPnl);

      JPanel gridSpacingPanel = new JPanel();
      gridSpacingPanel.setLayout(new BoxLayout(gridSpacingPanel, BoxLayout.X_AXIS));
      gridSpacingPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Grid Bounds",
            TitledBorder.LEFT, TitledBorder.TOP));
      gridSpacingPanel.add(gridSpacingChoicePnl);
      gridSpacingPanel.add(gridSpacingNumbersPnl);

      JPanel gridSubPanel = new JPanel();
      gridSubPanel.setLayout(new BoxLayout(gridSubPanel, BoxLayout.Y_AXIS));
      gridSubPanel.setBorder(BorderFactory.createEtchedBorder());
      gridSubPanel.add(gridColorStyleWidthPnl);
      gridSubPanel.add(Box.createVerticalGlue());
      gridSubPanel.add(gridTickMarksPnl);
      gridSubPanel.add(gridSpacingPanel);

      JPanel gridPanel = new JPanel();
      gridPanel.setLayout(new BoxLayout(gridPanel, BoxLayout.Y_AXIS));
      gridPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLoweredBevelBorder(), "Grid",
            TitledBorder.LEFT, TitledBorder.TOP));

      gridPanel.add(Box.createVerticalStrut(25));
      gridPanel.add(gridEnablePnl);
      gridPanel.add(Box.createVerticalStrut(25));
      gridPanel.add(gridSubPanel);
      gridPanel.add(Box.createVerticalStrut(150));
      gridEnablePnl.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            boolean enabled = gridEnablePnl.getValue();
            gridLineStylePnl.setEnabled(enabled);
            gridLineWidthPnl.setEnabled(enabled);
            gridColorPnl.setEnabled(enabled);
            rdoDefault.setEnabled(enabled);
            rdoIncrement.setEnabled(enabled);
            rdoInterval.setEnabled(enabled);
            if (enabled)
               setGridBoundsEnabling();
            else
            {
               initialPointPnl.setEnabled(enabled);
               finalPointPnl.setEnabled(enabled);
               gridIncrementPnl.setEnabled(enabled);
               intervalCountPnl.setEnabled(enabled);
            }
            gridTickmarkEnablePnl.setEnabled(enabled);
            gridTickmarkLengthPnl.setEnabled(gridTickmarkEnablePnl.getValue() && enabled);
            customTickMarkBtn.setEnabled(enabled);
         }
      }
      );

      gridTickmarkEnablePnl.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            boolean enabled = gridTickmarkEnablePnl.getValue();
            gridTickmarkLengthPnl.setEnabled(enabled);
         }
      }
      );

      // When the radio buttons for increment, interval or default grid
      // spacing change, enable and disable the appropriate text fields and
      // change the labels to show which fields are required.
      rdoIncrement.addActionListener(
         new ActionListener()
         {
            public void actionPerformed(ActionEvent ae)
            {
              setGridBoundsEnabling();
            }
         }
      );

      rdoInterval.addActionListener(
         new ActionListener()
         {
            public void actionPerformed(ActionEvent ae)
            {
               setGridBoundsEnabling();
           }
        }
      );

      rdoDefault.addActionListener(
         new ActionListener()
         {
            public void actionPerformed(ActionEvent ae)
            {
               setGridBoundsEnabling();
            }
         }
      );

      
      JPanel basicsPanel = new JPanel();
      basicsPanel.setLayout(new BoxLayout(basicsPanel,BoxLayout.Y_AXIS));
      basicsPanel.add(timePanel);
      basicsPanel.add(axisPanel);
      basicsPanel.add(tickPanel);
      JTabbedPane tabbedPane = new JTabbedPane();
      tabbedPane.add("Basic Settings", basicsPanel);
      tabbedPane.add("Grid Setttings", gridPanel);

      Container contentPane = getContentPane();
      contentPane.setLayout(new BorderLayout());
      contentPane.add(tabbedPane);
      contentPane.add(getButtonPanel(),BorderLayout.SOUTH);
      
      setModal(true);

      setUpDateFormattingForPanels();
   } // initialize()
   
   private long convertUnits(String newUnit, String oldUnit, long oldIncrementValue)
   {
      long hours_to_minutes = 60;
      long day_to_hours =24;
      long day_to_minutes = 1440;
      //units[0] = minutes , units[1] = "hours" units[2]= "days"
      if(oldUnit.equalsIgnoreCase(units[0])) 
      {
         if(newUnit.equalsIgnoreCase(units[0]))
         {
            return oldIncrementValue;
         }
         else if(newUnit.equalsIgnoreCase(units[1]))
         {
            return oldIncrementValue/hours_to_minutes; // possible loss of precision
         }
         else if(newUnit.equalsIgnoreCase(units[2]))
         {
            return oldIncrementValue/day_to_minutes; // possible loss of precision
         }
      }//if(oldUnit.equalsIgnoreCase("Minutes"))
      else if(oldUnit.equalsIgnoreCase(units[1]))
      {
         if(newUnit.equalsIgnoreCase(units[0]))
         {
            return oldIncrementValue * hours_to_minutes ;
         }
         else if(newUnit.equalsIgnoreCase(units[1]))
         {
            return oldIncrementValue;
         }
         else if(newUnit.equalsIgnoreCase(units[2]))
         {
            return oldIncrementValue/day_to_hours; // possible loss of precision
         }
      }//else if(oldUnit.equalsIgnoreCase("Hours"))
      else if(oldUnit.equalsIgnoreCase(units[2]))
      {
         if(newUnit.equalsIgnoreCase(units[0]))
         {
            return oldIncrementValue * day_to_minutes ;
         }
         else if(newUnit.equalsIgnoreCase(units[1]))
         {
            return oldIncrementValue * day_to_hours;
         }
         else if(newUnit.equalsIgnoreCase(units[2]))
         {
            return oldIncrementValue; 
         }
      }//else if(oldUnit.equalsIgnoreCase("Days"))
      else
      {
         DefaultUserInteractor.get().notify(this,"Error", oldUnit + " option is not valid", UserInteractor.ERROR);
      }
      return Long.MIN_VALUE;
   }//convertUnits

   private void enableWholeAxisPanel()
   {
      boolean enable = enableAxisPnl.getValue();
            axisColorPnl.setEnabled(enable);
            axisTextPnl.setEnabled(enable);
            axisMinPnl.setEnabled(enable);
            axisMaxPnl.setEnabled(enable);
            drawTickMarksPnl.setEnabled(enable);
            drawTickLabelsPnl.setEnabled(enable); 
            tickMarkLabelColorPnl.setEnabled(enable);
            tickMarkFontStylePnl.setEnabled(enable);
            tickMarkPerpPnl.setEnabled(enable);
            labelExpPnl.setEnabled(enable);
            positionPnl.setEnabled(enable);
            algorithmPnl.setEnabled(enable);
            gridLineStylePnl.setEnabled(enable);
            gridLineWidthPnl.setEnabled(enable);
            gridColorPnl.setEnabled(enable);
            rdoDefault.setEnabled(enable);
            rdoIncrement.setEnabled(enable);
            rdoInterval.setEnabled(enable);
            if (enable)
               setGridBoundsEnabling();
            else
            {
               initialPointPnl.setEnabled(enable);
               finalPointPnl.setEnabled(enable);
               gridIncrementPnl.setEnabled(enable);
               intervalCountPnl.setEnabled(enable);
            }
            gridTickmarkEnablePnl.setEnabled(enable);
            gridTickmarkLengthPnl.setEnabled(enable);
            tickIncrementPnl.setEnabled(enable);
            firstTickMarkPnl.setEnabled(enable);

   }

   /**
    * Populate the GUI to reflect the data that is in the underlying axis that this
    *  class represents.
    *
    * @author Daniel Gatti
    * @see gov.epa.mims.analysisengine.gui.OptionDialog#initGUIFromModel()
    */
   protected void initGUIFromModel()
   {
      super.initGUIFromModel();
      if (!(axis instanceof AxisTime))
      {
         DefaultUserInteractor.get().notify(this,"Incorrect Object Type",
               "Expected an axis of type AxisTime but found one of type " +
               axis.getClass().toString() + ".", UserInteractor.ERROR);
         return;
      }

      AxisTime axisTime = (AxisTime)axis;

      // Time formats
      String dateTimeFormat = axisTime.getTickLabelFormat();
      String dateFormat = "";
      String timeFormat = "";
      if (dateTimeFormat == null)
      {
         dateFormat = DEAFULT_DATE_FORMAT;
         timeFormat = EMPTY_TIME_FORMAT;
      }
      //In this logic assumption is that both dateFormat and timeFormat cannot be
      //"" at the same time and when we add both format a space " " is added in between. 
      else
      {
         int index = -1;
         for(int i=0; i<timeFormats.size(); i++)
         {
            String aTimeFormat = (String)timeFormats.get(i);
            index = dateTimeFormat.indexOf(aTimeFormat);
            //aTimeFormat can be "" and this will make index = 0
            if(index > 0) 
            {
               break;
            }//if(index > 0)
         }//for(i)
         
         if(index ==0)
         {
            timeFormat = EMPTY_TIME_FORMAT;
            dateFormat = dateTimeFormat.trim();
         }//if(index != -1)
         else
         {
            timeFormat = dateTimeFormat.substring(index,dateTimeFormat.length());
            dateFormat = dateTimeFormat.substring(0,index-1).trim();
         }
      }//else
      
      tickLabelDateFormatPnl.setValue(dateFormat);
      tickLabelTimeFormatPnl.setValue(timeFormat);
      constantTimeLabelFormatPnl.setValue(axisTime.getConstantTimeLabelFormat());

      // Grid
      initialPointPnl.setValue(axisTime.getInitialPoint());
      finalPointPnl.setValue(axisTime.getFinalPoint());
      Long gridIncrement = axisTime.getGridIncrement();
      if (gridIncrement != null)
      {
         double d = (double)gridIncrement.longValue() / (double)HOURS_TO_MILLISECONDS;
         gridIncrementPnl.setValue(d);
      }
      else
      {
         gridIncrementPnl.clearValue();
      }
      Integer interval = axisTime.getIntervalCount();
      if (interval != null)
      {
         intervalCountPnl.setValue(interval.intValue());
      }
      else
      {
         intervalCountPnl.clearValue();
      }

      // If increment is NaN, then we use either default or interval count.
      if (axisTime.getGridIncrement() == null)
      {
         // If interval count = 0, then we use default.
         if (interval == null || interval.intValue() == 0)
            rdoDefault.setSelected(true);
         else
            rdoInterval.setSelected(true);
      }
      else
         rdoIncrement.setSelected(true);

      enableWholeAxisPanel();
      boolean enabled = gridEnablePnl.getValue();
      gridLineStylePnl.setEnabled(enabled);
      gridLineWidthPnl.setEnabled(enabled);
      gridColorPnl.setEnabled(enabled);
      initialPointPnl.setEnabled(enabled);
      finalPointPnl.setEnabled(enabled);
      gridIncrementPnl.setEnabled(enabled);
      intervalCountPnl.setEnabled(enabled);
      rdoDefault.setEnabled(enabled);
      rdoIncrement.setEnabled(enabled);
      rdoInterval.setEnabled(enabled);
      gridTickmarkEnablePnl.setEnabled(enabled);

      // Enable the axis position box only if the default algorithm is NOT selected.
      String s = posAlignConv.getSystemOption(algorithmPnl.getValue());
      int i = Integer.parseInt(s);
      positionPnl.setEnabled(i != Axis.DEFAULT_POSITIONING);

      gridTickmarkEnablePnl.setValue(axisTime.getGridTickmarkEnable());
      if (gridTickmarkEnablePnl.getValue())
      {
         gridTickmarkLengthPnl.setValue(axisTime.getGridTickmarkLength());
      }
      gridTickmarkLengthPnl.setEnabled(gridTickmarkEnablePnl.getValue() && enabled);
      setGridBoundsEnabling();

      Object[] obj = axisTime.getAxisRange();

      if (obj != null && obj instanceof Double[])
      {
         Date[] minMax = (Date[])axisTime.getAxisRange();
         if (minMax.length == 2)
         {
            axisMinPnl.setValue(minMax[0]);
            axisMaxPnl.setValue(minMax[1]);
         }
         else
         {
            DefaultUserInteractor.get().notify(this,"Unexpected array length",
                  "Expected an Double[] of length 2 from AxisNumeric.getAxisRange() " +
                  "but found one of length " + minMax.length + " instead.",
                  UserInteractor.ERROR);
         }
      } // if (obj != null && obj instanceof Double[])

      //tickLabelFormatPnl.setValue(axisTime.getTickLabelFormat());
      //constantTimeLabelFormatPnl.setValue(axisTime.getConstantTimeLabelFormat());
      TimeZone tz = axisTime.getTimeZone();
      if (tz != null)
      {
         timeZonePnl.setValue(tz.getID());
      }
      else
      {
         timeZonePnl.setValue(null);
      }

      firstTickMarkPnl.setValue(axisTime.getFirstTickMark());
      if(tickIncrementUnit == null)
      {
         tickIncrementUnitComboBox.setSelectedItem(units[0]);
         tickIncrementPnl.setValue(((axisTime.getTickIncrement()/ HOURS_TO_MILLISECONDS)*60));
      }
      else 
      {
         tickIncrementUnitComboBox.setSelectedItem(tickIncrementUnit);
         if(tickIncrementUnit.equals(units[0]))
         {
            tickIncrementPnl.setValue(((axisTime.getTickIncrement()/ HOURS_TO_MILLISECONDS)*60));
         }
         else if(tickIncrementUnit.equals(units[1]))
         {
            tickIncrementPnl.setValue((axisTime.getTickIncrement()/ HOURS_TO_MILLISECONDS));
         }
         else //(tickIncrementUnit.equals(units[2])) //days
         {
            tickIncrementPnl.setValue((axisTime.getTickIncrement()/ (24*HOURS_TO_MILLISECONDS)));
         }
      }
      
      boolean enable = axisTime.getDrawTickMarks();
      tickIncrementPnl.setEnabled(enable);
      firstTickMarkPnl.setEnabled(enable);
      disableWidgetsFromPlotType();
   } // initGUIFromModel()


   /**
    * Populate the model with the values stored in the GUI.
    *
    * @author Daniel Gatti
    * @see gov.epa.mims.analysisengine.gui.OptionDialog#saveGUIValuesToModel()
    */
   protected void saveGUIValuesToModel() throws Exception
   {
      super.saveGUIValuesToModel();
      if (!(axis instanceof AxisTime))
      {
         DefaultUserInteractor.get().notify(this,"Incorrect Object Type",
               "Expected an axis of type AxisTime but found one of type " +
               axis.getClass().toString() + ".", UserInteractor.ERROR);
      }

      AxisTime axisTime = (AxisTime)axis;

 

      // Grid
      axisTime.setGridColor(gridColorPnl.getValue());
      ImageIcon img = gridLineStylePnl.getValue();
      String sysStr = lineStyleConv.getSystemOption(img);
      if (sysStr != null)
      {
         axisTime.setGridlineStyle(sysStr);
      }
      axisTime.setGridEnable(gridEnablePnl.getValue());
      axisTime.setGridTickmarkEnable(gridTickmarkEnablePnl.getValue());
      axisTime.setGridTickmarkLength(gridTickmarkLengthPnl.getValue());
      axisTime.setGridlineWidth(gridLineWidthPnl.getValue());

      Date axisMin = axisMinPnl.getValue();
      Date axisMax = axisMaxPnl.getValue();

      if ((axisMin == null) && (axisMax == null))
      {

      }
      axisTime.setAxisRange(axisMin, axisMax);

      Date initialPoint = initialPointPnl.getValue();
      Date finalPoint = finalPointPnl.getValue();

      // If the increment radio button is selected, then we expect a
      // required initial point and an optional final point.
      if (rdoIncrement.isSelected())
      {
         double incrementDbl = gridIncrementPnl.getValue();
         long increment = (long)(incrementDbl * (double)HOURS_TO_MILLISECONDS);
         // The initial point is required.
         if (initialPoint == null)
         {
            DefaultUserInteractor.get().notify(this, "Initial Point Required",
                  "When using the increment grid option, an initial point is required.",
                  UserInteractor.ERROR);
            shouldContinueClosing = false;
            return;
         }
         // The increment is required.
         else if (Double.isNaN(incrementDbl))
         {
            DefaultUserInteractor.get().notify(this, "Increment Required",
                  "When using the increment grid option, a non-zero increment value is required.",
                  UserInteractor.ERROR);
            shouldContinueClosing = false;
            return;
         }
         // The final point is optional.
         else if (finalPoint == null)
         {
            axisTime.setGrid(initialPoint, new Long(increment));
         }
         else
         {
            axisTime.setGrid(initialPoint, new Long(increment), finalPoint);
         }
      }
      // Interval count.
      else if (rdoInterval.isSelected())
      {
         int intervalCount = intervalCountPnl.getValue();
         // The initial point is required.
         if (initialPoint == null)
         {
            DefaultUserInteractor.get().notify(this, "Initial Point Required",
                  "When using the interval grid option, an initial point is required.",
                  UserInteractor.ERROR);
            shouldContinueClosing = false;
            return;
         }
         // The final point is required.
         else if (finalPoint == null)
         {
            DefaultUserInteractor.get().notify(this,"Final Point Required",
                  "When using the interval grid option, a final point is required.",
                  UserInteractor.ERROR);
            shouldContinueClosing = false;
            return;
         }
         // The interval count is required.
         else if (intervalCount == Integer.MIN_VALUE)
         {
            DefaultUserInteractor.get().notify(this, "Final Point Required",
                  "When using the interval grid option, an interval is required.",
                  UserInteractor.ERROR);
            shouldContinueClosing = false;
            return;
         }
         else
         {
            axisTime.setGrid(initialPoint, new Integer(intervalCount), finalPoint);
         }
      }
      // Default - reset everything in the AxisNumeric.
      else
      {
         axisTime.setGrid();
      }

      //axisTime.setTickLabelFormat(tickLabelFormatPnl.getValue());
      //axisTime.setConstantTimeLabelFormat(constantTimeLabelFormatPnl.getValue());
      TimeZone tz = TimeZone.getTimeZone(timeZonePnl.getValue());
      axisTime.setTimeZone(tz);
      
      Date date = firstTickMarkPnl.getValue();
      if(date == null)
      {
         DefaultUserInteractor.get().notify(this, "Enter a Value",
                  "Expected a date for the first tick mark",
                  UserInteractor.ERROR); 
         shouldContinueClosing = false;
         return;
      }//if(date == null)
      else
      {
         if(axisMin != null && date.compareTo(axisMin)<0)
         {
            DefaultUserInteractor.get().notify(this, "Error Input",
                  "First tick mark value " + date + " should come after the axis "+ 
                  " minimun value "+axisMin,
                  UserInteractor.ERROR); 
            shouldContinueClosing = false;
            return;
         }//if(axisMin != null && date.compareTo(axisMin)<0)
         if(axisMax != null && date.compareTo(axisMax)>0)
         {
            DefaultUserInteractor.get().notify(this, "Error Input",
                  "First tick mark value " + date + " should come before the axis "+ 
                  " maximun value "+axisMax,
                  UserInteractor.ERROR); 
            shouldContinueClosing = false;
            return;
         }//if(axisMax != null && date.compareTo(axisMax)>0)
         axisTime.setFirstTickMark(date);
      }//else
      
      long incrementValue = tickIncrementPnl.getValue();
      if(incrementValue == Long.MIN_VALUE)
      {
         DefaultUserInteractor.get().notify(this, "Enter a Value",
                  "Expected a tick mark improvement",
                  UserInteractor.ERROR); 
         shouldContinueClosing = false;
      }//if(incrementValue == Long.MIN_VALUE)
      else
      {
         String newUnit = (String)tickIncrementUnitComboBox.getSelectedItem();
         if(newUnit.equals(units[0]))
         {
            incrementValue /=60;
         }
         else if(newUnit.equals(units[2]))
         {
            incrementValue *=24;
         }
         axisTime.setTickIncrement(incrementValue * HOURS_TO_MILLISECONDS);
      }//else
      
           // Time formats
      String dateFormat = tickLabelDateFormatPnl.getValue();
      String timeFormat = tickLabelTimeFormatPnl.getValue();
      String dateTimeFormat = dateFormat + " " + timeFormat;
      axisTime.setTickLabelFormat(dateTimeFormat);
      // We are going to set the time format for the Text to be the same
      // as everything else.
      Text text = constantTimeLabelFormatPnl.getValue();
      if (text != null)
      {
         text.setTimeFormat(dateTimeFormat);
      }
      axisTime.setConstantTimeLabelFormat(text);
   } // saveGUIValuesToModel()


   /**
    * Enable or disable the fields that the user needs for the
    * grid bounds. This affects initial point, final point,
    * increment and interval.
    */
    protected void setGridBoundsEnabling()
    {
       if (rdoDefault.isSelected())
       {
          initialPointPnl.setEnabled(false);
          initialPointPnl.setLabel(initialPointStr);
          finalPointPnl.setEnabled(false);
          finalPointPnl.setLabel(finalPointStr);
          gridIncrementPnl.setEnabled(false);
          gridIncrementPnl.setLabel(incrementStr);
          intervalCountPnl.setEnabled(false);
          intervalCountPnl.setLabel(intervalStr);
       }
       else if (rdoIncrement.isSelected())
       {
          initialPointPnl.setEnabled(true);
          initialPointPnl.setLabel(initialPointStr + " (required)");
          finalPointPnl.setEnabled(true);
          finalPointPnl.setLabel(finalPointStr + " (optional)");
          gridIncrementPnl.setEnabled(true);
          gridIncrementPnl.setLabel(incrementStr + " (required)");
          intervalCountPnl.setEnabled(false);
          intervalCountPnl.setLabel(intervalStr);
       }
       else if (rdoInterval.isSelected())
       {
          initialPointPnl.setEnabled(true);
          initialPointPnl.setLabel(initialPointStr + " (required)");
          finalPointPnl.setEnabled(true);
          finalPointPnl.setLabel(finalPointStr + " (required)");
          gridIncrementPnl.setEnabled(false);
          gridIncrementPnl.setLabel(incrementStr);
          intervalCountPnl.setEnabled(true);
          intervalCountPnl.setLabel(intervalStr + " (required)");
       }
    } // setGridBoundsEnabling()


    /**
     * Set the type of plot that this dialog is reprenting.
     * This should be one of the plot constant from AnalysisEngineConstants.
     * @param plotTypeName String that is the name of the plot being produced.
     */
     public void setPlotTypeName(String newName)
     {
        super.setPlotTypeName(newName);
        // Don't do anything if we don't have a plot type.
        if (plotTypeName == null)
           return;
        disableWidgetsFromPlotType();
     }

     private void disableWidgetsFromPlotType()
     {
        if (plotTypeName == null) return;

        // Time Series
        if (plotTypeName.equals("TimeSeries"))
        {
           axisMinPnl.setVisible(false);
           axisMaxPnl.setVisible(false);
           customTickMarkBtn.setVisible(false);
        }
     }


    /**
     * Set up a listeneer on the time format combo box that sets a
     * new date formatter for each of the DateValuePanels when it changes.
     */
     protected void setUpDateFormattingForPanels()
     {
        datePanels = new DateValuePanel[5];
        datePanels[0] = finalPointPnl;
        datePanels[1] = firstTickMarkPnl;
        datePanels[2] = initialPointPnl;
        datePanels[3] = axisMinPnl;
        datePanels[4] = axisMaxPnl;
        ActionListener listener = new ActionListener()
        {
           public void actionPerformed(ActionEvent e)
           {
              String dateFormat = tickLabelDateFormatPnl.getValue();
              String timeFormat = tickLabelTimeFormatPnl.getValue();
              if(dateFormat.equals(EMPTY_DATE_FORMAT)
               && timeFormat.equals(EMPTY_TIME_FORMAT))
              {
                 DefaultUserInteractor.get().notify(AxisTimeEditor.this,"Note", "Both time and date " +
                 "formats cannot be empty",UserInteractor.NOTE);
                 dateFormat = prevDateFormat;
                 timeFormat = prevTimeFormat;
                 tickLabelDateFormatPnl.setValue(dateFormat);
                 tickLabelTimeFormatPnl.setValue(timeFormat);
              }//if()
              else
              {
                 prevDateFormat = dateFormat;
                 prevTimeFormat = timeFormat;
              }
              
              String dateTimeFormat = dateFormat + " " +  timeFormat;
              //If either of the formats are empty  a space ' ' is added at end 
              // or beginning of the format. So call trim();
              simpleDateFormat.applyPattern(dateTimeFormat.trim());
              for (int i = datePanels.length - 1; i >= 0; --i)
                 datePanels[i].setDateFormatter(simpleDateFormat);
           }
        };
        tickLabelDateFormatPnl.addActionListener(listener);     
        tickLabelTimeFormatPnl.addActionListener(listener);     
      }
     
    /**
    * main() for testing the GUI.
    */
   public static void main(String[] args)
   {
      AxisTime axis = new AxisTime();
      AxisTimeEditor ane = new AxisTimeEditor(axis);
      ane.setVisible(true);
      System.exit(0);
   }
} // class AxisTimeEditor
