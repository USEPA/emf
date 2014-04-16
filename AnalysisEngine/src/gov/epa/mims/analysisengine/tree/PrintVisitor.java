package gov.epa.mims.analysisengine.tree;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

/**
 * 
 * @author Tommy E. Cathey
 * @version $Id: PrintVisitor.java,v 1.3 2006/10/30 21:43:50 parthee Exp $
 * 
 */
public class PrintVisitor extends Visitor implements Serializable, AnalysisOptionConstantsIfc {
	/** DOCUMENT_ME */
	private int indent = 0;

	/*******************************************************************************************************************
	 * Creates a new PrintVisitor object.
	 ******************************************************************************************************************/
	public PrintVisitor() {
		super();
	}

	/*******************************************************************************************************************
	 * DOCUMENT_ME
	 * 
	 * @param s
	 *            DOCUMENT_ME
	 ******************************************************************************************************************/
	private void printIndent(String s) {
		for (int i = 0; i < indent; ++i) {
			System.out.print(" ");
		}

		System.out.println(s);
	}

	/*******************************************************************************************************************
	 * DOCUMENT_ME
	 * 
	 * @param p
	 *            DOCUMENT_ME
	 ******************************************************************************************************************/
	public void printTree(Plot p) {
		((gov.epa.mims.analysisengine.tree.VisitableIfc) p).accept(this);
	}

	/*******************************************************************************************************************
	 * DOCUMENT_ME
	 * 
	 * @param p
	 *            DOCUMENT_ME
	 ******************************************************************************************************************/
	public void visit(ScatterPlot p) {
		printIndent("ScatterPlot");
	}

	/*******************************************************************************************************************
	 * DOCUMENT_ME
	 * 
	 * @param d
	 *            DOCUMENT_ME
	 ******************************************************************************************************************/
	public void visit(DataSets d) {
		printIndent("DataSets");
		printIndent("" + d.getKeys());
	}

	/*******************************************************************************************************************
	 * DOCUMENT_ME
	 * 
	 * @param p
	 *            DOCUMENT_ME
	 ******************************************************************************************************************/
	public void visit(Page p) {
		printIndent("Page");
	}

	/*******************************************************************************************************************
	 * DOCUMENT_ME
	 * 
	 * @param o
	 *            DOCUMENT_ME
	 ******************************************************************************************************************/
	public void visit(AnalysisOptions o) {
		printIndent("AnalysisOptions");
		printIndent("" + o.getKeys());

		Set keys = o.getKeys();
		Iterator iter = keys.iterator();

		while (iter.hasNext()) {
			Object key = iter.next();
			Object obj = o.getOption(key);
			System.out.println(obj.toString() + "\n");
		}
	}

	/*******************************************************************************************************************
	 * DOCUMENT_ME
	 * 
	 * @param o
	 *            DOCUMENT_ME
	 ******************************************************************************************************************/
	public void printChildren(Object o) {
		((VisitableIfc) o).accept(this);

		if (o instanceof Branch) {
			int numChildren = ((Branch) o).getChildCount();

			for (int i = 0; i < numChildren; ++i) {
				printChildren(((Branch) o).getChild(i));
			}
		}
	}

	/**
	 * public static void main(String[] args) { ScatterPlot scatterPlot = new ScatterPlot(); Page page = new Page();
	 * DataSets dataSets = new DataSets(); String key1 = "key1"; String key2 = "key2"; dataSets.add(initData("My data
	 * set 1", 6), key1); dataSets.add(initData("My data set 2", 6), key2); AnalysisOptions options = new
	 * AnalysisOptions(); options.addOption(X_NUMERIC_AXIS,new AxisNumeric()); options.addOption(LINE_TYPE,new
	 * LineType()); dataSets.add(options); options.add(page); page.add(scatterPlot); PrintVisitor c = new
	 * PrintVisitor(); c.printChildren(dataSets);
	 *  }
	 */
	/*******************************************************************************************************************
	 * create and initialize a DoubleSeries
	 * 
	 * @param seriesName
	 *            name of the data set
	 * @param count
	 *            number of elements to generate
	 * 
	 * @return initialized DoubleSeries
	 ******************************************************************************************************************/
	/**
	 * private static gov.epa.mims.analysisengine.tree.test.DoubleSeries initData(String seriesName, int count) {
	 * gov.epa.mims.analysisengine.tree.test.DoubleSeries ds = new gov.epa.mims.analysisengine.tree.test.DoubleSeries();
	 * ds.setName(seriesName);
	 * 
	 * for (int i = 0; i < count; ++i) { double value = Math.random() * 10.0; ds.addData(value); }
	 * 
	 * return ds; }
	 */
}