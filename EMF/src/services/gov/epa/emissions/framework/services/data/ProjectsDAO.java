package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;

import java.util.List;

import javax.persistence.EntityManager;

public class ProjectsDAO {

    private HibernateFacade hibernateFacade;

    public ProjectsDAO() {
        hibernateFacade = new HibernateFacade();
    }

    public Project getProject(String name, EntityManager entityManager) {
        if (name == null || name.trim().isEmpty())
            return null;
        
        String query = " FROM " + Project.class.getSimpleName() + " as obj WHERE lower(obj.name)='" + name.toLowerCase()+ "'";
        List<?> projs = entityManager.createQuery(query).getResultList();
        
        if (projs == null || projs.size() == 0)
            return null;
        
        return (Project) projs.get(0);
    }
    
    public Project addProject(Project project, EntityManager entityManager) {
        hibernateFacade.add(project, entityManager);
        return loadProject(project.getName(), entityManager);
    }
    
    private Project loadProject(String name, EntityManager entityManager) {
        return hibernateFacade.load(Project.class, "name", name, entityManager);
    }

}