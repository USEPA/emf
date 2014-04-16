package gov.epa.mims.analysisengine.tree;

import java.awt.Color;

import java.io.Serializable;

/**
 * An analysis option which describes histogram plot. It is passed to
 * {@link AnalysisOptions#addOption(java.lang.String key, java.lang.Object obj) }
 * <p>
 * Elided Code Example:
 * 
 * <pre>
 *     :
 *     :
 *  String aHISTOGRAMTYPE = AnalysisEngineConstants.HISTOGRAM_TYPE;
 * 
 *  AnalysisOptions options = new AnalysisOptions();
 *  options.addOption(aHISTOGRAMTYPE, initHistogramType());
 *     :
 *     :
 *  private HistogramType initHistogramType()
 *  {
 *     HistogramType histogramType = new HistogramType();
 *     histogramType.setColor(java.awt.Color.yellow);
 *     histogramType.setFrequency(false);
 *     histogramType.setBorderColor(java.awt.Color.black);
 *     histogramType.setShadingAngle(null);
 *     histogramType.setShadingDensity(null);
 *     histogramType.setLinetype(HistogramType.SOLID);
 *     histogramType.setXRange(null,null);
 *     histogramType.setLabelsOn(false);
 * 
 *     return histogramType;
 *  }
 * 
 * </pre>
 * 
 * <p>
 * <A HREF="doc-files/ExampleHistogramPlot01.html"> <B>View an example</B></A>
 * 
 * @author Tommy E. Cathey
 * @version $Id: HistogramType.java,v 1.3 2006/10/30 21:43:50 parthee Exp $
 * 
 */
public class HistogramType extends AnalysisOption implements Serializable, Cloneable, LineTypeConstantsIfc {
	/*******************************************************************************************************************
	 * 
	 * fields
	 * 
	 ******************************************************************************************************************/
	/**
	 * serial version UID
	 */
	static final long serialVersionUID = 1;

	/**
	 * constant used as input to {@link HistogramType#setClosure(boolean)}
	 */
	private static final boolean RIGHT = true;

	/**
	 * Break Algorithm <br>
	 * Use as argument in {@link HistogramType#setBreaks(Object)}
	 */
	public static final String STURGES = "Sturges";

	/**
	 * Break Algorithm <br>
	 * Use as argument in {@link HistogramType#setBreaks(Object)}
	 */
	public static final String SCOTT = "Scott";

	/**
	 * Break Algorithm <br>
	 * Use as argument in {@link HistogramType#setBreaks(Object)}
	 */
	public static final String FD = "FD";

	/**
	 * color of the bar lines
	 */
	private Color borderColor = Color.black;

	/**
	 * color of the bars
	 */
	private Color color = Color.green;

	/**
	 * shading
	 */
	private Double shadingAngle = null;

	/**
	 * shading
	 */
	private Double shadingDensity = null;

	/**
	 * break points
	 */
	private Object breaks = null;

	/**
	 * line type of bar lines
	 */
	private String linetype = null;

	/**
	 * x range
	 */
	private double[] xRange = null;

	/**
	 * closure if TRUE, the histograms cells are right-closed intervals (default) if FALSE, the histograms cells are
	 * left-closed intervals
	 */
	private boolean closure = HistogramType.RIGHT;

	/**
	 * <ul>
	 * <li>true - plot frequencies (default)
	 * <li>false - plot relative frequencies or probabilities
	 * </ul>
	 * 
	 */
	private boolean frequency = true;

	/**
	 * includeLowest if TRUE, an `x[i]' equal to the `breaks' value will be included in the first (or last, for closure =
	 * HistogramType.LEFT) bar. This will be ignored unless breaks is a vector.
	 */
	private boolean includeLowest = HistogramType.RIGHT;

	/**
	 * <ul>
	 * <li>true - draw labels above the bars
	 * <li>false - DO NOT draw labels
	 * </ul>
	 * 
	 */
	private boolean labelsOn = false;

	/*******************************************************************************************************************
	 * set color of bar border color
	 * <p>
	 * <A HREF="doc-files/ExampleHistogramPlot06.html"> <B>View an example</B></A> <br>
	 * <A HREF="doc-files/ExampleHistogramPlot07.html"> <B>View an example</B></A>
	 * 
	 * @param arg
	 *            color of bar border color
	 ******************************************************************************************************************/
	public void setBorderColor(java.awt.Color arg) {
		this.borderColor = arg;
	}

	/*******************************************************************************************************************
	 * set color of bar border color
	 * 
	 * @return color of bar border color
	 ******************************************************************************************************************/
	public java.awt.Color getBorderColor() {
		return borderColor;
	}

	/*******************************************************************************************************************
	 * set breaks points of histogram
	 * <ul>
	 * <li>double[] giving the breakpoints between histogram cells
	 * <li>In the following last three cases the argument is a suggestion only.
	 * <ul>
	 * <li>Integer suggesting the number of cells for the histogram.
	 * <li>String suggesting an R function to compute the number of cells.
	 * <li>String suggesting an algorithm to compute the number of cells.
	 * <ol>
	 * <li>{@link HistogramType#STURGES} <br>
	 * Sturges' formula implicitly basing bin sizes on the range of the data.
	 * <li>{@link HistogramType#SCOTT} <br>
	 * normal distribution based on the estimate of the standard error.
	 * <li>{@link HistogramType#FD} <br>
	 * uses the Freedman-Diaconis choice based on the inter-quartile range.
	 * </ol>
	 * </ul>
	 * </ul>
	 * <ul>
	 * 
	 * @param arg
	 *            breaks points of histogram
	 ******************************************************************************************************************/
	public void setBreaks(java.lang.Object arg) {
		this.breaks = arg;
	}

	/*******************************************************************************************************************
	 * retrieve breaks points of histogram
	 * <ul>
	 * <li>double[] giving the breakpoints between histogram cells
	 * <li>In the following last three cases the argument is a suggestion only.
	 * <ul>
	 * <li>Integer suggesting the number of cells for the histogram.
	 * <li>String suggesting an R function to compute the number of cells.
	 * <li>String suggesting an algorithm to compute the number of cells.
	 * <ol>
	 * <li>{@link HistogramType#STURGES} <br>
	 * Sturges' formula implicitly basing bin sizes on the range of the data.
	 * <li>{@link HistogramType#SCOTT} <br>
	 * normal distribution based on the estimate of the standard error.
	 * <li>{@link HistogramType#FD} <br>
	 * uses the Freedman-Diaconis choice based on the inter-quartile range.
	 * </ol>
	 * </ul>
	 * </ul>
	 * 
	 * @return breaks points of histogram
	 ******************************************************************************************************************/
	public java.lang.Object getBreaks() {
		return breaks;
	}

	/*******************************************************************************************************************
	 * set closure if TRUE, the histograms cells are right-closed intervals if FALSE, the histograms cells are
	 * left-closed intervals
	 * 
	 * @param arg
	 *            closure flag
	 ******************************************************************************************************************/
	public void setClosure(boolean arg) {
		this.closure = arg;
	}

	/*******************************************************************************************************************
	 * retrieve closure if TRUE, the histograms cells are right-closed intervals if FALSE, the histograms cells are
	 * left-closed intervals
	 * 
	 * @return closure flag
	 ******************************************************************************************************************/
	public boolean getClosure() {
		return closure;
	}

	/*******************************************************************************************************************
	 * set color of bars
	 * <p>
	 * <A HREF="doc-files/ExampleHistogramPlot04.html"> <B>View an example</B></A> <br>
	 * <A HREF="doc-files/ExampleHistogramPlot05.html"> <B>View an example</B></A>
	 * 
	 * @param arg
	 *            color of bars
	 ******************************************************************************************************************/
	public void setColor(java.awt.Color arg) {
		this.color = arg;
	}

	/*******************************************************************************************************************
	 * get color of bars
	 * 
	 * @return color of bars
	 ******************************************************************************************************************/
	public java.awt.Color getColor() {
		return color;
	}

	/*******************************************************************************************************************
	 * set plot frequencies or probabilities flag
	 * <p>
	 * <ul>
	 * <li>true - plot frequencies (default)
	 * <li>false - plot relative frequencies or probabilities
	 * </ul>
	 * <p>
	 * <A HREF="doc-files/ExampleHistogramPlot02.html"> <B>View an example</B></A> <br>
	 * <A HREF="doc-files/ExampleHistogramPlot03.html"> <B>View an example</B></A>
	 * 
	 * @param arg
	 *            plot type flag
	 ******************************************************************************************************************/
	public void setFrequency(boolean arg) {
		this.frequency = arg;
	}

	/*******************************************************************************************************************
	 * get plot frequencies or probabilities flag
	 * <p>
	 * <ul>
	 * <li>true - plot frequencies (default)
	 * <li>false - plot relative frequencies or probabilities
	 * </ul>
	 * 
	 * @return plot type flag
	 ******************************************************************************************************************/
	public boolean getFrequency() {
		return frequency;
	}

	/*******************************************************************************************************************
	 * set includeLowest flag if TRUE, an `x[i]' equal to the `breaks' value will be included in the first (or last, for
	 * closure = HistogramType.LEFT) bar. This will be ignored unless breaks is a vector.
	 * 
	 * @param arg
	 *            includeLowest flag
	 ******************************************************************************************************************/
	public void setIncludeLowest(boolean arg) {
		this.includeLowest = arg;
	}

	/*******************************************************************************************************************
	 * retrieve includeLowest flag if TRUE, an `x[i]' equal to the `breaks' value will be included in the first (or
	 * last, for closure = HistogramType.LEFT) bar. This will be ignored unless breaks is a vector.
	 * 
	 * @return includeLowest flag
	 ******************************************************************************************************************/
	public boolean getIncludeLowest() {
		return includeLowest;
	}

	/*******************************************************************************************************************
	 * set labels on flag
	 * <p>
	 * <A HREF="doc-files/ExampleHistogramPlot15.html"> <B>View an example</B></A> <br>
	 * <A HREF="doc-files/ExampleHistogramPlot16.html"> <B>View an example</B></A>
	 * 
	 * @param arg
	 *            labels on flag
	 ******************************************************************************************************************/
	public void setLabelsOn(boolean arg) {
		this.labelsOn = arg;
	}

	/*******************************************************************************************************************
	 * get labels on flag
	 * 
	 * @return labels on flag
	 ******************************************************************************************************************/
	public boolean getLabelsOn() {
		return labelsOn;
	}

	/*******************************************************************************************************************
	 * set linetype of bar borders
	 * <p>
	 * <A HREF="doc-files/ExampleHistogramPlot12.html"> <B>View an example</B></A> <br>
	 * <A HREF="doc-files/ExampleHistogramPlot13.html"> <B>View an example</B></A>
	 * 
	 * @param arg
	 *            linetype of bar borders
	 ******************************************************************************************************************/
	public void setLinetype(java.lang.String arg) {
		this.linetype = arg;
	}

	/*******************************************************************************************************************
	 * get linetype of bar borders
	 * 
	 * @return linetype of bar borders
	 ******************************************************************************************************************/
	public java.lang.String getLinetype() {
		return linetype;
	}

	/*******************************************************************************************************************
	 * set shading angle when shading histogram
	 * <p>
	 * <A HREF="doc-files/ExampleHistogramPlot08.html"> <B>View an example</B></A> <br>
	 * <A HREF="doc-files/ExampleHistogramPlot09.html"> <B>View an example</B></A>
	 * 
	 * @param arg
	 *            shading angle
	 ******************************************************************************************************************/
	public void setShadingAngle(java.lang.Double arg) {
		this.shadingAngle = arg;
	}

	/*******************************************************************************************************************
	 * get shading angle when shading histogram
	 * 
	 * @return shading angle
	 ******************************************************************************************************************/
	public java.lang.Double getShadingAngle() {
		return shadingAngle;
	}

	/*******************************************************************************************************************
	 * set shading density when shading histogram
	 * <p>
	 * <A HREF="doc-files/ExampleHistogramPlot10.html"> <B>View an example</B></A> <br>
	 * <A HREF="doc-files/ExampleHistogramPlot11.html"> <B>View an example</B></A>
	 * 
	 * @param arg
	 *            shading density
	 ******************************************************************************************************************/
	public void setShadingDensity(java.lang.Double arg) {
		this.shadingDensity = arg;
	}

	/*******************************************************************************************************************
	 * get shading density when shading histogram
	 * 
	 * @return shading density
	 ******************************************************************************************************************/
	public java.lang.Double getShadingDensity() {
		return shadingDensity;
	}

	/*******************************************************************************************************************
	 * set X Range of histogram
	 * <p>
	 * <A HREF="doc-files/ExampleHistogramPlot14.html"> <B>View an example</B></A>
	 * 
	 * @param min
	 *            lower bound of X range
	 * @param max
	 *            upper bound of X range
	 ******************************************************************************************************************/
	public void setXRange(Double min, Double max) {
		if ((min == null) || (max == null)) {
			this.xRange = null;
		} else {
			this.xRange = new double[2];
			this.xRange[0] = min.doubleValue();
			this.xRange[1] = max.doubleValue();
		}
	}

	/*******************************************************************************************************************
	 * get X Range of histogram
	 * 
	 * @return X Range of histogram
	 ******************************************************************************************************************/
	public double[] getXRange() {
		double[] rtrn;

		if (xRange == null) {
			rtrn = xRange;
		} else {
			rtrn = new double[2];
			rtrn[0] = xRange[0];
			rtrn[1] = xRange[1];
		}

		return rtrn;
	}

	/*******************************************************************************************************************
	 * Creates and returns a copy of this object
	 * 
	 * @return a copy of this object
	 ******************************************************************************************************************/
	public Object clone() {
		try {
			HistogramType clone = (HistogramType) super.clone();

			return clone;
		} catch (CloneNotSupportedException e) {
			return null;
		}
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
			HistogramType other = (HistogramType) o;

			rtrn = (frequency == other.frequency)
					&& ((color == null) ? (other.color == null) : (color.equals(other.color)))
					&& ((borderColor == null) ? (other.borderColor == null) : (borderColor.equals(other.borderColor)))
					&& ((shadingAngle == null) ? (other.shadingAngle == null) : (shadingAngle
							.equals(other.shadingAngle)))
					&& ((shadingDensity == null) ? (other.shadingDensity == null) : (shadingDensity
							.equals(other.shadingDensity)))
					&& ((linetype == null) ? (other.linetype == null) : (linetype.equals(other.linetype)))
					&& ((xRange == null) ? (other.xRange == null) : (xRange[0] == other.xRange[0])
							&& (xRange[1] == other.xRange[1])) && (labelsOn == other.labelsOn);
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
}