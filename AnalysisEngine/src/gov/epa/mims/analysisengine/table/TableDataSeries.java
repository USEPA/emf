package gov.epa.mims.analysisengine.table;

import gov.epa.mims.analysisengine.tree.AbstractDataSet;
import gov.epa.mims.analysisengine.tree.LabeledDataSetIfc;

/**
 * This class implements the LabeledDataSetIfc and extends the AbstractDataSet class from the tree engine. When the plot
 * button is clicked in the table panel a DataSets object will be created containing a number of these TableDataSeries
 * 
 * @author Prashant Pai, CEP UNC
 * @version $Id: TableDataSeries.java,v 1.3 2006/10/30 21:43:50 parthee Exp $
 */

public class TableDataSeries extends AbstractDataSet implements LabeledDataSetIfc {
	/** serial version UID */
	static final long serialVersionUID = 1;

	/** reference count for the number of opens */
	protected transient int numUnmatchedOpens = 0;

	/** the model that is passed in * */
	protected OverallTableModel tableModel = null;

	/** the column in the model that represents this data series * */
	protected int valueColumn;

	/** selected label columns */
	protected int[] labelColumns;

	/** concatnation charactor for labels */
	protected char separator;

	/**
	 * NOTE: indexing based on the overalltablemodel, 0 index belongs to the rowheader RP
	 * 
	 * @param model
	 *            that is currently being displayed in the table the topmost level tablemodel
	 * @pre model != null
	 * @pre valueCol < plottingInfo.getValueColumns().length
	 */
	public TableDataSeries(OverallTableModel model, int valueCol, int[] labelColumns, char separator) {
		this.tableModel = model;
		this.labelColumns = labelColumns;
		this.valueColumn = valueCol;
		this.separator = separator;
	}

	/*******************************************************************************************************************
	 * get data element as a double
	 * 
	 * @param i
	 *            index into data series
	 * @return element as a double
	 * @throws java.lang.Exception
	 *             if series is not open
	 * @throws java.util.NoSuchElementException
	 *             if i is out of range
	 ******************************************************************************************************************/
	public double getElement(int i) throws Exception, java.util.NoSuchElementException {
		return Double.parseDouble(tableModel.getValueAt(i, valueColumn + 1).toString());
	}// getElement(int)

	/*******************************************************************************************************************
	 * get number of elements in this data set
	 * 
	 * @return number of elements
	 * @throws java.lang.Exception
	 *             if series is not open
	 ******************************************************************************************************************/
	public int getNumElements() throws Exception {
		return tableModel.getRowCount();
	}

	/*******************************************************************************************************************
	 * get data label
	 * 
	 * @param i
	 *            index into data series
	 * @return element label
	 * @throws java.lang.Exception
	 *             if series is not open
	 * @throws java.util.NoSuchElementException
	 *             if i is out of range
	 ******************************************************************************************************************/
	public String getLabel(int i) throws Exception, java.util.NoSuchElementException {
		String label = "";
		for (int j = 0; j < labelColumns.length; j++) {
			if (labelColumns[j] != -1) {
				// adding one to skip the first col in the overall table model: it's
				// the row header
				String value = tableModel.getFormattedValueAt(i, labelColumns[j] + 1);
				label = label.concat(value + separator);
			}// if(labelColNo != -1)
		}// for(j)
		// stripping the last char (separator)
		if (label.length() > 1) {
			label = label.substring(0, label.length() - 1);
		}// if (label.length() > 1)
		// System.out.println("label="+label);
		return label;
	}// getLabel(int)

	/**
	 * get the name for this dataset
	 * 
	 * @return
	 */
	public String getName() {
		return tableModel.getColumnName(valueColumn + 1);
	}// getName()

	/*******************************************************************************************************************
	 * print out the elements and their labels in this object
	 * 
	 * @throws Exception
	 *             if there is Exception while printing
	 ******************************************************************************************************************/
	public void print() throws Exception {
		System.out.println();
		System.out.println(this.getName());
		for (int i = 0; i < this.getNumElements(); i++) {
			System.out.print(this.getLabel(i) + ", ");
		}
		System.out.println();
		for (int i = 0; i < this.getNumElements(); i++) {
			System.out.print(this.getElement(i) + ", ");
		}
		System.out.println();
	}// print()

}
