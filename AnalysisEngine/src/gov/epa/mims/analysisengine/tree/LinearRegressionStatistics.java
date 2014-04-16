package gov.epa.mims.analysisengine.tree;

import java.io.Serializable;

/**
 * Description of the Class
 *
 * @author    tsb
 * @created   September 7, 2004
 */
public class LinearRegressionStatistics
    implements LineTypeConstantsIfc,
   CompassConstantsIfc,
   MarginConstantsIfc,
   Serializable
{
   /** Description of the Field */
   private boolean enable = false;
   /** Description of the Field */
   private boolean showEquation = true;
   /** Description of the Field */
   private boolean showResiduals = true;
   /** Description of the Field */
   private boolean showResidualStdErr = true;
   /** Description of the Field */
   private boolean showCoefficients = true;
   /** Description of the Field */
   private boolean showCoefficientsStdErr = true;
   /** Description of the Field */
   private boolean showCoefficientsTvalue = true;
   /** Description of the Field */
   private boolean showCoefficientsPvalue = true;
   /** Description of the Field */
   private boolean showFstatistics = true;
   /** Description of the Field */
   private TextBlock textBlock = new TextBlock();

   /**
    * Sets the textBlock attribute of the LinearRegressionStatistics object
    *
    * @param textBlock  The new textBlock value
    */
   public void setTextBlock(TextBlock textBlock)
   {
      this.textBlock = textBlock;
   }


   /**
    * Gets the textBlock attribute of the LinearRegressionStatistics object
    *
    * @return   The textBlock value
    */
   public TextBlock getTextBlock()
   {
      return textBlock;
   }


   /**
    * Sets the enable attribute of the LinearRegressionStatistics object
    *
    * @param enable  The new enable value
    */
   public void setEnable(boolean enable)
   {
      this.enable = enable;
   }


   /**
    * Sets the showEquation attribute of the LinearRegressionStatistics object
    *
    * @param showEquation  The new showEquation value
    */
   public void setShowEquation(boolean showEquation)
   {
      this.showEquation = showEquation;
   }


   /**
    * Sets the showResiduals attribute of the LinearRegressionStatistics object
    *
    * @param showResiduals  The new showResiduals value
    */
   public void setShowResiduals(boolean showResiduals)
   {
      this.showResiduals = showResiduals;
   }


   /**
    * Sets the showResidualStdErr attribute of the LinearRegressionStatistics
    * object
    *
    * @param showResidualStdErr  The new showResidualStdErr value
    */
   public void setShowResidualStdErr(boolean showResidualStdErr)
   {
      this.showResidualStdErr = showResidualStdErr;
   }


   /**
    * Sets the showCoefficients attribute of the LinearRegressionStatistics
    * object
    *
    * @param showCoefficients  The new showCoefficients value
    */
   public void setShowCoefficients(boolean showCoefficients)
   {
      this.showCoefficients = showCoefficients;
   }


   /**
    * Sets the showCoefficientsStdErr attribute of the
    * LinearRegressionStatistics object
    *
    * @param showCoefficientsStdErr  The new showCoefficientsStdErr value
    */
   public void setShowCoefficientsStdErr(boolean showCoefficientsStdErr)
   {
      this.showCoefficientsStdErr = showCoefficientsStdErr;
   }


   /**
    * Sets the showCoefficientsTvalue attribute of the
    * LinearRegressionStatistics object
    *
    * @param showCoefficientsTvalue  The new showCoefficientsTvalue value
    */
   public void setShowCoefficientsTvalue(boolean showCoefficientsTvalue)
   {
      this.showCoefficientsTvalue = showCoefficientsTvalue;
   }


   /**
    * Sets the showCoefficientsPvalue attribute of the
    * LinearRegressionStatistics object
    *
    * @param showCoefficientsPvalue  The new showCoefficientsPvalue value
    */
   public void setShowCoefficientsPvalue(boolean showCoefficientsPvalue)
   {
      this.showCoefficientsPvalue = showCoefficientsPvalue;
   }


   /**
    * Sets the showFstatistics attribute of the LinearRegressionStatistics
    * object
    *
    * @param showFstatistics  The new showFstatistics value
    */
   public void setShowFstatistics(boolean showFstatistics)
   {
      this.showFstatistics = showFstatistics;
   }

   /**
    * Gets the enable attribute of the LinearRegressionStatistics object
    *
    * @return   The enable value
    */
   public boolean getEnable()
   {
      return enable;
   }


   /**
    * Gets the showEquation attribute of the LinearRegressionStatistics object
    *
    * @return   The showEquation value
    */
   public boolean getShowEquation()
   {
      return showEquation;
   }


   /**
    * Gets the showResiduals attribute of the LinearRegressionStatistics object
    *
    * @return   The showResiduals value
    */
   public boolean getShowResiduals()
   {
      return showResiduals;
   }


   /**
    * Gets the showResidualStdErr attribute of the LinearRegressionStatistics
    * object
    *
    * @return   The showResidualStdErr value
    */
   public boolean getShowResidualStdErr()
   {
      return showResidualStdErr;
   }


   /**
    * Gets the showCoefficients attribute of the LinearRegressionStatistics
    * object
    *
    * @return   The showCoefficients value
    */
   public boolean getShowCoefficients()
   {
      return showCoefficients;
   }


   /**
    * Gets the showCoefficientsStdErr attribute of the
    * LinearRegressionStatistics object
    *
    * @return   The showCoefficientsStdErr value
    */
   public boolean getShowCoefficientsStdErr()
   {
      return showCoefficientsStdErr;
   }


   /**
    * Gets the showCoefficientsTvalue attribute of the
    * LinearRegressionStatistics object
    *
    * @return   The showCoefficientsTvalue value
    */
   public boolean getShowCoefficientsTvalue()
   {
      return showCoefficientsTvalue;
   }


   /**
    * Gets the showCoefficientsPvalue attribute of the
    * LinearRegressionStatistics object
    *
    * @return   The showCoefficientsPvalue value
    */
   public boolean getShowCoefficientsPvalue()
   {
      return showCoefficientsPvalue;
   }


   /**
    * Gets the showFstatistics attribute of the LinearRegressionStatistics
    * object
    *
    * @return   The showFstatistics value
    */
   public boolean getShowFstatistics()
   {
      return showFstatistics;
   }

}

