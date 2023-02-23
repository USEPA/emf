package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.commons.db.version.VersionedRecord;
import gov.epa.emissions.framework.ui.RowSource;

import java.util.ArrayList;
import java.util.List;

public class EditablePageRowSource implements RowSource {

    private VersionedRecord source;

    private Boolean selected;

    public EditablePageRowSource(VersionedRecord source) {
        this.source = source;
        this.selected = Boolean.FALSE;
    }

    public Object[] values() {
        List list = new ArrayList();
        list.add(selected);

        list.addAll(source.tokens());
        list.add(Integer.valueOf(source.getRecordId()));
        list.add(Integer.valueOf(source.getVersion()));
        list.add(source.getDeleteVersions());
        

        return list.toArray();
    }

    public void setValueAt(int column, Object val) {
        if (column == 0) {
            selected = (Boolean) val;
            return;
        }
        
        source.replace(column - 1, val);
    }

    public Object source() {
        return source;
    }

    public boolean isSelected() {
        return selected.booleanValue();
    }

    public void validate(int rowNumber) {
        //FIXME: validate each row before save
        
    }
}