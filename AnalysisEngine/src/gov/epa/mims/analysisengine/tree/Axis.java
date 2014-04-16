package gov.epa.mims.analysisengine.tree;

import java.awt.Color;

import java.io.Serializable;

/**
 * Abstract base class for all AnalysisOption Axes. It holds parameters which are common to all axes. Such as position,
 * color, tickmark and label parameters. setAxisRange(java.lang.Object, java.lang.Object) is abstract and must be
 * implemented by concrete subclasses.
 * 
 * @author Tommy E. Cathey
 * @version $Id: Axis.java,v 1.3 2006/10/30 21:43:50 parthee Exp $
 * 
 */
public abstract class Axis extends AnalysisOption implements Serializable, Cloneable, LineTypeConstantsIfc, TextIfc {
	/** serial version UID */
	static final long serialVersionUID = 1;

	/** positioning algorithm; used in {@link #setPosition(int,double)} */
	public static final int DEFAULT_POSITIONING = 0;

	/** positioning algorithm; used in {@link #setPosition(int,double)} */
	public static final int USER_COORDINATES = 1;

	/** positioning algorithm; used in {@link #setPosition(int,double)} */
	public static final int LINES_INTO_MARGIN = 2;

	/**
	 * color of the axis
	 * <p>
	 * <ul>
	 * <li>set by {@link #setAxisColor(java.awt.Color arg)}
	 * <li>retrieve by {@link #getAxisColor()}
	 * </ul>
	 */
	private Color axisColor = null;

	/**
	 * color of the tickmark labels
	 * <p>
	 * <ul>
	 * <li>set by {@link #setTickMarkLabelColor(java.awt.Color arg)}
	 * <li>retrieve by {@link #getTickMarkLabelColor()}
	 * </ul>
	 */
	private Color tickMarkLabelColor = null;

	/**
	 * font used for tick marks
	 * <p>
	 * <ul>
	 * <li>set by {@link #setTickMarkFont(java.lang.String)}
	 * <li>retrieve by {@link #getTickMarkFont()}
	 * </ul>
	 */
	private String tickMarkFont = null;

	/**
	 * axis label text
	 * <p>
	 * <ul>
	 * <li>set by {@link #setAxisLabelText(gov.epa.mims.analysisengine.tree.Text)}
	 * <li>retrieve by {@link #getAxisLabelText()}
	 * </ul>
	 */
	private Text axisLabelText = null;

	/**
	 * user specified tick mark labels
	 * <p>
	 * <ul>
	 * <li>set by {@link #setUserTickMarkLabels(java.lang.String[] arg))}
	 * <li>retrieve by {@link #getUserTickMarkLabels()}
	 * </ul>
	 */
	private String[] userTickMarkLabels = null;

	/**
	 * user selected tick mark positions
	 * <p>
	 * <ul>
	 * <li>set by {@link #setUserTickMarkPositions(double[] arg)}
	 * <li>retrieve by {@link #getUserTickMarkPositions()}
	 * </ul>
	 */
	private double[] userTickMarkPositions = null;

	/**
	 * draw tick mark labels flag
	 * <p>
	 * <ul>
	 * <li>set by {@link #setDrawTickMarkLabels(boolean arg)}
	 * <li>retrieve by {@link #getDrawTickMarkLabels()}
	 * <li>true -> draw tick mark labels
	 * <li>false -> do NOT draw tick mark labels
	 * </ul>
	 */
	private boolean drawTickMarkLabels = true;

	/**
	 * draw tick mark labels perpendicular to axis flag
	 * <p>
	 * <ul>
	 * <li>set by {@link #setDrawTickMarkLabelsPerpendicularToAxis(boolean arg)}
	 * <li>retrieve by {@link #getDrawTickMarkLabelsPerpendicularToAxis()}
	 * <li>true -> draw tick mark labels perpendicular to axis
	 * <li>false -> draw tick mark labels parallel to axis
	 * </ul>
	 */
	private boolean drawTickMarkLabelsPerpendicularToAxis = false;

	/**
	 * draw tick marks flag
	 * <p>
	 * <ul>
	 * <li>set by {@link #setDrawTickMarks(boolean arg)}
	 * <li>retrieve by {@link #getDrawTickMarks()}
	 * <li>true -> draw tick mark labels
	 * <li>false -> do NOT draw tick mark labels
	 * </ul>
	 */
	private boolean drawTickMarks = true;

	/**
	 * draw axis flag
	 * <p>
	 * <ul>
	 * <li>set by {@link #setEnableAxis(boolean flg)}
	 * <li>retrieve by {@link #getEnableAxis()}
	 * <li>true -> draw axis
	 * <li>false -> do NOT draw axis
	 * </ul>
	 */
	private boolean enableAxis = true;

	/**
	 * position to draw axis flag
	 * <p>
	 * <ul>
	 * <li>set by {@link #setPosition(int algorithm, double arg)}
	 * <li>retrieve by {@link #getPosition()}
	 * <li>the interpretation of {@link #position} will depend on the value of {@link #positioningAlgorithm}
	 * </ul>
	 */
	private double position = Double.NaN;

	/**
	 * tick mark label expansion coefficient
	 * <p>
	 * <ul>
	 * <li>set by {@link #setTickMarkLabelExpansion(double arg)}
	 * <li>retrieve by {@link #getTickMarkLabelExpansion()}
	 * </ul>
	 */
	private double tickMarkLabelExpansion = Double.NaN;

	/**
	 * positioning algorithm
	 * <p>
	 * <ul>
	 * <li>set by {@link #setPosition(int algorithm, double arg)}
	 * <li>retrieve by {@link #getPositioningAlgorithm()}
	 * </ul>
	 */
	private int positioningAlgorithm = DEFAULT_POSITIONING;

	/*******************************************************************************************************************
	 * set the axisColor value
	 * <p>
	 * sets {@link #axisColor}
	 * <p>
	 * Example of a Color.blue axis
	 * <p>
	 * <IMG BORDER="3" SRC="doc-files/Axis1.jpg">
	 * 
	 * @param arg
	 *            desired color of axis
	 * @see #getAxisColor()
	 ******************************************************************************************************************/
	public void setAxisColor(java.awt.Color arg) {
		this.axisColor = arg;
	}

	/*******************************************************************************************************************
	 * retrieve the axis color
	 * 
	 * @return {@link #axisColor}
	 * @see #setAxisColor(java.awt.Color arg)
	 ******************************************************************************************************************/
	public java.awt.Color getAxisColor() {
		return axisColor;
	}

	/*******************************************************************************************************************
	 * set the axis label Text object
	 * <p>
	 * Example of a Color.red axis label
	 * <p>
	 * <IMG BORDER="3" SRC="doc-files/Axis1.jpg">
	 * <p>
	 * The label is automatically centered in the plot margin however the user can override the position and formatting
	 * by setting options in {@link Text}
	 * 
	 * @param arg
	 *            string to use as text label
	 * @see #getAxisLabelText()
	 ******************************************************************************************************************/
	public void setAxisLabelText(gov.epa.mims.analysisengine.tree.Text arg) {
		if (arg != null) {
			this.axisLabelText = (Text) arg.clone();
		} else {
			this.axisLabelText = null;
		}
	}

	/*******************************************************************************************************************
	 * retrieve the axis label Text object
	 * 
	 * @return {@link #axisLabelText}
	 * @see #setAxisLabelText(gov.epa.mims.analysisengine.tree.Text arg)
	 ******************************************************************************************************************/
	public gov.epa.mims.analysisengine.tree.Text getAxisLabelText() {
		return (axisLabelText != null) ? (Text) axisLabelText.clone() : null;
	}

	/*******************************************************************************************************************
	 * set the Axis Range; an abstract class which subclasses must implement
	 * 
	 * @param min
	 *            min point in axis range
	 * @param max
	 *            max point in axis range
	 ******************************************************************************************************************/
	public abstract void setAxisRange(Object min, Object max);

	/*******************************************************************************************************************
	 * get the Axis Range; an abstract class which subclasses must implement
	 * 
	 * @return axis range
	 ******************************************************************************************************************/
	public abstract Object[] getAxisRange();

	/*******************************************************************************************************************
	 * set drawTickMarkLabels flag
	 * <p>
	 * set {@link #drawTickMarkLabels}
	 * <ul>
	 * <li>true - draw the axis tick mark labels
	 * <li>false - do not draw the axis tick mark labels
	 * </ul>
	 * 
	 * @param arg
	 *            drawTickMarkLabels flag
	 * @see #getDrawTickMarkLabels()
	 ******************************************************************************************************************/
	public void setDrawTickMarkLabels(boolean arg) {
		this.drawTickMarkLabels = arg;
	}

	/*******************************************************************************************************************
	 * retrieve drawTickMarkLabels flag
	 * <ul>
	 * <li>true - draw the axis tick mark labels
	 * <li>false - do not draw the axis tick mark labels
	 * </ul>
	 * 
	 * @return {@link #drawTickMarkLabels}
	 * @see #setDrawTickMarkLabels(boolean arg)
	 ******************************************************************************************************************/
	public boolean getDrawTickMarkLabels() {
		return drawTickMarkLabels;
	}

	/*******************************************************************************************************************
	 * set the orientation of the tick mark labels
	 * <p>
	 * "false" causes the labels to be displayed parallel to the axis
	 * <p>
	 * <IMG BORDER="3" SRC="doc-files/Axis1.jpg">
	 * <p>
	 * "true" causes the labels to be displayed perpendicular to the axis
	 * <p>
	 * <IMG BORDER="3" SRC="doc-files/Axis2.jpg">
	 * 
	 * @param arg
	 *            true->perpendicular to the axis; false->parallel to the axis
	 * @see #getDrawTickMarkLabelsPerpendicularToAxis()
	 ******************************************************************************************************************/
	public void setDrawTickMarkLabelsPerpendicularToAxis(boolean arg) {
		this.drawTickMarkLabelsPerpendicularToAxis = arg;
	}

	/*******************************************************************************************************************
	 * retrieve the orientation of the tick mark labels
	 * <ul>
	 * <li>true - the labels to be displayed perpendicular to the axis
	 * <li>false - the labels to be displayed parallel to the axis
	 * </ul>
	 * 
	 * @return {@link #drawTickMarkLabelsPerpendicularToAxis}
	 * @see #setDrawTickMarkLabelsPerpendicularToAxis(boolean arg)
	 ******************************************************************************************************************/
	public boolean getDrawTickMarkLabelsPerpendicularToAxis() {
		return drawTickMarkLabelsPerpendicularToAxis;
	}

	/*******************************************************************************************************************
	 * set drawTickMarks flag
	 * <ul>
	 * <li>true - draw the axis tick marks
	 * <p>
	 * <IMG BORDER="3" SRC="doc-files/Axis3.jpg">
	 * <li>false - do NOT draw the axis tick marks
	 * <p>
	 * <IMG BORDER="3" SRC="doc-files/Axis6.jpg">
	 * </ul>
	 * 
	 * @param arg
	 *            true->draw the axis tick marks; false->do not draw the axis tick marks
	 * @see #getDrawTickMarks()
	 ******************************************************************************************************************/
	public void setDrawTickMarks(boolean arg) {
		this.drawTickMarks = arg;
	}

	/*******************************************************************************************************************
	 * retrieve drawTickMarks flag
	 * <ul>
	 * <li>true - draw the axis tick marks
	 * <li>false - do NOT draw the axis tick marks
	 * </ul>
	 * 
	 * @return {@link #drawTickMarks}
	 * @see #setDrawTickMarks(boolean arg)
	 ******************************************************************************************************************/
	public boolean getDrawTickMarks() {
		return drawTickMarks;
	}

	/*******************************************************************************************************************
	 * set the axis enable flag
	 * <ul>
	 * <li>true = draw axis
	 * <li>false = do not draw axis
	 * </ul>
	 * 
	 * @param flg
	 *            true->draw; false->do not draw
	 * @see #getEnableAxis()
	 ******************************************************************************************************************/
	public void setEnableAxis(boolean flg) {
		this.enableAxis = flg;
	}

	/*******************************************************************************************************************
	 * get the axis enable flag
	 * <ul>
	 * <li>true = draw axis
	 * <li>false = do not draw axis
	 * </ul>
	 * 
	 * @return {@link #enableAxis}
	 * @see #setEnableAxis(boolean flg)
	 ******************************************************************************************************************/
	public boolean getEnableAxis() {
		return this.enableAxis;
	}

	/*******************************************************************************************************************
	 * set the positioning of the axis
	 * <p>
	 * if algorithm is:
	 * <ul>
	 * <li>{@link #DEFAULT_POSITIONING} then the default positioning is used
	 * <li>{@link #USER_COORDINATES} then location is in user coordinates Example of positioning the y-axis at -2.3 in
	 * user space <IMG BORDER="3" SRC="doc-files/Axis5.jpg">
	 * <li>{@link #LINES_INTO_MARGIN} then
	 * <ul>
	 * <li>positions the axis within the plot margin
	 * <li>Example of arg = 0.0
	 * <li><IMG BORDER="3" SRC="doc-files/Axis3.jpg">
	 * <li>Example of arg = 0.5
	 * <li><IMG BORDER="3" SRC="doc-files/Axis4.jpg">
	 * <li>Note that linesIntoMargin does not affect the axis label positioning. Also, negative values are allowed
	 * </ul>
	 * </ul>
	 * 
	 * @param algorithm
	 *            {@link #DEFAULT_POSITIONING} {@link #USER_COORDINATES} {@link #LINES_INTO_MARGIN}
	 * @param arg
	 *            the value of the Axis position
	 * @see #getPosition()
	 * @see #getPositioningAlgorithm()
	 ******************************************************************************************************************/
	public void setPosition(int algorithm, double arg) {
		this.positioningAlgorithm = algorithm;
		this.position = arg;
	}

	/*******************************************************************************************************************
	 * retrieve position set by user;
	 * 
	 * @return {@link #position}
	 * @see #setPosition(int algorithm,double arg)
	 ******************************************************************************************************************/
	public double getPosition() {
		return position;
	}

	/*******************************************************************************************************************
	 * retrieve positioning algorithm set by user
	 * 
	 * @return {@link #positioningAlgorithm}
	 * @see #setPosition(int algorithm,double arg)
	 ******************************************************************************************************************/
	public int getPositioningAlgorithm() {
		return positioningAlgorithm;
	}

	/*******************************************************************************************************************
	 * set tickMarkFont String
	 * <p>
	 * Valid values are:
	 * <p>
	 * {@link FontConstantsIfc#PLAIN_TEXT}
	 * <p>
	 * <IMG BORDER="3" SRC="doc-files/Axis7.jpg">
	 * <p>
	 * {@link FontConstantsIfc#BOLD_TEXT}
	 * <p>
	 * <IMG BORDER="3" SRC="doc-files/Axis8.jpg">
	 * <p>
	 * {@link FontConstantsIfc#ITALIC_TEXT}
	 * <p>
	 * <IMG BORDER="3" SRC="doc-files/Axis9.jpg">
	 * <p>
	 * {@link FontConstantsIfc#BOLD_ITALIC_TEXT}
	 * <p>
	 * <IMG BORDER="3" SRC="doc-files/Axis10.jpg">
	 * 
	 * @param arg
	 *            desired tick mark font
	 ******************************************************************************************************************/
	public void setTickMarkFont(java.lang.String arg) {
		this.tickMarkFont = arg;
	}

	/*******************************************************************************************************************
	 * retrieve tickMarkFont String
	 * 
	 * @return {@link #tickMarkFont}
	 * @see #setTickMarkFont(java.lang.String arg)
	 ******************************************************************************************************************/
	public java.lang.String getTickMarkFont() {
		return tickMarkFont;
	}

	/*******************************************************************************************************************
	 * set tickMarkLabelColor value
	 * <p>
	 * Example of Color.green tick mark labels
	 * <p>
	 * <IMG BORDER="3" SRC="doc-files/Axis1.jpg">
	 * 
	 * @param arg
	 *            desired color of tick mark labels
	 * @pre arg != null
	 ******************************************************************************************************************/
	public void setTickMarkLabelColor(java.awt.Color arg) {
		this.tickMarkLabelColor = arg;
	}

	/*******************************************************************************************************************
	 * retrieve the tickMarkLabelColor value
	 * 
	 * @return the color value set in setTickMarkLabelColor( java.awt.Color arg )
	 ******************************************************************************************************************/
	public java.awt.Color getTickMarkLabelColor() {
		return tickMarkLabelColor;
	}

	/*******************************************************************************************************************
	 * set tickMarkLabelExpansion value
	 * <p>
	 * causes tick mark labels to be scaled
	 * <p>
	 * Example: expansion = 0.75
	 * <p>
	 * <IMG BORDER="3" SRC="doc-files/AxisTickMarkLabelExpansion075.jpg">
	 * 
	 * <p>
	 * Example: expansion = 1.0
	 * <p>
	 * <IMG BORDER="3" SRC="doc-files/AxisTickMarkLabelExpansion10.jpg">
	 * 
	 * <p>
	 * Example: expansion = 1.5
	 * <p>
	 * <IMG BORDER="3" SRC="doc-files/AxisTickMarkLabelExpansion15.jpg">
	 * 
	 * @param arg
	 *            the desired tick mark label expansion coefficient
	 ******************************************************************************************************************/
	public void setTickMarkLabelExpansion(double arg) {
		this.tickMarkLabelExpansion = arg;
	}

	/*******************************************************************************************************************
	 * get tickMarkLabelExpansion value
	 * 
	 * @return tick mark label expansion coefficient
	 ******************************************************************************************************************/
	public double getTickMarkLabelExpansion() {
		return tickMarkLabelExpansion;
	}

	/*******************************************************************************************************************
	 * set userTickMarkLabels array
	 * 
	 * @pre arg != null
	 * @param arg
	 *            String array of user selected tick mark labels
	 ******************************************************************************************************************/
	public void setUserTickMarkLabels(java.lang.String[] arg) {
		this.userTickMarkLabels = (String[]) arg.clone();
	}

	/*******************************************************************************************************************
	 * get userTickMarkLabels array
	 * 
	 * @return String array of user selected tick mark labels
	 ******************************************************************************************************************/
	public java.lang.String[] getUserTickMarkLabels() {
		String[] tmp = userTickMarkLabels;

		return (tmp != null) ? (String[]) tmp.clone() : null;
	}

	/*******************************************************************************************************************
	 * set userTickMarkPositions array
	 * <p>
	 * NOT IMPLEMENTED
	 * 
	 * @pre arg != null
	 * @param arg
	 *            array of user selected tick mark positions
	 ******************************************************************************************************************/
	public void setUserTickMarkPositions(double[] arg) {
		this.userTickMarkPositions = (double[]) arg.clone();
	}

	/*******************************************************************************************************************
	 * get userTickMarkPositions array
	 * 
	 * @return array of user selected tick mark positions
	 * @pre userTickMarkPositions != null
	 ******************************************************************************************************************/
	public double[] getUserTickMarkPositions() {
		double[] rtrn = null;

		if (userTickMarkPositions != null) {
			rtrn = (double[]) userTickMarkPositions.clone();
		}

		return rtrn;
	}

	/*******************************************************************************************************************
	 * Creates and returns a copy of this object
	 * 
	 * @return a copy of this object
	 * @throws CloneNotSupportedException
	 *             is not cloneable
	 ******************************************************************************************************************/
	public Object clone() throws CloneNotSupportedException {
		Axis clone = (Axis) super.clone();

		clone.axisLabelText = (clone.axisLabelText == null) ? null : (Text) axisLabelText.clone();

		double[] tmp = (clone.userTickMarkPositions == null) ? null : (double[]) userTickMarkPositions.clone();
		clone.userTickMarkPositions = tmp;

		clone.userTickMarkLabels = (clone.userTickMarkLabels == null) ? null : (String[]) userTickMarkLabels.clone();

		return clone;
	}

	/*******************************************************************************************************************
	 * Compares this object to the specified object.
	 * 
	 * @param o
	 *            the object to compare this object against
	 * 
	 * @return true if the objects are equal; false otherwise
	 ******************************************************************************************************************/
	public boolean equals(Object o) {
		boolean rtrn = true;

		if (o == null) {
			rtrn = false;
		} else if (o == this) {
			rtrn = true;
		} else if (o.getClass() != getClass()) {
			rtrn = false;
		} else {
			Axis other = (Axis) o;

			boolean b = drawTickMarkLabelsPerpendicularToAxis;
			boolean bOther = other.drawTickMarkLabelsPerpendicularToAxis;
			rtrn = ((position == other.position)
					&& (positioningAlgorithm == other.positioningAlgorithm)
					&& (enableAxis == other.enableAxis)
					&& (axisLabelText.equals(other.axisLabelText))
					&& ((axisColor == null) ? (other.axisColor == null) : (axisColor.equals(other.axisColor)))
					&& (drawTickMarks == other.drawTickMarks)
					&& (drawTickMarkLabels == other.drawTickMarkLabels)
					&& (b == bOther)
					&& ((tickMarkLabelColor == null) ? (other.tickMarkLabelColor == null) : (tickMarkLabelColor
							.equals(other.tickMarkLabelColor)))
					&& ((tickMarkFont == null) ? (other.tickMarkFont == null) : (tickMarkFont
							.equals(other.tickMarkFont))) && (tickMarkLabelExpansion == other.tickMarkLabelExpansion)
					&& Util.equals(userTickMarkPositions, other.userTickMarkPositions) && Util.equals(
					userTickMarkLabels, other.userTickMarkLabels));
		}

		return rtrn;
	}

	/*******************************************************************************************************************
	 * describe object in a String
	 * 
	 * @return String describing object
	 ******************************************************************************************************************/
	public String toString() {
		return Util.toString(this);
	}

	/*******************************************************************************************************************
	 * DOCUMENT_ME
	 * 
	 * @return DOCUMENT_ME
	 ******************************************************************************************************************/
	public String getTextString() {
		if (axisLabelText == null) {
			return null;
		}
		return axisLabelText.getTextString();
	}

	/*******************************************************************************************************************
	 * DOCUMENT_ME
	 * 
	 * @param textString
	 *            DOCUMENT_ME
	 ******************************************************************************************************************/
	public void setTextString(String textString) {
		if (axisLabelText == null) {
			axisLabelText = new Text();
		}

		axisLabelText.setTextString(textString);
	}
}