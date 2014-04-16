package gov.epa.mims.analysisengine.table;

import java.util.Vector;

/**
 * FilesInTabbedPane.java Store the file names which are shown on the tab panes and remove or add when tabs closes and
 * opened. Also this class will be used to get a unique name for a filename to be shown in the tab tabName UniqueTabName
 * --- ---- --- ---
 * 
 * Created on March 30, 2004, 11:34 AM
 * 
 * @author parthee
 * @version $Id: FilesInTabbedPane.java,v 1.3 2006/10/30 21:43:50 parthee Exp $
 */
public class FilesInTabbedPane {

	/* to store the all the file names */
	private Vector fileNames;

	/** to stor the initially defined tab names */
	private Vector tabNames;

	/** store the unique names */
	private Vector uniqueTabNames;

	/** to count the tab no */
	private int tabNumber = -1;

	/** Creates a new instance of FilesInTabbedPane */
	public FilesInTabbedPane() {
		fileNames = new Vector();
		tabNames = new Vector();
		uniqueTabNames = new Vector();
		tabNumber = 0;
	}

	/**
	 * to add a file name
	 * 
	 * @param aFileName(absolutePath)
	 *            a name of file to be added to the tab
	 * @param aTabName
	 *            user specified tab Name
	 * @pre aFileName != null
	 * @pre aTabName != null
	 */
	public void addFileName(String aFileName, String aTabName) throws Exception {
		if (aFileName == null || aTabName == null) {
			throw new Exception("Tab name is null");
		}
		createUniqueTabName(aTabName);
		tabNames.add(aTabName);
		fileNames.add(aFileName);
		tabNumber++;
	}

	/*
	 * a helper method to create a unique tab name @param aTabName a file name @pre aFileName absolutePath
	 */
	private void createUniqueTabName(String aTabName) {
		int count = 0;
		int lastRepition = -1;
		// Don't do anything if the filename was null.
		if (aTabName == null)
			return;

		// check for duplicate tab names
		for (int i = 0; i < tabNames.size(); i++) {
			String name = (String) tabNames.get(i);
			if (aTabName.equals(name)) {
				lastRepition = i;
				count++;
			}
		}// for(i)
		if (count == 0) {
			uniqueTabNames.add(aTabName);
		}// if(count == 0)
		else if (count == 1) {
			uniqueTabNames.add(aTabName + "[" + (count + 1) + "]");
		} else {
			String lastReptUniqueName = (String) uniqueTabNames.get(lastRepition);
			int index = lastReptUniqueName.indexOf("]");
			if (index != -1) {
				int number = Integer.parseInt(lastReptUniqueName.substring(index - 1, index));
				uniqueTabNames.add(aTabName + "[" + (number + 1) + "]");
			} else {
				System.err.println("Programmer Error:FilesInTabbedPane.getUniqueFileName())");
				// This case should not occur
			}
			// uniqueTabNames.add(aTabName+"["+(count+1)+"]");
		}// else
	}// createUniqueTabName(String aTabName)

	public String getUniqueName(int tabNo) {
		return (String) uniqueTabNames.get(tabNo);
	}// getUniqueName(String fileName)

	public String[] getAllTabUniqueNames() {
		String[] names = new String[uniqueTabNames.size()];
		// System.out.println("names.length="+names.length);
		for (int i = 0; i < names.length; i++) {
			names[i] = (String) uniqueTabNames.get(i);
			// System.out.println("name="+names[i]);
		}// for(i)

		return names;
	}// getAllTabUniqueNames()

	/**
	 * WARNINTG tabNames should be unique
	 */
	public void setAllTabUniqueNames(String[] newTabNames) {
		// tabNames.clear();
		uniqueTabNames.clear();
		int length = newTabNames.length;
		for (int i = 0; i < length; i++) {
			// tabNames.add(newTabNames[i]);
			uniqueTabNames.add(newTabNames[i]);
			// System.out.println("newTabNames[" + i + "]="+ newTabNames[i]);
		}// for(i)
	}// setAllTabAndUniqueNames

	public int getTabCount() {
		return uniqueTabNames.size();
	}

	/**
	 * to remove a file from the tab
	 * 
	 * @parem closeTabName file to be closed
	 */
	public boolean remove(String closeTabName) {
		int index = uniqueTabNames.indexOf(closeTabName);
		if (index != -1) {
			fileNames.remove(index);
			tabNames.remove(index);
			uniqueTabNames.remove(index);
			tabNumber--;
			return true;
		}
		return false;
	}
}
