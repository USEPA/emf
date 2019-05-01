package gov.epa.emissions.framework.services.module;

import gov.epa.emissions.framework.services.basic.FilterField;

import java.util.LinkedHashMap;
import java.util.Map;

public class SearchFilterFields {
    protected Map<String, FilterField> filterFields = new LinkedHashMap<String, FilterField>();

    public String[] getFilterFieldNames() {
        return filterFields.keySet().toArray(new String[0]);
    }

    public Map<String, FilterField> getFilterFields() {
        return filterFields;
    }

    public void setFilterFields(Map<String, FilterField> filterFields) {
        this.filterFields = filterFields;
    }

    public void addFilterField(String fieldName, FilterField filterField) {
        this.filterFields.put(fieldName, filterField);
    }

}
