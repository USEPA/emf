package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.ida.IDASummary;
import gov.epa.emissions.commons.io.nif.nonpointNonroad.NIFNonpointNonRoadSummary;
import gov.epa.emissions.commons.io.nif.onroad.NIFOnRoadSummary;
import gov.epa.emissions.commons.io.nif.point.NIFPointSummary;
import gov.epa.emissions.commons.io.orl.ORLNonPointSummary;
import gov.epa.emissions.commons.io.orl.ORLNonRoadOnRoadSummary;
import gov.epa.emissions.commons.io.orl.ORLPointSummary;

public class SummaryTableFactory {

    private DbServer dbServer;

    public SummaryTableFactory(DbServer dbServer) {
        this.dbServer = dbServer;
    }

    public SummaryTable create(Dataset dataset) {
        DatasetType datasetType = dataset.getDatasetType();
        String prefix = datasetType.getName().toLowerCase();
        if (prefix.indexOf("nif3") >= 0)
            return nifSummaryTable(dbServer, dataset);
        if (prefix.indexOf("ida") >= 0)
            return idaSummaryTable(dataset);
        if (prefix.indexOf("orl") >= 0)
            return orlSummaryTable(dbServer, dataset);
        return null;
    }

    // FIXME: find a better way to create the importer than comparing
    // string
    private SummaryTable nifSummaryTable(DbServer dbServer, Dataset dataset) {
        Datasource emissions = dbServer.getEmissionsDatasource();
        Datasource reference = dbServer.getReferenceDatasource();

        DatasetType datasetType = dataset.getDatasetType();
        String name = datasetType.getName().toLowerCase();
        if (name.indexOf("nonpoint") >= 0)
            return new NIFNonpointNonRoadSummary(emissions, reference, dataset);

        if (name.indexOf("nonroad") >= 0)
            return new NIFNonpointNonRoadSummary(emissions, reference, dataset);

        if (name.indexOf("point") >= 0)
            return new NIFPointSummary(emissions, reference, dataset);

        if (name.indexOf("onroad") >= 0)
            return new NIFOnRoadSummary(emissions, reference, dataset);

        throw new RuntimeException("Dataset Type - " + name + " unsupported");
    }

    private SummaryTable idaSummaryTable(Dataset dataset) {
        return new IDASummary(dataset);
    }

    private SummaryTable orlSummaryTable(DbServer dbServer, Dataset dataset) {
        Datasource emissions = dbServer.getEmissionsDatasource();
        Datasource reference = dbServer.getReferenceDatasource();

        DatasetType datasetType = dataset.getDatasetType();
        String name = datasetType.getName().toLowerCase();
        if (name.indexOf("nonpoint") >= 0)
            return new ORLNonPointSummary(emissions, reference, dataset);
        if (name.indexOf("nonroad") >= 0 || name.indexOf("onroad") >= 0)
            return new ORLNonRoadOnRoadSummary(emissions, reference, dataset);
        if (name.indexOf("point") >= 0)
            return new ORLPointSummary(emissions, reference, dataset);

        throw new RuntimeException("Dataset Type - " + name + " unsupported");
    }

}
