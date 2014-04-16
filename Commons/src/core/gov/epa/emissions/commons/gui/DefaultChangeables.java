package gov.epa.emissions.commons.gui;

import java.util.ArrayList;
import java.util.List;

public class DefaultChangeables implements Changeables {
    private List list;

    private ChangeObserver observer;

    public DefaultChangeables(ChangeObserver observer) {
        this.observer = observer;
        list = new ArrayList();
    }

    public void add(Changeable changeable) {
        changeable.observe(this);
        list.add(changeable);
    }

    public void add(List changeables) {
        for (int i = 0; i < changeables.size(); i++)
            add((Changeable) changeables.get(i));
    }

    public boolean hasChanges() {
        for (int i = 0; i < list.size(); i++)
            if (query((Changeable) list.get(i)))
                return true;

        return false;
    }

    public void resetChanges() {
        for (int i = 0; i < list.size(); i++)
            ((Changeable) list.get(i)).clear();
    }

    private boolean query(Changeable c) {
        return c.hasChanges();
    }

    public void onChanges() {
        if (hasChanges())
            observer.signalChanges();
        else
            observer.signalSaved();
    }

}
