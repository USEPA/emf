package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.services.EmfException;

import javax.xml.namespace.QName;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Call;

public class EmfCall {

    private Call call;

    private EmfMappings mappings;

    private String service;

    public EmfCall(Call call, String service) {
        this.call = call;
        this.service = service;
        mappings = new EmfMappings();
        mappings.register(call);
    }

    public void setOperation(String operation) {
        mappings.setOperation(call, operation);
    }

    public void addStringParam(String param) {
        mappings.addStringParam(call, param);
    }

    public void setStringReturnType() {
        mappings.setStringReturnType(call);
    }

    public void request(Object[] params) throws EmfException {
        try {
            call.invoke(params);
        } catch (AxisFault fault) {
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to " + service);
        } finally {
            call.removeAllParameters();
        }
    }

    public void setReturnType(QName name) {
        mappings.setReturnType(call, name);
    }

    public Object requestResponse(Object[] params) throws EmfException {
        try {
            return call.invoke(params);
        } catch (AxisFault fault) {
            System.out.println(fault.toString());
            fault.printStackTrace();
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to " + service);
        } finally {
            call.removeAllParameters();
        }
    }

    public void addParam(String id, QName name) {
        mappings.addParam(call, id, name);
    }

    public void setVoidReturnType() {
        mappings.setVoidReturnType(call);
    }

    public void addBooleanParameter(String id) {
        mappings.addBooleanParameter(call, id);
    }

    public void addLongParam(String id) {
        mappings.addLongParam(call, id);
    }

    public void enableSession() {
        call.setMaintainSession(true);
        // never time out
        call.setTimeout(new Integer(0));
    }

    public void addIntegerParam(String id) {
        mappings.addIntegerParam(call, id);
    }

    //added 07/16/2007 --Conrad
//    public void addIntegerArrayParam(String id) {
//        mappings.addIntegerArrayParam(call, id);
//    }

    //  Is this really needed?? --07/16/2007 Conrad
    public void addIntArrayParam() {
        mappings.addIntArrayParam(call);
    }

    public void setIntegerReturnType() {
        mappings.setIntegerReturnType(call);
    }

    public void setLongReturnType() {
        mappings.setLongReturnType(call);
    }

    public void setIntArrayReturnType() {
        mappings.setIntArrayReturnType(call);
    }
    
    public void addFileArrayParam() {
        mappings.addFileArrayParam(call);
    }
    
    public void setStringArrayReturnType() {
        mappings.setStringArrayReturnType(call);
    }

    public void setFileArrayReturnType() {
        mappings.setFileArrayReturnType(call);
    }

    public void setFileSystemViewReturnType() {
        mappings.setFileSystemViewReturnType(call);
    }

    public void setBooleanReturnType() {
        mappings.setBooleanReturnType(call);
    }

    public void addIntParam() {
        mappings.addIntParam(call);

    }
    
    public void addByteParam() {
        mappings.addByteParam(call);
    }
    
    public void addByteArrayParam() {
        mappings.addByteArrayParam(call);
    }
    
    public void setByteReturnType() {
        mappings.setByteReturnType(call);
    }
    
    public void setByteArrayReturnType() {
        mappings.setByteArrayReturnType(call);
    }

    public void setTimeOut(int milliseconds) {
        call.setTimeout(new Integer(milliseconds));
    }

    public Call getCall() {
        return call;
    }
}
