package gov.epa.emissions.commons.io;

import java.sql.ResultSet;
import java.sql.SQLException;

import corejava.Format;

public class RealFormatter implements ColumnFormatter {

    public final Format FORMAT = new Format("%14.7e");

    private int spaces;

    private int width;

    public RealFormatter(int width, int spaces) {
        this.spaces = spaces;
        this.width = width;
    }

    //FIXME: we probably should never alow this construction!!! To have a width equaled to 0 means one will get nothing out of formatter
    public RealFormatter() {
        this.spaces = 0;
        this.width = 0;
    }

    public String format(String name, ResultSet data) throws SQLException {
        if (data.getString(name) == null || data.getFloat(name) == -9)
            return getSpaces(this.width + this.spaces);

        String dataValue = new Double(data.getDouble(name)).toString(); //add for rounding off unnecessary digits
        int dataWidth = dataValue.length();
        
        //NOTE: if width never specified, it should always be the width of the data itself
        if (this.width == 0)
            this.width = dataWidth;

        if (dataWidth < this.width) {
            int prefix = this.width - dataWidth;
            return getSpaces(prefix) + dataValue + getSpaces(this.spaces);
        }
        
        if (dataWidth == this.width)
            return dataValue;
        
        return dataValue.substring(0, this.width);
    }

    public String getSpaces(int n) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < n; i++)
            buf.append(" ");
        return buf.toString();
    }
}
