package gov.epa.emissions.framework.services.exim;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

import org.springframework.remoting.jaxrpc.ServletEndpointSupport;

@SuppressWarnings("deprecation")
public class ExImServiceWSEndPoint extends ServletEndpointSupport implements ExImService {

	private ExImService exImService;

	protected void onInit() {
		this.exImService = (ExImService) getWebApplicationContext().getBean(
				"exImService");
	}

    public void importDatasets(User user, String folderPath, String[] fileNames, DatasetType datasetType)
            throws EmfException {
        exImService.importDatasets(user, folderPath, fileNames, datasetType);
    }

    public String[] getFilenamesFromPattern(String folder, String pattern) throws EmfException {
        return exImService.getFilenamesFromPattern(folder, pattern);
    }

    public void importDataset(User user, String folderPath, String[] filenames, DatasetType datasetType,
            String datasetName) throws EmfException {
        exImService.importDataset(user, folderPath, filenames, datasetType, datasetName);
    }

    public void exportDatasets(User user, EmfDataset[] datasets, Version[] versions, String folder, String prefix,
            boolean overwrite, String rowFilters, EmfDataset filterDataset, Version filterDatasetVersion,
            String filterDatasetJoinCondition, String colOrders, String purpose) throws EmfException {
        exImService.exportDatasets(user, datasets, versions, folder, prefix, overwrite, rowFilters, filterDataset,
                filterDatasetVersion, filterDatasetJoinCondition, colOrders, purpose);
    }

    public Version getVersion(Dataset dataset, int version) throws EmfException {
        return exImService.getVersion(dataset, version);
    }

    public void exportDatasetids(User user, Integer[] datasetIds, Version[] versions, String folder, String prefix,
            boolean overwrite, String rowFilters, EmfDataset filterDataset, Version filterDatasetVersion,
            String filterDatasetJoinCondition, String colOrders, String purpose) throws EmfException {
        exImService.exportDatasetids(user, datasetIds, versions, folder, prefix, overwrite, rowFilters, filterDataset,
                filterDatasetVersion, filterDatasetJoinCondition, colOrders, purpose);
    }

    public void downloadDatasets(User user, Integer[] datasetIds, Version[] versions, String prefix, String rowFilters,
            EmfDataset filterDataset, Version filterDatasetVersion, String filterDatasetJoinCondition,
            String colOrders, String purpose) throws EmfException {
        exImService.downloadDatasets(user, datasetIds, versions, prefix, rowFilters, filterDataset,
                filterDatasetVersion, filterDatasetJoinCondition, colOrders, purpose);
    }

    public String printStatusExportTaskManager() throws EmfException {
        return exImService.printStatusExportTaskManager();
    }

    public String printStatusImportTaskManager() throws EmfException {
        return exImService.printStatusImportTaskManager();
    }
}
