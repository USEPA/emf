package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.framework.client.data.TableView;

public interface EditorPanelView extends TableView {

    ChangeSet changeset();

}
