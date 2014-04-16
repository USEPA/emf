package gov.epa.mims.analysisengine.table;

import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.TextArea;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.JScrollPane;

/**
 * A GUI for showing text or HTML in an editor pane
 * 
 * @author Alison Eyth
 * @version $Id: TextDialog.java,v 1.3 2006/10/30 21:43:50 parthee Exp $
 * 
 */
public class TextDialog extends EditingDialog {
	/** whether the text is editable */
	private boolean editable;

	/** whether text has been changed */
	private boolean changed;

	private TextArea textArea = new TextArea();

	private Component myCaller = null;

	/*******************************************************************************************************************
	 * Constructor.
	 * 
	 * @author Alison Eyth
	 * 
	 ******************************************************************************************************************/
	public TextDialog(Component myCaller, String title, String text, boolean editable) throws Exception {
		super(title, "OK", false);
		// Create reference from PropertyTypeGUI to the PropertyType
		// it refers to
		this.editable = editable;
		this.myCaller = myCaller;
		textArea.setEditable(editable);
		textArea.setBackground(Color.white);
		changed = false;
		super.jbInit();
		jbInit();
	}

	public void setText(String newText) {
		textArea.setText(newText);
	}

	public void setTextFromList(String prefix, ArrayList list) {
		String newText = prefix + "\n";
		for (int i = 0; i < list.size(); i++) {
			Object curr = list.get(i);
			newText = newText + curr.toString() + "\n";
		}
		textArea.setText(newText);
	}

	JScrollPane jScrollPane1 = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

	class KeyTyped implements java.awt.event.KeyListener {
		public void keyReleased(KeyEvent e) {
			if (!changed && editable) {
				setHasChanged();
			}
		}

		public void keyPressed(KeyEvent e) {
			if (!changed && editable) {
				setHasChanged();
			}
		}

		public void keyTyped(KeyEvent e) {
			if (!changed && editable) {
				setHasChanged();
			}
		}
	}

	KeyTyped keyListener = new KeyTyped();

	protected void jbInit() throws Exception {
		this.getContentPane().add(textArea, BorderLayout.CENTER);
		this.setSize(500, 200);
		// editorPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		this.pack();
		setLocation(ScreenUtils.getPointToCenter(this));
	}

	/**
	 * Called by EditingDialog when the Accept / OK button is pressed
	 * 
	 * @returns boolean whether it is OK to close the EditingDialog
	 */
	protected boolean acceptChanges() {
		if (myCaller != null)
			myCaller.repaint();
		return true;
	}

	/**
	 * Called by EditingDialog when the Close/Cancel button is pressed
	 * 
	 */
	protected void discardChanges() {

		if (myCaller != null) {
			myCaller.repaint();
			if (myCaller instanceof Window)
				((Window) myCaller).toFront();
		}
	}

	protected void getHelp() {
		// TODO Auto-generated method stub

	}
}
