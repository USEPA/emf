package gov.epa.emissions.framework.client.transport;

import java.io.File;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

import org.apache.axis.Constants;
import org.apache.axis.client.Call;

public class Mappings {

    protected Mapper mapper;

    public Mappings() {
        mapper = new Mapper();
    }

    public void array(Call call, Class clazz, String name) {
        array(call, clazz, qname(name));
    }

    public void array(Call call, Class clazz, QName name) {
        mapper.registerArrayMapping(call, clazz, name);
    }

    public void bean(Call call, Class clazz, String name) {
        bean(call, clazz, qname(name));
    }

    public void bean(Call call, Class clazz, QName name) {
        mapper.registerBeanMapping(call, clazz, name);
    }

    public QName qname(String name) {
        return mapper.qname(name);
    }

    public void addParam(Call call, String id, QName name) {
        call.addParameter(id, name, ParameterMode.IN);
    }

    public void addParam(Call call, String id, String name) {
        call.addParameter(id, qname(name), ParameterMode.IN);
    }

    public void addStringParam(Call call, String id) {
        call.addParameter(id, Constants.XSD_STRING, ParameterMode.IN);
    }

    public void addBooleanParameter(Call call, String id) {
        call.addParameter(id, Constants.XSD_BOOLEAN, ParameterMode.IN);
    }

    public void addIntegerParam(Call call, String id) {
        call.addParameter(id, Constants.XSD_INTEGER, ParameterMode.IN);
    }

//    public void addIntegerArrayParam(Call call, String id) {
//        // NOTE Auto-generated method stub
//        
//    }

    public void setOperation(Call call, String id) {
        call.setOperationName(qname(id));
    }

    public void setAnyReturnType(Call call) {
        call.setReturnType(Constants.XSD_ANY);
    }

    public void setBooleanReturnType(Call call) {
        call.setReturnType(Constants.XSD_BOOLEAN);
    }

    public void setVoidReturnType(Call call) {
        call.setReturnType(Constants.XSD_ANY);// FIXME: what's it exactly?
    }

    public void setStringReturnType(Call call) {
        call.setReturnType(Constants.XSD_STRING);
    }
    
    public QName string() {
        return Constants.XSD_STRING;
    }
    
    public QName strings() {
        return qname("strings");
    }

    // Added 07/16/2007 for exporting using datasetids -- Conrad
    public QName integer(){
        return Constants.XSD_INTEGER;
    }

    // Added 07/16/2007 for exporting using datasetids -- Conrad
    public QName integers(){
     return qname("integers");    
    }
    
    public QName files() {
        return qname("Files");
    }

    public void setReturnType(Call call, QName name) {
        call.setReturnType(name);
    }

    public void setStringArrayReturnType(Call call) {
        setReturnType(call, strings());
    }
    
    public void setFileSystemViewReturnType(Call call) {
        call.setReturnType(qname("EmfFileSystemView"));
    }

    public void addLongParam(Call call, String id) {
        call.addParameter(id, Constants.XSD_LONG, ParameterMode.IN);
    }
    
    public void addIntParam(Call call) {
        Class<?> cls = int.class;
        call.addParameter("int",qname("int"),cls,ParameterMode.IN);
    }

    public void addIntArrayParam(Call call){
        Class<?> cls = int[].class;
        call.addParameter("intArray",qname("intArray"),cls,ParameterMode.IN);
    }
    
    public void setIntArrayReturnType(Call call) {
        call.setReturnType(qname("intArray"));
    }
    
    public void addByteParam(Call call) {
        Class<?> cls = byte.class;
        call.addParameter("byte",qname("byte"),cls,ParameterMode.IN);
    }

    public void addByteArrayParam(Call call){
        Class<?> cls = byte[].class;
        call.addParameter("byteArray",qname("byteArray"),cls,ParameterMode.IN);
    }
    
    public void setByteReturnType(Call call) {
        call.setReturnType(Constants.XSD_BYTE);
    }
    
    public void setByteArrayReturnType(Call call) {
        call.setReturnType(qname("byteArray"));
    }

    public void addFileArrayParam(Call call){
        Class<?> cls = File[].class;
        call.addParameter("Files", files(), cls, ParameterMode.IN);
    }

    public void setFileArrayReturnType(Call call) {
        call.setReturnType(files());
    }
    
    public void setIntegerReturnType(Call call) {
        call.setReturnType(Constants.XSD_INT);
    }

    public void setLongReturnType(Call call) {
        call.setReturnType(Constants.XSD_LONG);
    }
}
