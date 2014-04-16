package gov.epa.emissions.commons.io.nif.point;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.FormatUnit;
import gov.epa.emissions.commons.io.importer.ImporterException;

import java.io.File;

public class NIFPointFileDatasetTypeUnits extends NIFPointDatasetTypeUnits {

    private File[] files;

    private String tablePrefix;

    public NIFPointFileDatasetTypeUnits(File[] files, String tablePrefix, SqlDataTypes sqlDataTypes, 
            DataFormatFactory factory) {
        super(sqlDataTypes, factory);
        this.files = files;
        this.tablePrefix = tablePrefix;
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
