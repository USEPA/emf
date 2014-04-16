package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.UserFeature;
import gov.epa.emissions.framework.services.EmfException;

public interface UpdateUserPresenter {

    void doSave() throws EmfException;

    void doClose() throws EmfException;
    
    DatasetType[] getDatasetTypes(int userId) throws EmfException;
    
    UserFeature[] getUserFeatures() throws EmfException;

    void onChange();

    void display(UpdatableUserView update, UserView view) throws EmfException;

}