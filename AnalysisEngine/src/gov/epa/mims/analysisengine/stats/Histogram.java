package gov.epa.mims.analysisengine.stats;

import java.text.DecimalFormat;

import gov.epa.mims.analysisengine.tree.DataSetIfc;
import gov.epa.mims.analysisengine.tree.LabeledDataSetIfc;
import gov.epa.mims.analysisengine.gui.LabeledDoubleSeries;
import hep.aida.ref.Histogram1D;

/**
 * <p>
 * Description: This class accepts a DataSetIfc and a set of bins and produces a LabeledDataSetIfc that is histogram of
 * the provided data broken up by the provided bins.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company: UNC-CH, Carolina Environmental Program
 * </p>
 * 
 * @author Daniel Gatti
 * @version $Id: Histogram.java,v 1.3 2006/10/30 21:43:50 parthee Exp $
 */
public class Histogram {
	/**
	 * The constant to be used for histograms that return the number of items in each bin.
	 */
	public static final byte FREQUENCY = -55;

	/**
	 * The constant to be used for histograms that return the percentage of items in each bin.
	 */
	public static final byte PERCENTAGE = -56;

	/**
	 * The constant to be used for histograms that return the probability of items in each bin.
	 */
	public static final byte PROBABILITY = -57;

	/**
	 * A pass through constant for the overflow bin in the colt Histogram implementation.
	 */
	public static final int OVERFLOW_BIN = Histogram1D.OVERFLOW;

	/**
	 * A pass through constant for the underflow bin in the colt Histogram implementation.
	 */
	public static final int UNDERFLOW_BIN = Histogram1D.UNDERFLOW;

	public Histogram() {
		//Empty
	} 

	/**
	 * The main Histogram method. Call this method with a DataSetIfc, a set of bins and an option that requests
	 * frequency or percentage. It will return a DataSetIfc with the resulting Histogram. Histogram1D returns a two
	 * additional bins '-Inf to bins[0]' and 'bin[last] to Inf' return label data set will only have these two
	 * additional bins if and only these bins have values other than 0
	 * 
	 * @param dataSet
	 *            DataSetIfc to use to create the histrogram.
	 * @param bins
	 *            double[] with the breakpoints between bins.
	 * @param format
	 *            DecimalFormat which will be applied to the format the labels can be null
	 * @param type
	 *            byte that is either FREQUENCY, PROBABLILITY or PERCENTAGE.
	 * @see Histogram1D
	 */
	public static LabeledDataSetIfc generate(DataSetIfc dataSet, double[] bins, DecimalFormat format, byte type)
			throws Exception {
		// Error checking of arguments.
		if (dataSet == null) {
			throw new IllegalArgumentException("The data set cannot be null in Histogram.generate()!");
		}

		if (bins == null) {
			throw new IllegalArgumentException("The bins cannot be null in Histogram.generate()!");
		}

		if (type != FREQUENCY && type != PERCENTAGE && type != PROBABILITY) {
			throw new IllegalArgumentException("The histogram type must be either "
					+ "Histogram.FREQUENCY, Histogram.PERCENTAGE or Histogram.PROBABILITY "
					+ "in Histogram.generate()!");
		}

		// Create the histogram with the bins set.
		Histogram1D hist = new Histogram1D("test", bins);

		// Fill the bins.
		dataSet.open();
		int size = dataSet.getNumElements();
		for (int i = 0; i < size; i++) {
			hist.fill(dataSet.getElement(i));
		}
		dataSet.close();

		LabeledDoubleSeries result = new LabeledDoubleSeries();
		result.setName(dataSet.getName());
		String[] binLabels = null;
		if (format != null) {
			binLabels = createBinLabels(format, bins);
		}// if(format == null)
		else {
			binLabels = createBinLabels(bins);
		}// else

		if (type == FREQUENCY) {
			// Extract the bin values and return them in DataSetIfc.
			int firstBinEntry = hist.binEntries(UNDERFLOW_BIN);
			// System.out.println("firstBinEntry="+firstBinEntry + " binLabels[0]= " + binLabels[0]);
			if (firstBinEntry > 0) {
				result.addData(firstBinEntry, binLabels[0]);
			}
			for (int i = 0; i < bins.length - 1; i++) {
				result.addData(hist.binEntries(i), binLabels[i + 1]);
				// System.out.println("hist.binEntries("+i+")="+hist.binEntries(i) + " binLabels["+(i+1)+"]= " +
				// binLabels[i+1]);
			}
			int lastBinEntry = hist.binEntries(OVERFLOW_BIN);
			// System.out.println("lastBinEntry="+lastBinEntry + " binLabels[last]= " + binLabels[binLabels.length -
			// 1]);
			if (lastBinEntry > 0) {
				result.addData(lastBinEntry, binLabels[binLabels.length - 1]);
			}
		} else if (type == PERCENTAGE) {
			double total = hist.allEntries();
			// Extract the bin values and return them in DataSetIfc.
			int firstBinEntry = hist.binEntries(UNDERFLOW_BIN);
			if (firstBinEntry > 0) {
				result.addData(firstBinEntry / total * 100.0, binLabels[0]);
			}

			for (int i = 0; i < bins.length - 1; i++) {
				result.addData(hist.binEntries(i) / total * 100.0, binLabels[i + 1]);
			}
			int lastBinEntry = hist.binEntries(OVERFLOW_BIN);
			if (lastBinEntry > 0) {
				result.addData(lastBinEntry / total * 100.0, binLabels[binLabels.length - 1]);
			}
		} else if (type == PROBABILITY) {
			double total = hist.allEntries();
			// Extract the bin values and return them in DataSetIfc.
			int firstBinEntry = hist.binEntries(UNDERFLOW_BIN);
			if (firstBinEntry > 0) {
				result.addData(firstBinEntry / total, binLabels[0]);
			}

			for (int i = 0; i < bins.length - 1; i++) {
				result.addData(hist.binEntries(i) / total, binLabels[i + 1]);
			}
			int lastBinEntry = hist.binEntries(OVERFLOW_BIN);
			if (lastBinEntry > 0) {
				result.addData(lastBinEntry / total, binLabels[binLabels.length - 1]);
			}
		}

		return result;
	} // generate()

	/**
	 * create the bin labels according to the format specified
	 * 
	 * @param fomat
	 *            DecimalFormat
	 * @param bin
	 *            double []
	 * @return String[] formatted labels
	 */
	private static String[] createBinLabels(DecimalFormat format, double[] bins) {
		String[] labels = new String[bins.length + 1];
		String[] formattedBins = new String[bins.length];

		for (int i = 0; i < bins.length; i++) {
			formattedBins[i] = format.format(bins[i]);
		}// for(i)

		labels[0] = "-Inf to " + formattedBins[0];
		for (int i = 1; i < labels.length - 1; i++) {
			labels[i] = formattedBins[i - 1] + " to " + formattedBins[i];
		}// for(i)
		labels[labels.length - 1] = formattedBins[bins.length - 1] + " to Inf";

		return labels;
	}// createBinLabels()

	/**
	 * create the bin labels if the format is not specified
	 * 
	 * @param bin
	 *            double []
	 * @return String[] formatted labels
	 */
	private static String[] createBinLabels(double[] bins) {
		String[] labels = new String[bins.length + 1];
		labels[0] = "-Inf to " + bins[0];
		for (int i = 1; i < bins.length - 1; i++) {
			labels[i] = bins[i - 1] + " to " + bins[i];
		}// for(i)

		labels[bins.length - 1] = bins[bins.length - 1] + " to Inf";
		return labels;
	}// createBinLabels()

} // class Histogram

