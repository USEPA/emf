package gov.epa.mims.analysisengine.tree;

import java.io.Serializable;

/**
 * class to store plot info -type of plot name -Class of plot type -array of DataSetInfo
 * 
 * @version $Revision: 1.5 $
 * @author Tommy E. Cathey
 */
public class PlotInfo extends AvailableOptionsAndDefaults implements Serializable {
	/** serial version UID */
	static final long serialVersionUID = 1;

	/** class of this plot type */
	private final Class plotClass;

	/** plot type name */
	private final String plotTypeName;

	/** array of DataSetInfo for this plot type */
	private final DataSetInfo[] dataSetInfos;

	/*******************************************************************************************************************
	 * Creates a new PlotInfo object.
	 * 
	 * @param plotTypeName
	 *            plot type name
	 * @param plotClass
	 *            class of this plot type
	 * @param allKeywords
	 *            allowed AnlysisOption keywords for this plot type
	 * @param dataSetInfos
	 *            array of DataSetInfo for this plot type
	 ******************************************************************************************************************/
	public PlotInfo(String plotTypeName, Class plotClass, String[] allKeywords, DataSetInfo[] dataSetInfos) {
		super(allKeywords);
		this.plotTypeName = plotTypeName;
		this.plotClass = plotClass;
		this.dataSetInfos = (DataSetInfo[]) dataSetInfos.clone();
	}

	/*******************************************************************************************************************
	 * returns array of DataSetInfo for this plot type
	 * 
	 * @return array of DataSetInfo for this plot type
	 ******************************************************************************************************************/
	public DataSetInfo[] getDataSetInfo() {
		return (dataSetInfos != null) ? (DataSetInfo[]) dataSetInfos.clone() : null;
	}

	/*******************************************************************************************************************
	 * returns number of dataSetInfos
	 * 
	 * @return length of dataSetInfos array
	 ******************************************************************************************************************/
	public int getNumDataSetInfo() {
		return dataSetInfos.length;
	}

	/*******************************************************************************************************************
	 * returns class of this plot type
	 * 
	 * @return class of this plot type
	 ******************************************************************************************************************/
	public Class getPlotClass() {
		return plotClass;
	}

	/*******************************************************************************************************************
	 * returns plot type name
	 * 
	 * @return plot type name
	 ******************************************************************************************************************/
	public String getPlotTypeName() {
		return plotTypeName;
	}

	/*******************************************************************************************************************
	 * Compares this object to the specified object.
	 * 
	 * @param o
	 *            the object to compare this object against
	 * 
	 * @return true if the objects are equal; false otherwise
	 ******************************************************************************************************************/
	public boolean equals(Object o) {
		boolean rtrn = true;

		if (!super.equals(o)) {
			rtrn = false;
		} else {
			PlotInfo other = (PlotInfo) o;
			rtrn = ((plotTypeName == null) ? (other.plotTypeName == null) : (plotTypeName.equals(other.plotTypeName)))
					&& (plotClass == other.plotClass) && Util.equals(dataSetInfos, other.dataSetInfos);
		}

		return rtrn;
	}

	/*******************************************************************************************************************
	 * print method for debugging should be removed
	 ******************************************************************************************************************/
	public void printDataSetInfo() {
		for (int i = 0; i < dataSetInfos.length; ++i) {
			System.out.println(dataSetInfos[i]);
		}
	}

	/*******************************************************************************************************************
	 * describe object in a String
	 * 
	 * @return String describing object
	 ******************************************************************************************************************/
	public String toString() {
		return Util.toString(this);
	}
}