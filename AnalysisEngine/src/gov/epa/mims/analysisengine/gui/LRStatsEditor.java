package gov.epa.mims.analysisengine.gui;

import gov.epa.mims.analysisengine.tree.LinearRegressionStatistics;

import gov.epa.mims.analysisengine.tree.TextBlock;
import java.awt.BorderLayout;

import javax.swing.*;
import java.awt.Color;

/*
 * LRStatsEditor.java
 * A editor for the LinearRegressionStatistics
 * @author  Parthee R Partheepan
 * @version $Id: LRStatsEditor.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 */
public class LRStatsEditor extends JPanel
{
   private LinearRegressionStatistics lrStats;

   //Components related to LinearRegressionStatistics
   private BooleanValuePanel lrsEnableBooleanPanel;

   private BooleanValuePanel lrsCoefBooleanPanel;
   private BooleanValuePanel lrsPValueBooleanPanel;
   private BooleanValuePanel lrsStdErrBooleanPanel;
   private BooleanValuePanel lrsTValueBooleanPanel;
   private BooleanValuePanel lrsEquatBooleanPanel;
   private BooleanValuePanel lrsFStatsBooleanPanel;
   private BooleanValuePanel lrsResBooleanPanel;
   private BooleanValuePanel lrsResStdErrBooleanPanel;

   private TextBlockEditor txtBlockEditor;

   /** Creates a new instance of LRStatsEditor */
   public LRStatsEditor()
   {
      initialize();
   }

   private void initialize()
   {
      lrsEnableBooleanPanel = new BooleanValuePanel("Enable?",false);
      lrsCoefBooleanPanel = new BooleanValuePanel("Coefficient",false);
      lrsPValueBooleanPanel = new BooleanValuePanel("PValue",false);
      lrsStdErrBooleanPanel = new BooleanValuePanel("Std Err",false);
      lrsTValueBooleanPanel = new BooleanValuePanel("TValue",false);
      lrsEquatBooleanPanel = new BooleanValuePanel("Equation",false);
      lrsFStatsBooleanPanel = new BooleanValuePanel("FStats",false);
      lrsResBooleanPanel = new BooleanValuePanel("Residuals",false);
      lrsResStdErrBooleanPanel = new BooleanValuePanel("Residuals Std Err",false);

      JPanel showStatsPanel1 = new JPanel();
      showStatsPanel1.add(lrsCoefBooleanPanel);
      showStatsPanel1.add(lrsPValueBooleanPanel);
      showStatsPanel1.add(lrsStdErrBooleanPanel);
      showStatsPanel1.add(lrsTValueBooleanPanel);
      JPanel showStatsPanel2 = new JPanel();
      showStatsPanel2.add(lrsEquatBooleanPanel);
      showStatsPanel2.add(lrsFStatsBooleanPanel);
      showStatsPanel2.add(lrsResBooleanPanel);
      showStatsPanel2.add(lrsResStdErrBooleanPanel);
      JPanel showStatsPanel = new JPanel();
      showStatsPanel.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createLineBorder(Color.black),"Show Results?"));
      showStatsPanel.setLayout(new BoxLayout(showStatsPanel, BoxLayout.Y_AXIS));
      showStatsPanel.add(showStatsPanel1);
      showStatsPanel.add(showStatsPanel2);

      JPanel txtBlockPanel = new JPanel(new BorderLayout());
      txtBlockEditor = new TextBlockEditor();
      txtBlockPanel.add(txtBlockEditor);

      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
      mainPanel.add(lrsEnableBooleanPanel);
      mainPanel.add(showStatsPanel);
      mainPanel.add(txtBlockPanel);
      this.setLayout(new BorderLayout());
      this.add(mainPanel);
   }

   public void initGUIFromModel(LinearRegressionStatistics lrStats)
   {
      this.lrStats = lrStats;
      lrsEnableBooleanPanel.setValue(lrStats.getEnable());
      lrsCoefBooleanPanel.setValue(lrStats.getShowCoefficients());
      lrsPValueBooleanPanel.setValue(lrStats.getShowCoefficientsPvalue());
      lrsStdErrBooleanPanel.setValue(lrStats.getShowCoefficientsStdErr());
      lrsTValueBooleanPanel.setValue(lrStats.getShowCoefficientsTvalue());
      lrsEquatBooleanPanel.setValue(lrStats.getShowEquation());
      lrsFStatsBooleanPanel.setValue(lrStats.getShowFstatistics());
      lrsResBooleanPanel.setValue(lrStats.getShowResiduals());
      lrsResStdErrBooleanPanel.setValue(lrStats.getShowResidualStdErr());
      txtBlockEditor.initGUIFromModel(lrStats.getTextBlock());
   }

   public LinearRegressionStatistics saveValuesGUIToModel()
      throws Exception
   {
      lrStats.setEnable(lrsEnableBooleanPanel.getValue());
      lrStats.setShowCoefficients(lrsCoefBooleanPanel.getValue());
      lrStats.setShowCoefficientsPvalue(lrsPValueBooleanPanel.getValue());
      lrStats.setShowCoefficientsStdErr(lrsStdErrBooleanPanel.getValue());
      lrStats.setShowCoefficientsTvalue(lrsTValueBooleanPanel.getValue());
      lrStats.setShowEquation(lrsEquatBooleanPanel.getValue());
      lrStats.setShowFstatistics(lrsFStatsBooleanPanel.getValue());
      lrStats.setShowResiduals(lrsResBooleanPanel.getValue());
      lrStats.setShowResidualStdErr(lrsResStdErrBooleanPanel.getValue());
      TextBlock txtBlock = txtBlockEditor.saveValuesGUIToModel();
      lrStats.setTextBlock(txtBlock);
      return lrStats;
   }

   /**
    * @param args the command line arguments
    */
   public static void main(String[] args)
   {
      LRStatsEditor editor = new LRStatsEditor();
      LinearRegressionStatistics lrs = new LinearRegressionStatistics();
      editor.initGUIFromModel(lrs);
      JFrame f = new JFrame();
      f.getContentPane().add(editor);
      f.pack();
      f.setVisible(true);
   }
}
