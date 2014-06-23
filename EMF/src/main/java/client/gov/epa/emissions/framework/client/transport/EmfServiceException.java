package gov.epa.emissions.framework.client.transport;

import org.apache.axis.AxisFault;

import gov.epa.emissions.framework.services.EmfException;

public class EmfServiceException extends EmfException {

    public EmfServiceException(AxisFault fault) {
        super(new EmfServiceFault(fault).message());
    }

}
