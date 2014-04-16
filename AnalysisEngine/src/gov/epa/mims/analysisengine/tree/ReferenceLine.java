package gov.epa.mims.analysisengine.tree;

import java.awt.Color;

import java.io.Serializable;


/**
* Class to store information about a Reference Line
*
* <p>Elided Code Example:
* <pre>
*    :
*    :
* AxisLog axis = new AxisLog();
* axis.setReferenceLineAdd(ReferenceLine1());
* axis.setReferenceLineAdd(ReferenceLine2());
*    :
*    :
* private ReferenceLine ReferenceLine1()
* {
*    //ReferenceLine ref = new ReferenceLine(6.5,0,Double.POSITIVE_INFINITY);
*    ReferenceLine ref = new ReferenceLine(6.5,10,0.0);
*    ref.setEnable(true);
*    ref.setLinestyle(ReferenceLine.SOLID);
*    ref.setLinewidth(1.0);
*    ref.setLinecolor(java.awt.Color.red);
*    TextBorder txt = new TextBorder();
*
*    txt.setColor(java.awt.Color.black);
*    txt.setPosition(Text.REFERENCE_LINE, Text.CENTER, 0.75, 0.0);
*    txt.setEnable(true);
*
*    txt.setTextExpansion(1.25);
*    txt.setTextString("Reference Line 1");
*
*    txt.setBackgroundColor(java.awt.Color.yellow);
*    txt.setBorderColor(java.awt.Color.red);
*    txt.setBorderLinestyle(TextBorder.SOLID);
*    txt.setBorderLinewidth(1.0);
*    txt.setPadLeft(0.25);
*    txt.setPadRight(0.25);
*    txt.setPadTop(0.25);
*    txt.setPadBottom(0.25);
*    txt.setDrawBorder(true);
*
*    ref.setLabel(txt);
*
*    return ref;
* }
*
* private ReferenceLine ReferenceLine2()
* {
*    //ReferenceLine ref = new ReferenceLine(6.5,0,Double.POSITIVE_INFINITY);
*    ReferenceLine ref = new ReferenceLine(6.5,1000,0.0);
*    ref.setEnable(true);
*    ref.setLinestyle(ReferenceLine.SOLID);
*    ref.setLinewidth(1.0);
*    ref.setLinecolor(java.awt.Color.red);
*    TextBorder txt = new TextBorder();
*
*    txt.setColor(java.awt.Color.black);
*    txt.setPosition(Text.REFERENCE_LINE, Text.CENTER, 0.15, 0.0);
*    txt.setEnable(true);
*
*    txt.setTextExpansion(1.25);
*    txt.setTextString("Reference Line 2");
*
*    txt.setBackgroundColor(java.awt.Color.yellow);
*    txt.setBorderColor(java.awt.Color.red);
*    txt.setBorderLinestyle(TextBorder.SOLID);
*    txt.setBorderLinewidth(1.0);
*    txt.setPadLeft(0.25);
*    txt.setPadRight(0.25);
*    txt.setPadTop(0.25);
*    txt.setPadBottom(0.25);
*    txt.setDrawBorder(true);
*
*    ref.setLabel(txt);
*
*    return ref;
* }
*
*    :
*    :
*
* </pre>
*
*
* <br><A HREF="doc-files/ExampleReferenceLine01.html"><B>Example</B></A>
* <br><A HREF="doc-files/ExampleReferenceLine02.html"><B>Example</B></A>
* <br><A HREF="doc-files/ExampleReferenceLine03.html"><B>Example</B></A>
* <br><A HREF="doc-files/ExampleReferenceLine04.html"><B>Example</B></A>
* <br><A HREF="doc-files/ExampleReferenceLine05.html"><B>Example</B></A>
* <br><A HREF="doc-files/ExampleReferenceLine06.html"><B>Example</B></A>
*
* @author Tommy E. Cathey
* @version $Id: ReferenceLine.java,v 1.2 2005/09/19 14:50:10 rhavaldar Exp $
*
**/
public class ReferenceLine
   implements LineTypeConstantsIfc,
              Serializable
{
   /** color of the reference line */
   private Color linecolor;

   /** line sytle for displaying the reference line */
   private String linestyle;

   /** Text objext to use as the reference line label */
   private gov.epa.mims.analysisengine.tree.Text label;

   /** true->draw reference line false->draw reference line */
   private boolean enable = true;

   /** line width of the reference line */
   private double linewidth;

   /** slope of line in the point-slope algorithm */
   private double m = Double.NaN;

   /** (x1,y1) point to draw reference line through using point-slope
    * or two point algorithm
    */
   private double x1 = Double.NaN;

   /** draw reference line from (x1,y1) to (x2,y2) using two point algorithm */
   private double x2 = Double.NaN;

   /** (x1,y1) point to draw reference line through using point-slope
    * or two point algorithm
    */
   private double y1 = Double.NaN;

   /** draw reference line from (x1,y1) to (x2,y2) using two point algorithm */
   private double y2 = Double.NaN;

   /**
    * Creates a new ReferenceLine object using the point-slope equation.
    *
    * <p>Calculation of the Reference Line
    * <ul>
    * <li>if(m == Double.POSITIVE_INFINITY) or
    *    (m == Double.NEGATIVE_INFINITY)) then
    * a vertical reference line is drawn through x. y is ignored.
    * <li>if(m == 0) then a horizontal reference line is drawn through y.
    * x is ignored.
    * <li>Otherwise then the point-slope equation is used
    * <p><font color="#CC0000">
    * y - y1 = m(x - x1)
    * </font><p>
    * </ul>
    * <br><A HREF="doc-files/ExampleReferenceLine01.html"><B>Example</B></A>
    * <br><A HREF="doc-files/ExampleReferenceLine02.html"><B>Example</B></A>
    *
    * @param x1 user space X coordinate of reference line in a point-slope
    * line equation
    * @param y1 user space Y coordinate of reference line in a point-slope
    * line equation
    * @param m slope of reference line in a point-slope line equation
    * @pre !((x1 == Double.NaN) && (y1 == Double.NaN))
    * @pre !((x1 != Double.NaN) && (y1 != Double.NaN) && (m == Double.NaN))
    ********************************************************/
   public ReferenceLine(double x1, double y1, double m)
   {
      this.x1 = x1;
      this.y1 = y1;
      this.m = m;
   }

   /**
    * Creates a new ReferenceLine object using the two points equation
    *  for a line.
    *
    * <p>Calculation of the Reference Line
    * <p>
    * <font color="#CC0000">
    * (y - y1)/(x - x1) = (y2 - y1)/(x2 - x1)
    * </font><p>
    * <br><A HREF="doc-files/ExampleReferenceLine05.html"><B>Example</B></A>
    *
    * @param x1 user space X coordinate of first point the reference
    * line intersects
    * @param y1 user space Y coordinate of first point the reference
    * line intersects
    * @param x2 user space X coordinate of second point the reference
    * line intersects
    * @param y2 user space Y coordinate of second point the reference
    * line intersects
    * @pre ((x1 != Double.NaN) && (y1 != Double.NaN))
    * @pre ((x2 != Double.NaN) && (y2 != Double.NaN))
    ********************************************************/
   public ReferenceLine(double x1, double y1, double x2, double y2)
   {
      this.x1 = x1;
      this.y1 = y1;
      this.x2 = x2;
      this.y2 = y2;
   }

   /**
    * set the the drawing enabled flag
    * <br><A HREF="doc-files/ExampleReferenceLine01.html"><B>Example</B></A>
    * <br><A HREF="doc-files/ExampleReferenceLine02.html"><B>Example</B></A>
    * <br><A HREF="doc-files/ExampleReferenceLine03.html"><B>Example</B></A>
    * <br><A HREF="doc-files/ExampleReferenceLine04.html"><B>Example</B></A>
    * <br><A HREF="doc-files/ExampleReferenceLine05.html"><B>Example</B></A>
    * <br><A HREF="doc-files/ExampleReferenceLine06.html"><B>Example</B></A>
    *
    * @param arg the drawing enabled flag
    ********************************************************/
   public void setEnable(boolean arg)
   {
      this.enable = arg;
   }

   /**
    * retrieve the drawing enabled flag
    *
    * @return the drawing enabled flag
    ********************************************************/
   public boolean getEnable()
   {
      return enable;
   }

   /**
    * set the label Text object
    *
    * <p>positioning of the label is relative to center of the reference line
    * @param arg label Text object
    ********************************************************/
   public void setLabel(gov.epa.mims.analysisengine.tree.Text arg)
   {
      this.label = arg;
   }

   /**
    * retrieve the label Text object
    *
    * @return label Text object
    ********************************************************/
   public gov.epa.mims.analysisengine.tree.Text getLabel()
   {
      return label;
   }

   /**
    * set the reference line color
    * <br><A HREF="doc-files/ExampleReferenceLine01.html"><B>Example</B></A>
    * <br><A HREF="doc-files/ExampleReferenceLine02.html"><B>Example</B></A>
    * <br><A HREF="doc-files/ExampleReferenceLine03.html"><B>Example</B></A>
    * <br><A HREF="doc-files/ExampleReferenceLine04.html"><B>Example</B></A>
    * <br><A HREF="doc-files/ExampleReferenceLine05.html"><B>Example</B></A>
    *
    * @param arg the reference line color
    ********************************************************/
   public void setLinecolor(java.awt.Color arg)
   {
      this.linecolor = arg;
   }

   /**
    * retrieve the reference line color
    *
    * @return reference line color
    ********************************************************/
   public java.awt.Color getLinecolor()
   {
      return linecolor;
   }

   /**
    * set the line style
    * <p>Valid values are of following two types
    * <ol>
    * <li>Predefined styles
    * <ul>
    * <li>{@link ReferenceLine#SOLID}
    * <li>{@link ReferenceLine#DASHED}
    * <li>{@link ReferenceLine#DOTTED}
    * <li>{@link ReferenceLine#DOTDASH}
    * <li>{@link ReferenceLine#LONGDASH}
    * <li>{@link ReferenceLine#TWODASH}
    * <li>{@link ReferenceLine#BLANK}
    * </ul>
    * <li>User defined styles
    * <ul>
    * <li>a string of up to 8 characters
    * (from 0:9, "A":"F") may be given, giving the length of line segments
    * which are alternatively drawn and skipped.
    * </ul>
    * </ol>
    * <br><A HREF="doc-files/ExampleReferenceLine01.html"><B>Example</B></A>
    * <br><A HREF="doc-files/ExampleReferenceLine02.html"><B>Example</B></A>
    * <br><A HREF="doc-files/ExampleReferenceLine03.html"><B>Example</B></A>
    * <br><A HREF="doc-files/ExampleReferenceLine04.html"><B>Example</B></A>
    * <br><A HREF="doc-files/ExampleReferenceLine05.html"><B>Example</B></A>
    *
    * @param arg line style
    ********************************************************/
   public void setLinestyle(java.lang.String arg)
   {
      this.linestyle = arg;
   }

   /**
    * retrieve the linestyle
    *
    * @return linestyle
    ********************************************************/
   public java.lang.String getLinestyle()
   {
      return linestyle;
   }

   /**
    * set the reference line width
    * <br><A HREF="doc-files/ExampleReferenceLine01.html"><B>Example</B></A>
    * <br><A HREF="doc-files/ExampleReferenceLine02.html"><B>Example</B></A>
    * <br><A HREF="doc-files/ExampleReferenceLine03.html"><B>Example</B></A>
    * <br><A HREF="doc-files/ExampleReferenceLine04.html"><B>Example</B></A>
    * <br><A HREF="doc-files/ExampleReferenceLine05.html"><B>Example</B></A>
    *
    * @param arg reference line width
    ********************************************************/
   public void setLinewidth(double arg)
   {
      this.linewidth = arg;
   }

   /**
    * retrieve the reference line width
    *
    * @return reference line width
    ********************************************************/
   public double getLinewidth()
   {
      return linewidth;
   }

   /**
    * retrieve m
    *
    * @return m
    ********************************************************/
   public double getM()
   {
      return m;
   }

   /**
    * retrieve x1
    *
    * @return x1
    ********************************************************/
   public double getX1()
   {
      return x1;
   }

   /**
    * retrieve x2
    *
    * @return x2
    ********************************************************/
   public double getX2()
   {
      return x2;
   }

   /**
    * retrieve y1
    *
    * @return y1
    ********************************************************/
   public double getY1()
   {
      return y1;
   }

   /**
    * retrieve y2
    *
    * @return y2
    ********************************************************/
   public double getY2()
   {
      return y2;
   }
}