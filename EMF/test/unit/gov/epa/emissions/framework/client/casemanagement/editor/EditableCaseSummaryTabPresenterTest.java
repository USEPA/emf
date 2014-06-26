package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;

import java.util.Date;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.constraint.IsInstanceOf;

public class EditableCaseSummaryTabPresenterTest extends MockObjectTestCase {

    private Mock caseObj;

    public void testUpdateDatasetOnSave() throws EmfException {
        caseObj = mock(Case.class);
        caseObj.expects(once()).method("setLastModifiedDate").with(new IsInstanceOf(Date.class));

        Mock view = mock(EditableCaseSummaryTabView.class);
        Object caseProxy = caseObj.proxy();
        view.expects(once()).method("save").with(eq(caseProxy));

        EditableCaseSummaryTabPresenter presenter = new EditableCaseSummaryTabPresenterImpl(null, (Case) caseProxy,
                (EditableCaseSummaryTabView) view.proxy());

        presenter.doSave();
    }
}
