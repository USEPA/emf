package gov.epa.mims.analysisengine.gui;

import gov.epa.mims.analysisengine.tree.*;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.BevelBorder;

/**
 * A GUI Editor to edit the values of a DisplaySizeType
 * @author Daniel Gatti
 * @version 1.0
 */
public class DisplaySizeEditor extends OptionDialog
{
   /** The DisplaySizeType that this GUI will edit. */
   protected DisplaySizeType displaySize = null;

   /** enable diplay size editor*/
   private BooleanValuePanel enableDisplay = null;

//   /**figure diplay size in percentage of screen: x1 value */
//   private DoubleValuePanel figureX1DoublePanel= null;
//
//   /**figure diplay size in percentage of screen: y1 value */
//   private DoubleValuePanel figureY1DoublePanel= null;
//
//   /**figure diplay size in percentage of screen: x2 value */
//   private DoubleValuePanel figureX2DoublePanel= null;
//
//   /**figure diplay size in percentage of screen: y2 value */
//   private DoubleValuePanel figureY2DoublePanel= null;
//
//   /**figure diplay size in Inches: width value */
//   private DoubleValuePanel figureWidthDoublePanel= null;
//
//   /**figure diplay size in Inches: height value */
//   private DoubleValuePanel figureHeightDoublePanel= null;
//
//   /**radion button to select whether figure display size is relative*/
//   private JRadioButton  figureRelativeSize = null;
//
//   /**radion button to select whether figure display size is absolute*/
//   private JRadioButton  figureAbsoluteSize = null;


   /**plot diplay size in percentage of screen: x1 value */
   private DoubleValuePanel plotX1DoublePanel= null;

   /**plot diplay size in percentage of screen: y1 value */
   private DoubleValuePanel plotY1DoublePanel= null;

   /**plot diplay size in percentage of screen: x2 value */
   private DoubleValuePanel plotX2DoublePanel= null;

   /**plot diplay size in percentage of screen: y2 value */
   private DoubleValuePanel plotY2DoublePanel= null;

   /**plot diplay size in Inches: width value */
   private DoubleValuePanel plotWidthDoublePanel= null;

   /**plot diplay size in Inches: height value */
   private DoubleValuePanel plotHeightDoublePanel= null;

   /**radion button to select whether plot display size is relative*/
   private JRadioButton  plotRelativeSize = null;

   /**radion button to select whether plot display size is absolute*/
   private JRadioButton  plotAbsoluteSize = null;


   /**margin diplay size in percentage of screen: x1 value */
   private DoubleValuePanel marginX1DoublePanel= null;

   /**margin diplay size in percentage of screen: y1 value */
   private DoubleValuePanel marginY1DoublePanel= null;

   /**margin diplay size in percentage of screen: x2 value */
   private DoubleValuePanel marginX2DoublePanel= null;

   /**margin diplay size in percentage of screen: y2 value */
   private DoubleValuePanel marginY2DoublePanel= null;

   /**radion button to select whether margin display size is relative*/
   private JRadioButton  marginRelativeSize = null;

   /**radion button to select whether margin display size is absolute*/
   private JRadioButton  marginAbsoluteSize = null;

   /** a double value panel set the width of the page */
   private DoubleValuePanel widthPanel;

   /** a double value panel set the height of the page */
   private DoubleValuePanel heightPanel;

   /** image icon for the displaySizeHelp icons */
   private static ImageIcon helpImageIcon = MultipleEditableTablePanel.createImageIcon(
         "/gov/epa/mims/analysisengine/gui/icons/displaySizeHelp.jpeg");

   /** to view the image */
   private ImageViewer imageViewer;

   private JLabel marginLabel = new JLabel("Page and Margin Size");

   /**
    * Constructor
    * @param displaySize DisplaySizeType to edit in this GUI. (may be null)
    */
   public DisplaySizeEditor(DisplaySizeType displaySize)
   {
      initialize();
      setDataSource(displaySize, "");
      setLocation(ScreenUtils.getPointToCenter(this));
   }

   public DisplaySizeEditor()
   {
      this(null);
   }


   /**
    * Build the GUI.
    */
   protected void initialize()
   {
      enableDisplay = new BooleanValuePanel("Enable Margin Settings",true);
      //widthPanel = new DoubleValuePanel("Width",false,0.0,10.0);
      widthPanel = new DoubleValuePanel("Width",false,0.0,Double.POSITIVE_INFINITY);
      widthPanel.addSelectAllFocusListener();
      //heightPanel = new DoubleValuePanel("Height",false,0.0,10.0);
      heightPanel = new DoubleValuePanel("Height",false,0.0,Double.POSITIVE_INFINITY);
      heightPanel.addSelectAllFocusListener();
      JPanel pagePanel = new JPanel();
      pagePanel.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createBevelBorder(BevelBorder.LOWERED),"Page Size"));
      pagePanel.add(widthPanel);
      pagePanel.add(heightPanel);

      JPanel plotRadioButtonPanel = new JPanel();
      plotRelativeSize = new JRadioButton("% Of Figure",true);
      plotAbsoluteSize = new JRadioButton("Inches",false);
      ButtonGroup plotChoiceGroup = new ButtonGroup();
      plotChoiceGroup.add(plotRelativeSize);
      plotChoiceGroup.add(plotAbsoluteSize);
      plotRadioButtonPanel.add(plotRelativeSize);
      plotRadioButtonPanel.add(plotAbsoluteSize);

      JPanel plotRelativePanel = new JPanel(new GridLayout(2,2,5,5));
      plotX1DoublePanel = new DoubleValuePanel("Left",false,0, 100);
      plotX1DoublePanel.addSelectAllFocusListener();
      plotX1DoublePanel.setToolTipText("Enter a value between 0 and 100");
      plotX1DoublePanel.setEnabled(true);
      plotRelativePanel.add(plotX1DoublePanel);
      plotY1DoublePanel = new DoubleValuePanel("Bottom",false,0, 100);
      plotY1DoublePanel.addSelectAllFocusListener();
      plotY1DoublePanel.setToolTipText("Enter a value between 0 and 100");
      plotY1DoublePanel.setEnabled(true);
      plotRelativePanel.add(plotY1DoublePanel);
      plotX2DoublePanel = new DoubleValuePanel("Right",false,0, 100);
      plotX2DoublePanel.addSelectAllFocusListener();
      plotX2DoublePanel.setToolTipText("Enter avalue between 0 and 100");
      plotRelativePanel.add(plotX2DoublePanel);
      plotX2DoublePanel.setEnabled(true);
      plotY2DoublePanel = new DoubleValuePanel("Top",false,0, 100);
      plotY2DoublePanel.addSelectAllFocusListener();
      plotY2DoublePanel.setToolTipText("Enter a value between 0 and 100");
      plotY2DoublePanel.setEnabled(true);
      plotRelativePanel.add(plotY2DoublePanel);

      JPanel plotAbsolutePanel = new JPanel();
      plotWidthDoublePanel = new DoubleValuePanel("Width",false,0,
      Double.POSITIVE_INFINITY);
      plotWidthDoublePanel.setToolTipText("Enter a value > 0");
      plotWidthDoublePanel.addSelectAllFocusListener();
      plotWidthDoublePanel.setEnabled(false);
      plotHeightDoublePanel = new DoubleValuePanel("Height",false,0,
      Double.POSITIVE_INFINITY);
      plotHeightDoublePanel.addSelectAllFocusListener();
      plotHeightDoublePanel.setToolTipText("Enter a value > 0");
      plotHeightDoublePanel.setEnabled(false);
      plotAbsolutePanel.add(plotWidthDoublePanel);
      plotAbsolutePanel.add(plotHeightDoublePanel);
      plotRelativeSize.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent ae)
         {
            enablePlotPanelSelections("relative");
         }
      });
      plotAbsoluteSize.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent ae)
         {
            enablePlotPanelSelections("absolute");
         }
      });

      final JPanel plotPanel = new JPanel(new BorderLayout());
      plotPanel.setBorder(BorderFactory.createTitledBorder(
      BorderFactory.createBevelBorder(BevelBorder.LOWERED),"Plot Size"));
      plotPanel.add(plotRadioButtonPanel, BorderLayout.NORTH);
      plotPanel.add(plotRelativePanel, BorderLayout.WEST);
      plotPanel.add(plotAbsolutePanel, BorderLayout.EAST);

      //figure
//      final JPanel figurePanel = new JPanel(new BorderLayout());
//      figurePanel.setBorder(BorderFactory.createTitledBorder(
//      BorderFactory.createBevelBorder(BevelBorder.LOWERED),"Figure"));


//      JPanel figureRadioButtonPanel = new JPanel();
//      figureRelativeSize = new JRadioButton("% Of Page",true);
//      figureAbsoluteSize = new JRadioButton("Inches",false);
//      ButtonGroup figureChoiceGroup = new ButtonGroup();
//      figureChoiceGroup.add(figureRelativeSize);
//      figureChoiceGroup.add(figureAbsoluteSize);
//      figureRadioButtonPanel.add(figureRelativeSize);
//      figureRadioButtonPanel.add(figureAbsoluteSize);
//      figurePanel.add(figureRadioButtonPanel, BorderLayout.NORTH);


//      final JPanel figureRelativePanel = new JPanel(new GridLayout(2,2,5,5));
//      figureX1DoublePanel = new DoubleValuePanel("Left",false,0, 1);
//      figureX1DoublePanel.setToolTipText("Enter a value between 0 and 1");
//      figureRelativePanel.add(figureX1DoublePanel);
//      figureY1DoublePanel = new DoubleValuePanel("Bottom",false,0, 1);
//      figureY1DoublePanel.setToolTipText("Enter a value between 0 and 1");
//      figureRelativePanel.add(figureY1DoublePanel);
//      figureX2DoublePanel = new DoubleValuePanel("Right",false,0, 1);
//      figureX2DoublePanel.setToolTipText("Enter a value between 0 and 1");
//      figureRelativePanel.add(figureX2DoublePanel);
//      figureY2DoublePanel = new DoubleValuePanel("Top",false,0, 1);
//      figureY2DoublePanel.setToolTipText("Enter a value between 0 and 1");
//      figureRelativePanel.add(figureY2DoublePanel);
//      figurePanel.add(figureRelativePanel, BorderLayout.WEST);

//      final JPanel figureAbsolutePanel = new JPanel();
//      figureWidthDoublePanel = new DoubleValuePanel("Width",false,0,
//      Double.POSITIVE_INFINITY);
//      figureWidthDoublePanel.setEnabled(false);
//      figureWidthDoublePanel.setToolTipText("Enter a value > 0");
//      figureHeightDoublePanel = new DoubleValuePanel("Height",false,0,
//      Double.POSITIVE_INFINITY);
//      figureHeightDoublePanel.setEnabled(false);
//      figureHeightDoublePanel.setToolTipText("Enter a value > 0");
//      figureAbsolutePanel.add(figureWidthDoublePanel);
//      figureAbsolutePanel.add(figureHeightDoublePanel);
//      figurePanel.add(figureAbsolutePanel, BorderLayout.EAST);
//
//      figureRelativeSize.addActionListener(new ActionListener()
//      {
//         public void actionPerformed(ActionEvent ae)
//         {
//            enableFigurePanelSelections("relative");
//         }
//      });
//
//      figureAbsoluteSize.addActionListener(new ActionListener()
//      {
//         public void actionPerformed(ActionEvent ae)
//         {
//            enableFigurePanelSelections("absolute");
//
//
//         }
//      });
//


      //margins
      JPanel labelPanel = new JPanel();
      labelPanel.add(marginLabel);

      JPanel marginRadioButtonPanel = new JPanel();
      marginRelativeSize = new JRadioButton("% Of Page",true);
      marginAbsoluteSize = new JRadioButton("Inches",false);
      ButtonGroup marginChoiceGroup = new ButtonGroup();
      marginChoiceGroup.add(marginRelativeSize);
      marginChoiceGroup.add(marginAbsoluteSize);
      marginRadioButtonPanel.add(marginRelativeSize);
      marginRadioButtonPanel.add(marginAbsoluteSize);

      JPanel marginX1X2Panel = new JPanel();
      marginX2DoublePanel = new DoubleValuePanel("Left",false,0, Double.POSITIVE_INFINITY);
      marginX2DoublePanel.addSelectAllFocusListener();
      marginX1X2Panel.add(marginX2DoublePanel);
      marginX1DoublePanel = new DoubleValuePanel("Bottom",false,0, Double.POSITIVE_INFINITY);
      marginX1DoublePanel.addSelectAllFocusListener();
      marginX1X2Panel.add(marginX1DoublePanel);


      JPanel marginY1Y2Panel = new JPanel();
      marginY2DoublePanel = new DoubleValuePanel("Right",false,0, Double.POSITIVE_INFINITY);
      marginY2DoublePanel.addSelectAllFocusListener();
      marginY1Y2Panel.add(marginY2DoublePanel);
      marginY1DoublePanel = new DoubleValuePanel("Top",false,0, Double.POSITIVE_INFINITY);
      marginY1DoublePanel.addSelectAllFocusListener();
      marginY1Y2Panel.add(marginY1DoublePanel);

      marginRelativeSize.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent ae)
         {
            setMarginToolTipsRelative();
         }
      });

      marginAbsoluteSize.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent ae)
         {
            setMarginToolTipsAbsolute();
         }
      });

      final JPanel marginPanel = new JPanel();
      marginPanel.setToolTipText("Figure Size = Page Size - Margin Size");
      marginPanel.setLayout(new BoxLayout(marginPanel,BoxLayout.Y_AXIS));
      marginPanel.setBorder(BorderFactory.createTitledBorder(
      BorderFactory.createBevelBorder(BevelBorder.LOWERED),"Figure Size"));
      marginPanel.add(labelPanel);
      marginPanel.add(marginRadioButtonPanel);
      marginPanel.add(marginX1X2Panel);
      marginPanel.add(marginY1Y2Panel);


      ActionListener helpListener = getHelpListener();
      JPanel buttonPanel = getButtonPanel(true,helpListener);

      this.setTitle("Set Page & Plot Size Properties");
      Container contentPane = this.getContentPane();
      contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
      contentPane.add(enableDisplay);
      contentPane.add(pagePanel);
      contentPane.add(marginPanel);
      contentPane.add(plotPanel);
      //contentPane.add(figurePanel);
      //contentPane.add(lbl);
      contentPane.add(buttonPanel);
      setModal(true);


      enableDisplay.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            boolean enable = enableDisplay.getValue();
            helpEnableDisplayEditor(enable);
         }
      }
      );


   } // initialize()

   private void setMarginToolTipsRelative()
   {
      marginX1DoublePanel.setBounds(0,100);
      marginX1DoublePanel.setToolTipText("Enter a value between 0 and 100");
      marginY1DoublePanel.setBounds(0,100);
      marginY1DoublePanel.setToolTipText("Enter a value between 0 and 100");
      marginX2DoublePanel.setBounds(0,100);
      marginX2DoublePanel.setToolTipText("Enter a value between 0 and 100");
      marginY2DoublePanel.setBounds(0,100);
      marginY2DoublePanel.setToolTipText("Enter a value between 0 and 100");
   }//setMarginToolTipsRelative

   private void setMarginToolTipsAbsolute()
   {
      marginX1DoublePanel.setBounds(0,Double.POSITIVE_INFINITY);
      marginX1DoublePanel.setToolTipText("Enter a value > 0");
      marginY1DoublePanel.setBounds(0,Double.POSITIVE_INFINITY);
      marginY1DoublePanel.setToolTipText("Enter a value > 0");
      marginX2DoublePanel.setBounds(0,Double.POSITIVE_INFINITY);
      marginX2DoublePanel.setToolTipText("Enter a value > 0");
      marginY2DoublePanel.setBounds(0,Double.POSITIVE_INFINITY);
      marginY2DoublePanel.setToolTipText("Enter a value > 0");
   }//setMarginToolTipsAbsolute

   private void helpEnableDisplayEditor(boolean enable)
   {
//            figureRelativeSize.setEnabled(enable);
//            figureAbsoluteSize.setEnabled(enable);

//            if(figureRelativeSize.isSelected())
//            {
//               figureX1DoublePanel.setEnabled(enable);
//               figureY1DoublePanel.setEnabled(enable);
//               figureX2DoublePanel.setEnabled(enable);
//               figureY2DoublePanel.setEnabled(enable);
//
//            }
//            if(figureAbsoluteSize.isSelected())
//            {
//               figureHeightDoublePanel.setEnabled(enable);
//               figureWidthDoublePanel.setEnabled(enable);
//            }
            widthPanel.setEnabled(enable);
            heightPanel.setEnabled(enable);
            plotRelativeSize.setEnabled(enable);
            plotAbsoluteSize.setEnabled(enable);
            boolean plotRelative = plotRelativeSize.isSelected();
            boolean plotRelEnable = enable && plotRelative;
            boolean plotAbsEnable = enable && !plotRelative;
            plotX1DoublePanel.setEnabled(plotRelEnable);
            plotY1DoublePanel.setEnabled(plotRelEnable);
            plotX2DoublePanel.setEnabled(plotRelEnable);
            plotY2DoublePanel.setEnabled(plotRelEnable);
            plotHeightDoublePanel.setEnabled(plotAbsEnable);
            plotWidthDoublePanel.setEnabled(plotAbsEnable);
            marginLabel.setEnabled(enable);
            marginX1DoublePanel.setEnabled(enable);
            marginY1DoublePanel.setEnabled(enable);
            marginX2DoublePanel.setEnabled(enable);
            marginY2DoublePanel.setEnabled(enable);
            marginRelativeSize.setEnabled(enable);
            marginAbsoluteSize.setEnabled(enable);
   }//helpEnableDisplayEditor(boolean enable)

   /** a helper method to create help listener
    */
   private ActionListener getHelpListener()
   {
      ActionListener helpListener = new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            imageViewer = new ImageViewer(DisplaySizeEditor.this,
               "Example of Page & Plot Size Settings",helpImageIcon);
            imageViewer.setVisible(true);
         }
      };
      return helpListener;
   }//getHelpListener()

//   /**
//    * enable or disable the apprppriate text field  for figure depend on the
//    * radion button selection
//    * @param enable boolean
//    */
//   private void enableFigurePanelSelections(String unit)
//   {
//      boolean  absolute = false;
//      if( unit == "absolute")
//      {
//         absolute = true;
//      }
//      else if ( unit == "relative")
//      {
//         absolute = false;
//
//      }
//      else
//      {
//         DefaultUserInteractor.get().notify("Programming Error",
//         "In Display size editor.enableFigurePanelSelections()",
//         UserInteractor.ERROR);
//      }
//      figureWidthDoublePanel.setEnabled(absolute);
//      figureHeightDoublePanel.setEnabled(absolute);
//      figureX1DoublePanel.setEnabled(!absolute);
//      figureY1DoublePanel.setEnabled(!absolute);
//      figureX2DoublePanel.setEnabled(!absolute);
//      figureY2DoublePanel.setEnabled(!absolute);
//   }

   /**
    * enable or disable the apprppriate text field  for figure depend on the
    * radion button selection
    * @param enable boolean
    */
   private void enablePlotPanelSelections(String unit)
   {
      boolean  absolute = false;
      if( unit == "absolute")
      {
         absolute = true;
      }
      else if ( unit == "relative")
      {
         absolute = false;

      }
      else
      {
         DefaultUserInteractor.get().notify(this,"Programming Error",
         "In Display size editor.enableFigurePanelSelections()",
         UserInteractor.ERROR);
      }
      plotWidthDoublePanel.setEnabled(absolute);
      plotHeightDoublePanel.setEnabled(absolute);
      plotX1DoublePanel.setEnabled(!absolute);
      plotY1DoublePanel.setEnabled(!absolute);
      plotX2DoublePanel.setEnabled(!absolute);
      plotY2DoublePanel.setEnabled(!absolute);
   }


   /**
    * Save the gui components value to the data source
    * @throws java.lang.Exception
    */
   protected void saveGUIValuesToModel() throws java.lang.Exception
   {
      /**@todo Implement this gov.epa.mims.analysisengine.gui.OptionDialog abstract method*/
      displaySize.setEnable(enableDisplay.getValue());
      double width = widthPanel.getValue();
      double height = heightPanel.getValue();
      double [] display = {width, height};
      displaySize.setDisplay(display);
      //RP: Saving the user settings even if display size is disabled.
      //Issue will be if the user has some values which are not satisfy the
      // constraints then it will popup dialog until constraints are satisfied
//      if(enableDisplay.getValue())
//      {
         //saving figure properties
//         if(figureRelativeSize.isSelected())
//         {
//            if( validValue(figureX1DoublePanel) &&
//            validValue(figureY1DoublePanel) && validValue(figureX2DoublePanel)
//            && validValue(figureY2DoublePanel))
//            {
//               displaySize.setFigure(figureX1DoublePanel.getValue(),
//               figureX2DoublePanel.getValue(),
//               figureY1DoublePanel.getValue(),
//               figureY2DoublePanel.getValue());
//            }
//
//         }
//         else if(figureAbsoluteSize.isSelected())
//         {
//            if( validValue(figureWidthDoublePanel) &&
//            validValue(figureHeightDoublePanel))
//            {
//
//               displaySize.setFigure(figureWidthDoublePanel.getValue(),
//               figureHeightDoublePanel.getValue());
//            }
//         }

         //saving plot properties
         if(plotRelativeSize.isSelected())
         {

            if( validValue(plotX1DoublePanel) &&
            validValue(plotY1DoublePanel) && validValue(plotX2DoublePanel)
            && validValue(plotY2DoublePanel))
            {
               double x1 = convertPercentToFraction(plotX1DoublePanel.getValue());
               double x2 = convertPercentToFraction(plotX2DoublePanel.getValue());
               double y1 = convertPercentToFraction(plotY1DoublePanel.getValue());
               double y2 = convertPercentToFraction(plotY2DoublePanel.getValue());
               displaySize.setPlot(x1,x2,y1, y2, DisplaySizeType.FOF);
            }

            //need any other units
         }
         else if(plotAbsoluteSize.isSelected())
         {

            if( validValue(plotWidthDoublePanel) &&
            validValue(plotHeightDoublePanel))
            {
               double [] plotAbsolute =
               {plotWidthDoublePanel.getValue(),
                plotHeightDoublePanel.getValue()};
                displaySize.setPlot(plotAbsolute,DisplaySizeType.INCHES);
            }
         }

         //saving margin properties
         int marginUnits = -1;
         if(marginRelativeSize.isSelected())
         {
            marginUnits = DisplaySizeType.NDC;
         }
         else if(marginAbsoluteSize.isSelected())
         {
            marginUnits = DisplaySizeType.INCHES;
         }
//System.out.println("marginUnits="+marginUnits);
         if( validValue(marginX1DoublePanel) &&
         validValue(marginY1DoublePanel) && validValue(marginX2DoublePanel)
         && validValue(marginY2DoublePanel))
         {
            double x1;
            double x2;
            double y1;
            double y2;
            if(marginUnits == DisplaySizeType.NDC)
            {
               x1 = convertPercentToFraction(marginX1DoublePanel.getValue());
               x2 = convertPercentToFraction(marginX2DoublePanel.getValue());
               y1 = convertPercentToFraction(marginY1DoublePanel.getValue());
               y2 = convertPercentToFraction(marginY2DoublePanel.getValue());
            }
            else
            {
               x1 = marginX1DoublePanel.getValue();
               x2 = marginX2DoublePanel.getValue();
               y1 = marginY1DoublePanel.getValue();
               y2 = marginY2DoublePanel.getValue();
            }
            displaySize.setMarginOuter(x1,x2, y1, y2, marginUnits);
         }
//      }
   }


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
   }

   /**
    * Initialize the gui components value from data source
    */
   protected void initGUIFromModel()
   {
      enableDisplay.setValue(displaySize.getEnable());
      double [] display = displaySize.getDisplay();
      double width = display[0];
      double height = display[1];
      widthPanel.setValue(width);
      heightPanel.setValue(height);

//      double [] figure = displaySize.getFigure();
//      if(figure != null)
//      {
//
//         if(figure.length == 2)
//         {
//            figureWidthDoublePanel.setValue(figure[0]);
//            figureHeightDoublePanel.setValue(figure[1]);
//            figureX1DoublePanel.clearValue();
//            figureX2DoublePanel.clearValue();
//            figureY1DoublePanel.clearValue();
//            figureY2DoublePanel.clearValue();
//            figureAbsoluteSize.setSelected(true);
//            enableFigurePanelSelections("absolute");
//         }
//         else if(figure.length ==4)
//         {
//            figureWidthDoublePanel.clearValue();
//            figureHeightDoublePanel.clearValue();
//            figureX1DoublePanel.setValue(figure[0]);
//            figureX2DoublePanel.setValue(figure[1]);
//            figureY1DoublePanel.setValue(figure[2]);
//            figureY2DoublePanel.setValue(figure[3]);
//            figureRelativeSize.setSelected(true);
//            enableFigurePanelSelections("relative");
//
//         }
//         else
//         {
//            DefaultUserInteractor.get().notify("Unexpected Figure Length",
//                  "The length of the figure[] array was expected to be either\n"+
//                  "2 or 4. It was " + figure.length + " and this length cannot " +
//                  "be handled.",  UserInteractor.NOTE);
//
//         }
//      }//if(figure != null)

      //initialize plot properties
      double [] plot = displaySize.getPlot();

      if(plot != null)
      {
         if(plot.length == 2)
         {
            plotX1DoublePanel.clearValue();
            plotX2DoublePanel.clearValue();
            plotY1DoublePanel.clearValue();
            plotY2DoublePanel.clearValue();

            plotWidthDoublePanel.setValue(plot[0]);
            plotHeightDoublePanel.setValue(plot[1]);
            plotAbsoluteSize.setSelected(true);
            enablePlotPanelSelections("absolute");
         }
         else if(plot.length == 4)
         {
            plotWidthDoublePanel.clearValue();
            plotHeightDoublePanel.clearValue();

            plotX1DoublePanel.setValue(convertFractionToPercent(plot[0]));
            plotX2DoublePanel.setValue(convertFractionToPercent(plot[1]));
            plotY1DoublePanel.setValue(convertFractionToPercent(plot[2]));
            plotY2DoublePanel.setValue(convertFractionToPercent(plot[3]));
            plotRelativeSize.setSelected(true);
            enablePlotPanelSelections("relative");
         }
         else
         {
            DefaultUserInteractor.get().notify(this,"Unexpected Plot Length",
                  "The length of the plot[] array was expected to be either2 or 4.\n" +
                  "Instead it was" + plot.length + " and this length is not handled.",
                 UserInteractor.NOTE);

         }
      }//if(plot != null)

      //initializing margin properties

      double [] margin = displaySize.getMarginOuter();
      int marginUnit = displaySize.getMarginOuterUnits();

      if(marginUnit == -1)
      {
         /* Nothing, it is unset. */
      }
      else if(marginUnit == DisplaySizeType.NDC)
      {
         marginRelativeSize.setSelected(true);
         setMarginToolTipsRelative();
         if(margin != null)
         {
            marginX1DoublePanel.setValue(convertFractionToPercent(margin[0]));
            marginX2DoublePanel.setValue(convertFractionToPercent(margin[1]));
            marginY1DoublePanel.setValue(convertFractionToPercent(margin[2]));
            marginY2DoublePanel.setValue(convertFractionToPercent(margin[3]));
         }// if(margin != null)
      }
      else if(marginUnit == DisplaySizeType.INCHES)
      {
         marginAbsoluteSize.setSelected(true);
         setMarginToolTipsAbsolute();
      }
      else
      {
         DefaultUserInteractor.get().notify(this,"Unhandled Margin Unit",
               "The margin unit was expected to be in either inches (" +
               DisplaySizeType.INCHES + ") or\n" +
               "normalized device coordinates (" + DisplaySizeType.NDC +
               "). Instead it was " + marginUnit + " and these units are not handled.",
               UserInteractor.ERROR);
      }
      disableWidgetsFromPlotType();
      // Enable or disable the panel depending on whether the enable
      // checkbox is checked or not.
      boolean enable = enableDisplay.getValue();
      helpEnableDisplayEditor(enable);

   }


   /**
    *set the data source
    */
   public void setDataSource(Object dataSource, String optionName)
   {
      this.displaySize = (DisplaySizeType)dataSource;
      super.setDataSource(dataSource, optionName);
      if (displaySize != null)
      {
         initGUIFromModel();
      }
      pack();
      this.repaint();
   }

   /*
    *checks whether value is valid
    * @param panel a DoubelValuePanel
    * @return boolean false if value is zero
    */
   private  boolean validValue(DoubleValuePanel panel)
   {
      if(Double.isNaN(panel.getValue()))
      {
         DefaultUserInteractor.get().notify(this,"Enter a value",

         "Enter a value for " +  panel.getLabelText(),
         UserInteractor.ERROR);
         shouldContinueClosing = false;
         return false;
      }
      else
         return true;
   }

   private double convertPercentToFraction(double percent)
   {
      return percent/100.0 ;
   }

   private double convertFractionToPercent(double fraction)
   {
      return fraction * 100.0 ;
   }


   public static void main(String[] args)
   {

      try
      {
         DisplaySizeType displayType = new DisplaySizeType();
         DisplaySizeEditor editor = new DisplaySizeEditor(displayType);
         editor.setVisible(true);
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
      System.exit(0);

   }//main()


} // class DisplaySizeEditor