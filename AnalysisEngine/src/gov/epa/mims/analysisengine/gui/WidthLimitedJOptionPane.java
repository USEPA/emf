package gov.epa.mims.analysisengine.gui;

import java.awt.Component;
import javax.swing.*;

/**
 * A JOptionPane that only allows a limited number of characters
 * to appear in a line.  This is very useful when displaying
 * long messages.
 *
 * <p>
 * Only a subset of the standard constructors and
 * convenience static "show" methods have
 * been implemented.  More can be added as needed.
 *
 * <p>
 * I originally intended that the text width could be
 * specified when an instance was constructed, but
 * JOptionPane appears to call getMaxCharactersPerLineCount
 * before the instance is fully initialized, which caused
 * a width of 0 to always be used.
 *
 * @author Steve Fine
 * @version $Id: WidthLimitedJOptionPane.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 * @reviewer Karl Castleton 2001/02/13, v 1.2
 **/

public class WidthLimitedJOptionPane extends javax.swing.JOptionPane
{
   /** The width of text in characters. **/
   /**
     * @review_comment Is setting the text_width here a good idea.  Maybe a
     * MIMSProperties could be this value instead.  Just a comment though.
   **/
   private final static int TEXT_WIDTH = 80;

   /**
    * Constructor
    *
    * @param message Object containing message
    *
    **/
   public WidthLimitedJOptionPane(Object message)
   {
      super(message);
   }

   /**
    * Constructor
    *
    * @param message - the Object to display
    * @param messageType - the type of message to be displayed: ERROR_MESSAGE, INFORMATION_MESSAGE,
    *     WARNING_MESSAGE, QUESTION_MESSAGE, or PLAIN_MESSAGE
    **/
   public WidthLimitedJOptionPane(Object message, int messageType)
   {
      super(message, messageType);
   }


   /**
    * show a dialog; no choices are offered
    *
    * @param parentComponent - determines the Frame in which the dialog is displayed; if null, or if the
    *      parentComponent has no Frame, a default Frame is used
    * @param message - the Object to display
    * @param title - the title string for the dialog
    * @param messageType - the type of message to be displayed: ERROR_MESSAGE, INFORMATION_MESSAGE,
    *     WARNING_MESSAGE, QUESTION_MESSAGE, or PLAIN_MESSAGE
    * @review_concern No pre-conditions?
    **/
   public static void showMessageDialog(Component parentComponent,
                                        Object message, String title,
                                        int messageType)
   {
      WidthLimitedJOptionPane pane =
         new WidthLimitedJOptionPane(message, messageType);
      JDialog dialog = pane.createDialog(parentComponent, title);
      dialog.setVisible(true);
   }



   public int getMaxCharactersPerLineCount()
   {
      return TEXT_WIDTH;
   }


   /**
    * test routine
    * @addressed_comment Should the code below be added to a JUnit test?
    * Response (Steve Fine): This test relies on the user to evaluate
    * the results.  We have not developed or identified an approach
    * for automated testing of GUI code.
    **/
   public static void main(String[] args)
   {
      WidthLimitedJOptionPane pane1 =
         new WidthLimitedJOptionPane("this is a long message " +
                                     "that should appear on two lines");
      JDialog dialog1 = pane1.createDialog(null, "Should See Two Lines");
      dialog1.setVisible(true);

      WidthLimitedJOptionPane.showMessageDialog(null,
                                                "this is a long message " +
                                     "that should appear on two lines",
                                                "Should See Two Lines",
                                                INFORMATION_MESSAGE);

      System.exit(0);
   }


}

