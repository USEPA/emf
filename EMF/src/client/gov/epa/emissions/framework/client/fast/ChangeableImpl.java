package gov.epa.emissions.framework.client.fast;

import gov.epa.emissions.commons.gui.Changeable;
import gov.epa.emissions.commons.gui.Changeables;

public class ChangeableImpl implements Changeable {

    private Changeables changeables;

    private boolean changed = false;

    public void clear() {
        this.changed = false;
    }

    public void notifyChanges() {

        changed = true;
        if (changeables != null) {
            changeables.onChanges();
        }
    }

    public boolean hasChanges() {
        return this.changed;
    }

    public void observe(Changeables changeables) {
        this.changeables = changeables;
    }
}
