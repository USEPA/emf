package gov.epa.emissions.commons.io;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.importer.FillDefaultValues;

public interface DataFormatFactory {

    TableFormat tableFormat(FileFormat fileFormat, SqlDataTypes sqlDataTypes);

    TableFormat tableFormatWithLineNoCol(FileFormat fileFormat, SqlDataTypes sqlDataTypes, String lineNum);
    
    FillDefaultValues defaultValuesFiller();

    ExportStatement exportStatement() throws ExporterException;
    
    Version getVersion();

}
