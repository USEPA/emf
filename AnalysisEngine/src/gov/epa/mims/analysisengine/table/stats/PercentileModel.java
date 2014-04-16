package gov.epa.mims.analysisengine.table.stats;

/**
 * PercentileModel.java
 * <p>
 * A data model for the percentile gui
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company: CEP, UNC-Chapel Hill
 * </p>
 * 
 * @author Parthee Partheepan
 * @version $Id: PercentileModel.java,v 1.1 2006/11/01 15:33:38 parthee Exp $
 */

public class PercentileModel {
	/**
	 * a counter to track the no of percentile gui created during a model run and used in creating the tab name
	 */
	public static int percentile_counter = 1;

	/** a tab name for the histogram */
	private String tabName = "Percentiles " + percentile_counter;

	// some default percentiles
	// standard percentiles
	public static final double[] STANDARD = { 0.01, 0.05, 0.1, 0.5, 0.90, 0.95, 0.99 };

	// quartiles
	public static final double[] QUARTILES = { 0.0, 0.25, 0.5, 0.75, 1.0 };

	// quintiles
	public static final double[] QUINTILES = { 0.0, 0.20, 0.40, 0.60, 0.80, 1.0 };

	// decililes
	public static final double[] DECILES = { 0.0, 0.10, 0.20, 0.30, 0.40, 0.50, 0.60, 0.70, 0.80, 0.90, 1.0 };

	/** percentils */
	private double[] percentiles = STANDARD;

	/** Creates a new instance of PercentileModel */
	public PercentileModel() {
		// Empty
	}

	/**
	 * Getter for property percentiles.
	 * 
	 * @return Value of property percentiles.
	 * 
	 */
	public double[] getPercentiles() {
		return this.percentiles;
	}

	/**
	 * Getter for property tabName.
	 * 
	 * @return Value of property tabName.
	 * 
	 */
	public String getTabName() {
		return tabName;
	}

	/**
	 * Setter for property percentiles.
	 * 
	 * @param percentiles
	 *            New value of property percentiles.
	 * 
	 */
	public void setPercentiles(double[] percentiles) {
		this.percentiles = percentiles;
	}

	/**
	 * Setter for property tabName.
	 * 
	 * @param tabName
	 *            New value of property tabName.
	 * 
	 */
	public void setTabName(String tabName) {
		this.tabName = tabName;
	}

}
