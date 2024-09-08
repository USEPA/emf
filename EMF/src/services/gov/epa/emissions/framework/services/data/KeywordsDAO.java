package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;

import org.hibernate.Session;

public class KeywordsDAO {

    private HibernateFacade hibernateFacade;

    public KeywordsDAO() {
        hibernateFacade = new HibernateFacade();
    }

    public Keyword add(Keyword keyword, Session session) {
        Keyword exist = getKeyword(keyword.getName(), session);

        if (exist != null)
            return exist;

        hibernateFacade.add(keyword, session);

        return getKeyword(keyword.getName(), session);
    }

    public List<Keyword> getKeywords(Session session) {
        CriteriaBuilderQueryRoot<Keyword> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Keyword.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<Keyword> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
    }

    public Keyword getKeyword(String name, Session session) {
        return hibernateFacade.load(Keyword.class, "name", name, session);
    }

}
