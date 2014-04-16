package gov.epa.emissions.framework.services.exim;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.Exporter;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
import gov.epa.emissions.commons.io.orl.FlexibleDBExporter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.lang.reflect.Constructor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VersionedExporterFactory {
    private static Log log = LogFactory.getLog(VersionedExporterFactory.class);

    private DbServer dbServer;

    private SqlDataTypes sqlDataTypes;

    private int batchSize;

    public VersionedExporterFactory(DbServer dbServer, SqlDataTypes sqlDataTypes, int batchSize) {
        this.dbServer = dbServer;
        this.sqlDataTypes = sqlDataTypes;
        this.batchSize = batchSize;
    }

    public Exporter create(Dataset dataset, Version version, String rowFilters, String colOrders, Dataset filterDataset, Version filterDatasetVersion, String filterDatasetJoinCondition) throws EmfException {
        try {
            String exporterName = dataset.getDatasetType().getExporterClassName();
            Class[] classParams;
            Object[] params;
            
            if (exporterName == null || exporterName.trim().isEmpty())
                throw new Exception("Exporter class name not specified with this dataset type.");

            Class exporterClass = Class.forName(exporterName);
//            if (exporterName.equals(DatasetType.FLEXIBLE_EXPORTER)){
//                Exporter exporter = new FlexibleDBExporter(dataset, dbServer,rowFilters, colOrders, 
//                        new VersionedDataFormatFactory(version, dataset), new Integer(batchSize));
//                return exporter;
//            }
            classParams = new Class[] { Dataset.class, String.class, DbServer.class,
                    DataFormatFactory.class, Integer.class, Dataset.class, Version.class, String.class };
            params = new Object[] { dataset, rowFilters, dbServer, new VersionedDataFormatFactory(version, dataset),
                    new Integer(batchSize), filterDataset, filterDatasetVersion, filterDatasetJoinCondition };

            Constructor exporterConstructor = exporterClass.getDeclaredConstructor(classParams);
            return (Exporter) exporterConstructor.newInstance(params);
        } catch (ClassNotFoundException e) {
            log.error("Failed to create exporter.", e);
            throw new EmfException("Exporter class name not found (either from database or commons.jar)--" + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not create Exporter", e);
            throw new EmfException("Could not create Exporter for Dataset Type: " + dataset.getDatasetTypeName() +"  "+ dataset.getDatasetType().getExporterClassName());
        }
    }

}
