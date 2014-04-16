package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.data.ProjectionShapeFile;
import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

public class QADAO {

    private HibernateFacade hibernateFacade;

    public QADAO() {
        hibernateFacade = new HibernateFacade();
    }

    public QAStep[] steps(EmfDataset dataset, Session session) {
        Criterion criterion = Restrictions.eq("datasetId", new Integer(dataset.getId()));

        List steps = session.createCriteria(QAStep.class).add(criterion).list();
        return (QAStep[]) steps.toArray(new QAStep[0]);
    }
    
    public QAStepResult[] qaRsults(EmfDataset dataset, Session session) {
        Criterion criterion = Restrictions.eq("datasetId", new Integer(dataset.getId()));

        List results = session.createCriteria(QAStepResult.class).add(criterion).list();
        return (QAStepResult[]) results.toArray(new QAStepResult[0]);
    }

    public void update(QAStep[] steps, Session session) {
        hibernateFacade.update(steps, session);
    }

    public void updateQAStepsIds(QAStep[] steps, Session session) {
        for (int i = 0; i < steps.length; i++) {
            Criterion[] criterions = qaStepKeyConstraints(steps[i]);
            List list = hibernateFacade.get(QAStep.class, criterions, session);
            if (!list.isEmpty())
                steps[i].setId(((QAStep) list.get(0)).getId());
        }
    }

    public void add(QAStep[] steps, Session session) {
        hibernateFacade.add(steps, session);
    }

    public QAProgram[] getQAPrograms(Session session) {
        List list = hibernateFacade.getAll(QAProgram.class, session);
        Collections.sort(list);
        return (QAProgram[]) list.toArray(new QAProgram[0]);
    }

    public ProjectionShapeFile[] getProjectionShapeFiles(Session session) {
        List<ProjectionShapeFile> list = hibernateFacade.getAll(ProjectionShapeFile.class, session);
        return list.toArray(new ProjectionShapeFile[0]);
    }

    public QAStepResult qaStepResult(QAStep step, Session session) {
        updateQAStepsIds(new QAStep[]{step},session);
        Criterion c1 = Restrictions.eq("datasetId", new Integer(step.getDatasetId()));
        Criterion c2 = Restrictions.eq("version", new Integer(step.getVersion()));
        Criterion c3 = Restrictions.eq("qaStepId", new Integer(step.getId()));
        Criterion[] criterions =  { c1, c2, c3 };
        List list = hibernateFacade.get(QAStepResult.class, criterions, session);
        
        if (!list.isEmpty())
            return (QAStepResult) list.get(0);
        
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
        Criterion[] criterions = qaStepKeyConstraints(step);
        return hibernateFacade.exists(QAStep.class, criterions, session);
    }

    private Criterion[] qaStepKeyConstraints(QAStep step) {
        Criterion c1 = Restrictions.eq("datasetId", new Integer(step.getDatasetId()));
        Criterion c2 = Restrictions.eq("version", new Integer(step.getVersion()));
        Criterion c3 = Restrictions.eq("name", step.getName());
        Criterion[] criterions = { c1, c2, c3 };
        return criterions;
    }

    public QAProgram addQAProgram(QAProgram program, Session session) {
        hibernateFacade.add(program, session);
        
        return (QAProgram)load(QAProgram.class, program.getName(), session);
    }
    
    private Object load(Class clazz, String name, Session session) {
        Criterion criterion = Restrictions.eq("name", name);
        return hibernateFacade.load(clazz, criterion, session);
    }

    public void deleteQASteps(QAStep[] steps, Session session) { // BUG3615
        hibernateFacade.remove(steps, session);
    }
    
    public void deleteQAStep(QAStep step, Session session) { // BUG3615
        hibernateFacade.remove(step, session);
    }
}
