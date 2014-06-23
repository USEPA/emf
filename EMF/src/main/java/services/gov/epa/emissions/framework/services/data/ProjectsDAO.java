package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

public class ProjectsDAO {

    private HibernateFacade hibernateFacade;

    public ProjectsDAO() {
        hibernateFacade = new HibernateFacade();
    }

    public Project getProject(String name, Session session) {
        if (name == null || name.trim().isEmpty())
            return null;
        
        String query = " FROM " + Project.class.getSimpleName() + " as obj WHERE lower(obj.name)='" + name.toLowerCase()+ "'";
        List<?> projs = session.createQuery(query).list();
        
        if (projs == null || projs.size() == 0)
            return null;
        
        return (Project) projs.get(0);
    }
    
    public Project addProject(Project project, Session session) {
        hibernateFacade.add(project, session);
        return loadProject(project.getName(), session);
    }
    
    private Project loadProject(String name, Session session) {
        Criterion criterion = Restrictions.eq("name", name);
        return (Project)hibernateFacade.load(Project.class, criterion, session);
    }

}