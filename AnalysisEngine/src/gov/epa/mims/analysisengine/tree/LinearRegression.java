package gov.epa.mims.analysisengine.tree;

import java.awt.Color;
import java.io.Serializable;

/**
 * Description of the Class
 *
 * @author    tsb
 * @created   August 20, 2004
 */
public class LinearRegression
    implements LineTypeConstantsIfc,
   Serializable
{
   /** Description of the Field */
   private Color linecolor = Color.black;
   /** Description of the Field */
   private String linestyle = LinearRegression.SOLID;
   /** Description of the Field */
   private boolean enable = true;
   /** Description of the Field */
   private double linewidth = 1.0;
   /** Description of the Field */
   private int separation = 0;
   /** Description of the Field */
   private int significantFigures = 4;
   /** Description of the Field */
   private gov.epa.mims.analysisengine.tree.TextBorder label = new TextBorder();
   /** Description of the Field */
   private boolean residualLines = true;
   /** Description of the Field */
   private String residualLineType = LinearRegression.SOLID;
   /** Description of the Field */
   private double residualLineWidth = 1.0;
   /** Description of the Field */
   private Color residualLineColor = java.awt.Color.black;
   /** Description of the Field */
   private boolean diagnosticPlots = true;
   /** Description of the Field */
   private boolean diagnosticPlotsSinglePage = false;
   /** Description of the Field */
   private LinearRegressionStatistics stats = new LinearRegressionStatistics();


   /**
    * Creates and returns a copy of this object
    *
    * @return   a copy of this object
    */
   public Object clone()
   {
      try
      {
         LinearRegression clone = (LinearRegression) super.clone();
         clone.label = (label == null)
             ? null
             : (TextBorder) label.clone();
         return clone;
      }
      catch (CloneNotSupportedException e)
      {
         return null;
      }
   }


   /**
    * Sets the stats attribute of the LinearRegression object
    *
    * @param stats  The new stats value
    */
   public void setStats(LinearRegressionStatistics stats)
   {
      this.stats = stats;
   }


   /**
    * Gets the stats attribute of the LinearRegression object
    *
    * @return   The stats value
    */
   public LinearRegressionStatistics getStats()
   {
      return stats;
   }


   /**
    * Sets the residualLineType attribute of the LinearRegression object
    *
    * @param residualLineType  The new residualLineType value
    */
   public void setResidualLineType(String residualLineType)
   {
      this.residualLineType = residualLineType;
   }


   /**
    * Sets the residualLineWidth attribute of the LinearRegression object
    *
    * @param residualLineWidth  The new residualLineWidth value
    */
   public void setResidualLineWidth(double residualLineWidth)
   {
      this.residualLineWidth = residualLineWidth;
   }


   /**
    * Sets the residualLineColor attribute of the LinearRegression object
    *
    * @param residualLineColor  The new residualLineColor value
    */
   public void setResidualLineColor(Color residualLineColor)
   {
      this.residualLineColor = residualLineColor;
   }


   /**
    * Gets the residualLineType attribute of the LinearRegression object
    *
    * @return   The residualLineType value
    */
   public String getResidualLineType()
   {
      return residualLineType;
   }


   /**
    * Gets the residualLineWidth attribute of the LinearRegression object
    *
    * @return   The residualLineWidth value
    */
   public double getResidualLineWidth()
   {
      return residualLineWidth;
   }


   /**
    * Gets the residualLineColor attribute of the LinearRegression object
    *
    * @return   The residualLineColor value
    */
   public Color getResidualLineColor()
   {
      return residualLineColor;
   }


   /**
    * Sets the residualLines attribute of the LinearRegression object
    *
    * @param residualLines  The new residualLines value
    */
   public void setResidualLines(boolean residualLines)
   {
      this.residualLines = residualLines;
   }


   /**
    * Sets the diagnosticPlots attribute of the LinearRegression object
    *
    * @param diagnosticPlots  The new diagnosticPlots value
    */
   public void setDiagnosticPlots(boolean diagnosticPlots)
   {
      this.diagnosticPlots = diagnosticPlots;
   }


   /**
    * Sets the diagnosticPlotsSinglePage attribute of the LinearRegression
    * object
    *
    * @param diagnosticPlotsSinglePage  The new diagnosticPlotsSinglePage value
    */
   public void setDiagnosticPlotsSinglePage(boolean diagnosticPlotsSinglePage)
   {
      this.diagnosticPlotsSinglePage = diagnosticPlotsSinglePage;
   }


   /**
    * Gets the residualLines attribute of the LinearRegression object
    *
    * @return   The residualLines value
    */
   public boolean getResidualLines()
   {
      return residualLines;
   }


   /**
    * Gets the diagnosticPlots attribute of the LinearRegression object
    *
    * @return   The diagnosticPlots value
    */
   public boolean getDiagnosticPlots()
   {
      return diagnosticPlots;
   }


   /**
    * Gets the diagnosticPlotsSinglePage attribute of the LinearRegression
    * object
    *
    * @return   The diagnosticPlotsSinglePage value
    */
   public boolean getDiagnosticPlotsSinglePage()
   {
      return diagnosticPlotsSinglePage;
   }


   /**
    * Sets the separation attribute of the LinearRegression object
    *
    * @param separation  The new separation value
    */
   public void setSeparation(int separation)
   {
      this.separation = separation;
   }


   /**
    * Sets the significantFigures attribute of the LinearRegression object
    *
    * @param significantFigures  The new significantFigures value
    */
   public void setSignificantFigures(int significantFigures)
   {
      this.significantFigures = significantFigures;
   }


   /**
    * Gets the separation attribute of the LinearRegression object
    *
    * @return   The separation value
    */
   public int getSeparation()
   {
      return separation;
   }


   /**
    * Gets the significantFigures attribute of the LinearRegression object
    *
    * @return   The significantFigures value
    */
   public int getSignificantFigures()
   {
      return significantFigures;
   }


   /**
    * Sets the linecolor attribute of the LinearRegession object
    *
    * @param linecolor  The new linecolor value
    */
   public void setLinecolor(Color linecolor)
   {
      this.linecolor = linecolor;
   }


   /**
    * Sets the linestyle attribute of the LinearRegession object
    *
    * @param linestyle  The new linestyle value
    */
   public void setLinestyle(String linestyle)
   {
      this.linestyle = linestyle;
   }


   /**
    * Sets the enable attribute of the LinearRegession object
    *
    * @param enable  The new enable value
    */
   public void setEnable(boolean enable)
   {
      this.enable = enable;
   }


   /**
    * Sets the linewidth attribute of the LinearRegession object
    *
    * @param linewidth  The new linewidth value
    */
   public void setLinewidth(double linewidth)
   {
      this.linewidth = linewidth;
   }


   /**
    * Sets the label attribute of the LinearRegession object
    *
    * @param label  The new label value
    */
   public void setLabel(gov.epa.mims.analysisengine.tree.TextBorder label)
   {
      this.label = label;
   }


   /**
    * Gets the linecolor attribute of the LinearRegession object
    *
    * @return   The linecolor value
    */
   public Color getLinecolor()
   {
      return linecolor;
   }


   /**
    * Gets the linestyle attribute of the LinearRegession object
    *
    * @return   The linestyle value
    */
   public String getLinestyle()
   {
      return linestyle;
   }


   /**
    * Gets the enable attribute of the LinearRegession object
    *
    * @return   The enable value
    */
   public boolean getEnable()
   {
      return enable;
   }

   /**
    * Gets the linewidth attribute of the LinearRegession object
    *
    * @return   The linewidth value
    */
   public double getLinewidth()
   {
      return linewidth;
   }

   /**
    * Gets the label attribute of the LinearRegession object
    *
    * @return   The label value
    */
   public gov.epa.mims.analysisengine.tree.Text getLabel()
   {
      return label;
   }

}

