package gov.epa.emissions.commons.db;

public interface SqlDataTypes {

    // FIXME: use DataType object intstead of String to denote a type
    String type(String name, String genericType, int width);

    String stringType(int size);

    String intType();

    String longType();

    String realType();

    String smallInt();

    String charType();

    String text();

    String autoIncrement();

    String booleanType();

    String timestamp();

    /*
     * get the string type w/o the size, used in INSERT statements 
     */
    String stringType(); 

}
