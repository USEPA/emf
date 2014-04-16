package gov.epa.mims.analysisengine.tree;

import java.io.Serializable;


/**
 * Data Set interface functions
 *
 * @author Tommy E. Cathey
 * @version $Id: LabeledDataSetIfc.java,v 1.3 2006/10/30 21:43:50 parthee Exp $
 *
 **************************************************/
public interface LabeledDataSetIfc
   extends DataSetIfc, Serializable
{
   /**
    * get data label
    *
    * @param i index into data series
    * @return element label
    * @throws java.lang.Exception if series is not open
    * @throws java.util.NoSuchElementException if i is out of range 
    **************************************************/
   String getLabel(int i)
            throws java.lang.Exception, 
                   java.util.NoSuchElementException;
}