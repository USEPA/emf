package gov.epa.emissions.commons.io.other;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.FixedWidthFileFormat;
import gov.epa.emissions.commons.io.IntegerFormatter;
import gov.epa.emissions.commons.io.RealFormatter;
import gov.epa.emissions.commons.io.StringFormatter;

public class InventoryTableFileFormat implements FileFormat, FixedWidthFileFormat {
    private Column[] cols;

    public InventoryTableFileFormat(SqlDataTypes types, int spacer) {
        cols = createCols(types, spacer);
    }

    public String identify() {
        return "Inventory Table Data";
    }

    public Column[] cols() {
        return cols;
    }

    private Column[] createCols(SqlDataTypes types, int spacer) {
        if (spacer < 0)
            spacer = 0;
        Column name = new Column("NAME", types.stringType(11), 11 + spacer, new StringFormatter(11, spacer));
        Column mode = new Column("MODE", types.stringType(3), 3 + spacer, new StringFormatter(3, spacer));
        Column cas = new Column("CAS", types.stringType(16), 16 + spacer, new StringFormatter(16, spacer));
        Column saroad = new Column("SPEC4ID", types.intType(), 5+spacer, new IntegerFormatter(5, spacer));
        Column react = new Column("REACT", types.intType(), 1 + spacer, new IntegerFormatter(1, spacer));
        Column keep = new Column("KEEP", types.stringType(1), 1 + spacer, new StringFormatter(1, spacer));
        Column factor = new Column("FACTOR", types.realType(), 6, new RealFormatter(6, 0));
        Column voctog = new Column("VOCTOG", types.stringType(1), 1 + spacer, new StringFormatter(1, spacer));
        Column species = new Column("SPECIES", types.stringType(1), 1 + spacer, new StringFormatter(1, spacer));
        Column explicit = new Column("EXPLICIT", types.stringType(1), 1 + spacer, new StringFormatter(1, spacer));
        Column activity = new Column("ACTIVITY", types.stringType(1), 1 + spacer, new StringFormatter(1, spacer));
        Column nti = new Column("NTI", types.intType(), 3 + spacer, new IntegerFormatter(3, spacer));
        Column units = new Column("UNITS", types.stringType(16), 16 + spacer, new StringFormatter(16, spacer));
        Column descrptn = new Column("DESCRPTN", types.stringType(40), 40, new StringFormatter(40));
        Column casdesc = new Column("CASDESC", types.stringType(40), 40, new StringFormatter(40));

        return new Column[] { name, mode, cas, saroad, react, keep, factor, voctog, species, explicit, activity, nti,
                units, descrptn, casdesc };
    }

}
