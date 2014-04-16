package gov.epa.mims.analysisengine.gui;

import java.awt.Component;
import java.io.*;

/**
 * Represent a source of commands which are submitted via a Stream.
 * <p>
 * I tried to make this be based on Readers and Writers but when I created
 * a Writer based on System.out, it did not work properly (no output).
 *
 * @author Steve Fine
 * @version $Id: StreamUserInteractor.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 *
 */

public class StreamUserInteractor implements UserInteractor
{


   protected InputStream stdin_; // input for commands
   protected PrintStream stdout_;   // standard output
   protected PrintStream stderr_;   // error output


/**
 * Construct a new source.
 *
 * @author Steve Fine
 *
 * @param stdin InputStream where the commands come from
 * @param stdout PrintStream where normal communications should go
 * @param stderr PrintStream where error communications should go
 ******************************************************************************/
   public StreamUserInteractor(InputStream stdin, PrintStream stdout,
                              PrintStream stderr)
   {
      stdin_ = stdin;
      stdout_ = stdout;
      stderr_ = stderr;
   }


/**
 * Send a notification through this source.
 *
 * @author Steve Fine
 *
 * @param title String containing title for the notification (which some
 *   sources might not use)
 * @param message String containing the content of the notification
 * @param type int code for the type
 *
 ******************************************************************************/
   public void notify(Component parent,String title, String message, int type)
   {
      switch (type)
      {
      case UserInteractor.WARNING :
         stderr_.println("Warning: " + message);
         break;

      case UserInteractor.ERROR :
         stderr_.println("Error: " + message);
         break;

      case UserInteractor.NOTE :
         stdout_.println("Note: " + message);
         break;

      case UserInteractor.PLAIN :
         stdout_.println(message);

      }
   }
/**
 * Send a notification about an Exception through a dialog.
 *
 * @author Alison Eyth
 *
 * @param title String containing title for the notification (which some
 *   sources might not use)
 * @param exception Throwable that occurred
 * @param type int code for the type
 *
 ******************************************************************************/
   public void notifyOfException(Component parent, String title, Throwable exception, int type)
   {
      notify(parent, title, exception.getClass().getName()+":\n"+exception.getMessage(), type);
   }

/**
 * Prompt for a string through this source
 *
 * @author Steve Fine
 *
 * @param title String containing title for the notification (which some
 *   sources might not use)
 * @param message String containing the content of the notification
 * @param initialValue String containing initial value
 * @return String or null, String may be empty
 *
 ******************************************************************************/
   public String getString(String title, String message, String initialValue)
   {
      throw new RuntimeException("StreamUserInteractor.getString() is not supported."); // TBD
   }

/**
 * Prompt the user for a new file.
 *
 * @return the File or null if none
 */
   public File specifyFile()
   {
      stdout_.println("Enter file");
      throw new RuntimeException("StreamUserInteractor.specifyFile() is not supported."); // TBD
   }

/**
 * Prompt the user to select an option
 *
 * <p>
 * This routine is duplicated in JFrameUserInteractor.
 *
 * @author Steve Fine
 *
 * @param title String containing title for the notification (which some
 *      sources might not use)
 * @param message String containing the content of the notification
 * @param type int, one of UserInteractor.YES_NO, OK_CANCEL, YES_NO_CANCEL
 * @param defaultValue int, one of YES, NO, OK, CANCEL, NONE. may not
 *      be used.
 * @return one of YES, NO, OK, CANCEL, NONE
 *
 ******************************************************************************/
   public int selectOption(Component parent, String title, String message, int type,
                           int defaultValue)
   {
      throw new RuntimeException("StreamUserInteractor.selectOption() is not supported."); // TBD
   }

/**
 * Prompt the user to select an option with a variable number of buttons.
 *
 * @author Daniel Gatti
 *
 * @param title String containing title for the notification (which some
 *      sources might not use)
 * @param message String containing the content of the notification
 * @param btnNames String[], the names of the buttons to create.
 * @param defaultValue int, the 0-based index of the default button.
 * @return The 0-based array in dex of the chosen button.
 *
 ******************************************************************************/
   public int selectOption(Component parent, String title, String message, String[] btnNames,
                           int defaultValue)
   {
      throw new RuntimeException("StreamUserInteractor.selectOption() is not supported."); // TBD
   }

   /**
    * Run a test sequence
    *
    * @author Steve Fine
    **/
   public static void main(String[] args)
   {
      UserInteractor method = new StreamUserInteractor(System.in, System.out, System.err);

      method.notify(null,"Test Notify", "This should be a note and the following " +
             "message should be an error.", UserInteractor.NOTE);
      method.notify(null,"Test Error", "This is an error", UserInteractor.ERROR);

      Exception ex = new RuntimeException("Sample exception");
      method.notifyOfException(null,"Test Exception", ex, UserInteractor.ERROR);
   }
}

