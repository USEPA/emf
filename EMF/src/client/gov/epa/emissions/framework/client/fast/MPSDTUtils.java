package gov.epa.emissions.framework.client.fast;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class MPSDTUtils {

    public static Version[] getVersions(EmfSession session, EmfDataset dataset) throws EmfException {

        Version[] versions = new Version[0];

        if (dataset != null) {
            versions = session.dataEditorService().getVersions(dataset.getId());
        }

        return versions;
    }

    public static DatasetType getDatasetType(EmfSession session, String name) throws EmfException {
        return session.getLightDatasetType(name);
    }

}
