package gov.epa.mims.analysisengine.gui;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.tree.*;
import gov.epa.mims.analysisengine.AnalysisException;

import java.awt.Color;
import java.io.Serializable;
import java.util.HashMap;

/**
 * A class which describes an option that can be set by the user. Options are applicable to both pages and plots. This
 * class will store the name, class type of value, tooltip text and the editor class for value. Note that a single
 * OptionInfo could be used in multiple types of plots.
 * 
 * @author Prashant Pai, MCNC , Tommy E. Cathey
 * @version $Id: OptionInfo.java,v 1.4 2007/05/31 14:29:33 qunhe Exp $
 * 
 */
public class OptionInfo implements gov.epa.mims.analysisengine.tree.AnalysisOptionConstantsIfc, Serializable {
	/** serial version UID */
	static final long serialVersionUID = 1;

	/** HashMap from keywords to OptionInfo */
	private static HashMap keywordToOptionInfo;

	/** HashMap from value Classes to default tableCellEditors or dialogs */
	private static HashMap defaultEditors;

	static {
		keywordToOptionInfo = new HashMap();

		// initialize the hash map with OptionInfo values for known keywords
		defaultEditors = new HashMap();

		// add default editors

		addNewOptionInfo(PLOT_TITLE, "Plot Title", Text.class, null, TextEditor.class);

		addNewOptionInfo(PLOT_SUBTITLE, "Plot Subtitle", Text.class, null, TextEditor.class);

		addNewOptionInfo(PLOT_FOOTER, "Plot Footer", Text.class, null, TextEditor.class);

		addNewOptionInfo(WORLD_COORDINATES, "World Coordinates", WorldCoordinates.class, null,
				WorldCoordinatesEditor.class);

		addNewOptionInfo(LEGEND, "Legend", Legend.class, null, LegendEditor.class);

		addNewOptionInfo(X_NUMERIC_AXIS, "X Axis", AxisNumeric.class, null, AxisNumericEditor.class);

		addNewOptionInfo(Y_NUMERIC_AXIS, "Y Axis", AxisNumeric.class, null, AxisNumericEditor.class);

		addNewOptionInfo(NUMERIC_AXIS, "Numeric Axis", AxisNumeric.class, null, AxisNumericEditor.class);

		addNewOptionInfo(CATEGORY_AXIS, "Category Axis", AxisCategory.class, null, AxisCategoryEditor.class);

		addNewOptionInfo(X_TIME_AXIS, "X Axis", AxisTime.class, null, AxisTimeEditor.class);

		addNewOptionInfo(LINE_TYPE, "Line Type", LineType.class, null, LineTypeEditor.class);

		addNewOptionInfo(OUTLINE_TYPE, "Borders", OutlineType.class, null, OutlineTypeEditor.class);

		addNewOptionInfo(GRID_TYPE, "Grid Type", GridType.class, null, GridTypeEditor.class);

		addNewOptionInfo(BAR_TYPE, "Bars", BarType.class, null, BarTypeEditor.class);

		// the following 2 line will not compile
		addNewOptionInfo(BOX_TYPE, "Box & Whiskers", BoxType.class, null, BoxTypeEditor.class);

		addNewOptionInfo(HISTOGRAM_TYPE, "Histogram", HistogramType.class, null, HistogramTypeEditor.class);

		addNewOptionInfo(PAGE_TYPE, "Page Options", PageType.class, null, PageTypeEditor.class);

		addNewOptionInfo(SORT_TYPE, "Sorting", SortType.class, null, SortTypeEditor.class);

		addNewOptionInfo(DISPLAY_SIZE_TYPE, "Margin Size", DisplaySizeType.class, null, DisplaySizeEditor.class);

		addNewOptionInfo(TEXT_BOXES, "Text Boxes", TextBoxesType.class, null, TextBoxesTypeEditor.class);

		addNewOptionInfo(TORNADO_TYPE, "TornadoType", TornadoType.class, null, TornadoTypeEditor.class);

		addNewOptionInfo(LINEAR_REGRESSIONS, "Linear Regressions", LinearRegressionType.class, null,
				LinearRegressionTypeEditor.class);

		TimeSeries.getPlotInfo();
		ScatterPlot.getPlotInfo();
		CdfPlot.getPlotInfo();
		setGlobalDefaultOptions();

	}

	/**
	 * add global default options
	 */
	private static void setGlobalDefaultOptions() {
		Text defaultTitle = new Text();
		defaultTitle.setPosition(Text.CENTER, 0.5, 0.5);
		defaultTitle.setTextExpansion(1.0);
		AvailableOptionsAndDefaults.addGlobalDefaultValue(PLOT_TITLE, defaultTitle);

		/* if this isn't set, the subtitle is on top of the title */
		Text defaultSubtitle = new Text();
		defaultSubtitle.setPosition(Text.SOUTH, 0.5, 0.5);
		defaultSubtitle.setTextExpansion(0.7);
		AvailableOptionsAndDefaults.addGlobalDefaultValue(PLOT_SUBTITLE, defaultSubtitle);

		/* need to have this default other wise the footer is on top of the axis */
		Text defaultFooter = new Text();
		defaultFooter.setPosition(Text.SOUTH, 0.5, 0.5);
		defaultFooter.setTextExpansion(0.6);
		AvailableOptionsAndDefaults.addGlobalDefaultValue(PLOT_FOOTER, defaultFooter);

		BarType defaultBarType = new BarType();
		defaultBarType.setXlim(new double[] { -0.05, 0.05 });
		defaultBarType.setWidth(new double[] { 1.0 });
		// defaultBarType.setAlphabetize(false);
		AvailableOptionsAndDefaults.addGlobalDefaultValue(BAR_TYPE, defaultBarType);

		BoxType defaultBoxType = new BoxType();
		AvailableOptionsAndDefaults.addGlobalDefaultValue(BOX_TYPE, defaultBoxType);

		TornadoType defaultTornadoType = new TornadoType();
		TextAttribute textAttribute = new TextAttribute();
		textAttribute.setCex(0.7);
		defaultTornadoType.setTextAttribute(textAttribute);
		AvailableOptionsAndDefaults.addGlobalDefaultValue(TORNADO_TYPE, defaultTornadoType);

		Legend defaultLegend = new Legend();
		defaultLegend.setCharacterExpansion(0.7);
		AvailableOptionsAndDefaults.addGlobalDefaultValue(LEGEND, defaultLegend);

		LineType defaultLineType = new LineType();
		defaultLineType.setPlotStyle(LineType.POINTS);
		AvailableOptionsAndDefaults.addGlobalDefaultValue(LINE_TYPE, defaultLineType);
		OutlineType defaultOutlineType = new OutlineType();
		defaultOutlineType.setColor(OutlineType.PLOT, Color.black);
		defaultOutlineType.setDraw(OutlineType.PLOT, true);
		defaultOutlineType.setDraw(OutlineType.FIGURE, false);
		defaultOutlineType.setDraw(OutlineType.INNER, false);
		defaultOutlineType.setDraw(OutlineType.OUTER, false);
		AvailableOptionsAndDefaults.addGlobalDefaultValue(OUTLINE_TYPE, defaultOutlineType);

		SortType defaultSortType = new SortType();
		defaultSortType.setSortMethod(SortType.SHELL);
		defaultSortType.setMissingData(SortType.BEGINNING);
		AvailableOptionsAndDefaults.addGlobalDefaultValue(SORT_TYPE, defaultSortType);

		AxisNumeric defaultXNumericAxis = new AxisNumeric();
		Text txtX = new Text();
		txtX.setPosition(Text.RELATIVE2XAXIS, Text.CENTER, 0.5, 0.1);
		defaultXNumericAxis.setAxisLabelText(txtX);
		defaultXNumericAxis.setTickMarkLabelExpansion(0.7);
		defaultXNumericAxis.setDrawTickMarkLabelsPerpendicularToAxis(true);
		AvailableOptionsAndDefaults.addGlobalDefaultValue(X_NUMERIC_AXIS, defaultXNumericAxis);

		AxisNumeric defaultYNumericAxis = new AxisNumeric();
		Text txtY = new Text();
		txtY.setPosition(Text.RELATIVE2YAXIS, Text.CENTER, 0.5, 0.11);
		// txtY.setPosition(CompassConstantsIfc.WEST,0.5, 0.5);
		txtY.setTextDegreesRotation(90.0);
		defaultYNumericAxis.setAxisLabelText(txtY);
		defaultYNumericAxis.setTickMarkLabelExpansion(0.7);
		defaultYNumericAxis.setDrawTickMarkLabelsPerpendicularToAxis(true);
		AvailableOptionsAndDefaults.addGlobalDefaultValue(Y_NUMERIC_AXIS, defaultYNumericAxis);

		AxisNumeric defaultNumericAxis = new AxisNumeric();
		Text txtN = new Text();
		txtN.setPosition(Text.RELATIVE2YAXIS, Text.CENTER, 0.5, 0.11);
		txtN.setTextDegreesRotation(90.0);
		defaultNumericAxis.setAxisLabelText(txtN);
		defaultNumericAxis.setTickMarkLabelExpansion(0.7);
		defaultNumericAxis.setDrawTickMarkLabelsPerpendicularToAxis(true);
		AvailableOptionsAndDefaults.addGlobalDefaultValue(NUMERIC_AXIS, defaultNumericAxis);

		AxisCategory defaultCategoryAxis = new AxisCategory();
		Text txtC = new Text();
		txtC.setPosition(Text.RELATIVE2XAXIS, Text.CENTER, 0.5, 0.1);
		defaultCategoryAxis.setAxisLabelText(txtC);
		defaultCategoryAxis.setTickMarkLabelExpansion(0.7);
		defaultCategoryAxis.setDrawTickMarkLabelsPerpendicularToAxis(true);
		AvailableOptionsAndDefaults.addGlobalDefaultValue(CATEGORY_AXIS, defaultCategoryAxis);

		AxisTime defaultXTimeAxis = new AxisTime();
		Text txtXTime = new Text();
		txtXTime.setPosition(Text.RELATIVE2XAXIS, Text.CENTER, 0.5, 0.1);
		defaultXTimeAxis.setAxisLabelText(txtXTime);
		Text defaultTimeLabel = new Text();
		defaultTimeLabel.setPosition(Text.SOUTHEAST, 1.0, 1.0);
		defaultTimeLabel.setColor(Color.white);
		defaultXTimeAxis.setConstantTimeLabelFormat(defaultTimeLabel);
		defaultXTimeAxis.setTickMarkLabelExpansion(0.7);
		defaultXTimeAxis.setDrawTickMarkLabelsPerpendicularToAxis(true);
		AvailableOptionsAndDefaults.addGlobalDefaultValue(X_TIME_AXIS, defaultXTimeAxis);

		HistogramType defaultHistogramType = new HistogramType();
		defaultHistogramType.setLinetype(HistogramType.SOLID);
		AvailableOptionsAndDefaults.addGlobalDefaultValue(HISTOGRAM_TYPE, defaultHistogramType);

		DisplaySizeType defaultDisplaySizeType = new DisplaySizeType();
		defaultDisplaySizeType.setEnable(false);
		defaultDisplaySizeType.setFigure(0.0, 1.0, 0.0, 1.0);
		defaultDisplaySizeType.setPlot(0.15, 0.70, 0.15, 0.85, DisplaySizeType.FOF);
		// defaultDisplaySizeType.setMarginOuter(0.1, 0.1, 0.1, 0.1, DisplaySizeType.NDC);
		defaultDisplaySizeType.setMarginOuter(0.05, 0.05, 0.05, 0.05, DisplaySizeType.NDC);
		AvailableOptionsAndDefaults.addGlobalDefaultValue(DISPLAY_SIZE_TYPE, defaultDisplaySizeType);

		TextBoxesType defaultTextBoxesType = new TextBoxesType();
		AvailableOptionsAndDefaults.addGlobalDefaultValue(TEXT_BOXES, defaultTextBoxesType);

		LinearRegressionType defaultLRT = new LinearRegressionType();
		AvailableOptionsAndDefaults.addGlobalDefaultValue(LINEAR_REGRESSIONS, defaultLRT);

	}// setGlobalDefaultOptions()

	// /** set the default options for the linetype externally
	// */
	// public static void setLineTypeGlobalDefaultOptions(LineType lineType)
	// {
	// if(lineType != null)
	// {
	// AvailableOptionsAndDefaults.addGlobalDefaultValue(
	// LINE_TYPE, lineType);
	// lineTypeDefaultSet = false;
	// }
	// }//

	private static void addNewOptionInfo(String keyword, String name, Class valueType, String toolTipText,
			Class valueEditorType) {
		OptionInfo optionInfo = new OptionInfo(keyword, name, valueType, toolTipText, valueEditorType);
		// TBD: what if keyword already exists
		OptionInfo.addKeywordAndInfo(keyword, optionInfo);
	}

	/** name of the option * */
	private String name = null;

	/** name of the option * */
	private String keyword = null;

	/** class type of value * */
	private Class valueType = null;

	/** tooltip text to describe the purpose of the option * */
	private String toolTipText = null;

	/** class type of the value editor * */
	private Class valueEditorType = null;

	/**
	 * Constructor to fully populate option info object
	 */
	public OptionInfo(String keyword, String name, Class valueType, String toolTipText, Class valueEditorType) {
		// make sure name and valueType are filled in appropriately
		this.keyword = keyword;
		this.name = name;
		this.valueType = valueType;
		this.toolTipText = toolTipText;
		this.valueEditorType = valueEditorType;
	}

	/*******************************************************************************************************************
	 * DOCUMENT_ME
	 * 
	 * @return DOCUMENT_ME
	 ******************************************************************************************************************/
	public String getName() {
		return name;
	}

	/*******************************************************************************************************************
	 * DOCUMENT_ME
	 * 
	 * @return DOCUMENT_ME
	 ******************************************************************************************************************/
	public Class getValueType() {
		return valueType;
	}

	/*******************************************************************************************************************
	 * DOCUMENT_ME
	 * 
	 * @return DOCUMENT_ME
	 ******************************************************************************************************************/
	public String getToolTipText() {
		return toolTipText;
	}

	/*******************************************************************************************************************
	 * describe object in a String
	 * 
	 * @return String describing object
	 ******************************************************************************************************************/
	public String toString() {
		StringBuffer buffer = new StringBuffer(500);
		buffer.append("defaultEditors = ");
		if (this.defaultEditors != null)
			buffer.append(this.defaultEditors.toString());
		else
			buffer.append("value is null");
		buffer.append("name = ");
		buffer.append(this.name);
		buffer.append("valueType = ");
		if (this.valueType != null)
			buffer.append(this.valueType.toString());
		else
			buffer.append("value is null");
		buffer.append("toolTipText = ");
		buffer.append(this.toolTipText);
		buffer.append("valueEditorType = ");
		if (this.valueEditorType != null)
			buffer.append(this.valueEditorType.toString());
		else
			buffer.append("value is null");
		return buffer.toString();
	}

	public boolean equals(Object o) {
		boolean rtrn = true;
		if (o == null)
			rtrn = false;
		else if (o == this)
			rtrn = true;
		else if (!(o instanceof OptionInfo))
			rtrn = false;
		else {
			OptionInfo other = (OptionInfo) o;

			if (name == null) {
				if (other.name != null)
					rtrn = false;
			} else if (!name.equals(other.name))
				rtrn = false;

			if (toolTipText == null) {
				if (other.toolTipText != null)
					rtrn = false;
			} else if (!toolTipText.equals(other.toolTipText))
				rtrn = false;

			if (valueType == null) {
				if (other.valueType != null)
					rtrn = false;
			} else if (!valueType.equals(other.valueType))
				rtrn = false;

			if (valueEditorType == null) {
				if (other.valueEditorType != null)
					rtrn = false;
			} else if (!valueEditorType.equals(other.valueEditorType))
				rtrn = false;

		}
		return rtrn;
	}

	/*******************************************************************************************************************
	 * DOCUMENT_ME
	 * 
	 * @return DOCUMENT_ME
	 ******************************************************************************************************************/
	public Class getValueEditorType() {
		return valueEditorType;
	}

	/**
	 * @param keyword
	 *            String keyword to lookup
	 * @return OptionInfo for the keyword, or null if it's not found
	 */
	public static OptionInfo lookUp(String keyword) {
		// look up keyword in the keywordToOptionInfo and return its value as
		// an OptionInfo object
		OptionInfo rtrn = null;
		if (keywordToOptionInfo.containsKey(keyword)) {
			rtrn = (OptionInfo) keywordToOptionInfo.get(keyword);
		}
		return rtrn;
	}

	/**
	 * Add a keyword and appropriate optionInfo to the hashmap
	 * 
	 * @param keyword
	 *            String that is the keyword to add
	 * @param optionInfo
	 *            OptionInfo to add as the value in the map
	 * @return null if the keyword wasn't in the map, or the old value if it's already in there
	 */
	public static void addKeywordAndInfo(String keyword, OptionInfo optionInfo) {
		// if the keyword or optionInfo is null or empty throw
		// IllegalArgumentException
		// if the keyword already exists in the map, return the old value and
		// don't replace it.
		// otherwise add the keyword and value
		// TBD: examine return values from put more closely and reflect that
		// TBD: who's responsible for adding these keyword / info pairs???
		Object rtrn = null;
		if (keyword == null)
			throw new IllegalArgumentException("keyword==null");
		if (optionInfo == null)
			throw new IllegalArgumentException("optionInfo==null");
		if (keywordToOptionInfo.containsKey(keyword)) {
			OptionInfo old = (OptionInfo) keywordToOptionInfo.get(keyword);
			OptionInfo nEw = optionInfo;
			if (!old.equals(nEw)) {
				String msg = "old OptionInfo = " + old.toString();
				msg += " new OptionInfo = " + nEw.toString();
				throw new AnalysisException("OptionInfo's differ: " + msg);
			}
		} else
			keywordToOptionInfo.put(keyword, optionInfo);

	}

	/**
	 * @return Object a default editor (i.e. dialog) for values with a particular class.
	 */
	public static Class getDefaultEditor(Class valueClass) {
		// TBD: define some default table cell editors for common types
		return (Class) defaultEditors.get(valueClass);
	}

	/**
	 * 
	 * @return AnalysisOption that is the default value or a new empty object if no default
	 * @throws Exception
	 *             if there is a problem cloning or getting a new instance
	 */
	public AnalysisOption getDefaultValue() throws Exception {
		AnalysisOption defaultObject = (AnalysisOption) AvailableOptionsAndDefaults.getGlobalDefaultValue(keyword);
		if (defaultObject == null) // no default is available for this object
		{
			Class classToEdit = getValueType();
			defaultObject = (AnalysisOption) classToEdit.newInstance();
		} else {
			defaultObject = (AnalysisOption) defaultObject.clone();
		}
		return defaultObject;
	}

	public static void setPlotTypeDefaults(String plotType) {
		if (plotType.equals(AnalysisEngineConstants.SCATTER_PLOT)) {
			setScatterPlotDefaults();
		} else if (plotType.equals(AnalysisEngineConstants.DISCRETE_CATEGORY_PLOT)) {
			setDiscreteCategoryPlotDefaults();
		} else if (plotType.equals(AnalysisEngineConstants.CDF_PLOT)) {
			setCdfDefaults();
		} else if (plotType.equals(AnalysisEngineConstants.RANK_ORDER_PLOT)) {
			setRankOrderDefaults();
		} else if (plotType.equals(AnalysisEngineConstants.TIME_SERIES_PLOT)) {
			setTimeSeriesPlotDefaults();
		}
		// else{
		// throw new IllegalArgumentException("'" + plotType + "' is not a valid argument");
		// }
	}

	private static void setScatterPlotDefaults() {
		LineType defaultLineType = new LineType();
		defaultLineType.setPlotStyle(LineType.POINTS);
		AvailableOptionsAndDefaults.addGlobalDefaultValue(LINE_TYPE, defaultLineType);
	}// setScatterPlotDefaults

	private static void setTimeSeriesPlotDefaults() {
		LineType defaultLineType = new LineType();
		defaultLineType.setPlotStyle(LineType.LINES);

		AvailableOptionsAndDefaults.addGlobalDefaultValue(LINE_TYPE, defaultLineType);
	}// setTimeSeriesPlotDefaults

	private static void setDiscreteCategoryPlotDefaults() {
		LineType defaultLineType = new LineType();
		defaultLineType.setPlotStyle(LineType.POINTS);

		AvailableOptionsAndDefaults.addGlobalDefaultValue(LINE_TYPE, defaultLineType);
	}// setDiscreteCategoryPlotDefaults

	private static void setCdfDefaults() {
		LineType defaultLineType = new LineType();
		defaultLineType.setPlotStyle(LineType.LINES);

		AvailableOptionsAndDefaults.addGlobalDefaultValue(LINE_TYPE, defaultLineType);
	}// setCdfDefaults()

	private static void setRankOrderDefaults() {
		LineType defaultLineType = new LineType();
		defaultLineType.setPlotStyle(LineType.LINES);

		AvailableOptionsAndDefaults.addGlobalDefaultValue(LINE_TYPE, defaultLineType);
	}// setCdfDefaults()

}
