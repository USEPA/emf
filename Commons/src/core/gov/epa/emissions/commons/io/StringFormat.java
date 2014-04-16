package gov.epa.emissions.commons.io;

public class StringFormat {

    private int size;
    
    private int spaces;

    public StringFormat(int size) {
        this.size = size;
        this.spaces = 0;
    }

    public StringFormat(int size, int trailingSpaces) {
        this.size = size;
        this.spaces = trailingSpaces;
    }

    public String getSpaces(int n)
    {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < n; i++)
            buf.append(" ");
        return buf.toString();
    }
    
    public String format(String val) {
        
        if (size == 0 || val.length() == 0 || val.length() > size)
            return getSpaces(size+spaces);
        
        return val + getSpaces(size - val.length() + spaces);
    }
    
}
