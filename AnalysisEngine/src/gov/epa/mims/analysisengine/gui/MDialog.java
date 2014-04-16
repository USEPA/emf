package gov.epa.mims.analysisengine.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Provide some common dialog behaviors.
 * 
 * The escape key handling is adapted from <a href="http://www.javaworld.com/javaworld/javatips/jw-javatip72.html">
 * JavaWorld Tips</a>. This doesn't seem to work with dialogs that have text fields (at least NewMemberDialog).
 * 
 * @author Steve Fine
 * @version $Id: MDialog.java,v 1.3 2007/01/09 23:06:15 parthee Exp $
 * 
 */

public class MDialog extends javax.swing.JDialog {

	/*******************************************************************************************************************
	 * 
	 * methods
	 * 
	 ******************************************************************************************************************/
	public MDialog() {
		this(JOptionPane.getRootFrame(), false);
	}

	public MDialog(java.awt.Frame owner) {
		this(owner, false);
	}

	public MDialog(java.awt.Frame owner, boolean modal) {
		this(owner, null, modal);
	}

	public MDialog(java.awt.Frame owner, String title) {
		this(owner, title, false);
	}

	public MDialog(java.awt.Frame owner, String title, boolean modal) {
		super(owner, title, modal);
	}

	public MDialog(Dialog owner) {
		this(owner, false);
	}

	public MDialog(Dialog owner, boolean modal) {
		this(owner, null, modal);
	}

	public MDialog(Dialog owner, String title) {
		this(owner, title, false);
	}

	public MDialog(Dialog owner, String title, boolean modal) {
		super(owner, title, modal);
	}

	/**
	 * create a custom root pane that will recognize when the escape key was pressed and destroy the dialog
	 */
	protected JRootPane createRootPane() {
		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				dispose();
			}
		};
		JRootPane rootPane = new JRootPane();
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		rootPane.registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		return rootPane;
	}
}
