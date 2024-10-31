package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.data.ProjectionShapeFile;
import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import javax.persistence.EntityManager;

public class QADAO {

    private HibernateFacade hibernateFacade;

    public QADAO() {
        hibernateFacade = new HibernateFacade();
    }

    public QAStep[] steps(EmfDataset dataset, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<QAStep> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(QAStep.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<QAStep> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade
                .get(criteriaBuilderQueryRoot, new Predicate[] { builder.equal(root.get("datasetId"), Integer.valueOf(dataset.getId())) }, entityManager)
                .toArray(new QAStep[0]);
    }
    
    public QAStepResult[] qaRsults(EmfDataset dataset, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<QAStepResult> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(QAStepResult.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<QAStepResult> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade
                .get(criteriaBuilderQueryRoot, new Predicate[] { builder.equal(root.get("datasetId"), Integer.valueOf(dataset.getId())) }, entityManager)
                .toArray(new QAStepResult[0]);
    }

    public void update(QAStep[] steps, EntityManager entityManager) {
        hibernateFacade.update(steps, entityManager);
    }

    public void updateQAStepsIds(QAStep[] steps, EntityManager entityManager) {
        for (int i = 0; i < steps.length; i++) {
            CriteriaBuilderQueryRoot<QAStep> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(QAStep.class, entityManager);

            Predicate[] criterions = qaStepKeyConstraints(steps[i], criteriaBuilderQueryRoot);
            List list = hibernateFacade.get(criteriaBuilderQueryRoot, criterions, entityManager);
            if (!list.isEmpty())
                steps[i].setId(((QAStep) list.get(0)).getId());
        }
    }

    public void add(QAStep[] steps, EntityManager entityManager) {
        hibernateFacade.add(steps, entityManager);
    }

    public QAProgram[] getQAPrograms(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<QAProgram> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(QAProgram.class, entityManager);
        List<QAProgram> list = hibernateFacade.getAll(criteriaBuilderQueryRoot, entityManager);
        Collections.sort(list);
        return list.toArray(new QAProgram[0]);
    }

    public ProjectionShapeFile[] getProjectionShapeFiles(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<ProjectionShapeFile> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ProjectionShapeFile.class, entityManager);
        List<ProjectionShapeFile> list = hibernateFacade.getAll(criteriaBuilderQueryRoot, entityManager);
        return list.toArray(new ProjectionShapeFile[0]);
    }

    public QAStepResult qaStepResult(QAStep step, EntityManager entityManager) {
        updateQAStepsIds(new QAStep[]{step},entityManager);

        CriteriaBuilderQueryRoot<QAStepResult> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(QAStepResult.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<QAStepResult> root = criteriaBuilderQueryRoot.getRoot();

        Predicate c1 = builder.equal(root.get("datasetId"), Integer.valueOf(step.getDatasetId()));
        Predicate c2 = builder.equal(root.get("version"), Integer.valueOf(step.getVersion()));
        Predicate c3 = builder.equal(root.get("qaStepId"), Integer.valueOf(step.getId()));
        Predicate[] criterions =  { c1, c2, c3 };
        List list = hibernateFacade.get(criteriaBuilderQueryRoot, criterions, entityManager);
        
        if (!list.isEmpty())
            return (QAStepResult) list.get(0);
        
        return null;
    }

    public QAStepResult getQAStepResult(Integer qaStepResultId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<QAStepResult> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(QAStepResult.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<QAStepResult> root = criteriaBuilderQueryRoot.getRoot();

        Predicate c1 = builder.equal(root.get("id"), qaStepResultId);
        Predicate[] criterions =  { c1 };
        List list = hibernateFacade.get(criteriaBuilderQueryRoot, criterions, entityManager);

        if (!list.isEmpty())
            return (QAStepResult) list.get(0);

        return null;
    }

    public QAStep getQAStep(Integer qaStepId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<QAStep> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(QAStep.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<QAStep> root = criteriaBuilderQueryRoot.getRoot();

        Predicate c1 = builder.equal(root.get("id"), qaStepId);
        Predicate[] criterions =  { c1 };
        List list = hibernateFacade.get(criteriaBuilderQueryRoot, criterions, entityManager);

        if (!list.isEmpty())
            return (QAStep) list.get(0);

        return null;
    }

    public void removeQAStepResult(QAStepResult stepResult, EntityManager entityManager) {
        hibernateFacade.remove(stepResult, entityManager);
    }
    
    public boolean getSameAsTemplate(QAStep step, DbServer dbServer) {
        
        String query = "select 1 as found from emf.dataset_types_qa_step_templates "
            + " where name = '" + step.getName().replace("'", "''") + "' "
            + " and program_arguments = '" + step.getProgramArguments().replace("'", "''") + "' "
            + " and dataset_type_id = (select dataset_type from emf.datasets where id = " + step.getDatasetId() + ")";
        
        ResultSet rs;
        boolean found = false;
        try {
            rs = dbServer.getEmissionsDatasource().query().executeQuery(query);
            if (rs.next()) found = true;
        } catch (SQLException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
//        select 1 as found
//        from emf.dataset_types_qa_step_templates 
//        where name = 'Summarize by US State and Pollutant'
//            and program_arguments = 'select fips.state_name, fips.state_abbr, fips.fipsst, e.POLL, sum(ann_emis) as ann_emis from $TABLE[1] e inner join reference.fips on fips.state_county_fips = e.FIPS where fips.country_num = ''0'' group by fips.state_name, fips.state_abbr, fips.fipsst, POLL order by fips.state_name, POLL'
//            and dataset_type_id = (select dataset_type from emf.datasets where id = 16)
//        List count = entityManager.createQuery( query )
//        .setString("qaStepName", step.getName())
//        .setString("qaStepArgument", step.getProgramArguments())
//        .setInteger("datasetId", step.getDatasetId())
//        .list();
       
        return found;//count != null && count.size() > 0;
    }

    public void removeQAStep(QAStep step, EntityManager entityManager) {
        hibernateFacade.remove(step, entityManager);
    }

    public void updateQAStepResult(QAStepResult result, EntityManager entityManager) {
        hibernateFacade.saveOrUpdate(result, entityManager);
    }

    public boolean exists(QAStep step, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<QAStep> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(QAStep.class, entityManager);

        Predicate[] criterions = qaStepKeyConstraints(step, criteriaBuilderQueryRoot);
        return hibernateFacade.exists(criteriaBuilderQueryRoot, criterions, entityManager);
    }

    private Predicate[] qaStepKeyConstraints(QAStep step, CriteriaBuilderQueryRoot<QAStep> criteriaBuilderQueryRoot) {
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<QAStep> root = criteriaBuilderQueryRoot.getRoot();

        Predicate c1 = builder.equal(root.get("datasetId"), Integer.valueOf(step.getDatasetId()));
        Predicate c2 = builder.equal(root.get("version"), Integer.valueOf(step.getVersion()));
        Predicate c3 = builder.equal(root.get("name"), step.getName());
        Predicate[] criterions = { c1, c2, c3 };
        return criterions;
    }

    public QAProgram addQAProgram(QAProgram program, EntityManager entityManager) {
        hibernateFacade.add(program, entityManager);
        
        return (QAProgram)load(QAProgram.class, program.getName(), entityManager);
    }
    
    private Object load(Class clazz, String name, EntityManager entityManager) {
        return hibernateFacade.load(clazz, "name", name, entityManager);
    }

    public void deleteQASteps(QAStep[] steps, EntityManager entityManager) { // BUG3615
        hibernateFacade.remove(steps, entityManager);
    }
    
    public void deleteQAStep(QAStep step, EntityManager entityManager) { // BUG3615
        hibernateFacade.remove(step, entityManager);
    }
}
