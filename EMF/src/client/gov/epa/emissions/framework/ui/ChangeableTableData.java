package gov.epa.emissions.framework.ui;

import gov.epa.emissions.commons.gui.Changeable;
import gov.epa.emissions.commons.gui.Changeables;

public abstract class ChangeableTableData extends AbstractTableData implements Changeable {

    private Changeables changeables;

    private boolean changed;

    public void notifyChanges() {
        changed = true;
        changeables.onChanges();
    }

    public void clear() {
        this.changed = false;
    }

    public boolean hasChanges() {
        return this.changed;
    }

    public void observe(Changeables changeables) {
        this.changeables = changeables;
    }

}
