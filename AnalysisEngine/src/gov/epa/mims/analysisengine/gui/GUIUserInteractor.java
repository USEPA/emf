package gov.epa.mims.analysisengine.gui;

import java.awt.Component;
import java.io.File;
import java.lang.*;
import javax.swing.*;

/**
 * Interaction with the user via a GUI.
 * 
 * @author Steve Fine
 * @version $Id: GUIUserInteractor.java,v 1.4 2007/01/09 23:06:15 parthee Exp $
 * 
 */

public class GUIUserInteractor implements UserInteractor {

	// /**
	// * Send a notification through a dialog.
	// *
	// * @author Steve Fine
	// *
	// * @param title String containing title for the notification (which some
	// * sources might not use)
	// * @param message String containing the content of the notification
	// * @param type int code for the type
	// *
	// ******************************************************************************/
	// public void notify(String title, String message, int type)
	// {
	// int messageType = JOptionPane.PLAIN_MESSAGE;
	// switch (type)
	// {
	// case UserInteractor.ERROR :
	// messageType = JOptionPane.ERROR_MESSAGE;
	// break;
	//   
	// case UserInteractor.WARNING :
	// messageType = JOptionPane.WARNING_MESSAGE;
	// break;
	//   
	// case UserInteractor.NOTE :
	// messageType = JOptionPane.INFORMATION_MESSAGE;
	// break;
	// }
	//   
	// WidthLimitedJOptionPane.showMessageDialog(null, message, title, messageType);
	// }

	/*******************************************************************************************************************
	 * Send a notification through a dialog.
	 * 
	 * @author Steve Fine
	 * 
	 * @param title
	 *            String containing title for the notification (which some sources might not use)
	 * @param message
	 *            String containing the content of the notification
	 * @param type
	 *            int code for the type
	 * 
	 ******************************************************************************************************************/
	public void notify(Component parent, String title, String message, int type) {
		int messageType = JOptionPane.PLAIN_MESSAGE;
		switch (type) {
		case UserInteractor.ERROR:
			messageType = JOptionPane.ERROR_MESSAGE;
			break;

		case UserInteractor.WARNING:
			messageType = JOptionPane.WARNING_MESSAGE;
			break;

		case UserInteractor.NOTE:
			messageType = JOptionPane.INFORMATION_MESSAGE;
			break;
		}

		WidthLimitedJOptionPane.showMessageDialog(parent, message, title, messageType);
	}

	/*******************************************************************************************************************
	 * Send a notification about an Exception through a dialog.
	 * 
	 * @author Alison Eyth
	 * 
	 * @param title
	 *            String containing title for the notification (which some sources might not use)
	 * @param exception
	 *            Throwable that occurred
	 * @param type
	 *            int code for the type
	 * 
	 ******************************************************************************************************************/
	public void notifyOfException(Component parent, String title, Throwable exception, int type) {
		exception.printStackTrace();
		notify(parent, title, ((exception instanceof Error || exception instanceof RuntimeException) ? (exception
				.getClass().getName() + ":\n\n") : "")
				+ exception.getMessage(), type);
		// gov.epa.mims.util.Frame.repaintAll(); // in some cases windows were invalid after exception was caught
	}

	/*******************************************************************************************************************
	 * Prompt for a string through this source
	 * 
	 * @author Steve Fine
	 * 
	 * @param title
	 *            String containing title for the notification (which some sources might not use)
	 * @param message
	 *            String containing the content of the notification
	 * @param initialValue
	 *            String containing initial value
	 * @return String or null, String may be empty
	 * 
	 ******************************************************************************************************************/
	public String getString(String title, String message, String initialValue) {
		return (String) JOptionPane.showInputDialog(null, message, title, JOptionPane.PLAIN_MESSAGE, null, null,
				initialValue);
	}

	/*******************************************************************************************************************
	 * Prompt the user to select an option
	 * 
	 * @author Steve Fine
	 * 
	 * @param title
	 *            String containing title for the notification (which some sources might not use)
	 * @param message
	 *            String containing the content of the notification
	 * @param type
	 *            int, one of UserInteractor.YES_NO, OK_CANCEL, YES_NO_CANCEL
	 * @pre type == YES_NO || type == OK_CANCEL || type == YES_NO_CANCEL
	 * @param defaultValue
	 *            int, one of YES, NO, OK, CANCEL, NONE. may not be used.
	 * @return one of YES, NO, OK, CANCEL, NONE
	 * @exception java.lang.IllegalArgumentException
	 *                is thrown if 'type' is invalid
	 * 
	 ******************************************************************************************************************/
	public int selectOption(Component parent, String title, String message, int type, int defaultValue) {
		int dtype = JOptionPane.YES_NO_OPTION;

		switch (type) {
		case UserInteractor.YES_NO:
			dtype = JOptionPane.YES_NO_OPTION;
			break;

		case UserInteractor.OK_CANCEL:
			dtype = JOptionPane.OK_CANCEL_OPTION;
			break;

		case UserInteractor.YES_NO_CANCEL:
			dtype = JOptionPane.YES_NO_CANCEL_OPTION;
			break;

		default:
			throw new IllegalArgumentException("Invalid argument: " + type);
		}

		int result = JOptionPane.showOptionDialog(parent, message, title, dtype, JOptionPane.PLAIN_MESSAGE, null, null,
				null);

		switch (result) {
		case JOptionPane.YES_OPTION:
			result = UserInteractor.YES;
			break;

		case JOptionPane.NO_OPTION:
			result = UserInteractor.NO;
			break;

		case JOptionPane.CANCEL_OPTION:
			result = UserInteractor.CANCEL;
			break;

		default:
			result = UserInteractor.NONE;
			break;
		}

		return result;
	}

	/*******************************************************************************************************************
	 * Prompt the user to select an option with a variable number of buttons.
	 * 
	 * @author Daniel Gatti
	 * 
	 * @param title
	 *            String containing title for the notification (which some sources might not use)
	 * @param message
	 *            String containing the content of the notification
	 * @param btnNames
	 *            String[], the names of the buttons to create.
	 * @param defaultValue
	 *            int, the 0-based index of the default button.
	 * @return The 0-based array in dex of the chosen button.
	 * 
	 ******************************************************************************************************************/
	public int selectOption(Component parent, String title, String message, String[] btnNames, int defaultValue) {
		int dtype = JOptionPane.YES_NO_CANCEL_OPTION;

		// changing the implementation makes the dialog come to the front with the
		// TRIM window instead of staying behind it
		int result = JOptionPane.showOptionDialog(parent, message, title, dtype, JOptionPane.PLAIN_MESSAGE, null,
				btnNames, btnNames[defaultValue]);
		return result;
	}

	/**
	 * Prompt the user for a new file.
	 * 
	 * @return the File or null if none
	 */
	public File specifyFile() {
		JFileChooser chooser = getFileChooser();

		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION)
			return chooser.getSelectedFile();
		else
			return null;
	}

	static private JFileChooser fileChooser = new JFileChooser();

	static public JFileChooser getFileChooser() {
		File saveDir = fileChooser.getCurrentDirectory();
		fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(saveDir);
		return fileChooser;
	}

	/*******************************************************************************************************************
	 * 
	 * static methods
	 * 
	 ******************************************************************************************************************/

	public static void main(String args[]) {
		GUIUserInteractor cs = new GUIUserInteractor();
		cs.notify(null, "Two Lines", "This long message should appear on two lines.", UserInteractor.WARNING);

		cs.notify(null, "Starting Test", "Button numbers start at 0.", UserInteractor.NOTE);

		String[] s = { "One", "Two", "Three", "Four", "Five" };
		int i = cs.selectOption(null, "Choose option", "Please press one of the buttons below.", s, 0);
		System.err.println("returned a " + i);

		String[] s2 = { "Fish", "Fowl", "Pheasant", "Venison", "Poultry", "Crustacean" };
		i = cs.selectOption(null, "Choose option", "Please press one of the buttons below.", s2, 3);
		System.err.println("returned a " + i);

		int result = cs.selectOption(null, "Test Choice", "Choose a value and check the " + "printed result.",
				YES_NO_CANCEL, NO);
		System.out.println(result == YES ? "YES" : (result == NO ? "NO" : (result == CANCEL ? "CANCEL" : "no choice")));

		result = cs.selectOption(null, "Test Choice Again", "Choose a value and check the " + "printed result.",
				YES_NO_CANCEL, NO);
		System.out.println(result == YES ? "YES" : (result == NO ? "NO" : (result == CANCEL ? "CANCEL" : "no choice")));

		System.out.println(cs.specifyFile());

		System.exit(0);
	}
}
