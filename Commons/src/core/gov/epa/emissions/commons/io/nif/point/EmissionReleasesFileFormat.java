package gov.epa.emissions.commons.io.nif.point;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.FixedWidthFileFormat;
import gov.epa.emissions.commons.io.nif.NIFFileFormat;

public class EmissionReleasesFileFormat implements FileFormat, FixedWidthFileFormat {

    private Column[] cols;

    public EmissionReleasesFileFormat(SqlDataTypes types) {
        cols = createCols(types);
    }

    public String identify() {
        return "NIF3.0 Point Emission Releases";
    }

    public Column[] cols() {
        return cols; 
    }
    
    private Column[] createCols(SqlDataTypes types) {
        String [] names ={"record_type", "state_county_fips", "state_facility_id", "spacer1", "emission_point_id", "emission_point_type", "spacer2", "stack_height", "stack_diameter", "stack_fenceline_dist", "exit_gas_temp", "exit_gas_velocity", "exit_gas_flow_rate", "x_coordinate", "y_coordinate", "utm_zone", "coordinate_type", "horiz_area_fugitive", "rel_height_fugitive", "fugitive_dims_units", "emission_point_desc", "submittal_flag", "horiz_coll_method_code", "horiz_acc_measure", "horiz_ref_datum_code", "reference_point_code", "source_map_scale_no", "coord_data_source_code", "tribal_code"};
        String [] colTypes={"C", "C", "C", "C", "C", "C", "C", "N", "N", "N", "N", "N", "N", "N", "N", "N", "C", "N", "N", "C", "C", "C", "C", "C", "C", "C", "C", "C", "C"};
        int [] widths = {2, 5, 15, 6, 6, 2, 10, 10, 10, 8, 10, 10, 10, 11, 10, 2, 8, 8, 8, 10, 80, 4, 3, 6, 3, 3, 10, 3, 4};
        
        NIFFileFormat format = new NIFFileFormat(types);
        return format.createCols(names,colTypes,widths);
    }
    
}
