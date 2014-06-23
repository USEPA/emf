package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.cost.ControlMeasureProperty;
import gov.epa.emissions.framework.ui.AbstractEditableTableData;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CMPropertiesTableData extends AbstractEditableTableData {

    private List<ViewableRow> rows;
    
    public CMPropertiesTableData(ControlMeasureProperty[] properties) {
        rows = createRows(properties);
    }

    private List<ViewableRow> createRows(ControlMeasureProperty[] properties) {
        rows = new ArrayList<ViewableRow>();
        for (ControlMeasureProperty property : properties) {
            rows.add(row(property));
            //add Cost Year
        }
        return rows;
    }

    private ViewableRow row(ControlMeasureProperty property) {
        Object[] values = {property.getName(), 
                property.getCategory() != null ? property.getCategory().getName() : "No Category", 
                property.getUnits(),
                property.getDataType(),
                (property.getValue() != null ? (property.getValue().length() > 50 ? property.getValue().substring(0, 50) + "..." : property.getValue()) : ""),
                property.getDbFieldName()};
        return new ViewableRow(property, values);
    }

    public String[] columns() {
        return new String[] { "Name", "Category", 
                "Units", "Data Type", 
                "Value", "DB Field Name"};
    }

    public Class getColumnClass(int col) {
        return String.class;
    }

    public List rows() {
        return this.rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

    public void refresh() {
        this.rows = createRows(sources());
    }

    public ControlMeasureProperty[] sources() {
        List<ControlMeasureProperty> sources = sourcesList();
        return sources.toArray(new ControlMeasureProperty[0]);
    }

    private List<ControlMeasureProperty> sourcesList() {
        List sources = new ArrayList();
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            sources.add(row.source());
        }

        return sources;
    }

    public void add(ControlMeasureProperty property) {
        rows.add(row(property));
    }

    private void remove(ControlMeasureProperty property) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            ControlMeasureProperty source = (ControlMeasureProperty) row.source();
            if (source.equals(property)) {
                rows.remove(row);
                return;
            }
        }
    }
    
    public void remove(ControlMeasureProperty[] properties) {
        for (int i = 0; i < properties.length; i++)
            remove(properties[i]);
    }
}
