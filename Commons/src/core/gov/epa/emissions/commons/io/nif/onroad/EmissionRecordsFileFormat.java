package gov.epa.emissions.commons.io.nif.onroad;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.FixedWidthFileFormat;
import gov.epa.emissions.commons.io.nif.NIFFileFormat;

public class EmissionRecordsFileFormat implements FileFormat, FixedWidthFileFormat {

    private Column[] cols;

    public EmissionRecordsFileFormat(SqlDataTypes types) {
        cols = createCols(types);
    }

    public String identify() {
        return "NIF3.0 Onroad Emission Records";
    }

    public Column[] cols() {
        return cols;
    }

    private Column[] createCols(SqlDataTypes types) {
        String [] names= {"record_type", "state_county_fips", "scc", "spacer1", "start_date", "end_date", "spacer2", "start_time", "end_time", "pollutant_code", "emission_desc", "emission_value", "emission_units_code", "emission_type", "reliability_ind", "submittal_flag", "tribal_code"};
        String [] colTypes ={"C", "C", "C", "C", "C", "C", "C", "C", "C", "C", "C", "N", "C", "C", "N", "C", "C"};
        int [] widths ={ 2, 5, 10, 10, 8, 8, 2, 4, 4, 9, 81, 10, 13, 4, 6, 8, 4};
        
        return new NIFFileFormat(types).createCols(names,colTypes,widths);
    }

}
