package gov.epa.emissions.framework.services.fast;

import java.io.Serializable;

public class Grid implements Serializable, Comparable<Grid> {

    private int id;

    private String name;

    private String abbreviation;

    private String resolution;

    private String mapProjection;

    private String description;

    private float xorig, yorig, xcell, ycell, xcent, ycent;

    private int ncols, nrows, nthik;

    /*
     * Default constructor needed for hibernate and axis serialization
     */
    public Grid() {
        //
    }

    public Grid(String name) {
        this();
        this.name = name;
    }

    public Grid(String name, String desc) {
        this();
        this.name = name;
        this.description = desc;
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

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
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

    public float getXcent() {
        return xcent;
    }

    public void setXcent(float xcent) {
        this.xcent = xcent;
    }

    public float getYcent() {
        return ycent;
    }

    public void setYcent(float ycent) {
        this.ycent = ycent;
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

    public boolean equals(Object other) {
        if (other == null || !(other instanceof Grid))
            return false;

        Grid that = (Grid) other;
        return this.id == that.getId() || this.name.equals(that.name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        String abbr = (abbreviation == null || abbreviation.trim().isEmpty()) ? "" : " (" + abbreviation.trim() + ")";

        return getName() + abbr;
    }

    public int compareTo(Grid other) {
        if (other == null)
            return -1;

        return name.compareToIgnoreCase(other.getName());
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }
}
