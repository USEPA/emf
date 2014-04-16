package gov.epa.emissions.commons.io.nif.nonpointNonroad;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.FormatUnit;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.nif.NIFImportHelper;

import java.io.File;

public class NIFNonRoadFileDatasetTypeUnits extends NIFNonRoadDatasetTypeUnits {

    private NIFImportHelper delegate;

    private File[] files;

    private String tablePrefix;

    public NIFNonRoadFileDatasetTypeUnits(File[] files, String tablePrefix, SqlDataTypes sqlDataTypes,
            DataFormatFactory factory) {
        super(sqlDataTypes, factory);
        this.files = files;
        this.tablePrefix = tablePrefix;
        delegate = new NIFImportHelper();
    }

    public void process() throws ImporterException {
        associateFileWithUnit(files, tablePrefix);
        requiredExist();
    }

    private void associateFileWithUnit(File[] files, String tableName) throws ImporterException {
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            String key = delegate.notation(file);
            FormatUnit formatUnit = keyToDatasetTypeUnit(key);
            if (formatUnit != null) {
                formatUnit.setInternalSource(delegate.internalSource(tableName, key, file, formatUnit));

            }
        }
    }

}
