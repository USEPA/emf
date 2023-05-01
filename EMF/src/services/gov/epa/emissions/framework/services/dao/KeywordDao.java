package gov.epa.emissions.framework.services.dao;

import java.util.List;

import gov.epa.emissions.commons.data.Keyword;

public interface KeywordDao {

    Keyword add(Keyword keyword);

    List<Keyword> getKeywords();

    public Keyword getKeyword(String name);
}