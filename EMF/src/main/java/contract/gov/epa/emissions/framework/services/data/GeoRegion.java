package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;

import java.io.Serializable;
import java.util.Date;

public class GeoRegion implements Serializable, Lockable, Comparable<GeoRegion> {

    private int id;

    private String name;
    
    private RegionType type;
    
    private String abbreviation;
    
    private String resolution;
    
    private String ioapiName;
    
    private String mapProjection;
    
    private String description;
    
    private float xorig, yorig, xcell, ycell;
    
    private int ncols, nrows, nthik;
    
    private int datasetId;
    
    private Version version;
    
    private Mutex lock;
    
    public static GeoRegion generic_grid = new GeoRegion("Generic template region", "Generic template region", "template_region");
    
    public int getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(int datasetId) {
        this.datasetId = datasetId;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getIoapiName() {
        return ioapiName;
    }

    public void setIoapiName(String ioapiName) {
        this.ioapiName = ioapiName;
    }

    public String getMapProjection() {
        return mapProjection;
    }

    public void setMapProjection(String mapProjection) {
        this.mapProjection = mapProjection;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getXorig() {
        return xorig;
    }

    public void setXorig(float xorig) {
        this.xorig = xorig;
    }

    public float getYorig() {
        return yorig;
    }

    public void setYorig(float yorig) {
        this.yorig = yorig;
    }

    public float getXcell() {
        return xcell;
    }

    public void setXcell(float xcell) {
        this.xcell = xcell;
    }

    public float getYcell() {
        return ycell;
    }

    public void setYcell(float ycell) {
        this.ycell = ycell;
    }

    public int getNcols() {
        return ncols;
    }

    public void setNcols(int ncols) {
        this.ncols = ncols;
    }

    public int getNrows() {
        return nrows;
    }

    public void setNrows(int nrows) {
        this.nrows = nrows;
    }

    public int getNthik() {
        return nthik;
    }

    public void setNthik(int nthik) {
        this.nthik = nthik;
    }

    /*
     * Default constructor needed for hibernate and axis serialization
     */
    public GeoRegion() {
        this.lock = new Mutex();
    }

    public GeoRegion(String name) {
        this();
        this.name = name;
    }
    
    public GeoRegion(String name, String desc) {
        this();
        this.name = name;
        this.description = desc;
    }
    
    public GeoRegion(String name, String desc, String abbr) {
        this();
        this.name = name;
        this.description = desc;
        this.abbreviation = abbr;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean equals(Object other) {
        if (other == null || !(other instanceof GeoRegion))
            return false;

        return this.id == ((GeoRegion) other).getId() || ((GeoRegion) other).name.equals(this.name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        String abbr = (abbreviation == null || abbreviation.trim().isEmpty()) ? "" : " (" + abbreviation.trim() + ")";
        
        return getName() + abbr;
    }

    public int compareTo(GeoRegion other) {
        if (other == null)
            return -1;
        
        return name.compareToIgnoreCase(other.getName());
    }

    public RegionType getType() {
        return type;
    }

    public void setType(RegionType type) {
        this.type = type;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }
    
    public Date getLockDate() {
        return lock.getLockDate();
    }

    public void setLockDate(Date lockDate) {
        lock.setLockDate(lockDate);
    }

    public String getLockOwner() {
        return lock.getLockOwner();
    }

    public void setLockOwner(String owner) {
        lock.setLockOwner(owner);
    }

    public boolean isLocked(String owner) {
        return lock.isLocked(owner);
    }

    public boolean isLocked(User owner) {
        return lock.isLocked(owner);
    }

    public boolean isLocked() {
        return lock.isLocked();
    }
}
