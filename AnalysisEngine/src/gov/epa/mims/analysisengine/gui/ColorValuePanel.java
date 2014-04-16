package gov.epa.mims.analysisengine.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;

/**
 * A utility panel for editing colors includes options to have a label, place the label on top or on the side
 * 
 * @author Prashant Pai, CEP UNC
 * @version $Id: ColorValuePanel.java,v 1.3 2007/01/10 22:31:35 parthee Exp $
 */

public class ColorValuePanel extends JPanel {

	/** the panel to select the color * */
	private JPanel valuePanel = null;

	/** the label * */
	String label = null;

	/** The JLabel for the above label * */
	JLabel jLabel = null;

	/** a variable to check whether the panel is enabled or not * */
	private boolean enabled = true;

	/** The Color for this panel. */
	private Color myColor = null;

	/** The maximum size for the color panel. */
	Dimension colorPanelDim = new Dimension(20, 20);

	public ColorValuePanel(String label, boolean labelOnTop) {
		this.label = label;
		setAlignmentX(JPanel.CENTER_ALIGNMENT);
		setAlignmentY(0.4f);
		jLabel = new JLabel(label);

		valuePanel = new JPanel();
		valuePanel.setToolTipText("Click here to change the color");
		valuePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.black),
				BorderFactory.createBevelBorder(BevelBorder.RAISED)));
		valuePanel.setMaximumSize(colorPanelDim);
		valuePanel.setPreferredSize(colorPanelDim);
		valuePanel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (!enabled)
					return;
				JComponent p = (JComponent) e.getSource();
				Color c = JColorChooser.showDialog(ColorValuePanel.this, "Choose Color", p.getBackground());
				if (c != null) {
					setValue(c);
				}
			}// mouseClicked()
		});// addMouseListener()

		// Set up the X or Y axis as the layout type.
		int layoutType = BoxLayout.X_AXIS;
		if (labelOnTop) {
			layoutType = BoxLayout.Y_AXIS;
			setLayout(new BoxLayout(this, layoutType));
			jLabel.setHorizontalAlignment(SwingConstants.CENTER);
			jLabel.setHorizontalTextPosition(SwingConstants.CENTER);
			add(jLabel);
			add(Box.createVerticalStrut(5));
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
			panel.add(Box.createHorizontalGlue());
			panel.add(valuePanel);
			panel.add(Box.createHorizontalGlue());
			add(panel);
		} else {
			add(jLabel);
			add(Box.createHorizontalStrut(5));
			add(valuePanel);
		}

		setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
	}// DoubleValuePanel()

	/**
	 * return the value in the textfield
	 * 
	 * @return
	 */
	public Color getValue() throws Exception {
		try {
			return myColor;
		} catch (Exception e) {
			throw new Exception("The " + label + " is not a proper color");
		}
	}// getValue()

	/**
	 * set the value in this panel
	 * 
	 * @param val
	 *            the COlor to set the panel to
	 */
	public void setValue(Color val) {
		valuePanel.setBackground(val);
		myColor = val;
	}// setValue()

	/**
	 * set the usability of the colorPanel within the panel
	 * 
	 * @param enable
	 *            to enable the colorpanel or not
	 */
	public void setEnabled(boolean enable) {
		this.enabled = enable;
		jLabel.setEnabled(enable);
		valuePanel.setEnabled(enable);
	}// setEnabled()

	/**
	 * Set the tool tip to appear over all of the components in the panel.
	 */
	public void setToolTipText(String newText) {
		jLabel.setToolTipText(newText);
		valuePanel.setToolTipText(newText);
	}
}
