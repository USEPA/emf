package gov.epa.mims.analysisengine.tree;

import java.io.Serializable;
import java.lang.reflect.Method;

/***********************************************************************************************************************
 * DOCUMENT_ME
 * 
 * @version $Revision: 1.3 $
 * @author $author$
 **********************************************************************************************************************/
public class OptionDefault extends Visitor implements Serializable, AnalysisOptionConstantsIfc {
	/** DOCUMENT_ME */
	private String analysisOptionStr = null;

	/** DOCUMENT_ME */
	private AnalysisOption analysisOption = null;

	/*******************************************************************************************************************
	 * Creates a new OptionDefault object.
	 ******************************************************************************************************************/
	public OptionDefault() {
		super();
	}

	/*******************************************************************************************************************
	 * DOCUMENT_ME
	 * 
	 * @param p
	 *            DOCUMENT_ME
	 * @param analysisOptionStr
	 *            DOCUMENT_ME
	 * 
	 * @return DOCUMENT_ME
	 ******************************************************************************************************************/
	public AnalysisOption getOption(Plot p, String analysisOptionStr) {
		this.analysisOption = (AnalysisOption) p.getOption(analysisOptionStr);

		if (this.analysisOption == null) {
			this.analysisOptionStr = analysisOptionStr;
			((gov.epa.mims.analysisengine.tree.VisitableIfc) p).accept(this);
		}

		return this.analysisOption;
	}

	/*******************************************************************************************************************
	 * DOCUMENT_ME
	 * 
	 * @param axis
	 *            DOCUMENT_ME
	 * 
	 * @return DOCUMENT_ME
	 ******************************************************************************************************************/
	public AnalysisOption getScatterPlot(AxisNumeric axis) {
		System.out.println("In: getScatterPlot(AxisNumeric axis)");

		return null;
	}

	/*******************************************************************************************************************
	 * DOCUMENT_ME
	 * 
	 * @param axis
	 *            DOCUMENT_ME
	 * 
	 * @return DOCUMENT_ME
	 ******************************************************************************************************************/
	public AnalysisOption getBarPlot(AxisCategory axis) {
		System.out.println("In: getBarPlot( AxisCategory axis)");
		axis.setEnableAxis(true);

		return axis;
	}

	/*******************************************************************************************************************
	 * DOCUMENT_ME
	 * 
	 * @param p
	 *            DOCUMENT_ME
	 ******************************************************************************************************************/
	public void visit(ScatterPlot p) {
		System.out.println("In: visit(ScatterPlot)");
		this.analysisOption = null;

		if (this.analysisOptionStr.equals(LINE_TYPE)) {
			this.analysisOption = new LineType();
		}
	}

	/*******************************************************************************************************************
	 * DOCUMENT_ME
	 * 
	 * @param aPlot
	 *            DOCUMENT_ME
	 * @param analysisOptionClass
	 *            DOCUMENT_ME
	 * 
	 * @return DOCUMENT_ME
	 ******************************************************************************************************************/
	public Object getDefault(Plot aPlot, Class analysisOptionClass) {
		System.out.println(aPlot.getClass());

		Object rtrnObj = null;

		// String methodName = "getDefault";
		String methodName = aPlot.getClass().getName();

		// String methodName = "get" + aPlot.getClass();
		methodName = "get" + methodName.substring(methodName.lastIndexOf('.') + 1);

		try {
			// Get the method visit(Foo foo)
			Method m = getClass().getMethod(methodName, new Class[] { analysisOptionClass });

			System.out.println("Invoking method: " + m);

			// Try to invoke visit(Foo foo)
			rtrnObj = m.invoke(this, new Object[] { analysisOptionClass.newInstance() });
		} catch (java.lang.InstantiationException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// No method, so do the default implementation
			e.printStackTrace();
		} catch (java.lang.IllegalAccessException e) {
			e.printStackTrace();
		} catch (java.lang.reflect.InvocationTargetException e) {
			e.printStackTrace();
		}

		return rtrnObj;
	}

	/*******************************************************************************************************************
	 * DOCUMENT_ME
	 * 
	 * @param args
	 *            DOCUMENT_ME
	 ******************************************************************************************************************/
	public static void main(String[] args) {
		// OptionDefault c = new OptionDefault(new DataSets());
		// c.getDefault(new ScatterPlot(), AxisNumeric.class);
		// c.getDefault(new BarPlot(), AxisCategory.class);
		ScatterPlot scatterPlot = new ScatterPlot();
		Page page = new Page();
		DataSets dataSets = new DataSets();
		AnalysisOptions options = new AnalysisOptions();
		options.addOption(X_NUMERIC_AXIS, new AxisNumeric());
		dataSets.add(options);
		options.add(page);
		page.add(scatterPlot);

		OptionDefault c = new OptionDefault();
		AnalysisOption opt = null;
		opt = c.getOption(scatterPlot, X_NUMERIC_AXIS);
		System.out.println(opt.toString());
		opt = c.getOption(scatterPlot, LINE_TYPE);
		System.out.println(opt.toString());
	}
}