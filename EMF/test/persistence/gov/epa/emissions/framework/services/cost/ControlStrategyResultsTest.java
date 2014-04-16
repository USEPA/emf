package gov.epa.emissions.framework.services.cost;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class ControlStrategyResultsTest extends ServiceTestCase {

    private ControlStrategyDAO controlStrategydao;

    private DatasetDAO datasetDAO;

    protected void doSetUp() throws Exception {
        controlStrategydao = new ControlStrategyDAO();
        datasetDAO = new DatasetDAO();

    }

    protected void doTearDown() throws Exception {
        // NOTE Auto-generated method stub

    }

    public void testShouldSaveControlStrategyResultsSummary() throws HibernateException, Exception {
        EmfDataset dataset = dataset();
        EmfDataset detailDataset = dataset();
        ControlStrategy element = controlStrategy(dataset);

        ControlStrategyResult result = new ControlStrategyResult();
        result.setControlStrategyId(element.getId());
        result.setInputDataset(dataset);
        result.setDetailedResultDataset(detailDataset);
        StrategyResultType detailedStrategyResultType = controlStrategydao.getDetailedStrategyResultType(session);
        result.setStrategyResultType(detailedStrategyResultType);

        try {
            int resultId = controlStrategydao.add(result, session);

            ControlStrategyResult savedResult = controlStrategydao.getControlStrategyResult(resultId, session);
            assertEquals(element.getId(), savedResult.getControlStrategyId());
        } finally {
            dropAll(ControlStrategyResult.class);
            dropAll(ControlStrategy.class);
            dropAll(EmfDataset.class);
        }
    }

    private ControlStrategy controlStrategy(EmfDataset dataset) throws HibernateException, Exception {
        Session session = sessionFactory().getSession();
        ControlStrategy element = new ControlStrategy("test" + Math.random());
        element.setControlStrategyInputDatasets(new ControlStrategyInputDataset[] { new ControlStrategyInputDataset(dataset) });
        ControlStrategyDAO dao = new ControlStrategyDAO();
        try {
            dao.add(element, session);
            return (ControlStrategy) dao.all(session).get(0);
        } finally {
            session.close();
        }
    }

    public void testShouldGetDetailedStrategyResultType() {
        StrategyResultType detailedStrategyResultType = controlStrategydao.getDetailedStrategyResultType(session);
        assertEquals(StrategyResultType.detailedStrategyResult, detailedStrategyResultType.getName());
    }

    private EmfDataset dataset() throws EmfException {
        EmfDataset dataset = new EmfDataset();
        dataset.setName("test_dataset" + Math.random());
        dataset.setCreator("emf");
        datasetDAO.add(dataset, session);
        return dataset;
    }

}
