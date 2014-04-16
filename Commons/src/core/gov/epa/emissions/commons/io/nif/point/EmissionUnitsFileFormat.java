package gov.epa.emissions.commons.io.nif.point;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.FixedWidthFileFormat;
import gov.epa.emissions.commons.io.nif.NIFFileFormat;

public class EmissionUnitsFileFormat implements FileFormat, FixedWidthFileFormat {

    private Column[] cols;

    public EmissionUnitsFileFormat(SqlDataTypes types) {
        cols = createCols(types);
    }

    public String identify() {
        return "NIF3.0 Point Emission Units";
    }

    public Column[] cols() {
        return cols; 
    }
    
    private Column[] createCols(SqlDataTypes types) {
        String [] names = {"record_type", "state_county_fips", "state_facility_id", "emission_unit_id", "oris_boiler_id", "unit_sic_code", "unit_naics_code", "spacer1", "dsgn_cap", "dsgn_cap_unit_enum", "dsgn_cap_unit_denom", "max_nameplate_cap", "emission_unit_desc", "submittal_flag", "tribal_code"};
        String [] colTypes= {"C", "C", "C", "C", "C", "C", "C", "C", "N", "C", "C", "N", "C", "C", "C"};
        int [] widths= {2, 5, 15, 6, 5, 4, 6, 2, 10, 10, 10, 10, 80, 4, 4};
        
        NIFFileFormat format = new NIFFileFormat(types);
        return format.createCols(names,colTypes,widths);
    }
    
}
