package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;

import javax.persistence.EntityManager;

public class KeywordsDAO {

    private HibernateFacade hibernateFacade;

    public KeywordsDAO() {
        hibernateFacade = new HibernateFacade();
    }

    public Keyword add(Keyword keyword, EntityManager entityManager) {
        Keyword exist = getKeyword(keyword.getName(), entityManager);

        if (exist != null)
            return exist;

        hibernateFacade.add(keyword, entityManager);

        return getKeyword(keyword.getName(), entityManager);
    }

    public List<Keyword> getKeywords(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<Keyword> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Keyword.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<Keyword> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

    public Keyword getKeyword(String name, EntityManager entityManager) {
        return hibernateFacade.load(Keyword.class, "name", name, entityManager);
    }

}
