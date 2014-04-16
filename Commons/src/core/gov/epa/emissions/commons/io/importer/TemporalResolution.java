package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.Enum;

import java.util.List;

public final class TemporalResolution extends Enum {

    public static final TemporalResolution NOT_AVAILABLE = new TemporalResolution("N/A");

    public static final TemporalResolution ANNUAL = new TemporalResolution("Annual");

    public static final TemporalResolution MONTHLY = new TemporalResolution("Monthly");

    public static final TemporalResolution WEEKLY = new TemporalResolution("Weekly");

    public static final TemporalResolution DAILY = new TemporalResolution("Daily");

    public static final TemporalResolution HOURLY = new TemporalResolution("Hourly");

    public static final List NAMES = getAllNames(TemporalResolution.class);

    private TemporalResolution(final String name) {
        super(name);
    }

}
