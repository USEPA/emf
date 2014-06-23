package gov.epa.emissions.framework.client.transport;

import java.io.File;

import javax.xml.namespace.QName;

import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.services.cost.controlStrategy.DoubleValue;

import org.apache.axis.client.Call;

public class EmfMappings extends Mappings {

    private DataMappings dataMappings;

    private CaseMappings caseMappings;

    public EmfMappings() {
        dataMappings = new DataMappings();
        caseMappings = new CaseMappings();
    }

    public void register(Call call) {
        caseMappings.register(call);
        dataMappings.register(call);

        bean(call, DoubleValue.class, doubleValue());
        array(call, DoubleValue[].class, doubleValueArray());

        bean(call, EmfFileSystemView.class, "EmfFileSystemView");
        
        array(call, String[].class, strings());
        array(call, File[].class, files());
    }

    public QName doubleValue() {
        return qname("DoubleValue");
    }
    
    public QName doubleValueArray() {
        return qname("DoubleValueArray");
    }

}
