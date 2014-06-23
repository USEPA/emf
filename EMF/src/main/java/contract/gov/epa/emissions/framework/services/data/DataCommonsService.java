package gov.epa.emissions.framework.services.data;

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
import gov.epa.emissions.framework.services.editor.Revision;

public interface DataCommonsService {
    // Keywords
    Keyword[] getKeywords() throws EmfException;

    // Projects
    Project[] getProjects() throws EmfException;

    Project addProject(Project project) throws EmfException;

    // intended use
    IntendedUse[] getIntendedUses() throws EmfException;

    void addIntendedUse(IntendedUse intendedUse) throws EmfException;

    // Regions
    Region[] getRegions() throws EmfException;

    void addRegion(Region region) throws EmfException;

    // Countries
    Country[] getCountries() throws EmfException;

    // Sectors
    Sector[] getSectors() throws EmfException;

    Sector obtainLockedSector(User owner, Sector sector) throws EmfException;

    Sector updateSector(Sector sector) throws EmfException;

    void addSector(Sector sector) throws EmfException;

    Sector releaseLockedSector(User user, Sector sector) throws EmfException;

    // DatasetType
    DatasetType[] getDatasetTypes() throws EmfException;

    // DatasetType (limit to viewable dataset types)
    DatasetType[] getDatasetTypes(int userId) throws EmfException;

    DatasetType[] getLightDatasetTypes(int userId) throws EmfException;

    DatasetType[] getLightDatasetTypes() throws EmfException;

    DatasetType getDatasetType(String name) throws EmfException;

    DatasetType getLightDatasetType(String name) throws EmfException;

    void addDatasetType(DatasetType type) throws EmfException;
    
    void addDatasetTypeWithFileFormat(DatasetType type, XFileFormat format, String formatFile) throws EmfException;
    
    void deleteDatasetTypes(User owner, DatasetType[] types) throws EmfException;
    
    DatasetType obtainLockedDatasetType(User owner, DatasetType type) throws EmfException;

    DatasetType updateDatasetType(DatasetType type) throws EmfException;

    void copyQAStepTemplates(User user, QAStepTemplate[] templates, 
            int[] datasetTypeIds, boolean replace) throws EmfException;

    DatasetType releaseLockedDatasetType(User owner, DatasetType type) throws EmfException;

    // FileDownload
    FileDownload[] getFileDownloads(Integer userId) throws EmfException;
    FileDownload[] getUnreadFileDownloads(Integer userId) throws EmfException;
    void addFileDownload(FileDownload fileDownload) throws EmfException;
    void markFileDownloadsRead(Integer[] fileDownloadIds) throws EmfException;
    void removeFileDownloads(Integer userId) throws EmfException;

    
    // Status
    Status[] getStatuses(String username) throws EmfException;

    // Revisions
    Revision obtainLockedRevision(User owner, Revision revision) throws EmfException;
    Revision releaseLockedRevision(User owner, Revision revision) throws EmfException;
    Revision[] getRevisions(int datasetId) throws EmfException;
    void addRevision(Revision revision) throws EmfException;
    Revision updateRevision(Revision revision) throws EmfException;

    // Notes
    Note[] getNameContainNotes(String nameContains) throws EmfException;
    Note[] getNotes(int[] noteIds) throws EmfException;  //get Notes from light notes
    DatasetNote[] getDatasetNotes(int datasetId) throws EmfException;
    void addDatasetNote(DatasetNote datasetNote) throws EmfException;
    void addDatasetNotes(DatasetNote[] dsNotes) throws EmfException;

    // Note types
    NoteType[] getNoteTypes() throws EmfException;
    
    // Pollutants
    Pollutant[] getPollutants() throws EmfException;
    void addPollutant(Pollutant pollutant) throws EmfException;
    
    // Source groups
    SourceGroup[] getSourceGroups() throws EmfException;
    void addSourceGroup(SourceGroup sourcegrp) throws EmfException;
    
    EmfFileInfo[] getEmfFileInfos(EmfFileInfo dir, String filter) throws EmfException;
    
    EmfFileInfo createNewFolder(String folder, String subfolder) throws EmfException;

    EmfFileInfo getDefaultDir() throws EmfException;
    
    EmfFileInfo getHomeDir() throws EmfException;
    
    EmfFileInfo[] getRoots() throws EmfException;

    EmfFileInfo[] getSubdirs(EmfFileInfo dir) throws EmfException;
    
    boolean isRoot(EmfFileInfo fileInfo) throws EmfException;
    
    boolean isFileSystemRoot(EmfFileInfo fileInfo) throws EmfException;
    
    EmfFileInfo getChild(EmfFileInfo file, String child) throws EmfException;

    EmfFileInfo getParentDirectory(EmfFileInfo file) throws EmfException;

    GeoRegion[] getGeoRegions() throws EmfException;

    GeoRegion addGeoRegion(GeoRegion grid) throws EmfException;
    
    GeoRegion updateGeoRegion(GeoRegion region, User user) throws EmfException;
    
    RegionType[] getRegionTypes() throws EmfException;

    GeoRegion obtainLockedRegion(User user, GeoRegion region) throws EmfException;
    
    UserFeature[] getUserFeatures() throws EmfException;

}
