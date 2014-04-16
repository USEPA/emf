package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.basic.EmfProperty;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.services.qa.QADAO;
import gov.epa.emissions.framework.services.qa.QAProperties;
import gov.epa.emissions.framework.services.qa.RunQAStepTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class QAStepTask {

    private static Log LOG = LogFactory.getLog(QAStepTask.class);

    private HibernateSessionFactory sessionFactory;

    private EmfDataset dataset;

    private User user;

    private int version;

    private DbServerFactory dbServerFactory;

    public QAStepTask(EmfDataset dataset, int version, User user, HibernateSessionFactory sessionFactory,
            DbServerFactory dbServerFactory) {
        this.sessionFactory = sessionFactory;
        this.dataset = dataset;
        this.user = user;
        this.version = version;
        this.dbServerFactory = dbServerFactory;
    }

    private QAStepTemplate[] getSummaryTemplates(String[] summaryQAStepNames) {
        List summaryTemplates = new ArrayList();
        QAStepTemplate[] templates = dataset.getDatasetType().getQaStepTemplates();

        for (int i = 0; i < templates.length; i++) {
            String name = templates[i].getName().trim();
            for (int j = 0; j < summaryQAStepNames.length; j++)
                if (name.equalsIgnoreCase(summaryQAStepNames[j])) {
                    summaryTemplates.add(templates[i]);
                }
        }

        return (QAStepTemplate[]) summaryTemplates.toArray(new QAStepTemplate[0]);
    }

    public void runSummaryQASteps(String[] qaStepNames) throws EmfException {
        QAStepTemplate[] summaryTemplates = loadQASummaryTemplates(qaStepNames);
        QAStep[] summarySteps = addQASteps(summaryTemplates, dataset, version);
        runQASteps(summarySteps);
    }

    public void runSummaryQAStepsAndExport(String[] qaStepNames, String exportDirectory, String[] exportFiles) throws EmfException {
        QAStepTemplate[] summaryTemplates = loadQASummaryTemplates(qaStepNames);
        QAStep[] summarySteps = addQASteps(summaryTemplates, dataset, version);
        runQAStepsAndExport(summarySteps, exportDirectory, exportFiles); // qa step export
    }

    private QAStepTemplate[] loadQASummaryTemplates(String[] qaStepNames) throws EmfException {
        QAStepTemplate[] summaryTemplates = getSummaryTemplates(qaStepNames);
        if (summaryTemplates.length < qaStepNames.length)
            throw new EmfException("Summary QAStepTemplate doesn't exist in dataset type: "
                    + dataset.getDatasetTypeName());
        return summaryTemplates;
    }

    private void runQASteps(QAStep[] summarySteps) throws EmfException {
        try {
            RunQAStepTask runner = new RunQAStepTask(removeUpToDateSteps(summarySteps), user, 
                    dbServerFactory, sessionFactory);
            runner.run();
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException("Can't run summary QASteps: " + e.getMessage());
        }
    }

    private void runQAStepsAndExport(QAStep[] summarySteps, String exportDirectory, String[] exportFiles) throws EmfException {
        try {
            RunQAStepTask runner = new RunQAStepTask(removeUpToDateSteps(summarySteps), user, 
                    dbServerFactory, sessionFactory, 
                    exportDirectory, false, exportFiles);
            runner.run();
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException("Can't run summary QASteps: " + e.getMessage());
        }
    }

    private QAStep[] removeUpToDateSteps(QAStep[] summarySteps) throws EmfException {
        List notCurrentSteps = new ArrayList();
        for (int i = 0; i < summarySteps.length; i++) {
            QAStepResult qaResult = getQAStepResult(summarySteps[i]);
            if (qaResult == null || (qaResult != null && !qaResult.isCurrentTable()))
                notCurrentSteps.add(summarySteps[i]);
        }
        return (QAStep[]) notCurrentSteps.toArray(new QAStep[0]);
    }

    private QAStep[] addQASteps(QAStepTemplate[] templates, EmfDataset dataset, int version) throws EmfException {
        QAStep[] steps = new QAStep[templates.length];
        for (int i = 0; i < templates.length; i++) {
            QAStep step = new QAStep(templates[i], version);
            step.setDatasetId(dataset.getId());
            step.setStatus(QAProperties.getStatus("generated"));
            step.setWho(user.getUsername());
            step.setDate(new Date());
            steps[i] = step;
        }
        updateSteps(getNonExistingSteps(steps));

        return steps;
    }

    private void updateSteps(QAStep[] steps) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            QADAO dao = new QADAO();
            dao.update(steps, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not update QA Steps");
        }
    }

    private QAStep[] getNonExistingSteps(QAStep[] steps) throws EmfException {
        List stepsList = new ArrayList();

        Session session = sessionFactory.getSession();
        try {
            QADAO dao = new QADAO();
            for (int i = 0; i < steps.length; i++) {
                if (!dao.exists(steps[i], session))
                    stepsList.add(steps[i]);
            }
        } catch (RuntimeException e) {
            throw new EmfException("Could not check whether QA Steps exist");
        }

        return (QAStep[]) stepsList.toArray(new QAStep[0]);
    }

    private QAStepResult getQAStepResult(QAStep step) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            QADAO dao = new QADAO();
            QAStepResult qaStepResult = dao.qaStepResult(step, session);
            if (qaStepResult != null)
                qaStepResult.setCurrentTable(isCurrentTable(qaStepResult, session));
            return qaStepResult;
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve QA Step Result", e);
            throw new EmfException("Could not retrieve QA Step Result");
        } finally {
            session.close();
        }
    }

    private boolean isCurrentTable(QAStepResult qaStepResult, Session session) {
        Version version = new Versions().get(qaStepResult.getDatasetId(), qaStepResult.getVersion(), session);
        Date versionDate = version.getLastModifiedDate();
        Date date = qaStepResult.getTableCreationDate();
        if (date == null || versionDate == null)
            return false;
        int value = date.compareTo(versionDate);
        if (value >= 0)
            return true;

        return false;
    }

    public String[] getDefaultSummaryQANames() {
        Session session = sessionFactory.getSession();
        List summaryQAList = new ArrayList();

        try {
            EmfProperty property = new EmfPropertiesDAO().getProperty("defaultQASummaries", session);
            String qaString = property.getValue().trim();
            StringTokenizer st = new StringTokenizer(qaString, "|");

            while (st.hasMoreTokens())
                summaryQAList.add(st.nextToken().trim());

            return (String[]) summaryQAList.toArray(new String[0]);
        } finally {
            session.close();
        }
    }

}
