package gov.epa.emissions.framework.client.data.sector;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class SectorsManagerPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() throws Exception {
        Sector[] sectors = { new Sector(), new Sector() };

        Mock service = mock(DataCommonsService.class);
        service.stubs().method("getSectors").withNoArguments().will(returnValue(sectors));
        DataCommonsService servicesProxy = (DataCommonsService) service.proxy();

        Mock view = mock(SectorsManagerView.class);
        view.expects(once()).method("display").with(same(sectors));

        SectorsManagerPresenter p = new SectorsManagerPresenter(null, (SectorsManagerView) view.proxy(), servicesProxy);
        view.expects(once()).method("observe").with(eq(p));

        p.doDisplay();
    }

    public void testShouldRefreshViewOnClickOfRefreshButton() throws EmfException {
        Sector[] sectors = {};

        Mock service = mock(DataCommonsService.class);
        service.stubs().method("getSectors").withNoArguments().will(returnValue(sectors));
        DataCommonsService servicesProxy = (DataCommonsService) service.proxy();

        Mock view = mock(SectorsManagerView.class);
        view.expects(once()).method("display").with(same(sectors));

        SectorsManagerPresenter p = new SectorsManagerPresenter(null, (SectorsManagerView) view.proxy(), servicesProxy);
        view.expects(once()).method("observe").with(eq(p));

        p.doDisplay();

        view.expects(once()).method("refresh").with(eq(sectors));
        p.doRefresh();
    }

    public void testShouldCloseViewOnClose() throws Exception {
        Mock view = mock(SectorsManagerView.class);
        view.expects(once()).method("disposeView").withNoArguments();

        SectorsManagerPresenter p = new SectorsManagerPresenter(null, (SectorsManagerView) view.proxy(), null);

        p.doClose();
    }

    public void testShouldDisplayEditSectorViewOnEdit() throws Exception {
        SectorsManagerPresenter p = new SectorsManagerPresenter(null, null, null);

        Mock presenter = mock(EditableSectorPresenter.class);
        presenter.expects(once()).method("doDisplay").withNoArguments();
        EditableSectorPresenter presenterProxy = (EditableSectorPresenter) presenter.proxy();

        Sector sector = new Sector();
        sector.setName("name");

        p.edit(presenterProxy);
    }

    public void testShouldShowDisplaySectorViewOnView() throws Exception {
        Mock view = mock(SectorsManagerView.class);
        Sector sector = new Sector();
        sector.setName("name");

        SectorsManagerPresenter p = new SectorsManagerPresenter(null, (SectorsManagerView) view.proxy(), null);

        Mock presenter = mock(ViewableSectorPresenter.class);
        presenter.expects(once()).method("doDisplay").withNoArguments();
        ViewableSectorPresenter presenterProxy = (ViewableSectorPresenter) presenter.proxy();

        p.view(presenterProxy);
    }

}
