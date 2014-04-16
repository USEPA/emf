package gov.epa.emissions.commons.io.ida;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.StringFormatter;
import gov.epa.emissions.commons.io.TableFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IDATableFormat implements TableFormat {

    private FileFormat base;

    private SqlDataTypes types;

    public IDATableFormat(FileFormat base, SqlDataTypes types) {
        this.base = base;
        this.types = types;
    }

    public String key() {
        return "Dataset_Id";
    }

    public String identify() {
        return base.identify();
    }

    public Column[] cols() {
        List cols = new ArrayList();
        cols.addAll(Arrays.asList(base.cols()));

        Column state = new Column("STATE", types.stringType(2), new StringFormatter(2));
        cols.add(0, state);

        Column fips = new Column("FIPS", types.stringType(6), new StringFormatter(6));
        cols.add(1, fips);

        return (Column[]) cols.toArray(new Column[0]);
    }

    public int getBaseLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getOffset() {
        // TODO Auto-generated method stub
        return 0;
    }

}
