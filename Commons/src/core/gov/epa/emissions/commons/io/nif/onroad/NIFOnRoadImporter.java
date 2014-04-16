package gov.epa.emissions.commons.io.nif.onroad;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.importer.DataTable;
import gov.epa.emissions.commons.io.importer.FilesFromPattern;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.NonVersionedDataFormatFactory;
import gov.epa.emissions.commons.io.nif.NIFImporter;

import java.io.File;

public class NIFOnRoadImporter implements Importer {

    private NIFImporter delegate;

    public NIFOnRoadImporter(File folder, String[] files, Dataset dataset, DbServer dbServer, SqlDataTypes sqlDataTypes)
            throws ImporterException {
        this(folder, files, dataset, dbServer, sqlDataTypes, new NonVersionedDataFormatFactory());
    }

    public NIFOnRoadImporter(File folder, String[] filePatterns, Dataset dataset, DbServer dbServer, SqlDataTypes sqlDataTypes,
            DataFormatFactory factory) throws ImporterException {
        //FIXME: demo code
        File[] files = null;
        if(filePatterns.length==1){
            files = new FilesFromPattern(folder,filePatterns,dataset).files();
        }else{
            files = new File[filePatterns.length];
            for(int i = 0; i < filePatterns.length; i++) 
                files[i] = new File(folder, filePatterns[i]);
        }

        String tablePrefix = new DataTable(dataset, dbServer.getEmissionsDatasource()).name();
        delegate = new NIFImporter(files, dataset, new NIFOnRoadFileDatasetTypeUnits(files, tablePrefix, sqlDataTypes,
                factory), dbServer);
    }

    public void run() throws ImporterException {
        delegate.run();
    }

}
