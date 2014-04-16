package gov.epa.mims.analysisengine.tree;

import java.io.Serializable;


/**
 * describes a layout for multiple plots on a single page.
 * It is used as an argument in
 * {@link PageType#setLayout(gov.epa.mims.analysisengine.tree.Layout)}
 * <p>Elided Code Example:
 * <pre>
 *    :
 *    :
 * pageType.setLayout(initLayout());
 *    :
 *    :
 * private Layout initLayout()
 * {
 *    Layout layout = new Layout();
 *    layout.setMatrix(new int[][]
 *    {
 *       { 1, 1 },
 *       { 2, 3 },
 *       { 4, 5 }
 *    });
 *    layout.setRelativeWidths(new int[] { 11, 11 });
 *    layout.setRelativeHeights(new int[] { 2, 10, 10 });
 *
 *    //      layout.setAbsoluteWidths( new double[]{ 5, 5 } );
 *    //      layout.setAbsoluteHeights( new double[]{ 1, 5 , 5 } );
 *    Text layoutTitle = new Text();
 *    layoutTitle.setTextString("Page Layout Title");
 *    layoutTitle.setColor(Color.blue);
 *    layoutTitle.setPosition("N",
 *                            0.5,
 *                            0.5);
 *    layoutTitle.setTextExpansion(1.2);
 *    layoutTitle.setTextDegreesRotation(0.0);
 *    layoutTitle.setTypeface("sans serif");
 *    layoutTitle.setStyle("italic");
 *    layout.setLayoutTitle(layoutTitle);
 *
 *    Text layoutSubTitle = (Text) layoutTitle.clone();
 *    layoutSubTitle.setTextString("Page Layout SubTitle");
 *    layoutSubTitle.setPosition("N", 0.5, 0.3);
 *    layoutSubTitle.setTextExpansion(1.0);
 *    layout.setLayoutSubTitle(layoutSubTitle);
 *
 *    return layout;
 * }
 *
 *
 * </pre>
 *
 * @author Tommy E. Cathey
 * @version $Id: Layout.java,v 1.2 2005/09/19 14:50:10 rhavaldar Exp $
 *
 **/
public class Layout
   implements Serializable,
              Cloneable
{
   /******************************************************
    *
    * fields
    *
    *****************************************************/
   static final long serialVersionUID = 1;

   /** layout subtitle */
   private Text layoutSubTitle;

   /** layout title */
   private Text layoutTitle;

   /** vector describing the absolute heights of the layout */
   private double[] absoluteHeights;

   /** vector describing the absolute widths of the layout */
   private double[] absoluteWidths;

   /** 2D matrix describing the layout */
   private int[][] matrix = null;

   /** vector describing the relative heights of the layout */
   private int[] relativeHeights;

   /** vector describing the relative widths of the layout */
   private int[] relativeWidths;

   /** determines if width unit equals height unit */
   private boolean widthUnitEqualsHeightUnit = false;

   /**
    * set absolute heights
    *
    * @param arg absolute heights
    * @pre arg != null
    ******************************************************/
   public void setAbsoluteHeights(double[] arg)
   {
      this.absoluteHeights = (double[]) arg.clone();
   }

   /**
    * retrieve absolute heights
    *
    * @return absolute heights
    * @pre absoluteHeights != null
    ******************************************************/
   public double[] getAbsoluteHeights()
   {
      boolean bool = (absoluteHeights == null);

      return bool
             ? null
             : (double[]) absoluteHeights.clone();
   }

   /**
    * set absolute widths of layout
    *
    * @param arg absolute widths of layout
    * @pre arg != null
    ******************************************************/
   public void setAbsoluteWidths(double[] arg)
   {
      this.absoluteWidths = (double[]) arg.clone();
   }

   /**
    * retrieve absolute widths of layout
    *
    * @return absolute widths of layout
    * @pre absoluteWidths != null
    ******************************************************/
   public double[] getAbsoluteWidths()
   {
      boolean bool = (absoluteWidths == null);

      return bool
             ? null
             : (double[]) absoluteWidths.clone();
   }

   /**
    *  set layout subtitle
    * <p><A HREF="doc-files/ExampleLayout01.html"><B>View an example</B></A>
    * <br><A HREF="doc-files/ExampleLayout02.html"><B>View an example</B></A>
    * <br><A HREF="doc-files/ExampleLayout03.html"><B>View an example</B></A>
    * <br><A HREF="doc-files/ExampleLayout04.html"><B>View an example</B></A>
    * <br><A HREF="doc-files/ExampleLayout05.html"><B>View an example</B></A>
    *
    * @pre arg != null
    * @param arg layout subtitle
    ******************************************************/
   public void setLayoutSubTitle(gov.epa.mims.analysisengine.tree.Text arg)
   {
      this.layoutSubTitle = arg;
   }

   /**
    * retrieve layout subtitle
    *
    * @return layout subtitle
    ******************************************************/
   public gov.epa.mims.analysisengine.tree.Text getLayoutSubTitle()
   {
      return layoutSubTitle;
   }

   /**
    *  set layout title
    * <p><A HREF="doc-files/ExampleLayout01.html"><B>View an example</B></A>
    * <br><A HREF="doc-files/ExampleLayout02.html"><B>View an example</B></A>
    * <br><A HREF="doc-files/ExampleLayout03.html"><B>View an example</B></A>
    * <br><A HREF="doc-files/ExampleLayout04.html"><B>View an example</B></A>
    * <br><A HREF="doc-files/ExampleLayout05.html"><B>View an example</B></A>
    *
    * @pre arg != null
    * @param arg layout title
    ******************************************************/
   public void setLayoutTitle(gov.epa.mims.analysisengine.tree.Text arg)
   {
      this.layoutTitle = arg;
   }

   /**
    * retrieve layout title
    *
    * @return layout title
    ******************************************************/
   public gov.epa.mims.analysisengine.tree.Text getLayoutTitle()
   {
      return layoutTitle;
   }

   /**
    * set layout matrix
    * <p> specifies the location of figures on the page.
    * Each value in the matrix must be 0 or a
    * positive integer. If N is the largest positive integer in the matrix,
    * then the integers {1,...,N-1} must also appear at least once
    * in the matrix.
    * <p>Example: the code
    * <pre>
    * Layout layout = new Layout();
    * layout.setMatrix(new int[][]
    * {
    *    { 1, 1 },
    *    { 2, 3 },
    *    { 4, 5 }
    * });
    * </pre>
    * <p>creates the following layout on the page
    * <p><IMG BORDER="3" SRC="doc-files/LayoutMatrix01.jpg">
    * <p>Note:
    * <ul>
    * <li>{@link Layout#setMatrix(int[][] arg)} 
    * only defines the order and placement
    * of figures on the page. It doesn't control the sizes of the regions. See
    * {@link Layout#setRelativeWidths(int[] arg)} and 
    * {@link Layout#setRelativeHeights(int[] arg)} or 
    * {@link Layout#setAbsoluteWidths(double[] arg)} and 
    * {@link Layout#setAbsoluteHeights(double[] arg)} for
    * setting of region sizes 
    * <li>region 1 is used for the Layout Titles see 
    * {@link Layout#setLayoutTitle(gov.epa.mims.analysisengine.tree.Text arg)}
    *  and 
    * {@link Layout#setLayoutSubTitle(Text arg)} 
    * </ul>
    *
    * @param arg layout matrix
    * <p><A HREF="doc-files/ExampleLayout01.html"><B>View an example</B></A>
    * <br><A HREF="doc-files/ExampleLayout02.html"><B>View an example</B></A>
    * <br><A HREF="doc-files/ExampleLayout03.html"><B>View an example</B></A>
    * <br><A HREF="doc-files/ExampleLayout04.html"><B>View an example</B></A>
    * <br><A HREF="doc-files/ExampleLayout05.html"><B>View an example</B></A>
    * @pre arg != null
    ******************************************************/
   public void setMatrix(int[][] arg)
   {
      if (arg != null)
      {
         matrix = new int[arg.length][];

         for (int i = 0; i < arg.length; ++i)
         {
            matrix[i] = (int[]) arg[i].clone();
         }
      }
   }

   /**
    * retrieve layout matrix
    *
    * @return layout matrix
    ******************************************************/
   public int[][] getMatrix()
   {
      int[][] rtrn = null;

      if (matrix != null)
      {
         rtrn = new int[matrix.length][matrix[0].length];

         for (int i = 0; i < matrix.length; ++i)
         {
            for (int j = 0; j < matrix[i].length; ++j)
            {
               rtrn[i][j] = matrix[i][j];
            }
         }
      }

      return rtrn;
   }

   /**
    * set relative heights of layout
    *
    * @param arg relative heights of layout
    * @pre arg != null
    ******************************************************/
   public void setRelativeHeights(int[] arg)
   {
      this.relativeHeights = (int[]) arg.clone();
   }

   /**
    * retrieve relative heights of layout
    *
    * @return relative heights of layout
    ******************************************************/
   public int[] getRelativeHeights()
   {
      return (relativeHeights != null)
             ? (int[]) relativeHeights.clone()
             : null;
   }

   /**
    * set relative widths of layout
    * <p><A HREF="doc-files/ExampleLayout01.html"><B>View an example</B></A>
    * <br><A HREF="doc-files/ExampleLayout02.html"><B>View an example</B></A>
    * <br><A HREF="doc-files/ExampleLayout03.html"><B>View an example</B></A>
    * <br><A HREF="doc-files/ExampleLayout04.html"><B>View an example</B></A>
    * <br><A HREF="doc-files/ExampleLayout05.html"><B>View an example</B></A>
    *
    * @param arg relative widths of layout
    * @pre arg != null
    ******************************************************/
   public void setRelativeWidths(int[] arg)
   {
      this.relativeWidths = (int[]) arg.clone();
   }

   /**
    * retrieve vector of relative widths
    *
    * @return vector of relative widths
    ******************************************************/
   public int[] getRelativeWidths()
   {
      return (relativeWidths != null)
             ? (int[]) relativeWidths.clone()
             : null;
   }

   /**
    * set the widthUnitEqualsHeightUnit flag
    *
    * @param arg widthUnitEqualsHeightUnit flag
    ******************************************************/
   public void setWidthUnitEqualsHeightUnit(boolean arg)
   {
      this.widthUnitEqualsHeightUnit = arg;
   }

   /**
    * retrieve the widthUnitEqualsHeightUnit flag
    *
    * @return widthUnitEqualsHeightUnit flag
    ******************************************************/
   public boolean getWidthUnitEqualsHeightUnit()
   {
      return widthUnitEqualsHeightUnit;
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
         Layout clone = (Layout) super.clone();

         if (matrix != null)
         {
            for (int i = 0; i < matrix.length; ++i)
            {
               clone.matrix[i] = (int[]) matrix[i].clone();
            }
         }

         clone.relativeWidths = (relativeWidths == null)
                                ? null
                                : (int[]) relativeWidths.clone();
         clone.relativeHeights = (relativeHeights == null)
                                 ? null
                                 : (int[]) relativeHeights.clone();
         clone.absoluteWidths = (absoluteWidths == null)
                                ? null
                                : (double[]) absoluteWidths.clone();
         clone.absoluteHeights = (absoluteHeights == null)
                                 ? null
                                 : (double[]) absoluteHeights.clone();
         clone.layoutTitle = (layoutTitle == null)
                             ? null
                             : (Text) layoutTitle.clone();
         clone.layoutSubTitle = (layoutSubTitle == null)
                                ? null
                                : (Text) layoutSubTitle.clone();

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
         Layout other = (Layout) o;

         boolean tmp = other.widthUnitEqualsHeightUnit;

         rtrn = Util.equals(matrix, other.matrix)
                && Util.equals(relativeWidths, other.relativeWidths)
                && Util.equals(relativeHeights, other.relativeHeights)
                && Util.equals(absoluteWidths, other.absoluteWidths)
                && Util.equals(absoluteHeights, other.absoluteHeights)
                && layoutTitle.equals(other.layoutTitle)
                && layoutSubTitle.equals(other.layoutSubTitle)
                && (widthUnitEqualsHeightUnit == tmp);
      }

      return rtrn;
   }

   /**
    * print object to string
    *
    * @return String describing object
    ******************************************************/
   public String toString()
   {
      return Util.toString(this);
   }
}