package gov.epa.mims.analysisengine.gui;

import java.awt.Point;
import java.awt.Dimension;
import java.awt.event.*;
import javax.swing.*;

/**
 * A dialog for presenting options. Each type of option stores data in a data object, and that info is presented in the
 * GUI. Derived classes should set the result appropriately when they are closed to OK_RESULT or CANCEL_RESULT. The info
 * in getInfoString will be shown in the OptionsTable.
 * 
 * @see gov.epa.mims.analysisengine.gui.OptionsTable
 * @author Alison Eyth, Prashant Pai, CEP UNC
 * @version $Id: OptionDialog.java,v 1.3 2007/01/09 23:06:15 parthee Exp $
 */
public abstract class OptionDialog extends MDialog {

	/** desired minimum margin from edge of screen for new frames * */
	private static int MARGIN = 50;

	/** offset between frames * */
	private static int OFFSET = 30;

	/** the result of the dialog is cancel */
	public static int CANCEL_RESULT = 0;

	/** the result of the dialog is OK */
	public static int OK_RESULT = 1;

	/** last location used for frame * */
	static private Point lastLocation = new Point(MARGIN - OFFSET, MARGIN - OFFSET);

	/** the data object that will be reflected in the GUI */
	Object dataSource = null;

	/** the name of the option that this dialog is editing */
	String optionName = "";

	private String titleText = "";

	/** the options table that this dialog belongs to */
	private OptionsTable optionsTable = null;

	/** the result of the dialog */
	private int result = CANCEL_RESULT;

	/**
	 * If true, then do not continue to close the GUI becuase there is an error that the user needs to correct.
	 */
	protected boolean shouldContinueClosing = true;

	/**
	 * The type of plot that is being edited. This is used to customize the GUI's for each plot. Use the plot constants
	 * in AnalysisEngineConstants.
	 */
	protected String plotTypeName = null;

	public OptionDialog() {
		DefaultUserInteractor.set(new GUIUserInteractor());
		// GUI.center(this);
		// DMG - Removed to allow custom positioning.
		/*
		 * addWindowListener( new WindowAdapter() { public void windowOpened(WindowEvent event) { setOffsetLocation(); } } );
		 */
		shouldContinueClosing = true;
	}

	public OptionDialog(JFrame parent) {
		super(parent);
		DefaultUserInteractor.set(new GUIUserInteractor());
		// GUI.center(this);
		shouldContinueClosing = true;
	}

	public OptionDialog(JDialog parent) {
		super(parent);
		DefaultUserInteractor.set(new GUIUserInteractor());
		// GUI.center(this);
		shouldContinueClosing = true;
	}

	public void setDataSource(Object dataSource, String optionName) {
		this.dataSource = dataSource;
		this.optionName = optionName;
		if (optionName.length() > 0) {
			super.setTitle(titleText + " (" + optionName + ")");
		}
	}

	/**
	 * sets the size and location of window frame allowing for offset
	 */
	public void setOffsetLocation() {
		lastLocation.translate(OFFSET, OFFSET);
		Dimension frameSize = getSize();
		Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();

		//
		// set the horizontal position
		if (frameSize.width + 2 * MARGIN < screenSize.width)
		//
		// the window is narrow enough to fit in the margins
		{
			if (lastLocation.x + frameSize.width > screenSize.width)
			//
			// put the frame at the left side of the screen
			{
				lastLocation.x = MARGIN;
			}
		} else {
			lastLocation.x = getLocation().x;
		}
		//
		// set the vertical position
		if (frameSize.height + 2 * MARGIN < screenSize.height)
		//
		// the window is short enough to fit in the margins
		{
			if (lastLocation.y + frameSize.height > screenSize.height)
			//
			// put the frame near the top of the screen
			{
				lastLocation.y = MARGIN;
			}
		} else {
			lastLocation.y = getLocation().y;
		}
		setLocation(lastLocation);
	}

	/**
	 * Set the type of plot that this dialog is reprenting. This should be one of the plot constant from
	 * AnalysisEngineConstants.
	 * 
	 * @param plotTypeName
	 *            String that is the name of the plot being produced.
	 */
	public void setPlotTypeName(String newName) {
		plotTypeName = newName;
	}

	public void setTitle(String newTitle) {
		super.setTitle(optionName + " " + newTitle);
		titleText = newTitle;
	}

	public void setTable(OptionsTable table) {
		this.optionsTable = table;
	}

	protected void setResult(int newResult) {
		result = newResult;
	}

	public int getResult() {
		return result;
	}

	/**
	 * Write this method to initialize the GUI from the value of the data object
	 */
	protected abstract void initGUIFromModel();

	/**
	 * Write this method to store the info from the GUI in the data object
	 * 
	 * @throws Exception
	 */
	protected abstract void saveGUIValuesToModel() throws Exception;

	/**
	 * Override this to specify the info shown in the table
	 * 
	 * @return String info to show in the options table
	 */
	public String getInfoString() {
		return "";
	}

	protected JPanel getButtonPanel() {
		// Create a new OK Cancel Panel with the OK and Cancel Action Listeners
		ActionListener okListener = getOKListener();
		ActionListener cancelListener = getCancelListener();
		JPanel buttonPanel = new OKCancelPanel(okListener, cancelListener, getRootPane());
		return buttonPanel;
	}

	public JPanel getButtonPanel(boolean helpButton, ActionListener helpListener) {
		if (helpButton) {
			ActionListener okListener = getOKListener();
			ActionListener cancelListener = getCancelListener();
			JPanel buttonPanel = new OKCancelHelpPanel(okListener, cancelListener, helpListener, getRootPane());
			return buttonPanel;
		} else {
			return getButtonPanel();
		}
	}

	private ActionListener getOKListener() {
		ActionListener okListener = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {
					saveGUIValuesToModel();
					if (shouldContinueClosing) {

						setResult(OptionDialog.OK_RESULT);
						dispose();
					}
					shouldContinueClosing = true;
				}// try
				catch (Exception e) {
					DefaultUserInteractor.get().notifyOfException(OptionDialog.this,
							"Error Setting " + " Plot Properties", e, UserInteractor.ERROR);
				}// catch
			}// actionPerformed
		};

		return okListener;
	}// getOKListener

	private ActionListener getCancelListener() {
		ActionListener cancelListener = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				shouldContinueClosing = true;
				dispose();
				setResult(OptionDialog.CANCEL_RESULT);
				initGUIFromModel();
			}
		};// cancelListener
		return cancelListener;
	}// getCancelListener

}
