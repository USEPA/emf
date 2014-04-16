package gov.epa.emissions.framework.client.data.sector;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.data.sector.ViewableSectorPresenter;
import gov.epa.emissions.framework.client.data.sector.ViewableSectorPresenterImpl;
import gov.epa.emissions.framework.client.data.sector.ViewableSectorView;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class DisplaySectorPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() throws Exception {
        Sector sector = new Sector();
        Mock view = mock(ViewableSectorView.class);

        ViewableSectorPresenter presenter = new ViewableSectorPresenterImpl((ViewableSectorView) view.proxy(), sector);
        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").with(eq(sector));

        presenter.doDisplay();
    }

    public void testShouldCloseViewOnClose() throws Exception {
        Sector sector = new Sector();
        Mock view = mock(ViewableSectorView.class);
        view.expects(once()).method("disposeView");

        ViewableSectorPresenter presenter = new ViewableSectorPresenterImpl((ViewableSectorView) view.proxy(), sector);

        presenter.doClose();
    }

}
