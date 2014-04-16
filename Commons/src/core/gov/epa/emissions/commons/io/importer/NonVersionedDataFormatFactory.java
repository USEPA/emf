package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.ExportStatement;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.NonVersionedTableFormat;
import gov.epa.emissions.commons.io.SimpleExportStatement;
import gov.epa.emissions.commons.io.TableFormat;

public class NonVersionedDataFormatFactory implements DataFormatFactory {

    public TableFormat tableFormat(FileFormat fileFormat, SqlDataTypes sqlDataTypes) {
        return new NonVersionedTableFormat(fileFormat, sqlDataTypes);
    }
    
    public TableFormat tableFormatWithLineNoCol(FileFormat fileFormat, SqlDataTypes sqlDataTypes, String lineNum) {
        return  new NonVersionedTableFormat(fileFormat, sqlDataTypes, lineNum);
    }

    public FillDefaultValues defaultValuesFiller() {
        return new FillRecordWithBlankValues();
    }

    public ExportStatement exportStatement() {
        return new SimpleExportStatement();
    }

    public Version getVersion() {
        return null;
    }

}
