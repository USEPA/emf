package gov.epa.emissions.commons.io;

import gov.epa.emissions.commons.db.DbColumn;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Column implements DbColumn, Serializable, Comparable<Column> {

    private String name;

    private String sqlType;

    private String defaultValue;

    private boolean mandatory;

    private String description;

    private int fixFormatStart;

    private int fixFormatEnd;

    private ColumnFormatter formatter;
    
    private String formatterClass;

    private int width = 0;

    private int spaces = 0;

    private String constraints;
    
    public Column() {
        //To satisfy serialization requirement
    }
    
    public Column(String name, String sqlType, String defaultValue, String mandatory, String desc, String formmater,
            String constraints, String width, String spaces, String fixedStart, String fixedEnd) {
        this.name = name;
        this.description = desc;
        this.sqlType = sqlType;
        this.defaultValue = defaultValue;
        this.formatterClass = formmater;
        this.constraints = constraints;
        this.mandatory = mandatory.equalsIgnoreCase("true");
        this.width = (width == null || width.isEmpty() ? 0 : Integer.parseInt(width));
        this.spaces = (spaces == null || spaces.isEmpty() ? 0 : Integer.parseInt(spaces));
        this.fixFormatStart = (fixedStart == null || fixedStart.isEmpty() ? 0 : Integer.parseInt(fixedStart));
        this.fixFormatEnd = (fixedEnd == null || fixedEnd.isEmpty() ? 0 : Integer.parseInt(fixedEnd));
    }

    public Column(String name, String sqlType, int width, ColumnFormatter formatter, String constraints) {
        this.name = name;
        this.sqlType = sqlType;
        this.width = width;
        this.constraints = constraints;
        this.formatter = formatter;
        this.formatterClass = formatter.getClass().getSimpleName();
    }

    public Column(String name, String sqlType, int width, ColumnFormatter formatter) {
        this(name, sqlType, width, formatter, null);
    }

    public Column(String name, String sqlType, int width) {
        this(name, sqlType, width, new NullFormatter());
    }

    public Column(String name, String sqlType, ColumnFormatter formatter) {
        this(name, sqlType, -1, formatter);
    }

    public Column(String name, String sqlType) {
        this(name, sqlType, new NullFormatter());
    }

    public Column(String name, String sqlType, ColumnFormatter formatter, String constraints) {
        this(name, sqlType, -1, formatter, constraints);
    }

    public String format(ResultSet data) throws SQLException {
        return getFormatter().format(name, data);
    }

    public String name() {
        return name;
    }

    public String sqlType() {
        return sqlType;
    }

    public int width() {
        return width;
    }

    public String constraints() {
        return constraints;
    }

    public boolean hasConstraints() {
        return constraints != null;
    }

    public String toString() {
        return name;
    }

    public int compareTo(Column other) {
        return name.compareToIgnoreCase(other.name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSqlType() {
        return sqlType;
    }

    public void setSqlType(String sqlType) {
        this.sqlType = sqlType;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getFixFormatStart() {
        return fixFormatStart;
    }

    public void setFixFormatStart(int fixFormatStart) {
        this.fixFormatStart = fixFormatStart;
    }

    public int getFixFormatEnd() {
        return fixFormatEnd;
    }

    public void setFixFormatEnd(int fixFormatEnd) {
        this.fixFormatEnd = fixFormatEnd;
    }

    private ColumnFormatter getFormatter() {
        if (formatter != null)
            return formatter;
        
        if (formatterClass == null)
            return new NullFormatter();
        
        if (formatterClass.equals(CharFormatter.class.getSimpleName()))
            return new CharFormatter();
        
        if (formatterClass.equals(IntegerFormatter.class.getSimpleName()))
            return new IntegerFormatter(width, spaces);
        
        if (formatterClass.equals(LongFormatter.class.getSimpleName()))
            return new LongFormatter();
        
        if (formatterClass.equals(RealFormatter.class.getSimpleName()))
            return new RealFormatter(width, spaces);
        
        if (formatterClass.equals(SmallIntegerFormatter.class.getSimpleName()))
            return new SmallIntegerFormatter();
        
        if (formatterClass.equals(StringFormatter.class.getSimpleName()))
            return new StringFormatter(width, spaces);
        
        return new NullFormatter();
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getConstraints() {
        return constraints;
    }

    public void setConstraints(String constraints) {
        this.constraints = constraints;
    }

    public int getSpaces() {
        return spaces;
    }

    public void setSpaces(int space) {
        this.spaces = space;
    }

    public String getFormatterClass() {
        return formatterClass;
    }

    public void setFormatterClass(String formatterClass) {
        this.formatterClass = formatterClass;
    }

}
