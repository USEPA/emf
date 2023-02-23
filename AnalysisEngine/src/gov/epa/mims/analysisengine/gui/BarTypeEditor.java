package gov.epa.mims.analysisengine.gui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import gov.epa.mims.analysisengine.tree.BarType;

/**
 * A Dialog to edit "properties" of a bartype, which is one of the analysis options for a barplot.
 * 
 * @author Alison Eyth, Prashant Pai, CEP UNC
 * @version $Id: BarTypeEditor.java,v 1.4 2007/01/09 23:06:15 parthee Exp $
 * @see BarType.java
 * @see BarPlot.java
 * @see AnalysisOptions.java
 */

public class BarTypeEditor extends OptionDialog {
	/** the bar type class to be edited * */
	private BarType barType = null;

	/** a double value panel for the space between bars * */
	private DoubleValuePanel barSpace = null;

	/** a double value panel for the space between categories * */
	private DoubleValuePanel categorySpace = null;

	/** a boolean value panel to check whether the data sets span categories * */
	private BooleanValuePanel spanPanel = null;

	/**
	 * a check box to indicate whether to sort the labels or not
	 */
	private BooleanValuePanel sortLabelPanel = null;

	/**
	 * a boolean value panel to check whether the numeric axis should be in log scale
	 */
	// private BooleanValuePanel logScalePanel = null;
	/** a boolean value panel to check whether the bars should be stacked or adjacent * */
	private BooleanValuePanel stackedPanel = null;

	/*******************************************************************************************************************
	 * a boolean value panel to check whether the bars should be horizontal or vertical
	 ******************************************************************************************************************/
	private BooleanValuePanel horizPanel = null;

	/** a color value panel to edit the border colors * */
	private ColorValuePanel borderColorPanel = null;

	/** a double array editor panel to edit the bar widths * */
	private DoubleEditableTablePanel widthPanel = null;

	/** a color array editor panel to edit the bar colors * */
	private ColorEditableTablePanel foreColorsPanel = null;

	/** to specify whether to shade the bars or not * */
	private JCheckBox shadeBarsCB = null;

	/** a muliple column table panel for settings for shading bars */
	private MultipleEditableTablePanel shadeBarTablePanel = null;

	/**
	 * a converter for converting pretty options to system options and vice versa
	 */
	private PrettyOptionImageIconConverter lineTypeConverter = null;

	private static final int LINE_STYLE_COL = 1;

	private static final int LINE_WIDTH_COL = 2;

	private static final int LINE_ANGLE_COL = 3;

	private static final int SHADING_DENSITY_COL = 4;

	// /////////////////////METHODS//////////////////////////////////
	public BarTypeEditor(BarType aBarType) {
		super();
		initialize();
		setDataSource(aBarType, "");
		setLocation(ScreenUtils.getPointToCenter(this));
		// show();
	}// BarTypeEditor()

	/**
	 * constructor need for class.newInstance
	 */
	public BarTypeEditor() {
		this(null);
	}

	public void setDataSource(Object dataSource, String optionName) {
		this.barType = (BarType) dataSource;
		super.setDataSource(dataSource, optionName);
		if (barType != null) {
			initGUIFromModel();
		}
		pack();
		this.repaint();
	}

	/**
	 * the initialize method for the dialog
	 */
	private void initialize() {
		this.setModal(true);
		// set the title
		this.setTitle("Edit Bar Type Properties");
		Container contentPane = this.getContentPane();
		// set the layout
		contentPane.setLayout(new BorderLayout());

		// add the boolean to a boolean panel
		JPanel booleanPanel = new JPanel(new BorderLayout());
		JPanel orientPanel = new JPanel(new GridLayout(1, 2, 3, 3));
		spanPanel = new BooleanValuePanel("Transpose data?");
		spanPanel.setToolTipText("Caution: Don't select this option if data sets have repeated labels");
		sortLabelPanel = new BooleanValuePanel("Sort Labels?");
		sortLabelPanel.setToolTipText("To sort the labels of the selected datasets");
		// logScalePanel = new BooleanValuePanel("Log Scale?");

		JPanel spanSortPanel = new JPanel();
		spanSortPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		spanSortPanel.add(spanPanel);
		spanSortPanel.add(sortLabelPanel);
		// spanLogPanel.add(logScalePanel);
		booleanPanel.add(spanSortPanel, BorderLayout.NORTH);
		stackedPanel = new BooleanValuePanel("Adjacent", "Stacked", false);
		stackedPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED),
				"Stacking", TitledBorder.LEFT, TitledBorder.TOP));
		horizPanel = new BooleanValuePanel("Vertical", "Horizontal", false);
		horizPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED),
				"Orientation", TitledBorder.LEFT, TitledBorder.TOP));
		orientPanel.add(stackedPanel);
		orientPanel.add(horizPanel);
		booleanPanel.add(orientPanel, BorderLayout.SOUTH);
		contentPane.add(booleanPanel, BorderLayout.NORTH);

		// create a positions panel for bar widths, space between bars, and space
		// between categories
		JPanel positionPanel = new JPanel();
		positionPanel.setLayout(new BoxLayout(positionPanel, BoxLayout.Y_AXIS));
		// set the title border with Bar Positions as the title
		positionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED),
				"Bar Widths", TitledBorder.LEFT, TitledBorder.TOP));

		widthPanel = new DoubleEditableTablePanel("Bar Widths");
		widthPanel.setToolTipText("The width values are used cyclically");
		positionPanel.add(widthPanel);

		JPanel colorPanel = new JPanel();
		colorPanel.setLayout(new BoxLayout(colorPanel, BoxLayout.Y_AXIS));
		// set the title border with Bar Positions as the title
		colorPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED),
				"Bar Colors", TitledBorder.LEFT, TitledBorder.TOP));

		foreColorsPanel = new ColorEditableTablePanel("Bar Colors");
		foreColorsPanel.setToolTipText("These colors are used cyclically");
		colorPanel.add(foreColorsPanel);

		JPanel twoTablePanel = new JPanel(new GridLayout(1, 2));
		twoTablePanel.add(colorPanel);
		twoTablePanel.add(positionPanel);

		barSpace = new DoubleValuePanel("Space between bars:", false, 0, Double.POSITIVE_INFINITY);
		barSpace.setToolTipText("Space between bars within categories");
		categorySpace = new DoubleValuePanel("Space between categories:", false);
		borderColorPanel = new ColorValuePanel("Border Color:", false);

		JPanel lowerMiddlePanel = new JPanel();
		lowerMiddlePanel.setLayout(new BoxLayout(lowerMiddlePanel, BoxLayout.Y_AXIS));
		lowerMiddlePanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		lowerMiddlePanel.add(barSpace);
		lowerMiddlePanel.add(categorySpace);
		lowerMiddlePanel.add(borderColorPanel);

		shadeBarsCB = new JCheckBox("Use Patterns ? ");
		shadeBarsCB.setHorizontalTextPosition(SwingConstants.LEFT);
		shadeBarsCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean enable = shadeBarsCB.isSelected();
				// System.out.println("enable = "+ enable);
				shadeBarTablePanel.setEnabledAll(enable);
			}
		});
		JPanel checkBoxShadeBarsPanel = new JPanel();
		checkBoxShadeBarsPanel.add(shadeBarsCB);

		lineTypeConverter = PrettyOptionImageIconConverter.getHistogramLineStyleConverter();
		ImageIcon[] icons = lineTypeConverter.getAllPrettyOptions();
		ImageTableColumn lineStyleColumn = new ImageTableColumn(LINE_STYLE_COL, "Style");
		lineStyleColumn.setValidChoices(icons);
		DoubleTableColumn lineWidthColumn = new DoubleTableColumn(LINE_WIDTH_COL, "Width");
		lineWidthColumn.setBounds(0.0, 10.0);
		DoubleTableColumn angleColumn = new DoubleTableColumn(LINE_ANGLE_COL, "Angle");

		angleColumn.setBounds(0.0, 180.0);
		DoubleTableColumn shadeDensityColumn = new DoubleTableColumn(SHADING_DENSITY_COL, "Density");
		shadeDensityColumn.setBounds(0.0, Double.POSITIVE_INFINITY);
		try {
			lineWidthColumn.setDefaultValue(Double.valueOf(1.0));
			angleColumn.setDefaultValue(Double.valueOf(30.0));
			shadeDensityColumn.setDefaultValue(Double.valueOf(20.0));
		} catch (Exception e) {/* NOTHING */
		}

		TableColumn[] tableColumns = { lineStyleColumn, lineWidthColumn, angleColumn, shadeDensityColumn };
		shadeBarTablePanel = new MultipleEditableTablePanel(tableColumns);
		shadeBarTablePanel.setRowHeight(20);
		shadeBarTablePanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		JPanel shadePanel = new JPanel();
		shadePanel.setLayout(new BorderLayout());
		shadePanel.add(checkBoxShadeBarsPanel, BorderLayout.NORTH);
		shadePanel.add(shadeBarTablePanel, BorderLayout.CENTER);

		JPanel middlePanel = new JPanel(new BorderLayout());
		middlePanel.add(twoTablePanel, BorderLayout.NORTH);
		middlePanel.add(lowerMiddlePanel, BorderLayout.CENTER);
		middlePanel.add(shadePanel, BorderLayout.SOUTH);
		contentPane.add(middlePanel, BorderLayout.CENTER);
		contentPane.add(getButtonPanel(), BorderLayout.SOUTH);
	} // initialize()

	/**
	 * @pre the object i.e. BarType is not null
	 */
	protected void initGUIFromModel() {
		spanPanel.setValue(barType.getCategoriesSpanDataSets());
		sortLabelPanel.setValue(barType.getAlphabetize());
		// logScalePanel.setValue(barType.getLog());
		stackedPanel.setValue(barType.getStacked());
		horizPanel.setValue(barType.getHorizontal());

		barSpace.setValue(barType.getSpaceBetweenBars());
		categorySpace.setValue(barType.getSpaceBetweenCategories());
		// we decided not to expose the feature where user can set the border colors
		// for each bars instead we are going to have one color
		borderColorPanel.setValue(barType.getBorderColor()[0]);
		try {
			widthPanel.setValue(barType.getWidth());
		} catch (NullPointerException ne) {
		}
		try {
			foreColorsPanel.setValue(barType.getColor());
		} catch (NullPointerException ne) {
		}
		// //////////////////////////////////////////////////////
		// RP: A temp solution until Tommy implements a flag to check whether we will have shading or not
		String[] lineStyles = barType.getLty();
		double[] lineWidth = barType.getLwd();
		double[] angle = barType.getAngle();
		double[] density = barType.getDensity();
		if (lineStyles != null && lineWidth != null && angle != null && density != null
				&& (lineStyles.length == lineWidth.length) && (lineStyles.length == angle.length)
				&& (lineStyles.length == density.length)) {
			for (int i = 0; i < lineStyles.length; i++) {
				shadeBarTablePanel.setValueAt(lineStyles[i], i, LINE_STYLE_COL);
				shadeBarTablePanel.setValueAt(Double.valueOf(lineWidth[i]), i, LINE_WIDTH_COL);
				shadeBarTablePanel.setValueAt(Double.valueOf(angle[i]), i, LINE_ANGLE_COL);
				shadeBarTablePanel.setValueAt(Double.valueOf(density[i]), i, SHADING_DENSITY_COL);
			}// for(i)s
		}// if
		boolean enable = shadeBarsCB.isSelected();
		shadeBarTablePanel.setEnabledAll(enable);

		// ////////////////////////////////////////////////////////////////////////////////////

	}// initGUIFromModel()

	protected void saveGUIValuesToModel() throws Exception {
		barType.setCategoriesSpanDataSets(spanPanel.getValue());
		barType.setAlphabetize(sortLabelPanel.getValue());
		// barType.setLog(logScalePanel.getValue());
		barType.setStacked(stackedPanel.getValue());
		barType.setHorizontal(horizPanel.getValue());

		barType.setSpaceBetweenBars(barSpace.getValue());
		barType.setSpaceBetweenCategories(categorySpace.getValue());
		// if (!borderColorPanel.getBackground().equals(borderColorPanel.getValue()))
		Color[] borderColor = { borderColorPanel.getValue() };
		barType.setBorderColor(borderColor);

		if (foreColorsPanel.getValue() != null) {
			Object obj = foreColorsPanel.getValue();
			if (obj instanceof Color[]) {
				barType.setColor((Color[]) obj);
			} else {
				DefaultUserInteractor.get().notify(
						this,
						"Unexpected object type",
						"Expected a Color[] in BarTypeEditor.saveGUIValuesToModel(). " + "Found a " + obj.getClass()
								+ " instead.", UserInteractor.ERROR);
			}
		}

		if (widthPanel.getValue() != null) {
			barType.setWidth(widthPanel.getValueAsPrimitive());
		}

		// RP:Temporary solution until Tommy implements a flag for the shading
		if (shadeBarsCB.isSelected()) {
			int rowCount = shadeBarTablePanel.getRowCount();
			ImageIcon[] icons = new ImageIcon[rowCount];
			Double[] width = new Double[rowCount];
			Double[] angle = new Double[rowCount];
			Double[] density = new Double[rowCount];
			for (int i = 0; i < rowCount; i++) {
				icons[i] = (ImageIcon) shadeBarTablePanel.getValueAt(i, LINE_STYLE_COL);
				width[i] = (Double) shadeBarTablePanel.getValueAt(i, LINE_WIDTH_COL);
				angle[i] = (Double) shadeBarTablePanel.getValueAt(i, LINE_ANGLE_COL);
				density[i] = (Double) shadeBarTablePanel.getValueAt(i, SHADING_DENSITY_COL);
			}// for(i)

			String[] styles = lineTypeConverter.getSystemOptions(icons);
			barType.setLty(styles);
			double[] widthPrim = new double[rowCount];
			double[] anglePrim = new double[rowCount];
			double[] densityPrim = new double[rowCount];
			for (int i = 0; i < rowCount; i++) {
				widthPrim[i] = width[i].doubleValue();
				anglePrim[i] = angle[i].doubleValue();
				densityPrim[i] = density[i].doubleValue();
			}// for(i)
			barType.setLwd(widthPrim);
			barType.setAngle(anglePrim);
			barType.setDensity(densityPrim);
		}// if(shadeBarsCB.isSelected())
		else {
			int rowCount = shadeBarTablePanel.getRowCount();
			for (int i = 0; i < rowCount; i++) {
				shadeBarTablePanel.removeRow(0);
			}// for(i)

			barType.setLty(null);
			barType.setLwd(null);
			barType.setAngle(null);
			barType.setDensity(null);
		}// ekse
	}// saveGUIValuesToModel()

	public static void main(String[] args) {
		DefaultUserInteractor.set(new GUIUserInteractor());
		BarTypeEditor barTypeEditor = new BarTypeEditor(new BarType());
		barTypeEditor.show();
	}// main()

}// class BarTypeEditor

