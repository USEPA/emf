package gov.epa.mims.analysisengine.tree;

import java.awt.Color;

import java.io.Serializable;


/**
 * describes the display of the plot outline box. It
 * is used as an argument in
 * {@link AnalysisOptions#addOption(java.lang.String key,
 *  java.lang.Object obj) }
 * <p>Elided Code Example:
 * <pre>
 *    :
 * String aOUTLINETYPE = OUTLINE_TYPE;
 * AnalysisOptions options = new AnalysisOptions();
 * options.addOption(aOUTLINETYPE, initOutlineType());
 *    :
 *    :
 * private OutlineType initOutlineType()
 * {
 *    OutlineType outlineType = new OutlineType();
 *    outlineType.setLineStyle("SOLID");
 *    outlineType.setLineWidth(2.0);
 *    outlineType.setColor(Color.red);
 *    outlineType.setDraw(true);
 *
 *    return outlineType;
 * }
 *
 * </pre>
 *
 * @author Tommy E. Cathey
 * @version $Id: OutlineType.java,v 1.2 2005/09/19 14:50:10 rhavaldar Exp $
 *
 **/
public class OutlineType
   extends AnalysisOption
   implements Serializable,
              Cloneable,
              LineTypeConstantsIfc,
              MarginConstantsIfc,
              AnalysisOptionConstantsIfc
{
   /** serial version UID */
   static final long serialVersionUID = 1;

   /** array of allowed regions */
   public static final int[] TYPES = new int[] 
   {
      FIGURE, 
      PLOT, 
      INNER, 
      OUTER
   };

   /**
    * line color of the outlined
    * <p>set by call to {@link OutlineType#setColor(Color)}
    * <p>returned by call to {@link OutlineType#getColor()}
    */
   private Color[] color = new Color[] 
   {
      Color.black, 
      Color.black,
      Color.black,
      Color.black
   };

   /**
    * draw grid if true; do not draw grid if false
    * <p>set by call to {@link OutlineType#setDraw(boolean)}
    * <p>returned by call to {@link OutlineType#getDraw()}
    */
   private boolean[] draw = new boolean[] 
   {
      true, 
      true, 
      true, 
      true
   };

   /**
    * Line style
    * <p>set by call to {@link OutlineType#setLineStyle(java.lang.String)}
    * <p>returned by call to {@link OutlineType#getLineStyle()}
    */
   private String[] lineStyle = new String[] 
   {
      SOLID, 
      SOLID, 
      SOLID, 
      SOLID
   };

   /**
    * line width of the outlined
    * <p>set by call to {@link OutlineType#setLineWidth(double)}
    * <p>returned by call to {@link OutlineType#getLineWidth()}
    */
   private double[] lineWidth = new double[] 
   {
      1.0, 
      1.0, 
      1.0, 
      1.0
   };

   /**
    * set desired line color
    *
    * <p><A HREF="doc-files/ExampleOutlineType01.html"><B>View Example</B></A>
    * <p><A HREF="doc-files/ExampleOutlineType05.html"><B>View Example</B></A>
    * @preXXX color != arg
    * @param type {@link #FIGURE} {@link #PLOT} {@link #INNER} or {@link #OUTER}
    * @param arg desired line color
    ******************************************************/
   public void setColor(int type, java.awt.Color arg)
   {
      this.color[type] = arg;
   }

   /**
    * retrieve line color
    *
    * @param type {@link #FIGURE} {@link #PLOT} {@link #INNER} or {@link #OUTER}
    * @return color set in setColor( java.awt.Color arg )
    ******************************************************/
   public java.awt.Color getColor(int type)
   {
      return color[type];
   }

   /**
    * set the draw flag
    * <p>
    * <ul>
    * <li>true - draw the grid
    * <li>false - do not draw the grid
    * </ul>
    *
    * <p><A HREF="doc-files/ExampleOutlineType01.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleOutlineType06.html"><B>View Example</B></A>
    * @param type {@link #FIGURE} {@link #PLOT} {@link #INNER} or {@link #OUTER}
    * @param arg flag to determine whether grid should be drawn
    ******************************************************/
   public void setDraw(int type, boolean arg)
   {
      this.draw[type] = arg;
   }

   /**
    * retrieve draw flag
    * <pre>
    * o true - draw the grid
    * o false - do not draw the grid
    * </pre>
    *
    * @param type {@link #FIGURE} {@link #PLOT} {@link #INNER} or {@link #OUTER}
    * @return flag set in setDraw( boolean arg )
    ******************************************************/
   public boolean getDraw(int type)
   {
      return draw[type];
   }

   /**
    * set line style
    * <p>Valid Values:
    * <ul>
    * <li>{@link OutlineType#SOLID}
    * <li>{@link OutlineType#DASHED}
    * <li>{@link OutlineType#DOTTED}
    * <li>{@link OutlineType#DOTDASH}
    * <li>{@link OutlineType#LONGDASH}
    * <li>{@link OutlineType#TWODASH}
    * <li>{@link OutlineType#BLANK}
    * <li>Alternatively, a string of up to 8 characters
    * (from 0:9, "A":"F") may be given, giving the length of line segments
    * which are alternatively drawn and skipped. Used cyclically.
    * NOTE: mixing named types with the up to 8 characters string types can
    * be problematic when generating legends.
    * </ul>
    * <br><A HREF="doc-files/ExampleOutlineType01.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleOutlineType02.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleAxisLog03.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleAxisLog04.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleReferenceLine07.html">
    *       <B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleReferenceLine08.html">
    *       <B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleLineType21.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleLineType22.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleLineType23.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleLineType24.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleLineType25.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleLineType26.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleLayout06.html"><B>View Example</B></A>
    *
    * @pre arg != null
    * @param type {@link #FIGURE} {@link #PLOT} {@link #INNER} or {@link #OUTER}
    * @param arg the desired line style
    ******************************************************/
   public void setLineStyle(int type, java.lang.String arg)
   {
      this.lineStyle[type] = arg;
   }

   /**
    * retrieve the line style
    * <p>Returns one of following Values:
    * <ul>
    * <li>{@link OutlineType#SOLID}
    * <li>{@link OutlineType#DASHED}
    * <li>{@link OutlineType#DOTTED}
    * <li>{@link OutlineType#DOTDASH}
    * <li>{@link OutlineType#LONGDASH}
    * <li>{@link OutlineType#TWODASH}
    * <li>{@link OutlineType#BLANK}
    * <li>Alternatively, a string of up to 8 characters
    * (from 0:9, "A":"F") may be given, giving the length of line segments
    * which are alternatively drawn and skipped. Used cyclically.
    * NOTE: mixing named types with the up to 8 characters string types can
    * be problematic when generating legends.
    * </ul>
    *
    * @param type {@link #FIGURE} {@link #PLOT} {@link #INNER} or {@link #OUTER}
    * @return line style set in setLineStyle( java.lang.String arg )
    ******************************************************/
   public java.lang.String getLineStyle(int type)
   {
      return lineStyle[type];
   }

   /**
    * set line width
    *
    * <p><A HREF="doc-files/ExampleOutlineType01.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleOutlineType03.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleOutlineType04.html"><B>View Example</B></A>
    * @pre !Double.isNaN(arg)
    * @param type {@link #FIGURE} {@link #PLOT} {@link #INNER} or {@link #OUTER}
    * @param arg desired line width
    ******************************************************/
   public void setLineWidth(int type, double arg)
   {
      this.lineWidth[type] = arg;
   }

   /**
    * retrieve line width
    *
    * @param type {@link #FIGURE} {@link #PLOT} {@link #INNER} or {@link #OUTER}
    * @return line width set in setLineWidth( double arg )
    ******************************************************/
   public double getLineWidth(int type)
   {
      return lineWidth[type];
   }

   /**
    * generate clone
    *
    * @return clone of this object
    ******************************************************/
   public Object clone()
   {
      try
      {
         OutlineType clone = (OutlineType) super.clone();

         clone.lineStyle = (lineStyle == null)
                           ? null
                           : (String[]) lineStyle.clone();
         clone.lineWidth = (lineWidth == null)
                           ? null
                           : (double[]) lineWidth.clone();
         clone.color = (color == null)
                       ? null
                       : (Color[]) color.clone();
         clone.draw = (draw == null)
                      ? null
                      : (boolean[]) draw.clone();

         return clone;
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
         OutlineType other = (OutlineType) o;

         rtrn = (Util.equals(lineStyle, other.lineStyle)
                && Util.equals(lineWidth, other.lineWidth)
                && Util.equals(color, other.color)
                && Util.equals(draw, other.draw));
      }

      return rtrn;
   }

   /**
    * print object to a string
    *
    * @return String describing object
    ******************************************************/
   public String toString()
   {
      return Util.toString(this);
   }
}