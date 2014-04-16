package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.ExternalSource;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.DefaultEmfSession;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.info.InfoTabPresenter;
import gov.epa.emissions.framework.client.meta.info.InfoTabView;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.data.EmfDataset;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class InfoTabPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayInternalSourcesAndVersionsOnDisplay() throws Exception {
        EmfDataset dataset = new EmfDataset();
        dataset.setId(1);
        dataset.addInternalSource(new InternalSource());

        DatasetType type = new DatasetType();
        dataset.setDatasetType(type);
        
        Mock commonsServices = mock(DataCommonsService.class);
        Mock locator = mock(ServiceLocator.class);
        locator.stubs().method("dataCommonsService").will(returnValue(commonsServices.proxy()));

        System.setProperty("USER_PREFERENCES", "test/data/preference/emfpreference.txt");
        
        User user = new User();
        user.setUsername("user");
        EmfSession session = new DefaultEmfSession(user, (ServiceLocator) locator.proxy());

        Mock view = mock(InfoTabView.class);
        view.expects(once()).method("observe").will(returnValue(null));
        view.expects(once()).method("displayInternalSources").with(eq(dataset.getInternalSources()));

        InfoTabPresenter presenter = new InfoTabPresenter((InfoTabView) view.proxy(), dataset, session);

        presenter.doDisplay("");
    }

    public void testShouldDisplayExternalSourcesAndVersionsIfDatasetTypeIsExternalOnDisplay() throws Exception {
        EmfDataset dataset = new EmfDataset();
        dataset.setId(1);

        DatasetType type = new DatasetType();
        type.setExternal(true);
        dataset.setDatasetType(type);
        
        Mock commonsServices = mock(DataCommonsService.class);
        Mock locator = mock(ServiceLocator.class);
        locator.stubs().method("dataCommonsService").will(returnValue(commonsServices.proxy()));

        Mock dataservice = mock(DataService.class);
        locator.stubs().method("dataService").will(returnValue(dataservice.proxy()));
        
        System.setProperty("USER_PREFERENCES", "test/data/preference/emfpreference.txt");
        
        User user = new User();
        user.setUsername("user");
        EmfSession session = new DefaultEmfSession(user, (ServiceLocator) locator.proxy());

        Mock view = mock(InfoTabView.class);
        view.expects(once()).method("observe").will(returnValue(null));
        view.expects(once()).method("displayExternalSources").with(eq(new ExternalSource()));

        InfoTabPresenter presenter = new InfoTabPresenter((InfoTabView) view.proxy(), dataset, session);

        presenter.doDisplay("");
    }

}
