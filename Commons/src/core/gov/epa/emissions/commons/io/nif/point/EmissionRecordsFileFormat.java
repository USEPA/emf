package gov.epa.emissions.commons.io.nif.point;

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
        return "NIF3.0 Point Emission Records";
    }

    public Column[] cols() {
        return cols;
    }

    private Column[] createCols(SqlDataTypes types) {
        String[] names = { "record_type", "state_county_fips", "state_facility_id", "emission_unit_id",
                "emission_process_id", "pollutant_code", "spacer1", "emission_point_id", "start_date", "end_date",
                "start_time", "end_time", "spacer2", "emission_value", "emission_units_code", "emission_type",
                "reliability_ind", "factor_value", "factor_units_num", "factor_units_denom", "material_code",
                "material_io", "spacer3", "calc_method_code", "ef_reliability_ind", "rule_effect",
                "rule_effect_method", "spacer4", "hap_emis_prf_lvl", "control_status", "emission_data_lvl",
                "submittal_flag", "tribal_code" };
        
        String[] colTypes = { "C", "C", "C", "C", "C", "C", "C", "C", "C", "C", "N", "N", "C", "N", "C", "C", "N", "N",
                "C", "C", "N", "C", "C", "C", "C", "N", "C", "C", "C", "C", "C", "C", "C" };
        int[] widths = { 2, 5, 15, 6, 6, 9, 7, 6, 8, 8, 4, 4, 10, 20, 10, 2, 5, 10, 10, 10, 4, 10, 5, 2, 5, 5, 2, 3, 2,
                12, 10, 4, 4 };
        NIFFileFormat format = new NIFFileFormat(types);
        return format.createCols(names, colTypes, widths);
    }

}
