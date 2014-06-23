package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.exim.ExImService;

public class ExImServiceTransport implements ExImService {

    private DataMappings mappings;

    private EmfCall call;

    public ExImServiceTransport(EmfCall call) {
        this.call = call;
        mappings = new DataMappings();
    }

    public void exportDatasets(User user, EmfDataset[] datasets, Version[] versions, String folder, String prefix,
            boolean overwrite, String rowFilters, EmfDataset filterDataset,
            Version filterDatasetVersion, String filterDatasetJoinCondition, String colOrders, String purpose) throws EmfException {
        call.setOperation("exportDatasets");
        call.addParam("user", mappings.user());
        call.addParam("datasets", mappings.datasets());
        call.addParam("versions", mappings.versions());
        call.addStringParam("folder");
        call.addStringParam("prefix");
        call.addBooleanParameter("overwrite");
        call.addStringParam("rowFilters");
        call.addParam("filterDataset", mappings.dataset());
        call.addParam("filterDatasetVersion", mappings.version());
        call.addStringParam("filterDatasetJoinCondition");
        call.addStringParam("colOrders");
        call.addStringParam("purpose");
        call.setVoidReturnType();

        call.request(new Object[] { user, datasets, versions, folder, prefix, overwrite, rowFilters, filterDataset, filterDatasetVersion, filterDatasetJoinCondition, colOrders, purpose });
    }

    public void importDataset(User user, String folderPath, String[] fileNames, DatasetType datasetType,
            String datasetName) throws EmfException {
        call.setOperation("importDataset");
        call.addParam("user", mappings.user());
        call.addParam("folderPath", mappings.string());
        call.addParam("fileNames", mappings.strings());
        call.addParam("datasetType", mappings.datasetType());
        call.addParam("datasetName", mappings.string());
        call.setVoidReturnType();

        call.request(new Object[] { user, folderPath, fileNames, datasetType, datasetName });
    }

    public void importDatasets(User user, String folderPath, String[] fileNames, DatasetType datasetType)
            throws EmfException {
        call.setOperation("importDatasets");
        call.addParam("user", mappings.user());
        call.addParam("folderPath", mappings.string());
        call.addParam("fileNames", mappings.strings());
        call.addParam("datasetType", mappings.datasetType());
        call.setVoidReturnType();

        call.request(new Object[] { user, folderPath, fileNames, datasetType });
    }

    public String[] getFilenamesFromPattern(String folder, String pattern) throws EmfException {
        call.setOperation("getFilenamesFromPattern");
        call.addParam("folder", mappings.string());
        call.addParam("pattern", mappings.string());
        call.setReturnType(mappings.strings());

        return (String[]) call.requestResponse(new Object[] { folder, pattern });
    }

    public Version getVersion(Dataset dataset, int version) throws EmfException {
        call.setOperation("getVersion");
        // call.addIntegerParam("dataset"); //commented out since parameter and paramType mismatch
        call.addParam("dataset", mappings.dataset()); // added 07/13/2007
        call.addIntegerParam("version");
        call.setReturnType(mappings.version());

        return (Version) call.requestResponse(new Object[] { dataset, new Integer(version) });
    }

    /**
     * Added 07/16/2007 for exporting with Datasetids - Conrad
     */
    public void exportDatasetids(User user, Integer[] datasetIds, Version[] versions, String folder, String prefix,
            boolean overwrite, String rowFilters, EmfDataset filterDataset, Version filterDatasetVersion, String filterDatasetJoinCondition,
            String colOrders, String purpose) throws EmfException {
        call.setOperation("exportDatasetids");
        call.addParam("user", mappings.user());
        call.addParam("datasetids", mappings.integers());
        call.addParam("versions", mappings.versions());
        call.addStringParam("folder");
        call.addStringParam("prefix");
        call.addBooleanParameter("overwrite");
        call.addStringParam("rowFilters");
        call.addParam("filterDataset", mappings.dataset());
        call.addParam("filterDatasetVersion", mappings.version());
        call.addStringParam("filterDatasetJoinCondition");
        call.addStringParam("colOrders");
        call.addStringParam("purpose");
        call.setVoidReturnType();

        call.request(new Object[] { user, datasetIds, versions, folder, prefix, overwrite, rowFilters, filterDataset, filterDatasetVersion, filterDatasetJoinCondition, colOrders, purpose });
    }

    public String printStatusExportTaskManager() throws EmfException {
        call.setOperation("printStatusExportTaskManager");
        call.setStringReturnType();
        return (String) call.requestResponse(new Object[] { });
    }

    public String printStatusImportTaskManager() throws EmfException {
        call.setOperation("printStatusImportTaskManager");
        call.setStringReturnType();
        return (String) call.requestResponse(new Object[] { });
    }

    public void downloadDatasets(User user, Integer[] datasetIds, Version[] versions, String prefix, String rowFilters,
            EmfDataset filterDataset, Version filterDatasetVersion, String filterDatasetJoinCondition,
            String colOrders, String purpose) throws EmfException {
        call.setOperation("downloadDatasets");
        call.addParam("user", mappings.user());
        call.addParam("datasetids", mappings.integers());
        call.addParam("versions", mappings.versions());
        call.addStringParam("prefix");
        call.addStringParam("rowFilters");
        call.addParam("filterDataset", mappings.dataset());
        call.addParam("filterDatasetVersion", mappings.version());
        call.addStringParam("filterDatasetJoinCondition");
        call.addStringParam("colOrders");
        call.addStringParam("purpose");
        call.setVoidReturnType();

        call.request(new Object[] { user, datasetIds, versions, prefix, rowFilters, filterDataset, filterDatasetVersion, filterDatasetJoinCondition, colOrders, purpose });
    }

}
