package gov.epa.emissions.framework.client.transport;

import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import gov.epa.emissions.commons.CommonDebugLevel;
import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.threadsafe.DataEditorServiceImpl;
import gov.epa.emissions.framework.client.threadsafe.DataEditorServiceImplService;
import gov.epa.emissions.framework.client.threadsafe.DataEditorServiceImplServiceLocator;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.editor.DataAccessToken;
import gov.epa.emissions.framework.services.editor.DataEditorService;

public class DataEditorServiceTransport implements DataEditorService {
    private DataMappings mappings;

    private EmfCall call;

    public DataEditorServiceTransport(EmfCall call) {
        this.call = call;
        mappings = new DataMappings();
    }

    public DataEditorServiceTransport(DataEditorServiceImpl clientService) {
        this.port = clientService;
    }

    private EmfCall _call;
    
    private synchronized EmfCall getCall() throws EmfException {
        return this.call;
//        if (this.call == null)
//            this.call = RemoteServiceLocator.dataEditorServiceCall();
//        else {
////            this.call.getCall().clearHeaders();
////            this.call.getCall().clearOperation();
////            this.call.getCall().removeAllParameters();
//        }
//        return this.call;
    }

    // Now use the service to get a stub which implements the SDI.
    private DataEditorServiceImpl port = null;

    private DataEditorServiceImpl getClientService() {
        if (port == null) {
            // Make a service
            DataEditorServiceImplService service = new DataEditorServiceImplServiceLocator();

            try {
                port = service.getGovEpaEmfServicesEditorDataEditorService();
            } catch (ServiceException e) {
                // NOTE Auto-generated catch block
                e.printStackTrace();
            }
        }
        return port;
    }
    
    @Override
    public Page getPage(DataAccessToken token, int pageNumber) throws EmfException {
        try {
            return getClientService().getPage(token, pageNumber);
        } catch (RemoteException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getPageCount(DataAccessToken token) throws EmfException {
        try {
            return getClientService().getPageCount(token);
        } catch (RemoteException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public Page getPageWithRecord(DataAccessToken token, int record) throws EmfException {
        try {
            return getClientService().getPageWithRecord(token, record);
        } catch (RemoteException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getTotalRecords(DataAccessToken token) throws EmfException {
        try {
            return getClientService().getTotalRecords(token);
        } catch (RemoteException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public Version[] getVersions(int datasetId) throws EmfException {
        try {
            return getClientService().getVersions(datasetId);
        } catch (RemoteException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Version getVersion(int datasetId, int version) throws EmfException {
        try {
            return getClientService().getVersion(datasetId, version);
        } catch (RemoteException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public TableMetadata getTableMetadata(String table) throws EmfException {
        try {
            return getClientService().getTableMetadata(table);
        } catch (RemoteException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void submit(DataAccessToken token, ChangeSet changeset, int page) throws EmfException {
        try {
            getClientService().submit(token, changeset, page);
        } catch (RemoteException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void discard(DataAccessToken token) throws EmfException {
        try {
            getClientService().discard(token);
        } catch (RemoteException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public DataAccessToken save(DataAccessToken token, EmfDataset dataset, Version version) throws EmfException {
        try {
            return getClientService().save(token, dataset, version);
        } catch (RemoteException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        return token;
    }

    @Override
    public Version derive(Version baseVersion, User user, String name) throws EmfException {
        try {
            return getClientService().derive(baseVersion, user, name);
        } catch (RemoteException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        return baseVersion;
    }

    @Override
    public Version markFinal(DataAccessToken token) throws EmfException {
        try {
            return getClientService().markFinal(token);
        } catch (RemoteException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean hasChanges(DataAccessToken token) throws EmfException {
        try {
            return getClientService().hasChanges(token);
        } catch (RemoteException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public DataAccessToken openSession(User user, DataAccessToken token) throws EmfException {
        try {
            return getClientService().openSession(user, token);
        } catch (RemoteException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        return token;
    }

    @Override
    public void closeSession(User user, DataAccessToken token) throws EmfException {
        try {
            getClientService().closeSession(user, token);
        } catch (RemoteException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public Page applyConstraints(DataAccessToken token, String rowFilter, String sortOrder) throws EmfException {
        try {
            return getClientService().applyConstraints(token, rowFilter, sortOrder);
        } catch (RemoteException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
//    public Page getPage(DataAccessToken token, int pageNumber) throws EmfException {
//        EmfCall call = getCall();
//        
//        call.addParam("token", mappings.dataAccessToken());
//        call.addIntegerParam("pageNumber");
//
//        call.setOperation("getPage");
//        call.setReturnType(mappings.page());
//        
//        Page page = (Page) call.requestResponse(new Object[] { token, new Integer(pageNumber) });
//        if ( CommonDebugLevel.DEBUG_PAGE_2) {
//            page.print();
//        }
//
//        return page;
//    }
//
//    public int getPageCount(DataAccessToken token) throws EmfException {
//        EmfCall call = getCall();
//        call.setOperation("getPageCount");
//        call.addParam("token", mappings.dataAccessToken());
//        call.setIntegerReturnType();
//
//        Integer cnt = (Integer) call.requestResponse(new Object[] { token });
//        return cnt.intValue();
//    }
//
//    public Page getPageWithRecord(DataAccessToken token, int recordId) throws EmfException {
//        EmfCall call = getCall();
//        call.setOperation("getPageWithRecord");
//        call.setReturnType(mappings.page());
//
//        call.addParam("token", mappings.dataAccessToken());
//        call.addIntegerParam("recordId");
//        
//        Page page = (Page) call.requestResponse(new Object[] { token, new Integer(recordId) });
//        if ( CommonDebugLevel.DEBUG_PAGE_2) {
//            page.print();
//        }   
//
//        return page;
//    }
//
//    public int getTotalRecords(DataAccessToken token) throws EmfException {
//        EmfCall call = getCall();
//        call.setOperation("getTotalRecords");
//        call.addParam("token", mappings.dataAccessToken());
//        call.setIntegerReturnType();
//
//        Integer cnt = (Integer) call.requestResponse(new Object[] { token });
//        return cnt.intValue();
//    }
//
//    public void close() throws EmfException {
//        EmfCall call = getCall();
//        call.setOperation("close");
//        call.setVoidReturnType();
//
//        call.request(new Object[0]);
//    }
//
//    public Version derive(Version baseVersion, User user, String name) throws EmfException {
//        EmfCall call = getCall();
//        call.addParam("baseVersion", mappings.version());
//        call.addParam("user",mappings.user());
//        call.addStringParam("name");
//
//        call.setOperation("derive");
//        call.setReturnType(mappings.version());
//
//        return (Version) call.requestResponse(new Object[] { baseVersion, user,name });
//    }
//
//    public void submit(DataAccessToken token, ChangeSet changeset, int pageNumber) throws EmfException {
//        EmfCall call = getCall();
//        
//        // TODO: JIZHEN debug ChangeSet
//        if ( CommonDebugLevel.DEBUG_PAGE_2 && changeset != null) {
//            changeset.print();
//        }  
//        
//        call.addParam("token", mappings.dataAccessToken());
//        call.addParam("changeset", mappings.changeset());
//        call.addIntegerParam("pageNumber");
//        call.setOperation("submit");
//        call.setVoidReturnType();
//
//        call.request(new Object[] { token, changeset, new Integer(pageNumber) });
//    }
//
    public boolean hasChanges2(DataAccessToken token) throws EmfException {
        EmfCall call = getCall();
        call.addParam("token", mappings.dataAccessToken());
        call.setOperation("hasChanges");
        call.setBooleanReturnType();

        Object result = call.requestResponse(new Object[] { token });
        return ((Boolean) result).booleanValue();
    }
//
//    public void discard(DataAccessToken token) throws EmfException {
//        EmfCall call = getCall();
//        call.addParam("token", mappings.dataAccessToken());
//        call.setOperation("discard");
//        call.setVoidReturnType();
//
//        call.request(new Object[] { token });
//    }
//
//    public DataAccessToken openSession(User user, DataAccessToken token) throws EmfException {
//        EmfCall call = getCall();
//        call.setOperation("openSession");
//        call.addParam("user", mappings.user());
//        call.addParam("token", mappings.dataAccessToken());
//        call.setReturnType(mappings.dataAccessToken());
//
//        return (DataAccessToken) call.requestResponse(new Object[] { user, token });
//    }
//
//    public void closeSession(User user, DataAccessToken token) throws EmfException {
//        EmfCall call = getCall();
//        call.addParam("user", mappings.user());
//        call.addParam("token", mappings.dataAccessToken());
//        call.setOperation("closeSession");
//        call.setVoidReturnType();
//
//        call.request(new Object[] { user, token });
//    }
//
//    public DataAccessToken save(DataAccessToken token, EmfDataset dataset, Version version) throws EmfException {
//        EmfCall call = getCall();
//        call.addParam("token", mappings.dataAccessToken());
//        call.addParam("dataset", mappings.dataset());
//        call.addParam("version",mappings.version());
//        
//        call.setOperation("save");
//        call.setReturnType(mappings.dataAccessToken());
//
//        return (DataAccessToken) call.requestResponse(new Object[] { token, dataset, version });
//    }
//
//    public Version markFinal(DataAccessToken token) throws EmfException {
//        EmfCall call = getCall();
//        call.addParam("token", mappings.dataAccessToken());
//        call.setOperation("markFinal");
//        call.setReturnType(mappings.version());
//
//        return (Version) call.requestResponse(new Object[] { token });
//    }
//
//    public Version[] getVersions(int datasetId) throws EmfException {
//        EmfCall call = getCall();
//        call.addLongParam("datasetId");
//        call.setOperation("getVersions");
//        call.setReturnType(mappings.versions());
//
//        return (Version[]) call.requestResponse(new Object[] { new Integer(datasetId) });
//    }
//
//    public Page applyConstraints(DataAccessToken token, String rowFilter, String sortOrder) throws EmfException {
//        EmfCall call = getCall();
//        call.addParam("token", mappings.dataAccessToken());
//        call.addStringParam("rowFilter");
//        call.addStringParam("sortOrder");
//
//        call.setOperation("applyConstraints");
//        call.setReturnType(mappings.page());
//        
//        Page page = (Page) call.requestResponse(new Object[] { token, rowFilter, sortOrder });
//        if ( CommonDebugLevel.DEBUG_PAGE_2) {
//            page.print();
//        }
//
//        return page;
//    }
//
//    public TableMetadata getTableMetadata(String table) throws EmfException {
//        EmfCall call = getCall();
//        call.addStringParam("table");
//
//        call.setOperation("getTableMetadata");
//        call.setReturnType(mappings.tablemetadata());
//        
//        return (TableMetadata)call.requestResponse(new Object[] { table });
//    }
//
//    public Version getVersion(int datasetId, int version) throws EmfException {
//        EmfCall call = getCall();
//        call.setOperation("getVersion");
//        call.addLongParam("datasetId");
//        call.addLongParam("version");
//        call.setReturnType(mappings.version());
//
//        return (Version) call.requestResponse(new Object[] { new Integer(datasetId), new Integer(version) });
//    }
}
