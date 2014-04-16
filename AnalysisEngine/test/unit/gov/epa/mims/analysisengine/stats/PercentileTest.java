package gov.epa.mims.analysisengine.stats;

import gov.epa.mims.analysisengine.gui.DoubleSeries;
import gov.epa.mims.analysisengine.gui.LabeledDoubleSeries;
import junit.framework.TestCase;
import cern.jet.random.Normal;
import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;

/**
 * <p>
 * Description: Tests for the Percentile class.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company: UNC-CH, Carolina Environmental Program
 * </p>
 * 
 * @author Daniel Gatti
 * @version $Id: PercentileTest.java,v 1.2 2006/10/30 21:43:51 parthee Exp $
 */
public class PercentileTest extends TestCase {
	// The tolerance that we will accept when comparing doubles.
	public static final double EPSILON = 1.0E-7;

	DoubleSeries ds1 = new DoubleSeries();

	DoubleSeries ds2 = new DoubleSeries();

	DoubleSeries ds3 = new DoubleSeries();

	LabeledDoubleSeries ds4 = new LabeledDoubleSeries();

	LabeledDoubleSeries ds5 = new LabeledDoubleSeries();

	DoubleSeries ds6 = new DoubleSeries();

	DoubleSeries ds7 = new DoubleSeries();

	DoubleSeries ds8 = new DoubleSeries();

	public PercentileTest(String name) {
		super(name);
	}

	/**
	 * Test numbers from 1 to 100 with percentiles from 1 to 100.
	 */
	public void testBasicOperation() {
		double[] percentiles = new double[100];
		double[] answers = new double[100];
		for (int i = 0; i < 100; i++) {
			ds1.addData(i);
			percentiles[i] = i / 100.0;
			answers[i] = i;
		}
		ds1.addData(100.0);

		try {
			LabeledDoubleSeries result = (LabeledDoubleSeries) Percentile.generate(ds1, percentiles);
			result.open();
			for (int i = 0; i < result.getNumElements(); i++) {
				assertEquals("percentile " + i, answers[i], result.getElement(i), EPSILON);
			}
			result.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	} // testBasicOperation()

	/**
	 * Test with a uniform distribution.
	 */
	public void testUniformDisribution() {
		Uniform uniform = new Uniform(0.0, 1000.0, (int) System.currentTimeMillis());

		int numElements = 1000000;
		for (int i = 0; i < numElements; i++) {
			ds2.addData(uniform.nextDouble());
		}

		double[] percentiles = { 0.01, 0.1, 0.25, 0.5, 0.75, 0.90, 0.99 };
		double[] answers = { 10.0, 100.0, 250.0, 500.0, 750.0, 900.0, 990.0 };

		try {
			LabeledDoubleSeries result = (LabeledDoubleSeries) Percentile.generate(ds2, percentiles);
			result.open();
			for (int i = 0; i < result.getNumElements(); i++) {
				// System.out.println(result.getLabel(i) + " : " + result.getElement(i));
				// NOTE: I'm using a varying error window of 1.5%.
				assertEquals("percentile " + i, answers[i], result.getElement(i), answers[i] * 0.015);
			}
			result.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	} // testUniformDisribution()

	/**
	 * Test very small numbers.
	 */
	public void testSmallNumbers() {
		for (int i = 0; i < 11; i++) {
			ds2.addData(i * 1.0E-49);
		}

		double[] percentiles = { 0.01, 0.1, 0.25, 0.5, 0.75, 0.90, 0.99 };
		double[] answers = { 1.0E-50, 1.0E-49, 2.5E-49, 5.0E-49, 7.5E-49, 9.0E-49, 9.9E-49 };

		try {
			LabeledDoubleSeries result = (LabeledDoubleSeries) Percentile.generate(ds2, percentiles);
			result.open();
			for (int i = 0; i < result.getNumElements(); i++) {
				// System.out.println(result.getLabel(i) + " : " + result.getElement(i));
				// NOTE: I'm using a varying error window of 2.0%.
				assertEquals("percentile " + i, answers[i], result.getElement(i), answers[i] * 0.02);
			}
			result.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	} // testSmallNumbers()

	/**
	 * Test very large numbers.
	 */
	public void testLargeNumbers() {
		for (int i = 0; i < 11; i++) {
			ds3.addData(i * 1.0E200);
		}

		double[] percentiles = { 0.01, 0.1, 0.25, 0.5, 0.75, 0.90, 0.99 };
		double[] answers = { 1.0E199, 1.0E200, 2.5E200, 5.0E200, 7.5E200, 9.0E200, 9.9E200 };

		try {
			LabeledDoubleSeries result = (LabeledDoubleSeries) Percentile.generate(ds3, percentiles);
			result.open();
			for (int i = 0; i < result.getNumElements(); i++) {
				// System.out.println(result.getLabel(i) + " : " + result.getElement(i));
				// NOTE: I'm using a varying error window of 2.0%.
				assertEquals("percentile " + i, answers[i], result.getElement(i), answers[i] * 0.02);
			}
			result.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	} // testLargeNumbers()

	public void testLargeDataSet() {
		for (int i = 0; i < 1000000; i++) {
			ds4.addData(i);
		}

		double[] percentiles = { 0.000001, 0.00001, 0.0001, 0.001, 0.01, 0.1, 0.25, 0.5, 0.75, 0.90, 0.99, 0.999,
				0.9999, 0.99999, 0.999999 };
		double[] answers = { 1.0, 10.0, 100.0, 1000.0, 10000.0, 100000.0, 250000.0, 500000.0, 750000.0, 900000.0,
				990000.0, 999000.0, 999900.0, 999990.0, 999999.0 };

		try {
			LabeledDoubleSeries result = (LabeledDoubleSeries) Percentile.generate(ds4, percentiles);
			result.open();
			for (int i = 0; i < result.getNumElements(); i++) {
				// System.out.println(result.getLabel(i) + " : " + result.getElement(i));
				assertEquals("percentile " + i, answers[i], result.getElement(i), answers[i] * 0.01);
			}
			result.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		ds4 = null;
	} // testLargeDataSet()

	public void FIXME_testLabels() {
		for (int i = 0; i < 100; i++) {
			ds5.addData(i);
		}

		double[] percentiles = { 0.10, 0.25, 0.5, 0.75, 0.90 };
		String[] answers = { "10.0 %", "25.0 %", "50.0 %", "75.0 %", "90.0 %" };

		try {
			LabeledDoubleSeries result = (LabeledDoubleSeries) Percentile.generate(ds5, percentiles);
			result.open();
			for (int i = 0; i < result.getNumElements(); i++) {
				// System.out.println(result.getLabel(i));
				assertEquals("percentile " + i, answers[i], result.getLabel(i));
			}
			result.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	} // testLabels()

	public void testNormalDistribution() {
		Normal normal = new Normal(0.0, 1.0, new MersenneTwister());
		int numElements = 1000000;
		for (int i = 0; i < numElements; i++) {
			ds6.addData(normal.nextDouble());
		}

		double[] percentiles = { 0.01, 0.1, 0.25, 0.5, 0.75, 0.90, 0.99 };

		try {
			LabeledDoubleSeries result = (LabeledDoubleSeries) Percentile.generate(ds6, percentiles);
			result.open();
			for (int i = 0; i < result.getNumElements(); i++) {
				// System.out.println(result.getLabel(i) + " : " + result.getElement(i) + " " +
				// normal.cdf(result.getElement(i))+" "+percentiles[i]);
				// NOTE: I'm using the Normal distribution's cdf() function to
				// generate answers on the fly. This may be a bad idea if the
				// Normal.cdf() and the Descriptive.quantile() methods use the
				// same algorithm.
				assertEquals("percentile " + i, normal.cdf(result.getElement(i)), percentiles[i], 0.01);
			}
			result.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		ds6 = null;
	} // testNormalDistribution()

	public void testNegativeNumbers() {
		for (int i = -100; i <= 0; i++) {
			ds7.addData(i);
		}

		double[] percentiles = { 0.01, 0.1, 0.25, 0.5, 0.75, 0.90, 0.99 };
		double[] answers = { -99.0, -90.0, -75.0, -50.0, -25.0, -10., -1.0 };

		try {
			LabeledDoubleSeries result = (LabeledDoubleSeries) Percentile.generate(ds7, percentiles);
			result.open();
			for (int i = 0; i < result.getNumElements(); i++) {
				// System.out.println(result.getLabel(i) + " : " + result.getElement(i) + " " + answers[i]);
				// NOTE: Error of 1%.
				assertEquals("percentile " + i, answers[i], result.getElement(i), 0.01);
			}
			result.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	} // testNormalDistribution()

	/*******************************************************************************************************************
	 * main - runs all tests with the SwingUI.
	 * 
	 * @param args
	 *            not used
	 ******************************************************************************************************************/
	public static void main(String[] args) {
		junit.swingui.TestRunner.run(PercentileTest.class);
	}
} // class PercentileTest

