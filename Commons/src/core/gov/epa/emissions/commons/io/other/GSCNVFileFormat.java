package gov.epa.emissions.commons.io.other;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.DelimitedFileFormat;
import gov.epa.emissions.commons.io.FileFormatWithOptionalCols;
import gov.epa.emissions.commons.io.RealFormatter;
import gov.epa.emissions.commons.io.StringFormatter;
import gov.epa.emissions.commons.io.importer.FillDefaultValues;
import gov.epa.emissions.commons.io.importer.FillRecordWithBlankValues;

public class GSCNVFileFormat implements FileFormatWithOptionalCols, DelimitedFileFormat {

    private SqlDataTypes types;

    private FillDefaultValues filler;

    private Column[] minCols;

    private Column[] optionalCols;

    public GSCNVFileFormat(SqlDataTypes types) {
        this(types, new FillRecordWithBlankValues());
    }

    public GSCNVFileFormat(SqlDataTypes types, FillDefaultValues filler) {
        this.types = types;
        this.filler = filler;
        this.minCols = createMinCols();
        this.optionalCols = new Column[0];
    }

    public String identify() {
        return "GSCNV";
    }

    public Column[] cols() {
        return asArray(minCols, optionalCols);
    }

    private Column[] asArray(Column[] minCols, Column[] optionalCols) {
        List list = new ArrayList();
        list.addAll(Arrays.asList(minCols));
        list.addAll(Arrays.asList(optionalCols));

        return (Column[]) list.toArray(new Column[0]);
    }

    public Column[] minCols() {
        return minCols;
    }

    public Column[] optionalCols() {
        return optionalCols;
    }

    private Column[] createMinCols() {
        List cols = new ArrayList();

        cols.add(new Column("Pollutant_1", types.stringType(16), 16, new StringFormatter(16)));
        cols.add(new Column("Pollutant_2", types.stringType(16), 16, new StringFormatter(16)));
        cols.add(new Column("Speciation_Code", types.stringType(10), 10, new StringFormatter(10)));
        cols.add(new Column("Factor", types.realType(), new RealFormatter()));

        return (Column[]) cols.toArray(new Column[0]);
    }

    public void fillDefaults(List data, long datasetId) {
        filler.fill(this, data, datasetId);
    }
}
