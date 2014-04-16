package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.RowSource;

public class EditableInputDatasetRowSource implements RowSource {

    private EmfDataset inputDataset;

    private Boolean selected;

    public EditableInputDatasetRowSource(EmfDataset inputDataset) {
        this.inputDataset = inputDataset;
        this.selected = Boolean.FALSE;
    }

    public Object[] values() {
        return new Object[] { selected, inputDataset.getDatasetType().getName(), inputDataset.getName(), inputDataset.getDefaultVersion() };
    }

    public void setValueAt(int column, Object val) {
        switch (column) {
        case 0:
            selected = (Boolean) val;
            break;
//        case 1:
//            inputDataset.setKeyword(keyword(val));
//            break;
//        case 2:
//            inputDataset.setValue((String) val);
//            break;
        case 3:
            inputDataset.setDefaultVersion((Integer) val);
            break;
        default:
            throw new RuntimeException("invalid column - " + column);
        }
    }

    public Object source() {
        return inputDataset;
    }

    public boolean isSelected() {
        return selected.booleanValue();
    }

    public void validate(int rowNumber) {
//        Keyword keyword = inputDataset.getKeyword();
//        if (keyword == null || keyword.getName().trim().length() == 0) {
//            throw new EmfException("On Keywords panel, empty keyword at row "+rowNumber);
//        }
//        String value = source.getValue();
//        if (value == null || value.trim().length() == 0) {
//            throw new EmfException("On Keywords panel, empty keyword value at row "+rowNumber);
//        }
    }
}