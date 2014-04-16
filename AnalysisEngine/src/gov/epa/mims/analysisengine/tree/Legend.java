package gov.epa.mims.analysisengine.tree;

import java.awt.Color;

import java.io.Serializable;


/**
 * Legend describes a legend box for a plot. It
 * is used as an argument in
 * {@link AnalysisOptions#addOption(java.lang.String key,
 *  java.lang.Object obj) }
 * <p>Elided Code Example:
 * <pre>
 *    :
 *    :
 *  String aLEGEND = LEGEND;
 *  AnalysisOptions options = new AnalysisOptions();
 *  options.addOption(aLEGEND, initLegend());
 *    :
 *    :
 *  private Legend initLegend()
 *  {
 *     Legend legend = new Legend();
 *     legend.setPosition(Legend.RIGHT_HAND_MARGIN, 1.75, 0.5, 0.5);
 *     legend.setCharacterExpansion(0.9);
 *     legend.setNumberColumns(1);
 *     legend.setHorizontal(false);
 *     legend.setXInterspacing(1.2);
 *     legend.setYInterspacing(1.2);
 *     legend.setBackgroundColor(java.awt.Color.lightGray);
 *
 *     return legend;
 *  }
 * </pre>
 *
 *
 * @author Tommy E. Cathey
 * @version $Id: Legend.java,v 1.2 2005/09/19 14:50:10 rhavaldar Exp $
 *
 **/
public class Legend
   extends AnalysisOption
   implements Serializable,
              Cloneable,
              CompassConstantsIfc,
              MarginConstantsIfc,
              AnalysisOptionConstantsIfc
{
   /** serial version UID */
   static final long serialVersionUID = 1;

   /** background color for legend */
   private Color backgroundColor = null;

   /** Region to display Legend
    * <p> used as first argument in
    * {@link setPosition(String,double,double,double)}'
    */
   private String position = RIGHT_HAND_MARGIN;

   /** draw horizontal flag */
   private boolean horizontal = false;

   /** flag set to true when absolute x and y are set */
   private boolean xySet = false;

   /** character expansion coefficient for legend */
   private double characterExpansion = 1.0;

   /** the size of the legend region in inches */
   private double legendRegionSize = 2;

   /** absolute x location of legend in user coordinates */
   private double x;

   /** x interspacing */
   private double xInterspacing = 1.0;

   /** x justification about selected location */
   private double xJustification = 0.5;

   /** absolute y location of legend in user coordinates */
   private double y;

   /** y interspacing */
   private double yInterspacing = 1.0;

   /** y justification about selected location */
   private double yJustification = 0.5;

   /** number of columns in legend */
   private int numberColumns = 1;

   /** enable/disable flag; if false, then is not drawn  */
   private boolean enable = true;

   /**
    * set legend background color
    * <p><A HREF="doc-files/ExampleLegend01.html"><B>View Example</B></A>
    *
    * @param arg legend background color
    * @pre arg != null
    ******************************************************/
   public void setBackgroundColor(java.awt.Color arg)
   {
      this.backgroundColor = arg;
   }

   /**
    * retrieve legend background color
    *
    * @return legend background color
    ******************************************************/
   public java.awt.Color getBackgroundColor()
   {
      return backgroundColor;
   }

   /**
    * set character expansion coefficient
    * <p>
    * <ul>
    * <li>arg = 0.75
    * <p>
    * <img BORDER="3" SRC="doc-files/ExampleLegend04a.jpg" height=92 width=85>
    * <br><A HREF="doc-files/ExampleLegend04.html"><B>View Example</B></A>
    * <li>arg = 1.00
    * <p>
    * <img BORDER="3" SRC="doc-files/ExampleLegend03a.jpg" height=107 width=105>
    * <br><A HREF="doc-files/ExampleLegend03.html"><B>View Example</B></A>
    * <li>arg = 1.50
    * <p>
    * <img BORDER="3" SRC="doc-files/ExampleLegend02a.jpg" height=152 width=153>
    * <br><A HREF="doc-files/ExampleLegend02.html"><B>View Example</B></A>
    * </ul>
    *
    * @param arg character expansion coefficient
    ******************************************************/
   public void setCharacterExpansion(double arg)
   {
      this.characterExpansion = arg;
   }

   /**
    * retrieve character expansion coefficient
    *
    * @return character expansion coefficient
    ******************************************************/
   public double getCharacterExpansion()
   {
      return characterExpansion;
   }

   /**
    * set horizontal flag;
    * <p><A HREF="doc-files/ExampleLegend06.html"><B>View Example</B></A>
    * <ul>
    * <li> true->horizontal legend;
    * <li> false->vertical legend;
    * <li> default = false;
    * </ul>
    * <br> if arg = 'true' then setHorizontal(boolean arg)
    * overrides the value set in {@link Legend#setNumberColumns(int arg)}
    * @param arg true-horizontal legend; false-vertical legend
    ******************************************************/
   public void setHorizontal(boolean arg)
   {
      this.horizontal = arg;
   }

   /**
    * retrieve horizontal flag;
    * <ul>
    * <li> true->horizontal legend;
    * <li> false->vertical legend;
    * <li> default = false;
    * </ul>
    * @return true-horizontal legend; false-vertical legend
    ******************************************************/
   public boolean getHorizontal()
   {
      return horizontal;
   }

   /**
    * retrieve the legend region size in inches
    *
    * @return the legend region size in inches
    ******************************************************/
   public double getLegendRegionSize()
   {
      return legendRegionSize;
   }

   /**
    * set number of columns to use in the legend
    * <p><A HREF="doc-files/ExampleLegend05.html"><B>View Example</B></A>
    * <br>setNumberColumns() is overridden if
    * {@link Legend#setHorizontal(boolean arg)} is set to "true"
    *
    * @param arg number of columns to use in the legend
    * @pre arg >= 1
    ******************************************************/
   public void setNumberColumns(int arg)
   {
      this.numberColumns = arg;
   }

   /**
    * retrieve number of columns to use in the legend
    *
    * @return number of columns to use in the legend
    ******************************************************/
   public int getNumberColumns()
   {
      return numberColumns;
   }

   /**
    * position the legend on the figure
    * <p>
    * <ul>
    * <li>position
    * <br>determines the location of the legend
    * <br>Valid values are:
    *   <ul>
    *   <li>{@link Legend#RIGHT_HAND_MARGIN}
    *   <li>{@link Legend#LEFT_HAND_MARGIN} NOT YET IMPLEMENTED
    *   <li>{@link Legend#TOP_HAND_MARGIN} NOT YET IMPLEMENTED
    *   <li>{@link Legend#BOTTOM_HAND_MARGIN} NOT YET IMPLEMENTED
    *   <li>{@link Legend#PLOT_REGION} NOT YET IMPLEMENTED
    *   </ul>
    * <br><A HREF="doc-files/MarginOutlinesLabeled.jpg">
    *  <B>View margin diagram</B></A>
    * <li>legendRegionSize
    * <br>determines the amount of space in inches to allocate to the legend
    *   <ul>
    *     <li>if position = Legend.RIGHT_HAND_MARGIN, then the right
    *         hand margin is
    *         set to a width of legendRegionSize inches
    *     <li>if position = Legend.LEFT_HAND_MARGIN, then the left hand
    *         margin is set to a width of legendRegionSize inches
    *   </ul>
    * <li>xJustification
    * <br>determines the X positioning of the legend within the region defined
    * by 'position'
    * <br>Valid values are in the range [0,1]
    *   <ul>
    *   <li>0.0 = left justified
    *   <li>0.5 = centered
    *   <li>1.0 = right justified
    *   </ul>
    * <li>yJustification
    * <br>determines the Y positioning of the legend within the region defined
    * by 'position'
    * <br>Valid values are in the range [0,1]
    *   <ul>
    *   <li>0.0 = bottom justified
    *   <li>0.5 = centered
    *   <li>1.0 = top justified
    *   </ul>
    * </ul>
    * <p>NOTE:
    * <br>The plotting code determines the size of the legend and centers
    * it about the point
    * determined by xJustification and yJustification
    *
    * @param position where to locate the legend relative to the plot
    * @param legendRegionSize size of legend region in inches
    * @param xJustification X justification parameter in range [0,1]
    * @param yJustification Y justification parameter in range [0,1]
    */
   public void setPosition(String position, double legendRegionSize, 
      double xJustification, double yJustification)
   {
      this.position = position;
      this.legendRegionSize = legendRegionSize;
      this.xJustification = xJustification;
      this.yJustification = yJustification;
   }

   /**
    * WILL BE REMOVED
    * set position for legend
    * <pre>
    *
    *    "NW" | "N" | "NE"
    *    -----------------
    *     "W" | "C" | "E"
    *    -----------------
    *    "SW" | "S" | "SE"
    *
    * position should be one of the Strings from the above grid
    *
    * The X justification parameter
    *    range of values = [0,1];
    *    0.0 = left justified;
    *    0.5 = centered;
    *    1.0 = right justified
    *
    * The Y justification parameter
    *    range of values = [0,1];
    *    0.0 = left justified;
    *    0.5 = centered;
    *    1.0 = right justified
    * </pre>
    *
    * @param position String describing where to draw the legend
    * @param xJustification X justification parameter in range [0,1]
    * @param yJustification Y justification parameter in range [0,1]
    * @deprecated REMOVE_ME
    ******************************************************/
   public void setPosition(String position, double xJustification, 
      double yJustification)
   {
      this.position = position;
      this.xJustification = xJustification;
      this.yJustification = yJustification;
   }

   /**
    * set position for legend
    *
    * @param x the x position in user coordinates
    * @param y the y position in user coordinates
    * @deprecated REMOVE_ME
    ******************************************************/
   public void setPosition(double x, double y)
   {
      this.x = x;
      this.y = y;
      this.xySet = true;
   }

   /**
    * retrieve position for legend
    *
    * @return String describing where to draw the legend
    ******************************************************/
   public java.lang.String getPosition()
   {
      return position;
   }

   /**
    * retrieve the X coordinate to draw the legend;
    * <ul>
    * <li> ignored if position set in setPosition( java.lang.String arg )
    * </ul>
    * @return X coordinate to draw the legend
    ******************************************************/
   public double getX()
   {
      return x;
   }

   /**
    * set X interspacing
    * <p>
    * <ul>
    * <li>arg = 0.5
    * <p>
    * <img BORDER="3" SRC="doc-files/ExampleLegend07a.jpg" height=105 width=113>
    * <br><A HREF="doc-files/ExampleLegend07.html"><B>View Example</B></A>
    * <li>arg = 1.0
    * <p>
    * <img BORDER="3" SRC="doc-files/ExampleLegend08a.jpg" height=110 width=113>
    * <br><A HREF="doc-files/ExampleLegend08.html"><B>View Example</B></A>
    * <li>arg = 1.5
    * <p>
    * <img BORDER="3" SRC="doc-files/ExampleLegend09a.jpg" height=115 width=113>
    * <br><A HREF="doc-files/ExampleLegend09.html"><B>View Example</B></A>
    * </ul>
    *
    * @param arg X interspacing
    ******************************************************/
   public void setXInterspacing(double arg)
   {
      this.xInterspacing = arg;
   }

   /**
    * retrieve X interspacing
    *
    * @return X interspacing
    ******************************************************/
   public double getXInterspacing()
   {
      return xInterspacing;
   }

   /**
    * retrieve the X justification parameter;
    * <ul>
    * <li> range of values = [0,1];
    * <li> 0.0 = left justified;
    * <li> 0.5 = centered;
    * <li> 1.0 = right justified
    * </ul>
    * @return the X justification parameter
    ******************************************************/
   public double getXJustification()
   {
      return xJustification;
   }

   /**
    * retrieve the Y coordinate to draw the legend;
    * <ul>
    * <li> ignored if position set in setPosition( java.lang.String arg )
    * </ul>
    * @return Y coordinate to draw the legend
    ******************************************************/
   public double getY()
   {
      return y;
   }

   /**
    * set Y interspacing
    * <p>
    * <ul>
    * <li>arg = 0.5
    * <p>
    * <img BORDER="3" SRC="doc-files/ExampleLegend10a.jpg" height=100 width=109>
    * <br><A HREF="doc-files/ExampleLegend10.html"><B>View Example</B></A>
    * <li>arg = 1.0
    * <p>
    * <img BORDER="3" SRC="doc-files/ExampleLegend11a.jpg" height=99 width=110>
    * <br><A HREF="doc-files/ExampleLegend11.html"><B>View Example</B></A>
    * <li>arg = 1.5
    * <p>
    * <img BORDER="3" SRC="doc-files/ExampleLegend12a.jpg" height=132 width=110>
    * <br><A HREF="doc-files/ExampleLegend12.html"><B>View Example</B></A>
    * </ul>
    *
    * @param arg Y interspacing
    ******************************************************/
   public void setYInterspacing(double arg)
   {
      this.yInterspacing = arg;
   }

   /**
    * retrieve Y interspacing
    *
    * @return Y interspacing
    ******************************************************/
   public double getYInterspacing()
   {
      return yInterspacing;
   }

   /**
    * retrieve the Y justification parameter;
    * <ul>
    * <li> range of values = [0,1];
    * <li> 0.0 = left justified;
    * <li> 0.5 = centered;
    * <li> 1.0 = right justified
    * </ul>
    * @return the Y justification parameter
    ******************************************************/
   public double getYJustification()
   {
      return yJustification;
   }

   /**
    * Creates and returns a copy of this object
    *
    * @return a copy of this object
    ******************************************************/
   public Object clone()
   {
      try
      {
         return super.clone();
      }
      catch (CloneNotSupportedException e)
      {
         return null;
      }
   }

   /**
    * Compares this object to the specified object.
    *
    * @param o the object to compare this object against
    *
    * @return true if the objects are equal; false otherwise
    ********************************************************/
   public boolean equals(Object o)
   {
      boolean rtrn = true;

      if (o == null)
      {
         rtrn = false;
      }
      else if (o == this)
      {
         rtrn = true;
      }
      else if (o.getClass() != getClass())
      {
         rtrn = false;
      }
      else
      {
         Legend other = (Legend) o;

         rtrn = ((position == null)
                 ? (other.position == null)
                 : (position.equals(other.position)))
                && (xJustification == other.xJustification)
                && (yJustification == other.yJustification)
                && (legendRegionSize == other.legendRegionSize)
                && (x == other.x) && (y == other.y) && (xySet == other.xySet)
                && ((backgroundColor == null)
                    ? (other.backgroundColor == null)
                    : (backgroundColor.equals(other.backgroundColor)))
                && (characterExpansion == other.characterExpansion)
                && (numberColumns == other.numberColumns)
                && (horizontal == other.horizontal)
                && (xInterspacing == other.xInterspacing)
                && (enable == other.enable)
                && (yInterspacing == other.yInterspacing);
      }

      return rtrn;
   }

   /**
    * enable/disable this AnalysisOption
    *
    * @param arg true->enable; false->disable
    ********************************************************/
   public void setEnable(boolean arg)
   {
      this.enable = arg;
   }

   /**
    * retrieve enable/disable flag
    *
    * @return true->enable; false->disable
    ********************************************************/
   public boolean getEnable()
   {
      return enable;
   }

   /**
    * retrieve the xySet flag
    *
    * @return true if {@link Legend#setPosition(double,double)} has
    * been called otherwise false
    ********************************************************/
   public boolean getxySet()
   {
      return xySet;
   }

   /**
    * describe object in a String
    *
    * @return String describing object
    ******************************************************/
   public String toString()
   {
      return Util.toString(this);
   }
}