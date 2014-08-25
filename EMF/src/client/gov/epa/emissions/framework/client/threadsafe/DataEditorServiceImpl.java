/**
 * DataEditorServiceImpl.java
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
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.editor.DataAccessToken;

public interface DataEditorServiceImpl extends java.rmi.Remote {
    public DataAccessToken save(DataAccessToken token, EmfDataset dataset, Version version) throws java.rmi.RemoteException, EmfException;
    public boolean hasChanges(DataAccessToken token) throws java.rmi.RemoteException, EmfException;
    public Version getVersion(int datasetId, int version) throws java.rmi.RemoteException, EmfException;
    public void submit(DataAccessToken token, ChangeSet changeset, int pageNumber) throws java.rmi.RemoteException, EmfException;
    public Version[] getVersions(int datasetId) throws java.rmi.RemoteException, EmfException;
    public DataAccessToken openSession(User user, DataAccessToken token) throws java.rmi.RemoteException, EmfException;
    public DataAccessToken openSession(User user, DataAccessToken token, int pageSize) throws java.rmi.RemoteException, EmfException;
    public void closeSession(User user, DataAccessToken token) throws java.rmi.RemoteException, EmfException;
    public Version derive(Version base, User user, java.lang.String name) throws java.rmi.RemoteException, EmfException;
    public int getPageCount(DataAccessToken token) throws java.rmi.RemoteException, EmfException;
    public void discard(DataAccessToken token) throws java.rmi.RemoteException, EmfException;
    public Page getPage(DataAccessToken token, int pageNumber) throws java.rmi.RemoteException, EmfException;
    public Page getPageWithRecord(DataAccessToken token, int record) throws java.rmi.RemoteException, EmfException;
    public int getTotalRecords(DataAccessToken token) throws java.rmi.RemoteException, EmfException;
    public Page applyConstraints(DataAccessToken token, java.lang.String rowFilter, java.lang.String sortOrder) throws java.rmi.RemoteException, EmfException;
    public TableMetadata getTableMetadata(java.lang.String table) throws java.rmi.RemoteException, EmfException;
    public Version markFinal(DataAccessToken token) throws java.rmi.RemoteException, EmfException;
}
