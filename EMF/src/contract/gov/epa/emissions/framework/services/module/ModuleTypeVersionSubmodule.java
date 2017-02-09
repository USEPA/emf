package gov.epa.emissions.framework.services.module;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModuleTypeVersionSubmodule implements Serializable {

    // By default, in PostgreSQL, NAMEDATALEN is 64 so the maximum identifier length is 63 bytes.
    public static final int MAX_NAME_LEN = 63; // NAMEDATALEN-1

    private int id;

    private ModuleTypeVersion compositeModuleTypeVersion;

    private String name;

    private ModuleTypeVersion moduleTypeVersion;

    private String description;

    public ModuleTypeVersionSubmodule deepCopy() {
        ModuleTypeVersionSubmodule newModuleTypeVersionSubmodule = new ModuleTypeVersionSubmodule();
        newModuleTypeVersionSubmodule.setCompositeModuleTypeVersion(compositeModuleTypeVersion);
        newModuleTypeVersionSubmodule.setName(name);
        newModuleTypeVersionSubmodule.setModuleTypeVersion(moduleTypeVersion);
        newModuleTypeVersionSubmodule.setDescription(description);
        return newModuleTypeVersionSubmodule;
    }
    
    public static boolean isValidName(String name, final StringBuilder error) {
        error.setLength(0);
        name = name.trim();
        if (name.length() == 0) {
            error.append("Submodule name cannot be empty.");
            return false;
        }
        if (name.length() > MAX_NAME_LEN) {
            error.append(String.format("Submodule name '%s' is longer than %d characters.", name, MAX_NAME_LEN));
            return false;
        }
        Matcher matcher = Pattern.compile("[^a-zA-Z0-9_ ]", Pattern.CASE_INSENSITIVE).matcher(name);
        if (matcher.find()) {
            error.append(String.format("Submodule name '%s' contains illegal characters.", name));
            return false;
        }
        if (name.charAt(0) != '_' && !Character.isLetter(name.charAt(0))) {
            error.append(String.format("Submodule name '%s' must begin with a letter or _ (underscore).", name));
            return false;
        }
        return true;
    }
    
    public boolean isValid(final StringBuilder error) {
        error.setLength(0);
        if (!isValidName(name, error))
            return false;
        if (!moduleTypeVersion.isValid(error))
            return false;
        return true;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ModuleTypeVersion getCompositeModuleTypeVersion() {
        return compositeModuleTypeVersion;
    }

    public void setCompositeModuleTypeVersion(ModuleTypeVersion compositeModuleTypeVersion) {
        this.compositeModuleTypeVersion = compositeModuleTypeVersion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ModuleTypeVersion getModuleTypeVersion() {
        return moduleTypeVersion;
    }

    public void setModuleTypeVersion(ModuleTypeVersion moduleTypeVersion) {
        this.moduleTypeVersion = moduleTypeVersion;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toString() {
        return getName();
    }

    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object other) {
        return (other instanceof ModuleTypeVersionSubmodule && ((ModuleTypeVersionSubmodule) other).getName() == name);
    }

    public int compareTo(ModuleTypeVersionSubmodule o) {
        return name.compareTo(o.getName());
    }
}
