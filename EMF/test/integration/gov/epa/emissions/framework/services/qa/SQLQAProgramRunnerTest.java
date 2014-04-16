package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;

public class SQLQAProgramRunnerTest extends ServiceTestCase {

    protected void doSetUp() throws Exception {
        // NOTE Auto-generated method stub

    }

    protected void doTearDown() throws Exception {
        // NOTE Auto-generated method stub

    }

    public void testShouldRunSQLQAStep() throws Exception {
        EmfDataset dataset = null;
        String tableName = null;
        try {
            dataset = newDataset();
            QAStep step = new QAStep();
            step.setName("QA1");
            step.setProgramArguments("SELECT * FROM reference.pollutants");
            step.setDatasetId(dataset.getId());
            step.setVersion(0);
            add(step);
            tableName = tableName(step);
            SQLQAProgramRunner runner = new SQLQAProgramRunner(dbServer(), sessionFactory(),step);
            runner.run();
            assertEquals(8, countRecords(tableName));
        } finally {
            if (dataset != null)
                dropTable(tableName, dbServer().getEmissionsDatasource());
            dropAll(QAStepResult.class);
            dropAll(QAStep.class);
            dropAll(EmfDataset.class);
        }

    }

    private String tableName(QAStep qaStep) {
        return "QA" + qaStep.getName() + "_DSID" + qaStep.getDatasetId() + "_V" + qaStep.getVersion();
    }

    private EmfDataset newDataset() {
        User owner = new UserDAO().get("emf", session);

        EmfDataset dataset = new EmfDataset();
        dataset.setName("dataset-dao-test");
        dataset.setCreator(owner.getUsername());
        add(dataset);
        return (EmfDataset) load(EmfDataset.class, dataset.getName());
    }

}
