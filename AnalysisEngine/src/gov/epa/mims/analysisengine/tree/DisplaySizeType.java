package gov.epa.mims.analysisengine.tree;

import java.io.Serializable;

/**
 * class for controlling the region sizes of a plot <br>
 * <A HREF="doc-files/ExampleDisplaySizeType00.html"><B>View Example</B> </A>
 * <p>
 *
 * <p>
 *
 * Elided Code Example: <pre>
 *    :
 *    :
 *    :
 *    :
 *
 * </pre> <br>
 * <A HREF="doc-files/ExampleLineType15.html"><B>View Example</B> </A> <br>
 * <A HREF="doc-files/ExampleLineType16.html"><B>View Example</B> </A> <br>
 * <A HREF="doc-files/ExampleLineType17.html"><B>View Example</B> </A> <br>
 * <A HREF="doc-files/ExampleLineType18.html"><B>View Example</B> </A> <br>
 * <A HREF="doc-files/ExampleLineType19.html"><B>View Example</B> </A> <br>
 * <A HREF="doc-files/ExampleLineType20.html"><B>View Example</B> </A>
 *
 * @author    Tommy E. Cathey
 * @created   August 17, 2004
 * @version   $Id: DisplaySizeType.java,v 1.2 2005/09/19 14:50:10 rhavaldar Exp $
 */
public class DisplaySizeType
    extends AnalysisOption
    implements Serializable,
   Cloneable
{
   /** serial version UID */
   final static long serialVersionUID = 1;

   /**
    * Fraction of Figure region <p>
    *
    * Use as units argument in {@link #setMarginOuter(double,double,double,double,int)}
    * {@link #setMarginOuter(double,double,int)}
    */
   public final static int FOF = 0;

   /**
    * Normalized Device Coordinates <p>
    *
    * Use as units argument in {@link #setMarginOuter(double,double,double,double,int)}
    * {@link #setMarginOuter(double,double,int)}
    */
   public final static int NDC = 1;

   /**
    * Inches <p>
    *
    * Use as units argument in {@link #setMarginOuter(double,double,double,double,int)}
    * {@link #setMarginOuter(double,double,int)}
    */
   public final static int INCHES = 2;

   /**
    * Lines of Text <p>
    *
    * Use as units argument in {@link #setMarginOuter(double,double,double,double,int)}
    * {@link #setMarginOuter(double,double,int)}
    */
   public final static int LINES_OF_TEXT = 3;

   /** figure units */
   private double[] figure = null;

   /** display width & height in inches */
   private double[] display = new double[]{7.0, 7.0};

   /** outer margin coordinates */
   private double[] marginOuter = null;

   /** plot coordinates */
   private double[] plot = null;

   /** enable/disable flag; if false the R defaults are used */
   private boolean enable = false;

   /** outer margin units */
   private int marginOuterUnits = -1;

   /** plot units */
   private int plotUnits = -1;
   
   /** a approximate conversion factor for from inches to pixes */
   public static final int PIXELS_PER_INCH = 72;

   /**
    * Creates and returns a copy of this object
    *
    * @return                             a copy of this object
    * @throws CloneNotSupportedException  is cloning not supported
    */
   public Object clone()
   {
      DisplaySizeType clone = null;

      try
      {
         clone = (DisplaySizeType) super.clone();

         clone.figure = this.getFigure();

         return clone;
      }
      catch (CloneNotSupportedException e)
      {
         //clone will remain null
      }

      return clone;
   }


   /**
    * Compares this object to the specified object.
    *
    * @param o  the object to compare this object against
    * @return   true if the objects are equal; false otherwise
    */
   public boolean equals(Object o)
   {
      boolean rtrn = true;

      if (!super.equals(o))
      {
         rtrn = false;
      }
      else if (o == null)
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
         DisplaySizeType other = (DisplaySizeType) o;

         rtrn = (Util.equals(figure, other.figure) && (enable == other.enable)
             && Util.equals(marginOuter, other.marginOuter)
             && Util.equals(marginOuterUnits, other.marginOuterUnits)
             && Util.equals(plot, other.plot)
             && Util.equals(plotUnits, other.plotUnits));
      }

      return rtrn;
   }


   /**
    * Sets the display attribute of the DisplaySizeType object display =
    * {width,height} of display region in inches
    *
    * @param display  The new display value
    */
   public void setDisplay(double[] display)
   {
      this.display = display;
   }


   /**
    * Gets the display attribute of the DisplaySizeType object
    *
    * @return   The display value
    */
   public double[] getDisplay()
   {
      return display;
   }


   /**
    * enable/disable this AnalysisOption
    *
    * @param arg  true->enable; false->disable
    */
   public void setEnable(boolean arg)
   {
      this.enable = arg;
   }


   /**
    * retrieve enable/disable flag
    *
    * @return   true->enable; false->disable
    */
   public boolean getEnable()
   {
      return enable;
   }


   /**
    * set figure coordinates in Normalized Coordinates <pre>
    *
    *     ----------------Device Boundary-----------------
    *     |                                              |
    *     |                                              |
    *     | --------------Outer Margin------------------ |
    *     | |                                          | |
    *     | |                                          | |
    * y2  | |  -----------Figure Region--------------  | |
    * ^   | |  |                                    |  | |
    * |   | |  |                                    |  | |
    * |   | |  |   -------Plot Region-----------    |  | |
    * |   | |  |   |                           |    |  | |
    * |   | |  | A |                           |    |  | |
    * |   | |  | x |                           |    |  | |
    * |   | |  | i |                           |    |  | |
    * |   | |  | s |                           |    |  | |
    * |   | |  |   |                           |    |  | |
    * |   | |  | L |                           |    |  | |
    * |   | |  | a |                           |    |  | |
    * |   | |  | b |                           |    |  | |
    * |   | |  | e |                           |    |  | |
    * |   | |  | l |                           |    |  | |
    * |   | |  | s |                           |    |  | |
    * |   | |  |   |                           |    |  | |
    * |   | |  |   |                           |    |  | |
    * |   | |  |   -----------------------------    |  | |
    * |   | |  |    Axis Labels                     |  | |
    * |   | |  |                                    |  | |
    * |y1 | |  --------------------------------------  | |
    * |^  | |                                          | |
    * ||  | |                                          | |
    * --  | O------------------------------------------- |
    *     |                                              |
    *     ------------------------------------------------
    *
    *       |--> x1
    *       |-------------------------------------->x2
    *
    * </pre> all values are Normalized so their values should range from [0,1]
    * and values must be such that x2 > x1 and y2 > y1. If the figure region is
    * not set then it defaults to its maximum size which is equal to the Outer
    * Margin.
    *
    * @param x1  min x coordinate
    * @param x2  max x coordinate
    * @param y1  min y coordinate
    * @param y2  max y coordinate
    * @pre       x1 >= 0.0
    * @pre       x2 < 1.0
    * @pre       x2 > x1
    * @pre       y1 >= 0.0
    * @pre       y2 < 1.0
    * @pre       y2 > y1
    */
   public void setFigure(double x1, double x2, double y1, double y2)
   {
      validate(x1, x2, y1, y2, DisplaySizeType.NDC);
      this.figure = new double[4];
      this.figure[0] = x1;
      this.figure[1] = x2;
      this.figure[2] = y1;
      this.figure[3] = y2;
   }


   /**
    * set figure dimensions in inches <pre>
    *
    *     ----------------Device Boundary-----------------
    *     |                                              |
    *     |                                              |
    *     | --------------Outer Margin------------------ |
    *     | |                                          | |
    *     | |                                          | |
    * -   | |  -----------Figure Region--------------  | |
    * ^   | |  |                                    |  | |
    * |   | |  |                                    |  | |
    * |   | |  |   -------Plot Region-----------    |  | |
    * |   | |  |   |                           |    |  | |
    * |   | |  | A |                           |    |  | |
    * |   | |  | x |                           |    |  | |
    * |   | |  | i |                           |    |  | |
    * |   | |  | s |                           |    |  | |
    *     | |  |   |                           |    |  | |
    * y   | |  | L |                           |    |  | |
    *     | |  | a |                           |    |  | |
    * |   | |  | b |                           |    |  | |
    * |   | |  | e |                           |    |  | |
    * |   | |  | l |                           |    |  | |
    * |   | |  | s |                           |    |  | |
    * |   | |  |   |                           |    |  | |
    * |   | |  |   |                           |    |  | |
    * |   | |  |   -----------------------------    |  | |
    * |   | |  |    Axis Labels                     |  | |
    * v   | |  |                                    |  | |
    * -   | |  --------------------------------------  | |
    *     | |                                          | |
    *     | |                                          | |
    *     | O------------------------------------------- |
    *     |                                              |
    *     ------------------------------------------------
    *
    *          |<-------------- x ---------------->|
    *
    * </pre> x and y should be positive numbers and their values should fit
    * within the Outer Margin area else the behavior is unpredictable
    *
    * @param x  x width in inches
    * @param y  y width in inches
    * @pre      x > 0
    * @pre      y > 0
    */
   public void setFigure(double x, double y)
   {
      validate(x, y, DisplaySizeType.INCHES);
      this.figure = new double[2];
      this.figure[0] = x;
      this.figure[1] = y;
   }


   /**
    * set figure coordinates by means of an array <p>
    *
    * valid options
    * <ul>
    *   <li> double[4] array with values consistent with those in {@link
    *   #setFigure(double, double, double, double)}
    *   <li> double[2] array with values consistent with those in {@link
    *   #setFigure(double, double)}
    *   <li> null for resetting to default behavior
    * </ul>
    *
    *
    * @param arg  double[] of figure coordinates
    */
   public void setFigure(double[] arg)
   {
      if (arg == null)
      {
         this.figure = arg;
      }
      else if (arg.length == 4)
      {
         setFigure(arg[0], arg[1], arg[2], arg[3]);
      }
      else if (arg.length == 2)
      {
         setFigure(arg[0], arg[1]);
      }
      else
      {
         StringBuffer b = new StringBuffer(300);
         b.append(getClass().getName());
         b.append(" must have arg == null or arg.length == 2");
         b.append(" or arg.length == 4");
         throw new IllegalArgumentException(b.toString());
      }
   }


   /**
    * retrieve figure coordinates
    *
    * @return   (x1,x2,y1,y2) in NDC or (x,y) in inches
    */
   public double[] getFigure()
   {
      return (figure == null)
          ? null
          : (double[]) figure.clone();
   }


   /**
    * set outer margin coordinates <pre>
    *
    *   - ----------------Device Boundary-----------------
    *   | |                                              |
    *top| |                                              |
    *   | |                                              |
    *   - | --------------Outer Margin------------------ |
    *     | |                                          | |
    *     | |                                          | |
    *     | |  -----------Figure Region--------------  | |
    *     | |  |                                    |  | |
    *     | |  |                                    |  | |
    *     | |  |   -------Plot Region-----------    |  | |
    *     | |  |   |                           |    |  | |
    *     | |  | A |                           |    |  | |
    *     | |  | x |                           |    |  | |
    *     | |  | i |                           |    |  | |
    *     | |  | s |                           |    |  | |
    *     | |  |   |                           |    |  | |
    *     | |  | L |                           |    |  | |
    *     | |  | a |                           |    |  | |
    *     | |  | b |                           |    |  | |
    *     | |  | e |                           |    |  | |
    *     | |  | l |                           |    |  | |
    *     | |  | s |                           |    |  | |
    *     | |  |   |                           |    |  | |
    *     | |  |   |                           |    |  | |
    *     | |  |   -----------------------------    |  | |
    *     | |  |    Axis Labels                     |  | |
    *     | |  |                                    |  | |
    *     | |  --------------------------------------  | |
    *     | |                                          | |
    *     | |                                          | |
    *   - | -------------------------------------------- |
    *bot| |                                              |
    *   - ------------------------------------------------
    *
    *   ->| |<-                                      ->| |<-
    *     left                                       right
    *
    * </pre>
    *
    * @param b      bottom width
    * @param l      left with
    * @param t      top with
    * @param r      right with
    * @param units  {@link #NDC} {@link #INCHES} {@link #LINES_OF_TEXT}
    * @pre          b > 0
    * @pre          l > 0
    * @pre          t > 0
    * @pre          r > 0
    * @pre          ((units==NDC)||(units==INCHES)||(units==LINES_OF_TEXT))
    */
   public void setMarginOuter(double b, double l, double t, double r,
      int units)
   {
      String m = "DisplaySizeType.setMarginOuter(b,l,t,r,units) ";
      validateUNITS(units,
         new int[]
         {
         NDC,
         INCHES,
         LINES_OF_TEXT
         });
      if(units == INCHES || units == LINES_OF_TEXT)
      {
         validateGE(m + "b should be >= 0.0 ", b, 0.0);
         validateGE(m + "l should be >= 0.0 ", l, 0.0);
         validateGE(m + "t should be >= 0.0 ", t, 0.0);
         validateGE(m + "r should be >= 0.0 ", r, 0.0);
      }
      else if(units == NDC)
      {
         validateLT(m + "b + t should be < 1.0 ", b + t, 1.0);
         validateLT(m + "l + r should be < 1.0 ", l + r, 1.0);
      }
      this.marginOuter = new double[4];
      this.marginOuter[0] = b;
      this.marginOuter[1] = l;
      this.marginOuter[2] = t;
      this.marginOuter[3] = r;
      this.marginOuterUnits = units;
   }


   /**
    * set outer margin coordinates <p>
    *
    * elements of double[] arg must be consistent with {@link
    * #setMarginOuter(double,double,double,double,int)
    *
    * @param arg    double[4] coordinates of outer margin
    * @param units  {@link #NDC} {@link #INCHES} {@link #LINES_OF_TEXT}
    */
   public void setMarginOuter(double[] arg, int units)
   {
      if (arg == null)
      {
         this.marginOuter = arg;
      }
      else if (arg.length == 4)
      {
         setMarginOuter(arg[0], arg[1], arg[2], arg[3], units);
      }
      else
      {
         StringBuffer b = new StringBuffer(300);
         b.append(getClass().getName());
         b.append(" must have arg == null or arg.length == 4");
         throw new IllegalArgumentException(b.toString());
      }
   }


   /**
    * retrieve outer margin coordinates
    *
    * @return   double[] of outer margin coordinates
    */
   public double[] getMarginOuter()
   {
      return (marginOuter == null)
          ? null
          : (double[]) marginOuter.clone();
   }


   /**
    * retrieve the outer margin units
    *
    * @return   outer margin units
    */
   public int getMarginOuterUnits()
   {
      return marginOuterUnits;
   }


   /**
    * set plot coordinates <pre>
    *
    *     ----------------Device Boundary-----------------
    *     |                                              |
    *     |                                              |
    *     | --------------Outer Margin------------------ |
    *     | |                                          | |
    *     | |                                          | |
    * a4  | |  -----------Figure Region--------------  | |
    * ^   | |  |                                    |  | |
    * |   | |  |                                    |  | |
    * |   | |  |   -------Plot Region-----------    |  | |
    * |   | |  |   |                           |    |  | |
    * |   | |  | A |                           |    |  | |
    * |   | |  | x |                           |    |  | |
    * |   | |  | i |                           |    |  | |
    * |   | |  | s |       when using          |    |  | |
    * |   | |  |   |       FOF units           |    |  | |
    * |   | |  | L |                           |    |  | |
    * |   | |  | a |                           |    |  | |
    * |   | |  | b |                           |    |  | |
    * |   | |  | e |                           |    |  | |
    * |   | |  | l |                           |    |  | |
    * |   | |  | s |                           |    |  | |
    * |   | |  |   |                           |    |  | |
    * |   | |  |   |                           |    |  | |
    * |a3 | |  |   -----------------------------    |  | |
    * |^  | |  |    Axis Labels                     |  | |
    * ||  | |  |                                    |  | |
    * --  | |  0-------------------------------------  | |
    *     | |                                          | |
    *     | |                                          | |
    *     | -------------------------------------------- |
    *     |                                              |
    *     ------------------------------------------------
    *
    *          |-> a1
    *          |------------------------------->a2
    *
    *
    *
    *     ----------------Device Boundary-----------------
    *     |                                              |
    *     |                                              |
    *     |                                              |
    *     | --------------Outer Margin------------------ |
    *     | |                                          | |
    *     | |                                          | |
    *   - | |  -----------Figure Region--------------  | |
    *top| | |  |                                    |  | |
    *   | | |  |                                    |  | |
    *   - | |  |   -------Plot Region-----------    |  | |
    *     | |  |   |                           |    |  | |
    *     | |  | A |                           |    |  | |
    *     | |  | x |                           |    |  | |
    *     | |  | i |    when using             |    |  | |
    *     | |  | s |    INCHES or              |    |  | |
    *     | |  |   |    LINES_OF_TEXT          |    |  | |
    *     | |  | L |    units                  |    |  | |
    *     | |  | a |                           |    |  | |
    *     | |  | b |                           |    |  | |
    *     | |  | e |                           |    |  | |
    *     | |  | l |                           |    |  | |
    *     | |  | s |                           |    |  | |
    *     | |  |   |                           |    |  | |
    *     | |  |   |                           |    |  | |
    *   - | |  |   -----------------------------    |  | |
    *bot| | |  |    Axis Labels                     |  | |
    *   | | |  |                                    |  | |
    *   - | |  --------------------------------------  | |
    *     | |                                          | |
    *     | |                                          | |
    *     | -------------------------------------------- |
    *     |                                              |
    *     ------------------------------------------------
    *
    *        ->|  |<-                        ->|    |<-
    *          left                            right
    * </pre>
    * <ul>
    *   <li> when u is in {@link #FOF} the coordinates are as follows
    *   <ul>
    *     <li> a1 min x coordinate
    *     <li> a2 max x coordinate
    *     <li> a3 min y coordinate
    *     <li> a4 max y coordinate
    *   </ul>
    *
    *   <li> when u is in {@link #INCHES} the coordinates are as follows
    *   <ul>
    *     <li> a1 = bottom margin between the Plot and Figure regions
    *     <li> a2 = left margin between the Plot and Figure regions
    *     <li> a3 = top margin between the Plot and Figure regions
    *     <li> a4 = right margin between the Plot and Figure regions
    *   </ul>
    *
    *   <li> when u is in {@link #LINES_OF_TEXT} the coordinates are as follows
    *
    *   <ul>
    *     <li> a1 = bottom margin between the Plot and Figure regions
    *     <li> a2 = left margin between the Plot and Figure regions
    *     <li> a3 = top margin between the Plot and Figure regions
    *     <li> a4 = right margin between the Plot and Figure regions
    *   </ul>
    *
    * </ul>
    *
    *
    * @param a1  min x coordinate or bottom margin size depending on units
    * @param a2  max x coordinate or left margin size depending on units
    * @param a3  min y coordinate or top margin size depending on units
    * @param a4  max y coordinate or right margin size depending on units
    * @param u   {@link #FOF} {@link #INCHES} {@link #LINES_OF_TEXT}
    */
   public void setPlot(double a1, double a2, double a3, double a4, int u)
   {
      String m = "DisplaySizeType.setPlot(a1,a2,a3,a4,u) ";
      validateUNITS(u,
         new int[]
         {
         FOF,
         INCHES,
         LINES_OF_TEXT
         });
      validate(a1, a2, a3, a4, u);

      if (u == FOF)
      {
         validateLT(m + "a1 should be < a2 ", a1, a2);
         validateLT(m + "a3 should be < a4 ", a3, a4);
         validateLT(m + "a2 should be < 1.0 ", a2, 1.0);
         validateLT(m + "a4 should be < 1.0 ", a4, 1.0);
      }

      this.plot = new double[4];
      this.plot[0] = a1;
      this.plot[1] = a2;
      this.plot[2] = a3;
      this.plot[3] = a4;
      this.plotUnits = u;
   }


   /**
    * set plot dimensions in inches
    *
    * @param x  x width in inches
    * @param y  y width in inches
    */
   public void setPlot(double x, double y)
   {
      validate(x, y, DisplaySizeType.INCHES);

      this.plot = new double[2];
      this.plot[0] = x;
      this.plot[1] = y;
      this.plotUnits = DisplaySizeType.INCHES;
   }


   /**
    * set plot coordinates by means of an array <p>
    *
    * valid options
    * <ul>
    *   <li> double[4] array with values consistent with those in {@link
    *   #setPlot(double, double, double, double)}
    *   <li> double[2] array with values consistent with those in {@link
    *   #setPlot(double, double)}
    *   <li> null for resetting to default behavior
    * </ul>
    *
    *
    * @param arg    double[] of Plot coordinates
    * @param units  {@link #FOF} {@link #INCHES} {@link #LINES_OF_TEXT}
    */
   public void setPlot(double[] arg, int units)
   {
      if (arg == null)
      {
         this.plot = arg;
      }
      else if (arg.length == 4)
      {
         setPlot(arg[0], arg[1], arg[2], arg[3], units);
      }
      else if (arg.length == 2)
      {
         validateUNITS(units,
            new int[]
            {
            INCHES
            });
         setPlot(arg[0], arg[1]);
      }
      else
      {
         StringBuffer b = new StringBuffer(300);
         b.append(getClass().getName());
         b.append(" must have arg == null or arg.length == 2");
         b.append(" or arg.length == 4");
         throw new IllegalArgumentException(b.toString());
      }
   }


   /**
    * retrieve plot coordinates
    *
    * @return   (x1,x2,y1,y2) in NDC or (x,y) in inches
    */
   public double[] getPlot()
   {
      return (plot == null)
          ? null
          : (double[]) plot.clone();
   }


   /**
    * retrieve the plot units
    *
    * @return   plot units
    */
   public int getPlotUnits()
   {
      return plotUnits;
   }


   /**
    * describe object in a String
    *
    * @return   String describing object
    */
   public String toString()
   {
      return Util.toString(this);
   }


   /**
    * DOCUMENT_ME
    *
    * @param x      DOCUMENT_ME
    * @param y      DOCUMENT_ME
    * @param units  DOCUMENT_ME
    */
   private void validate(double x, double y, int units)
   {
      validateUNITS(units,
         new int[]
         {
         FOF,
         INCHES,
         LINES_OF_TEXT
         });
      validateGE(x, y);
   }


   /**
    * DOCUMENT_ME
    *
    * @param x1     DOCUMENT_ME
    * @param x2     DOCUMENT_ME
    * @param y1     DOCUMENT_ME
    * @param y2     DOCUMENT_ME
    * @param units  DOCUMENT_ME
    */
   private void validate(double x1, double x2, double y1, double y2, int units)
   {
      validateUNITS(units,
         new int[]
         {
         FOF,
         NDC,
         INCHES,
         LINES_OF_TEXT
         });
      validateGE(x1, x2, y1, y2);

      if (units == NDC)
      {
         validateGE("x1 should be >= 0.0 ", x1, 0.0);
         validateGE("x2 should be >= x1 ", x2, x1);
         validateGE("y1 should be >= 0.0 ", y1, 0.0);
         validateGE("y2 should be >= y1 ", y2, y1);
      }
      else
      {
         validateGE("x1 should be >= 0.0 ", x1, 0.0);
         validateGE("x2 should be >= 0.0 ", x2, 0.0);
         validateGE("y1 should be >= 0.0 ", y1, 0.0);
         validateGE("y2 should be >= 0.0 ", y2, 0.0);
      }
   }


   /**
    * validation method
    *
    * @param x  Description of the Parameter
    * @param y  Description of the Parameter
    */
   private void validateGE(double x, double y)
   {
      validateGE(" x must be >= 0.0; but x = " + x, x, 0.0);
      validateGE(" y must be >= 0.0; but y = " + y, y, 0.0);
   }


   /**
    * validation method
    *
    * @param x1  min x
    * @param x2  max x
    * @param y1  min y
    * @param y2  max y
    */
   private void validateGE(double x1, double x2, double y1, double y2)
   {
      validateGE(" x1 must be >= 0.0; but x1 = " + x1, x1, 0.0);
      validateGE(" x2 must be >= 0.0; but x2 = " + x2, x2, 0.0);
      validateGE(" y1 must be >= 0.0; but y1 = " + y1, y1, 0.0);
      validateGE(" y2 must be >= 0.0; but y2 = " + y2, y2, 0.0);
   }


   /**
    * DOCUMENT_ME
    *
    * @param msg  DOCUMENT_ME
    * @param d1   DOCUMENT_ME
    * @param d2   DOCUMENT_ME
    */
   private void validateLT(String msg, double d1, double d2)
   {
      if (d1 >= d2)
      {
         throw new IllegalArgumentException(msg);
      }
   }


   /**
    * DOCUMENT_ME
    *
    * @param msg  DOCUMENT_ME
    * @param d1   DOCUMENT_ME
    * @param d2   DOCUMENT_ME
    */
   private void validateGE(String msg, double d1, double d2)
   {
      if (d1 < d2)
      {
         throw new IllegalArgumentException(msg);
      }
   }


   /**
    * DOCUMENT_ME
    *
    * @param unit   DOCUMENT_ME
    * @param units  DOCUMENT_ME
    */
   private void validateUNITS(int unit, int[] units)
   {
      String msg = " Allowed units are: ";
      boolean matchFound = false;

      for (int i = 0; i < units.length; i++)
      {
         if (unit == units[i])
         {
            matchFound = true;
         }

         msg += (" " + units[i]);
      }

      if (!matchFound)
      {
         msg = msg + " unit=" + unit;
         throw new IllegalArgumentException(msg);
      }
   }
}

