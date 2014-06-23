package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

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

    public List getKeywords(Session session) {
        return hibernateFacade.getAll(Keyword.class, Order.asc("name"), session);
    }

    public Keyword getKeyword(String name, Session session) {
        Criterion crit = Restrictions.eq("name", name);

        return (Keyword) hibernateFacade.load(Keyword.class, crit, session);
    }

}
