package gov.epa.mims.analysisengine.gui;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;

/*
 * Although Java 1.4 has a spinner, we need one that works in Java 1.3
 * This class uses a JScrollBar and a DecimalField to provide the same
 * functionality.
 * @author Daniel Gatti
 * @version $Id: NumericSpinner.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 */
public class NumericSpinner extends javax.swing.JPanel implements ActionListener
{
   /** The formatted text field that displays the number. */
   protected DecimalField decimalField = null;

   /** the label for this widget **/
   protected JLabel spinnerLabel = null;

   /** The scorll bar that provides the increment/decrement message. */
   protected JScrollBar scrollBar = new JScrollBar(Adjustable.VERTICAL);

   /** The underlying data model for the scroll bar. */
   protected BoundedRangeModel model = null;

   /** The amount by which we should increment or decrement. */
   protected double increment = 0.0;

   /** The last value provided by the model. */
   protected double oldModelValue = 0.0;

   /** The format to use when displaying numbers. */
   protected DecimalFormat format = null;

   /** The listeners that should be notified when the value changes. */
   protected ActionListener[] listeners = null;

   /** The minimum value that the test field can take. */
   protected double minimum = 0.0;

   /** The maximum value that the test field can take. */
   protected double maximum = 0.0;

   /**
    * Constructor.
    *
    * @param label the label for this widget
    * @param value double that is the starting value to place in the text field.
    * @param format DecimalFormat that is the format to use to display the numbers.
    * @param increment double that is the increment or decrement applied each time
    *    that a button is pressed.
    * @param minimum double that is the lowest value that the text field can take.
    * @param maximum double that is the highest value that the text field can take.
    */
   public NumericSpinner(String label, double value, DecimalFormat format,
                         double increment, double minimum, double maximum)
   {
      this.increment = increment;
      this.format    = format;
      this.minimum   = minimum;
      this.maximum   = maximum;

      spinnerLabel = new JLabel(label);

      decimalField = new DecimalField(value, 10, format);
      decimalField.addActionListener(this);

      scrollBar.setMinimum((int)(value - increment * 1E6));
      scrollBar.setMaximum((int)(value + increment * 1E6));

      // Add a listener to receive increment/decrement events from the model.
      model = scrollBar.getModel();
      model.addChangeListener(
         new ChangeListener()
         {
            public void stateChanged(ChangeEvent e)
            {
               double currentValue = model.getValue();
               adjustValue(oldModelValue > currentValue);
               oldModelValue = currentValue;
            }
         }
      );

      //setLayout(new BorderLayout());
      setLayout(new GridLayout(1, 2));
      //add(spinnerLabel, BorderLayout.WEST);
      JPanel spinnerPanel = new JPanel(new GridLayout(1,2));
      add(spinnerLabel);
      //add(decimalField, BorderLayout.CENTER);
      spinnerPanel.add(decimalField);
      //add(scrollBar, BorderLayout.EAST);
      spinnerPanel.add(scrollBar);
      add(spinnerPanel);
      // Fix the size for now.
      Dimension dim = new Dimension(200, 25);
      setMinimumSize(dim);
      setPreferredSize(dim);
      setMaximumSize(dim);
   } // NumericSpinner()


   /**
    * Called when the value in the text field changes.
    */
    public void actionPerformed(ActionEvent e)
    {
       fireActionPerformed(e);
    } // actionPerformed()


   /**
    * Add an ActionListener to the list of listeners.
    * @param newListener ActionListener to add to the list.
    */
   public void addActionListener(ActionListener newListener)
   {
      if (listeners == null)
      {
         listeners = new ActionListener[1];
      }
      else
      {
         ActionListener[] tmp = new ActionListener[listeners.length];
         System.arraycopy(listeners, 0, tmp, 0, listeners.length);
         listeners = tmp;
      }

      listeners[listeners.length - 1] = newListener;
   } // addActionListener()

   /**
    * enable or disable the spinner.. if you do a setEnabled() on a container
    *  it does not enable or disable its components.. You need to do it explicitly
    *
    * @param enable
    */
   public void setEnabled(boolean enable)
   {
      spinnerLabel.setEnabled(enable);
      decimalField.setEditable(enable);
      decimalField.setEnabled(enable);
      scrollBar.setEnabled(enable);
   }

   /**
    * Increment or derement the current value in the text field depending on
    * the argument.
    * @param up boolean that is true if we should increment the value.
    */
   protected void adjustValue(boolean up)
   {
      double d = decimalField.getValue();

      if(up)
      {
         d += increment;
         // Don't exceed the maximum.
         if (d > maximum)
         {
            d -= increment;
         }
      }
      else
      {
         d -= increment;
         // Don't exceed the minimum.
         if (d < minimum)
         {
            d += increment;
         }
      }

      decimalField.setValue(d);
      fireActionPerformed(new ActionEvent(decimalField, 0, ""));
   } // adjustValue()


   /**
    * Notify the listeners that the text field value has changed.
    */
   protected void fireActionPerformed(ActionEvent e)
   {
      if (listeners != null)
      {
         for (int i = 0; i < listeners.length; i++)
         {
            listeners[i].actionPerformed(e);
         }
      }
   } // fireActionPerformed()


   /**
    * Get the value in the text field.
    * @return double that is the value in the text field.
    */
   public double getValue()
   {
      return decimalField.getValue();
   }


   /**
    * Remove the given ActionListener.
    * @param oldListener ActionListener to remove from the list.
    */
   public void removeActionListener(ActionListener oldListener)
   {
      if (listeners == null)
         return;

      int numFound = 0;
      for (int i = 0; i < listeners.length; i++)
      {
         if (listeners[i].equals(oldListener))
         {
            listeners[i] = null;
            numFound++;
         }
      }

      if (numFound == listeners.length)
      {
         listeners = null;
      } // if (numFound == listeners.length)
      else
      {
         ActionListener[] tmp = new ActionListener[listeners.length - numFound];
         int j = 0;
         for (int i = 0; i < listeners.length; i++)
         {
            if (listeners[i] != null)
            {
               tmp[j] = listeners[i];
               j++;
            }
         } // for(i)
         listeners = tmp;
      } // else
   } // removeActionListener()


   /**
    * Set the value of the spinner. This must be withing the min and max or
    * no action will be taken.
    * @param newValue double that is the new value to set.
    */
   public void setValue(double newValue)
   {
      if (newValue >= minimum && newValue <= maximum)
      {
         decimalField.setValue(newValue);
      }
   } // setValue()


   /**
    * main() for testing.
    */
   public static void main(String[] args)
   {
      JFrame f = new JFrame();
      DecimalFormat df = new DecimalFormat("0.00");
      f.getContentPane().add(new NumericSpinner("test", 0.0, df, 1.0, 0.0, 10.0));
      f.pack();
      f.setVisible(true);
   } // main()
} // class NumericSpinner
