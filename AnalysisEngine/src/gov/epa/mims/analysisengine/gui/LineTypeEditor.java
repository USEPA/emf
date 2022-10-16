package gov.epa.mims.analysisengine.gui;

import gov.epa.mims.analysisengine.tree.LineType;
import gov.epa.mims.analysisengine.tree.LineTypeConstantsIfc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.table.TableColumn;

/**
 * Editor for line type
 * 
 * @author Prashant Pai, Parthee Partheepan UNC
 * @version $Id: LineTypeEditor.java,v 1.5 2007/01/11 20:05:39 parthee Exp $
 * 
 */

public class LineTypeEditor extends OptionDialog {

	/*******************************************************************************************************************
	 * 
	 * fields
	 * 
	 ******************************************************************************************************************/

	/** the linetype to be edited * */
	private LineType lineType = null;

	/** a panel to edit symbol sizes * */
	private MultipleEditableTablePanel tablePanel = null;

	/** an interface to convert system strings into symbol icons * */
	private PrettyOptionImageIconConverter symbolsStyleConverter;

	/** an interface to convert system strings into line style icons * */
	private PrettyOptionImageIconConverter lineStyleConverter;

	/** a check box to specify whether to draw lines */
	private JCheckBox lineCheckBox;

	/** a check box to specify whether to draw lines */
	private JCheckBox pointCheckBox;

	/** Symbol column number */
	private final int SYMBOL_COLUMN_NUMBER = 1;

	/** Size column number */
	private final int SIZE_COLUMN_NUMBER = 2;

	/** Line Style column number */
	private final int LINESTYLE_COLUMN_NUMBER = 3;

	/** Line Width column number */
	private final int LINEWIDTH_COLUMN_NUMBER = 4;

	/** Color column number */
	private final int COLOR_COLUMN_NUMBER = 5;

	/** Indicates whether lines will be drawn */
	private boolean linePlot = true;

	/** Indicates whether points will be drawn */
	private boolean pointPlot = true;

	/*******************************************************************************************************************
	 * 
	 * methods
	 * 
	 ******************************************************************************************************************/

	/**
	 * constructor that edits a line type
	 * 
	 * @param aLineType
	 *            the linetype to be edited
	 */
	public LineTypeEditor(LineType aLineType) {
		super();
		initialize();
		setDataSource(aLineType, "");
		setLocation(ScreenUtils.getPointToCenter(this));
	}// LineTypeEditor(LineType)

	/**
	 * constructor need for class.newInstance
	 */
	public LineTypeEditor() {
		this(null);
	}// LineTypeEditor()

	/**
	 * Set the data source for the editor: tree.LineType
	 * 
	 * @param dataSource
	 *            source of the data of type tree.LineType
	 * @param optionName
	 *            String title for the dialog
	 */

	public void setDataSource(Object dataSource, String optionName) {
		this.lineType = (LineType) dataSource;

		super.setDataSource(dataSource, optionName);
		if (lineType != null) {
			initGUIFromModel();
		}
		pack();
		this.repaint();
	}

	/**
	 * a private method to initialize the GUI
	 */

	private void initialize() {
		this.setModal(true);
		Container contentPane = getContentPane();
		setTitle("Edit LineType Properties");
		contentPane.setLayout(new BorderLayout());

		lineCheckBox = new JCheckBox("Draw Lines", false);
		pointCheckBox = new JCheckBox("Draw Points", true);

		JPanel plotStylePanel = new JPanel();
		plotStylePanel.add(lineCheckBox);
		plotStylePanel.add(pointCheckBox);
		plotStylePanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		contentPane.add(plotStylePanel, BorderLayout.NORTH);

		// setup the first column: symbols
		symbolsStyleConverter = PrettyOptionImageIconConverter.getSymbolsConverter();
		ImageIcon[] icons = symbolsStyleConverter.getAllPrettyOptions();
		ImageTableColumn symbolsColumn = new ImageTableColumn(SYMBOL_COLUMN_NUMBER, "Symbol");
		symbolsColumn.setPreferredWidth(100);
		symbolsColumn.setValidChoices(icons);

		// setup the second column: size
		DoubleTableColumn sizeColumn = new DoubleTableColumn(SIZE_COLUMN_NUMBER, "Size");
		sizeColumn.setColumnHeaderTooltip("Relative Size of the symbol compared to the default size");
		sizeColumn.setPreferredWidth(50);
		sizeColumn.setBounds(0, 10);

		// setup the third column: line styles
		lineStyleConverter = PrettyOptionImageIconConverter.getLineStyleConverter();
		ImageIcon[] lineStyleIcons = lineStyleConverter.getAllPrettyOptions();
		ImageTableColumn lineStyleColumn = new ImageTableColumn(LINESTYLE_COLUMN_NUMBER, "LineStyle");
		lineStyleColumn.setPreferredWidth(100);
		lineStyleColumn.setInsertDefaultValue(true);
		lineStyleColumn.setValidChoices(lineStyleIcons);

		// setup the fourth column: width
		DoubleTableColumn widthColumn = new DoubleTableColumn(LINEWIDTH_COLUMN_NUMBER, "Width");
		widthColumn.setColumnHeaderTooltip("The line width is in pixcels");
		widthColumn.setPreferredWidth(50);
		widthColumn.setBounds(0, 10);

		// setup the fifth column: color
		ColorTableColumn colorColumn = new ColorTableColumn(COLOR_COLUMN_NUMBER, "Color");
		colorColumn.setPreferredWidth(50);
		try {
			symbolsColumn.setDefaultValue(MultipleEditableTablePanel
					.createImageIcon("/gov/epa/mims/analysisengine/gui/icons/symbols/circle.jpg"));
			sizeColumn.setDefaultValue(Double.valueOf(1.0));
			lineStyleColumn.setDefaultValue(MultipleEditableTablePanel
					.createImageIcon("/gov/epa/mims/analysisengine/gui/icons/lineStyles/blank.jpg"));
			widthColumn.setDefaultValue(Double.valueOf(1.0));
			colorColumn.setDefaultValue(Color.black);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// creating a table consist of above columns
		TableColumn[] tableColumns = { symbolsColumn, sizeColumn, lineStyleColumn, widthColumn, colorColumn };
		tablePanel = new MultipleEditableTablePanel(tableColumns);
		tablePanel.setRowHeight(20);
		tablePanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		contentPane.add(tablePanel);
		contentPane.add(getButtonPanel(), BorderLayout.SOUTH);
		// pack();
	}// initialize()

	/**
	 * Add an ActionListener to the checkbox or the radio buttons.
	 * 
	 * @param listener
	 *            ActionListener that will recieve events from this GUI.
	 */
	public void setPlotStyle() {
		if (pointCheckBox.isSelected()) {
			if (!lineCheckBox.isSelected()) {
				lineType.setPlotStyle(LineType.POINTS);
			} else {
				lineType.setPlotStyle(LineType.POINTS_N_LINES);
			}
		} else if (lineCheckBox.isSelected()) {
			lineType.setPlotStyle(LineType.LINES);
		} else {
			lineType.setPlotStyle(LineType.POINTS);
			lineCheckBox.setSelected(false);
			pointCheckBox.setSelected(true);
		}
	}

	/**
	 * Initialize the gui with default settings from the LineType
	 * 
	 * @pre the object i.e. LineType is not null
	 * 
	 */
	protected void initGUIFromModel() {
		if (lineType.getPlotStyle().equals(LineType.POINTS)) {
			linePlot = false;
			pointPlot = true;
		} else if (lineType.getPlotStyle().equals(LineType.LINES)) {
			linePlot = true;
			pointPlot = false;
		} else if (lineType.getPlotStyle().equals(LineType.POINTS_N_LINES)) {
			linePlot = true;
			pointPlot = true;
		}

		lineCheckBox.setSelected(linePlot);
		pointCheckBox.setSelected(pointPlot);

		if (this.lineType != null) {

			int rowCount = tablePanel.getRowCount();
			for (int i = 0; i < rowCount; i++) {
				tablePanel.removeRow(0);
			}

			// Only called when this GUI initialized for the first time
			if (lineType.getLineStyle().length != lineType.getSymbol().length) {
				setupEqualLengthProperties();
			}

			for (int i = 0; i < lineType.getSymbol().length; i++) {
				tablePanel.insertRow(false);
			}

			tablePanel.setValueAt(symbolsStyleConverter.getPrettyOptions(lineType.getSymbol()), SYMBOL_COLUMN_NUMBER);

			double[] symbolSize = lineType.getSymbolExpansion();
			Double[] symbolSizeObjects = new Double[symbolSize.length];
			for (int i = 0; i < symbolSize.length; i++) {
				symbolSizeObjects[i] = Double.valueOf(symbolSize[i]);
			}// for(i)
			tablePanel.setValueAt(symbolSizeObjects, SIZE_COLUMN_NUMBER);

			tablePanel
					.setValueAt(lineStyleConverter.getPrettyOptions(lineType.getLineStyle()), LINESTYLE_COLUMN_NUMBER);

			double[] lineWidth = lineType.getLineWidth();
			Double[] lineWidthObjects = new Double[lineWidth.length];
			for (int i = 0; i < lineWidth.length; i++) {
				lineWidthObjects[i] = Double.valueOf(lineWidth[i]);
			}
			tablePanel.setValueAt(lineWidthObjects, LINEWIDTH_COLUMN_NUMBER);

			tablePanel.setValueAt(lineType.getColor(), COLOR_COLUMN_NUMBER);

		} else {
			DefaultUserInteractor.get().notify(this, "Null Pointer Exception", "The lines have not yet been created.",
					UserInteractor.NOTE);
		}

	}// initGUIFromModel()

	/**
	 * The LineType does not have equal length arrays for symbols, linestyles, colors linewidth and symbol expansion. So
	 * this method is only called when this gui is initialized first time. and this will create equal arrays and set the
	 * values in the LineType. WARNING: This method based on on the default values in the LineType class.
	 * 
	 * @see LineType
	 */
	private void setupEqualLengthProperties() {
		// ASSUMPTION symbols.length > lineTypes.length && symbols.length > colors.length
		String[] symbols = LineType.getDefaultSymbols();
		Color[] colors = LineType.getCyclicColors();
		double symbolExpansion = lineType.getSymbolExpansion()[0]; // Assump:length=1
		double lineWidth = lineType.getLineWidth()[0];// Assump:length=1
		String[] newLineStyles = new String[symbols.length];
		Color[] newColors = new Color[symbols.length];
		double[] newSymbolExpansions = new double[symbols.length];
		double[] newLineWidth = new double[symbols.length];
		int countColor = 0;
		for (int i = 0; i < symbols.length; i++) {
			newColors[i] = colors[countColor++];
			if (countColor >= colors.length ) {
				countColor = 0;
			}
			newSymbolExpansions[i] = symbolExpansion;
			newLineWidth[i] = lineWidth;
		}// for(i)

		for (int i = 0; i < symbols.length; i++) {

			int index = (int)Math.floor(i / colors.length);
			switch (index) {
			case 0:
				newLineStyles[i] = LineTypeConstantsIfc.SOLID;
				break;
			case 1:
				newLineStyles[i] = LineTypeConstantsIfc.LONGDASH;
				break;
			case 2:
				newLineStyles[i] = LineTypeConstantsIfc.DOTTED;
				break;
			case 3:
				newLineStyles[i] = LineTypeConstantsIfc.DASHED;
				break;
			default:
				newLineStyles[i] = LineTypeConstantsIfc.SOLID;
				break;
			}
		}// for(i)

		lineType.setLineStyle(newLineStyles);
		lineType.setColor(newColors);
		lineType.setSymbolExpansion(newSymbolExpansions);
		lineType.setLineWidth(newLineWidth);
	}// setupEqualLengthProperties

	/**
	 * Save the setting of the gui to the LineType
	 */
	protected void saveGUIValuesToModel() throws Exception {
		if (tablePanel.getRowCount() == 0) {
			DefaultUserInteractor.get().notify(this, "Message",
					"You should have at least one line in the table. Please insert a line.", UserInteractor.ERROR);
			this.shouldContinueClosing = false;
		} else {
			this.shouldContinueClosing = true;
		}

		this.setPlotStyle();
		if (tablePanel.getValueAt(SYMBOL_COLUMN_NUMBER) != null) {
			Object[] tempObjects = tablePanel.getValueAt(SYMBOL_COLUMN_NUMBER);
			ImageIcon[] tempIcons = new ImageIcon[tempObjects.length];
			for (int i = 0; i < tempObjects.length; i++) {
				tempIcons[i] = (ImageIcon) tempObjects[i];
			}
			String[] tempString = symbolsStyleConverter.getSystemOptions(tempIcons);

			lineType.setSymbol(tempString);
		}

		if (tablePanel.getValueAt(SIZE_COLUMN_NUMBER) != null) {
			Object[] tempObjects = tablePanel.getValueAt(SIZE_COLUMN_NUMBER);
			double[] doubles = new double[tempObjects.length];
			for (int i = 0; i < tempObjects.length; i++) {
				doubles[i] = ((Double) tempObjects[i]).doubleValue();
			}
			lineType.setSymbolExpansion(doubles);
		}

		if (tablePanel.getValueAt(LINESTYLE_COLUMN_NUMBER) != null) {
			Object[] tempObjects = tablePanel.getValueAt(LINESTYLE_COLUMN_NUMBER);
			ImageIcon[] tempIcons = new ImageIcon[tempObjects.length];
			for (int i = 0; i < tempObjects.length; i++) {
				tempIcons[i] = (ImageIcon) tempObjects[i];
			}

			lineType.setLineStyle(lineStyleConverter.getSystemOptions(tempIcons));
		}

		if (tablePanel.getValueAt(LINEWIDTH_COLUMN_NUMBER) != null) {
			Object[] tempObjects = tablePanel.getValueAt(LINEWIDTH_COLUMN_NUMBER);
			double[] doubles = new double[tempObjects.length];
			for (int i = 0; i < tempObjects.length; i++) {
				doubles[i] = ((Double) tempObjects[i]).doubleValue();

			}
			lineType.setLineWidth(doubles);
		}

		if (tablePanel.getValueAt(COLOR_COLUMN_NUMBER) != null) {
			Object[] tempObjects = tablePanel.getValueAt(COLOR_COLUMN_NUMBER);
			Color[] colorObjects = new Color[tempObjects.length];
			for (int i = 0; i < tempObjects.length; i++) {
				colorObjects[i] = (Color) tempObjects[i];
			}

			lineType.setColor(colorObjects);
		}

	}// saveGUIValuesToModel()

	public static void main(String[] args) {
		DefaultUserInteractor.set(new GUIUserInteractor());
		try {
			LineType line = new LineType();
			line.setPlotStyle("POINTS");
			LineTypeEditor lineTypeEditor = new LineTypeEditor(line);
			lineTypeEditor.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}// main()

}
