package gov.epa.emissions.commons.io.nif.nonpointNonroad;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.FixedWidthFileFormat;
import gov.epa.emissions.commons.io.RealFormatter;
import gov.epa.emissions.commons.io.StringFormatter;

public class EmissionRecordsFileFormat implements FileFormat, FixedWidthFileFormat {

	private Column[] cols;
    
    private String identifier;

	public EmissionRecordsFileFormat(SqlDataTypes types, String identifier) {
		cols = createCols(types);
        this.identifier = identifier;
	}

	public String identify() {
		return identifier;
	}

	public Column[] cols() {
		return cols;
	}

	private Column[] createCols(SqlDataTypes types) {
		Column recordType = new Column("record_type", types.stringType(2), 2,
				new StringFormatter(2));
		Column state_county_fips = new Column("state_county_fips", types
				.stringType(5), 5, new StringFormatter(5));
		Column scc = new Column("scc", types.stringType(10), 10,
				new StringFormatter(10));
		Column pollutanCode = new Column("pollutant_code", types.stringType(9),
				9, new StringFormatter(9));
		Column spacer1 = new Column("spacer1", types.stringType(11), 11,
				new StringFormatter(11));
		Column startDate = new Column("start_date", types.stringType(8), 8,
				new StringFormatter(8));
		Column endDate = new Column("end_date", types.stringType(8), 8,
				new StringFormatter(8));
		Column spacer2 = new Column("spacer2", types.stringType(2), 2,
				new StringFormatter(2));
		Column startTime = new Column("start_time", types.realType(), 4,
				new RealFormatter(4,0));
		Column endTime = new Column("end_time", types.realType(), 4,
				new RealFormatter(4,0));
		Column emissionValue = new Column("emission_value", types.realType(),
				20, new RealFormatter(20,0));
		Column emissionUnitsCode = new Column("emission_units_code", types
				.stringType(10), 10, new StringFormatter(10));
		Column emissionType = new Column("emission_type", types.stringType(2),
				2, new StringFormatter(2));
		Column reliabilityInd = new Column("reliability_ind", types.realType(),
				5, new RealFormatter(5,0));
		Column factorValue = new Column("factor_value", types.realType(), 10,
				new RealFormatter(10,0));
		Column factorUnitsNum = new Column("factor_units_num", types
				.stringType(10), 10, new StringFormatter(10));
		Column factorUnitsDenom = new Column("factor_units_denom", types
				.stringType(10), 10, new StringFormatter(10));
		Column materialCode = new Column("material_code", types.realType(), 4,
				new RealFormatter(4,0));
		Column materialIO = new Column("material_io", types.stringType(10), 10,
				new StringFormatter(10));
		Column spacer3 = new Column("spacer3", types.stringType(5), 5,
				new StringFormatter(5));
		Column calcMethodCode = new Column("calc_method_code", types
				.stringType(2), 2, new StringFormatter(2));
		Column efReliabilityInd = new Column("ef_reliability_ind", types
				.stringType(5), 5, new StringFormatter(5));
		Column ruleEffect = new Column("rule_effect", types.realType(), 5,
				new RealFormatter(5,0));
		Column ruleEffectMethod = new Column("rule_effect_method", types
				.stringType(2), 2, new StringFormatter(2));
		Column rulePenetration = new Column("rule_penetration", types
				.realType(), 5, new RealFormatter(5,0));
		Column submittalFlag = new Column("submittal_flag",
				types.stringType(4), 4, new StringFormatter(4));
		Column tribalCode = new Column("tribal_code", types.stringType(4), 4,
				new StringFormatter(4));

		return new Column[] { recordType, state_county_fips, scc, pollutanCode,
				spacer1, startDate, endDate, spacer2, startTime, endTime,
				emissionValue, emissionUnitsCode, emissionType, reliabilityInd,
				factorValue, factorUnitsNum, factorUnitsDenom, materialCode,
				materialIO, spacer3, calcMethodCode, efReliabilityInd,
				ruleEffect, ruleEffectMethod, rulePenetration, submittalFlag,
				tribalCode };

	}

}
