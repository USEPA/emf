package gov.epa.emissions.commons.db.postgres;

import gov.epa.emissions.commons.db.SqlDataTypes;

public class PostgresSqlDataTypes implements SqlDataTypes {

    // TODO: get rid of this
    public String type(String name, String genericType, int width) {
        if (genericType.equals("C"))
            return "VARCHAR(" + width + ")";
        if (genericType.equals("I"))
            return "INT";
        // if the type is "N" that means number, then check if there is either
        // "date" or "time" contained in the name.. if so.. return appropriate
        // type
        if (genericType.equals("N")) {
            // if the name contains date
            if (name.indexOf("date") > -1) {
                return "DATE";
            }
            if (name.indexOf("time") > -1) {
                return "INT";
            }
            return "float(15)";// TODO: what's the appropriate size for double
            // ?
        }
        return null;
    }

    public String stringType(int size) {
        return "VARCHAR(" + size + ")";
    }

    public String intType() {
        return "INTEGER";
    }

    public String longType() {
        return "BIGINT";
    }

    public String realType() {
        return "double precision";
    }

    public String smallInt() {
        return "INT2";
    }

    public String charType() {
        return stringType(1);
    }

    public String text() {
        return "TEXT";
    }

    public String autoIncrement() {
        return "SERIAL";
    }

    public String booleanType() {
        return "BOOL";
    }

    public String timestamp() {
        return "TIMESTAMP";
    }
    
    public String stringType() {
        return "VARCHAR";
    }    

}
