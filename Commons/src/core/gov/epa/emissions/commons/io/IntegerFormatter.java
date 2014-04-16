package gov.epa.emissions.commons.io;

import java.sql.ResultSet;
import java.sql.SQLException;

import corejava.Format;

public class IntegerFormatter implements ColumnFormatter {

    public final Format FORMAT = new Format("%5d");
    
    private int spaces = 0;
    
    private int width = 0;
    
    public IntegerFormatter(int width, int spaces) {
        this.spaces = spaces;
        this.width = width;
    }

    public IntegerFormatter() {
        this.spaces = 0;
        this.width = 0;
    }
    
    public String getSpaces(int n)
    {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < n; i++)
            buf.append(" ");
        return buf.toString();
    }
    
//    public String format(String name, ResultSet data) throws SQLException {
//        String val;
//        if (data.getString(name) == null || data.getFloat(name) == -9)
//        {    
//            val = "";
//        }    
//        else
//        {
//            val = FORMAT.format(data.getInt(name));
//        }
//        int n = spaces + width - val.length();
//        if (n > 0)
//        {
//            return val + getSpaces(n);
//        }
//        return val;
//    }
    
    public String format (String name, ResultSet data) throws SQLException {
        if (data.getString(name) == null || data.getFloat(name) == -9)
            return getSpaces(this.width + this.spaces);
        
        int prefix = this.width - data.getString(name).length();

        return getSpaces(prefix) + data.getString(name) + getSpaces(this.spaces);
    }

}
