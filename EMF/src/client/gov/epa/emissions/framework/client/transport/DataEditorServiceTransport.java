package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.CommonDebugLevel;
import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.commons.security.User;
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

    public Page getPage(DataAccessToken token, int pageNumber) throws EmfException {
        call.addParam("token", mappings.dataAccessToken());
        call.addIntegerParam("pageNumber");

        call.setOperation("getPage");
        call.setReturnType(mappings.page());
        
        Page page = (Page) call.requestResponse(new Object[] { token, new Integer(pageNumber) });
        if ( CommonDebugLevel.DEBUG_PAGE_2) {
            page.print();
        }

        return page;
    }

    public int getPageCount(DataAccessToken token) throws EmfException {
        call.setOperation("getPageCount");
        call.addParam("token", mappings.dataAccessToken());
        call.setIntegerReturnType();

        Integer cnt = (Integer) call.requestResponse(new Object[] { token });
        return cnt.intValue();
    }

    public Page getPageWithRecord(DataAccessToken token, int recordId) throws EmfException {
        call.setOperation("getPageWithRecord");
        call.setReturnType(mappings.page());

        call.addParam("token", mappings.dataAccessToken());
        call.addIntegerParam("recordId");
        
        Page page = (Page) call.requestResponse(new Object[] { token, new Integer(recordId) });
        if ( CommonDebugLevel.DEBUG_PAGE_2) {
            page.print();
        }   

        return page;
    }

    public int getTotalRecords(DataAccessToken token) throws EmfException {
        call.setOperation("getTotalRecords");
        call.addParam("token", mappings.dataAccessToken());
        call.setIntegerReturnType();

        Integer cnt = (Integer) call.requestResponse(new Object[] { token });
        return cnt.intValue();
    }

    public void close() throws EmfException {
        call.setOperation("close");
        call.setVoidReturnType();

        call.request(new Object[0]);
    }

    public Version derive(Version baseVersion, User user, String name) throws EmfException {
        call.addParam("baseVersion", mappings.version());
        call.addParam("user",mappings.user());
        call.addStringParam("name");

        call.setOperation("derive");
        call.setReturnType(mappings.version());

        return (Version) call.requestResponse(new Object[] { baseVersion, user,name });
    }

    public void submit(DataAccessToken token, ChangeSet changeset, int pageNumber) throws EmfException {
        
        // TODO: JIZHEN debug ChangeSet
        if ( CommonDebugLevel.DEBUG_PAGE_2 && changeset != null) {
            changeset.print();
        }  
        
        call.addParam("token", mappings.dataAccessToken());
        call.addParam("changeset", mappings.changeset());
        call.addIntegerParam("pageNumber");
        call.setOperation("submit");
        call.setVoidReturnType();

        call.request(new Object[] { token, changeset, new Integer(pageNumber) });
    }

    public boolean hasChanges(DataAccessToken token) throws EmfException {
        call.addParam("token", mappings.dataAccessToken());
        call.setOperation("hasChanges");
        call.setBooleanReturnType();

        Object result = call.requestResponse(new Object[] { token });
        return ((Boolean) result).booleanValue();
    }

    public void discard(DataAccessToken token) throws EmfException {
        call.addParam("token", mappings.dataAccessToken());
        call.setOperation("discard");
        call.setVoidReturnType();

        call.request(new Object[] { token });
    }

    public DataAccessToken openSession(User user, DataAccessToken token) throws EmfException {
        call.setOperation("openSession");
        call.addParam("user", mappings.user());
        call.addParam("token", mappings.dataAccessToken());
        call.setReturnType(mappings.dataAccessToken());

        return (DataAccessToken) call.requestResponse(new Object[] { user, token });
    }

    public void closeSession(User user, DataAccessToken token) throws EmfException {
        call.addParam("user", mappings.user());
        call.addParam("token", mappings.dataAccessToken());
        call.setOperation("closeSession");
        call.setVoidReturnType();

        call.request(new Object[] { user, token });
    }

    public DataAccessToken save(DataAccessToken token, EmfDataset dataset, Version version) throws EmfException {
        call.addParam("token", mappings.dataAccessToken());
        call.addParam("dataset", mappings.dataset());
        call.addParam("version",mappings.version());
        
        call.setOperation("save");
        call.setReturnType(mappings.dataAccessToken());

        return (DataAccessToken) call.requestResponse(new Object[] { token, dataset, version });
    }

    public Version markFinal(DataAccessToken token) throws EmfException {
        call.addParam("token", mappings.dataAccessToken());
        call.setOperation("markFinal");
        call.setReturnType(mappings.version());

        return (Version) call.requestResponse(new Object[] { token });
    }

    public Version[] getVersions(int datasetId) throws EmfException {
        call.addLongParam("datasetId");
        call.setOperation("getVersions");
        call.setReturnType(mappings.versions());

        return (Version[]) call.requestResponse(new Object[] { new Integer(datasetId) });
    }

    public Page applyConstraints(DataAccessToken token, String rowFilter, String sortOrder) throws EmfException {
        call.addParam("token", mappings.dataAccessToken());
        call.addStringParam("rowFilter");
        call.addStringParam("sortOrder");

        call.setOperation("applyConstraints");
        call.setReturnType(mappings.page());
        
        Page page = (Page) call.requestResponse(new Object[] { token, rowFilter, sortOrder });
        if ( CommonDebugLevel.DEBUG_PAGE_2) {
            page.print();
        }

        return page;
    }

    public TableMetadata getTableMetadata(String table) throws EmfException {
        call.addStringParam("table");

        call.setOperation("getTableMetadata");
        call.setReturnType(mappings.tablemetadata());
        
        return (TableMetadata)call.requestResponse(new Object[] { table });
    }

    public Version getVersion(int datasetId, int version) throws EmfException {
        call.setOperation("getVersion");
        call.addLongParam("datasetId");
        call.addLongParam("version");
        call.setReturnType(mappings.version());

        return (Version) call.requestResponse(new Object[] { new Integer(datasetId), new Integer(version) });
    }
}
