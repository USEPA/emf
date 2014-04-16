package gov.epa.emissions.framework.client.data.sector;

import gov.epa.emissions.commons.data.Sector;

public class ViewableSectorPresenterImpl implements ViewableSectorPresenter {

    private ViewableSectorView view;

    private Sector sector;

    public ViewableSectorPresenterImpl(ViewableSectorView view, Sector sector) {
        this.view = view;
        this.sector = sector;
    }

    public void doDisplay() {
        view.observe(this);
        view.display(sector);
    }

    public void doClose() {
        view.disposeView();
    }

}
