package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

public class EditableDatasetTypePresenterImpl implements EditableDatasetTypePresenter {

    private EditableDatasetTypeView editable;

    private DatasetType type;

    private EmfSession session;

    private ViewableDatasetTypeView viewable;

    public EditableDatasetTypePresenterImpl(EmfSession session, EditableDatasetTypeView editable,
            ViewableDatasetTypeView viewable, DatasetType type) {
        this.session = session;
        this.editable = editable;
        this.viewable = viewable;
        this.type = type;
    }

    public void doDisplay() throws EmfException {
        type = service().obtainLockedDatasetType(session.user(), type);

        if (!type.isLocked(session.user())) {// view mode, locked by another user
            new ViewableDatasetTypePresenterImpl(viewable, type).doDisplay();
            return;
        }

        editable.observe(this);
        Keyword[] keywords = dataCommonsService().getKeywords();
        QAProgram[] programs = session.qaService().getQAPrograms();
        editable.display(type,programs, keywords);
    }

    private DataCommonsService service() {
        return session.dataCommonsService();
    }

    private DataCommonsService dataCommonsService() {
        return session.dataCommonsService();
    }

    public void doClose() throws EmfException {
        service().releaseLockedDatasetType(session.user(), type);
        closeView();
    }

    private void closeView() {
        editable.disposeView();
    }

    public void doSave(String name, String description, KeyVal[] keyVals, String sortOrder, QAStepTemplate[] templates, Column[] columns) throws EmfException {
        update(name, description, keyVals, sortOrder, templates, columns);
        type = service().updateDatasetType(type);
        closeView();
    }

    private void update(String name, String description, KeyVal[] keyVals, String sortOrder
            , QAStepTemplate[] templates, Column[] columns) throws EmfException {
        type.setName(name);
        type.setDescription(description);
        type.setDefaultSortOrder(sortOrder);
        type.setQaStepTemplates(templates);
 
        verifyDuplicates(keyVals);
        type.setKeyVals(keyVals);
        type.setLastModifiedDate(new Date());
        if (type.getFileFormat() != null && columns != null) {
            type.getFileFormat().setColumns(columns);
        }

    }

    private void verifyDuplicates(KeyVal[] keyVals) throws EmfException {
        Set set = new TreeSet();
        for (int i = 0; i < keyVals.length; i++) {
            String name = keyVals[i].getKeyword().getName();
            if (!set.add(name))
                throw new EmfException("duplicate keyword: '" + name + "'");
        }
    }

}
