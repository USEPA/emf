package gov.epa.emissions.framework.services.module;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.EmfException;

import java.io.Serializable;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class ModuleTypeVersion implements Serializable {

    private int id;

    private ModuleType moduleType;

    private int version;

    private String name;

    private String description;

    private Date creationDate;

    private Date lastModifiedDate;

    private User creator;

    private int baseVersion;

    private String algorithm;

    private boolean isFinal;

    private Map<String, ModuleTypeVersionDataset> moduleTypeVersionDatasets;

    private Map<String, ModuleTypeVersionParameter> moduleTypeVersionParameters;

    private List<ModuleTypeVersionRevision> moduleTypeVersionRevisions;

    private Map<String, ModuleTypeVersionSubmodule> moduleTypeVersionSubmodules;

    private Map<String, ModuleTypeVersionDatasetConnection> moduleTypeVersionDatasetConnections;
    
    private Map<String, ModuleTypeVersionParameterConnection> moduleTypeVersionParameterConnections;
    
    public ModuleTypeVersion()
    {
        moduleTypeVersionDatasets = new HashMap<String, ModuleTypeVersionDataset>();
        moduleTypeVersionParameters = new HashMap<String, ModuleTypeVersionParameter>();
        moduleTypeVersionRevisions = new ArrayList<ModuleTypeVersionRevision>();
        moduleTypeVersionSubmodules = new HashMap<String, ModuleTypeVersionSubmodule>();
        moduleTypeVersionDatasetConnections = new HashMap<String, ModuleTypeVersionDatasetConnection>();
        moduleTypeVersionParameterConnections = new HashMap<String, ModuleTypeVersionParameterConnection>();
    }

    public void prepareForImport(final StringBuilder changeLog, User user) {
        if (id == 0)
            return;
        id = 0;
        name = "Imported " + name;
        creator = user;
        for(ModuleTypeVersionDataset dataset : moduleTypeVersionDatasets.values()) {
            dataset.prepareForImport(changeLog, user);
        }
        for(ModuleTypeVersionParameter parameter : moduleTypeVersionParameters.values()) {
            parameter.prepareForImport(changeLog, user);
        }
        for(ModuleTypeVersionRevision revision : moduleTypeVersionRevisions) {
            revision.prepareForImport(changeLog, user);
        }
//        for(ModuleTypeVersionSubmodule submodule : moduleTypeVersionSubmodules.values()) {
//            submodule.prepareForImport(user);
//        }
//        for(ModuleTypeVersionDatasetConnection datasetConnection : moduleTypeVersionDatasetConnections.values()) {
//            datasetConnection.prepareForImport(user);
//        }
//        for(ModuleTypeVersionParameterConnection parameterConnection : moduleTypeVersionParameterConnections.values()) {
//            parameterConnection.prepareForImport(user);
//        }
    }
    
    public ModuleTypeVersion deepCopy(User user) {
        ModuleTypeVersion newModuleTypeVersion = new ModuleTypeVersion();
        newModuleTypeVersion.setModuleType(moduleType);
        newModuleTypeVersion.setVersion(version); // same version for now
        newModuleTypeVersion.setName(name + " Copy"); // TODO search for unique new name
        newModuleTypeVersion.setDescription("Copy of " + name + ".\n" + description);
        Date date = new Date();
        newModuleTypeVersion.setCreationDate(date);
        newModuleTypeVersion.setLastModifiedDate(date);
        newModuleTypeVersion.setCreator(user);   
        newModuleTypeVersion.setBaseVersion(version);
        newModuleTypeVersion.setAlgorithm(algorithm);
        newModuleTypeVersion.setIsFinal(false);
        for(ModuleTypeVersionDataset moduleTypeVersionDataset : moduleTypeVersionDatasets.values()) {
            ModuleTypeVersionDataset newModuleTypeVersionDataset = moduleTypeVersionDataset.deepCopy();
            newModuleTypeVersion.addModuleTypeVersionDataset(newModuleTypeVersionDataset);
        }
        for(ModuleTypeVersionParameter moduleTypeVersionParameter : moduleTypeVersionParameters.values()) {
            ModuleTypeVersionParameter newModuleTypeVersionParameter = moduleTypeVersionParameter.deepCopy();
            newModuleTypeVersion.addModuleTypeVersionParameter(newModuleTypeVersionParameter);
        }
        ModuleTypeVersionRevision moduleTypeVersionRevision = new ModuleTypeVersionRevision();
        moduleTypeVersionRevision.setDescription("Copy of " + name);
        moduleTypeVersionRevision.setCreationDate(date);
        moduleTypeVersionRevision.setCreator(user);
        newModuleTypeVersion.addModuleTypeVersionRevision(moduleTypeVersionRevision);
        for(ModuleTypeVersionSubmodule moduleTypeVersionSubmodule : moduleTypeVersionSubmodules.values()) {
            ModuleTypeVersionSubmodule newModuleTypeVersionSubmodule = moduleTypeVersionSubmodule.deepCopy();
            newModuleTypeVersion.addModuleTypeVersionSubmodule(newModuleTypeVersionSubmodule);
        }
        for(ModuleTypeVersionDatasetConnection moduleTypeVersionDatasetConnection : moduleTypeVersionDatasetConnections.values()) {
            ModuleTypeVersionDatasetConnection newModuleTypeVersionDatasetConnection = moduleTypeVersionDatasetConnection.deepCopy();
            ModuleTypeVersionSubmodule targetSubmodule = newModuleTypeVersionDatasetConnection.getTargetSubmodule();
            if (targetSubmodule != null) {
                newModuleTypeVersionDatasetConnection.setTargetSubmodule(newModuleTypeVersion.getModuleTypeVersionSubmodules().get(targetSubmodule.getName()));
            }
            ModuleTypeVersionSubmodule sourceSubmodule = newModuleTypeVersionDatasetConnection.getSourceSubmodule();
            if (sourceSubmodule != null) {
                newModuleTypeVersionDatasetConnection.setSourceSubmodule(newModuleTypeVersion.getModuleTypeVersionSubmodules().get(sourceSubmodule.getName()));
            }
            newModuleTypeVersion.addModuleTypeVersionDatasetConnection(newModuleTypeVersionDatasetConnection);
        }
        for(ModuleTypeVersionParameterConnection moduleTypeVersionParameterConnection : moduleTypeVersionParameterConnections.values()) {
            ModuleTypeVersionParameterConnection newModuleTypeVersionParameterConnection = moduleTypeVersionParameterConnection.deepCopy();
            ModuleTypeVersionSubmodule targetSubmodule = newModuleTypeVersionParameterConnection.getTargetSubmodule();
            if (targetSubmodule != null) {
                newModuleTypeVersionParameterConnection.setTargetSubmodule(newModuleTypeVersion.getModuleTypeVersionSubmodules().get(targetSubmodule.getName()));
            }
            ModuleTypeVersionSubmodule sourceSubmodule = newModuleTypeVersionParameterConnection.getSourceSubmodule();
            if (sourceSubmodule != null) {
                newModuleTypeVersionParameterConnection.setSourceSubmodule(newModuleTypeVersion.getModuleTypeVersionSubmodules().get(sourceSubmodule.getName()));
            }
            newModuleTypeVersion.addModuleTypeVersionParameterConnection(newModuleTypeVersionParameterConnection);
        }
        
        return newModuleTypeVersion;
    }

    public boolean isValidAlgorithm(final StringBuilder error) {
        error.setLength(0);
        
        // Important: keep the list of valid placeholders in sync with
        //            gov.epa.emissions.framework.services.module.ModuleRunner
        Set<String> validPlaceholders = new HashSet<String>();
        validPlaceholders.add("${user.full_name}");
        validPlaceholders.add("${user.id}");
        validPlaceholders.add("${user.account_name}");
        validPlaceholders.add("${module.name}");
        validPlaceholders.add("${module.id}");
        validPlaceholders.add("${module.final}");
        validPlaceholders.add("${run.id}");
        validPlaceholders.add("${run.date}");
        validPlaceholders.add("${run.time}");
        
        for(ModuleTypeVersionDataset moduleTypeVersionDataset : moduleTypeVersionDatasets.values()) {
            String placeholderName = moduleTypeVersionDataset.getPlaceholderName().toLowerCase();
            validPlaceholders.add("${" + placeholderName + "}"); // new syntax using views
            validPlaceholders.add("${" + placeholderName + ".dataset_name}");
            validPlaceholders.add("${" + placeholderName + ".dataset_id}");
            validPlaceholders.add("${" + placeholderName + ".version}");
            validPlaceholders.add("${" + placeholderName + ".table_name}");
            validPlaceholders.add("${" + placeholderName + ".view}");
            validPlaceholders.add("${" + placeholderName + ".is_optional}");
            validPlaceholders.add("${" + placeholderName + ".is_set}");
            validPlaceholders.add("${" + placeholderName + ".mode}");
            if (moduleTypeVersionDataset.getMode().equals(ModuleTypeVersionDataset.OUT)) {
                validPlaceholders.add("${" + placeholderName + ".output_method}");
            }
        }
        
        // verify that all placeholders in the algorithm are valid
        String startPattern = "\\$\\{\\s*";
        String endPattern = "\\s*\\}";
        Matcher matcher = Pattern.compile(startPattern + ".*?" + endPattern, Pattern.CASE_INSENSITIVE).matcher(algorithm);
        while (matcher.find()) {
            int start = matcher.start();
            if (isSqlComment(algorithm, start))
                continue;
            String match = matcher.group().replaceAll("\\{\\s*", "{").replaceAll("\\s*\\.\\s*", ".").replaceAll("\\s*\\}", "}").toLowerCase();
            if (!validPlaceholders.contains(match)) {
                error.append(String.format("Unrecognized placeholder %s at location %d.", matcher.group(), start));
                return false;
            }
        }
        
        validPlaceholders.clear();
        for(ModuleTypeVersionParameter moduleTypeVersionParameter : moduleTypeVersionParameters.values()) {
            String parameter = moduleTypeVersionParameter.getParameterName().toLowerCase();
            validPlaceholders.add("#{" + parameter + "}");
            validPlaceholders.add("#{" + parameter + ".sql_type}");
            validPlaceholders.add("#{" + parameter + ".is_optional}");
            validPlaceholders.add("#{" + parameter + ".is_set}");
            validPlaceholders.add("#{" + parameter + ".mode}");
            if (!moduleTypeVersionParameter.getMode().equals(ModuleTypeVersionParameter.OUT)) {
                validPlaceholders.add("#{" + parameter + ".input_value}");
            }
        }
        
        // verify that all parameters in the algorithm are valid
        startPattern = "\\#\\{\\s*";
        matcher = Pattern.compile(startPattern + ".*?" + endPattern, Pattern.CASE_INSENSITIVE).matcher(algorithm);
        while (matcher.find()) {
            int start = matcher.start();
            if (isSqlComment(algorithm, start))
                continue;
            String match = matcher.group().replaceAll("\\{\\s*", "{").replaceAll("\\s*\\.\\s*", ".").replaceAll("\\s*\\}", "}").toLowerCase();
            if (!validPlaceholders.contains(match)) {
                error.append(String.format("Unrecognized parameter placeholder %s at location %d.", matcher.group(), start));
                return false;
            }
        }
        
        return true;
    }
    
    public static boolean isSqlComment(String text, int position) {
        if (text.length() < 2)
            return false;
        if (position >= text.length())
            position = text.length() - 1;
        char prev_ch = ' ';
        while(position >= 0) {
            char ch = text.charAt(position--);
            if (ch == '\r' || ch == '\n')
                return false;
            if (ch == '-' && prev_ch == '-')
                return true;
            prev_ch = ch;
        }
        return false;
    }

    private boolean hasCircularConnections(final StringBuilder error, Deque<ModuleTypeVersionSubmodule> submodules, Deque<String> connections) {
        error.setLength(0);
        ModuleTypeVersionSubmodule submodule = submodules.peek();
        for(ModuleTypeVersionDatasetConnection nextConnection : moduleTypeVersionDatasetConnections.values()) {
            if (!submodule.equals(nextConnection.getSourceSubmodule()))
                continue;
            ModuleTypeVersionSubmodule nextSubmodule = nextConnection.getTargetSubmodule();
            if (nextSubmodule == null)
                continue;
            String nextConnectionText = String.format("[dataset] %s -> %s" , nextConnection.getSourceName(), nextConnection.getTargetName());
            if (submodules.contains(nextSubmodule)) {
                error.append("Circular connections detected: ");
                Iterator<String> iterator = connections.descendingIterator();
                while (iterator.hasNext())
                    error.append(iterator.next() + ", ");
                error.append(nextConnectionText + ".");
                return true;
            }
            submodules.push(nextSubmodule);
            connections.push(nextConnectionText);
            if (hasCircularConnections(error, submodules, connections))
                return true;
            submodules.pop();
            connections.pop();
        }
        for(ModuleTypeVersionParameterConnection nextConnection : moduleTypeVersionParameterConnections.values()) {
            if (!submodule.equals(nextConnection.getSourceSubmodule()))
                continue;
            ModuleTypeVersionSubmodule nextSubmodule = nextConnection.getTargetSubmodule();
            if (nextSubmodule == null)
                continue;
            String nextConnectionText = String.format("[parameter] %s -> %s" , nextConnection.getSourceName(), nextConnection.getTargetName());
            if (submodules.contains(nextSubmodule)) {
                error.append("Circular connections detected: ");
                Iterator<String> iterator = connections.descendingIterator();
                while (iterator.hasNext())
                    error.append(iterator.next() + ", ");
                error.append(nextConnectionText + ".");
                return true;
            }
            submodules.push(nextSubmodule);
            connections.push(nextConnectionText);
            if (hasCircularConnections(error, submodules, connections))
                return true;
            submodules.pop();
            connections.pop();
        }
        return false;
    }

    private boolean hasCircularConnections(final StringBuilder error) {
        error.setLength(0);
        Deque<ModuleTypeVersionSubmodule> submodules = new ArrayDeque<ModuleTypeVersionSubmodule>();
        Deque<String> connections = new ArrayDeque<String>();
        for(ModuleTypeVersionSubmodule submodule : moduleTypeVersionSubmodules.values()) {
            submodules.push(submodule);
            if (hasCircularConnections(error, submodules, connections))
                return true;
            submodules.pop();
        }
        return false;
    }

    public boolean isValid(final StringBuilder error) {
        error.setLength(0);

        for(ModuleTypeVersionDataset moduleTypeVersionDataset : moduleTypeVersionDatasets.values())
            if (!moduleTypeVersionDataset.isValid(error))
                return false;
        for(ModuleTypeVersionParameter moduleTypeVersionParameter : moduleTypeVersionParameters.values())
            if (!moduleTypeVersionParameter.isValid(error))
                return false;
        
        if (isComposite()) {
            for(ModuleTypeVersionSubmodule moduleTypeVersionSubmodule : moduleTypeVersionSubmodules.values())
                if (!moduleTypeVersionSubmodule.isValid(error))
                    return false;
            for(ModuleTypeVersionDatasetConnection moduleTypeVersionDatasetConnection : moduleTypeVersionDatasetConnections.values())
                if (!moduleTypeVersionDatasetConnection.isValid(error))
                    return false;
            for(ModuleTypeVersionParameterConnection moduleTypeVersionParameterConnection : moduleTypeVersionParameterConnections.values())
                if (!moduleTypeVersionParameterConnection.isValid(error))
                    return false;
            // TODO check that the list of connections matches the list of all internal and external targets
            // TODO check that no connection target matches a composite module type input
            // TODO check that no connection source matches a composite module type output
            if (hasCircularConnections(error))
                return false;
            
        } else {
            if (!isValidAlgorithm(error))
                return false;
        }
        
        return true;
    }

    public boolean isComposite() {
        return moduleType.isComposite();
    }
    
    public Map<String, ModuleTypeVersionDatasetConnectionEndpoint> getTargetDatasetEndpoints() {
        Map<String, ModuleTypeVersionDatasetConnectionEndpoint> endpoints = new HashMap<String, ModuleTypeVersionDatasetConnectionEndpoint>();
        for (ModuleTypeVersionDataset dataset : moduleTypeVersionDatasets.values()) {
            if (!dataset.getMode().equals(ModuleTypeVersionDataset.IN)) {
                ModuleTypeVersionDatasetConnectionEndpoint endpoint = new ModuleTypeVersionDatasetConnectionEndpoint(this, null, dataset.getPlaceholderName());
                endpoints.put(endpoint.getEndpointName(), endpoint);
            }
        }
        for (ModuleTypeVersionSubmodule submodule : moduleTypeVersionSubmodules.values()) {
            for (ModuleTypeVersionDataset dataset : submodule.getModuleTypeVersion().getModuleTypeVersionDatasets().values()) {
                if (!dataset.getMode().equals(ModuleTypeVersionDataset.OUT)) {
                    ModuleTypeVersionDatasetConnectionEndpoint endpoint = new ModuleTypeVersionDatasetConnectionEndpoint(this, submodule, dataset.getPlaceholderName());
                    endpoints.put(endpoint.getEndpointName(), endpoint);
                }
            }
        }
        return endpoints;
    }
    
    public Map<String, ModuleTypeVersionParameterConnectionEndpoint> getTargetParameterEndpoints() {
        Map<String, ModuleTypeVersionParameterConnectionEndpoint> endpoints = new HashMap<String, ModuleTypeVersionParameterConnectionEndpoint>();
        for (ModuleTypeVersionParameter parameter : moduleTypeVersionParameters.values()) {
            if (!parameter.getMode().equals(ModuleTypeVersionParameter.IN)) {
                ModuleTypeVersionParameterConnectionEndpoint endpoint = new ModuleTypeVersionParameterConnectionEndpoint(this, null, parameter.getParameterName());
                endpoints.put(endpoint.getEndpointName(), endpoint);
            }
        }
        for (ModuleTypeVersionSubmodule submodule : moduleTypeVersionSubmodules.values()) {
            for (ModuleTypeVersionParameter parameter : submodule.getModuleTypeVersion().getModuleTypeVersionParameters().values()) {
                if (!parameter.getMode().equals(ModuleTypeVersionParameter.OUT)) {
                    ModuleTypeVersionParameterConnectionEndpoint endpoint = new ModuleTypeVersionParameterConnectionEndpoint(this, submodule, parameter.getParameterName());
                    endpoints.put(endpoint.getEndpointName(), endpoint);
                }
            }
        }
        return endpoints;
    }
    
    public Map<String, ModuleTypeVersionDatasetConnectionEndpoint> getSourceDatasetEndpoints(ModuleTypeVersionDatasetConnection datasetConnection) {
        Map<String, ModuleTypeVersionDatasetConnectionEndpoint> endpoints = new HashMap<String, ModuleTypeVersionDatasetConnectionEndpoint>();
        boolean includeOptional = datasetConnection.isOptional();
        try {
            String datasetTypeName = datasetConnection.getTargetDatasetTypeName();
            for (ModuleTypeVersionDataset dataset : moduleTypeVersionDatasets.values()) {
                if (dataset.getIsOptional() && !includeOptional)
                    continue;
                if (dataset.getDatasetType().getName().equals(datasetTypeName) && !dataset.getMode().equals(ModuleTypeVersionDataset.OUT)) {
                    ModuleTypeVersionDatasetConnectionEndpoint endpoint = new ModuleTypeVersionDatasetConnectionEndpoint(this, null, dataset.getPlaceholderName());
                    endpoints.put(endpoint.getEndpointName(), endpoint);
                }
            }
            for (ModuleTypeVersionSubmodule submodule : moduleTypeVersionSubmodules.values()) {
                if (submodule.equals(datasetConnection.getTargetSubmodule()))
                    continue;
                for (ModuleTypeVersionDataset dataset : submodule.getModuleTypeVersion().getModuleTypeVersionDatasets().values()) {
                    if (dataset.getIsOptional() && !includeOptional)
                        continue;
                    if (dataset.getDatasetType().getName().equals(datasetTypeName) && !dataset.getMode().equals(ModuleTypeVersionDataset.IN)) {
                        ModuleTypeVersionDatasetConnectionEndpoint endpoint = new ModuleTypeVersionDatasetConnectionEndpoint(this, submodule, dataset.getPlaceholderName());
                        endpoints.put(endpoint.getEndpointName(), endpoint);
                    }
                }
            }
        } catch (EmfException e) {
            e.printStackTrace();
        }
        return endpoints;
    }
    
    private static boolean compatibleParameterTypes(String sourceSqlType, String targetSqlType) {
        // TODO a text source should not be compatible with a non-text target
        return true; // for now
    }

    public Map<String, ModuleTypeVersionParameterConnectionEndpoint> getSourceParameterEndpoints(ModuleTypeVersionParameterConnection parameterConnection) {
        Map<String, ModuleTypeVersionParameterConnectionEndpoint> endpoints = new HashMap<String, ModuleTypeVersionParameterConnectionEndpoint>();
        try {
            String targetSqlType = parameterConnection.getTargetSqlType();
            for (ModuleTypeVersionParameter parameter : moduleTypeVersionParameters.values()) {
                if (compatibleParameterTypes(parameter.getSqlParameterType(), targetSqlType) && !parameter.getMode().equals(ModuleTypeVersionParameter.OUT)) {
                    ModuleTypeVersionParameterConnectionEndpoint endpoint = new ModuleTypeVersionParameterConnectionEndpoint(this, null, parameter.getParameterName());
                    endpoints.put(endpoint.getEndpointName(), endpoint);
                }
            }
            for (ModuleTypeVersionSubmodule submodule : moduleTypeVersionSubmodules.values()) {
                if (submodule.equals(parameterConnection.getTargetSubmodule()))
                    continue;
                for (ModuleTypeVersionParameter parameter : submodule.getModuleTypeVersion().getModuleTypeVersionParameters().values()) {
                    if (compatibleParameterTypes(parameter.getSqlParameterType(), targetSqlType) && !parameter.getMode().equals(ModuleTypeVersionParameter.IN)) {
                        ModuleTypeVersionParameterConnectionEndpoint endpoint = new ModuleTypeVersionParameterConnectionEndpoint(this, submodule, parameter.getParameterName());
                        endpoints.put(endpoint.getEndpointName(), endpoint);
                    }
                }
            }
        } catch (EmfException e) {
            e.printStackTrace();
        }
        return endpoints;
    }
    
    public Map<String, ModuleInternalDataset> computeInternalDatasets(Module compositeModule) {
        Map<String, ModuleInternalDataset> internalDatasets = new HashMap<String, ModuleInternalDataset>();
        for (ModuleTypeVersionSubmodule submodule : moduleTypeVersionSubmodules.values()) {
            submodule.computeInternalDatasets(internalDatasets, "" + submodule.getId(), submodule.getName(), compositeModule);
        }
        return internalDatasets;
    }

    public void computeInternalDatasets(Map<String, ModuleInternalDataset> internalDatasets, String placeholderPath, String placeholderPathNames,
                                        ModuleTypeVersionSubmodule parentSubmodule, Module compositeModule) {
        for(ModuleTypeVersionDataset moduleTypeVersionDataset : moduleTypeVersionDatasets.values()) {
            if (moduleTypeVersionDataset.isModeIN())
                continue;
            // if this output dataset is connected to an output dataset for the parent composite module then it's not internal
            boolean isExternal = false;
            for(ModuleTypeVersionDatasetConnection datasetConnection : parentSubmodule.getCompositeModuleTypeVersion().getModuleTypeVersionDatasetConnections().values()) {
                if ((datasetConnection.getTargetSubmodule() == null) &&
                    (parentSubmodule == datasetConnection.getSourceSubmodule()) &&
                    moduleTypeVersionDataset.getPlaceholderName().equals(datasetConnection.getSourcePlaceholderName())) {
                    isExternal = true;
                    break;
                }
            }
            if (isExternal)
                continue;
            // TODO revisit this logic for INOUT datasets (maybe take snapshots of the data and expose them as internal datasets)
            ModuleInternalDataset internalDataset = new ModuleInternalDataset();
            internalDataset.setCompositeModule(compositeModule);
            internalDataset.setPlaceholderPath(placeholderPath + "/" + moduleTypeVersionDataset.getPlaceholderName());
            internalDataset.setPlaceholderPathNames(placeholderPathNames + " / " + moduleTypeVersionDataset.getPlaceholderName());
            internalDataset.setModuleTypeVersionDataset(moduleTypeVersionDataset);
            internalDataset.setKeep(false);
            internalDataset.setDatasetNamePattern("");
            internalDatasets.put(internalDataset.getPlaceholderPath(), internalDataset);
        }
        for (ModuleTypeVersionSubmodule submodule : moduleTypeVersionSubmodules.values()) {
            submodule.computeInternalDatasets(internalDatasets, placeholderPath + "/" + submodule.getId(), placeholderPathNames + " / " + submodule.getName(), compositeModule);
        }
    }

    public Map<String, ModuleInternalParameter> computeInternalParameters(Module compositeModule) {
        Map<String, ModuleInternalParameter> internalParameters = new HashMap<String, ModuleInternalParameter>();
        for (ModuleTypeVersionSubmodule submodule : moduleTypeVersionSubmodules.values()) {
            submodule.computeInternalParameters(internalParameters, "" + submodule.getId(), submodule.getName(), compositeModule);
        }
        return internalParameters;
    }

    public void computeInternalParameters(Map<String, ModuleInternalParameter> internalParameters, String parameterPath, String parameterPathNames,
                                          ModuleTypeVersionSubmodule parentSubmodule, Module compositeModule) {
        for(ModuleTypeVersionParameter moduleTypeVersionParameter : moduleTypeVersionParameters.values()) {
            if (moduleTypeVersionParameter.isModeIN())
                continue;
            // if this output parameter is connected to an output parameter for the parent composite module then it's not internal 
            boolean isExternal = false;
            for(ModuleTypeVersionParameterConnection datasetConnection : parentSubmodule.getCompositeModuleTypeVersion().getModuleTypeVersionParameterConnections().values()) {
                if ((datasetConnection.getTargetSubmodule() == null) &&
                    (parentSubmodule == datasetConnection.getSourceSubmodule()) &&
                    moduleTypeVersionParameter.getParameterName().equals(datasetConnection.getSourceParameterName())) {
                    isExternal = true;
                    break;
                }
            }
            if (isExternal)
                continue;
            // TODO revisit this logic for INOUT parameters (maybe take snapshots of the data and expose them as internal parameters)
            ModuleInternalParameter internalParameter = new ModuleInternalParameter();
            internalParameter.setCompositeModule(compositeModule);
            internalParameter.setParameterPath(parameterPath + "/" + moduleTypeVersionParameter.getParameterName());
            internalParameter.setParameterPathNames(parameterPathNames + " / " + moduleTypeVersionParameter.getParameterName());
            internalParameter.setModuleTypeVersionParameter(moduleTypeVersionParameter);
            internalParameter.setKeep(false);
            internalParameters.put(internalParameter.getParameterPath(), internalParameter);
        }
        for (ModuleTypeVersionSubmodule submodule : moduleTypeVersionSubmodules.values()) {
            submodule.computeInternalParameters(internalParameters, parameterPath + "/" + submodule.getId(), parameterPathNames + " / " + submodule.getName(), compositeModule);
        }
    }

    public TreeMap<Integer, ModuleTypeVersion> getUnfinalizedSubmodules() {
        TreeMap<Integer, ModuleTypeVersion> unfinalizedSubmodules = new TreeMap<Integer, ModuleTypeVersion>();
        for (ModuleTypeVersionSubmodule submodule : moduleTypeVersionSubmodules.values()) {
            ModuleTypeVersion moduleTypeVersion = submodule.getModuleTypeVersion();
            if (moduleTypeVersion.getIsFinal())
                continue;
            if (unfinalizedSubmodules.containsKey(moduleTypeVersion.getId()))
                continue;
            unfinalizedSubmodules.put(moduleTypeVersion.getId(), moduleTypeVersion);
            unfinalizedSubmodules.putAll(moduleTypeVersion.getUnfinalizedSubmodules());
        }
        return unfinalizedSubmodules;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ModuleType getModuleType() {
        return moduleType;
    }

    public void setModuleType(ModuleType moduleType) {
        this.moduleType = moduleType;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String versionName() {
        return version + " - " + name;
    }

    public String versionNameFinal() {
        return versionName() + (isFinal ? " - Final" : "");
    }

    // %s for module type, %d for version number, %s for version name
    public String fullNameSDS(String format) {
        return String.format(format, moduleType.getName(), version, name);
    }

    // %s for module type, %s for version name
    public String fullNameSS(String format) {
        return String.format(format, moduleType.getName(), versionName());
    }

    public String getDescription() {
        return description;
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

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public int getBaseVersion() {
        return baseVersion;
    }

    public void setBaseVersion(int baseVersion) {
        this.baseVersion = baseVersion;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public boolean getIsFinal() {
        return isFinal;
    }

    public void setIsFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    // moduleTypeVersionDatasets

    public Map<String, ModuleTypeVersionDataset> getModuleTypeVersionDatasets() {
        return this.moduleTypeVersionDatasets;
    }

    public ModuleTypeVersionDataset getModuleTypeVersionDataset(String placeholderName) throws EmfException {
        if (moduleTypeVersionDatasets.containsKey(placeholderName))
            return moduleTypeVersionDatasets.get(placeholderName);
        String errorMessage = String.format("Module type \"%s\" version \"%s\" does not have a dataset placeholder named \"%s\"",
                                            moduleType.getName(), versionName(), placeholderName);
        throw new EmfException(errorMessage);
    }

    public boolean containsDatasetId(int datasetId) {
        for(ModuleTypeVersionDataset moduleTypeVersionDataset : moduleTypeVersionDatasets.values()) {
            if (moduleTypeVersionDataset.getId() == datasetId) {
                return true;
            }
        }
        return false;
    }
    
    public void setModuleTypeVersionDatasets(Map<String, ModuleTypeVersionDataset> moduleTypeVersionDatasets) {
        this.moduleTypeVersionDatasets = moduleTypeVersionDatasets;
    }

    public void addModuleTypeVersionDataset(ModuleTypeVersionDataset moduleTypeVersionDataset) {
        if (this.moduleTypeVersionDatasets.containsKey(moduleTypeVersionDataset.getPlaceholderName())) {
            removeModuleTypeVersionDataset(moduleTypeVersionDataset.getPlaceholderName());
        }
        moduleTypeVersionDataset.setModuleTypeVersion(this);
        this.moduleTypeVersionDatasets.put(moduleTypeVersionDataset.getPlaceholderName(), moduleTypeVersionDataset);
        
        // add connections to the new targets (OUT/INOUT datasets)
        if (isComposite() && !moduleTypeVersionDataset.getMode().equals(ModuleTypeVersionParameter.IN)) {
            ModuleTypeVersionDatasetConnection datasetConnection = new ModuleTypeVersionDatasetConnection();
            datasetConnection.setTargetSubmodule(null);
            datasetConnection.setTargetPlaceholderName(moduleTypeVersionDataset.getPlaceholderName());
            addModuleTypeVersionDatasetConnection(datasetConnection);
        }
    }

    public void removeModuleTypeVersionDataset(ModuleTypeVersionDataset moduleTypeVersionDataset) {
        removeModuleTypeVersionDataset(moduleTypeVersionDataset.getPlaceholderName());
    }

    public void removeModuleTypeVersionDataset(String placeholderName) {
        this.moduleTypeVersionDatasets.remove(placeholderName);
        
        if (isComposite()) {
            // if the removed placeholder is the target of a connection, remove the connection
            removeModuleTypeVersionDatasetConnection(placeholderName); 
            // if the removed placeholder is the source of a connection, set the connection source to null
            for (ModuleTypeVersionDatasetConnection datasetConnection : moduleTypeVersionDatasetConnections.values()) {
                if ((datasetConnection.getSourceSubmodule() == null) && placeholderName.equals(datasetConnection.getSourcePlaceholderName())) {
                    datasetConnection.setSourcePlaceholderName(null);
                }
            }
        }
    }

    // moduleTypeVersionParameters

    public Map<String, ModuleTypeVersionParameter> getModuleTypeVersionParameters() {
        return this.moduleTypeVersionParameters;
    }

    public ModuleTypeVersionParameter getModuleTypeVersionParameter(String parameterName) throws EmfException {
        if (moduleTypeVersionParameters.containsKey(parameterName))
            return moduleTypeVersionParameters.get(parameterName);
        String errorMessage = String.format("Module type \"%s\" version \"%s\" does not have a parameter named \"%s\"",
                                            moduleType.getName(), versionName(), parameterName);
        throw new EmfException(errorMessage);
    }

    public boolean containsParameterId(int parameterId) {
        for(ModuleTypeVersionParameter moduleTypeVersionParameter : moduleTypeVersionParameters.values()) {
            if (moduleTypeVersionParameter.getId() == parameterId) {
                return true;
            }
        }
        return false;
    }
    
    public void setModuleTypeVersionParameters(Map<String, ModuleTypeVersionParameter> moduleTypeVersionParameters) {
        this.moduleTypeVersionParameters = moduleTypeVersionParameters;
    }

    public void addModuleTypeVersionParameter(ModuleTypeVersionParameter moduleTypeVersionParameter) {
        if (this.moduleTypeVersionParameters.containsKey(moduleTypeVersionParameter.getParameterName())) {
            removeModuleTypeVersionParameter(moduleTypeVersionParameter.getParameterName());
        }
        moduleTypeVersionParameter.setModuleTypeVersion(this);
        this.moduleTypeVersionParameters.put(moduleTypeVersionParameter.getParameterName(), moduleTypeVersionParameter);
        
        // add connections to the new targets (OUT/INOUT parameters)
        if (isComposite() && !moduleTypeVersionParameter.getMode().equals(ModuleTypeVersionParameter.IN)) {
            ModuleTypeVersionParameterConnection parameterConnection = new ModuleTypeVersionParameterConnection();
            parameterConnection.setTargetSubmodule(null);
            parameterConnection.setTargetParameterName(moduleTypeVersionParameter.getParameterName());
            addModuleTypeVersionParameterConnection(parameterConnection);
        }
    }

    public void removeModuleTypeVersionParameter(ModuleTypeVersionParameter moduleTypeVersionParameter) {
        removeModuleTypeVersionParameter(moduleTypeVersionParameter.getParameterName());
    }

    public void removeModuleTypeVersionParameter(String parameterName) {
        this.moduleTypeVersionParameters.remove(parameterName);
        
        if (isComposite()) {
            // if the removed parameter is the target of a connection, remove the connection
            removeModuleTypeVersionParameterConnection(parameterName);
            // if the removed parameter is the source of a connection, set the connection source to null
            for (ModuleTypeVersionParameterConnection parameterConnection : moduleTypeVersionParameterConnections.values()) {
                if ((parameterConnection.getSourceSubmodule() == null) && parameterName.equals(parameterConnection.getSourceParameterName())) {
                    parameterConnection.setSourceParameterName(null);
                }
            }
        }
    }

    // moduleTypeVersionRevisions

    public List<ModuleTypeVersionRevision> getModuleTypeVersionRevisions() {
        return this.moduleTypeVersionRevisions;
    }

    public void setModuleTypeVersionRevisions(List<ModuleTypeVersionRevision> moduleTypeVersionRevisions) {
        this.moduleTypeVersionRevisions = moduleTypeVersionRevisions;
    }

    public void addModuleTypeVersionRevision(ModuleTypeVersionRevision moduleTypeVersionRevision) {
        moduleTypeVersionRevision.setModuleTypeVersion(this);
        moduleTypeVersionRevision.setRevision(this.moduleTypeVersionRevisions.size());
        this.moduleTypeVersionRevisions.add(moduleTypeVersionRevision);
    }

    public String revisionsReport() {
        StringBuilder revisionsReport = new StringBuilder();
        
        for (ModuleTypeVersionRevision moduleTypeVersionRevision : moduleTypeVersionRevisions) {
            String creationDate = (moduleTypeVersionRevision.getCreationDate() == null)
                                  ? "?"
                                  : CustomDateFormat.format_MM_DD_YYYY_HH_mm(moduleTypeVersionRevision.getCreationDate());
            
            String creator = (moduleTypeVersionRevision.getCreator() == null)
                             ? "?"
                             : moduleTypeVersionRevision.getCreator().getName();
            
            String record = String.format("Revision %d created on %s by %s\n%s\n\n",
                                          moduleTypeVersionRevision.getRevision(),
                                          creationDate, creator,
                                          moduleTypeVersionRevision.getDescription());
            
            revisionsReport.append(record);
        }
        
        return revisionsReport.toString();
    }
    
    // moduleTypeVersionSubmodules

    public Map<String, ModuleTypeVersionSubmodule> getModuleTypeVersionSubmodules() {
        return this.moduleTypeVersionSubmodules;
    }

    public boolean containsSubmoduleId(int submoduleId) {
        for(ModuleTypeVersionSubmodule moduleTypeVersionSubmodule : moduleTypeVersionSubmodules.values()) {
            if (moduleTypeVersionSubmodule.getId() == submoduleId) {
                return true;
            }
        }
        return false;
    }
    
    public void setModuleTypeVersionSubmodules(Map<String, ModuleTypeVersionSubmodule> moduleTypeVersionSubmodules) {
        this.moduleTypeVersionSubmodules = moduleTypeVersionSubmodules;
    }

    public void addModuleTypeVersionSubmodule(ModuleTypeVersionSubmodule moduleTypeVersionSubmodule) {
        ModuleTypeVersion submoduleTypeVersion = moduleTypeVersionSubmodule.getModuleTypeVersion();

        // save the IDs of all dataset connections indexed by connection name
        Map<String, Integer> datasetTargetIds = new HashMap<String, Integer>();
        for (ModuleTypeVersionDatasetConnection datasetConnection : moduleTypeVersionDatasetConnections.values()) {
            datasetTargetIds.put(datasetConnection.getConnectionName(), datasetConnection.getId());
        }
        // save the IDs of all parameter connections indexed by connection name
        Map<String, Integer> parameterTargetIds = new HashMap<String, Integer>();
        for (ModuleTypeVersionParameterConnection parameterConnection : moduleTypeVersionParameterConnections.values()) {
            parameterTargetIds.put(parameterConnection.getConnectionName(), parameterConnection.getId());
        }

        if (this.moduleTypeVersionSubmodules.containsKey(moduleTypeVersionSubmodule.getName())) {
            removeModuleTypeVersionSubmodule(moduleTypeVersionSubmodule.getName());
        }
        moduleTypeVersionSubmodule.setCompositeModuleTypeVersion(this);
        this.moduleTypeVersionSubmodules.put(moduleTypeVersionSubmodule.getName(), moduleTypeVersionSubmodule);

        // add connections for the new internal targets (IN/INOUT datasets and parameters)
        for (ModuleTypeVersionDataset dataset : submoduleTypeVersion.getModuleTypeVersionDatasets().values()) {
            if (dataset.getMode().equals(ModuleTypeVersionDataset.OUT))
                continue;
            ModuleTypeVersionDatasetConnection datasetConnection = new ModuleTypeVersionDatasetConnection();
            datasetConnection.setTargetSubmodule(moduleTypeVersionSubmodule);
            datasetConnection.setTargetPlaceholderName(dataset.getPlaceholderName());
            addModuleTypeVersionDatasetConnection(datasetConnection);
            
            // reuse the old dataset connection ID for the matching connection name
            // otherwise we get constraint violations from database
            String datasetConnectionName = datasetConnection.getConnectionName();
            if (datasetTargetIds.containsKey(datasetConnectionName)) {
                datasetConnection.setId(datasetTargetIds.get(datasetConnectionName));
            }
        }
        for (ModuleTypeVersionParameter parameter : submoduleTypeVersion.getModuleTypeVersionParameters().values()) {
            if (parameter.getMode().equals(ModuleTypeVersionParameter.OUT))
                continue;
            ModuleTypeVersionParameterConnection parameterConnection = new ModuleTypeVersionParameterConnection();
            parameterConnection.setTargetSubmodule(moduleTypeVersionSubmodule);
            parameterConnection.setTargetParameterName(parameter.getParameterName());
            addModuleTypeVersionParameterConnection(parameterConnection);
            
            // reuse the old parameter connection ID for the matching connection name
            // otherwise we get constraint violations from database
            String parameterConnectionName = parameterConnection.getConnectionName();
            if (parameterTargetIds.containsKey(parameterConnectionName)) {
                parameterConnection.setId(parameterTargetIds.get(parameterConnectionName));
            }
        }
    }

    public void removeModuleTypeVersionSubmodule(ModuleTypeVersionSubmodule moduleTypeVersionSubmodule) {
        removeModuleTypeVersionSubmodule(moduleTypeVersionSubmodule.getName());
    }

    public void removeModuleTypeVersionSubmodule(String name) {
        this.moduleTypeVersionSubmodules.remove(name);
        
        // if the removed submodule is the target of a connection, remove the connection
        // if the removed submodule is the source of a connection, set the connection source to null
        List<String> deletedTargetNames = new ArrayList<String>();
        for (ModuleTypeVersionDatasetConnection datasetConnection : moduleTypeVersionDatasetConnections.values()) {
            if ((datasetConnection.getTargetSubmodule() != null) && datasetConnection.getTargetSubmodule().getName().equals(name)) {
                deletedTargetNames.add(datasetConnection.getTargetName());
            } else if ((datasetConnection.getSourceSubmodule() != null) && datasetConnection.getSourceSubmodule().getName().equals(name)) {
                datasetConnection.setSourceSubmodule(null);
                datasetConnection.setSourcePlaceholderName(null);
            }
        }
        for (String targetName : deletedTargetNames) {
            moduleTypeVersionDatasetConnections.remove(targetName);
        }
        
        // if the removed submodule is the target of a connection, remove the connection
        // if the removed submodule is the source of a connection, set the connection source to null
        deletedTargetNames.clear();
        for (ModuleTypeVersionParameterConnection parameterConnection : moduleTypeVersionParameterConnections.values()) {
            if ((parameterConnection.getTargetSubmodule() != null) && parameterConnection.getTargetSubmodule().getName().equals(name)) {
                deletedTargetNames.add(parameterConnection.getTargetName());
            } else if ((parameterConnection.getSourceSubmodule() != null) && parameterConnection.getSourceSubmodule().getName().equals(name)) {
                parameterConnection.setSourceSubmodule(null);
                parameterConnection.setSourceParameterName(null);
            }
        }
        for (String targetName : deletedTargetNames) {
            moduleTypeVersionParameterConnections.remove(targetName);
        }
    }

    // moduleTypeVersionDatasetConnections

    public Map<String, ModuleTypeVersionDatasetConnection> getModuleTypeVersionDatasetConnections() {
        return this.moduleTypeVersionDatasetConnections;
    }

    public boolean containsDatasetConnectionId(int datasetConnectionId) {
        for(ModuleTypeVersionDatasetConnection moduleTypeVersionDatasetConnection : moduleTypeVersionDatasetConnections.values()) {
            if (moduleTypeVersionDatasetConnection.getId() == datasetConnectionId) {
                return true;
            }
        }
        return false;
    }
    
    public void setModuleTypeVersionDatasetConnections(Map<String, ModuleTypeVersionDatasetConnection> moduleTypeVersionDatasetConnections) {
        this.moduleTypeVersionDatasetConnections = moduleTypeVersionDatasetConnections;
    }

    private void addModuleTypeVersionDatasetConnection(ModuleTypeVersionDatasetConnection moduleTypeVersionDatasetConnection) {
        moduleTypeVersionDatasetConnection.setCompositeModuleTypeVersion(this);
        this.moduleTypeVersionDatasetConnections.put(moduleTypeVersionDatasetConnection.getTargetName(), moduleTypeVersionDatasetConnection);
    }

    public void removeModuleTypeVersionDatasetConnection(String targetName) {
        this.moduleTypeVersionDatasetConnections.remove(targetName);
    }

    public void clearModuleTypeVersionDatasetConnections(String placeholderName) {
        // clear all connections to/from this placeholder
        for (ModuleTypeVersionDatasetConnection datasetConnection : moduleTypeVersionDatasetConnections.values()) {
            if ((datasetConnection.getSourceSubmodule() == null) && placeholderName.equals(datasetConnection.getSourcePlaceholderName())) {
                datasetConnection.setSourcePlaceholderName(null);
            } else if ((datasetConnection.getTargetSubmodule() == null) && placeholderName.equals(datasetConnection.getTargetPlaceholderName())) {
                datasetConnection.setSourceSubmodule(null);
                datasetConnection.setSourcePlaceholderName(null);
            }
        }
    }

    // moduleTypeVersionParameterConnections

    public Map<String, ModuleTypeVersionParameterConnection> getModuleTypeVersionParameterConnections() {
        return this.moduleTypeVersionParameterConnections;
    }

    public boolean containsParameterConnectionId(int parameterConnectionId) {
        for(ModuleTypeVersionParameterConnection moduleTypeVersionParameterConnection : moduleTypeVersionParameterConnections.values()) {
            if (moduleTypeVersionParameterConnection.getId() == parameterConnectionId) {
                return true;
            }
        }
        return false;
    }

    public void setModuleTypeVersionParameterConnections(Map<String, ModuleTypeVersionParameterConnection> moduleTypeVersionParameterConnections) {
        this.moduleTypeVersionParameterConnections = moduleTypeVersionParameterConnections;
    }

    private void addModuleTypeVersionParameterConnection(ModuleTypeVersionParameterConnection moduleTypeVersionParameterConnection) {
        moduleTypeVersionParameterConnection.setCompositeModuleTypeVersion(this);
        this.moduleTypeVersionParameterConnections.put(moduleTypeVersionParameterConnection.getTargetName(), moduleTypeVersionParameterConnection);
    }

    public void removeModuleTypeVersionParameterConnection(String targetName) {
        this.moduleTypeVersionParameterConnections.remove(targetName);
    }
    
    public void clearModuleTypeVersionParameterConnections(String parameterName) {
        // clear all connections to/from this parameter
        for (ModuleTypeVersionParameterConnection parameterConnection : moduleTypeVersionParameterConnections.values()) {
            if ((parameterConnection.getSourceSubmodule() == null) && parameterName.equals(parameterConnection.getSourceParameterName())) {
                parameterConnection.setSourceParameterName(null);
            } else if ((parameterConnection.getTargetSubmodule() == null) && parameterName.equals(parameterConnection.getTargetParameterName())) {
                parameterConnection.setSourceSubmodule(null);
                parameterConnection.setSourceParameterName(null);
            }
        }
    }

    // all connections
    
    public boolean updateConnections(final StringBuilder cleanupScriptBuilder) {
        boolean updated = false;
        cleanupScriptBuilder.setLength(0);
        
        Map<String, ModuleTypeVersionDatasetConnectionEndpoint> newDatasetTargets = getTargetDatasetEndpoints();
        
        Set<String> oldDatasetTargetNames = new HashSet<String>();
        oldDatasetTargetNames.addAll(moduleTypeVersionDatasetConnections.keySet());

        // handle modified or removed dataset connections
        StringBuilder ids = new StringBuilder(); 
        for(String oldDatasetTargetName : oldDatasetTargetNames) {
            ModuleTypeVersionDatasetConnection datasetConnection = moduleTypeVersionDatasetConnections.get(oldDatasetTargetName);
            if (newDatasetTargets.containsKey(oldDatasetTargetName)) {
                Map<String, ModuleTypeVersionDatasetConnectionEndpoint> newDatasetSources = getSourceDatasetEndpoints(datasetConnection);
                if (!newDatasetSources.containsKey(datasetConnection.getSourceName())) { // modified dataset connection
                    datasetConnection.setSourceSubmodule(null);
                    datasetConnection.setSourcePlaceholderName(null);
                    updated = true;
                }
            } else { // removed dataset connection
                removeModuleTypeVersionDatasetConnection(oldDatasetTargetName);
                updated = true;
                if (ids.length() > 0)
                    ids.append(",");
                ids.append(datasetConnection.getId());
            }
        }
        if (ids.length() > 0) {
            cleanupScriptBuilder.append("DELETE FROM modules.module_types_versions_connections_datasets WHERE id IN (" + ids.toString() + ");\n");
        }
        // handle new dataset connections
        for(String newDatasetTargetName : newDatasetTargets.keySet()) {
            if (!moduleTypeVersionDatasetConnections.containsKey(newDatasetTargetName)) {
                ModuleTypeVersionDatasetConnectionEndpoint newDatasetTarget = newDatasetTargets.get(newDatasetTargetName);
                ModuleTypeVersionDatasetConnection newDatasetConnection = new ModuleTypeVersionDatasetConnection();
                newDatasetConnection.setTargetSubmodule(newDatasetTarget.getSubmodule());
                newDatasetConnection.setTargetPlaceholderName(newDatasetTarget.getPlaceholderName());
                addModuleTypeVersionDatasetConnection(newDatasetConnection);
                updated = true;
            }
        }
        
        Map<String, ModuleTypeVersionParameterConnectionEndpoint> newParameterTargets = getTargetParameterEndpoints();
        
        Set<String> oldParameterTargetNames = new HashSet<String>();
        oldParameterTargetNames.addAll(moduleTypeVersionParameterConnections.keySet());

        // handle modified or removed parameter connections
        ids.setLength(0);
        for(String oldParameterTargetName : oldParameterTargetNames) {
            ModuleTypeVersionParameterConnection parameterConnection = moduleTypeVersionParameterConnections.get(oldParameterTargetName);
            if (newParameterTargets.containsKey(oldParameterTargetName)) {
                Map<String, ModuleTypeVersionParameterConnectionEndpoint> newParameterSources = getSourceParameterEndpoints(parameterConnection);
                if (!newParameterSources.containsKey(parameterConnection.getSourceName())) { // modified parameter connection
                    parameterConnection.setSourceSubmodule(null);
                    parameterConnection.setSourceParameterName(null);
                    updated = true;
                }
            } else { // removed parameter connection
                removeModuleTypeVersionParameterConnection(oldParameterTargetName);
                updated = true;
                if (ids.length() > 0)
                    ids.append(",");
                ids.append(parameterConnection.getId());
            }
        }
        if (ids.length() > 0) {
            cleanupScriptBuilder.append("DELETE FROM modules.module_types_versions_connections_parameters WHERE id IN (" + ids.toString() + ");\n");
        }
        // handle new parameter connections
        for(String newParameterTargetName : newParameterTargets.keySet()) {
            if (!moduleTypeVersionParameterConnections.containsKey(newParameterTargetName)) {
                ModuleTypeVersionParameterConnectionEndpoint newParameterTarget = newParameterTargets.get(newParameterTargetName);
                ModuleTypeVersionParameterConnection newParameterConnection = new ModuleTypeVersionParameterConnection();
                newParameterConnection.setTargetSubmodule(newParameterTarget.getSubmodule());
                newParameterConnection.setTargetParameterName(newParameterTarget.getParameterName());
                addModuleTypeVersionParameterConnection(newParameterConnection);
                updated = true;
            }
        }
        
        return updated;
    }

    // standard methods

    public String toString() {
        return getName();
    }

    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ModuleTypeVersion)) return false;
        return ((ModuleTypeVersion) other).getId() == id;
    }

    public int compareTo(ModuleTypeVersion o) {
        if (this == o) return 0;
        return new Integer(id).compareTo(o.getId());
    }
}
