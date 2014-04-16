package gov.epa.emissions.commons.io.nif.point;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.FixedWidthFileFormat;
import gov.epa.emissions.commons.io.nif.NIFFileFormat;

public class EmissionPeriodsFileFormat implements FileFormat, FixedWidthFileFormat {

    private Column[] cols;

    public EmissionPeriodsFileFormat(SqlDataTypes types) {
        cols = createCols(types);
    }

    public String identify() {
        return "NIF3.0 Point Emission Periods";
    }

    public Column[] cols() {
        return cols; 
    }
    
    private Column[] createCols(SqlDataTypes types) {
        String [] names = {"record_type", "state_county_fips", "state_facility_id", "emission_unit_id", "emission_process_id", "start_date", "end_date", "spacer1", "start_time", "end_time", "spacer2", "thruput", "thruput_units", "material_code", "material_io", "days_per_week", "weeks_per_period", "hours_per_day", "hours_per_period", "submittal_flag", "tribal_code"};
        String [] colTypes= {"C", "C", "C", "C", "C", "C", "C", "C", "N", "N", "C", "N", "C", "N", "C", "N", "N", "N", "N", "C", "C"};
        int [] widths= {2, 5, 15, 6, 6, 8, 8, 2, 4, 4, 10, 10, 10, 4, 10, 1, 2, 2, 4, 4, 4};

        NIFFileFormat format = new NIFFileFormat(types);
        return format.createCols(names,colTypes,widths);
    }
    
}
