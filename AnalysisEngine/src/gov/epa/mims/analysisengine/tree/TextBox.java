package gov.epa.mims.analysisengine.tree;

import java.awt.Color;

import java.io.Serializable;

import java.util.ArrayList;


/**
 *
 * describes a text box object
 *
 * @author Tommy E. Cathey
 * @version $Id: TextBox.java,v 1.2 2005/09/19 14:50:10 rhavaldar Exp $
 *
 * <p>Elided Code Example:
 * <pre>
 *    :
 *    :
 * TextBox textBox = new TextBox();
 *    :
 *    :
 * TextBoxArrow arrow1 = new TextBoxArrow();
 * TextBoxArrow arrow2 = new TextBoxArrow();
 * 
 * arrow1.setLength(0.25);
 * arrow1.setColor(java.awt.Color.blue);
 * arrow1.setEnable(true);
 * arrow1.setPosition(3.9845, 3.6618,TextBoxArrow.USER_UNITS);
 * arrow1.setBoxContactPt(TextBoxArrow.WEST);
 * arrow1.setAngle(30.0);
 * arrow1.setCode(1);
 * arrow1.setLty(TextBoxArrow.SOLID);
 * arrow1.setWidth(3.0);
 * arrow1.setBackoff(1.0);
 * 
 * arrow2.setLength(0.75);
 * arrow2.setColor(java.awt.Color.red);
 * arrow2.setEnable(true); 
 * arrow2.setPosition(0.6945,4.9186,TextBoxArrow.USER_UNITS);
 * arrow2.setBoxContactPt(TextBoxArrow.WEST);
 * arrow2.setAngle(8.0);
 * arrow2.setCode(3);
 * arrow2.setLty(TextBoxArrow.SOLID);
 * arrow2.setWidth(1.0);
 * arrow2.setBackoff(0.0);
 * 
 * textBox.addArrow(arrow1);
 * textBox.addArrow(arrow2);
 *
 *
 * TextBoxesType textBoxesType = new TextBoxesType();
 * textBoxesType.addTextBox( textBox );
 *    :
 *    :
 * AnalysisOptions options = new AnalysisOptions();
 * options.addOption(TEXT_BOXES,textBoxesType);
 *
 * <br><A HREF="doc-files/ExampleTextBoxArrow01.html"><B>View Example</B></A>
 * <br><A HREF="doc-files/ExampleTextBoxArrow02.html"><B>View Example</B></A>
 * <br><A HREF="doc-files/ExampleTextBoxArrow03.html"><B>View Example</B></A>
 * <br><A HREF="doc-files/ExampleTextBoxArrow04.html"><B>View Example</B></A>
 * <br><A HREF="doc-files/ExampleTextBoxArrow05.html"><B>View Example</B></A>
 * <br><A HREF="doc-files/ExampleTextBoxArrow06.html"><B>View Example</B></A>
 * <br><A HREF="doc-files/ExampleTextBox01.html"><B>View Example</B></A>
 * <br><A HREF="doc-files/ExampleTextBox02.html"><B>View Example</B></A>
 * <br><A HREF="doc-files/ExampleTextBox03.html"><B>View Example</B></A>
 * <br><A HREF="doc-files/ExampleTextBox04.html"><B>View Example</B></A>
 * <br><A HREF="doc-files/ExampleTextBox05.html"><B>View Example</B></A>
 * <br><A HREF="doc-files/ExampleTextBox06.html"><B>View Example</B></A>
 * <br><A HREF="doc-files/ExampleTextBox07.html"><B>View Example</B></A>
 * <br><A HREF="doc-files/ExampleTextBox08.html"><B>View Example</B></A>
 * <br><A HREF="doc-files/ExampleTextBox09.html"><B>View Example</B></A>
 * <br><A HREF="doc-files/ExampleTextBox10.html"><B>View Example</B></A>
 * <br><A HREF="doc-files/ExampleTextBox11.html"><B>View Example</B></A>
 * <br><A HREF="doc-files/ExampleTextBox12.html"><B>View Example</B></A>
 * <br><A HREF="doc-files/ExampleTextBox13.html"><B>View Example</B></A>
 * <br><A HREF="doc-files/ExampleTextBox14.html"><B>View Example</B></A>
 * <br><A HREF="doc-files/ExampleTextBox15.html"><B>View Example</B></A>
 * <br><A HREF="doc-files/ExampleTextBox16.html"><B>View Example</B></A>
 * <br><A HREF="doc-files/ExampleTextBox17.html"><B>View Example</B></A>
 * <br><A HREF="doc-files/ExampleTextBox18.html"><B>View Example</B></A>
 * <br><A HREF="doc-files/ExampleTextBox19.html"><B>View Example</B></A>
 * <br><A HREF="doc-files/ExampleTextBox20.html"><B>View Example</B></A>
 * <br><A HREF="doc-files/ExampleTextBox21.html"><B>View Example</B></A>
 **/
public class TextBox
   extends TextBorder
   implements Serializable,
              TextBoxConstantsIfc,
              UnitsConstantsIfc,
              Cloneable
{
   /** serial version UID */
   static final long serialVersionUID = 1;

   /** max number of characters before text is wrapped;
    * wrapping is done on word boundaries */
   private int wrap = 25;

   /** number of characters to indent the first line */
   private int indent = 0;

   /** number of characters to indent the lines following the first line */
   private int exdent = 0;

   /** vertical spacing between wrapped text lines in % or char height */
   private double vSpace = 0.2;

   /** clipping */
   private int xpd = DEVICE;

   /** arrows to draw */
   private ArrayList arrows = new ArrayList();

   /** width of the TextBox */
   private double boxWidth = 0.2; //Double.NaN;

   /** height of the TextBox */
   private double boxHeight = 0.1; //Double.NaN;

   /** TextBox width-height type;
     * determines how {@link #boxWidth} and {@link #boxHeight}
     * are used
     * <p>
     * <br>
     * <ul>
     * <li>{@link #MAX_WIDTH} the TextBox is variable and is sized
     * fit the String {@link Text#setTextString(java.lang.String)}
     * <li>{@link #ABSOLUTE_WIDTH} the size of the TextBox is fixed
     * </ul>
     */
   private int boxWHtype = ABSOLUTE_WIDTH;

   /** units of {@link #boxWidth} and {@link #boxHeight}
     * <p>
     * <br>
     * <ul>Valid Values
     * <li>{@link #USER_UNITS}
     * <li>{@link #FIGURE_UNITS}
     * <li>{@link #DEVICE_UNITS}
     * </ul>
     */
   private int unitsWH = FIGURE_UNITS;

   /** rotation of Text should always be 0.0 for the TextBox */
   private double degreesRotation = 0.0;

   /** text X justification within the TextBox */
   private double textXjustification = 0.5;

   /** text Y justification within the TextBox */
   private double textYjustification = 0.5;

   /** compass positioning of the TextBox within the {@link #region} */
   private java.lang.String position = CENTER;

   /** region to draw the TextBox */
   private java.lang.String region = PLOT_REGION;

   /** X location of the TextBox */
   private double x;

   /** X justification of the TextBox */
   private double xJustification = 0.5;

   /** Y location of the TextBox */
   private double y;

   /** Y justification of the TextBox */
   private double yJustification = 0.5;

   /** units of {@link #x} and {@link #y} */
   private int unitsXY = USER_UNITS;

   /**
    * retrieve the compass position setting for this TextBox
    * <p>
    * <ul>the return value will be one of the constants below:
    * <li>{@link #NORTHWEST} 
    * <li>{@link #NORTH} 
    * <li>{@link #NORTHEAST} 
    * <li>{@link #WEST} 
    * <li>{@link #CENTER} 
    * <li>{@link #EAST} 
    * <li>{@link #SOUTHWEST} 
    * <li>{@link #SOUTH} 
    * <li>{@link #SOUTHEAST} 
    * </ul>
    * @return compass position setting for this TextBox
    ********************************************************/
   public java.lang.String getPosition()
   {
      return position;
   }

   /**
    * retrieve the region setting for this TextBox
    * <p>
    * <ul>the return value will be one of the constants:
    * <li>{@link #RIGHT_HAND_MARGIN} 
    * <li>{@link #LEFT_HAND_MARGIN}
    * <li>{@link #TOP_HAND_MARGIN}
    * <li>{@link #TOP_HAND_MARGIN}
    * <li>{@link #BOTTOM_HAND_MARGIN}
    * <li>{@link #BOTTOM_HAND_MARGIN}
    * <li>{@link #PLOT_REGION} 
    * </ul>
    *
    * @return region setting for this TextBox
    ********************************************************/
   public java.lang.String getRegion()
   {
      return region;
   }

   /**
    * retrieve the x position of the TextBox
    *
    * @return x position of the TextBox
    ********************************************************/
   public double getX()
   {
      return x;
   }

   /**
    * retrieve the x justification of the TextBox
    *
    * @return x justification of the TextBox
    ********************************************************/
   public double getXJustification()
   {
      return xJustification;
   }

   /**
    * retrieve the y position of the TextBox
    *
    * @return y position of the TextBox
    ********************************************************/
   public double getY()
   {
      return y;
   }

   /**
    * retrieve the y justification of the TextBox
    *
    * @return y justification of the TextBox
    ********************************************************/
   public double getYJustification()
   {
      return yJustification;
   }

   /**
    * retrieve the xy units
    * <p>
    * <ul>one of the following:
    * <li> {@link USER_UNITS}
    * <li> {@link FIGURE_UNITS}
    * <li> {@link DEVICE_UNITS}
    * </ul>
    * @return the xy units
    ********************************************************/
   public int getUnitsXY()
   {
      return unitsXY;
   }

   /**
    * retrieve the units of the TextBox width and height
    * <p>
    * <ul>one of the following:
    * <li> {@link USER_UNITS}
    * <li> {@link FIGURE_UNITS}
    * <li> {@link DEVICE_UNITS}
    * </ul>
    * @return the width & height units
    ********************************************************/
   public int getUnitsWH()
   {
      return unitsWH;
   }

   /**
    * set position of the TextBox
    * <p>
    * <ul>the region value should be one of the constants:
    * <li>{@link #RIGHT_HAND_MARGIN} 
    * <li>{@link #LEFT_HAND_MARGIN}
    * <li>{@link #TOP_HAND_MARGIN}
    * <li>{@link #TOP_HAND_MARGIN}
    * <li>{@link #BOTTOM_HAND_MARGIN}
    * <li>{@link #BOTTOM_HAND_MARGIN}
    * <li>{@link #PLOT_REGION} 
    * </ul>
    *
    * <p>
    * <ul>the position value will be one of the constants below:
    * <li>{@link #NORTHWEST} 
    * <li>{@link #NORTH} 
    * <li>{@link #NORTHEAST} 
    * <li>{@link #WEST} 
    * <li>{@link #CENTER} 
    * <li>{@link #EAST} 
    * <li>{@link #SOUTHWEST} 
    * <li>{@link #SOUTH} 
    * <li>{@link #SOUTHEAST} 
    * </ul>
    * <br><A HREF="doc-files/ExampleTextBox01.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleTextBox02.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleTextBox03.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleTextBox04.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleTextBox05.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleTextBox06.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleTextBox07.html"><B>View Example</B></A>
    *
    * @param region region to draw the TextBox in
    * @param position the positioning of the TextBox within the region
    * @param xJustification X justification within position
    * @param yJustification Y justification within position
    ********************************************************/
   public void setPosition(java.lang.String region, java.lang.String position, 
      double xJustification, double yJustification)
   {
      this.region = region;
      this.position = position;
      this.xJustification = xJustification;
      this.yJustification = yJustification;
   }

   /**
    * set position of the TextBox
    *
    * <p>
    * <ul>units determines the units of x&Y and should be one of the following:
    * <li> {@link USER_UNITS}
    * <li> {@link FIGURE_UNITS}
    * <li> {@link DEVICE_UNITS}
    * </ul>
    * @param x x location of TextBox
    * @param y y location of TextBox
    * @param units units of x & y
    * @param xJustification x justification of the TextBox about x
    * @param yJustification y justification of the TextBox about y
    ********************************************************/
   public void setPosition(double x, double y, int units, 
      double xJustification, double yJustification)
   {
      this.x = x;
      this.y = y;
      this.unitsXY = units;
      this.xJustification = xJustification;
      this.yJustification = yJustification;
      this.region = null;
      this.position = null;
   }

   /**
    * retrieve the text x justification
    *
    * @return the text x justification
    ********************************************************/
   public double getTextXjustification()
   {
      return textXjustification;
   }

   /**
    * retrieve the text y justification
    *
    * @return the text y justification
    ********************************************************/
   public double getTextYjustification()
   {
      return textYjustification;
   }

   /**
    * set the Text justification within the TextBox
    * <p>
    * <ul>for x:
    * <li> 0.0 - left justified
    * <li> 0.5 - center justified
    * <li> 1.0 - right justified
    * </ul>
    * <p>
    * <ul>for y:
    * <li> 0.0 - bottom justified
    * <li> 0.5 - center justified
    * <li> 1.0 - top justified
    * </ul>
    * <br><A HREF="doc-files/ExampleTextBox08.html"><B>View Example</B></A>
    *
    * @param x horizontal text justification
    * @param y vertical text justification
    ********************************************************/
   public void setTextJustification(double x, double y)
   {
      this.textXjustification = x;
      this.textYjustification = y;
   }

   /**
    * retrieve the TextBox rotation
    *
    * @return degrees of TextBox rotation
    ********************************************************/
   public double getTextDegreesRotation()
   {
      return degreesRotation;
   }

   /**
    * set the TextBox rotation
    * <p>
    * the only valid value is 0.0; other values are ignored
    *
    * @param arg degrees of TextBox rotation
    ********************************************************/
   public void setTextDegreesRotation(double arg)
   {
      //overrides setTextDegreesRotation( double ) from super class
      //don't allow rotation
      this.degreesRotation = 0.0;
   }

   //   /**
   //    * retrieve the type face
   //    *
   //    * @return degrees of TextBox rotation
   //    ********************************************************/
   //   public String getTypeface()
   //   {
   //      return typeface;
   //   }
   //
   //   /**
   //    * set the TextBox rotation
   //    * <p>
   //    * the only valid value is 0.0; other values are ignored
   //    *
   //    * @param arg degrees of TextBox rotation
   //    ********************************************************/
   //   public void setTypeface(String arg)
   //   {
   //      //overrides setTypeFace() from super class
   //      super.typeface = null;
   //   }

   /**
    * set the TextBox width and height
    * <p>
    * <ul>units determines the units of w&h and should be one of the following:
    * <li> {@link USER_UNITS}
    * <li> {@link FIGURE_UNITS}
    * <li> {@link DEVICE_UNITS}
    * </ul>
    *
    * <p>
    * <ul>type determines how the TextBox size is determined
    * <li> {@link ABSOLUTE_WIDTH} - w&h determine the size of the box
    * <li> {@link MAX_WIDTH} - the size of the box grows with the text size and
    * the text wrapping until the values of w&h are reached. If the box is too
    * small for the text, then the text size is
    * decreased until it can fit within w&h
    * </ul>
    * <br><A HREF="doc-files/ExampleTextBox09.html"><B>View Example</B></A>
    * <br><A HREF="doc-files/ExampleTextBox10.html"><B>View Example</B></A>
    * @param w width
    * @param h height
    * @param units units of w & h
    * @param type type of sizing method
    ********************************************************/
   public void setBoxWidthAndHeight(double w, double h, int units, int type)
   {
      this.boxWidth = w;
      this.boxHeight = h;
      this.unitsWH = units;
      this.boxWHtype = type;
   }

   /**
    * retrieve the TextBox width and height Type
    *
    * @return the TextBox width and height Type
    ********************************************************/
   public int getBoxWHtype()
   {
      return this.boxWHtype;
   }

   /**
    * retrieve the TextBox height
    *
    * @return the TextBox height
    ********************************************************/
   public double getBoxHeight()
   {
      return boxHeight;
   }

   /**
    * retrieve the TextBox width
    *
    * @return the TextBox width
    ********************************************************/
   public double getBoxWidth()
   {
      return boxWidth;
   }

   /**
    * retrieve the text wrap
    *
    * @return the number of characters to wrap the text to
    ********************************************************/
   public int getWrap()
   {
      return wrap;
   }

   /**
    * retieve the number of characters to indent the first line of text
    *
    * @return the number of characters to indent the first line of text
    ********************************************************/
   public int getIndent()
   {
      return indent;
   }

   /**
    * retieve the number of characters to indent all lines after the first line of text
    *
    * @return the number of characters to indent all lines after the first line of text
    ********************************************************/
   public int getExdent()
   {
      return exdent;
   }

   /**
    * set the text wrapping and indentation
    * <p>
    * <ul>
    * <li> wrap - the number of characters to wrap the text to
    * <li> indent - the number of characters to indent the first line of text
    * <li> exdent - the number of characters to indent all lines after the first line of text
    * </ul>
    *
    * <br><A HREF="doc-files/ExampleTextBox11.html"><B>View Example</B></A>
    * @param wrap the number of characters to wrap the text to
    * @param indent the number of characters to indent the first line of text
    * @param exdent the number of characters to indent all lines after the first line of text
    ********************************************************/
   public void setWrap(int wrap, int indent, int exdent)
   {
      this.wrap = wrap;
      this.indent = indent;
      this.exdent = exdent;
   }

   /**
    * retrieve the line spacing in terms of % of character height
    *
    * @return the line spacing in terms of % of character height
    ********************************************************/
   public double getVSpace()
   {
      return vSpace;
   }

   /**
    * set the line spacing in terms of % of character height
    * <br><A HREF="doc-files/ExampleTextBox12.html"><B>View Example</B></A>
    *
    * @param arg the line spacing in terms of % of character height
    ********************************************************/
   public void setVSpace(double arg)
   {
      this.vSpace = arg;
   }

   /**
    * retrieve the TextBox clipping parameter
    * <p>
    * <ul>should be one of the following:
    * <li>{@link FIGURE}
    * <li>{@link PLOT}
    * <li>{@link DEVICE}
    * </ul>
    *
    * @return TextBox clipping parameter
    ********************************************************/
   public int getXpd()
   {
      return xpd;
   }

   /**
    * set the TextBox clipping parameter
    * <p>
    * <ul>should be one of the following:
    * <li>{@link FIGURE}
    * <li>{@link PLOT}
    * <li>{@link DEVICE}
    * </ul>
    * <br><A HREF="doc-files/ExampleTextBox13.html"><B>View Example</B></A>
    *
    * @param arg TextBox clipping parameter
    ********************************************************/
   public void setXpd(int arg)
   {
      this.xpd = arg;
   }

   /**
    * add a TextBoxArrow to the TextBox
    *
    * @param arrow a TextBoxArrow
    * @see TextBoxArrow
    ********************************************************/
   public void addArrow(TextBoxArrow arrow)
   {
      arrows.add(arrow);
   }

   /**
    * retrieve the TextBoxArrow at index i
    *
    * @param i index
    * @return TextBoxArrow at index i
    ********************************************************/
   public TextBoxArrow getArrow(int i)
   {
      return (TextBoxArrow) arrows.get(i);
   }

   /**
    * remove the TextBoxArrow at index i
    *
    * @param i index of TextBoxArrow to remove
    ********************************************************/
   public void removeArrow(int i)
   {
      arrows.remove(i);
   }

   /**
    * remove all the TextBoxArrows
    ********************************************************/
   public void clearArrows()
   {
      arrows.clear();
   }

   /**
    * retrieve the number of TextBoxArrows
    *
    * @return number of TextBoxArrows
    ********************************************************/
   public int getNumArrows()
   {
      return arrows.size();
   }

   /**
    * Creates and returns a copy of this object
    *
    * @return a copy of this object
    ******************************************************/
   public Object clone()
   {
      TextBox clone = (TextBox) super.clone();

      if (clone != null)
      {
         if (arrows != null)
         {
            clone.arrows = (ArrayList) arrows.clone();
         }
      }

      return clone;
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
         TextBox other = (TextBox) o;

         rtrn = (Util.equals(wrap, other.wrap))
                && (Util.equals(indent, other.indent))
                && (Util.equals(exdent, other.exdent))
                && (Util.equals(vSpace, other.vSpace))
                && (Util.equals(xpd, other.xpd))
                && (Util.equals(arrows, other.arrows))
                && (Util.equals(boxWidth, other.boxWidth))
                && (Util.equals(boxHeight, other.boxHeight))
                && (Util.equals(boxWHtype, other.boxWHtype))
                && (Util.equals(unitsWH, other.unitsWH))
                && (Util.equals(degreesRotation, other.degreesRotation))
                && (Util.equals(textXjustification, other.textXjustification))
                && (Util.equals(textYjustification, other.textYjustification))
                && (Util.equals(position, other.position))
                && (Util.equals(region, other.region))
                && (Util.equals(x, other.x)) && (Util.equals(y, other.y))
                && (Util.equals(xJustification, other.xJustification))
                && (Util.equals(yJustification, other.yJustification))
                && (Util.equals(unitsXY, other.unitsXY));
      }

      return rtrn;
   }

   /**
    * print to string in a pretty way
    *
    * @param separator separator to use (might be "\n")
    *
    * @return string of TextBox
    ********************************************************/
   public String toStringPretty(String separator)
   {
      StringBuffer b = new StringBuffer();

      if (region != null)
      {
         b.append("setPosition(" + region + "," + position + ","
                  + xJustification + "," + yJustification + ")");
      }
      else
      {
         b.append("setPosition(" + x + "," + y + "," + unitsXY + ","
                  + xJustification + "," + yJustification + ")");
      }

      b.append(separator);
      b.append("setTextJustification(" + textXjustification + ","
               + textYjustification + ")");
      b.append(separator);
      b.append("setTextDegreesRotation(" + degreesRotation + ")");
      b.append(separator);
      b.append("setBoxWidthAndHeight(" + boxWidth + "," + boxHeight + ","
               + unitsWH + "," + boxWHtype + ")");
      b.append(separator);
      b.append("setWrap(" + wrap + "," + indent + "," + exdent + ")");
      b.append(separator);
      b.append("setVSpace(" + vSpace + ")");
      b.append(separator);
      b.append("setXpd(" + xpd + ")");
      b.append(separator);

      b.append(super.toStringPretty(separator));

      return b.toString();
   }
}