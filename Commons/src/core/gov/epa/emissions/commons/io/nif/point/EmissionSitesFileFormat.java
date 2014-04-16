package gov.epa.emissions.commons.io.nif.point;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.FixedWidthFileFormat;
import gov.epa.emissions.commons.io.nif.NIFFileFormat;

public class EmissionSitesFileFormat implements FileFormat, FixedWidthFileFormat {

    private Column[] cols;

    public EmissionSitesFileFormat(SqlDataTypes types) {
        cols = createCols(types);
    }

    public String identify() {
        return "NIF3.0 Point Emission Sites";
    }

    public Column[] cols() {
        return cols;
    }

    private Column[] createCols(SqlDataTypes types) {
        String[] names = { "record_type", "state_county_fips", "state_facility_id", "facility_registry_id", "facility_category", "oris_facility_code", "sic_code", "naics_code", "facility_name", "site_desc", "location_address", "location_city", "location_state", "location_zip", "location_country", "nti_site_id", "d_b_no", "tri_id", "submittal_flag", "tribal_code" };
        String[] colTypes = { "C", "C", "C", "C", "C", "C", "C", "C", "C", "C", "C", "C", "C", "C", "C", "C", "C", "C", "C", "C" };
        int[] widths = { 2, 5, 15, 12, 2, 6, 4, 6, 80, 40, 50, 60, 2, 14, 40, 20, 9, 20, 4, 4 };

        NIFFileFormat format = new NIFFileFormat(types);
        return format.createCols(names, colTypes, widths);
    }

}
