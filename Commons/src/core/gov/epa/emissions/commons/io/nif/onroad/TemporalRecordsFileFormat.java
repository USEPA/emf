package gov.epa.emissions.commons.io.nif.onroad;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.FixedWidthFileFormat;
import gov.epa.emissions.commons.io.nif.NIFFileFormat;

public class TemporalRecordsFileFormat implements FileFormat, FixedWidthFileFormat {

    private Column[] cols;

    public TemporalRecordsFileFormat(SqlDataTypes types) {
        cols = createCols(types);
    }

    public String identify() {
        return "NIF3.0 Onroad Temporal Records";
    }

    public Column[] cols() {
        return cols;
    }

    private Column[] createCols(SqlDataTypes types) {
        String[] names = { "record_type", "state_county_fips", "org_name", "trans_type", "inventory_year", "inventory_type", "trans_creation_date", "incr_submittal_no", "reliability_ind", "trans_comments", "contact_name", "contact_phone", "contact_phone_type", "e_address", "e_address_type", "source_type", "affiliation_type", "format_version", "tribal_code" };
        String[] colTypes = { "C", "C", "C", "C", "N", "C", "N", "N", "N", "C", "C", "C", "C", "C", "C", "C", "C", "N", "C" };
        int[] widths = { 2, 5, 80, 2, 4, 10, 8, 4, 5, 80, 70, 15, 10, 100, 10, 25, 40, 4, 4 };
        
        return new NIFFileFormat(types).createCols(names,colTypes,widths);
    }

}
