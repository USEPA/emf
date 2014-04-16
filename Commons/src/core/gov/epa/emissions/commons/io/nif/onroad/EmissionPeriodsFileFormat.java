package gov.epa.emissions.commons.io.nif.onroad;

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
        return "NIF3.0 Onroad Emission Periods";
    }

    public Column[] cols() {
        return cols;
    }

    private Column[] createCols(SqlDataTypes types) {
        String [] names ={"record_type", "state_county_fips", "scc", "start_date", "end_date", "spacer1", "start_time", "end_time", "actual_throughput", "throughput_unit", "submital_flag", "tribal_code"};
        String [] colTypes ={"C", "C", "C", "C", "C", "C", "C", "C", "N", "C", "C", "C"};   
        int [] widths ={ 2, 5, 10, 8, 8, 2, 4, 4, 12, 8, 4, 4};  

        return new NIFFileFormat(types).createCols(names,colTypes,widths);
    }

}
