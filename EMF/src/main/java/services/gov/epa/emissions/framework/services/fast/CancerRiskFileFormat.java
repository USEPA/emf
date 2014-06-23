package gov.epa.emissions.framework.services.fast;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.DelimitedFileFormat;
import gov.epa.emissions.commons.io.FileFormatWithOptionalCols;
import gov.epa.emissions.commons.io.RealFormatter;
import gov.epa.emissions.commons.io.StringFormatter;
import gov.epa.emissions.commons.io.importer.FillDefaultValues;
import gov.epa.emissions.commons.io.importer.FillRecordWithBlankValues;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CancerRiskFileFormat implements FileFormatWithOptionalCols, DelimitedFileFormat {

    protected SqlDataTypes types;

    private FillDefaultValues filler;

    public CancerRiskFileFormat(SqlDataTypes types) {
        this(types, new FillRecordWithBlankValues());
    }

    public CancerRiskFileFormat(SqlDataTypes types, FillDefaultValues filler) {
        this.types = types;
        this.filler = filler;
    }

    public String identify() {
        return "ORL Point";
    }

    public Column[] cols() {
        return asArray(minCols(), optionalCols());
    }

    private Column[] asArray(Column[] minCols, Column[] optionalCols) {
        List<Column> list = new ArrayList<Column>();
        list.addAll(Arrays.asList(minCols));
        list.addAll(Arrays.asList(optionalCols));

        return list.toArray(new Column[0]);
    }

    public Column[] minCols() {
        List<Column> cols = new ArrayList<Column>();

        cols.add(new Column("CMAQ_POLLUTANT", types.stringType(64), 64, new StringFormatter(64)));
        cols.add(new Column("CANCER_RISK_URE", types.realType(), new RealFormatter()));
        
        return cols.toArray(new Column[0]);
    }

    public Column[] optionalCols() {
        List<Column> cols = new ArrayList<Column>();


        return cols.toArray(new Column[0]);
    }

    public void fillDefaults(List<Column> data, long datasetId) {
        filler.fill(this, data, datasetId);
    }

}
