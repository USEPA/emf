package gov.epa.emissions.commons.io.temporal;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.DelimitedFileFormat;
import gov.epa.emissions.commons.io.FileFormatWithOptionalCols;
import gov.epa.emissions.commons.io.IntegerFormatter;
import gov.epa.emissions.commons.io.StringFormatter;
import gov.epa.emissions.commons.io.importer.FillDefaultValues;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TemporalReferenceFileFormat implements FileFormatWithOptionalCols, DelimitedFileFormat {

    private Column[] requiredCols;

    private Column[] optionalCols;

    private FillDefaultValues filler;

    public TemporalReferenceFileFormat(SqlDataTypes types, FillDefaultValues filler) {
        this.requiredCols = createRequiredCols(types);
        this.optionalCols = createOptionalCols(types);
        this.filler = filler;
    }

    public String identify() {
        return "Temporal Cross-Reference";
    }

    public Column[] cols() {
        List<Column> allCols = new ArrayList<Column>();
        allCols.addAll(Arrays.asList(requiredCols));
        allCols.addAll(Arrays.asList(optionalCols));

        return allCols.toArray(new Column[0]);
    }

    public Column[] optionalCols() {
        return optionalCols;
    }

    public Column[] minCols() {
        return requiredCols;
    }

    public void fillDefaults(List data, long datasetId) {
        filler.fill(this, data, datasetId);

    }

    public Column[] createRequiredCols(SqlDataTypes types) {
        List<Column> columns = new ArrayList<Column>();

        columns.add(new Column("SCC", types.stringType(10), 10, new StringFormatter(10)));
        columns.add(new Column("Monthly_Code", types.intType(), new IntegerFormatter()));
        columns.add(new Column("Weekly_Code", types.intType(), new IntegerFormatter()));
        columns.add(new Column("Diurnal_Code", types.intType(), new IntegerFormatter()));
        

        return columns.toArray(new Column[0]);
    }

    private Column[] createOptionalCols(SqlDataTypes types) {
        List<Column> columns = new ArrayList<Column>();
        
        columns.add(new Column("Pollutants", types.stringType(32), 32, new StringFormatter(32)));
        columns.add(new Column("FIPS", types.stringType(6), 6, new StringFormatter(6)));
        columns.add(new Column("LinkID_PlantID", types.stringType(32), 32, new StringFormatter(32)));
        columns.add(new Column("Characteristic_1", types.stringType(32), 32, new StringFormatter(32)));
        columns.add(new Column("Characteristic_2", types.stringType(32), 32, new StringFormatter(32)));
        columns.add(new Column("Characteristic_3", types.stringType(32), 32, new StringFormatter(32)));
        columns.add(new Column("Characteristic_4", types.stringType(32), 32, new StringFormatter(32)));
        columns.add(new Column("Characteristic_5", types.stringType(32), 32, new StringFormatter(32)));

        return columns.toArray(new Column[0]);
    }

}
