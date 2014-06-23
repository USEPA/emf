package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.CommonDebugLevel;
import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.editor.DataAccessToken;
import gov.epa.emissions.framework.services.editor.DataViewService;

public class DataViewServiceTransport implements DataViewService {
    private EmfCall call;

    private DataMappings mappings;

    public DataViewServiceTransport(EmfCall call) {
        this.call = call;
        mappings = new DataMappings();
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

    public Page getPageWithRecord(DataAccessToken token, int record) throws EmfException {
        call.setOperation("getPageWithRecord");
        call.setReturnType(mappings.page());

        call.addParam("token", mappings.dataAccessToken());
        call.addIntegerParam("record");
        
        Page page = (Page) call.requestResponse(new Object[] { token, new Integer(record) });

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

    public DataAccessToken openSession(DataAccessToken token) throws EmfException {
        call.addParam("token", mappings.dataAccessToken());
        call.setOperation("openSession");
        call.setReturnType(mappings.dataAccessToken());

        return (DataAccessToken) call.requestResponse(new Object[] { token });
    }

    public void closeSession(DataAccessToken token) throws EmfException {
        call.addParam("token", mappings.dataAccessToken());
        call.setOperation("closeSession");
        call.setVoidReturnType();

        call.request(new Object[] { token });
    }

    public Version[] getVersions(int datasetId) throws EmfException {
        call.addLongParam("datasetId");
        call.setOperation("getVersions");
        call.setReturnType(mappings.versions());

        return (Version[]) call.requestResponse(new Object[] { new Integer(datasetId) });
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
