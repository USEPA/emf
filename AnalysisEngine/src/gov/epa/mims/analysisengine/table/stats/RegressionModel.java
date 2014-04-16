package gov.epa.mims.analysisengine.table.stats;

import gov.epa.mims.analysisengine.table.LRResultTableModel;
import gov.epa.mims.analysisengine.table.OverallTableModel;
import gov.epa.mims.analysisengine.table.SpecialTableModel;
import gov.epa.mims.analysisengine.table.format.FormatAndIndexInfoIfc;
import gov.epa.mims.analysisengine.table.format.NullFormatter;

import java.util.Hashtable;
import java.util.Vector;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;

/**
 * 
 * @author kthanga
 */
public class RegressionModel implements java.io.Serializable, FormatAndIndexInfoIfc {
	/** serial version UID */
	static final long serialVersionUID = 1;

	transient OverallTableModel model;

	private Vector SLRIndepVariables;

	private Vector SLRDepVariables;

	private Vector LRIndepVariables;

	private Vector LRDepVariables;

	private String tabName_slr;

	private String tabName_lr;

	private String[] colNames;

	private boolean conciseReport = true;

	private boolean normalized = false;

	/** Creates a new instance of RegressionModel */
	public RegressionModel() {
		initialize();
	}

	public RegressionModel(OverallTableModel model) {
		this.model = model;
		initialize();
	}

	public Vector getLRDepVariables() {
		String[] colNames = model.getColumnNames();
		Vector vect = new Vector();

		for (int i = 0; i < colNames.length; i++) {
			vect.add(colNames[i]);
		}

		Vector vars = new Vector();
		vars.addAll(LRDepVariables);

		for (int i = 0; i < LRDepVariables.size(); i++) {
			if (!vect.contains(LRDepVariables.get(i))) {
				vars.remove(LRDepVariables.get(i));
			}
		}

		return vars;
	}

	public Vector getSLRDepVariables() {
		String[] colNames = model.getColumnNames();
		Vector vect = new Vector();

		for (int i = 0; i < colNames.length; i++) {
			vect.add(colNames[i]);
		}

		Vector vars = new Vector();
		vars.addAll(SLRDepVariables);

		for (int i = 0; i < SLRDepVariables.size(); i++) {
			if (!vect.contains(SLRDepVariables.get(i))) {
				vars.remove(SLRDepVariables.get(i));
			}
		}

		return vars;
	}

	public Vector getLRIndepVariables() {
		String[] colNames = model.getColumnNames();
		Vector vect = new Vector();

		for (int i = 0; i < colNames.length; i++) {
			vect.add(colNames[i]);
		}

		Vector vars = new Vector();
		vars.addAll(LRIndepVariables);

		for (int i = 0; i < LRIndepVariables.size(); i++) {
			if (!vect.contains(LRIndepVariables.get(i))) {
				vars.remove(LRIndepVariables.get(i));
			}
		}

		return vars;
	}

	public Vector getSLRIndepVariables() {
		String[] colNames = model.getColumnNames();
		Vector vect = new Vector();

		for (int i = 0; i < colNames.length; i++) {
			vect.add(colNames[i]);
		}

		Vector vars = new Vector();
		vars.addAll(SLRIndepVariables);

		for (int i = 0; i < SLRIndepVariables.size(); i++) {
			if (!vect.contains(SLRIndepVariables.get(i))) {
				vars.remove(SLRIndepVariables.get(i));
			}
		}

		return vars;
	}

	public void setTabName(boolean slr, String tabName) {
		if (slr) {
			this.tabName_slr = tabName;
		} else {
			this.tabName_lr = tabName;
		}
	}

	public String getTabName(boolean slr) {
		if (slr) {
			return tabName_slr;
		}
		return tabName_lr;
	}

	String[] getColumnNames() {
		if (colNames == null) {
			Vector names = new Vector();
			String[] coluNames = model.getColumnNames();

			for (int i = 0; i < coluNames.length; i++) {
				Class cla = model.getColumnClass(i + 1);

				if (cla.equals(Double.class) || cla.equals(Integer.class)) {
					names.add(coluNames[i]);
				}
			}

			colNames = (String[]) names.toArray(new String[names.size()]);
		}

		return colNames;
	}

	public void clearColumnNames() {
		colNames = null;
	}

	private void initialize() {
		SLRIndepVariables = new Vector();
		SLRDepVariables = new Vector();
		LRIndepVariables = new Vector();
		LRDepVariables = new Vector();
	}

	public void setValues(boolean genSLR, Vector depVars, Vector indepVars) {
		if (genSLR) {
			SLRIndepVariables = indepVars;
			SLRDepVariables = depVars;
		} else {
			LRIndepVariables = indepVars;
			LRDepVariables = depVars;
		}
	}

	public SpecialTableModel computeLR(String fileName, boolean genSLR) throws Exception {
		SpecialTableModel resModel;
		Vector indepVars;
		Vector depVars;
		Hashtable data;

		if (genSLR) {
			indepVars = SLRIndepVariables;
			depVars = SLRDepVariables;

			if (conciseReport) {
				resModel = new SLRResultTableModel(SLRDepVariables, SLRIndepVariables, model.getRowCount(), fileName);
			} else {
				resModel = new SLRDetailedResultTableModel(SLRDepVariables, SLRIndepVariables, model.getRowCount(),
						fileName);
			}

			data = createInstances(SLRDepVariables, SLRIndepVariables);
		} else {
			indepVars = LRIndepVariables;
			depVars = LRDepVariables;
			resModel = new LRResultTableModel(LRDepVariables, LRIndepVariables, model.getRowCount(), fileName);
			data = createInstances(LRDepVariables, LRIndepVariables);
		}

		if (genSLR == false) {
			Instances indepInstances;
			indepInstances = (Instances) data.get(indepVars.get(0));

			for (int i = 1; i < indepVars.size(); i++) {
				indepInstances = Instances.mergeInstances(indepInstances, (Instances) data.get(indepVars
						.get(i)));
			}

			for (int i = 0; i < depVars.size(); i++) {
				if (indepVars.contains(depVars.get(i))) {
					indepInstances.setClassIndex(indepVars.indexOf(depVars.get(i)));
				} else {
					indepInstances = Instances.mergeInstances(indepInstances, (Instances) data.get(depVars.get(i)));
					indepInstances.setClassIndex(indepInstances.numAttributes() - 1);
				}

				// indepInstances.merge(data.get(depVars.get(i)));
				Vector result = computeLR(indepInstances);
				result.add(0, depVars.get(i));

				if (!indepVars.contains(depVars.get(i))) {
					indepInstances.setClassIndex(-1);
					indepInstances.deleteAttributeAt(indepInstances.numAttributes() - 1);
					result.remove(2 + indepVars.size());
				}

				((LRResultTableModel) resModel).addRowData(result);
			}
		} else {
			if (conciseReport) {
				for (int i = 0; i < depVars.size(); i++) {
					Vector result = new Vector();
					result.add(depVars.get(i));

					for (int j = 0; j < indepVars.size(); j++) {
						result.addAll(computeSLR(Instances.mergeInstances((Instances) data.get(depVars.get(i)),
								(Instances) data.get(indepVars.get(j))), conciseReport));
					}

					((SLRResultTableModel) resModel).addRowData(result);
				}
			} else {
				for (int i = 0; i < depVars.size(); i++) {
					for (int j = 0; j < indepVars.size(); j++) {
						Vector result = computeSLR(Instances.mergeInstances((Instances) data.get(depVars.get(i)),
								(Instances) data.get(indepVars.get(j))), conciseReport);
						((SLRDetailedResultTableModel) resModel).addRowData(result);
					}
				}
			}
		}

		return resModel;
	}

	private Hashtable createInstances(Vector depVars, Vector indepVars) {
		Vector vars = new Vector();
		vars.addAll(depVars);

		for (int i = 0; i < indepVars.size(); i++) {
			if (!vars.contains(indepVars.get(i))) {
				vars.add(indepVars.get(i));
			}
		}

		Vector attrs = new Vector();

		for (int i = 0; i < vars.size(); i++) {
			attrs.addElement(new Attribute((String) vars.get(i)));
		}

		Hashtable result = new Hashtable();

		for (int k = 0; k < attrs.size(); k++) {
			FastVector vect = new FastVector();
			vect.addElement(attrs.get(k));

			Instances data1 = new Instances(((Attribute) attrs.get(k)).name(), vect, model.getRowCount());
			int colIndex = model.getColumnNameIndex((String) vars.get(k)) + 1;

			for (int i = 0; i < model.getRowCount(); i++) {
				double[] value = new double[1];
				Object obj = model.getValueAt(i, colIndex);

				if (obj instanceof Double) {
					value[0] = ((Double) model.getValueAt(i, colIndex)).doubleValue();
				} else {
					value[0] = ((Integer) model.getValueAt(i, colIndex)).intValue();
				}

				data1.add(new Instance(1.0, value));
			}

			result.put(vars.get(k), data1);
		}

		return result;
	}

	private Vector computeSLR(Instances data, boolean concise) throws Exception {
		java.util.Vector result = new java.util.Vector();

		Classifier classy = (Classifier) Class.forName("weka.classifiers.functions.SimpleLinearRegression")
				.newInstance();
		data.setClassIndex(0);

		Evaluation eval = new Evaluation(data, null);

		classy.buildClassifier(data);
		eval.evaluateModel(classy, data);

		if (!concise) {
			result.add(data.attribute(0).name());
			result.add(data.attribute(1).name());
			result.add(new Double(eval.correlationCoefficient()));
			result.add(new Double(((weka.classifiers.functions.SimpleLinearRegression) classy).getSlope()));
			result.add(new Double(((weka.classifiers.functions.SimpleLinearRegression) classy).getIntercept()));

			String equation = classy.toString();

			if (equation.indexOf("onstant") >= 0) {
				equation = equation.substring(equation.indexOf("onstant") + 7);
			} else {
				equation = equation.substring(equation.indexOf("\n\n") + 2);
			}

			equation = equation.replaceAll("\n", " ");
			equation = equation.replaceAll(" ", "");
			result.add(equation);
			result.add(new Double(eval.meanAbsoluteError()));
			result.add(new Double(eval.meanPriorAbsoluteError()));
			result.add(new Double(eval.relativeAbsoluteError()));
			result.add(new Double(eval.rootMeanSquaredError()));
			result.add(new Double(eval.rootMeanPriorSquaredError()));
			result.add(new Double(eval.rootRelativeSquaredError()));
		} else {
			if (((weka.classifiers.functions.SimpleLinearRegression) classy).getSlope() > 0) {
				result.add(new Double(eval.correlationCoefficient()));
			} else {
				result.add(new Double(eval.correlationCoefficient() * -1));
			}
		}

		return result;
	}

	private Vector computeLR(Instances data) throws Exception {
		Classifier classy = (Classifier) Class.forName("weka.classifiers.functions.LinearRegression").newInstance();

		if (normalized) {
			Filter norm = new Normalize();
			int index = data.classIndex();
			data.setClassIndex(-1);
			norm.setInputFormat(data);

			Instances dat = norm.useFilter(data, norm);
			data = dat;
			data.setClassIndex(index);
		}

		Evaluation eval = new Evaluation(data, null);
		classy.buildClassifier(data);
		eval.evaluateModel(classy, data);

		double[] coefficients = ((weka.classifiers.functions.LinearRegression) classy).coefficients();
		java.util.Vector result = new java.util.Vector();
		String equation = classy.toString();
		equation = equation.substring(equation.indexOf("Linear Regression Model")
				+ new String("Linear Regression Model").length());
		equation = equation.replaceAll("\n", " ");
		equation = equation.replaceAll(" ", "");
		result.add(equation);

		for (int i = 0; i < (coefficients.length - 1); i++) {
			result.add(new Double(coefficients[i]));
		}

		result.add(new Double(coefficients[coefficients.length - 1]));
		result.add(new Double(eval.correlationCoefficient()));
		result.add(new Double(eval.meanAbsoluteError()));
		result.add(new Double(eval.meanPriorAbsoluteError()));
		result.add(new Double(eval.relativeAbsoluteError()));
		result.add(new Double(eval.rootMeanSquaredError()));
		result.add(new Double(eval.rootMeanPriorSquaredError()));
		result.add(new Double(eval.rootRelativeSquaredError()));

		return result;
	}

	public int getColumnNameIndex(String colName) {
		if (colName.equalsIgnoreCase("Column Name")) {
			return 0;
		}
		return 1;
	}

	public java.text.Format getFormat(String name) {
		return new NullFormatter();
	}

	public void setConciseReport(boolean val) {
		conciseReport = val;
	}

	public void setNormalized(boolean norm) {
		normalized = true;
	}

	public Class getColumnClass(int index) {
		return model.getColumnClass(index);
	}
}
