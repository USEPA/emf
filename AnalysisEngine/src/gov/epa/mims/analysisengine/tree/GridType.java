package gov.epa.mims.analysisengine.tree;

import java.awt.Color;

import java.io.Serializable;


/**
 * describes lines and point symbols for a plot
 *
 * @author Tommy E. Cathey
 * @version $Id: GridType.java,v 1.2 2005/09/19 14:50:10 rhavaldar Exp $
 *
 **/
public class GridType
   extends AnalysisOption
   implements Serializable,
              Cloneable,
              LineTypeConstantsIfc
{
   /******************************************************
    *
    * fields
    *
    *****************************************************/
   static final long serialVersionUID = 1;

   /** grid line color */
   private Color color = null;

   /** line style for grid lines */
   private String lineStyle = null;

   /** draw grid flag */
   private boolean draw = true;

   /** line width for grid lines */
   private double lineWidth;

   /** number of cells in the X direction */
   private int numberXcells = -1;

   /** number of cells in the Y direction */
   private int numberYcells = -1;

   /**
    * set color of grid lines
    *
    * @param arg desired color of grid lines
    * @pre arg != null
    * @deprecated use gridded axis instead
    ******************************************************/
   public void setColor(java.awt.Color arg)
   {
      this.color = arg;
   }

   /**
    * retrieve color of grid lines
    *
    * @return color of grid lines
    * @deprecated use gridded axis instead
    ******************************************************/
   public java.awt.Color getColor()
   {
      return color;
   }

   /**
    * set the draw flag
    *
    * @param arg true->draw the grid; false->do not draw the grid
    * @deprecated use gridded axis instead
    ******************************************************/
   public void setDraw(boolean arg)
   {
      this.draw = arg;
   }

   /**
    * retrieve draw flag
    *
    * @return true->draw the grid; false->do not draw the grid
    * @deprecated use gridded axis instead
    ******************************************************/
   public boolean getDraw()
   {
      return draw;
   }

   /**
    * set line style for grid lines
    *
    * @param arg desired line style for grid lines
    * @pre arg != null
    * @deprecated use gridded axis instead
    ******************************************************/
   public void setLineStyle(java.lang.String arg)
   {
      this.lineStyle = arg;
   }

   /**
    * retrieve line style for grid lines
    *
    * @return line style for grid lines
    * @deprecated use gridded axis instead
    ******************************************************/
   public java.lang.String getLineStyle()
   {
      return lineStyle;
   }

   /**
    * set line width for grid lines
    *
    * @param arg desired line width for grid lines
    * @pre !Double.isNaN(arg)
    * @deprecated use gridded axis instead
    ******************************************************/
   public void setLineWidth(double arg)
   {
      this.lineWidth = arg;
   }

   /**
    * retrieve line width for grid lines
    *
    * @return line width for grid lines
    * @deprecated use gridded axis instead
    ******************************************************/
   public double getLineWidth()
   {
      return lineWidth;
   }

   /**
    * set number of X cells;
    * <ul>
    * <li> Optional;
    * <li> -1 causes cells to align with tick marks;
    * <li> -1 is the default
    * </ul>
    *
    * @param arg the number of X cells
    * @pre (( arg == -1 ) || ( arg > 0 ))
    * @deprecated use gridded axis instead
    ******************************************************/
   public void setNumberXcells(int arg)
   {
      this.numberXcells = arg;
   }

   /**
    * retrieve number of X cells;
    * <ul>
    * <li> Optional;
    * <li> -1 causes cells to align with tick marks;
    * <li> -1 is the default
    * </ul>
    *
    * @return number of X cells
    * @deprecated use gridded axis instead
    ******************************************************/
   public int getNumberXcells()
   {
      return numberXcells;
   }

   /**
    * set number of Y cells;
    * <ul>
    * <li> Optional;
    * <li> -1 causes cells to align with tick marks;
    * <li> -1 is the default
    * </ul>
    *
    * @param arg the number of Y cells
    * @pre (( arg == -1 ) || ( arg > 0 ))
    * @deprecated use gridded axis instead
    ******************************************************/
   public void setNumberYcells(int arg)
   {
      this.numberYcells = arg;
   }

   /**
    * retrieve number of Y cells;
    * <ul>
    * <li> Optional;
    * <li> -1 causes cells to align with tick marks;
    * <li> -1 is the default
    * </ul>
    *
    * @return number of Y cells
    * @deprecated use gridded axis instead
    ******************************************************/
   public int getNumberYcells()
   {
      return numberYcells;
   }

   /**
    * creates a clone of this object
    *
    * @return a clone of this object
    * @deprecated use gridded axis instead
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
    * DOCUMENT_ME
    *
    * @param o DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    * @deprecated use gridded axis instead
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
         GridType other = (GridType) o;

         rtrn = ((lineStyle == null)
                 ? (other.lineStyle == null)
                 : (lineStyle.equals(other.lineStyle)))
                && (lineWidth == other.lineWidth)
                && (numberXcells == other.numberXcells)
                && (numberYcells == other.numberYcells)
                && (color.equals(other.color)) && (draw == other.draw);
      }

      return rtrn;
   }

   /**
    * describe object in a String
    *
    * @return String describing object
    * @deprecated use gridded axis instead
    ******************************************************/
   public String toString()
   {
      return Util.toString(this);
   }
}