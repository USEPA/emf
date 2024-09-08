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

import org.hibernate.Session;

public class QADAO {

    private HibernateFacade hibernateFacade;

    public QADAO() {
        hibernateFacade = new HibernateFacade();
    }

    public QAStep[] steps(EmfDataset dataset, Session session) {
        CriteriaBuilderQueryRoot<QAStep> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(QAStep.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<QAStep> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade
                .get(criteriaBuilderQueryRoot, new Predicate[] { builder.equal(root.get("datasetId"), Integer.valueOf(dataset.getId())) }, session)
                .toArray(new QAStep[0]);
    }
    
    public QAStepResult[] qaRsults(EmfDataset dataset, Session session) {
        CriteriaBuilderQueryRoot<QAStepResult> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(QAStepResult.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<QAStepResult> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade
                .get(criteriaBuilderQueryRoot, new Predicate[] { builder.equal(root.get("datasetId"), Integer.valueOf(dataset.getId())) }, session)
                .toArray(new QAStepResult[0]);
    }

    public void update(QAStep[] steps, Session session) {
        hibernateFacade.update(steps, session);
    }

    public void updateQAStepsIds(QAStep[] steps, Session session) {
        for (int i = 0; i < steps.length; i++) {
            CriteriaBuilderQueryRoot<QAStep> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(QAStep.class, session);

            Predicate[] criterions = qaStepKeyConstraints(steps[i], criteriaBuilderQueryRoot);
            List list = hibernateFacade.get(criteriaBuilderQueryRoot, criterions, session);
            if (!list.isEmpty())
                steps[i].setId(((QAStep) list.get(0)).getId());
        }
    }

    public void add(QAStep[] steps, Session session) {
        hibernateFacade.add(steps, session);
    }

    public QAProgram[] getQAPrograms(Session session) {
        CriteriaBuilderQueryRoot<QAProgram> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(QAProgram.class, session);
        List<QAProgram> list = hibernateFacade.getAll(criteriaBuilderQueryRoot, session);
        Collections.sort(list);
        return list.toArray(new QAProgram[0]);
    }

    public ProjectionShapeFile[] getProjectionShapeFiles(Session session) {
        CriteriaBuilderQueryRoot<ProjectionShapeFile> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ProjectionShapeFile.class, session);
        List<ProjectionShapeFile> list = hibernateFacade.getAll(criteriaBuilderQueryRoot, session);
        return list.toArray(new ProjectionShapeFile[0]);
    }

    public QAStepResult qaStepResult(QAStep step, Session session) {
        updateQAStepsIds(new QAStep[]{step},session);

        CriteriaBuilderQueryRoot<QAStepResult> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(QAStepResult.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<QAStepResult> root = criteriaBuilderQueryRoot.getRoot();

        Predicate c1 = builder.equal(root.get("datasetId"), Integer.valueOf(step.getDatasetId()));
        Predicate c2 = builder.equal(root.get("version"), Integer.valueOf(step.getVersion()));
        Predicate c3 = builder.equal(root.get("qaStepId"), Integer.valueOf(step.getId()));
        Predicate[] criterions =  { c1, c2, c3 };
        List list = hibernateFacade.get(criteriaBuilderQueryRoot, criterions, session);
        
        if (!list.isEmpty())
            return (QAStepResult) list.get(0);
        
        return null;
    }

    public QAStepResult getQAStepResult(Integer qaStepResultId, Session session) {
        CriteriaBuilderQueryRoot<QAStepResult> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(QAStepResult.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<QAStepResult> root = criteriaBuilderQueryRoot.getRoot();

        Predicate c1 = builder.equal(root.get("id"), qaStepResultId);
        Predicate[] criterions =  { c1 };
        List list = hibernateFacade.get(criteriaBuilderQueryRoot, criterions, session);

        if (!list.isEmpty())
            return (QAStepResult) list.get(0);

        return null;
    }

    public QAStep getQAStep(Integer qaStepId, Session session) {
        CriteriaBuilderQueryRoot<QAStep> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(QAStep.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<QAStep> root = criteriaBuilderQueryRoot.getRoot();

        Predicate c1 = builder.equal(root.get("id"), qaStepId);
        Predicate[] criterions =  { c1 };
        List list = hibernateFacade.get(criteriaBuilderQueryRoot, criterions, session);

        if (!list.isEmpty())
            return (QAStep) list.get(0);

        return null;
    }

    public void removeQAStepResult(QAStepResult stepResult, Session session) {
        hibernateFacade.remove(stepResult, session);
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
//        List count = session.createQuery( query )
//        .setString("qaStepName", step.getName())
//        .setString("qaStepArgument", step.getProgramArguments())
//        .setInteger("datasetId", step.getDatasetId())
//        .list();
       
        return found;//count != null && count.size() > 0;
    }

    public void removeQAStep(QAStep step, Session session) {
        hibernateFacade.remove(step, session);
    }

    public void updateQAStepResult(QAStepResult result, Session session) {
        hibernateFacade.saveOrUpdate(result, session);
    }

    public boolean exists(QAStep step, Session session) {
        CriteriaBuilderQueryRoot<QAStep> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(QAStep.class, session);

        Predicate[] criterions = qaStepKeyConstraints(step, criteriaBuilderQueryRoot);
        return hibernateFacade.exists(criteriaBuilderQueryRoot, criterions, session);
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

    public QAProgram addQAProgram(QAProgram program, Session session) {
        hibernateFacade.add(program, session);
        
        return (QAProgram)load(QAProgram.class, program.getName(), session);
    }
    
    private Object load(Class clazz, String name, Session session) {
        return hibernateFacade.load(clazz, "name", name, session);
    }

    public void deleteQASteps(QAStep[] steps, Session session) { // BUG3615
        hibernateFacade.remove(steps, session);
    }
    
    public void deleteQAStep(QAStep step, Session session) { // BUG3615
        hibernateFacade.remove(step, session);
    }
}
