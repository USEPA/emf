/**
 * GovEpaEmfServicesEditorDataEditorServiceSoapBindingStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package gov.epa.emissions.framework.client.threadsafe;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.transport.DataMappings;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.editor.DataAccessToken;

public class GovEpaEmfServicesEditorDataEditorServiceSoapBindingStub extends org.apache.axis.client.Stub implements DataEditorServiceImpl {
//    private java.util.Vector cachedSerClasses = new java.util.Vector();
//    private java.util.Vector cachedSerQNames = new java.util.Vector();
//    private java.util.Vector cachedSerFactories = new java.util.Vector();
//    private java.util.Vector cachedDeserFactories = new java.util.Vector();
    static DataMappings _dataMappings;
    static org.apache.axis.description.OperationDesc [] _operations;

    static {
        _dataMappings = new DataMappings();
        _operations = new org.apache.axis.description.OperationDesc[17];
        _initOperationDesc1();
        _initOperationDesc2();
    }

    private static void _initOperationDesc1(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("save");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "token"), org.apache.axis.description.ParameterDesc.IN, _dataMappings.dataAccessToken(), DataAccessToken.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "dataset"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "EmfDataset"), EmfDataset.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "version"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Version"), Version.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(_dataMappings.dataAccessToken());
        oper.setReturnClass(DataAccessToken.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "saveReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://localhost:8080/emf/services/gov.epa.emf.services.editor.DataEditorService", "fault"),
                      "gov.epa.emissions.framework.services.EmfException",
                      new javax.xml.namespace.QName("http://services.framework.emissions.epa.gov", "EmfException"), 
                      true
                     ));
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("hasChanges");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "token"), org.apache.axis.description.ParameterDesc.IN, _dataMappings.dataAccessToken(), DataAccessToken.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        oper.setReturnClass(boolean.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "hasChangesReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://localhost:8080/emf/services/gov.epa.emf.services.editor.DataEditorService", "fault"),
                      "gov.epa.emissions.framework.services.EmfException",
                      new javax.xml.namespace.QName("http://services.framework.emissions.epa.gov", "EmfException"), 
                      true
                     ));
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getVersion");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "datasetId"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "version"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Version"));
        oper.setReturnClass(Version.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getVersionReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://localhost:8080/emf/services/gov.epa.emf.services.editor.DataEditorService", "fault"),
                      "gov.epa.emissions.framework.services.EmfException",
                      new javax.xml.namespace.QName("http://services.framework.emissions.epa.gov", "EmfException"), 
                      true
                     ));
        _operations[2] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("submit");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "token"), org.apache.axis.description.ParameterDesc.IN, _dataMappings.dataAccessToken(), DataAccessToken.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "changeset"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ChangeSet"), ChangeSet.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "pageNumber"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://localhost:8080/emf/services/gov.epa.emf.services.editor.DataEditorService", "fault"),
                      "gov.epa.emissions.framework.services.EmfException",
                      new javax.xml.namespace.QName("http://services.framework.emissions.epa.gov", "EmfException"), 
                      true
                     ));
        _operations[3] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getVersions");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "datasetId"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Versions"));
        oper.setReturnClass(Version[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getVersionsReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://localhost:8080/emf/services/gov.epa.emf.services.editor.DataEditorService", "fault"),
                      "gov.epa.emissions.framework.services.EmfException",
                      new javax.xml.namespace.QName("http://services.framework.emissions.epa.gov", "EmfException"), 
                      true
                     ));
        _operations[4] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("openSession");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "user"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "User"), User.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "token"), org.apache.axis.description.ParameterDesc.IN, _dataMappings.dataAccessToken(), DataAccessToken.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(_dataMappings.dataAccessToken());
        oper.setReturnClass(DataAccessToken.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "openSessionReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://localhost:8080/emf/services/gov.epa.emf.services.editor.DataEditorService", "fault"),
                      "gov.epa.emissions.framework.services.EmfException",
                      new javax.xml.namespace.QName("http://services.framework.emissions.epa.gov", "EmfException"), 
                      true
                     ));
        _operations[5] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("openSession");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "user"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "User"), User.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "token"), org.apache.axis.description.ParameterDesc.IN, _dataMappings.dataAccessToken(), DataAccessToken.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "pageSize"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(_dataMappings.dataAccessToken());
        oper.setReturnClass(DataAccessToken.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "openSessionReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://localhost:8080/emf/services/gov.epa.emf.services.editor.DataEditorService", "fault"),
                      "gov.epa.emissions.framework.services.EmfException",
                      new javax.xml.namespace.QName("http://services.framework.emissions.epa.gov", "EmfException"), 
                      true
                     ));
        _operations[6] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("closeSession");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "user"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "User"), User.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "token"), org.apache.axis.description.ParameterDesc.IN, _dataMappings.dataAccessToken(), DataAccessToken.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://localhost:8080/emf/services/gov.epa.emf.services.editor.DataEditorService", "fault"),
                      "gov.epa.emissions.framework.services.EmfException",
                      new javax.xml.namespace.QName("http://services.framework.emissions.epa.gov", "EmfException"), 
                      true
                     ));
        _operations[7] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("derive");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "base"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Version"), Version.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "user"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "User"), User.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "name"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Version"));
        oper.setReturnClass(Version.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "deriveReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://localhost:8080/emf/services/gov.epa.emf.services.editor.DataEditorService", "fault"),
                      "gov.epa.emissions.framework.services.EmfException",
                      new javax.xml.namespace.QName("http://services.framework.emissions.epa.gov", "EmfException"), 
                      true
                     ));
        _operations[8] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getPageCount");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "token"), org.apache.axis.description.ParameterDesc.IN, _dataMappings.dataAccessToken(), DataAccessToken.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        oper.setReturnClass(int.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getPageCountReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://localhost:8080/emf/services/gov.epa.emf.services.editor.DataEditorService", "fault"),
                      "gov.epa.emissions.framework.services.EmfException",
                      new javax.xml.namespace.QName("http://services.framework.emissions.epa.gov", "EmfException"), 
                      true
                     ));
        _operations[9] = oper;

    }

    private static void _initOperationDesc2(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("discard");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "token"), org.apache.axis.description.ParameterDesc.IN, _dataMappings.dataAccessToken(), DataAccessToken.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://localhost:8080/emf/services/gov.epa.emf.services.editor.DataEditorService", "fault"),
                      "gov.epa.emissions.framework.services.EmfException",
                      new javax.xml.namespace.QName("http://services.framework.emissions.epa.gov", "EmfException"), 
                      true
                     ));
        _operations[10] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getPage");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "token"), org.apache.axis.description.ParameterDesc.IN, _dataMappings.dataAccessToken(), DataAccessToken.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "pageNumber"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Page"));
        oper.setReturnClass(Page.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getPageReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://localhost:8080/emf/services/gov.epa.emf.services.editor.DataEditorService", "fault"),
                      "gov.epa.emissions.framework.services.EmfException",
                      new javax.xml.namespace.QName("http://services.framework.emissions.epa.gov", "EmfException"), 
                      true
                     ));
        _operations[11] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getPageWithRecord");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "token"), org.apache.axis.description.ParameterDesc.IN, _dataMappings.dataAccessToken(), DataAccessToken.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "record"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Page"));
        oper.setReturnClass(Page.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getPageWithRecordReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://localhost:8080/emf/services/gov.epa.emf.services.editor.DataEditorService", "fault"),
                      "gov.epa.emissions.framework.services.EmfException",
                      new javax.xml.namespace.QName("http://services.framework.emissions.epa.gov", "EmfException"), 
                      true
                     ));
        _operations[12] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getTotalRecords");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "token"), org.apache.axis.description.ParameterDesc.IN, _dataMappings.dataAccessToken(), DataAccessToken.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        oper.setReturnClass(int.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getTotalRecordsReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://localhost:8080/emf/services/gov.epa.emf.services.editor.DataEditorService", "fault"),
                      "gov.epa.emissions.framework.services.EmfException",
                      new javax.xml.namespace.QName("http://services.framework.emissions.epa.gov", "EmfException"), 
                      true
                     ));
        _operations[13] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("applyConstraints");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "token"), org.apache.axis.description.ParameterDesc.IN, _dataMappings.dataAccessToken(), DataAccessToken.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "rowFilter"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "sortOrder"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Page"));
        oper.setReturnClass(Page.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "applyConstraintsReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://localhost:8080/emf/services/gov.epa.emf.services.editor.DataEditorService", "fault"),
                      "gov.epa.emissions.framework.services.EmfException",
                      new javax.xml.namespace.QName("http://services.framework.emissions.epa.gov", "EmfException"), 
                      true
                     ));
        _operations[14] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getTableMetadata");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "table"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "TableMetadata"));
        oper.setReturnClass(TableMetadata.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getTableMetadataReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://localhost:8080/emf/services/gov.epa.emf.services.editor.DataEditorService", "fault"),
                      "gov.epa.emissions.framework.services.EmfException",
                      new javax.xml.namespace.QName("http://services.framework.emissions.epa.gov", "EmfException"), 
                      true
                     ));
        _operations[15] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("markFinal");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "token"), org.apache.axis.description.ParameterDesc.IN, _dataMappings.dataAccessToken(), DataAccessToken.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Version"));
        oper.setReturnClass(Version.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "markFinalReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://localhost:8080/emf/services/gov.epa.emf.services.editor.DataEditorService", "fault"),
                      "gov.epa.emissions.framework.services.EmfException",
                      new javax.xml.namespace.QName("http://services.framework.emissions.epa.gov", "EmfException"), 
                      true
                     ));
        _operations[16] = oper;

    }

    public GovEpaEmfServicesEditorDataEditorServiceSoapBindingStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public GovEpaEmfServicesEditorDataEditorServiceSoapBindingStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public GovEpaEmfServicesEditorDataEditorServiceSoapBindingStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
        if (service == null) {
            super.service = new org.apache.axis.client.Service();
        } else {
            super.service = service;
        }
        ((org.apache.axis.client.Service)super.service).setTypeMappingVersion("1.2");
            java.lang.Class cls;
            javax.xml.namespace.QName qName;
            javax.xml.namespace.QName qName2;
            java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
            java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
            java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
            java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
            java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
            java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
            java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
            java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
            java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
            java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
//        addBindings0();
//        addBindings1();
//        addBindings2();
    }

//    private void addBindings0() {
//            java.lang.Class cls;
//            javax.xml.namespace.QName qName;
//            javax.xml.namespace.QName qName2;
//            java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
//            java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
//            java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
//            java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
//            java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
//            java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
//            java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
//            java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
//            java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
//            java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
//            qName = new javax.xml.namespace.QName("http://data.commons.emissions.epa.gov", "LockableImpl");
//            cachedSerQNames.add(qName);
//            cls = gov.epa.emissions.commons.data.LockableImpl.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("http://db.commons.emissions.epa.gov", "DatabaseRecord");
//            cachedSerQNames.add(qName);
//            cls = gov.epa.emissions.commons.db.DatabaseRecord.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("http://localhost:8080/emf/services/gov.epa.emf.services.editor.DataEditorService", "ArrayOf_soapenc_string");
//            cachedSerQNames.add(qName);
//            cls = java.lang.String[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("http://localhost:8080/emf/services/gov.epa.emf.services.editor.DataEditorService", "ArrayOf_xsd_anyType");
//            cachedSerQNames.add(qName);
//            cls = java.lang.Object[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyType");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("http://services.framework.emissions.epa.gov", "EmfException");
//            cachedSerQNames.add(qName);
//            cls = gov.epa.emissions.framework.services.EmfException.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Abbreviation");
//            cachedSerQNames.add(qName);
//            cls = Abbreviation.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Abbreviations");
//            cachedSerQNames.add(qName);
//            cls = Abbreviation[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Abbreviation");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "AccessLog");
//            cachedSerQNames.add(qName);
//            cls = AccessLog.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "AirQualityModel");
//            cachedSerQNames.add(qName);
//            cls = AirQualityModel.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "AirQualityModels");
//            cachedSerQNames.add(qName);
//            cls = AirQualityModel[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "AirQualityModel");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "AllAccessLogs");
//            cachedSerQNames.add(qName);
//            cls = AccessLog[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "AccessLog");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Case");
//            cachedSerQNames.add(qName);
//            cls = Case.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "CaseCategories");
//            cachedSerQNames.add(qName);
//            cls = CaseCategory[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "CaseCategory");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "CaseCategory");
//            cachedSerQNames.add(qName);
//            cls = CaseCategory.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "CaseInput");
//            cachedSerQNames.add(qName);
//            cls = CaseInput.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "CaseInputs");
//            cachedSerQNames.add(qName);
//            cls = CaseInput[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "CaseInput");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "CaseJob");
//            cachedSerQNames.add(qName);
//            cls = CaseJob.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "CaseJobs");
//            cachedSerQNames.add(qName);
//            cls = CaseJob[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "CaseJob");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "CaseOutput");
//            cachedSerQNames.add(qName);
//            cls = CaseOutput.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "CaseOutputs");
//            cachedSerQNames.add(qName);
//            cls = CaseOutput[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "CaseOutput");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "CaseParameter");
//            cachedSerQNames.add(qName);
//            cls = CaseParameter.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "CaseParameters");
//            cachedSerQNames.add(qName);
//            cls = CaseParameter[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "CaseParameter");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "CaseProgram");
//            cachedSerQNames.add(qName);
//            cls = CaseProgram.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "CasePrograms");
//            cachedSerQNames.add(qName);
//            cls = CaseProgram[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "CaseProgram");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Cases");
//            cachedSerQNames.add(qName);
//            cls = Case[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Case");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "CasesSens");
//            cachedSerQNames.add(qName);
//            cls = CasesSens.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "CasesSenses");
//            cachedSerQNames.add(qName);
//            cls = CasesSens[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "CasesSens");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ChangeSet");
//            cachedSerQNames.add(qName);
//            cls = ChangeSet.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Column");
//            cachedSerQNames.add(qName);
//            cls = Column.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ColumnMetaData");
//            cachedSerQNames.add(qName);
//            cls = ColumnMetaData.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ColumnMetaDatas");
//            cachedSerQNames.add(qName);
//            cls = ColumnMetaData[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ColumnMetaData");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Columns");
//            cachedSerQNames.add(qName);
//            cls = Column[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Column");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlMeasure");
//            cachedSerQNames.add(qName);
//            cls = ControlMeasure.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlMeasureClass");
//            cachedSerQNames.add(qName);
//            cls = ControlMeasureClass.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlMeasureClasses");
//            cachedSerQNames.add(qName);
//            cls = ControlMeasureClass[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlMeasureClass");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlMeasureEquation");
//            cachedSerQNames.add(qName);
//            cls = ControlMeasureEquation.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlMeasureEquations");
//            cachedSerQNames.add(qName);
//            cls = ControlMeasureEquation[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlMeasureEquation");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlMeasureMonth");
//            cachedSerQNames.add(qName);
//            cls = ControlMeasureMonth.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlMeasureMonths");
//            cachedSerQNames.add(qName);
//            cls = ControlMeasureMonth[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlMeasureMonth");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlMeasureNEIDevice");
//            cachedSerQNames.add(qName);
//            cls = ControlMeasureNEIDevice.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlMeasureNEIDevices");
//            cachedSerQNames.add(qName);
//            cls = ControlMeasureNEIDevice[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlMeasureNEIDevice");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlMeasureProperties");
//            cachedSerQNames.add(qName);
//            cls = ControlMeasureProperty[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlMeasureProperty");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlMeasureProperty");
//            cachedSerQNames.add(qName);
//            cls = ControlMeasureProperty.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlMeasurePropertyCategories");
//            cachedSerQNames.add(qName);
//            cls = ControlMeasurePropertyCategory[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlMeasurePropertyCategory");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlMeasurePropertyCategory");
//            cachedSerQNames.add(qName);
//            cls = ControlMeasurePropertyCategory.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlMeasures");
//            cachedSerQNames.add(qName);
//            cls = ControlMeasure[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlMeasure");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlProgram");
//            cachedSerQNames.add(qName);
//            cls = ControlProgram.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlPrograms");
//            cachedSerQNames.add(qName);
//            cls = ControlProgram[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlProgram");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlProgramType");
//            cachedSerQNames.add(qName);
//            cls = ControlProgramType.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlProgramTypes");
//            cachedSerQNames.add(qName);
//            cls = ControlProgramType[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlProgramType");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlStrategies");
//            cachedSerQNames.add(qName);
//            cls = ControlStrategy[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlStrategy");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlStrategy");
//            cachedSerQNames.add(qName);
//            cls = ControlStrategy.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlStrategyConstraint");
//            cachedSerQNames.add(qName);
//            cls = ControlStrategyConstraint.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlStrategyInputDataset");
//            cachedSerQNames.add(qName);
//            cls = ControlStrategyInputDataset.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlStrategyInputDatasets");
//            cachedSerQNames.add(qName);
//            cls = ControlStrategyInputDataset[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlStrategyInputDataset");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlStrategyMeasure");
//            cachedSerQNames.add(qName);
//            cls = ControlStrategyMeasure.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlStrategyMeasures");
//            cachedSerQNames.add(qName);
//            cls = ControlStrategyMeasure[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlStrategyMeasure");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlStrategyResult");
//            cachedSerQNames.add(qName);
//            cls = ControlStrategyResult.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlStrategyResults");
//            cachedSerQNames.add(qName);
//            cls = ControlStrategyResult[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlStrategyResult");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlStrategyTargetPollutant");
//            cachedSerQNames.add(qName);
//            cls = ControlStrategyTargetPollutant.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlStrategyTargetPollutants");
//            cachedSerQNames.add(qName);
//            cls = ControlStrategyTargetPollutant[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlStrategyTargetPollutant");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlTechnologies");
//            cachedSerQNames.add(qName);
//            cls = ControlTechnology[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlTechnology");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ControlTechnology");
//            cachedSerQNames.add(qName);
//            cls = ControlTechnology.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Countries");
//            cachedSerQNames.add(qName);
//            cls = Country[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Country");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = _dataMappings.country();
//            cachedSerQNames.add(qName);
//            cls = Country.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = _dataMappings.dataAccessToken();
//            cachedSerQNames.add(qName);
//            cls = DataAccessToken.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Dataset");
//            cachedSerQNames.add(qName);
//            cls = Dataset.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "DatasetNote");
//            cachedSerQNames.add(qName);
//            cls = DatasetNote.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "DatasetNotes");
//            cachedSerQNames.add(qName);
//            cls = DatasetNote[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "DatasetNote");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "DatasetType");
//            cachedSerQNames.add(qName);
//            cls = DatasetType.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "DatasetTypes");
//            cachedSerQNames.add(qName);
//            cls = DatasetType[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "DatasetType");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "DependentJob");
//            cachedSerQNames.add(qName);
//            cls = DependentJob.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "DependentJobs");
//            cachedSerQNames.add(qName);
//            cls = DependentJob[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "DependentJob");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "DoubleValue");
//            cachedSerQNames.add(qName);
//            cls = DoubleValue.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "DoubleValueArray");
//            cachedSerQNames.add(qName);
//            cls = DoubleValue[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "DoubleValue");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "EfficiencyRecord");
//            cachedSerQNames.add(qName);
//            cls = EfficiencyRecord.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "EfficiencyRecords");
//            cachedSerQNames.add(qName);
//            cls = EfficiencyRecord[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "EfficiencyRecord");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "EmfDataset");
//            cachedSerQNames.add(qName);
//            cls = EmfDataset.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "EmfDatasets");
//            cachedSerQNames.add(qName);
//            cls = EmfDataset[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "EmfDataset");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "EmfFileInfo");
//            cachedSerQNames.add(qName);
//            cls = EmfFileInfo.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "EmfFileInfos");
//            cachedSerQNames.add(qName);
//            cls = EmfFileInfo[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "EmfFileInfo");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "EmissionsYear");
//            cachedSerQNames.add(qName);
//            cls = EmissionsYear.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "EmissionsYears");
//            cachedSerQNames.add(qName);
//            cls = EmissionsYear[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "EmissionsYear");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "EquationType");
//            cachedSerQNames.add(qName);
//            cls = EquationType.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "EquationTypes");
//            cachedSerQNames.add(qName);
//            cls = EquationType[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "EquationType");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "EquationTypeVariable");
//            cachedSerQNames.add(qName);
//            cls = EquationTypeVariable.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "EquationTypeVariables");
//            cachedSerQNames.add(qName);
//            cls = EquationTypeVariable[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "EquationTypeVariable");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Executable");
//            cachedSerQNames.add(qName);
//            cls = Executable.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Executables");
//            cachedSerQNames.add(qName);
//            cls = Executable[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Executable");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ExternalSource");
//            cachedSerQNames.add(qName);
//            cls = ExternalSource.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ExternalSources");
//            cachedSerQNames.add(qName);
//            cls = ExternalSource[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ExternalSource");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "FastAnalyses");
//            cachedSerQNames.add(qName);
//            cls = FastAnalysis[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "FastAnalysis");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "FastAnalysis");
//            cachedSerQNames.add(qName);
//            cls = FastAnalysis.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "FastAnalysisInputSector");
//            cachedSerQNames.add(qName);
//            cls = FastAnalysisInputSector.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "FastAnalysisInputSectors");
//            cachedSerQNames.add(qName);
//            cls = FastAnalysisInputSector[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "FastAnalysisInputSector");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "FastAnalysisOutput");
//            cachedSerQNames.add(qName);
//            cls = FastAnalysisOutput.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "FastAnalysisOutputs");
//            cachedSerQNames.add(qName);
//            cls = FastAnalysisOutput[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "FastAnalysisOutput");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "FastAnalysisOutputType");
//            cachedSerQNames.add(qName);
//            cls = FastAnalysisOutputType.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "FastAnalysisOutputTypes");
//            cachedSerQNames.add(qName);
//            cls = FastAnalysisOutputType[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "FastAnalysisOutputType");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "FastAnalysisRun");
//            cachedSerQNames.add(qName);
//            cls = FastAnalysisRun.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//    }
//    private void addBindings1() {
//            java.lang.Class cls;
//            javax.xml.namespace.QName qName;
//            javax.xml.namespace.QName qName2;
//            java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
//            java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
//            java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
//            java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
//            java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
//            java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
//            java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
//            java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
//            java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
//            java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "FastAnalysisRuns");
//            cachedSerQNames.add(qName);
//            cls = FastAnalysisRun[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "FastAnalysisRun");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "FastDataset");
//            cachedSerQNames.add(qName);
//            cls = FastDataset.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "FastDatasets");
//            cachedSerQNames.add(qName);
//            cls = FastDataset[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "FastDataset");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "FastNonPointDataset");
//            cachedSerQNames.add(qName);
//            cls = FastNonPointDataset.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "FastNonPointDatasets");
//            cachedSerQNames.add(qName);
//            cls = FastNonPointDataset[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "FastNonPointDataset");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "FastRun");
//            cachedSerQNames.add(qName);
//            cls = FastRun.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "FastRunInventories");
//            cachedSerQNames.add(qName);
//            cls = FastRunInventory[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "FastRunInventory");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "FastRunInventory");
//            cachedSerQNames.add(qName);
//            cls = FastRunInventory.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "FastRunOutput");
//            cachedSerQNames.add(qName);
//            cls = FastRunOutput.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "FastRunOutputs");
//            cachedSerQNames.add(qName);
//            cls = FastRunOutput[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "FastRunOutput");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "FastRunOutputType");
//            cachedSerQNames.add(qName);
//            cls = FastRunOutputType.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "FastRunOutputTypes");
//            cachedSerQNames.add(qName);
//            cls = FastRunOutputType[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "FastRunOutputType");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "FastRuns");
//            cachedSerQNames.add(qName);
//            cls = FastRun[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "FastRun");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "FileDownload");
//            cachedSerQNames.add(qName);
//            cls = FileDownload.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "FileDownloads");
//            cachedSerQNames.add(qName);
//            cls = FileDownload[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "FileDownload");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "GeoRegion");
//            cachedSerQNames.add(qName);
//            cls = GeoRegion.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "GeoRegions");
//            cachedSerQNames.add(qName);
//            cls = GeoRegion[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "GeoRegion");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Grid");
//            cachedSerQNames.add(qName);
//            cls = Grid.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = _dataMappings.grids();
//            cachedSerQNames.add(qName);
//            cls = Grid[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Grid");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Host");
//            cachedSerQNames.add(qName);
//            cls = Host.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Hosts");
//            cachedSerQNames.add(qName);
//            cls = Host[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Host");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "InputEnvtVar");
//            cachedSerQNames.add(qName);
//            cls = InputEnvtVar.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "InputEnvtVars");
//            cachedSerQNames.add(qName);
//            cls = InputEnvtVar[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "InputEnvtVar");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "InputName");
//            cachedSerQNames.add(qName);
//            cls = InputName.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "InputNames");
//            cachedSerQNames.add(qName);
//            cls = InputName[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "InputName");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "IntendedUse");
//            cachedSerQNames.add(qName);
//            cls = IntendedUse.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "IntendedUses");
//            cachedSerQNames.add(qName);
//            cls = IntendedUse[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "IntendedUse");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "InternalSource");
//            cachedSerQNames.add(qName);
//            cls = InternalSource.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "InternalSources");
//            cachedSerQNames.add(qName);
//            cls = InternalSource[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "InternalSource");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "JobMessage");
//            cachedSerQNames.add(qName);
//            cls = JobMessage.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "JobMessages");
//            cachedSerQNames.add(qName);
//            cls = JobMessage[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "JobMessage");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "JobRunStatus");
//            cachedSerQNames.add(qName);
//            cls = JobRunStatus.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "JobRunStatuses");
//            cachedSerQNames.add(qName);
//            cls = JobRunStatus[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "JobRunStatus");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "KeyVal");
//            cachedSerQNames.add(qName);
//            cls = KeyVal.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "KeyVals");
//            cachedSerQNames.add(qName);
//            cls = KeyVal[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "KeyVal");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Keyword");
//            cachedSerQNames.add(qName);
//            cls = Keyword.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Keywords");
//            cachedSerQNames.add(qName);
//            cls = Keyword[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Keyword");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "LightControlMeasure");
//            cachedSerQNames.add(qName);
//            cls = LightControlMeasure.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "LightControlMeasures");
//            cachedSerQNames.add(qName);
//            cls = LightControlMeasure[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "LightControlMeasure");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "MeteorlogicalYear");
//            cachedSerQNames.add(qName);
//            cls = MeteorlogicalYear.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "MeteorlogicalYears");
//            cachedSerQNames.add(qName);
//            cls = MeteorlogicalYear[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "MeteorlogicalYear");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ModelToRun");
//            cachedSerQNames.add(qName);
//            cls = ModelToRun.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ModelToRuns");
//            cachedSerQNames.add(qName);
//            cls = ModelToRun[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ModelToRun");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Mutex");
//            cachedSerQNames.add(qName);
//            cls = Mutex.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Note");
//            cachedSerQNames.add(qName);
//            cls = Note.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Notes");
//            cachedSerQNames.add(qName);
//            cls = Note[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Note");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "NoteType");
//            cachedSerQNames.add(qName);
//            cls = NoteType.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "NoteTypes");
//            cachedSerQNames.add(qName);
//            cls = NoteType[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "NoteType");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Page");
//            cachedSerQNames.add(qName);
//            cls = Page.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ParameterEnvVar");
//            cachedSerQNames.add(qName);
//            cls = ParameterEnvVar.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ParameterEnvVars");
//            cachedSerQNames.add(qName);
//            cls = ParameterEnvVar[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ParameterEnvVar");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ParameterName");
//            cachedSerQNames.add(qName);
//            cls = ParameterName.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ParameterNames");
//            cachedSerQNames.add(qName);
//            cls = ParameterName[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ParameterName");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "PivotConfiguration");
//            cachedSerQNames.add(qName);
//            cls = PivotConfiguration.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "PivotConfigurations");
//            cachedSerQNames.add(qName);
//            cls = PivotConfiguration[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "PivotConfiguration");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Pollutant");
//            cachedSerQNames.add(qName);
//            cls = Pollutant.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Pollutants");
//            cachedSerQNames.add(qName);
//            cls = Pollutant[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Pollutant");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Project");
//            cachedSerQNames.add(qName);
//            cls = Project.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ProjectionShapeFile");
//            cachedSerQNames.add(qName);
//            cls = ProjectionShapeFile.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ProjectionShapeFiles");
//            cachedSerQNames.add(qName);
//            cls = ProjectionShapeFile[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ProjectionShapeFile");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Projects");
//            cachedSerQNames.add(qName);
//            cls = Project[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Project");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "QAProgram");
//            cachedSerQNames.add(qName);
//            cls = QAProgram.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "QAPrograms");
//            cachedSerQNames.add(qName);
//            cls = QAProgram[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "QAProgram");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "QAStep");
//            cachedSerQNames.add(qName);
//            cls = QAStep.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "QASteps");
//            cachedSerQNames.add(qName);
//            cls = QAStep[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "QAStep");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "QAStepTemplate");
//            cachedSerQNames.add(qName);
//            cls = QAStepTemplate.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "QAStepTemplates");
//            cachedSerQNames.add(qName);
//            cls = QAStepTemplate[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "QAStepTemplate");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Record");
//            cachedSerQNames.add(qName);
//            cls = VersionedRecord.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Records");
//            cachedSerQNames.add(qName);
//            cls = VersionedRecord[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Record");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Reference");
//            cachedSerQNames.add(qName);
//            cls = Reference.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "References");
//            cachedSerQNames.add(qName);
//            cls = Reference[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Reference");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Region");
//            cachedSerQNames.add(qName);
//            cls = Region.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Regions");
//            cachedSerQNames.add(qName);
//            cls = Region[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Region");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "RegionType");
//            cachedSerQNames.add(qName);
//            cls = RegionType.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "RegionTypes");
//            cachedSerQNames.add(qName);
//            cls = RegionType[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "RegionType");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Revision");
//            cachedSerQNames.add(qName);
//            cls = Revision.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Revisions");
//            cachedSerQNames.add(qName);
//            cls = Revision[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Revision");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Scc");
//            cachedSerQNames.add(qName);
//            cls = Scc.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Sccs");
//            cachedSerQNames.add(qName);
//            cls = Scc[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Scc");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Sector");
//            cachedSerQNames.add(qName);
//            cls = Sector.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "SectorCriteria");
//            cachedSerQNames.add(qName);
//            cls = SectorCriteria.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "SectorCriterias");
//            cachedSerQNames.add(qName);
//            cls = SectorCriteria[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "SectorCriteria");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Sectors");
//            cachedSerQNames.add(qName);
//            cls = Sector[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Sector");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "SectorScenario");
//            cachedSerQNames.add(qName);
//            cls = SectorScenario.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "SectorScenarioInventories");
//            cachedSerQNames.add(qName);
//            cls = SectorScenarioInventory[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "SectorScenarioInventory");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "SectorScenarioInventory");
//            cachedSerQNames.add(qName);
//            cls = SectorScenarioInventory.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "SectorScenarioOutput");
//            cachedSerQNames.add(qName);
//            cls = SectorScenarioOutput.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "SectorScenarioOutputs");
//            cachedSerQNames.add(qName);
//            cls = SectorScenarioOutput[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "SectorScenarioOutput");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "SectorScenarioOutputType");
//            cachedSerQNames.add(qName);
//            cls = SectorScenarioOutputType.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "SectorScenarioOutputTypes");
//            cachedSerQNames.add(qName);
//            cls = SectorScenarioOutputType[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "SectorScenarioOutputType");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "SectorScenarios");
//            cachedSerQNames.add(qName);
//            cls = SectorScenario[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "SectorScenario");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "SourceGroup");
//            cachedSerQNames.add(qName);
//            cls = SourceGroup.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "SourceGroups");
//            cachedSerQNames.add(qName);
//            cls = SourceGroup[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "SourceGroup");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Speciation");
//            cachedSerQNames.add(qName);
//            cls = Speciation.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Speciations");
//            cachedSerQNames.add(qName);
//            cls = Speciation[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Speciation");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Status");
//            cachedSerQNames.add(qName);
//            cls = Status.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Statuses");
//            cachedSerQNames.add(qName);
//            cls = Status[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Status");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "StrategyResultType");
//            cachedSerQNames.add(qName);
//            cls = StrategyResultType.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "StrategyType");
//            cachedSerQNames.add(qName);
//            cls = StrategyType.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "StrategyTypes");
//            cachedSerQNames.add(qName);
//            cls = StrategyType[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "StrategyType");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//    }
//    private void addBindings2() {
//            java.lang.Class cls;
//            javax.xml.namespace.QName qName;
//            javax.xml.namespace.QName qName2;
//            java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
//            java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
//            java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
//            java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
//            java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
//            java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
//            java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
//            java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
//            java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
//            java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "SubDir");
//            cachedSerQNames.add(qName);
//            cls = SubDir.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "SubDirs");
//            cachedSerQNames.add(qName);
//            cls = SubDir[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "SubDir");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "SumEffRec");
//            cachedSerQNames.add(qName);
//            cls = SumEffRec.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "SumEffRecs");
//            cachedSerQNames.add(qName);
//            cls = SumEffRec[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "SumEffRec");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "TableMetadata");
//            cachedSerQNames.add(qName);
//            cls = TableMetadata.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "User");
//            cachedSerQNames.add(qName);
//            cls = User.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "UserFeature");
//            cachedSerQNames.add(qName);
//            cls = UserFeature.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "UserFeatures");
//            cachedSerQNames.add(qName);
//            cls = UserFeature[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "UserFeature");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Users");
//            cachedSerQNames.add(qName);
//            cls = User[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "User");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ValueType");
//            cachedSerQNames.add(qName);
//            cls = ValueType.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ValueTypes");
//            cachedSerQNames.add(qName);
//            cls = ValueType[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "ValueType");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Version");
//            cachedSerQNames.add(qName);
//            cls = Version.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Versions");
//            cachedSerQNames.add(qName);
//            cls = Version[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "Version");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "XFileFormat");
//            cachedSerQNames.add(qName);
//            cls = XFileFormat.class;
//            cachedSerClasses.add(cls);
//            cachedSerFactories.add(beansf);
//            cachedDeserFactories.add(beandf);
//
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "XFileFormats");
//            cachedSerQNames.add(qName);
//            cls = XFileFormat[].class;
//            cachedSerClasses.add(cls);
//            qName = new javax.xml.namespace.QName("urn:gov.epa.services.EmfService", "XFileFormat");
//            qName2 = null;
//            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
//            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
//
//    }

    protected org.apache.axis.client.Call createCall() throws java.rmi.RemoteException {
        try {
            org.apache.axis.client.Call _call = super._createCall();
            _call.setMaintainSession(true);
            // never time out
            _call.setTimeout(Integer.valueOf(0));
//            if (super.maintainSessionSet) {
//                _call.setMaintainSession(super.maintainSession);
//            }
            if (super.cachedUsername != null) {
                _call.setUsername(super.cachedUsername);
            }
            if (super.cachedPassword != null) {
                _call.setPassword(super.cachedPassword);
            }
            if (super.cachedEndpoint != null) {
                _call.setTargetEndpointAddress(super.cachedEndpoint);
            }
            if (super.cachedTimeout != null) {
                _call.setTimeout(super.cachedTimeout);
            }
            if (super.cachedPortName != null) {
                _call.setPortName(super.cachedPortName);
            }
            java.util.Enumeration keys = super.cachedProperties.keys();
            while (keys.hasMoreElements()) {
                java.lang.String key = (java.lang.String) keys.nextElement();
                _call.setProperty(key, super.cachedProperties.get(key));
            }
            // All the type mapping information is registered
            // when the first call is made.
            // The type mapping information is actually registered in
            // the TypeMappingRegistry of the service, which
            // is the reason why registration is only needed for the first call.
            synchronized (this) {
                if (firstCall()) {
                    // must set encoding style before registering serializers
                    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
                    _call.setEncodingStyle(org.apache.axis.Constants.URI_SOAP11_ENC);
                    _dataMappings.register(_call);
//                    for (int i = 0; i < cachedSerFactories.size(); ++i) {
//                        java.lang.Class cls = (java.lang.Class) cachedSerClasses.get(i);
//                        javax.xml.namespace.QName qName =
//                                (javax.xml.namespace.QName) cachedSerQNames.get(i);
//                        java.lang.Object x = cachedSerFactories.get(i);
//                        if (x instanceof Class) {
//                            java.lang.Class sf = (java.lang.Class)
//                                 cachedSerFactories.get(i);
//                            java.lang.Class df = (java.lang.Class)
//                                 cachedDeserFactories.get(i);
//                            _call.registerTypeMapping(cls, qName, sf, df, false);
//                        }
//                        else if (x instanceof javax.xml.rpc.encoding.SerializerFactory) {
//                            org.apache.axis.encoding.SerializerFactory sf = (org.apache.axis.encoding.SerializerFactory)
//                                 cachedSerFactories.get(i);
//                            org.apache.axis.encoding.DeserializerFactory df = (org.apache.axis.encoding.DeserializerFactory)
//                                 cachedDeserFactories.get(i);
//                            _call.registerTypeMapping(cls, qName, sf, df, false);
//                        }
//                    }
                }
            }
            return _call;
        }
        catch (java.lang.Throwable _t) {
            throw new org.apache.axis.AxisFault("Failure trying to get the Call object", _t);
        }
    }

    public DataAccessToken save(DataAccessToken token, EmfDataset dataset, Version version) throws java.rmi.RemoteException, gov.epa.emissions.framework.services.EmfException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://editor.services.framework.emissions.epa.gov", "save"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {token, dataset, version});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (DataAccessToken) _resp;
            } catch (java.lang.Exception _exception) {
                return (DataAccessToken) org.apache.axis.utils.JavaUtils.convert(_resp, DataAccessToken.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof gov.epa.emissions.framework.services.EmfException) {
              throw (gov.epa.emissions.framework.services.EmfException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public boolean hasChanges(DataAccessToken token) throws java.rmi.RemoteException, gov.epa.emissions.framework.services.EmfException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://editor.services.framework.emissions.epa.gov", "hasChanges"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {token});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return ((java.lang.Boolean) _resp).booleanValue();
            } catch (java.lang.Exception _exception) {
                return ((java.lang.Boolean) org.apache.axis.utils.JavaUtils.convert(_resp, boolean.class)).booleanValue();
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof gov.epa.emissions.framework.services.EmfException) {
              throw (gov.epa.emissions.framework.services.EmfException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public Version getVersion(int datasetId, int version) throws java.rmi.RemoteException, gov.epa.emissions.framework.services.EmfException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://editor.services.framework.emissions.epa.gov", "getVersion"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {java.lang.Integer.valueOf(datasetId), java.lang.Integer.valueOf(version)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (Version) _resp;
            } catch (java.lang.Exception _exception) {
                return (Version) org.apache.axis.utils.JavaUtils.convert(_resp, Version.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof gov.epa.emissions.framework.services.EmfException) {
              throw (gov.epa.emissions.framework.services.EmfException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public void submit(DataAccessToken token, ChangeSet changeset, int pageNumber) throws java.rmi.RemoteException, gov.epa.emissions.framework.services.EmfException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[3]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://editor.services.framework.emissions.epa.gov", "submit"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {token, changeset, java.lang.Integer.valueOf(pageNumber)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        extractAttachments(_call);
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof gov.epa.emissions.framework.services.EmfException) {
              throw (gov.epa.emissions.framework.services.EmfException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public Version[] getVersions(int datasetId) throws java.rmi.RemoteException, gov.epa.emissions.framework.services.EmfException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[4]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://editor.services.framework.emissions.epa.gov", "getVersions"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {java.lang.Integer.valueOf(datasetId)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (Version[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (Version[]) org.apache.axis.utils.JavaUtils.convert(_resp, Version[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof gov.epa.emissions.framework.services.EmfException) {
              throw (gov.epa.emissions.framework.services.EmfException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public DataAccessToken openSession(User user, DataAccessToken token) throws java.rmi.RemoteException, gov.epa.emissions.framework.services.EmfException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[5]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://editor.services.framework.emissions.epa.gov", "openSession"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {user, token});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (DataAccessToken) _resp;
            } catch (java.lang.Exception _exception) {
                return (DataAccessToken) org.apache.axis.utils.JavaUtils.convert(_resp, DataAccessToken.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof gov.epa.emissions.framework.services.EmfException) {
              throw (gov.epa.emissions.framework.services.EmfException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public DataAccessToken openSession(User user, DataAccessToken token, int pageSize) throws java.rmi.RemoteException, gov.epa.emissions.framework.services.EmfException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[6]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://editor.services.framework.emissions.epa.gov", "openSession"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {user, token, java.lang.Integer.valueOf(pageSize)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (DataAccessToken) _resp;
            } catch (java.lang.Exception _exception) {
                return (DataAccessToken) org.apache.axis.utils.JavaUtils.convert(_resp, DataAccessToken.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof gov.epa.emissions.framework.services.EmfException) {
              throw (gov.epa.emissions.framework.services.EmfException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public void closeSession(User user, DataAccessToken token) throws java.rmi.RemoteException, gov.epa.emissions.framework.services.EmfException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[7]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://editor.services.framework.emissions.epa.gov", "closeSession"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {user, token});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        extractAttachments(_call);
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof gov.epa.emissions.framework.services.EmfException) {
              throw (gov.epa.emissions.framework.services.EmfException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public Version derive(Version base, User user, java.lang.String name) throws java.rmi.RemoteException, gov.epa.emissions.framework.services.EmfException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[8]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://editor.services.framework.emissions.epa.gov", "derive"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {base, user, name});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (Version) _resp;
            } catch (java.lang.Exception _exception) {
                return (Version) org.apache.axis.utils.JavaUtils.convert(_resp, Version.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof gov.epa.emissions.framework.services.EmfException) {
              throw (gov.epa.emissions.framework.services.EmfException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public int getPageCount(DataAccessToken token) throws java.rmi.RemoteException, gov.epa.emissions.framework.services.EmfException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[9]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://editor.services.framework.emissions.epa.gov", "getPageCount"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {token});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return ((java.lang.Integer) _resp).intValue();
            } catch (java.lang.Exception _exception) {
                return ((java.lang.Integer) org.apache.axis.utils.JavaUtils.convert(_resp, int.class)).intValue();
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof gov.epa.emissions.framework.services.EmfException) {
              throw (gov.epa.emissions.framework.services.EmfException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public void discard(DataAccessToken token) throws java.rmi.RemoteException, gov.epa.emissions.framework.services.EmfException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[10]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://editor.services.framework.emissions.epa.gov", "discard"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {token});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        extractAttachments(_call);
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof gov.epa.emissions.framework.services.EmfException) {
              throw (gov.epa.emissions.framework.services.EmfException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public Page getPage(DataAccessToken token, int pageNumber) throws java.rmi.RemoteException, gov.epa.emissions.framework.services.EmfException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[11]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://editor.services.framework.emissions.epa.gov", "getPage"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {token, java.lang.Integer.valueOf(pageNumber)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (Page) _resp;
            } catch (java.lang.Exception _exception) {
                return (Page) org.apache.axis.utils.JavaUtils.convert(_resp, Page.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof gov.epa.emissions.framework.services.EmfException) {
              throw (gov.epa.emissions.framework.services.EmfException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public Page getPageWithRecord(DataAccessToken token, int record) throws java.rmi.RemoteException, gov.epa.emissions.framework.services.EmfException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[12]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://editor.services.framework.emissions.epa.gov", "getPageWithRecord"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {token, java.lang.Integer.valueOf(record)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (Page) _resp;
            } catch (java.lang.Exception _exception) {
                return (Page) org.apache.axis.utils.JavaUtils.convert(_resp, Page.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof gov.epa.emissions.framework.services.EmfException) {
              throw (gov.epa.emissions.framework.services.EmfException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public int getTotalRecords(DataAccessToken token) throws java.rmi.RemoteException, gov.epa.emissions.framework.services.EmfException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[13]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://editor.services.framework.emissions.epa.gov", "getTotalRecords"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {token});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return ((java.lang.Integer) _resp).intValue();
            } catch (java.lang.Exception _exception) {
                return ((java.lang.Integer) org.apache.axis.utils.JavaUtils.convert(_resp, int.class)).intValue();
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof gov.epa.emissions.framework.services.EmfException) {
              throw (gov.epa.emissions.framework.services.EmfException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public Page applyConstraints(DataAccessToken token, java.lang.String rowFilter, java.lang.String sortOrder) throws java.rmi.RemoteException, gov.epa.emissions.framework.services.EmfException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[14]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://editor.services.framework.emissions.epa.gov", "applyConstraints"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {token, rowFilter, sortOrder});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (Page) _resp;
            } catch (java.lang.Exception _exception) {
                return (Page) org.apache.axis.utils.JavaUtils.convert(_resp, Page.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof gov.epa.emissions.framework.services.EmfException) {
              throw (gov.epa.emissions.framework.services.EmfException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public TableMetadata getTableMetadata(java.lang.String table) throws java.rmi.RemoteException, gov.epa.emissions.framework.services.EmfException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[15]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://editor.services.framework.emissions.epa.gov", "getTableMetadata"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {table});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (TableMetadata) _resp;
            } catch (java.lang.Exception _exception) {
                return (TableMetadata) org.apache.axis.utils.JavaUtils.convert(_resp, TableMetadata.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof gov.epa.emissions.framework.services.EmfException) {
              throw (gov.epa.emissions.framework.services.EmfException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public Version markFinal(DataAccessToken token) throws java.rmi.RemoteException, gov.epa.emissions.framework.services.EmfException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[16]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://editor.services.framework.emissions.epa.gov", "markFinal"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {token});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (Version) _resp;
            } catch (java.lang.Exception _exception) {
                return (Version) org.apache.axis.utils.JavaUtils.convert(_resp, Version.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof gov.epa.emissions.framework.services.EmfException) {
              throw (gov.epa.emissions.framework.services.EmfException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

}
