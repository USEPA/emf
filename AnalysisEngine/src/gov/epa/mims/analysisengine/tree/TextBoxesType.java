package gov.epa.mims.analysisengine.tree;

import java.awt.Color;

import java.io.Serializable;

import java.util.ArrayList;


/**
 *
 * @author Tommy E. Cathey
 * @version $Id: TextBoxesType.java,v 1.2 2005/09/19 14:50:10 rhavaldar Exp $
 *
 * An AnlysisOption for drawing TextBoxes
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
 * <br><A HREF="doc-files/ExampleTextBox16.html"><B>View Example</B></A>
 * <br><A HREF="doc-files/ExampleTextBox18.html"><B>View Example</B></A>
 * <br><A HREF="doc-files/ExampleTextBox19.html"><B>View Example</B></A>
 * <br><A HREF="doc-files/ExampleTextBox20.html"><B>View Example</B></A>
 * <br><A HREF="doc-files/ExampleTextBox21.html"><B>View Example</B></A>
 **/
public class TextBoxesType
   extends AnalysisOption
   implements Serializable,
              Cloneable
{
   /** serial version UID */
   static final long serialVersionUID = 1;

   /** list of TextBox */
   private ArrayList textBoxes = new ArrayList();

   /**
    * add a {@link TextBox}
    *
    * @param textBox TextBox to add
    ********************************************************/
   public void addTextBox(TextBox textBox)
   {
      textBoxes.add(textBox);
   }

   /**
    * retrieve the TextBox at index i
    *
    * @param i index
    *
    * @return TextBox at index i
    ********************************************************/
   public TextBox getTextBox(int i)
   {
      return (TextBox) textBoxes.get(i);
   }

   /**
    * remove the TextBox at index i
    *
    * @param i index
    ********************************************************/
   public void removeTextBox(int i)
   {
      textBoxes.remove(i);
   }

   /**
    * remove all the TextBoxes
    ********************************************************/
   public void clearTextBox()
   {
      textBoxes.clear();
   }

   /**
    * retrieve the number of TextBoxes
    *
    * @return the number of TextBoxes
    ********************************************************/
   public int getNumTextBoxes()
   {
      return textBoxes.size();
   }

   /**
    * Creates and returns a copy of this object
    *
    * @return a copy of this object
    * @throws CloneNotSupportedException is not cloneable
    ******************************************************/
   public Object clone()
                throws CloneNotSupportedException
   {
      TextBoxesType clone = (TextBoxesType) super.clone();

      if (clone != null)
      {
         clone.textBoxes = (ArrayList) textBoxes.clone();
      }
      else
      {
         throw new CloneNotSupportedException();
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
         TextBoxesType other = (TextBoxesType) o;

         rtrn = Util.equals(textBoxes, other.textBoxes);
      }

      return rtrn;
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