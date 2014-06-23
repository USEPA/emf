package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.data.GeoRegion;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CasesTableData extends AbstractTableData {
    private List<Row> rows;

    public CasesTableData(Case[] cases) {
        this.rows = createRows(cases);
    }

    public String[] columns() {
        return new String[] { "Name", "Last Modified Date", "Last Modified By", 
                "Abbrev. ", "Run Status", "Base Year","Future Year",
                "Start Date", "End Date", "Regions", 
                "Model to Run", "Downstream", "Speciation", 
                "Category", "Project", "Is Final"};
    }

    public Class<?> getColumnClass(int col) {
        if (col == 15)
            return Boolean.class;
        
        return String.class;
    }

    public List<Row> rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }
    
    public void add(Case caseObj) {
        rows.add(row(caseObj));
    }
    
    private Row row(Case caseObj) {
        return new ViewableRow(caseObj, rowValues(caseObj));
    }


    private List<Row> createRows(Case[] cases) {
        List<Row> rows = new ArrayList<Row>();

        for (int i = 0; i < cases.length; i++) 
            rows.add(row(cases[i]));

        return rows;
    }
    
    public void refresh() {
        this.rows = createRows(sources());
    }
    
    public Case[] sources() {
        List<Case> sources = sourcesList();
        return sources.toArray(new Case[0]);
    }

    private List<Case> sourcesList() {
        List<Case> sources = new ArrayList<Case>();
        
        for (Iterator<Row> iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            sources.add((Case)row.source());
        }

        return sources;
    }

    private Object[] rowValues(Case element) {
        Object[] values = { element.getName(), format(element.getLastModifiedDate()), modifiedBy(element),
                abbreviation(element), element.getRunStatus(),emissionsYear(element), futureYear(element),
                format(element.getStartDate()), format(element.getEndDate()),region(element),
                modelToRun(element),airQualityModel(element),speciation(element),   
                caseCategory(element), project(element), isFinal(element),  };
        return values;
    }

    private String modelToRun(Case element) {
        return element.getModel() != null ? element.getModel().getName()+" "+element.getModelVersion() : "";
    }
    
    private Boolean isFinal(Case element) {
        return new Boolean(element.getIsFinal());
    }

    private String futureYear(Case element) {
        return element.getFutureYear()+"" != null ? element.getFutureYear()+"" : "";
    }

    private String abbreviation(Case element) {
        return element.getAbbreviation() != null ? element.getAbbreviation().getName() : "";
    }

    private String airQualityModel(Case element) {
        return element.getAirQualityModel() != null ? element.getAirQualityModel().getName() : "";
    }

    private String speciation(Case element) {
        return element.getSpeciation() != null ? element.getSpeciation().getName() : "";

    }
    private String emissionsYear(Case element) {
        return element.getEmissionsYear() != null ? element.getEmissionsYear().getName() : "";
    }

    private String region(Case element) {
        GeoRegion[] regions = element.getRegions();
        String reg = "";
        if (regions != null && regions.length > 0)
            reg = regions[0].getAbbreviation();
        for (int i= 1; i < regions.length; i++){ 
            if (regions[i] != null)
            reg = reg + ", "+ regions[i].getAbbreviation();
        }
        return reg;
    }

    private String caseCategory(Case element) {
        return element.getCaseCategory() != null ? element.getCaseCategory().getName() : "";
    }

    private String project(Case element) {
        return element.getProject() != null ? element.getProject().getName() : "";
    }

    private String modifiedBy(Case element) {
        return element.getLastModifiedBy() != null ? element.getLastModifiedBy().getName() : "";
    }

}
