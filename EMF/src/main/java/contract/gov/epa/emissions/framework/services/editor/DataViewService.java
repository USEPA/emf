package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.services.EmfException;

public interface DataViewService extends DataAccessService {
    DataAccessToken openSession(DataAccessToken token) throws EmfException;

    void closeSession(DataAccessToken token) throws EmfException;

}