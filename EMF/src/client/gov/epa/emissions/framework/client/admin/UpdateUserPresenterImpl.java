package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.UserFeature;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.UserService;

public class UpdateUserPresenterImpl implements UpdateUserPresenter {

    private UserService service;

    private UpdatableUserView updateView;

    private boolean userDataChanged;

    private User user;

    private EmfSession session;

    public UpdateUserPresenterImpl(EmfSession session, User user, UserService service) {
        this.session = session;
        this.user = user;
        this.service = service;
    }

    public void display(UpdatableUserView update, UserView view) throws EmfException {
        user = service.obtainLocked(session.user(), user);
        if (!user.isLocked(session.user())) {// view mode, locked by another user
            new ViewUserPresenterImpl(user).display(view);
            return;
        }

        this.updateView = update;
        updateView.observe(this);

        updateView.display(user);
    }

    public void doSave() throws EmfException {
        service.checkDuplicatesByEmail(user);
        service.updateUser(user);
        this.userDataChanged = false;// reset
    }
    
    public DatasetType[] getDatasetTypes(int userID) throws EmfException {
        return session.dataCommonsService().getLightDatasetTypes(userID);
    }

    public void doClose() throws EmfException {
        if (userDataChanged) {
            updateView.closeOnConfirmLosingChanges();
            return;
        }

        service.releaseLocked(session.user(), user);
        updateView.disposeView();
    }

    public void onChange() {
        this.userDataChanged = true;
    }

    public UserFeature[] getUserFeatures() throws EmfException {
        return session.dataCommonsService().getUserFeatures();
    }

}
