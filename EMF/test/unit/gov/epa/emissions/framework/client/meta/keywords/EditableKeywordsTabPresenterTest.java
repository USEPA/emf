package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.Keywords;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class EditableKeywordsTabPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() {
        Mock view = mock(EditableKeywordsTabView.class);

        Mock dataset = mock(EmfDataset.class);
        KeyVal[] values = new KeyVal[] { new KeyVal(), new KeyVal() };
        dataset.stubs().method("getKeyVals").will(returnValue(values));

        Keywords keywords = new Keywords(new Keyword[0]);
        view.expects(once()).method("display").with(eq(dataset.proxy()), same(keywords));

        Mock type = mock(DatasetType.class);
        dataset.stubs().method("getDatasetType").withNoArguments().will(returnValue(type.proxy()));

        EditableKeywordsTabPresenter presenter = new EditableKeywordsTabPresenterImpl((EditableKeywordsTabView) view.proxy(), (EmfDataset) dataset
                .proxy(), null);

        presenter.display(keywords);
    }

    public void testShouldDisplayKeyValuesForAllKeywordsOfAssociatedDatasetType() {
        final Keyword keyword1 = new Keyword("key1");
        final Keyword keyword2 = new Keyword("key2");
        Keyword[] keywordsList = { keyword1, keyword2 };

        Mock dataset = mock(EmfDataset.class);
        KeyVal keyVal = new KeyVal();
        keyVal.setKeyword(keyword1);
        final KeyVal[] values = new KeyVal[] { keyVal };
        dataset.stubs().method("getKeyVals").will(returnValue(values));

        Mock type = mock(DatasetType.class);
        dataset.stubs().method("getDatasetType").withNoArguments().will(returnValue(type.proxy()));

        Mock view = mock(EditableKeywordsTabView.class);
        Keywords keywords = new Keywords(keywordsList);
        view.expects(once()).method("display").with(eq(dataset.proxy()), same(keywords));

        EditableKeywordsTabPresenter presenter = new EditableKeywordsTabPresenterImpl((EditableKeywordsTabView) view.proxy(), (EmfDataset) dataset
                .proxy(), null);

        presenter.display(keywords);
    }

    public void testUpdateDatasetOnSave() throws EmfException {
        KeyVal[] keyvals = {};
        Mock dataset = mock(EmfDataset.class);
        dataset.expects(once()).method("setKeyVals").with(same(keyvals));
        Mock view = mock(EditableKeywordsTabView.class);
        view.expects(once()).method("updates").withNoArguments().will(returnValue(keyvals));

        EditableKeywordsTabPresenter presenter = new EditableKeywordsTabPresenterImpl((EditableKeywordsTabView) view.proxy(), ((EmfDataset) dataset
                .proxy()), null);

        presenter.doSave();
    }

    public void testShouldFailWithErrorIfDuplicateKeywordsInKeyValsOnSave() {
        KeyVal keyval = new KeyVal();
        keyval.setKeyword(new Keyword("name"));
        KeyVal[] keyvals = { keyval, keyval };

        Mock dataset = mock(EmfDataset.class);
        Mock view = mock(EditableKeywordsTabView.class);
        view.expects(once()).method("updates").withNoArguments().will(returnValue(keyvals));

        EditableKeywordsTabPresenter presenter = new EditableKeywordsTabPresenterImpl((EditableKeywordsTabView) view.proxy(), ((EmfDataset) dataset
                .proxy()), null);

        try {
            presenter.doSave();
        } catch (EmfException e) {
            assertEquals("duplicate keyword 'name'", e.getMessage());
            return;
        }

        fail("should have raised an error on duplicate keyword entries");
    }
}
