package gov.epa.emissions.commons.io;

import gov.epa.emissions.commons.util.CustomDateFormat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CommonFileHeaderReader {
    private static final String emf_start_date = "EMF_START_DATE";

    private static final String emf_end_date = "EMF_END_DATE";

    private static final String emf_temporal_resolution = "EMF_TEMPORAL_RESOLUTION";

    private static final String emf_sector = "EMF_SECTOR";

    private static final String emf_region = "EMF_REGION";

    private static final String emf_project = "EMF_PROJECT";

    private static final String emf_country = "EMF_COUNTRY";

    private Map<String, String> map;

    private BufferedReader fileReader;

    public CommonFileHeaderReader(File file) throws Exception {
        map = new HashMap<String, String>();
        fileReader = new BufferedReader(new CustomCharSetInputStreamReader(new FileInputStream(file)));
    }

    public Date getStartDate() {
        try {
            return CustomDateFormat.parse_MM_DD_YYYY_HH_mm(map.get(emf_start_date));
        } catch (ParseException e) {
            try {
                return CustomDateFormat.parse_MMddyyyy(map.get(emf_start_date));
            } catch (ParseException e1) {
                return null;
            }
        }
    }

    public Date getEndDate() {
        try {
            return CustomDateFormat.parse_MM_DD_YYYY_HH_mm(map.get(emf_end_date));
        } catch (ParseException e) {
            try {
                return CustomDateFormat.parse_MMddyyyy(map.get(emf_end_date));
            } catch (ParseException e1) {
                return null;
            }
        }
    }

    public String getProject() {
        return map.get(emf_project);
    }

    public String getRegion() {
        return map.get(emf_region);
    }

    public String getSector() {
        return map.get(emf_sector);
    }

    public String getTemporalResolution() {
        return map.get(emf_temporal_resolution);
    }

    public String getCountry() {
        return map.get(emf_country);
    }

    public void readHeader() throws IOException {
        String line = null;

        while ((line = fileReader.readLine()) != null) {
            line = line.trim();

            if (!line.startsWith("#"))
                break;

            if (line.startsWith("#EMF_"))
                extractEmfInfo(line);
        }
    }

    private void extractEmfInfo(String line) {
        if (line.toUpperCase().startsWith("#" + emf_start_date))
            putValues(emf_start_date, line);

        else if (line.toUpperCase().startsWith("#" + emf_end_date))
            putValues(emf_end_date, line);

        else if (line.toUpperCase().startsWith("#" + emf_temporal_resolution))
            putValues(emf_temporal_resolution, line);

        else if (line.toUpperCase().startsWith("#" + emf_sector))
            putValues(emf_sector, line);

        else if (line.toUpperCase().startsWith("#" + emf_region))
            putValues(emf_region, line);

        else if (line.toUpperCase().startsWith("#" + emf_project))
            putValues(emf_project, line);

        else if (line.toUpperCase().startsWith("#" + emf_country))
            putValues(emf_country, line);
    }

    private void putValues(String key, String line) {
        String value = line.substring(line.indexOf("=") + 1);

        if (value != null && value.contains("\""))
            value = value.replace('"', ' ');

        if (value != null)
            value = value.trim();

        map.put(key, value);
    }

    public void close() throws IOException {
        fileReader.close();
    }

}
