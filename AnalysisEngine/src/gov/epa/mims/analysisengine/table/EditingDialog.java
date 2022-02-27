package gov.epa.mims.analysisengine.table;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * A dialog with OK, Cancel, and help buttons.
 * 
 * @author Alison Eyth
 * @version $Id: EditingDialog.java,v 1.3 2006/10/30 21:43:50 parthee Exp $
 */
public abstract class EditingDialog extends JDialog {
	private int returnValue;

	BorderLayout borderLayout1 = new BorderLayout();

	BorderLayout borderLayout2 = new BorderLayout();

	protected JPanel mainPanel = new JPanel();

	protected JPanel buttonPanel = new JPanel();

	GridLayout gridLayout1 = new GridLayout();

	JButton okButton = new JButton();

	JButton helpButton = new JButton();

	JButton closeButton = new JButton();

	String baseTitle = null;

	String okCaption = null;

	// UndoCommandProcessor undoPcsr = null;
	String actionDescription;

	// protected UndoCommandProcessor processor;
	boolean helpAvailable;

	boolean hasChanged = false;

	// protected static CommandSource cmdSource = MainGUI.getCommandSource();

	private static Vector openDialogs = new Vector();

	public EditingDialog(String baseTitle, String OKCaption, boolean helpAvailable) {
		super();
		this.setTitle(baseTitle);
		this.baseTitle = baseTitle;
		this.okCaption = OKCaption;
		this.helpAvailable = helpAvailable;
		// processor = MainGUI.getUndoCommandProcessor();
		// returnValue = CommandSource.CANCEL;
		openDialogs.addElement(this);
		try {
			// jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void dispose() {
		super.dispose();
		openDialogs.removeElement(this);
	}

	public static void closeAll() {
		EditingDialog thisDialog;
		// Be careful - size is changing, can't use openDialogs.size() in the loop
		// statement
		int numOpen = openDialogs.size();
		for (int i = 0; i < numOpen; i++) {
			// don't use elementAt(i) - size is changing!
			thisDialog = (EditingDialog) openDialogs.elementAt(0);
			// dispose also removes from list...
			thisDialog.dispose();
		}
	}

	/**
	 * Call this when an object on the dialog has changed. It changes the title, enables the "OK" button, and changes
	 * the name of the "Close" button to "Cancel".
	 */
	public void setHasChanged() {
		okButton.setEnabled(true);
		closeButton.setText("Cancel");
		setTitle(baseTitle + '*');
		hasChanged = true;
	}

	protected void jbInit() throws Exception {
		this.setTitle(baseTitle);
		this.getContentPane().setLayout(borderLayout1);
		buttonPanel.setLayout(gridLayout1);
		gridLayout1.setHgap(10);
		Insets buttonInsets = new Insets(2, 3, 2, 3);
		okButton.setMargin(buttonInsets);
		okButton.setActionCommand("okAction");
		okButton.setToolTipText("Ok");
		okButton.setText(okCaption);
		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				okButton_actionPerformed(e);
			}
		});
		helpButton.setMargin(buttonInsets);
		helpButton.setActionCommand("helpAction");
		helpButton.setText("Help");
		helpButton.setToolTipText("Help");
		helpButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				helpButton_actionPerformed(e);
			}
		});
		closeButton.setMargin(buttonInsets);
		closeButton.setActionCommand("closeAction");
		closeButton.setToolTipText("Close");
		closeButton.setText(" Close ");
		closeButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeButton_actionPerformed(e);
			}
		});
		this.getContentPane().add(mainPanel, BorderLayout.CENTER);
		this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		buttonPanel.add(okButton, null);
		buttonPanel.add(closeButton, null);
		if (helpAvailable)
			buttonPanel.add(helpButton, null);
		okButton.setEnabled(false);

		// border: top, left, bottom, right
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				closeButton_actionPerformed(null);
			}
		});

		mainPanel.setLayout(borderLayout2);
		// in child jbInit, do mainPanel.add(childPanel);
	}

	/*
	 * Return the value associated with the button that was pressed.
	 */
	public int getValue() {
		return returnValue;
	}

	void okButton_actionPerformed(ActionEvent e) {
		// returnValue = CommandSource.YES;
		setTitle(baseTitle);
		if (acceptChanges()) {
			setVisible(false);
		}
		// if changes aren't accepted, the window stays up
	}

	void closeButton_actionPerformed(ActionEvent e) {
		// returnValue = CommandSource.CANCEL;
		discardChanges();
		setVisible(false);
		// if this is true, some change has been made so make a new window next time
		if (hasChanged)
			dispose();
	}

	void helpButton_actionPerformed(ActionEvent e) {
		getHelp();
	}

	/**
	 * A method that will be called with the OK / Accept button is pushed
	 * 
	 * @returns boolean whether the window can be closed
	 */
	protected abstract boolean acceptChanges();

	/**
	 * A method to be called when the Close / Cancel button is pushed
	 */
	protected abstract void discardChanges();

	/**
	 * A method to be called when the Help button is pushed
	 */
	protected abstract void getHelp();

}
