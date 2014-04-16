package gov.epa.mims.analysisengine.tree;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * describes lines and point symbols for a plot <br>
 * It is used as an argument in {@link AnalysisOptions#addOption(java.lang.String key, java.lang.Object obj) }
 * <p>
 * 
 * Elided Code Example:
 * 
 * <pre>
 *          :
 *          :
 *       String aLINETYPE = LINE_TYPE;
 *       AnalysisOptions options = new AnalysisOptions();
 *       options.addOption(aLINETYPE, initLineType());
 *          :
 *          :
 *       private LineType initLineType()
 *       {
 *          LineType lineType = new LineType();
 *          lineType.setPlotStyle(LineType.POINTS_N_LINES);
 *          lineType.setColor(
 *                new Color[]
 *          {
 *             Color.blue,
 *             Color.red,
 *             Color.yellow
 *          });
 *          lineType.setLineStyle(
 *                new String[]
 *          {
 *             &quot;SOLID&quot;,
 *             &quot;DASHED&quot;,
 *             &quot;DOTTED&quot;
 *          });
 *          lineType.setLineWidth(new double[]
 *          {
 *             2.5,
 *             3.3
 *          });
 *          lineType.setSymbol(new String[]
 *          {
 *             &quot;TRIANGLE_UP&quot;,
 *             &quot;CIRCLE&quot;
 *          });
 *          lineType.setSymbolExpansion(new double[]
 *          {
 *             1.5,
 *             2.5
 *          });
 *          return lineType;
 *       }
 *          :
 *          :
 *      
 * </pre>
 * 
 * 
 * @author Tommy E. Cathey
 * @version $Id: LineType.java,v 1.6 2007/01/11 20:06:27 parthee Exp $
 * 
 */
public class LineType extends AnalysisOption implements Serializable, Cloneable, LineTypeConstantsIfc,
		SymbolsConstantsIfc, AnalysisOptionConstantsIfc {
	/** serial version UID */
	final static long serialVersionUID = 1;

	/** the style of the plot */
	private String plotStyle = LineType.LINES;

	/** the style of the plot */
	private String[] plotStyles = null;

	/** array of colors for lines */
	private Color[] color = getCyclicColors();

	/** array of line styles */
	private String[] lineStyle = getDefaultLineStyles();

	/** array of line widths */
	private double[] lineWidth = { 2.0 };

	/** array of symbols */
	private String[] symbol = getDefaultSymbols();

	/** symbol expansion coefficients */
	private double[] symbolExpansion = { 1.0 };

	/**
	 * set array of line colors; used cyclically
	 * <p>
	 * 
	 * <A HREF="doc-files/ExampleLineType07.html"><B>View Example</B> </A> <br>
	 * <A HREF="doc-files/ExampleLineType14.html"><B>View Example</B> </A>
	 * 
	 * @param arg
	 *            array of line colors
	 * @pre arg != null
	 */
	public void setColor(java.awt.Color[] arg) {
		this.color = (Color[]) arg.clone();
	}

	/**
	 * get array of line colors
	 * 
	 * @return array of line colors
	 */
	public java.awt.Color[] getColor() {
		return (color != null) ? (Color[]) color.clone() : null;
	}

	/**
	 * set line style
	 * <p>
	 * 
	 * Valid Values:
	 * <ul>
	 * <li> {@link LineType#SOLID}
	 * <li> {@link LineType#DASHED}
	 * <li> {@link LineType#DOTTED}
	 * <li> {@link LineType#DOTDASH}
	 * <li> {@link LineType#LONGDASH}
	 * <li> {@link LineType#TWODASH}
	 * <li> {@link LineType#BLANK}
	 * <li> Alternatively, a string of up to 8 characters (from 0:9, "A":"F") may be given, giving the length of line
	 * segments which are alternatively drawn and skipped. Used cyclically. NOTE: mixing named types with the up to 8
	 * characters string types can be problematic when generating legends.
	 * 
	 * </ul>
	 * <br>
	 * <A HREF="doc-files/ExampleLineType05.html"><B>View Example</B> </A> <br>
	 * <A HREF="doc-files/ExampleLineType06.html"><B>View Example</B> </A> <br>
	 * <A HREF="doc-files/ExampleLineType07.html"><B>View Example</B> </A>
	 * 
	 * @param arg
	 *            desired line style
	 * @pre arg != null
	 */
	public void setLineStyle(java.lang.String[] arg) {
		this.lineStyle = (String[]) arg.clone();
	}

	/**
	 * retrieve line style
	 * <p>
	 * 
	 * 
	 * <ul>
	 * <li> {@link LineType#SOLID}
	 * <li> {@link LineType#DASHED}
	 * <li> {@link LineType#DOTTED}
	 * <li> {@link LineType#DOTDASH}
	 * <li> {@link LineType#LONGDASH}
	 * <li> {@link LineType#TWODASH}
	 * <li> {@link LineType#BLANK}
	 * <li> Alternatively, a string of up to 8 characters (from 0:9, "A":"F") may be given, giving the length of line
	 * segments which are alternatively drawn and skipped. Used cyclically. NOTE: mixing named types with the up to 8
	 * characters string types can be problematic when generating legends.
	 * 
	 * </ul>
	 * 
	 * 
	 * @return line style
	 */
	public java.lang.String[] getLineStyle() {
		return (lineStyle != null) ? (String[]) lineStyle.clone() : null;
	}

	/**
	 * set array of line widths; used cyclically
	 * <p>
	 * 
	 * <br>
	 * <A HREF="doc-files/ExampleLineType07.html"><B>View Example</B> </A> <br>
	 * <A HREF="doc-files/ExampleLineType08.html"><B>View Example</B> </A>
	 * 
	 * @param arg
	 *            array of line widths; used cyclically
	 * @pre arg != null
	 */
	public void setLineWidth(double[] arg) {
		this.lineWidth = (double[]) arg.clone();
	}

	/**
	 * retrieve array of line widths; used cyclically
	 * 
	 * @return array of line widths
	 */
	public double[] getLineWidth() {
		return (lineWidth != null) ? (double[]) lineWidth.clone() : null;
	}

	/**
	 * set the plot style
	 * <p>
	 * 
	 * valid values are:
	 * <ul>
	 * <li> {@link LineType#LINES} <A HREF="doc-files/ExampleLineType01.html"> <B>View Example</B> </A>
	 * <li> {@link LineType#POINTS} <A HREF="doc-files/ExampleLineType02.html"> <B>View Example</B> </A>
	 * <li> {@link LineType#POINTS_N_LINES} <A HREF="doc-files/ExampleLineType03.html"><B>View Example</B> </A>
	 * <li> {@link LineType#NO_PLOTTING} <A HREF="doc-files/ExampleLineType04.html"><B>View Example</B> </A>
	 * <li> Default <A HREF="doc-files/ExampleLineType07.html"><B>View Example </B></A>
	 * </ul>
	 * 
	 * 
	 * @param arg
	 *            the plot style
	 * @pre arg != null
	 */
	public void setPlotStyle(String arg) {
		this.plotStyle = arg;
		this.plotStyles = null;
	}

	/**
	 * retrieve the plot style
	 * <p>
	 * 
	 * 
	 * <ul>
	 * <li> {@link LineType#LINES} <A HREF="doc-files/ExampleLineType01.html"> <B>View Example</B> </A>
	 * <li> {@link LineType#POINTS} <A HREF="doc-files/ExampleLineType02.html"> <B>View Example</B> </A>
	 * <li> {@link LineType#POINTS_N_LINES} <A HREF="doc-files/ExampleLineType03.html"><B>View Example</B> </A>
	 * <li> {@link LineType#NO_PLOTTING} <A HREF="doc-files/ExampleLineType04.html"><B>View Example</B> </A>
	 * <li> Default <A HREF="doc-files/ExampleLineType07.html"><B>View Example </B></A>
	 * </ul>
	 * 
	 * 
	 * @return the plot style
	 */
	public java.lang.String getPlotStyle() {
		return plotStyle;
	}

	/**
	 * set the plot styles
	 * <p>
	 * 
	 * valid value is an Array of are:
	 * <ul>
	 * <li> {@link LineType#LINES} <A HREF="doc-files/ExampleLineType01.html"> <B>View Example</B> </A>
	 * <li> {@link LineType#POINTS} <A HREF="doc-files/ExampleLineType02.html"> <B>View Example</B> </A>
	 * <li> {@link LineType#POINTS_N_LINES} <A HREF="doc-files/ExampleLineType03.html"><B>View Example</B> </A>
	 * <li> {@link LineType#NO_PLOTTING} <A HREF="doc-files/ExampleLineType04.html"><B>View Example</B> </A>
	 * <li> Default <A HREF="doc-files/ExampleLineType07.html"><B>View Example </B></A>
	 * </ul>
	 * 
	 * 
	 * @param arg
	 *            the plot style
	 */
	public void setPlotStyle(java.lang.String[] arg) {
		this.plotStyles = arg;
		this.plotStyle = null;
	}

	/**
	 * retrieve the plot styles
	 * 
	 * @return the plot styles
	 */
	public java.lang.String[] getPlotStyles() {
		return plotStyles;
	}

	/**
	 * set array of symbols; used cyclically
	 * <p>
	 * 
	 * 
	 * <ul>
	 * <li> {@link LineType#CIRCLE}
	 * <li> {@link LineType#TRIANGLE_UP}
	 * <li> {@link LineType#PLUS}
	 * <li> {@link LineType#CROSS}
	 * <li> {@link LineType#SQUARE_ROTATED}
	 * <li> {@link LineType#TRIANGLE_DOWN}
	 * <li> {@link LineType#CROSS_IN_SQUARE}
	 * <li> {@link LineType#STARBURST}
	 * <li> {@link LineType#PLUS_IN_SQUARE_ROTATED}
	 * <li> {@link LineType#PLUS_IN_CIRCLE}
	 * <li> {@link LineType#TRIANGLE_UP_AND_DOWN}
	 * <li> {@link LineType#PLUS_IN_SQUARE}
	 * <li> {@link LineType#CIRCLE_AND_CROSS}
	 * <li> {@link LineType#UP_TRIANGLE_IN_SQUARE}
	 * <li> {@link LineType#SQUARE_SOLID}
	 * <li> {@link LineType#CIRCLE_SOLID}
	 * <li> {@link LineType#UP_TRIANGLE_SOLID}
	 * <li> {@link LineType#SQUARE_ROTATED_SOLID}
	 * <li> {@link LineType#CIRCLE_FILLED}
	 * <li> {@link LineType#BULLET}
	 * <li> {@link LineType#CIRCLE2}
	 * <li> {@link LineType#SQUARE}
	 * <li> {@link LineType#DIAMOND}
	 * <li> Character Symbols "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "_", "-", "+", "=", "|", "\", ":", ";",
	 * "?", "/", lower case letters "a" to "z", or upper case letters "A" to "Z" NOTE: mixing named symbols with
	 * character symbols can be problematic when doing Legends.
	 * </ul>
	 * <br>
	 * <A HREF="doc-files/ExampleLineType09.html"><B>View Example</B> </A> <br>
	 * <A HREF="doc-files/ExampleLineType07.html"><B>View Example</B> </A> <br>
	 * <A HREF="doc-files/ExampleLineType10.html"><B>View Example</B> </A> <br>
	 * <A HREF="doc-files/ExampleLineType11.html"><B>View Example</B> </A> <br>
	 * <A HREF="doc-files/ExampleLineType12.html"><B>View Example</B> </A>
	 * 
	 * @param arg
	 *            array of symbols
	 * @pre arg != null
	 */
	public void setSymbol(java.lang.String[] arg) {
		this.symbol = (String[]) arg.clone();
	}

	/**
	 * retrieve array of symbols; used cyclically
	 * <p>
	 * 
	 * 
	 * <ul>
	 * <li> {@link LineType#CIRCLE}
	 * <li> {@link LineType#TRIANGLE_UP}
	 * <li> {@link LineType#PLUS}
	 * <li> {@link LineType#CROSS}
	 * <li> {@link LineType#SQUARE_ROTATED}
	 * <li> {@link LineType#TRIANGLE_DOWN}
	 * <li> {@link LineType#CROSS_IN_SQUARE}
	 * <li> {@link LineType#STARBURST}
	 * <li> {@link LineType#PLUS_IN_SQUARE_ROTATED}
	 * <li> {@link LineType#PLUS_IN_CIRCLE}
	 * <li> {@link LineType#TRIANGLE_UP_AND_DOWN}
	 * <li> {@link LineType#PLUS_IN_SQUARE}
	 * <li> {@link LineType#CIRCLE_AND_CROSS}
	 * <li> {@link LineType#UP_TRIANGLE_IN_SQUARE}
	 * <li> {@link LineType#SQUARE_SOLID}
	 * <li> {@link LineType#CIRCLE_SOLID}
	 * <li> {@link LineType#UP_TRIANGLE_SOLID}
	 * <li> {@link LineType#SQUARE_ROTATED_SOLID}
	 * <li> {@link LineType#CIRCLE_FILLED}
	 * <li> {@link LineType#BULLET}
	 * <li> {@link LineType#CIRCLE2}
	 * <li> {@link LineType#SQUARE}
	 * <li> {@link LineType#DIAMOND}
	 * <li> Character Symbols "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "_", "-", "+", "=", "|", "\", ":", ";",
	 * "?", "/", lower case letters "a" to "z", or upper case letters "A" to "Z" NOTE: mixing named symbols with
	 * character symbols can be problematic when doing Legends.
	 * </ul>
	 * 
	 * 
	 * @return array of symbols
	 */
	public java.lang.String[] getSymbol() {
		return (symbol != null) ? (String[]) symbol.clone() : null;
	}

	/**
	 * set the symbol expansion coefficient array
	 * <p>
	 * 
	 * <A HREF="doc-files/ExampleLineType07.html"><B>View Example</B> </A> <br>
	 * <A HREF="doc-files/ExampleLineType13.html"><B>View Example</B> </A>
	 * 
	 * @param arg
	 *            symbol expansion coefficient array
	 * @pre arg != null
	 */
	public void setSymbolExpansion(double[] arg) {
		this.symbolExpansion = (double[]) arg.clone();
	}

	/**
	 * retrieve the symbol expansion coefficient array
	 * 
	 * @return symbol expansion coefficient array
	 */
	public double[] getSymbolExpansion() {
		boolean bool = (symbolExpansion != null);

		return bool ? (double[]) symbolExpansion.clone() : null;
	}

	/**
	 * Creates and returns a copy of this object
	 * 
	 * @return a copy of this object
	 */
	public Object clone() {
		try {
			LineType clone = (LineType) super.clone();

			if (lineStyle != null) {
				clone.lineStyle = (String[]) lineStyle.clone();
			}

			if (lineWidth != null) {
				clone.lineWidth = (double[]) lineWidth.clone();
			}

			if (symbol != null) {
				clone.symbol = (String[]) symbol.clone();
			}

			if (symbolExpansion != null) {
				clone.symbolExpansion = (double[]) symbolExpansion.clone();
			}

			if (color != null) {
				clone.color = (Color[]) color.clone();
			}

			return clone;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	/**
	 * Compares this object to the specified object.
	 * 
	 * @param o
	 *            the object to compare this object against
	 * @return true if the objects are equal; false otherwise
	 */
	public boolean equals(Object o) {
		boolean rtrn = true;

		if (o == null) {
			rtrn = false;
		} else if (o == this) {
			rtrn = true;
		} else if (o.getClass() != getClass()) {
			rtrn = false;
		} else {
			LineType other = (LineType) o;

			rtrn = ((plotStyle == null) ? (other.plotStyle == null) : (plotStyle.equals(other.plotStyle)))
					&& Util.equals(lineStyle, other.lineStyle) && Util.equals(lineWidth, other.lineWidth)
					&& Util.equals(symbol, other.symbol) && Util.equals(symbolExpansion, other.symbolExpansion)
					&& Util.equals(color, other.color);
		}

		return rtrn;
	}

	/**
	 * describe object in a String
	 * 
	 * @return String describing object
	 */
	public String toString() {
		return Util.toString(this);
	}

	/*******************************************************************************************************************
	 * set default colors
	 * 
	 * @return array of colors
	 ******************************************************************************************************************/
	public static Color[] getCyclicColors() {
		// ArrayList colorList = new ArrayList();
		// colorList.add(Color.black);
		// colorList.add(Color.red);
		// // colorList.add(Color.yellow);
		// colorList.add(Color.blue);
		// // colorList.add(Color.cyan);
		// colorList.add(Color.green);
		// // colorList.add(Color.magenta);
		// // colorList.add(Color.orange);
		// colorList.add(new Color(255, 150, 0));
		// colorList.add(new Color(200, 50, 0));
		// colorList.add(new Color(204, 51, 255));
		// colorList.add(new Color(50, 150, 50));
		// colorList.add(new Color(2, 176, 176));
		// colorList.add(new Color(215, 139, 139));
		// colorList.add(Color.gray);
		//
		// Color[] colors = new Color[colorList.size()];
		// colorList.toArray(colors);

		return ColorConstantsIfc.DEFAULT_COLORS;
	}

	/*******************************************************************************************************************
	 * default line styles
	 * 
	 * @return array of line styles
	 ******************************************************************************************************************/
	public static String[] getDefaultLineStyles() {
		ArrayList l = new ArrayList();
		l.add(SOLID);
		l.add(DASHED);
		l.add(DOTTED);
		l.add(DOTDASH);
		l.add(LONGDASH);
		l.add(TWODASH);

		String[] strings = new String[l.size()];
		l.toArray(strings);

		return strings;
	}

	/*******************************************************************************************************************
	 * default symbols
	 * 
	 * @return array of symbols
	 ******************************************************************************************************************/
	public static String[] getDefaultSymbols() {
		// Three symbols are commented out since they look similar to other symbols
		// in the list
		ArrayList l = new ArrayList();
		l.add(CIRCLE_SOLID);
		l.add(SQUARE_SOLID);
		l.add(UP_TRIANGLE_SOLID);
		l.add(SQUARE_ROTATED_SOLID);
		// l.add(CIRCLE_FILLED);
		l.add(CIRCLE);
		l.add(TRIANGLE_UP);
		l.add(PLUS);
		l.add(CROSS);
		l.add(SQUARE);
		l.add(SQUARE_ROTATED);
		l.add(TRIANGLE_DOWN);
		l.add(CROSS_IN_SQUARE);
		l.add(STARBURST);
		l.add(PLUS_IN_SQUARE_ROTATED);
		l.add(PLUS_IN_CIRCLE);
		l.add(TRIANGLE_UP_AND_DOWN);
		l.add(PLUS_IN_SQUARE);
		l.add(CIRCLE_AND_CROSS);
		l.add(UP_TRIANGLE_IN_SQUARE);
		// l.add(CIRCLE2);
		l.add(DIAMOND);
		// l.add(BULLET);

		String[] strings = new String[l.size()];
		l.toArray(strings);

		return strings;
	}
}
