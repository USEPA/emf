package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.services.EmfException;

public interface DataAccessService {
    // read
    /**
     * Applies constraints and returns Page 1
     */
    Page applyConstraints(DataAccessToken token, String rowFilter, String sortOrder) throws EmfException;
    // TODO: JIZHEN - debug page

    Page getPage(DataAccessToken token, int pageNumber) throws EmfException;

    int getPageCount(DataAccessToken token) throws EmfException;

    Page getPageWithRecord(DataAccessToken token, int record) throws EmfException;

    int getTotalRecords(DataAccessToken token) throws EmfException;

    // version-related
    Version[] getVersions(int datasetId) throws EmfException;
    
    Version getVersion(int datasetId, int version) throws EmfException;

    TableMetadata getTableMetadata(String table) throws EmfException;

}