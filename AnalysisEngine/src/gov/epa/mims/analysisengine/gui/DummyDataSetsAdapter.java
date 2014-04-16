package gov.epa.mims.analysisengine.gui;

import gov.epa.mims.analysisengine.tree.DataSetIfc;
import gov.epa.mims.analysisengine.tree.DataSets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * An concrete data sets adapter that can generate dummy data sets of the
 * types requested and return datasets of the requested type.
 *
 * @author Alison Eyth
 * @version $Id: DummyDataSetsAdapter.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 *
 **/

public class DummyDataSetsAdapter //implements DataSetsAdapter
{
   /** serial version UID */
   static final long serialVersionUID = 1;

   ArrayList allDatasets = new ArrayList();

   public void DummyDataSetsAdapter()
   {
   }

   /**
    * @param type Class that specifies the type of data set. If = null,
    *    return all data sets.
    * @return DataSets the data sets with the specified type */
   public DataSetIfc [] getDataSets(Class type, java.awt.Component owner)
   {
      // TBD:
      // foreach dataset in allDatasets
      //    if current dataset is an instance of class "type"
      //       add current dataset to the list of data sets to return
      // return all matching datasets

      if (type == null)
      {
         return (DataSetIfc [])allDatasets.toArray();
      }
      else
      {
         // TBD find all data sets with requested type and return those
      }
      return (DataSetIfc [])allDatasets.toArray();  // update later
   }

   /**
    * Synthesize new data sets of the specified type.  Alternatively, call
    * getDummyDataSets for the Plot of interest and add those.
    * @param numDatasets int number of datasets to create
    * @param numDataPoints int number of data points to create in each dataset
    * @param type Class of datasets to create
    * @return boolean whether the add was successful
    */
   public boolean addDummyDataSets(int numDataSets, int numDataPoints, Class type)
   {
      // TBD: synthesize the specified number of datasets with the specified
      //      number of data points and add them to allDatasets
      return true;
   }

   /**
    * Add data sets to the current set of data sets known by the adapter.
    * @param newDatasets Datasets to add.
    * @return boolean whether the add succeeded
    */
   public boolean addDataSets(DataSetIfc [] newDatasets)
   {
      for (int i = 0; i < newDatasets.length; i++)
      {
         allDatasets.add(newDatasets[i]);
      }
      return true;
   }
}

