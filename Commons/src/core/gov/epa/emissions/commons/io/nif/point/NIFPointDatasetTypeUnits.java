package gov.epa.emissions.commons.io.nif.point;

import gov.epa.emissions.commons.data.DatasetTypeUnit;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.FormatUnit;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.nif.NIFDatasetTypeUnits;
import gov.epa.emissions.commons.io.nif.NIFImportHelper;

public abstract class NIFPointDatasetTypeUnits implements NIFDatasetTypeUnits {

    protected FormatUnit ceDatasetTypeUnit;

    protected FormatUnit emDatasetTypeUnit;

    protected FormatUnit epDatasetTypeUnit;

    protected FormatUnit peDatasetTypeUnit;

    protected DatasetTypeUnit erDatasetTypeUnit;

    protected DatasetTypeUnit euDatasetTypeUnit;

    protected DatasetTypeUnit siDatasetTypeUnit;

    protected NIFImportHelper delegate;

    public NIFPointDatasetTypeUnits(SqlDataTypes sqlDataTypes, DataFormatFactory factory) {
        FileFormat ceFileFormat = new ControlEquipmentFileFormat(sqlDataTypes);
        TableFormat ceTableFormat = factory.tableFormat(ceFileFormat, sqlDataTypes);
        ceDatasetTypeUnit = new DatasetTypeUnit(ceTableFormat, ceFileFormat, false);

        FileFormat emFileFormat = new EmissionRecordsFileFormat(sqlDataTypes);
        TableFormat emTableFormat = factory.tableFormat(emFileFormat, sqlDataTypes);
        emDatasetTypeUnit = new DatasetTypeUnit(emTableFormat, emFileFormat, false);

        FileFormat epFileFormat = new EmissionProcessFileFormat(sqlDataTypes);
        TableFormat epTableFormat = factory.tableFormat(epFileFormat, sqlDataTypes);
        epDatasetTypeUnit = new DatasetTypeUnit(epTableFormat, epFileFormat, false);
        
        FileFormat erFileFormat = new EmissionReleasesFileFormat(sqlDataTypes);
        TableFormat erTableFormat = factory.tableFormat(erFileFormat, sqlDataTypes);
        erDatasetTypeUnit = new DatasetTypeUnit(erTableFormat, erFileFormat, false);

        FileFormat euFileFormat = new EmissionUnitsFileFormat(sqlDataTypes);
        TableFormat euTableFormat = factory.tableFormat(euFileFormat, sqlDataTypes);
        euDatasetTypeUnit = new DatasetTypeUnit(euTableFormat, euFileFormat, false);

        FileFormat peFileFormat = new EmissionPeriodsFileFormat(sqlDataTypes);
        TableFormat peTableFormat = factory.tableFormat(peFileFormat, sqlDataTypes);
        peDatasetTypeUnit = new DatasetTypeUnit(peTableFormat, peFileFormat, false);

        FileFormat siFileFormat = new EmissionSitesFileFormat(sqlDataTypes);
        TableFormat siTableFormat = factory.tableFormat(siFileFormat, sqlDataTypes);
        siDatasetTypeUnit = new DatasetTypeUnit(siTableFormat, siFileFormat, false);
        delegate = new NIFImportHelper();
    }

    public FormatUnit[] formatUnits() {
        return new FormatUnit[] { ceDatasetTypeUnit, emDatasetTypeUnit, epDatasetTypeUnit, erDatasetTypeUnit,
                euDatasetTypeUnit, peDatasetTypeUnit, siDatasetTypeUnit };
    }

    public String dataTable() {
        return emDatasetTypeUnit.getInternalSource().getTable();
    }

    protected void requiredExist() throws ImporterException {
        FormatUnit[] reqUnits = { emDatasetTypeUnit, epDatasetTypeUnit, erDatasetTypeUnit, euDatasetTypeUnit,
                peDatasetTypeUnit, siDatasetTypeUnit };
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < reqUnits.length; i++) {
            if (reqUnits[i].getInternalSource() == null) {
                sb.append("\t" + reqUnits[i].fileFormat().identify() + "\n");
            }
        }

        if (sb.length() > 0) {
            throw new ImporterException("NIF point import requires following types \n" + sb.toString());
        }
    }

    protected FormatUnit keyToDatasetTypeUnit(String key) {
        if (key == null) {
            return null;
        }
        key = key.toLowerCase();
        if ("ce".equals(key)) {
            return ceDatasetTypeUnit;
        }

        if ("em".equals(key)) {
            return emDatasetTypeUnit;
        }

        if ("ep".equals(key)) {
            return epDatasetTypeUnit;
        }

        if ("eu".equals(key)) {
            return euDatasetTypeUnit;
        }

        if ("er".equals(key)) {
            return erDatasetTypeUnit;
        }

        if ("pe".equals(key)) {
            return peDatasetTypeUnit;
        }

        if ("si".equals(key)) {
            return siDatasetTypeUnit;
        }
        return null;
    }

}
