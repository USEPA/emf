package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureService;
import gov.epa.emissions.framework.ui.RefreshObserver;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import com.itextpdf.text.DocumentException;

public class EditorControlMeasurePresenterImpl implements ControlMeasurePresenter {

    protected ControlMeasure measure;

    protected ControlMeasureView view;

    private List presenters;

    protected EmfSession session;

    // private RefreshObserver parent;

    private ControlMeasureSccTabView sccTabView;

    private ControlMeasureTabView summaryTabView;

    private static File lastReportDirectoryUsed;

    public EditorControlMeasurePresenterImpl(ControlMeasure measure, ControlMeasureView view, EmfSession session,
            RefreshObserver parent) {
        this.measure = measure;
        this.view = view;
        this.session = session;
        // this.parent = parent;
        presenters = new ArrayList();
    }

    public void doDisplay() throws EmfException {
        view.observe(this);

        // make sure the editor is EITHER the admin or creator of the measure...
        // need to load a full object for this check, the initialized measure is light in scope
        measure = session.controlMeasureService().getMeasure(measure.getId());
        if (!measure.getCreator().equals(session.user()) && !session.user().isAdmin()) {
            view.notifyEditFailure(measure);
            return;
        }

        measure = session.controlMeasureService().obtainLockedMeasure(session.user(), measure.getId());
        if (!measure.isLocked(session.user())) {// view mode, locked by another user
            view.notifyLockFailure(measure);
            return;
        }
        display();
    }

    void display() {
        view.display(measure);
    }

    public void doClose() throws EmfException {
        session.controlMeasureService().releaseLockedControlMeasure(session.user(), measure.getId());
        try {
            view.disposeView();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void doReport() throws EmfException {
        createReport(measure);
    }

    private void createReport(ControlMeasure controlMeasure) throws EmfException {

        File emptyFile = new File("");

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                return f.getName().endsWith(".pdf") || f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "PDF";
            }
        });

        File reportFile = null;
        int overwrite;
        int retVal = JFileChooser.CANCEL_OPTION;

        do {

            fileChooser.setSelectedFile(emptyFile);

            overwrite = JOptionPane.YES_OPTION;

            if (lastReportDirectoryUsed != null) {
                fileChooser.setCurrentDirectory(lastReportDirectoryUsed);
            }

            retVal = fileChooser.showDialog(null, "Generate");

            File currentDirectory = fileChooser.getCurrentDirectory();
            if (currentDirectory != null) {
                lastReportDirectoryUsed = currentDirectory;
            }

            if (retVal == JFileChooser.APPROVE_OPTION) {

                reportFile = fileChooser.getSelectedFile();
                if (reportFile.exists()) {
                    overwrite = JOptionPane.showConfirmDialog(null, "File " + reportFile.getName()
                            + " already exist. Overwrite?", "Overwrite?", JOptionPane.YES_NO_OPTION);
                }
            }

        } while (overwrite == JOptionPane.NO_OPTION);

        if (retVal == JFileChooser.APPROVE_OPTION) {

            reportFile = fileChooser.getSelectedFile();

            ControlMeasurePDFReportGenerator generator = new ControlMeasurePDFReportGenerator();
            generator.setSession(session);
            generator.setControlMeasure(controlMeasure);
            generator.setOutputFile(reportFile);
            try {
                generator.generate();
            } catch (MalformedURLException e) {

                e.printStackTrace();
                throw new EmfException(e.getLocalizedMessage());
            } catch (DocumentException e) {

                e.printStackTrace();
                throw new EmfException(e.getLocalizedMessage());
            } catch (IOException e) {

                e.printStackTrace();
                throw new EmfException(e.getLocalizedMessage());
            }
        } else {
//            System.out.println("Save command cancelled by user.");
        }

    }

    public void doSave(boolean shouldDispose) throws EmfException {
        save(measure, session.controlMeasureService(), presenters, view, shouldDispose);
    }

    void save(ControlMeasure measure, ControlMeasureService service, List presenters, ControlMeasureView view,
            boolean shouldDispose) throws EmfException {
        for (Iterator iter = presenters.iterator(); iter.hasNext();) {
            ControlMeasureTabPresenter element = (ControlMeasureTabPresenter) iter.next();
            element.doSave(measure);
        }

        if (shouldDispose) {
            service.updateMeasure(measure, sccTabView.sccs());
        }
        else {
            service.updateMeasureAndHoldLock(measure, sccTabView.sccs());
        }
        
        if (shouldDispose) {
            view.disposeView();
        }
    }

    public void set(ControlMeasureSummaryTab summary) {
        this.summaryTabView = summary;
        ControlMeasureTabPresenterImpl tabPresenter = new ControlMeasureTabPresenterImpl(summary);
        presenters.add(tabPresenter);
    }

    public void set(ControlMeasureEfficiencyTabView effTabView) {
        EditableCMEfficiencyTabPresenterImpl effTabPresenter = new EditableCMEfficiencyTabPresenterImpl(effTabView);
        presenters.add(effTabPresenter);
    }

    public void set(ControlMeasureSccTabView sccTabView) {
        this.sccTabView = sccTabView;
        ControlMeasureTabPresenterImpl sccPresenter = new ControlMeasureTabPresenterImpl(sccTabView);
        presenters.add(sccPresenter);
    }

    public void set(ControlMeasureEquationTab equationTabView) {
        ControlMeasureTabPresenterImpl equationPresenter = new ControlMeasureTabPresenterImpl(equationTabView);
        presenters.add(equationPresenter);
    }

    public void set(ControlMeasurePropertyTab propertyTabView) {
        ControlMeasureTabPresenterImpl propertyPresenter = new ControlMeasureTabPresenterImpl(propertyTabView);
        presenters.add(propertyPresenter);
    }

    public void set(ControlMeasureReferencesTab referencesTabView) {

        ControlMeasureTabPresenterImpl propertyPresenter = new ControlMeasureTabPresenterImpl(referencesTabView);
        presenters.add(propertyPresenter);
    }

    public void doRefresh(ControlMeasure controlMeasure) {
        this.measure = controlMeasure;
    }

    public void doModify() {
        measure.setLastModifiedBy(session.user().getName());
        measure.setLastModifiedTime(new Date());
        summaryTabView.modify();
    }

    public void fireTracking() {
        view.signalChanges();
    }

    public Pollutant[] getPollutants() throws EmfException {
        return session.dataCommonsService().getPollutants();
    }

}
