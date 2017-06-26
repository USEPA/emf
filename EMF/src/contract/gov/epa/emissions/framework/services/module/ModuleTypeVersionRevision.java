package gov.epa.emissions.framework.services.module;

import java.io.Serializable;
import java.util.Date;
import java.util.regex.Pattern;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;

public class ModuleTypeVersionRevision implements Serializable {

    private int id;

    private ModuleTypeVersion moduleTypeVersion;

    private int revision;

    private String description;

    private Date creationDate;

    private User creator;

    public void prepareForExport() {
        if (id == 0)
            return;
        id = 0;
        description = getRecord("| ");
        creator = null;
    }
    
    public void prepareForImport(String exportImportMessage) {
        if (revision == (moduleTypeVersion.getModuleTypeVersionRevisions().size() - 1)) {
            if ((description == null) || description.trim().isEmpty()) {
                description = exportImportMessage;
            } else {
                description += "\n" + exportImportMessage;
            }
        }
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ModuleTypeVersion getModuleTypeVersion() {
        return moduleTypeVersion;
    }

    public void setModuleTypeVersion(ModuleTypeVersion moduleTypeVersion) {
        this.moduleTypeVersion = moduleTypeVersion;
    }

    public int getRevision() {
        return revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }

    public String getDescription() {
        return description;
    }

    public String getRecord() {
        if (creator == null) {
            if ((description == null) || description.trim().isEmpty())
                return "";
            return description + "\n\n";
        }

        String creationText = (creationDate == null) ? "?" : CustomDateFormat.format_MM_DD_YYYY_HH_mm(creationDate);
        
        if ((description == null) || description.trim().isEmpty()) {
            return String.format("Revision %d created on %s by %s\n\n",
                                 revision, creationText, creator.getName());
        }
        
        return String.format("Revision %d created on %s by %s\n%s\n\n",
                             revision, creationText, creator.getName(), description);
    }

    public String getRecord(String indent) {
        return indent + Pattern.compile("\\n").matcher(getRecord()).replaceAll("\n" + indent);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public String toString() {
        return String.format("%d", revision);
    }

    public int hashCode() {
        return revision;
    }

    public boolean equals(Object other) {
        return (other instanceof ModuleTypeVersionRevision && ((ModuleTypeVersionRevision) other).getRevision() == revision);
    }

    public int compareTo(ModuleTypeVersionRevision o) {
        return revision - o.getRevision();
    }
}
