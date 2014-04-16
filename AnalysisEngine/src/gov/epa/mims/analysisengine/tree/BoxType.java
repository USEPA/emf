package gov.epa.mims.analysisengine.tree;

import java.awt.Color;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

/**
 * <p>
 * Elided Code Example:
 * 
 * <pre>
 *     :
 *     :
 *  String aBOX_TYPE = BOX_TYPE;
 *     :
 *     :
 *  options.addOption(aBOX_TYPE, initBoxType());
 *     :
 *     :
 *  private BoxType initBoxType()
 *  {
 *     BoxType boxType = new BoxType();
 *     
 *     boxType.setColor(new Color[]{Color.red, Color.green,Color.yellow,Color.blue,Color.orange});
 *     boxType.setBorderColor(Color.gray);
 *     boxType.setHorizontal(false);
 *     //boxType.setWidth([D);
 *     boxType.setNotch(true);
 *     boxType.setVarwidths(true);
 *     //boxType.setOutliers(boolean);
 *     //boxType.setBoxwex([D);
 *     boxType.setNotchFrac(0.5);
 *     boxType.setRange(3.5);
 *     //boxType.setLwd(double);
 *     boxType.setAt(new double[]{1.0,1.5,2.0,3.0,4.0});
 * 
 *     return boxType;
 *  }
 *     :
 *     :
 * 
 * </pre>
 * 
 * <br>
 * <A HREF="doc-files/ExampleBoxPlot00.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleBoxPlot01.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleBoxPlot02.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleBoxPlot03.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleBoxPlot04.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleBoxPlot05.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleBoxPlot06.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleBoxPlot07.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleBoxPlot08.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleBoxPlot20.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleBoxPlot50.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleBoxPlot51.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleBoxPlot52.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleBoxPlot53.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleBoxPlot54.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleBoxPlot55.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleBoxPlot56.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleBoxPlot57.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleBoxPlot58.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleBoxPlot59.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleBoxPlot60.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleBoxPlot61.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleBoxPlot62.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleBoxPlot63.html"><B>View Example</B></A> <br>
 * <A HREF="doc-files/ExampleBoxPlot64.html"><B>View Example</B></A>
 * 
 * 
 * @author Tommy E. Cathey
 * @version $Id: BoxType.java,v 1.3 2007/01/09 23:06:15 parthee Exp $
 * 
 */
public class BoxType extends AnalysisOption implements Serializable, Cloneable, BoxPlotConstantsIfc,
		AnalysisOptionConstantsIfc, ColorConstantsIfc {
	/*******************************************************************************************************************
	 * 
	 * fields
	 * 
	 ******************************************************************************************************************/
	static final long serialVersionUID = 1;

	/** color for the box borders */
	private Color borderColor;

	/** color of the box used cyclically */
	private Color[] color = DEFAULT_COLORS;

	/** the widths of the box used cyclically */
	private double[] width;

	/** flag to generate notched boxes */
	private boolean notch = false;

	/**
	 * flag to generate boxes of variable widths based on the number of observations
	 */
	private boolean varwidths = false;

	/** flag to draw outliers */
	private boolean outliers = true;

	/** flag to draw horizontal box plots */
	private boolean horizontal = false;

	/** set processing type */
	private int processing = BoxPlotConstantsIfc.USE_R;

	/** box expansion factors */
	private double[] boxwex = null;

	/** notch width as a faction of the box width */
	private double notchFrac = 0.5;

	/** line width to draw the boxes */
	private double lwd = 1.0;

	/** range of whiskers */
	private double range = 1.5;

	/** user selected locations for the boxes */
	private double[] at = null;

	/** flag to reverse the order of the boxes */
	private boolean reversePlotOrder = false;

	/** DOCUMENT_ME */
	private HashMap customPercentiles = null;

	/*******************************************************************************************************************
	 * set the border color of the box plots
	 * <p>
	 * <br>
	 * <A HREF="doc-files/ExampleBoxPlot08.html"><B>View Example</B></A> <br>
	 * <A HREF="doc-files/ExampleBoxPlot06.html"><B>View Example</B></A>
	 * 
	 * @param arg
	 *            color
	 ******************************************************************************************************************/
	public void setBorderColor(java.awt.Color arg) {
		this.borderColor = (arg != null) ? arg : null;
	}

	/*******************************************************************************************************************
	 * retrieve the box plot border color
	 * 
	 * @return box plot border color
	 ******************************************************************************************************************/
	public java.awt.Color getBorderColor() {
		return borderColor;
	}

	/*******************************************************************************************************************
	 * set cyclic colors for box plot interiors
	 * <p>
	 * <br>
	 * <A HREF="doc-files/ExampleBoxPlot55.html"><B>View Example</B></A>
	 * 
	 * @param arg
	 *            cyclic colors for box plot interiors
	 ******************************************************************************************************************/
	public void setColor(java.awt.Color[] arg) {
		this.color = (arg != null) ? arg : null;
	}

	/*******************************************************************************************************************
	 * retrieve cyclic colors for box plot interiors
	 * 
	 * @return cyclic colors for box plot interiors
	 ******************************************************************************************************************/
	public java.awt.Color[] getColor() {
		return (color != null) ? (Color[]) color.clone() : null;
	}

	/*******************************************************************************************************************
	 * set processing;
	 * <p>
	 * <br>
	 * <ul>
	 * <li> BoxPlotConstantsIfc.PRECOMPUTED
	 * <li> BoxPlotConstantsIfc.CUSTOM
	 * <li> BoxPlotConstantsIfc.PRECOMPUTED
	 * </ul>
	 * 
	 * @param processing
	 *            processing type
	 ******************************************************************************************************************/
	public void setProcessing(int processing) {
		this.processing = processing;
	}

	/*******************************************************************************************************************
	 * retrieve processing type
	 * 
	 * @return
	 ******************************************************************************************************************/
	public int getProcessing() {
		return processing;
	}

	/*******************************************************************************************************************
	 * set the custom percentiles used when custom processing is set in setProcessing(BoxPlotConstantsIfc.CUSTOM)
	 * <p>
	 * <br>
	 * the following keys maybe used in customPercentiles
	 * <ul>
	 * <li> BoxPlotConstantsIfc.LOWER_WHISKER
	 * <li> BoxPlotConstantsIfc.LOWER_HINGE
	 * <li> BoxPlotConstantsIfc.MEDIAN
	 * <li> BoxPlotConstantsIfc.UPPER_HINGE
	 * <li> BoxPlotConstantsIfc.UPPER_WHISKER
	 * <li> BoxPlotConstantsIfc.LOWER_NOTCH_EXTREME
	 * <li> BoxPlotConstantsIfc.UPPER_NOTCH_EXTREME
	 * </ul>
	 * 
	 * @param customPercentiles
	 *            percentiles
	 ******************************************************************************************************************/
	public void setCustomPercentiles(HashMap customPercentiles) {
		this.customPercentiles = customPercentiles;
	}

	/*******************************************************************************************************************
	 * retrieve processing type
	 * 
	 * @return
	 ******************************************************************************************************************/
	public HashMap getCustomPercentiles() {
		// return customPercentiles;
		return (HashMap) customPercentiles.clone();
	}

	/*******************************************************************************************************************
	 * set horizontal flag;
	 * <p>
	 * <br>
	 * <ul>
	 * <li> default = false;
	 * <li> true = horizontal <br>
	 * <A HREF="doc-files/ExampleBoxPlot05.html"><B>View Example</B></A>
	 * <li> false = vertical <br>
	 * <A HREF="doc-files/ExampleBoxPlot06.html"><B>View Example</B></A>
	 * </ul>
	 * 
	 * @param arg
	 *            user selected orientation flag
	 ******************************************************************************************************************/
	public void setHorizontal(boolean arg) {
		this.horizontal = arg;
	}

	/*******************************************************************************************************************
	 * retrieve horizontal flag
	 * 
	 * @return true->horizontal; false->vertical
	 ******************************************************************************************************************/
	public boolean getHorizontal() {
		return horizontal;
	}

	/*******************************************************************************************************************
	 * set the widths of the box plots
	 * <p>
	 * <br>
	 * <A HREF="doc-files/ExampleBoxPlot56.html"><B>View Example</B></A>
	 * 
	 * @param arg
	 *            widths of the box plots
	 ******************************************************************************************************************/
	public void setWidth(double[] arg) {
		this.width = (arg != null) ? arg : null;
	}

	/*******************************************************************************************************************
	 * retrieve the widths of the box plots
	 * 
	 * @return widths of the box plots
	 ******************************************************************************************************************/
	public double[] getWidth() {
		return (width != null) ? (double[]) width.clone() : null;
	}

	/*******************************************************************************************************************
	 * Creates and returns a copy of this object
	 * 
	 * @return a copy of this object
	 ******************************************************************************************************************/
	public Object clone() {
		try {
			BoxType clone = (BoxType) super.clone();
			clone.color = (color == null) ? null : (Color[]) color.clone();
			clone.width = (width == null) ? null : (double[]) width.clone();
			clone.boxwex = (boxwex == null) ? null : (double[]) boxwex.clone();
			clone.at = (at == null) ? null : (double[]) at.clone();

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
			BoxType other = (BoxType) o;

			rtrn = Util.equals(width, other.width) && Util.equals(borderColor, other.borderColor)
					&& Util.equals(color, other.color) && Util.equals(notch, other.notch)
					&& Util.equals(varwidths, other.varwidths) && Util.equals(outliers, other.outliers)
					&& Util.equals(horizontal, other.horizontal) && Util.equals(boxwex, other.boxwex)
					&& Util.equals(notchFrac, other.notchFrac) && Util.equals(lwd, other.lwd)
					&& Util.equals(at, other.at) && Util.equals(range, other.range)
					&& Util.equals(reversePlotOrder, other.reversePlotOrder);
		}

		return rtrn;
	}

	/*******************************************************************************************************************
	 * retrieve the notch flag
	 * 
	 * @return notch flag
	 ******************************************************************************************************************/
	public boolean getNotch() {
		return notch;
	}

	/*******************************************************************************************************************
	 * set the notch flag
	 * <p>
	 * <br>
	 * <ul>
	 * <li>true - draw notched boxes <br>
	 * <A HREF="doc-files/ExampleBoxPlot62.html"><B>View Example</B></A>
	 * <li>false - draw rectangular boxes <br>
	 * <A HREF="doc-files/ExampleBoxPlot53.html"><B>View Example</B></A>
	 * </ul>
	 * <br>
	 * 
	 * @param arg
	 *            notch flag
	 ******************************************************************************************************************/
	public void setNotch(boolean arg) {
		this.notch = arg;
	}

	/*******************************************************************************************************************
	 * retrieve the variable widths flag
	 * 
	 * @return variable widths flag
	 ******************************************************************************************************************/
	public boolean getVarwidths() {
		return varwidths;
	}

	/*******************************************************************************************************************
	 * set the variable widths flag
	 * <p>
	 * <br>
	 * <ul>
	 * <li>true - the widths are determined by the number of data points <br>
	 * <A HREF="doc-files/ExampleBoxPlot08.html"><B>View Example</B></A> <br>
	 * <A HREF="doc-files/ExampleBoxPlot20.html"><B>View Example</B></A> <br>
	 * <A HREF="doc-files/ExampleBoxPlot54.html"><B>View Example</B></A> <br>
	 * <A HREF="doc-files/ExampleBoxPlot55.html"><B>View Example</B></A> <br>
	 * <A HREF="doc-files/ExampleBoxPlot56.html"><B>View Example</B></A> <br>
	 * <A HREF="doc-files/ExampleBoxPlot57.html"><B>View Example</B></A> <br>
	 * <A HREF="doc-files/ExampleBoxPlot58.html"><B>View Example</B></A> <br>
	 * <A HREF="doc-files/ExampleBoxPlot59.html"><B>View Example</B></A> <br>
	 * <A HREF="doc-files/ExampleBoxPlot60.html"><B>View Example</B></A> <br>
	 * <A HREF="doc-files/ExampleBoxPlot61.html"><B>View Example</B></A> <br>
	 * <A HREF="doc-files/ExampleBoxPlot62.html"><B>View Example</B></A> <br>
	 * <A HREF="doc-files/ExampleBoxPlot63.html"><B>View Example</B></A> <br>
	 * <A HREF="doc-files/ExampleBoxPlot64.html"><B>View Example</B></A>
	 * <li>false - the widths are uniform <br>
	 * <A HREF="doc-files/ExampleBoxPlot02.html"><B>View Example</B></A> <br>
	 * <A HREF="doc-files/ExampleBoxPlot03.html"><B>View Example</B></A> <br>
	 * <A HREF="doc-files/ExampleBoxPlot04.html"><B>View Example</B></A> <br>
	 * <A HREF="doc-files/ExampleBoxPlot05.html"><B>View Example</B></A> <br>
	 * <A HREF="doc-files/ExampleBoxPlot06.html"><B>View Example</B></A> <br>
	 * <A HREF="doc-files/ExampleBoxPlot07.html"><B>View Example</B></A>
	 * </ul>
	 * <br>
	 * 
	 * @param arg
	 *            variable widths flag
	 ******************************************************************************************************************/
	public void setVarwidths(boolean arg) {
		this.varwidths = arg;
	}

	/*******************************************************************************************************************
	 * retrieve the reverse order flag
	 * 
	 * @return reverse order flag
	 ******************************************************************************************************************/
	public boolean getReversePlotOrder() {
		return reversePlotOrder;
	}

	/*******************************************************************************************************************
	 * set reverse order flag
	 * <p>
	 * <br>
	 * <ul>
	 * <li>true - reverse the order of the box plots <br>
	 * <A HREF="doc-files/ExampleBoxPlot04.html"><B>View Example</B></A>
	 * </ul>
	 * <br>
	 * 
	 * @param arg
	 *            reverse order flag
	 ******************************************************************************************************************/
	public void setReversePlotOrder(boolean arg) {
		this.reversePlotOrder = arg;
	}

	/*******************************************************************************************************************
	 * retrieve the outlier flag
	 * 
	 * @return outlier flag
	 ******************************************************************************************************************/
	public boolean getOutliers() {
		return outliers;
	}

	/*******************************************************************************************************************
	 * set the outlier flag
	 * <p>
	 * <br>
	 * <ul>
	 * <li>true - draw the data outliers
	 * <li>false - do not draw the data outliers <br>
	 * <A HREF="doc-files/ExampleBoxPlot60.html"><B>View Example</B></A>
	 * </ul>
	 * <br>
	 * 
	 * @param arg
	 *            outlier flag
	 ******************************************************************************************************************/
	public void setOutliers(boolean arg) {
		this.outliers = arg;
	}

	/*******************************************************************************************************************
	 * retrieve the box expansion factors
	 * 
	 * @return box expansion factors
	 ******************************************************************************************************************/
	public double[] getBoxwex() {
		return (boxwex != null) ? (double[]) boxwex.clone() : null;
	}

	/*******************************************************************************************************************
	 * set the box expansion factors
	 * <p>
	 * <br>
	 * <A HREF="doc-files/ExampleBoxPlot60.html"><B>View Example</B></A>
	 * 
	 * @param arg
	 *            box expansion factors
	 ******************************************************************************************************************/
	public void setBoxwex(double[] arg) {
		this.boxwex = (arg != null) ? arg : null;
	}

	/*******************************************************************************************************************
	 * retrieve the notch fraction
	 * 
	 * @return notch fraction
	 ******************************************************************************************************************/
	public double getNotchFrac() {
		return notchFrac;
	}

	/*******************************************************************************************************************
	 * set the notch fraction
	 * <p>
	 * <br>
	 * <A HREF="doc-files/ExampleBoxPlot62.html"><B>View Example</B></A> <br>
	 * <A HREF="doc-files/ExampleBoxPlot60.html"><B>View Example</B></A>
	 * 
	 * @param arg
	 *            notch fraction
	 ******************************************************************************************************************/
	public void setNotchFrac(double arg) {
		this.notchFrac = arg;
	}

	/*******************************************************************************************************************
	 * retrieve the line width of the box plots
	 * 
	 * @return line width of the box plots
	 ******************************************************************************************************************/
	public double getLwd() {
		return lwd;
	}

	/*******************************************************************************************************************
	 * set the line width of the box plots
	 * <p>
	 * <br>
	 * <A HREF="doc-files/ExampleBoxPlot63.html"><B>View Example</B></A>
	 * 
	 * @param arg
	 *            line width of the box plots
	 ******************************************************************************************************************/
	public void setLwd(double arg) {
		this.lwd = arg;
	}

	/*******************************************************************************************************************
	 * retrieve the range of the whiskers
	 * 
	 * @return range of the whiskers
	 ******************************************************************************************************************/
	public double getRange() {
		return range;
	}

	/*******************************************************************************************************************
	 * set the range of the whiskers <br>
	 * <ul>
	 * from the R documentation:
	 * <li>determines how far the plot whiskers extend out from the box. If range is positive, the whiskers extend to
	 * the most extreme data point which is no more than range times the interquartile range from the box. A value of
	 * zero causes the whiskers to extend to the data extremes.
	 * <li>I could not get it to work. It could be a error in R itself.
	 * </ul>
	 * 
	 * @param arg
	 *            range of the whiskers
	 ******************************************************************************************************************/
	public void setRange(double arg) {
		this.range = arg;
	}

	/*******************************************************************************************************************
	 * retrieve the user selected locations of the box plots
	 * 
	 * @return user selected locations of the box plots
	 ******************************************************************************************************************/
	public double[] getAt() {
		return (at != null) ? (double[]) at.clone() : null;
	}

	/*******************************************************************************************************************
	 * set the user selected locations of the box plots
	 * <p>
	 * <br>
	 * <ul>
	 * <li> the length of arg must equal the number of boxes <br>
	 * <A HREF="doc-files/ExampleBoxPlot64.html"><B>View Example</B></A> <br>
	 * <A HREF="doc-files/ExampleBoxPlot20.html"><B>View Example</B></A>
	 * </ul>
	 * <br>
	 * 
	 * @param arg
	 *            user selected locations of the box plots
	 ******************************************************************************************************************/
	public void setAt(double[] arg) {
		this.at = (arg != null) ? arg : null;
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
	 * print class as a pretty String
	 * 
	 * @param separator
	 *            separator to separate the fields "\n" works well
	 * 
	 * @return class as a pretty String
	 ******************************************************************************************************************/
	public String toStringPretty(String separator) {
		StringBuffer b = new StringBuffer();
		String s = "" + borderColor;
		b.append("borderColor= " + s.substring(s.indexOf("[")));
		b.append(separator);
		b.append("color= ");

		for (int i = 0; i < color.length; i++) {
			String s2 = "" + color[i];
			b.append(s2.substring(s2.indexOf("[")));

			if (i < (color.length - 1)) {
				b.append(",  ");
			}
		}

		b.append(separator);

		if (width == null) {
			b.append("width= null");
			b.append(separator);
		} else {
			b.append("width= (");

			for (int i = 0; i < width.length; i++) {
				b.append("" + width[i]);

				if (i < (width.length - 1)) {
					b.append(", ");
				}
			}

			b.append(")");

			b.append(separator);
		}
		b.append("processing= " + processing);
		b.append(separator);

		if (customPercentiles == null) {
			b.append("customPercentiles= null");
			b.append(separator);
		} else {
			b.append("customPercentiles= ");

			Set keys = customPercentiles.keySet();
			Iterator keyIter = keys.iterator();
			while (keyIter.hasNext()) {
				String key = (String) keyIter.next();
				b.append(key + "=");
				b.append("" + customPercentiles.get(key) + ";");
			}

			b.append(separator);
		}

		b.append("notch= " + notch);
		b.append(separator);
		b.append("varwidths= " + varwidths);
		b.append(separator);
		b.append("outliers= " + outliers);
		b.append(separator);
		b.append("horizontal= " + horizontal);
		b.append(separator);

		if (boxwex == null) {
			b.append("boxwex= null");
			b.append(separator);
		} else {
			b.append("boxwex= ");

			for (int i = 0; i < boxwex.length; i++) {
				b.append("" + boxwex[i]);

				if (i < (boxwex.length - 1)) {
					b.append(", ");
				}
			}

			b.append(separator);
		}

		b.append("notchFrac= " + notchFrac);
		b.append(separator);
		b.append("lwd= " + lwd);
		b.append(separator);
		b.append("range= " + range + "  (NOTE: range does not apply to LabeledDataSetIfc data)");
		b.append(separator);
		b.append("reversePlotOrder= " + reversePlotOrder);
		b.append(separator);

		if (at == null) {
			b.append("at= null");
			b.append(separator);
		} else {
			b.append("at= ");

			for (int i = 0; i < at.length; i++) {
				b.append("" + at[i]);

				if (i < (at.length - 1)) {
					b.append(", ");
				}
			}

			b.append(separator);
		}

		return b.toString();
	}
}
