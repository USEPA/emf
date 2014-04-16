package gov.epa.emissions.commons.io.other;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.FileFormat;

import java.util.HashMap;
import java.util.Map;

public class CountryStateCountyFileFormatFactory {
    private Map map;

    public CountryStateCountyFileFormatFactory(SqlDataTypes sqlType) {
        map = new HashMap();
        map.put("COUNTRY", new CountryFileFormat(sqlType));
        map.put("STATE", new StateFileFormat(sqlType));
        map.put("COUNTY", new CountyFileFormat(sqlType));
    }

    public FileFormat get(String header) {
        return (FileFormat) map.get(header);
    }

}
