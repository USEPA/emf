package gov.epa.emissions.framework.ui;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import gov.epa.emissions.commons.gui.Changeable;
import gov.epa.emissions.commons.gui.Changeables;
import gov.epa.emissions.commons.gui.RefreshableTableModel;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;

public class TrackableSortFilterSelectModel extends SortFilterSelectModel implements Changeable {
 
    private Changeables listOfChangeables;

    private boolean changed = false;
    
    private int numOfRows;
    
    public TrackableSortFilterSelectModel(RefreshableTableModel delegate) {
        super(delegate);
        this.numOfRows = getRowCount();
        addTableModelListener(tableModelListener());
    }
    
    private TableModelListener tableModelListener() {
        return new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                if(e.getColumn() != 0 || e.getLastRow() != numOfRows)
                    notifyChanges();
            }

        };
    }
    
    public void clear() {
        this.changed = false;
    }

    void notifyChanges() {
        this.changed = true;
        this.listOfChangeables.onChanges();
    }

    public boolean hasChanges() {
        return this.changed;
    }

    public void observe(Changeables list) {
        this.listOfChangeables = list;
    }
}
