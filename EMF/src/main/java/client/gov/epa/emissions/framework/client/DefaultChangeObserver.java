package gov.epa.emissions.framework.client;

import gov.epa.emissions.commons.gui.ChangeObserver;

public class DefaultChangeObserver implements ChangeObserver {

    private EmfView view;

    public DefaultChangeObserver(EmfView view) {
        this.view = view;
    }

    public void signalChanges() {
        if (!view.getTitle().endsWith(" *"))
            view.setTitle(view.getTitle() + " *");
    }

    public void signalSaved() {
        String title = view.getTitle();
        int starPosition = title.indexOf(" *");
        if (starPosition >= 0)
            view.setTitle(title.substring(0, starPosition));
    }

}
