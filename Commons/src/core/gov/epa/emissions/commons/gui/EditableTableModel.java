package gov.epa.emissions.commons.gui;

import javax.swing.table.TableModel;

public interface EditableTableModel extends TableModel {

    boolean shouldTrackChange(int column);

}
