package gov.epa.emissions.commons.db.mysql;

import gov.epa.emissions.commons.db.SqlDataTypes;

public class MySqlDataTypes implements SqlDataTypes {

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
            return "DOUBLE";
        }
        return null;
    }

    public String stringType(int size) {
        return "VARCHAR(" + size + ")";
    }

    public String intType() {
        return "INT";
    }

    public String longType() {
        return "INT";// FIXME: verify
    }

    public String realType() {
        return "DOUBLE";// FIXME: verify precision
    }

    public String smallInt() {
        return "SMALLINT";
    }

    public String charType() {
        return stringType(1);
    }

    public String text() {
        return "TEXT"; //"VARCHAR(255)"; // FIXME: find the mysql type that equivalent to the postgres 'Text' type.
    }

    public String autoIncrement() {
        return "INT AUTO_INCREMENT";
    }

    public String booleanType() {
        return "BOOL";
    }

    public String timestamp() {
        return "";
    }

    public String stringType() {
        return "VARCHAR";
    }

}
