package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.qa.QAService;

import java.util.Date;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.Constraint;
import org.jmock.core.constraint.IsInstanceOf;

public class EditableDatasetTypePresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewAfterObtainingLockForDatasetTypeOnDisplay() throws Exception {
        DatasetType type = new DatasetType();

        User user = new User();
        user.setUsername("name");
        type.setLockOwner(user.getUsername());
        type.setLockDate(new Date());

        QAProgram[] programs = {};
        Mock qaService = mock(QAService.class);
        qaService.expects(once()).method("getQAPrograms").withNoArguments().will(returnValue(programs));
        
        Mock dcService = mock(DataCommonsService.class);
        dcService.expects(once()).method("obtainLockedDatasetType").with(same(user), same(type)).will(returnValue(type));

        Keyword[] keywords = new Keyword[0];
        dcService.stubs().method("getKeywords").withNoArguments().will(returnValue(keywords));

        Mock session = session(user, dcService.proxy());
        session.stubs().method("qaService").withNoArguments().will(returnValue(qaService.proxy()));

        Mock view = mock(EditableDatasetTypeView.class);

        EditableDatasetTypePresenter presenter = new EditableDatasetTypePresenterImpl((EmfSession) session.proxy(),
                (EditableDatasetTypeView) view.proxy(), null, type);
        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").with(new Constraint[]{same(type),same(programs), same(keywords)});

        presenter.doDisplay();
    }

    private Mock session(User user, Object dataCommonsServiceProxy) {
        Mock session = mock(EmfSession.class);
        session.stubs().method("user").withNoArguments().will(returnValue(user));
        session.stubs().method("dataCommonsService").withNoArguments().will(returnValue(dataCommonsServiceProxy));
        
        return session;
    }

    public void testShouldShowNonEditViewAfterFailingToObtainLockForSectorOnDisplay() throws Exception {
        DatasetType type = new DatasetType();// no lock
        User user = new User();
        user.setUsername("name");

        Mock service = mock(DataCommonsService.class);
        service.expects(once()).method("obtainLockedDatasetType").with(same(user), same(type)).will(returnValue(type));

        Mock session = session(user, service.proxy());

        Mock view = mock(ViewableDatasetTypeView.class);
        view.expects(once()).method("observe").with(new IsInstanceOf(ViewableDatasetTypePresenterImpl.class));
        view.expects(once()).method("display").with(eq(type));

        EditableDatasetTypePresenter presenter = new EditableDatasetTypePresenterImpl((EmfSession) session.proxy(),
                null, (ViewableDatasetTypeView) view.proxy(), type);

        presenter.doDisplay();
    }

    public void testShouldCloseViewOnClose() throws Exception {
        DatasetType type = new DatasetType();
        Mock view = mock(EditableDatasetTypeView.class);
        view.expects(once()).method("disposeView");

        User user = new User();
        Mock service = mock(DataCommonsService.class);
        service.expects(once()).method("releaseLockedDatasetType").with(same(user), same(type)).will(returnValue(type));

        Mock session = session(user, service.proxy());

        EditableDatasetTypePresenter presenter = new EditableDatasetTypePresenterImpl((EmfSession) session.proxy(),
                (EditableDatasetTypeView) view.proxy(), null, type);

        presenter.doClose();
    }

    public void testShouldUpdateSectorAndCloseOnSave() throws Exception {
        String name = "name";
        String desc = "desc";
        String sortOrder = "default sortOrder";
        
        KeyVal val1 = new KeyVal();
        val1.setId(1);
        val1.setKeyword(new Keyword("key1"));
        val1.setValue("val1");

        KeyVal val2 = new KeyVal();
        val2.setId(2);
        val2.setKeyword(new Keyword("key2"));
        val2.setValue("val2");
        
        KeyVal[] keyVals = {val1, val2};
        
        QAStepTemplate step1 = new QAStepTemplate();
        QAStepTemplate step2 = new QAStepTemplate();
        QAStepTemplate[] getQAStepTemps = {step1, step2};

        Column column1 = new Column();
        Column column2 = new Column();
        Column[] columns = {column1, column2};

        Mock type = mock(DatasetType.class);
        type.expects(once()).method("setName").with(same(name));
        type.expects(once()).method("setDescription").with(same(desc));
        type.expects(once()).method("setKeyVals").with(same(keyVals));
        type.expects(once()).method("setDefaultSortOrder").with(same(sortOrder));
        DatasetType typeProxy = (DatasetType) type.proxy();

        Mock view = mock(EditableDatasetTypeView.class);
        view.expects(once()).method("disposeView");

        User user = new User();
        user.setUsername("test");
        Mock service = mock(DataCommonsService.class);
        service.expects(once()).method("updateDatasetType").with(same(typeProxy)).will(returnValue(typeProxy));

        Mock session = session(user, service.proxy());

        EditableDatasetTypePresenter presenter = new EditableDatasetTypePresenterImpl((EmfSession) session.proxy(),
                (EditableDatasetTypeView) view.proxy(), null, typeProxy);

        presenter.doSave(name, desc, keyVals, sortOrder, getQAStepTemps, columns);
    }

    public void testShouldFailWithErrorIfDuplicateKeywordsInKeyValsOnSave() {
        String name = "name";
        String desc = "desc";
        String sortOrder = "default sortOrder";
        
        KeyVal val1 = new KeyVal();
        val1.setId(1);
        val1.setKeyword(new Keyword("key1"));
        val1.setValue("val1");

        KeyVal[] keyVals = {val1, val1};
        
        QAStepTemplate step1 = new QAStepTemplate();
        QAStepTemplate step2 = new QAStepTemplate();
        QAStepTemplate[] getQAStepTemps = {step1, step2};

        Column column1 = new Column();
        Column column2 = new Column();
        Column[] columns = {column1, column2};

        Mock type = mock(DatasetType.class);
        type.expects(once()).method("setName").with(same(name));
        type.expects(once()).method("setDescription").with(same(desc));
        type.expects(once()).method("setDefaultSortOrder").with(same(sortOrder));
        
        EditableDatasetTypePresenter presenter = new EditableDatasetTypePresenterImpl(null, null, null,
                ((DatasetType) type.proxy()));

        try {
            presenter.doSave(name, desc, keyVals, sortOrder,getQAStepTemps, columns);
        } catch (EmfException e) {
            assertEquals("duplicate keyword: 'key1'", e.getMessage());
            return;
        }

        fail("should have raised an error on duplicate keyword entries");
    }
}
