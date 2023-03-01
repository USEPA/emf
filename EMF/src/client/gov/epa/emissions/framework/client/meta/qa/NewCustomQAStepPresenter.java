package gov.epa.emissions.framework.client.meta.qa;

import java.util.ArrayList;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;

public class NewCustomQAStepPresenter {

    private NewCustomQAStepView stepView;

    private EmfDataset dataset;

    private Version[] versions;

    private EmfSession session;

    private EditableQATabView parentView;

    public NewCustomQAStepPresenter(NewCustomQAStepView stepView, EmfDataset dataset, Version[] versions,
            EditableQATabView view, EmfSession session) {
        this.stepView = stepView;
        this.dataset = dataset;
        this.versions = versions;
        this.parentView = view;
        this.session = session;
    }

    public void display() throws EmfException {
        // build list of QA programs without Compare Dataset Fields
        ArrayList<QAProgram> programs = new ArrayList<QAProgram>();
        for (QAProgram program : session.qaService().getQAPrograms()) {
            if (program.getName().equals("Compare Dataset Fields")) continue;
            programs.add(program);
        }
        stepView.display(dataset, programs.toArray(new QAProgram[programs.size()]), versions, parentView, session);
    }

    public void doSave() throws EmfException {
        QAStep step = stepView.save();
        step = session.qaService().update(step);
        parentView.addCustomQAStep(step);
    }

}
