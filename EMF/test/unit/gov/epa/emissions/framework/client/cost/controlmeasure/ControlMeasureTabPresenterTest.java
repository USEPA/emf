package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class ControlMeasureTabPresenterTest extends MockObjectTestCase {

    public void testUpdateDatasetOnSave() throws EmfException {
        Mock measure = mock(ControlMeasure.class);

        Mock view = mock(ControlMeasureTabView.class);
        Object measureProxy = measure.proxy();
        view.expects(once()).method("save").with(eq(measureProxy));

        ControlMeasureTabPresenter presenter = new EditableCMSummaryTabPresenterImpl((ControlMeasureTabView) view
                .proxy());

        presenter.doSave((ControlMeasure) measure.proxy());
    }
}
