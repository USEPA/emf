package gov.epa.emissions.framework.services.module;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
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
        //            gov.epa.emissions.framework.services.module.ModuleRunnerTask
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
            String match = matcher.group().replaceAll("\\{\\s*", "{").replaceAll("\\s*\\.\\s*", ".").replaceAll("\\s*\\}", "}").toLowerCase();
            if (!validPlaceholders.contains(match)) {
                error.append(String.format("Unrecognized placeholder %s at location %d.", match, start));
                return false;
            }
        }
        
        validPlaceholders.clear();
        for(ModuleTypeVersionParameter moduleTypeVersionParameter : moduleTypeVersionParameters.values()) {
            String parameter = moduleTypeVersionParameter.getParameterName().toLowerCase();
            validPlaceholders.add("#{" + parameter + "}");
            validPlaceholders.add("#{" + parameter + ".sql_type}");
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
            String match = matcher.group().replaceAll("\\{\\s*", "{").replaceAll("\\s*\\.\\s*", ".").replaceAll("\\s*\\}", "}").toLowerCase();
            if (!validPlaceholders.contains(match)) {
                error.append(String.format("Unrecognized parameter placeholder %s at location %d.", match, start));
                return false;
            }
        }
        
        return true;
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
    
    public Map<String, ModuleTypeVersionDatasetConnectionEndpoint> getSourceDatasetEndpoints(ModuleTypeVersionDatasetConnection datasetConnection) {
        String datasetTypeName = datasetConnection.getDatasetTypeName();
        Map<String, ModuleTypeVersionDatasetConnectionEndpoint> endpoints = new HashMap<String, ModuleTypeVersionDatasetConnectionEndpoint>();
        for (ModuleTypeVersionDataset dataset : moduleTypeVersionDatasets.values()) {
            if (dataset.getDatasetType().getName().equals(datasetTypeName) && !dataset.getMode().equals(ModuleTypeVersionDataset.OUT)) {
                ModuleTypeVersionDatasetConnectionEndpoint endpoint = new ModuleTypeVersionDatasetConnectionEndpoint(this, null, dataset.getPlaceholderName());
                endpoints.put(endpoint.getEndpointName(), endpoint);
            }
        }
        for (ModuleTypeVersionSubmodule submodule : moduleTypeVersionSubmodules.values()) {
            if (submodule.equals(datasetConnection.getTargetSubmodule())) continue;
            for (ModuleTypeVersionDataset dataset : submodule.getModuleTypeVersion().getModuleTypeVersionDatasets().values()) {
                if (dataset.getDatasetType().getName().equals(datasetTypeName) && !dataset.getMode().equals(ModuleTypeVersionDataset.IN)) {
                    ModuleTypeVersionDatasetConnectionEndpoint endpoint = new ModuleTypeVersionDatasetConnectionEndpoint(this, submodule, dataset.getPlaceholderName());
                    endpoints.put(endpoint.getEndpointName(), endpoint);
                }
            }
        }
        
        return endpoints;
    }
    
    public Map<String, ModuleTypeVersionParameterConnectionEndpoint> getSourceParameterEndpoints(ModuleTypeVersionParameterConnection parameterConnection) {
        String sqlType = parameterConnection.getSqlType();
        Map<String, ModuleTypeVersionParameterConnectionEndpoint> endpoints = new HashMap<String, ModuleTypeVersionParameterConnectionEndpoint>();
        for (ModuleTypeVersionParameter parameter : moduleTypeVersionParameters.values()) {
            if (parameter.getSqlParameterType().equals(sqlType) && !parameter.getMode().equals(ModuleTypeVersionParameter.OUT)) {
                ModuleTypeVersionParameterConnectionEndpoint endpoint = new ModuleTypeVersionParameterConnectionEndpoint(this, null, parameter.getParameterName());
                endpoints.put(endpoint.getEndpointName(), endpoint);
            }
        }
        for (ModuleTypeVersionSubmodule submodule : moduleTypeVersionSubmodules.values()) {
            if (submodule.equals(parameterConnection.getTargetSubmodule())) continue;
            for (ModuleTypeVersionParameter parameter : submodule.getModuleTypeVersion().getModuleTypeVersionParameters().values()) {
                if (parameter.getSqlParameterType().equals(sqlType) && !parameter.getMode().equals(ModuleTypeVersionParameter.IN)) {
                    ModuleTypeVersionParameterConnectionEndpoint endpoint = new ModuleTypeVersionParameterConnectionEndpoint(this, submodule, parameter.getParameterName());
                    endpoints.put(endpoint.getEndpointName(), endpoint);
                }
            }
        }
        
        return endpoints;
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

    public String versionAndName() {
        return version + " - " + name;
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

    public void setModuleTypeVersionSubmodules(Map<String, ModuleTypeVersionSubmodule> moduleTypeVersionSubmodules) {
        this.moduleTypeVersionSubmodules = moduleTypeVersionSubmodules;
    }

    public void addModuleTypeVersionSubmodule(ModuleTypeVersionSubmodule moduleTypeVersionSubmodule) {
        if (this.moduleTypeVersionSubmodules.containsKey(moduleTypeVersionSubmodule.getName())) {
            removeModuleTypeVersionSubmodule(moduleTypeVersionSubmodule.getName());
        }
        moduleTypeVersionSubmodule.setCompositeModuleTypeVersion(this);
        this.moduleTypeVersionSubmodules.put(moduleTypeVersionSubmodule.getName(), moduleTypeVersionSubmodule);
        
        // add connections for the new internal targets (IN/INOUT datasets and parameters)
        ModuleTypeVersion submoduleTypeVersion = moduleTypeVersionSubmodule.getModuleTypeVersion();
        for (ModuleTypeVersionDataset dataset : submoduleTypeVersion.getModuleTypeVersionDatasets().values()) {
            if (dataset.getMode().equals(ModuleTypeVersionDataset.OUT))
                continue;
            ModuleTypeVersionDatasetConnection datasetConnection = new ModuleTypeVersionDatasetConnection();
            datasetConnection.setTargetSubmodule(moduleTypeVersionSubmodule);
            datasetConnection.setTargetPlaceholderName(dataset.getPlaceholderName());
            addModuleTypeVersionDatasetConnection(datasetConnection);
        }
        for (ModuleTypeVersionParameter parameter : submoduleTypeVersion.getModuleTypeVersionParameters().values()) {
            if (parameter.getMode().equals(ModuleTypeVersionParameter.OUT))
                continue;
            ModuleTypeVersionParameterConnection parameterConnection = new ModuleTypeVersionParameterConnection();
            parameterConnection.setTargetSubmodule(moduleTypeVersionSubmodule);
            parameterConnection.setTargetParameterName(parameter.getParameterName());
            addModuleTypeVersionParameterConnection(parameterConnection);
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
    
    // moduleTypeVersionParameterConnections

    public Map<String, ModuleTypeVersionParameterConnection> getModuleTypeVersionParameterConnections() {
        return this.moduleTypeVersionParameterConnections;
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
