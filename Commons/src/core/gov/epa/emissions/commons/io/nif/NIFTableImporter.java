package gov.epa.emissions.commons.io.nif;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.TableDefinition;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.FormatUnit;
import gov.epa.emissions.commons.io.importer.ImporterException;

import java.util.ArrayList;
import java.util.List;

public class NIFTableImporter {

    private Dataset dataset;

    private NIFDatasetTypeUnits datasetTypeUnits;

    private Datasource datasource;

    public NIFTableImporter(String[] tables, Dataset dataset, NIFDatasetTypeUnits datasetTypeUnits, DbServer dbServer) throws ImporterException {
        this.dataset = dataset;
        this.datasetTypeUnits = datasetTypeUnits;
        this.datasource = dbServer.getEmissionsDatasource();
        validate(tables);
    }

    private void validate(String[] tables) throws ImporterException {
        validateTableNames(tables);
        datasetTypeUnits.process();
        updateInternalSources(datasetTypeUnits.formatUnits(), dataset);
    }


    public void run() throws ImporterException {
        loadDataset();
    }
    
    private void validateTableNames(String[] tables) throws ImporterException {
        TableDefinition tableDefinition = datasource.tableDefinition();
        List notExist = new ArrayList();
        try {
            for (int i = 0; i < tables.length; i++) {
                if (!tableDefinition.tableExists(tables[i])) {
                    notExist.add(tables[i]);
                }
            }
        } catch (Exception e) {
            throw new ImporterException(e.getMessage());
        }
        if(!notExist.isEmpty()){
            throw new ImporterException("The tables '"+notExist.toString()+"' do not exist");
        }
    }


    private void loadDataset() throws ImporterException {
        String table = datasetTypeUnits.dataTable();
        new NIFImportHelper().loadDataset(table,dataset,datasource);
    }

    private void updateInternalSources(FormatUnit[] formatUnits, Dataset dataset) {
        List sources = new ArrayList();
        for (int i = 0; i < formatUnits.length; i++) {
            InternalSource source = formatUnits[i].getInternalSource();
            if (source != null) {
                sources.add(source);
                source.setCols(colNames(formatUnits[i].fileFormat().cols()));
            }
        }
        dataset.setInternalSources((InternalSource[]) sources.toArray(new InternalSource[0]));
    }

    //TODO: use HelpImporter
    private String[] colNames(Column[] cols) {
        List names = new ArrayList();
        for (int i = 0; i < cols.length; i++)
            names.add(cols[i].name());

        return (String[]) names.toArray(new String[0]);
    }


    public InternalSource[] internalSources() {
        return dataset.getInternalSources();
    }

}
