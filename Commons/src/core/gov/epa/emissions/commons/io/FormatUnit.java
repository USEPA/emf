package gov.epa.emissions.commons.io;

import gov.epa.emissions.commons.data.InternalSource;

public interface FormatUnit {

    FileFormat fileFormat();

    TableFormat tableFormat();

    boolean isRequired();

    void setInternalSource(InternalSource internalSource);

    InternalSource getInternalSource();
}