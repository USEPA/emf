package gov.epa.emissions.framework.services.dao.impl;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.framework.services.dao.KeywordDao;
import gov.epa.emissions.framework.services.persistence.AbstractJpaDao;

@Repository(value = "keywordDao")
public class KeywordDaoImpl extends AbstractJpaDao<Keyword> implements KeywordDao {

    @Transactional("transactionManager")
    public Keyword add(Keyword keyword) {
        Keyword exist = getKeyword(keyword.getName());

        if (exist != null)
            return exist;

        return create(keyword);
    }

    public List getKeywords() {
        return findAll().stream().sorted(Comparator.comparing(Keyword::getName)).collect(Collectors.toList());
    }

    public Keyword getKeyword(String name) {
        return findByName(name);
    }

}
