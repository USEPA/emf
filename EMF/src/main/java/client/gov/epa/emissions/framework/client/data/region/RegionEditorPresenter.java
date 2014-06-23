package gov.epa.emissions.framework.client.data.region;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.editor.EditableCaseSummaryTabPresenterImpl;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.GeoRegion;
import gov.epa.emissions.framework.services.data.RegionType;

public class RegionEditorPresenter {
    private EmfSession session;

    private Object parentPresenter;

    public RegionEditorPresenter(Object parentPresenter, EmfSession session) {
        this.session = session;
        this.parentPresenter = parentPresenter;
    }

    public void display(RegionEditorView view) throws EmfException {
        view.display();
    }

    public DataCommonsService getService() {
        return session.dataCommonsService();
    }

    public GeoRegion saveRegion(GeoRegion region, boolean newRegion) throws EmfException {
        if (newRegion) 
            return getService().addGeoRegion(region);

        return getService().updateGeoRegion(region, session.user());
    }

    public RegionType[] getAllRegionTypes() throws EmfException {
        return getService().getRegionTypes();
    }

    public GeoRegion obtainLock(GeoRegion region) throws EmfException {
        return getService().obtainLockedRegion(session.user(), region);
    }

    public void refreshParentRegionCash() {
        ((EditableCaseSummaryTabPresenterImpl) parentPresenter).refreshObjectManager();
    }

    public GeoRegion[] getAllRegions() throws EmfException {
        refreshParentRegionCash();
        return ((EditableCaseSummaryTabPresenterImpl) parentPresenter).getAllGeoRegions();
    }

}
