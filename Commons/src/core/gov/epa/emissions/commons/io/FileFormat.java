package gov.epa.emissions.commons.io;

public interface FileFormat {

    String identify();

    Column[] cols();
}