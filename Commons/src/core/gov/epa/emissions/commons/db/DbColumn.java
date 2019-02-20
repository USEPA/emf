package gov.epa.emissions.commons.db;

public interface DbColumn {

    String name();

    String sqlType();

    String constraints();

    boolean hasConstraints();

}