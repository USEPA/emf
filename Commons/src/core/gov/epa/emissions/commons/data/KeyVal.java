package gov.epa.emissions.commons.data;

import java.io.Serializable;

public class KeyVal implements Serializable {

    private long id;

    private Keyword keyword;

    private String value;
    
    private String kwname;

    private long listindex;

    public KeyVal() {// persistence/bean
    }

    public KeyVal(Keyword keyword, String value) {
        this.keyword = keyword;
        this.value = value;
        this.kwname = keyword.getName();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Keyword getKeyword() {
        return keyword;
    }

    public void setKeyword(Keyword keyword) {
        this.keyword = keyword;
        this.kwname = keyword.getName();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getListindex() {
        return listindex;
    }

    public void setListindex(long listindex) {
        this.listindex = listindex;
    }

    public String getName() {
        return keyword.getName();
    }
    
    public String getKwname() {
        kwname = keyword.getName();
        return kwname;
    }

    public void setKwname(String kwname) {
        //this.kwname = kwname;
    }

}
