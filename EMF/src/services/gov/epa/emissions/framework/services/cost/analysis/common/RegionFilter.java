package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

public class RegionFilter {

    private GenerateRegionMap generateRegionMap;

    public RegionFilter(DbServer dbServer, 
            HibernateSessionFactory sessionFactory) {
        this.generateRegionMap = new GenerateRegionMap(dbServer, sessionFactory);
    }

    public boolean filter(ControlMeasure controlMeasure, String fips) throws EmfException {
        return regionFilter(controlMeasure, fips);
    }

    private boolean regionFilter(ControlMeasure controlMeasure, String fips) throws EmfException {
        EmfDataset regionDataset = controlMeasure.getRegionDataset();

        //see if a region was specified, if NOT then this measure is OK since there is no region to consider
        if (regionDataset == null) 
            return true;
        
        //so a region was specified, lets see if this fips is included in the region dataset
        int regionDatasetVersion = controlMeasure.getRegionDatasetVersion();

        //go get the region map
        RegionMap regionMap = generateRegionMap.getRegionMap(regionDataset, regionDatasetVersion);
        
        if (regionMap.hasFips(regionDataset.getId() + "_" + regionDatasetVersion, fips))
            return true;

        //this fips wasn't in the region dataset, so this measure can't be considered for this source.
        return false;
    }
}
