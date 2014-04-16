package gov.epa.emissions.commons.io.nif.nonpointNonroad;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.FixedWidthFileFormat;
import gov.epa.emissions.commons.io.RealFormatter;
import gov.epa.emissions.commons.io.StringFormatter;

public class EmissionProcessFileFormat implements FileFormat, FixedWidthFileFormat {

	private Column[] cols;
    
    private String identifier;

	public EmissionProcessFileFormat(SqlDataTypes types, String identifier) {
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
		Column stateCountyFips = new Column("state_county_fips", types
				.stringType(5), 5, new StringFormatter(5));
		Column scc = new Column("scc", types.stringType(10), 10,
				new StringFormatter(10));
		Column mactCode = new Column("mact_code", types.stringType(6), 6,
				new StringFormatter(6));
		Column processDesc = new Column("process_desc", types.stringType(78),
				78, new StringFormatter(78));
		Column sicCode = new Column("sic_code", types.stringType(4), 4,
				new StringFormatter(4));
		Column naicsCode = new Column("naics_code", types.stringType(6), 6,
				new StringFormatter(6));
		Column winterThruputPct = new Column("winter_thruput_pct", types
				.realType(), 3, new RealFormatter(3,0));
		Column springThruputPct = new Column("spring_thruput_pct", types
				.realType(), 3, new RealFormatter(3,0));
		Column summerThruputPct = new Column("summer_thruput_pct", types
				.realType(), 3, new RealFormatter(3,0));
		Column fallThruputPct = new Column("fall_thruput_pct",
				types.realType(), 3, new RealFormatter(3,0));
		Column avgDaysPerWeek = new Column("avg_days_per_week", types
				.realType(), 1, new RealFormatter(1,0));
		Column avgWeeksPerYear = new Column("avg_weeks_per_year", types
				.realType(), 2, new RealFormatter(2,0));
		Column avgHoursPerDay = new Column("avg_hours_per_day", types
				.realType(), 2, new RealFormatter(2,0));
		Column avgHoursPerYear = new Column("avg_hours_per_year", types
				.realType(), 4, new RealFormatter(4,0));
		Column heatContent = new Column("heat_content", types.realType(), 8,
				new RealFormatter(8,0));
		Column sulfurContent = new Column("sulfur_content", types.realType(),
				5, new RealFormatter(5,0));
		Column ashContent = new Column("ash_content", types.realType(), 5,
				new RealFormatter(5,0));
		Column mactCompliance = new Column("mact_compliance", types
				.stringType(6), 6, new StringFormatter(6));
		Column submittalFlag = new Column("submittal_flag",
				types.stringType(4), 4, new StringFormatter(4));
		Column tribalCode = new Column("tribal_code", types.stringType(4), 4,
				new StringFormatter(4));
		return new Column[] { recordType, stateCountyFips, scc, mactCode,
				processDesc, sicCode, naicsCode, winterThruputPct,
				springThruputPct, summerThruputPct, fallThruputPct,
				avgDaysPerWeek, avgWeeksPerYear, avgHoursPerDay,
				avgHoursPerYear, heatContent, sulfurContent, ashContent,
				mactCompliance, submittalFlag, tribalCode };

	}

}
