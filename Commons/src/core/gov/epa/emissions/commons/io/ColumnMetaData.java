package gov.epa.emissions.commons.io;

public class ColumnMetaData {

    private String name;
    
    private String type;
    
    private int size;
    
    public ColumnMetaData() {
        super();
    }

    public ColumnMetaData(String columnLabel, String columnClassName, int columnDisplaySize) {
       this.name=columnLabel;
       this.type=columnClassName;
       this.size=columnDisplaySize;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
