package gov.epa.mims.analysisengine.stats;

import gov.epa.mims.analysisengine.tree.DataSetIfc;
import gov.epa.mims.analysisengine.tree.LabeledDataSetIfc;
import gov.epa.mims.analysisengine.gui.LabeledDoubleSeries;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

/**
 * <p>
 * Description: This class accepts a DataSetIfc and a set of bins and produces a LabeledDataSetIfc that lists the
 * percentiles in the provided bins.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company: UNC-CH, Carolina Environmental Program
 * </p>
 * 
 * @author Daniel Gatti
 * @version $Id: Percentile.java,v 1.3 2006/10/30 21:43:50 parthee Exp $
 */
public class Percentile {
	public Percentile() {
		//Empty
	}

	/**
	 * Given a set of data and a list of quantiles, return the value of each percentile. Colt requires that the data be
	 * sorted first, so we have to copy the values from the DataSet and sort them.
	 * 
	 * @param dataSet
	 *            DataSetIfc that is the data to partition.
	 * @param percentiles
	 *            double[] with values between 0.0 and 1.0 that indicate the percentiles to use.
	 */
	public static LabeledDataSetIfc generate(DataSetIfc dataSet, double[] percentiles) throws Exception {
		// First verify that we have percentiles between 0.0 and 1.0.
		for (int i = 0; i < percentiles.length; i++) {
			if (percentiles[i] < 0.0 || percentiles[i] > 1.0)
				throw new Exception("The percentile must be entered as percentages "
						+ "between 0.0 and 1.0. (ie. 50% = 0.5)");
		}

		// Open the data set and place the values in an array.
		dataSet.open();
		int size = dataSet.getNumElements();
		double[] values = new double[size];
		for (int i = 0; i < size; i++) {
			values[i] = dataSet.getElement(i);
		}
		dataSet.close();

		// Sort the array in ascending order.
		java.util.Arrays.sort(values);

		DoubleArrayList dataList = new DoubleArrayList(values);
		DoubleArrayList quantileList = new DoubleArrayList(percentiles);
		DoubleArrayList resultList = Descriptive.quantiles(dataList, quantileList);

		LabeledDoubleSeries result = new LabeledDoubleSeries();
		result.setName(dataSet.getName());

		result.open();
		size = resultList.size();
		for (int i = 0; i < size; i++) {
			double percentValue = percentiles[i] * 100.0;
			String label = Double.toString(percentValue);
			if (percentValue < 100.0 && percentValue >= 10.0) {
				label = " " + label;
			} else if (percentValue < 9.0 && percentValue >= 0.0) {
				label = "  " + label;
			}
			result.addData(resultList.get(i), (label + "%"));
		}
		result.close();

		return result;
	} // generate()

	/**
	 * RP: A Temporary method: For the TABLE readers to read the percentile labels as doubles
	 */
	public static LabeledDataSetIfc generate(DataSetIfc dataSet, double[] percentiles, boolean table) throws Exception {
		// First verify that we have percentiles between 0.0 and 1.0.
		for (int i = 0; i < percentiles.length; i++) {
			if (percentiles[i] < 0.0 || percentiles[i] > 1.0)
				throw new Exception("The percentile must be entered as percentages "
						+ "between 0.0 and 1.0. (ie. 50% = 0.5)");
		}

		// Open the data set and place the values in an array.
		dataSet.open();
		int size = dataSet.getNumElements();
		double[] values = new double[size];
		for (int i = 0; i < size; i++) {
			values[i] = dataSet.getElement(i);
		}
		dataSet.close();

		// Sort the array in ascending order.
		java.util.Arrays.sort(values);

		DoubleArrayList dataList = new DoubleArrayList(values);
		DoubleArrayList quantileList = new DoubleArrayList(percentiles);
		DoubleArrayList resultList = Descriptive.quantiles(dataList, quantileList);

		LabeledDoubleSeries result = new LabeledDoubleSeries();
		result.setName(dataSet.getName());

		result.open();
		size = resultList.size();
		for (int i = 0; i < size; i++) {
			// multiply by 100:we might add this as an option later on
			String label = Double.toString(percentiles[i]);
			result.addData(resultList.get(i), label);
		}
		result.close();

		return result;
	} // generate()

} // class Percentile

