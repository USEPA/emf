package gov.epa.emissions.commons.gui;

import java.util.List;

import javax.swing.table.TableModel;

public interface RefreshableTableModel extends TableModel {
    void refresh();

    List elements(int[] selected);
}
