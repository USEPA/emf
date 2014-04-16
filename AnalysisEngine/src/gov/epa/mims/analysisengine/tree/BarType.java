package gov.epa.mims.analysisengine.tree;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * An analysis option which describes bar plot. It is passed to {@link AnalysisOptions#addOption(java.lang.String key,
 * java.lang.Object obj) }
 * <p>
 * 
 * Elided Code Example:
 * 
 * <pre>
 *         :
 *         :
 *      String aBARTYPE = BAR_TYPE;
 *     
 *      AnalysisOptions options = new AnalysisOptions();
 *      options.addOption(aBARTYPE, initBarType());
 *         :
 *         :
 *      private BarType initBarType()
 *      {
 *         BarType barType = new BarType();
 *         barType.setBorderColor(Color.green);
 *         barType.setHorizontal(false);
 *         barType.setStacked(true);
 *         barType.setCategoriesSpanDataSets(false);
 *         barType.setSpaceBetweenBars(1.0);
 *         barType.setSpaceBetweenCategories(1.0);
 *         barType.setWidth(new double[]
 *         {
 *            0.5
 *         });
 *     
 *         return barType;
 *      }
 * </pre>
 * 
 * @author Tommy E. Cathey
 * @created July 30, 2004
 * @version $Id: BarType.java,v 1.4 2007/01/10 22:31:35 parthee Exp $
 */
public class BarType extends AnalysisOption implements Serializable, Cloneable, LineTypeConstantsIfc,
		AnalysisOptionConstantsIfc {
	/** fields */
	final static long serialVersionUID = 1;

	/** Description of the Field */
	private boolean alphabetize = true;

	/** Description of the Field */
	private double[] density = null;

	/** Description of the Field */
	private double[] angle = null;

	/** Description of the Field */
	private String[] lty = new String[] { LineTypeConstantsIfc.SOLID };

	/** Description of the Field */
	private double[] lwd = new double[] { 1.0 };

	/** Description of the Field */
	// 091504 private boolean log = false;
	/** Description of the Field */
	private double[] xlim = new double[] { 0.0, 0.0 };

	/** color for the bar borders */
	private Color[] borderColor = new Color[] { Color.black };

	/** border line type */
	private String[] borderLty = new String[] { LineTypeConstantsIfc.SOLID };

	/** border line width */
	private double[] borderLwd = new double[] { 1.0 };

	/** color of the bars used cyclically */
	private Color[] color = getCyclicColors();

	/** the widths of the bars used cyclically */
	private double[] width;

	/**
	 * set to true if categories span data sets set to false if each data set is its own category
	 */
	private boolean categoriesSpanDataSets = false;

	/** flag to draw the bars horizontally */
	private boolean horizontal;

	/** flag to create stacked bars */
	private boolean stacked;

	/** space between the bars */
	private double spaceBetweenBars = 1;

	/** space between the categories */
	private double spaceBetweenCategories = 2;

	/**
	 * Sets the alphabetize attribute of the BarType object
	 * 
	 * @param alphabetize
	 *            The new alphabetize value
	 */
	public void setAlphabetize(boolean alphabetize) {
		this.alphabetize = alphabetize;
	}

	/**
	 * Gets the alphabetize attribute of the BarType object
	 * 
	 * @return The alphabetize value
	 */
	public boolean getAlphabetize() {
		return alphabetize;
	}

	/**
	 * Sets the borderColor attribute of the BarType object
	 * <p>
	 * 
	 * <A HREF="doc-files/ExampleBarPlot12.html"> <B>View an example: false</B> </A>
	 * 
	 * @param borderColor
	 *            The new borderColor value
	 */
	public void setBorderColor(Color[] borderColor) {
		this.borderColor = (Color[]) borderColor.clone();
	}

	/**
	 * Sets the borderLty attribute of the BarType object
	 * 
	 * @param borderLty
	 *            The new borderLty value
	 */
	public void setBorderLty(String[] borderLty) {
		this.borderLty = (String[]) borderLty.clone();
	}

	/**
	 * Sets the borderLwd attribute of the BarType object
	 * 
	 * @param borderLwd
	 *            The new borderLwd value
	 */
	public void setBorderLwd(double[] borderLwd) {
		this.borderLwd = (double[]) borderLwd.clone();
	}

	/**
	 * Gets the borderLty attribute of the BarType object
	 * 
	 * @return The borderLty value
	 */
	public String[] getBorderLty() {
		return (String[]) borderLty.clone();
	}

	/**
	 * Gets the borderLwd attribute of the BarType object
	 * 
	 * @return The borderLwd value
	 */
	public double[] getBorderLwd() {
		return (double[]) borderLwd.clone();
	}

	/**
	 * Sets the xlim attribute of the BarType object
	 * 
	 * @param xlim
	 *            The new xlim value
	 */
	public void setXlim(double[] xlim) {
		this.xlim = xlim;
	}

	/**
	 * Gets the xlim attribute of the BarType object
	 * 
	 * @return The xlim value
	 */
	public double[] getXlim() {
		return xlim;
	}

	/**
	 * Gets the borderColor attribute of the BarType object
	 * 
	 * @return The borderColor value
	 */
	public Color[] getBorderColor() {
		return borderColor;
	}

	/**
	 * Sets the density attribute of the BarType object
	 * 
	 * @param density
	 *            The new density value
	 */
	public void setDensity(double[] density) {
		this.density = density;
	}

	/**
	 * Sets the angle attribute of the BarType object
	 * 
	 * @param angle
	 *            The new angle value
	 */
	public void setAngle(double[] angle) {
		this.angle = angle;
	}

	/**
	 * Sets the lty attribute of the BarType object
	 * 
	 * @param lty
	 *            The new lty value
	 */
	public void setLty(String[] lty) {
		this.lty = lty;
	}

	/**
	 * Sets the lwd attribute of the BarType object
	 * 
	 * @param lwd
	 *            The new lwd value
	 */
	public void setLwd(double[] lwd) {
		this.lwd = lwd;
	}

	/**
	 * Sets the log attribute of the BarType object
	 * 
	 * @return The density value
	 */
	// 091504 public void setLog(boolean log)
	// 091504 {
	// 091504 this.log = log;
	// 091504 }
	/**
	 * Gets the density attribute of the BarType object
	 * 
	 * @return The density value
	 */
	public double[] getDensity() {
		return density;
	}

	/**
	 * Gets the angle attribute of the BarType object
	 * 
	 * @return The angle value
	 */
	public double[] getAngle() {
		return angle;
	}

	/**
	 * Gets the lty attribute of the BarType object
	 * 
	 * @return The lty value
	 */
	public String[] getLty() {
		return lty;
	}

	/**
	 * Gets the lwd attribute of the BarType object
	 * 
	 * @return The lwd value
	 */
	public double[] getLwd() {
		return lwd;
	}

	/**
	 * Gets the log attribute of the BarType object
	 * 
	 * @param arg
	 *            The new categoriesSpanDataSets value
	 */
	// 091504 public boolean getLog()
	// 091504 {
	// 091504 return log;
	// 091504 }
	/**
	 * set the categories span data sets flag
	 * <p>
	 * 
	 * <A HREF="doc-files/ExampleBarPlot11.html"> <B>View an example: true</B> </A>
	 * <p>
	 * 
	 * <A HREF="doc-files/ExampleBarPlot12.html"> <B>View an example: false</B> </A>
	 * 
	 * @param arg
	 *            the categories span data sets flag
	 */
	public void setCategoriesSpanDataSets(boolean arg) {
		this.categoriesSpanDataSets = arg;
	}

	/**
	 * get the categories span data sets flag
	 * 
	 * @return the categories span data sets flag
	 */
	public boolean getCategoriesSpanDataSets() {
		return categoriesSpanDataSets;
	}

	/**
	 * set array of user selected colors for bars;
	 * <ul>
	 * <li> values are cyclical
	 * </ul>
	 * <p>
	 * 
	 * <A HREF="doc-files/ExampleBarPlot09.html"> <B>View an example: user defined colors</B> </A>
	 * <p>
	 * 
	 * <A HREF="doc-files/ExampleBarPlot10.html"> <B>View an example: default colors</B> </A>
	 * 
	 * @param arg
	 *            array of user selected colors for bars
	 * @pre arg != null
	 */
	public void setColor(java.awt.Color[] arg) {
		this.color = (java.awt.Color[]) arg.clone();
	}

	/**
	 * get array of user selected colors for bars;
	 * <ul>
	 * <li> values are cyclical
	 * </ul>
	 * 
	 * 
	 * @return array of user selected colors
	 */
	public java.awt.Color[] getColor() {
		return (color != null) ? (Color[]) color.clone() : null;
	}

	/**
	 * set horizontal flag;
	 * <ul>
	 * <li> default = false;
	 * <li> true = horizontal bars;
	 * <li> false = vertical bars
	 * </ul>
	 * <p>
	 * 
	 * <A HREF="doc-files/ExampleBarPlot07.html"><B>View an example</B> </A>
	 * 
	 * @param arg
	 *            user selected orientation flag
	 */
	public void setHorizontal(boolean arg) {
		this.horizontal = arg;
	}

	/**
	 * retrieve horizontal flag
	 * 
	 * @return true->draw horizontal bars; false->draw vertical bars
	 */
	public boolean getHorizontal() {
		return horizontal;
	}

	/**
	 * set space between bars within a category;
	 * <ul>
	 * </ul>
	 * <p>
	 * 
	 * <A HREF="doc-files/ExampleBarPlot05.html"><B>View an example</B> </A>
	 * 
	 * @param arg
	 *            user selected space between bars within a category
	 */
	public void setSpaceBetweenBars(double arg) {
		this.spaceBetweenBars = arg;
	}

	/**
	 * get space between bars within a category;
	 * <ul>
	 * </ul>
	 * 
	 * 
	 * @return user selected space between bars within a category
	 */
	public double getSpaceBetweenBars() {
		return spaceBetweenBars;
	}

	/**
	 * set space between categories;
	 * <ul>
	 * </ul>
	 * <p>
	 * 
	 * <A HREF="doc-files/ExampleBarPlot05.html"><B>View an example</B> </A>
	 * 
	 * @param arg
	 *            user selected space between categories
	 */
	public void setSpaceBetweenCategories(double arg) {
		this.spaceBetweenCategories = arg;
	}

	/**
	 * get space between categories;
	 * <ul>
	 * </ul>
	 * 
	 * 
	 * @return user selected space between categories
	 */
	public double getSpaceBetweenCategories() {
		return spaceBetweenCategories;
	}

	/**
	 * set stacked flag;
	 * <ul>
	 * <li> default = false
	 * </ul>
	 * <p>
	 * 
	 * <A HREF="doc-files/ExampleBarPlot06.html"><B>View an example</B> </A>
	 * 
	 * @param arg
	 *            true->stacked bars; false->nonstacked bars
	 */
	public void setStacked(boolean arg) {
		this.stacked = arg;
	}

	/**
	 * retrieve stacked flag
	 * 
	 * @return true->stacked bars; false->nonstacked bars
	 */
	public boolean getStacked() {
		return stacked;
	}

	/**
	 * set array of user selected bar widths;
	 * <ul>
	 * <li> values are Cyclical
	 * </ul>
	 * <p>
	 * 
	 * <A HREF="doc-files/ExampleBarPlot04.html"><B>View an example</B> </A>
	 * 
	 * @param arg
	 *            array of user selected bar widths
	 * @pre arg != null
	 */
	public void setWidth(double[] arg) {
		this.width = (double[]) arg.clone();
	}

	/**
	 * get array of user selected bar widths;
	 * <ul>
	 * <li> values are Cyclical;
	 * <li> null if not set by user;
	 * </ul>
	 * 
	 * 
	 * @return array of user selected bar widths or null if not set
	 */
	public double[] getWidth() {
		return (width != null) ? (double[]) width.clone() : null;
	}

	/**
	 * Creates and returns a copy of this object
	 * 
	 * @return a copy of this object
	 */
	public Object clone() {
		try {
			BarType clone = (BarType) super.clone();
			clone.color = (color == null) ? null : (Color[]) color.clone();

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
			BarType other = (BarType) o;

			rtrn = Util.equals(width, other.width) && (spaceBetweenBars == other.spaceBetweenBars)
					&& (spaceBetweenCategories == other.spaceBetweenCategories) && (stacked == other.stacked)
					&& (alphabetize == other.alphabetize) && (horizontal == other.horizontal)
					&& Util.equals(color, other.color) && Util.equals(borderColor, other.borderColor)
					&& Util.equals(borderLty, other.borderLty) && Util.equals(borderLwd, other.borderLwd)
					&& (categoriesSpanDataSets == other.categoriesSpanDataSets);
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

	/**
	 * Description of the Method
	 * 
	 * @param l
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	public String toString(List l) {
		// 091504 l.add("BarType.log = " + this.getLog());
		// 091504 l.add("\n");
		l.add("BarType.categoriesSpanDataSets = ");
		l.add("" + this.getCategoriesSpanDataSets());
		l.add("\n");
		l.add("BarType.horizontal = ");
		l.add("" + this.getHorizontal());
		l.add("\n");
		l.add("BarType.stacked = ");
		l.add("" + this.getStacked());
		l.add("\n");
		l.add("BarType.spaceBetweenBars = ");
		l.add("" + this.getSpaceBetweenBars());
		l.add("\n");
		l.add("BarType.spaceBetweenCategories = ");
		l.add("" + this.getSpaceBetweenCategories());
		l.add("\n");
		l.add(toString("BarType.xlim", this.getXlim()));
		l.add("\n");
		l.add(toString("BarType.width", this.getWidth()));
		l.add("\n");
		l.add(toString("BarType.lwd", this.getLwd()));
		l.add("\n");
		l.add(toString("BarType.angle", this.getAngle()));
		l.add("\n");
		l.add(toString("BarType.density", this.getDensity()));
		l.add("\n");
		l.add(toString("BarType.lty", this.getLty()));
		l.add("\n");
		l.add(toString("BarType.borderColor", this.getBorderColor()));
		l.add("\n");
		l.add(toString("BarType.color", this.getColor()));
		l.add("\n");
		l.add("BarType.categoriesSpanDataSets = ");
		l.add("" + this.getCategoriesSpanDataSets());
		l.add("\n");
		l.add("BarType.alphabetize = ");
		l.add("" + this.getAlphabetize());
		return null;
	}

	/**
	 * Description of the Method
	 * 
	 * @param name
	 *            Description of the Parameter
	 * @param o
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	private String toString(String name, Object[] o) {
		StringBuffer b = new StringBuffer();
		b.append("BarType." + name + " =");
		if (o == null) {
			b.append("NULL");
		} else {
			for (int i = 0; i < o.length; i++) {
				b.append("" + o[i]);
				if (i < o.length - 1) {
					b.append(", ");
				}
			}
		}
		return b.toString();
	}

	/**
	 * Description of the Method
	 * 
	 * @param name
	 *            Description of the Parameter
	 * @param d
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	private String toString(String name, double[] d) {
		StringBuffer b = new StringBuffer();
		b.append("BarType." + name + " =");
		if (d == null) {
			b.append("NULL");
		} else {
			for (int i = 0; i < d.length; i++) {
				b.append("" + d[i]);
				if (i < d.length - 1) {
					b.append(", ");
				}
			}
		}
		return b.toString();
	}

	/**
	 * set default colors for bars
	 * 
	 * @return array of bar colors
	 */
	private static Color[] getCyclicColors() {
		ArrayList colorList = new ArrayList();
		colorList.add(Color.red);
		colorList.add(Color.yellow);
 		colorList.add(Color.blue);
		colorList.add(Color.green);
		colorList.add(Color.magenta);
		colorList.add(Color.orange);
		colorList.add(Color.pink);
		colorList.add(Color.lightGray);
		colorList.add(Color.gray);
		colorList.add(Color.black);
		colorList.add(new Color(255, 150, 0));
		colorList.add(new Color(200, 50, 0));
		colorList.add(Color.cyan);
		colorList.add(new Color(204, 51, 255));
		colorList.add(new Color(50, 150, 50));
		colorList.add(new Color(2, 176, 176));
		colorList.add(new Color(215, 139, 139));
		colorList.add(new Color(142, 142, 212));
		colorList.add(new Color(153, 0, 153));
		colorList.add(new Color(0, 255, 204));

		Color[] colors = new Color[colorList.size()];
		colorList.toArray(colors);

		return colors;
		// return ColorConstantsIfc.DEFAULT_COLORS;
	}
}
