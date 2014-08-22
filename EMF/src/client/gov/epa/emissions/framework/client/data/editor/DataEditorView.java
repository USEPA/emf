package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.data.DatasetNote;
import gov.epa.emissions.framework.services.editor.DataAccessToken;
import gov.epa.emissions.framework.services.editor.Revision;

import java.util.Date;

public interface DataEditorView extends ManagedView {

    void display(Version version, String table, User user);

    void observe(DataEditorPresenter presenter);

    void notifyLockFailure(DataAccessToken token);

    void updateLockPeriod(Date start, Date end);

    void notifySaveFailure(String message);

    boolean confirmDiscardChanges();

    Revision revision();
    
    //void reloadVersions();

    boolean verifyRevisionInput();
    
    void disableSaveDiscard();

    boolean hasReplacedValues();

    void populate(String table);

}
