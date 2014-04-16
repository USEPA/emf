package gov.epa.mims.analysisengine.gui;

import java.awt.Component;
import java.io.File;

/**
 * Functions for simple interactions with the MIMS user.
 *
 * @author Steve Fine
 * @version $Id: UserInteractor.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 *
 */

public interface UserInteractor
{
  
  /**
   * Code for a plain message.
   */
  public final static int PLAIN = 0;
  
  /**
   * Code for a warning.
   */
  public final static int WARNING = 1;
  
  /**
   * Code for an error message.
   */
  public final static int ERROR = 2;
  
  /**
   * Code for a note.
   */
  public final static int NOTE = 3;
  
  //
  // codes for possible responses
  public final static int NONE = 4;
  public final static int YES = 5;	// also OK
  public final static int NO = 6;
  public final static int CANCEL = 8;
  public final static int OK_CANCEL = 9;
  public final static int YES_NO = 10;
  public final static int YES_NO_CANCEL = 11;
  
  /**
   * Send a notification through this source.
   *
   * @author Steve Fine
   *
   * @param title String containing title for the notification (which some
   *		sources might not use)
   * @param message String containing the content of the notification
   * @param type int code for the type
   *
   ******************************************************************************/
  public void notify(Component parent,String title, String message, int type);
  
  /**
   * Send a notification about an Exception through this source.
   *
   * @author Alison Eyth
   *
   * @param title String containing title for the notification (which some
   *		sources might not use)
   * @param exception Throwable that occurred
   * @param type int code for the type
   *
   ******************************************************************************/
  public void notifyOfException(Component parent, String title, Throwable exception, int type);
  
  
  /**
   * Prompt for a string through this source
   *
   * @author Steve Fine
   *
   * @param title String containing title for the notification (which some
   *		sources might not use)
   * @param message String containing the content of the notification
   * @param initialValue String containing initial value
   * @return String or null, String may be empty
   *
   ******************************************************************************/
  public String getString(String title, String message, String initialValue);
  
  
  /**
   * Prompt the user to select an option
   *
   * @author Steve Fine
   *
   * @param title String containing title for the notification (which some
   *		sources might not use)
   * @param message String containing the content of the notification
   * @param type int, one of YES_NO, OK_CANCEL, YES_NO_CANCEL
   * @param defaultValue int, one of YES, NO, OK, CANCEL, NONE. may not
   *		be used.
   * @return One of YES, NO, OK, CANCEL, NONE
   *
   ******************************************************************************/
  public int selectOption(Component parent,String title, String message, int type,
  	int defaultValue);
  
  /**
   * Prompt the user to select an option with a variable number of buttons.
   *
   * @author Daniel Gatti
   *
   * @param title String containing title for the notification (which some
   *		sources might not use)
   * @param message String containing the content of the notification
   * @param btnNames String[], the names of the buttons to create.
   * @param defaultValue int, the 0-based index of the default button.
   * @return The 0-based array in dex of the chosen button.
   *
   ******************************************************************************/
  public int selectOption(Component parent,String title, String message, String[] btnNames,
  	int defaultValue);
  
  /**
   * Prompt the user for a new file.
   *
   * @return the File or null if none
   */
  public File specifyFile();
  
}
