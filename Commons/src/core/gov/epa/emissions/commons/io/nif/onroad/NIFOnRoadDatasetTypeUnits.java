package gov.epa.emissions.commons.io.nif.onroad;

import gov.epa.emissions.commons.data.DatasetTypeUnit;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.FormatUnit;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.nif.NIFDatasetTypeUnits;
import gov.epa.emissions.commons.io.nif.NIFImportHelper;

public abstract class NIFOnRoadDatasetTypeUnits implements NIFDatasetTypeUnits {

    protected FormatUnit emDatasetTypeUnit;

    protected FormatUnit peDatasetTypeUnit;

    protected FormatUnit trDatasetTypeUnit;

    protected NIFImportHelper delegate;

    public NIFOnRoadDatasetTypeUnits(SqlDataTypes sqlDataTypes, DataFormatFactory factory) {
        FileFormat emFileFormat = new EmissionRecordsFileFormat(sqlDataTypes);
        TableFormat emTableFormat = factory.tableFormat(emFileFormat, sqlDataTypes);
        emDatasetTypeUnit = new DatasetTypeUnit(emTableFormat, emFileFormat, false);

        FileFormat peFileFormat = new EmissionPeriodsFileFormat(sqlDataTypes);
        TableFormat peTableFormat = factory.tableFormat(peFileFormat, sqlDataTypes);
        peDatasetTypeUnit = new DatasetTypeUnit(peTableFormat, peFileFormat, false);

        FileFormat trFileFormat = new TemporalRecordsFileFormat(sqlDataTypes);
        TableFormat trTableFormat = factory.tableFormat(trFileFormat, sqlDataTypes);
        trDatasetTypeUnit = new DatasetTypeUnit(trTableFormat, trFileFormat, false);
        
        delegate = new NIFImportHelper();
    }

    public FormatUnit[] formatUnits() {
        return new FormatUnit[] { emDatasetTypeUnit, peDatasetTypeUnit, trDatasetTypeUnit };
    }
    
    public String dataTable(){
        return emDatasetTypeUnit.getInternalSource().getTable();
    }

    protected void requiredExist() throws ImporterException {
        FormatUnit[] reqUnits = { emDatasetTypeUnit, peDatasetTypeUnit };
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < reqUnits.length; i++) {
            if (reqUnits[i].getInternalSource() == null) {
                sb.append("\t" + reqUnits[i].fileFormat().identify() + "\n");
            }
        }

        if (sb.length() > 0) {
            throw new ImporterException("NIF onroad import requires following types \n" + sb.toString());
        }
    }

    protected FormatUnit keyToDatasetTypeUnit(String key) {
        if (key == null) {
            return null;
        }
        key = key.toLowerCase();
        if ("em".equals(key)) {
            return emDatasetTypeUnit;
        }

        if ("pe".equals(key)) {
            return peDatasetTypeUnit;
        }

        if ("tr".equals(key)) {
            return trDatasetTypeUnit;
        }
        return null;
    }

}
