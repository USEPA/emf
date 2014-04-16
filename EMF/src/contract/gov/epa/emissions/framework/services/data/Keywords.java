package gov.epa.emissions.framework.services.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gov.epa.emissions.commons.data.Keyword;

public class Keywords {

    private List keywords;

    public Keywords(Keyword[] keywords) {
        this.keywords = new ArrayList(Arrays.asList(keywords));
    }

    //FIXME: why we are creating new keyword
    public Keyword get(String name) {
        name = name.trim();
        for (int i = 0; i < keywords.size(); i++) {
            Keyword keyword = ((Keyword)keywords.get(i));
            if (keyword.getName().equalsIgnoreCase(name))
                return keyword;
        }
        return new Keyword(name);
    }
    
    public boolean remove(Keyword keyword){
        return keywords.remove(keyword);
    }

    public Keyword[] all() {
        return (Keyword[]) keywords.toArray(new Keyword[0]);
    }

    public boolean contains(String name) {
        for (int i = 0; i < keywords.size(); i++) {
            if (((Keyword) keywords.get(i)).getName().equalsIgnoreCase(name))
                return true;
        }

        return false;
    }

}
