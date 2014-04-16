
package gov.epa.mims.analysisengine.gui;

import gov.epa.mims.analysisengine.tree.LinearRegression;
import gov.epa.mims.analysisengine.tree.LinearRegressionStatistics;
import gov.epa.mims.analysisengine.tree.TextBorder;
import gov.epa.mims.analysisengine.tree.Text;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.*;
import javax.swing.*;

/*
 * LinearRegressionEditor.java
 * A editor for the LinearRegression.
 * @author  Parthee R Partheepan
 * @version $Id: LinearRegressionEditor.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 */
public class LinearRegressionEditor extends OptionDialog
{
   /** data model for this gui **/
   private LinearRegression linearRegression;

   private JTabbedPane tabbedPane;

   private static final int LR_TAB =0;

   private static final int LR_STATS_TAB =1;

   //component for lrPanel
   /*lr diagnostic plots */
   private BooleanValuePanel lrDiagnosticPlotsPanel;

   /*lr diagnostic plots single page */
   private BooleanValuePanel lrDiagnosticPlotsSinglePagePanel;

   /** lr line color */
   private ColorValuePanel lrLineColorPanel;

   /*lr line style */
   private ImageChooserPanel lrLineStylePanel;

   /** to convert system line style options to pretty line styles */
   private PrettyOptionImageIconConverter lineConverter;

   /** lr line width */
   private DoubleValuePanel lrLineWidthPanel;

   /* lr residual line */
   private BooleanValuePanel lrResLineEnablePanel;

   private ColorValuePanel lrResLineColorPanel;

   /*lr residual line style */
   private ImageChooserPanel lrResLineStylePanel;

   /** lr residual line width */
   private DoubleValuePanel lrResLineWidthPanel;

   private BooleanValuePanel lrLabelEnablePanel;

   private ColorValuePanel lrLabelColorPanel;

   private DoubleValuePanel lrLabelSizePanel;

   private IntegerValuePanel lrLabelSepPanel;

   private IntegerValuePanel lrLabelSigFigPanel;

   private DoubleValuePanel lrLabelXPosPanel;

   private DoubleValuePanel lrLabelYPosPanel;

   private ColorValuePanel lrLabelBGColorPanel;

   //private ColorValuePanel lrLabelBorderColorPanel;

   //private DoubleValuePanel lrLabelBorderWidthPanel;

   private LRStatsEditor lrStatsEditor;

   /** Creates a new instance of LinearRegressionEditor */
   public LinearRegressionEditor(LinearRegression linearRegression)
   {
      initialize();
      setTitle("Linear Regression Editor");
      setDataSource(linearRegression, "");
      pack();
      setLocation(ScreenUtils.getPointToCenter(this));
      this.repaint();
   }

   public LinearRegressionEditor()
   {
      this(null);
   }

   public void setDataSource(Object dataSource, String optionName)
   {
      this.linearRegression = (LinearRegression)dataSource;
      if (linearRegression != null)
      {
         initGUIFromModel();
      }
   }

   private void initialize()
   {
      this.setModal(true);
      tabbedPane = new JTabbedPane();
      JPanel lrPanel = createLinearRegressionPanel();
      lrStatsEditor = new LRStatsEditor();
      tabbedPane.insertTab("Lines ", null,lrPanel,null,LR_TAB);
      tabbedPane.insertTab("Statistics ", null,lrStatsEditor,null,LR_STATS_TAB);
      JPanel mainPanel = new JPanel(new BorderLayout());
      mainPanel.add(tabbedPane);
      JPanel buttonPanel = getButtonPanel();

      Container container = this.getContentPane();
      container.setLayout(new BorderLayout());
      container.add(mainPanel);
      container.add(buttonPanel,BorderLayout.SOUTH);

   }

   /** return a lineaRegressionPanel
    */
   private JPanel createLinearRegressionPanel()
   {
      lrDiagnosticPlotsPanel = new BooleanValuePanel("Plots? ",false);
      lrDiagnosticPlotsPanel.setToolTipText("Additional plots describing more information");
      lrDiagnosticPlotsSinglePagePanel = new BooleanValuePanel("In a Single Page?",false);
      lrDiagnosticPlotsSinglePagePanel.setToolTipText("Additional plots in a single page");
      ActionListener listener = new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
           boolean sel = lrDiagnosticPlotsPanel.getValue();
           lrDiagnosticPlotsSinglePagePanel.setEnabled(sel);
        }
      };
      lrDiagnosticPlotsPanel.addActionListener(listener);
      JPanel plotPanel = new JPanel();
      plotPanel.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createLineBorder(Color.black),"Diagnostic Plots"));
      plotPanel.add(lrDiagnosticPlotsPanel);
      plotPanel.add(lrDiagnosticPlotsSinglePagePanel);

      lrLineColorPanel = new ColorValuePanel("Color",false);
      lineConverter = PrettyOptionImageIconConverter.getHistogramLineStyleConverter();
      ImageIcon [] lineStyles = lineConverter.getAllPrettyOptions();
      lrLineStylePanel = new ImageChooserPanel("Style",false,lineStyles);
      lrLineWidthPanel = new DoubleValuePanel("Width",false,0,5);
      lrLineWidthPanel.setToolTipText("Regression line width > 0 ");
      JPanel linePanel = new JPanel();
      linePanel.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createLineBorder(Color.black),"Regression Line"));
      linePanel.setLayout(new BoxLayout(linePanel, BoxLayout.X_AXIS));
      linePanel.add(lrLineColorPanel);
      linePanel.add(lrLineStylePanel);
      linePanel.add(lrLineWidthPanel);

      lrResLineEnablePanel = new BooleanValuePanel("Enable?",false);
      lrResLineColorPanel = new ColorValuePanel("Color",false);
      lrResLineStylePanel = new ImageChooserPanel("Style",false,lineStyles);
      lrResLineWidthPanel = new DoubleValuePanel("Width",false,0,5);
      lrResLineWidthPanel.setToolTipText("Residual line width > 0 ");

      JPanel resLinePanel1 = new JPanel();
      resLinePanel1.add(lrResLineEnablePanel);
      JPanel resLinePanel2 = new JPanel();
      resLinePanel2.setLayout(new BoxLayout(resLinePanel2, BoxLayout.X_AXIS));
      resLinePanel2.add(lrResLineColorPanel);
      resLinePanel2.add(lrResLineStylePanel);
      resLinePanel2.add(lrResLineWidthPanel);

      JPanel resLinePanel = new JPanel();
      resLinePanel.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createLineBorder(Color.black),"Residual Lines"));
      resLinePanel.setLayout(new BoxLayout(resLinePanel, BoxLayout.Y_AXIS));
      resLinePanel.add(resLinePanel1);
      resLinePanel.add(resLinePanel2);

      lrLabelEnablePanel = new BooleanValuePanel("Enable?",false);

      lrLabelColorPanel = new ColorValuePanel("Color",false);
      lrLabelSizePanel = new DoubleValuePanel("Size",false,0.0, 10.0);
      lrLabelSizePanel.setToolTipText("Font size > 0");
      lrLabelSepPanel = new IntegerValuePanel("Separation",false,0, 10);
      lrLabelSepPanel.setToolTipText("Separation between text in the equation >=0");
      JPanel labelPanel1 = new JPanel();
      labelPanel1.add(lrLabelColorPanel);
      labelPanel1.add(lrLabelSizePanel);
      labelPanel1.add(lrLabelSepPanel);

      lrLabelSigFigPanel = new IntegerValuePanel("Sig. Digits",false,1, 10);
      lrLabelSigFigPanel.setToolTipText("Number of significant digits to appear in the equation > 0");
      JLabel position = new JLabel("Position");
      lrLabelXPosPanel = new DoubleValuePanel("X",false,0.0, 1.0);
      lrLabelXPosPanel.setToolTipText("Position of the text label along the line."+
         " 0.0=left, 0.5=middle. 1.0 right");
      lrLabelYPosPanel = new DoubleValuePanel("Y",false,0.0, 1.0);
      lrLabelYPosPanel.setToolTipText("Position of the text label  perpendicular to the line."+
         " 0.0=bottom, 0.5=middle. 1.0 top");
      JPanel positionPanel = new JPanel();
      positionPanel.add(position);
      positionPanel.add(lrLabelXPosPanel);
      positionPanel.add(lrLabelYPosPanel);

      JPanel labelPanel2 = new JPanel();
      labelPanel2.add(lrLabelSigFigPanel);
      labelPanel2.add(positionPanel);

      lrLabelBGColorPanel = new ColorValuePanel("Background Color",false);
      JPanel labelPanel = new JPanel();
      labelPanel.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createLineBorder(Color.black),"Label"));
      labelPanel.setLayout(new BoxLayout(labelPanel,BoxLayout.Y_AXIS));
      labelPanel.add(lrLabelEnablePanel);
      labelPanel.add(labelPanel1);
      labelPanel.add(labelPanel2);
      labelPanel.add(lrLabelBGColorPanel);

      JPanel lrPanel = new JPanel();
      lrPanel.setLayout(new BoxLayout(lrPanel,BoxLayout.Y_AXIS));
      lrPanel.add(plotPanel);
      lrPanel.add(linePanel);
      lrPanel.add(resLinePanel);
      lrPanel.add(labelPanel);

      return lrPanel;
   }

  /**
    * getter for the date source
    * @return Object actual type type TextBox
   */
   public Object getDataSource()
   {
      return linearRegression;
   }

   public void initGUIFromModel()
   {
//System.out.println("linearRegression.getLinecolor()="+linearRegression.getLinecolor());
      lrDiagnosticPlotsPanel.setValue(linearRegression.getDiagnosticPlots());
      lrDiagnosticPlotsSinglePagePanel.setValue(linearRegression.getDiagnosticPlotsSinglePage());
      lrLineColorPanel.setValue(linearRegression.getLinecolor());
      ImageIcon prettyLine = lineConverter.getPrettyOption(linearRegression.getLinestyle());
      lrLineStylePanel.setValue(prettyLine);
      lrLineWidthPanel.setValue(linearRegression.getLinewidth());

      lrResLineEnablePanel.setValue(linearRegression.getResidualLines());
      lrResLineColorPanel.setValue(linearRegression.getResidualLineColor());
      ImageIcon prettyResLine = lineConverter.getPrettyOption(
         linearRegression.getResidualLineType());
      lrResLineStylePanel.setValue(prettyResLine);
      lrResLineWidthPanel.setValue(linearRegression.getResidualLineWidth());

      TextBorder txt = (TextBorder)linearRegression.getLabel();
      lrLabelEnablePanel.setValue(txt.getEnable());
      lrLabelColorPanel.setValue(txt.getColor());
      lrLabelSizePanel.setValue(txt.getTextExpansion());
      lrLabelXPosPanel.setValue(txt.getXJustification());
      lrLabelYPosPanel.setValue(txt.getYJustification());
      lrLabelBGColorPanel.setValue(txt.getBackgroundColor());
      lrLabelSepPanel.setValue(linearRegression.getSeparation());
      lrLabelSigFigPanel.setValue(linearRegression.getSignificantFigures());

      LinearRegressionStatistics lrs = linearRegression.getStats();
      lrStatsEditor.initGUIFromModel(lrs);

   }

   /** save the gui to the model */
   protected void saveGUIValuesToModel() throws Exception
   {
      linearRegression.setDiagnosticPlots(lrDiagnosticPlotsPanel.getValue());
      linearRegression.setDiagnosticPlotsSinglePage(
         lrDiagnosticPlotsSinglePagePanel.getValue());
      linearRegression.setLinecolor(lrLineColorPanel.getValue());
      String sysLine = lineConverter.getSystemOption(lrLineStylePanel.getValue());
      linearRegression.setLinestyle(sysLine);
      linearRegression.setLinewidth(lrLineWidthPanel.getValue());

      linearRegression.setResidualLines(lrResLineEnablePanel.getValue());
      linearRegression.setResidualLineColor(lrResLineColorPanel.getValue());
      String sysResLine = lineConverter.getSystemOption(lrResLineStylePanel.getValue());
      linearRegression.setResidualLineType(sysResLine);
      linearRegression.setResidualLineWidth(lrResLineWidthPanel.getValue());

      TextBorder txt = (TextBorder)linearRegression.getLabel();
      txt.setEnable(lrLabelEnablePanel.getValue());
      txt.setColor(lrLabelColorPanel.getValue());
      txt.setTextExpansion(lrLabelSizePanel.getValue());
      double xJust = lrLabelXPosPanel.getValue();
      double yJust = lrLabelYPosPanel.getValue();
      txt.setTextString(null);
      txt.setPosition(Text.REGRESSION_LINE, Text.CENTER,xJust,yJust);
      txt.setBackgroundColor(lrLabelBGColorPanel.getValue());
      linearRegression.setLabel(txt);
//System.out.println("In LRE: Region="+((TextBorder)linearRegression.getLabel()).getRegion());
      linearRegression.setSeparation(lrLabelSepPanel.getValue());
      linearRegression.setSignificantFigures(lrLabelSigFigPanel.getValue());
      linearRegression.setStats(lrStatsEditor.saveValuesGUIToModel());
   }

   /**
    * @param args the command line arguments
    */
   public static void main(String[] args)
   {
      DefaultUserInteractor.set(new GUIUserInteractor());
      try
      {
         LinearRegression aLR = new LinearRegression();
         LinearRegressionEditor editor = new LinearRegressionEditor(aLR);
         editor.setVisible(true);
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }//main()

}
