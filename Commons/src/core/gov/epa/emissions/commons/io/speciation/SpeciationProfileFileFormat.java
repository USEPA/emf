package gov.epa.emissions.commons.io.speciation;

import java.util.ArrayList;
import java.util.List;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.DelimitedFileFormat;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.RealFormatter;
import gov.epa.emissions.commons.io.StringFormatter;

public class SpeciationProfileFileFormat implements FileFormat, DelimitedFileFormat {

    private Column[] columns;
    
    public SpeciationProfileFileFormat(SqlDataTypes type){
        columns = createCols(type);
    }
    
    public String identify() {
        return "Chem Speciation Profile";
    }

    public Column[] cols() {
        return columns;
    }
    
    private Column[] createCols(SqlDataTypes types) {
        List<Column> columns = new ArrayList<Column>();

        columns.add(new Column("CODE", types.stringType(10), 10, new StringFormatter(10)));
        columns.add(new Column("POLLUTANT", types.stringType(16), 16, new StringFormatter(16)));
        columns.add(new Column("SPECIES", types.stringType(16), 16, new StringFormatter(16)));
        columns.add(new Column("SPLIT", types.realType(), new RealFormatter()));
        columns.add(new Column("DIVISOR", types.realType(), new RealFormatter()));
        columns.add(new Column("MASSFRAC", types.realType(), new RealFormatter()));

        return columns.toArray(new Column[0]);
    }

}
