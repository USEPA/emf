package gov.epa.mims.analysisengine.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;

/**
 * A helper Dialog to edit arrays of values.. Presently edits double[], Color[], String[] from a available set of
 * Strings
 * 
 * @author Prashant Pai, CEP UNC
 * @version $Id: ArrayEditorDialog.java,v 1.4 2007/01/10 22:31:35 parthee Exp $
 */

public class ArrayEditorDialog extends JDialog {

	/*******************************************************************************************************************
	 * 
	 * fields
	 * 
	 ******************************************************************************************************************/

	/** the double array to edit * */
	private double[] doubleArray = null;

	/** the color array to edit * */
	private Color[] colorArray = null;

	/** the string array to edit * */
	private String[] stringArray = null;

	/** the available set of strings for editing the string array * */
	private String[] availStringChoices = null;

	/** a list to include all the editable values * */
	private JList list = null;

	/** the arraylist holding the objects to be displayed * */
	private ArrayList objectList = null;

	/** the type of values being edited * */
	private String type = null;

	/** the lower bound to be placed on the values in the array * */
	private double lowBound = Double.NEGATIVE_INFINITY;

	/** the upper bound to be placed on the values in the array * */
	private double upBound = Double.POSITIVE_INFINITY;

	/** a static set of variables that helps this class know what it is editing * */
	private static String COLORS = "Colors";

	private static String DOUBLES = "Doubles";

	private static String STRINGS = "Strings";

	/*******************************************************************************************************************
	 * 
	 * methods
	 * 
	 ******************************************************************************************************************/

	/**
	 * constructor to edit a double[]
	 * 
	 * @param doubles
	 *            the double[] to be edited
	 */
	public ArrayEditorDialog(double[] doubles, String label, double lowBound, double upBound) {
		this.doubleArray = doubles;
		this.lowBound = lowBound;
		this.upBound = upBound;
		this.type = DOUBLES;
		setTitle("Edit " + label);
		initialize();
	}// ArrayEditorDialog(double[])

	/**
	 * constructor to edit a Color[]
	 * 
	 * @param colors
	 *            the Color[] to be edited
	 */
	public ArrayEditorDialog(Color[] colors, String label) {
		this.colorArray = colors;
		this.type = COLORS;
		setTitle("Edit " + label);
		initialize();
	}// ArrayEditorDialog(Color[])

	public ArrayEditorDialog(String[] strings, String[] availStrings, String label) {
		this.stringArray = strings;
		this.availStringChoices = availStrings;
		this.type = STRINGS;
		setTitle("Edit " + label);
		initialize();
	}// ArrayEditorDialog(Color[])

	private void initialize() {
		// make the dialog come up in the center of the screen
		this.setLocationRelativeTo(JOptionPane.getRootFrame());
		Container contentPane = this.getContentPane();
		contentPane.setLayout(new BorderLayout());
		list = new JList();
		contentPane.add(new JScrollPane(list), BorderLayout.CENTER);

		// set the renderer so that it renders colors differently
		list.setCellRenderer(new ColorListCellRenderer());

		// a panel for the add, remove and ok and cancel buttons
		JPanel buttonPanel = new JPanel(new GridLayout(2, 1));
		contentPane.add(buttonPanel, BorderLayout.SOUTH);
		JButton addButton = new JButton("Add");
		JButton removeButton = new JButton("Remove");
		JButton editButton = new JButton("Edit");
		JPanel button1Panel = new JPanel();
		button1Panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		button1Panel.add(addButton);
		button1Panel.add(removeButton);
		button1Panel.add(editButton);
		buttonPanel.add(button1Panel);
		OKCancelPanel ocPanel = new OKCancelPanel(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		}, new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				objectList = null;
				dispose();
			}
		}, getRootPane());
		buttonPanel.add(ocPanel);
		objectList = new ArrayList();
		ActionListener editListener = null;

		// different configurations for COLORS, DOUBLES and STRINGS
		if (type.equals(DOUBLES)) {
			if (doubleArray != null)
				for (int i = 0; i < doubleArray.length; i++)
					objectList.add(Double.valueOf(doubleArray[i]));
			list.setListData(objectList.toArray());
			list.setSelectedIndex(0);
			addButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					Double value = null;
					try {
						String enter = (new GUIUserInteractor()).getString("Enter a floating point value",
								"Add new value", "0.0");
						if (enter == null)
							return;
						value = Double.valueOf(enter);
						if (value.doubleValue() < lowBound || value.doubleValue() > upBound)
							throw new Exception("Value should be between " + lowBound + " and " + upBound);
						objectList.add(value);
						list.setListData(objectList.toArray());
						list.setSelectedIndex(list.getModel().getSize() - 1);
					} catch (Exception e) {
						(new GUIUserInteractor()).notify(ArrayEditorDialog.this, "Error evaluating number",
								"The input is not a valid floating point number. " + e.getMessage(),
								UserInteractor.ERROR);
						return;
					}
				}// actionPeformed()
			});// addActionListener()
			removeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					if (list.isSelectionEmpty())
						return;
					Object[] selectedItems = list.getSelectedValues();
					for (int i = 0; i < selectedItems.length; i++) {
						objectList.remove(selectedItems[i]);
					}
					list.setListData(objectList.toArray());
					if (list.getModel().getSize() > 0)
						list.setSelectedIndex(0);
				}// actionPeformed()
			});
			editListener = new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					if (list.isSelectionEmpty())
						return;
					Double value = null;
					try {
						int index = list.getSelectedIndex();
						String enter = (new GUIUserInteractor()).getString("Enter a floating point value",
								"Add new value", objectList.get(index).toString());
						if (enter == null)
							return;
						value = Double.valueOf(enter);
						if (value.doubleValue() < lowBound || value.doubleValue() > upBound)
							throw new Exception("Value should be between " + lowBound + " and " + upBound);
						objectList.remove(index);
						objectList.add(index, value);
						list.setListData(objectList.toArray());
						list.setSelectedIndex(index);
					} catch (Exception e) {
						// e.printStackTrace();
						(new GUIUserInteractor()).notify(ArrayEditorDialog.this, "Error evaluating number",
								"The input is not a valid floating point number. " + e.getMessage(),
								UserInteractor.ERROR);
						return;
					}
				}
			};
			editButton.addActionListener(editListener);
			MouseListener mouseListener = new MouseAdapter() {
				public void mouseClicked(MouseEvent me) {
					if (me.getClickCount() == 2) {
						if (list.isSelectionEmpty())
							return;
						Double value = null;
						try {
							int index = list.getSelectedIndex();
							String enter = (new GUIUserInteractor()).getString("Enter a floating point value",
									"Add new value", objectList.get(index).toString());
							if (enter == null)
								return;
							value = Double.valueOf(enter);
							objectList.remove(index);
							objectList.add(index, value);
							list.setListData(objectList.toArray());
							list.setSelectedIndex(index);
						} catch (Exception e) {
							e.printStackTrace();
							(new GUIUserInteractor()).notify(ArrayEditorDialog.this, "Error evaluating number",
									"The input is not a floating point number", UserInteractor.ERROR);
							return;
						}
					}
				}
			};
			list.addMouseListener(mouseListener);

		} else if (type.equals(COLORS)) {
			if (colorArray != null)
				for (int i = 0; i < colorArray.length; i++)
					objectList.add(colorArray[i]);
			list.setListData(objectList.toArray());
			list.setSelectedIndex(0);
			addButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					Color value = null;
					try {
						value = JColorChooser.showDialog(ArrayEditorDialog.this, "Choose Color", null);
						if (value == null)
							return;
						objectList.add(value);
						list.setListData(objectList.toArray());
						list.setSelectedIndex(list.getModel().getSize() - 1);
					} catch (Exception e) {
						e.printStackTrace();
						(new GUIUserInteractor()).notify(ArrayEditorDialog.this, "Error evaluating number",
								"The input is not a valid color", UserInteractor.ERROR);
						return;
					}
				}// actionPeformed()
			});// addActionListener()
			removeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					if (list.isSelectionEmpty())
						return;
					objectList.remove(list.getSelectedIndex());
					list.setListData(objectList.toArray());
					if (list.getModel().getSize() > 0)
						list.setSelectedIndex(0);
				}// actionPeformed()
			});
			editListener = new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					if (list.isSelectionEmpty())
						return;
					Color value = null;
					try {
						int index = list.getSelectedIndex();
						value = JColorChooser.showDialog(null, "Choose Color", (Color) objectList.get(index));
						if (value == null)
							return;
						objectList.remove(index);
						objectList.add(index, value);
						list.setListData(objectList.toArray());
						list.setSelectedIndex(index);
					} catch (Exception e) {
						(new GUIUserInteractor()).notify(ArrayEditorDialog.this, "Error evaluating number",
								"The input is not a valid color", UserInteractor.ERROR);
						return;
					}
				}
			};
			editButton.addActionListener(editListener);
			MouseListener mouseListener = new MouseAdapter() {
				public void mouseClicked(MouseEvent me) {
					if (me.getClickCount() == 2) {
						if (list.isSelectionEmpty())
							return;
						Color value = null;
						try {
							int index = list.getSelectedIndex();
							value = JColorChooser.showDialog(null, "Choose Color", (Color) objectList.get(index));
							if (value == null)
								return;
							objectList.remove(index);
							objectList.add(index, value);
							list.setListData(objectList.toArray());
							list.setSelectedIndex(index);
						} catch (Exception e) {
							(new GUIUserInteractor()).notify(ArrayEditorDialog.this, "Error evaluating number",
									"The input is not a valid color", UserInteractor.ERROR);
							return;
						}
					}
				}
			};
			list.addMouseListener(mouseListener);
		}// else if (type.equals(DOUBLES))
		else if (type.equals(STRINGS)) {
			if (stringArray != null)
				for (int i = 0; i < stringArray.length; i++)
					objectList.add(stringArray[i]);
			list.setListData(objectList.toArray());
			list.setSelectedIndex(0);
			addButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					String value = null;
					try {
						if (availStringChoices != null)
							value = (String) JOptionPane.showInputDialog(null, "Select one for the available options",
									"Choose one", JOptionPane.INFORMATION_MESSAGE, null, availStringChoices,
									availStringChoices[0]);
						else
							value = JOptionPane.showInputDialog("Enter a value");
						if (value == null)
							return;
						objectList.add(value);
						list.setListData(objectList.toArray());
						list.setSelectedIndex(list.getModel().getSize() - 1);
					} catch (Exception e) {
						e.printStackTrace();
						(new GUIUserInteractor()).notify(ArrayEditorDialog.this, "Error evaluating entry",
								"The input is not a valid choice", UserInteractor.ERROR);
						return;
					}
				}// actionPeformed()
			});// addActionListener()
			removeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					if (list.isSelectionEmpty())
						return;
					objectList.remove(list.getSelectedIndex());
					list.setListData(objectList.toArray());
					if (list.getModel().getSize() > 0)
						list.setSelectedIndex(0);
				}// actionPeformed()
			});
			editListener = new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					if (list.isSelectionEmpty())
						return;
					String value = null;
					try {
						int index = list.getSelectedIndex();
						value = (String) JOptionPane.showInputDialog(null, "Select one for the available options",
								"Choose one", JOptionPane.INFORMATION_MESSAGE, null, availStringChoices, list
										.getSelectedValue());
						if (value == null)
							return;
						objectList.remove(index);
						objectList.add(index, value);
						list.setListData(objectList.toArray());
						list.setSelectedIndex(index);
					} catch (Exception e) {
						(new GUIUserInteractor()).notify(ArrayEditorDialog.this, "Error evaluating entry",
								"The input is not a valid choice", UserInteractor.ERROR);
						return;
					}
				}
			};
			editButton.addActionListener(editListener);
			MouseListener mouseListener = new MouseAdapter() {
				public void mouseClicked(MouseEvent me) {
					if (me.getClickCount() == 2) {
						if (list.isSelectionEmpty())
							return;
						String value = null;
						try {
							int index = list.getSelectedIndex();
							if (availStringChoices != null)
								value = (String) JOptionPane.showInputDialog(null,
										"Select one for the available options", "Choose one",
										JOptionPane.INFORMATION_MESSAGE, null, availStringChoices, list
												.getSelectedValue());
							else
								value = JOptionPane.showInputDialog("Enter a value");

							if (value == null)
								return;
							objectList.remove(index);
							objectList.add(index, value);
							list.setListData(objectList.toArray());
							list.setSelectedIndex(index);
						} catch (Exception e) {
							(new GUIUserInteractor()).notify(ArrayEditorDialog.this, "Error evaluating entry",
									"The input is not a valid choice", UserInteractor.ERROR);
							return;
						}
					}
				}
			};
			list.addMouseListener(mouseListener);
		}// else
		else
			(new GUIUserInteractor()).notify(ArrayEditorDialog.this, "Invalid Choice", "This type of array "
					+ "cannot be edited", UserInteractor.WARNING);

		setModal(true);
		pack();
		setVisible(true);
	}// initialize()

	public double[] getDoubleValues() {
		if (objectList == null)
			return doubleArray;
		doubleArray = new double[objectList.size()];
		for (int i = 0; i < objectList.size(); i++)
			doubleArray[i] = ((Double) objectList.get(i)).doubleValue();
		return doubleArray;
	}// getDoubleValues()

	/**
	 * get the set of colors set using this dialog
	 * 
	 * @return Color[] the edited color array
	 */
	public Color[] getColorValues() {
		if (objectList == null)
			return colorArray;
		colorArray = new Color[objectList.size()];
		for (int i = 0; i < objectList.size(); i++)
			colorArray[i] = (Color) objectList.get(i);
		return colorArray;
	}// getColorValues()

	public String[] getStringValues() {
		if (objectList == null)
			return stringArray;
		stringArray = new String[objectList.size()];
		for (int i = 0; i < objectList.size(); i++)
			stringArray[i] = (String) objectList.get(i);
		return stringArray;
	}// getStringValues()

	/**
	 * static method to edit color array in a dialog
	 * 
	 * @param colors
	 *            Color[] to edit
	 * @return Color[] the edited Color[]
	 */
	public static Color[] editColors(Color[] colors) {
		ArrayEditorDialog arrayEditor = new ArrayEditorDialog(colors, "");
		return arrayEditor.getColorValues();
	}// editColors()

	/**
	 * static method to edit double array in a dialog
	 * 
	 * @param doubles
	 *            double[] to edit
	 * @return double[] the edited double array
	 */
	public static double[] editDoubles(double[] doubles) {
		ArrayEditorDialog arrayEditor = new ArrayEditorDialog(doubles, "", Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY);
		return arrayEditor.getDoubleValues();
	}// editDoubles()

	public void invoke(Object[] objects) {
		if (objectList != null) {
			setVisible(true);
			return;
		}
		objectList = new ArrayList();
		for (int i = 0; i < objects.length; i++)
			objectList.add(objects[i]);
		setVisible(true);
	}

	public void invoke(double[] doubles) {
		if (objectList != null) {
			setVisible(true);
			return;
		}
		objectList = new ArrayList();
		for (int i = 0; i < doubles.length; i++)
			objectList.add(Double.valueOf(doubles[i]));
		setVisible(true);
	}

	/**
	 * static method to edit a set of strings in a dialog these are chosen from a available set of strings
	 * 
	 * @param origChoices
	 *            the original set of choices that the user wants to edit
	 * @param availChoices
	 *            the available set of values
	 * @return String[] the edited choice set
	 */
	public static String[] editStrings(String[] origChoices, String[] availChoices) {
		ArrayEditorDialog arrayEditor = new ArrayEditorDialog(origChoices, availChoices, "");
		return arrayEditor.getStringValues();
	}

	/**
	 * class to render colors in an array editor dialog
	 * 
	 * @see javax.swing.DefaultListCellRenderer
	 */
	class ColorListCellRenderer extends DefaultListCellRenderer {

		public Component getListCellRendererComponent(JList list, Object value, // value to display
				int index, // cell index
				boolean isSelected, // is the cell selected
				boolean cellHasFocus) // the list and the cell have the focus
		{
			if (value instanceof Color) {
				JPanel outerPanel = new JPanel();
				outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.X_AXIS));
				outerPanel.add(Box.createGlue());
				JPanel colorPanel = new JPanel();
				outerPanel.add(colorPanel);
				outerPanel.add(Box.createGlue());
				colorPanel.setBackground((Color) value);
				colorPanel.setSize(0, 20);
				if (isSelected) {
					outerPanel.setBackground(list.getSelectionBackground());
					// outerPanel.setForeground(list.getSelectionForeground());
				} else {
					outerPanel.setBackground(list.getBackground());
					// outerPanel.setForeground(list.getForeground());
				}
				return outerPanel;
			} else {
				return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			}
			/*
			 * Component rend = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus); if
			 * (value instanceof Color) { ((JLabel) rend).setText(""); rend.setForeground((Color) value);
			 * rend.setBackground((Color) value); ((JLabel) rend).setMinimumSize(new Dimension(0, 20)); ((JLabel)
			 * rend).setMaximumSize(new Dimension(0, 20)); //((JLabel) rend).setSize(); } System.out.print("min " +
			 * ((JLabel) rend).getMinimumSize().getHeight()); System.out.println(" max " + ((JLabel)
			 * rend).getMaximumSize().getHeight()); return rend;
			 */
		}
	}

}
