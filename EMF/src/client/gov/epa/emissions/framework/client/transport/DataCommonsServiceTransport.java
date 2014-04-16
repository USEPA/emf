package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.data.Country;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.data.SourceGroup;
import gov.epa.emissions.commons.data.UserFeature;
import gov.epa.emissions.commons.db.intendeduse.IntendedUse;
import gov.epa.emissions.commons.io.XFileFormat;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.FileDownload;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.DatasetNote;
import gov.epa.emissions.framework.services.data.GeoRegion;
import gov.epa.emissions.framework.services.data.Note;
import gov.epa.emissions.framework.services.data.NoteType;
import gov.epa.emissions.framework.services.data.RegionType;
import gov.epa.emissions.framework.services.editor.Revision;

public class DataCommonsServiceTransport implements DataCommonsService {
    private DataMappings mappings;

    private CallFactory callFactory;
    
    private EmfCall call;
    
    public DataCommonsServiceTransport(String endPoint) {
        callFactory = new CallFactory(endPoint);
        mappings = new DataMappings();
    }

    private EmfCall call() throws EmfException {
        if (call == null)
            call = callFactory.createSessionEnabledCall("DataCommons Service");
        
        return call;
    }

    public synchronized Country[] getCountries() throws EmfException {
        EmfCall call = call();

        call.setOperation("getCountries");
        call.setReturnType(mappings.countries());

        return (Country[]) call.requestResponse(new Object[] {});
    }

    public synchronized Sector[] getSectors() throws EmfException {
        EmfCall call = call();

        call.setOperation("getSectors");
        call.setReturnType(mappings.sectors());

        return (Sector[]) call.requestResponse(new Object[] {});
    }

    public synchronized Keyword[] getKeywords() throws EmfException {
        EmfCall call = call();

        call.setOperation("getKeywords");
        call.setReturnType(mappings.keywords());

        return (Keyword[]) call.requestResponse(new Object[] {});
    }

    public synchronized Sector obtainLockedSector(User owner, Sector sector) throws EmfException {
        EmfCall call = call();

        call.addParam("owner", mappings.user());
        call.addParam("sector", mappings.sector());
        call.setOperation("obtainLockedSector");
        call.setReturnType(mappings.sector());

        return (Sector) call.requestResponse(new Object[] { owner, sector });
    }

    public synchronized Sector updateSector(Sector sector) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateSector");
        call.addParam("sector", mappings.sector());
        call.setReturnType(mappings.sector());

        return (Sector) call.requestResponse(new Object[] { sector });
    }

    public synchronized Sector releaseLockedSector(User user, Sector sector) throws EmfException {
        EmfCall call = call();

        call.setOperation("releaseLockedSector");
        call.addParam("user", mappings.user());
        call.addParam("sector", mappings.sector());
        call.setReturnType(mappings.sector());

        return (Sector) call.requestResponse(new Object[] { user, sector });
    }

    public synchronized DatasetType[] getDatasetTypes() throws EmfException {
        EmfCall call = call();

        call.setOperation("getDatasetTypes");
        call.setReturnType(mappings.datasetTypes());

        return (DatasetType[]) call.requestResponse(new Object[] {});
    }

    // DatasetType (limit to viewable dataset types)
    public synchronized DatasetType[] getDatasetTypes(int userId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getDatasetTypes");
        call.addIntegerParam("userId");
        call.setReturnType(mappings.datasetTypes());

        return (DatasetType[]) call.requestResponse(new Object[] { new Integer(userId) });
    }

    public synchronized DatasetType[] getLightDatasetTypes(int userId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getLightDatasetTypes");
        call.addIntegerParam("userId");
        call.setReturnType(mappings.datasetTypes());

        return (DatasetType[]) call.requestResponse(new Object[] { new Integer(userId) });
    }

    public synchronized DatasetType[] getLightDatasetTypes() throws EmfException {
        EmfCall call = call();

        call.setOperation("getLightDatasetTypes");
        call.setReturnType(mappings.datasetTypes());

        return (DatasetType[]) call.requestResponse(new Object[] {});
    }

    public synchronized DatasetType getDatasetType(String name) throws EmfException {
        EmfCall call = call();

        call.setOperation("getDatasetType");
        call.addStringParam("name");
        call.setReturnType(mappings.datasetType());

        return (DatasetType) call.requestResponse(new Object[] { name });
    }

    public synchronized DatasetType getLightDatasetType(String name) throws EmfException {
        EmfCall call = call();

        call.setOperation("getLightDatasetType");
        call.addStringParam("name");
        call.setReturnType(mappings.datasetType());

        return (DatasetType) call.requestResponse(new Object[] { name });
    }

    public synchronized DatasetType obtainLockedDatasetType(User owner, DatasetType type) throws EmfException {
        EmfCall call = call();

        call.setOperation("obtainLockedDatasetType");
        call.addParam("owner", mappings.user());
        call.addParam("type", mappings.datasetType());
        call.setReturnType(mappings.datasetType());

        return (DatasetType) call.requestResponse(new Object[] { owner, type });
    }

    public synchronized DatasetType updateDatasetType(DatasetType type) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateDatasetType");
        call.addParam("type", mappings.datasetType());
        call.setReturnType(mappings.datasetType());

        return (DatasetType) call.requestResponse(new Object[] { type });
    }

    public synchronized DatasetType releaseLockedDatasetType(User owner, DatasetType type) throws EmfException {
        EmfCall call = call();

        call.setOperation("releaseLockedDatasetType");
        call.addParam("owner", mappings.user());
        call.addParam("type", mappings.datasetType());
        call.setReturnType(mappings.datasetType());

        return (DatasetType) call.requestResponse(new Object[] { owner, type });
    }

    public synchronized Status[] getStatuses(String username) throws EmfException {
        EmfCall call = call();

        call.setOperation("getStatuses");
        call.addStringParam("username");
        call.setReturnType(mappings.statuses());

        return (Status[]) call.requestResponse(new Object[] { username });
    }

    public synchronized Project[] getProjects() throws EmfException {
        EmfCall call = call();

        call.setOperation("getProjects");
        call.setReturnType(mappings.projects());

        return (Project[]) call.requestResponse(new Object[] {});
    }

    public synchronized Region[] getRegions() throws EmfException {
        EmfCall call = call();

        call.setOperation("getRegions");
        call.setReturnType(mappings.regions());

        return (Region[]) call.requestResponse(new Object[] {});
    }
    
    public synchronized UserFeature[] getUserFeatures() throws EmfException {
        EmfCall call = call();

        call.setOperation("getUserFeatures");
        call.setReturnType(mappings.userFeatures());

        return (UserFeature[]) call.requestResponse(new Object[] {});
    }


    public synchronized IntendedUse[] getIntendedUses() throws EmfException {
        EmfCall call = call();

        call.setOperation("getIntendedUses");
        call.setReturnType(mappings.intendeduses());

        return (IntendedUse[]) call.requestResponse(new Object[] {});
    }

    public synchronized void addRegion(Region region) throws EmfException {
        EmfCall call = call();

        call.addParam("region", mappings.region());
        call.setOperation("addRegion");
        call.setVoidReturnType();

        call.request(new Object[] { region });
    }

    public synchronized Project addProject(Project project) throws EmfException {
        EmfCall call = call();

        call.addParam("project", mappings.project());
        call.setOperation("addProject");
        call.setReturnType(mappings.project());

        return (Project)call.requestResponse(new Object[] { project });
    }

    public synchronized void addIntendedUse(IntendedUse intendedUse) throws EmfException {
        EmfCall call = call();

        call.addParam("intendeduse", mappings.intendeduse());
        call.setOperation("addIntendedUse");
        call.setVoidReturnType();

        call.request(new Object[] { intendedUse });
    }

    public synchronized void addSector(Sector sector) throws EmfException {
        EmfCall call = call();

        call.addParam("sector", mappings.sector());
        call.setOperation("addSector");
        call.setVoidReturnType();

        call.request(new Object[] { sector });
    }

    public synchronized void addDatasetType(DatasetType type) throws EmfException {
        EmfCall call = call();

        call.addParam("type", mappings.datasetType());
        call.setOperation("addDatasetType");
        call.setVoidReturnType();

        call.request(new Object[] { type });
    }

    public synchronized DatasetNote[] getDatasetNotes(int datasetId) throws EmfException {
        EmfCall call = call();

        call.addIntegerParam("datasetId");
        call.setOperation("getDatasetNotes");
        call.setReturnType(mappings.datasetNotes());

        return (DatasetNote[]) call.requestResponse(new Object[] { new Integer(datasetId) });
    }
    
    public synchronized Note[] getNotes(int[] noteIds) throws EmfException {
        EmfCall call = call();

        call.addIntArrayParam();
        call.setOperation("getNotes");
        call.setReturnType(mappings.notes());

        return (Note[]) call.requestResponse(new Object[] {noteIds });
    }
    
    public synchronized Note[] getNameContainNotes(String nameContains) throws EmfException {
        EmfCall call = call();

        call.setOperation("getNameContainNotes");
        call.addStringParam("getNameContainNotes");
        call.setReturnType(mappings.notes());

        return (Note[]) call.requestResponse(new Object[] {nameContains});
    }

    public synchronized void addDatasetNote(DatasetNote note) throws EmfException {
        EmfCall call = call();

        call.addParam("note", mappings.datasetNotes());
        call.setOperation("addDatasetNote");
        call.setVoidReturnType();

        call.request(new Object[] { note });
    }

    public synchronized void addDatasetNotes(DatasetNote[] dsNotes) throws EmfException {
        EmfCall call = call();

        call.addParam("dsNotes", mappings.datasetNotes());
        call.setOperation("addDatasetNotes");
        call.setVoidReturnType();

        call.request(new Object[] { dsNotes });
    }

    public synchronized NoteType[] getNoteTypes() throws EmfException {
        EmfCall call = call();

        call.setOperation("getNoteTypes");
        call.setReturnType(mappings.notetypes());

        return (NoteType[]) call.requestResponse(new Object[] {});
    }

    public synchronized Revision[] getRevisions(int datasetId) throws EmfException {
        EmfCall call = call();

        call.addIntegerParam("datasetId");
        call.setOperation("getRevisions");
        call.setReturnType(mappings.revisions());

        return (Revision[]) call.requestResponse(new Object[] { new Integer(datasetId) });
    }

    public synchronized void addRevision(Revision revision) throws EmfException {
        EmfCall call = call();

        call.addParam("revision", mappings.revision());
        call.setOperation("addRevision");
        call.setVoidReturnType();

        call.request(new Object[] { revision });
    }

    public synchronized Revision obtainLockedRevision(User owner, Revision revision) throws EmfException {
        EmfCall call = call();

        call.addParam("owner", mappings.user());
        call.addParam("revision", mappings.revision());
        call.setOperation("obtainLockedRevision");
        call.setReturnType(mappings.revision());

        return (Revision) call.requestResponse(new Object[] { owner, revision });
    }

    public synchronized Revision releaseLockedRevision(User user, Revision revision) throws EmfException {
        EmfCall call = call();

        call.setOperation("releaseLockedRevision");
        call.addParam("user", mappings.user());
        call.addParam("revision", mappings.revision());
        call.setReturnType(mappings.revision());

        return (Revision) call.requestResponse(new Object[] { user, revision });
    }

    public synchronized Revision updateRevision(Revision revision) throws EmfException {

        EmfCall call = call();

        call.setOperation("updateRevision");
        call.addParam("revision", mappings.revision());
        call.setReturnType(mappings.revision());

        return (Revision) call.requestResponse(new Object[] { revision });
    }

    public synchronized Pollutant[] getPollutants() throws EmfException {
        EmfCall call = call();

        call.setOperation("getPollutants");
        call.setReturnType(mappings.pollutants());

        return (Pollutant[]) call.requestResponse(new Object[] {});
    }

    public synchronized void addPollutant(Pollutant pollutant) throws EmfException {
        EmfCall call = call();

        call.addParam("pollutant", mappings.pollutant());
        call.setOperation("addPollutant");
        call.setVoidReturnType();

        call.request(new Object[] { pollutant });
    }

    public synchronized SourceGroup[] getSourceGroups() throws EmfException {
        EmfCall call = call();

        call.setOperation("getSourceGroups");
        call.setReturnType(mappings.sourceGroups());

        return (SourceGroup[]) call.requestResponse(new Object[] {});
    }

    public synchronized void addSourceGroup(SourceGroup sourcegrp) throws EmfException {
        EmfCall call = call();

        call.addParam("sourcegrp", mappings.sourceGroup());
        call.setOperation("addSourceGroup");
        call.setVoidReturnType();

        call.request(new Object[] { sourcegrp });
    }

    public synchronized EmfFileInfo createNewFolder(String folder, String subfolder) throws EmfException {
        EmfCall call = call();

        call.setOperation("createNewFolder");
        call.addStringParam("folder");
        call.addStringParam("subfolder");
        call.setReturnType(mappings.emfFileInfo());

        return (EmfFileInfo)call.requestResponse(new Object[] { folder, subfolder });
    }

    public synchronized EmfFileInfo getDefaultDir() throws EmfException {
        EmfCall call = call();

        call.setOperation("getDefaultDir");
        call.setReturnType(mappings.emfFileInfo());

        return (EmfFileInfo)call.requestResponse(new Object[] {});
    }

    public synchronized EmfFileInfo getHomeDir() throws EmfException {
        EmfCall call = call();

        call.setOperation("getHomeDir");
        call.setReturnType(mappings.emfFileInfo());

        return (EmfFileInfo)call.requestResponse(new Object[] {});
    }

    public synchronized EmfFileInfo[] getRoots() throws EmfException {
        EmfCall call = call();
        
        call.setOperation("getRoots");
        call.setReturnType(mappings.emfFileInfos());
        
        return (EmfFileInfo[])call.requestResponse(new Object[] {});
    }

    public synchronized boolean isFileSystemRoot(EmfFileInfo fileInfo) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("isFileSystemRoot");
        call.addBooleanParameter("fileInfo");
        call.setBooleanReturnType();
        
        return (Boolean)call.requestResponse(new Object[] { fileInfo });
    }

    public synchronized boolean isRoot(EmfFileInfo fileInfo) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("isRoot");
        call.addBooleanParameter("fileInfo");
        call.setBooleanReturnType();
        
        return (Boolean)call.requestResponse(new Object[] { fileInfo });
    }

    public synchronized EmfFileInfo getChild(EmfFileInfo file, String child) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("getChild");
        call.addParam("file", mappings.emfFileInfo());
        call.addStringParam("child");
        call.setReturnType(mappings.emfFileInfo());
        
        return (EmfFileInfo)call.requestResponse(new Object[] { file, child });
    }

    public synchronized EmfFileInfo getParentDirectory(EmfFileInfo file) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("getParent");
        call.addParam("file", mappings.emfFileInfo());
        call.setReturnType(mappings.emfFileInfo());
        
        return (EmfFileInfo)call.requestResponse(new Object[] { file });
    }

    public synchronized EmfFileInfo[] getSubdirs(EmfFileInfo dir) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("getSubdirs");
        call.addParam("dir", mappings.emfFileInfo());
        call.setReturnType(mappings.emfFileInfos());
        call.setTimeOut(60000); //set time out in milliseconds to terminate if service doesn't response
        
        return (EmfFileInfo[])call.requestResponse(new Object[] { dir });
    }

    public synchronized EmfFileInfo[] getEmfFileInfos(EmfFileInfo dir, String filter) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("getEmfFileInfos");
        call.addParam("dir", mappings.emfFileInfo());
        call.addStringParam("filter");
        call.setReturnType(mappings.emfFileInfos());
        call.setTimeOut(60000); //set time out in milliseconds to terminate if service doesn't response
        
        return (EmfFileInfo[])call.requestResponse(new Object[] { dir, filter });
    }

    public void copyQAStepTemplates(User user, QAStepTemplate[] templates, int[] datasetTypeIds, boolean replace) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("copyQAStepTemplates");
        call.addParam("user", mappings.user());
        call.addParam("templates", mappings.qaStepTemplates());
        call.addParam("datasetTypeIds", mappings.integers());
        call.addBooleanParameter("replace");
        call.setVoidReturnType();

        call.request(new Object[] { user, templates, datasetTypeIds, new Boolean(replace) });
    }

    public GeoRegion addGeoRegion(GeoRegion region) throws EmfException {
        EmfCall call = call();

        call.setOperation("addGeoRegion");
        call.addParam("region", mappings.geoRegion());
        call.setReturnType(mappings.geoRegion());

        return (GeoRegion) call.requestResponse(new Object[] { region });
    }
    
    public GeoRegion updateGeoRegion(GeoRegion region, User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateGeoRegion");
        call.addParam("region", mappings.geoRegion());
        call.addParam("user", mappings.user());
        call.setReturnType(mappings.geoRegion());

        return (GeoRegion) call.requestResponse(new Object[] { region, user });
    }

    public GeoRegion[] getGeoRegions() throws EmfException {
        EmfCall call = call();

        call.setOperation("getGeoRegions");
        call.setReturnType(mappings.geoRegions());

        return (GeoRegion[]) call.requestResponse(new Object[] {});    
    }

    public RegionType[] getRegionTypes() throws EmfException {
        EmfCall call = call();

        call.setOperation("getRegionTypes");
        call.setReturnType(mappings.regionTypes());

        return (RegionType[]) call.requestResponse(new Object[] {});  
    }

    public GeoRegion obtainLockedRegion(User user, GeoRegion region) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("obtainLockedRegion");
        call.addParam("user", mappings.user());
        call.addParam("region", mappings.geoRegion());
        call.setReturnType(mappings.geoRegion());
        
        return (GeoRegion) call.requestResponse(new Object[]{user, region});
    }

    public void addDatasetTypeWithFileFormat(DatasetType type, XFileFormat format, String formatFile)
            throws EmfException {
        EmfCall call = call();
        
        call.setOperation("addDatasetTypeWithFileFormat");
        call.addParam("type", mappings.datasetType());
        call.addParam("format", mappings.fileFormat());
        call.addParam("formatFile", mappings.string());
        call.setVoidReturnType();
        
        call.request(new Object[]{type, format, formatFile});
    }

    public void deleteDatasetTypes(User owner, DatasetType[] types) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("deleteDatasetTypes");
        call.addParam("owner", mappings.user());
        call.addParam("types", mappings.datasetTypes());
        call.setVoidReturnType();
        
        call.request(new Object[]{owner, types}); 
    }

    public synchronized FileDownload[] getFileDownloads(Integer userId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getFileDownloads");
        call.addIntegerParam("userId");
        call.setReturnType(mappings.fileDownloads());

        return (FileDownload[]) call.requestResponse(new Object[] { userId });
    }

    public synchronized void addFileDownload(FileDownload fileDownload) throws EmfException {
        EmfCall call = call();

        call.setOperation("addFileDownload");
        call.addParam("fileDownload", mappings.fileDownload());
        call.setVoidReturnType();

        call.requestResponse(new Object[] { fileDownload });
    }

    public synchronized FileDownload[] getUnreadFileDownloads(Integer userId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getUnreadFileDownloads");
        call.addIntegerParam("userId");
        call.setReturnType(mappings.fileDownloads());

        return (FileDownload[]) call.requestResponse(new Object[] { userId });
    }

    public synchronized void markFileDownloadsRead(Integer[] fileDownloadIds) throws EmfException {
        EmfCall call = call();

        call.setOperation("markFileDownloadsRead");
        call.addParam("fileDownloadIds", mappings.integers());
        call.setVoidReturnType();

        call.requestResponse(new Object[] { fileDownloadIds });
    }

    public synchronized void removeFileDownloads(Integer userId) throws EmfException {
        EmfCall call = call();

        call.setOperation("removeFileDownloads");
        call.addIntegerParam("userId");
        call.setVoidReturnType();

        call.requestResponse(new Object[] { userId });
    }
}
