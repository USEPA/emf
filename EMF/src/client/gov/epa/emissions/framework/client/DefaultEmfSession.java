package gov.epa.emissions.framework.client;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.security.EMFCipher;
import gov.epa.emissions.commons.security.SecurityConstants;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.preference.DefaultUserPreferences;
import gov.epa.emissions.framework.client.preference.UserPreference;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.LoggingService;
import gov.epa.emissions.framework.services.basic.UserService;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.cost.ControlMeasureService;
import gov.epa.emissions.framework.services.cost.ControlProgramService;
import gov.epa.emissions.framework.services.cost.ControlStrategyService;
import gov.epa.emissions.framework.services.cost.controlmeasure.ControlMeasureExportService;
import gov.epa.emissions.framework.services.cost.controlmeasure.ControlMeasureImportService;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.editor.DataEditorService;
import gov.epa.emissions.framework.services.editor.DataViewService;
import gov.epa.emissions.framework.services.exim.ExImService;
import gov.epa.emissions.framework.services.fast.FastService;
import gov.epa.emissions.framework.services.module.LiteModule;
import gov.epa.emissions.framework.services.module.ModuleService;
import gov.epa.emissions.framework.services.module.ParameterType;
import gov.epa.emissions.framework.services.qa.QAService;
import gov.epa.emissions.framework.services.sms.SectorScenarioService;
import gov.epa.emissions.framework.services.tempalloc.TemporalAllocationService;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

public class DefaultEmfSession implements EmfSession {

    private User user;

    private ServiceLocator serviceLocator;

    private String mostRecentExportFolder;

    private UserPreference preferences;
    
    private CaseService caseService;
    
    private PublicKey publicKey;
    
    private byte[] encryptedPassword;
    
    Cache<ObjectCacheType, Object> objectCache;
    
    public Cache<ObjectCacheType, Object> getObjectCache() {
        return objectCache;
    }

    public enum ObjectCacheType {
        LIGHT_DATASET_TYPES_LIST, PROJECTS_LIST, PARAMETER_TYPES_LIST, LITE_MODULES_LIST
    }
    
    public DefaultEmfSession(final User user, ServiceLocator locator) throws EmfException {
        serviceLocator = locator;
        locator.setEmfSession(this);
        this.preferences = new DefaultUserPreferences();
        this.user = user;
        
        objectCache = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build(
                new CacheLoader<ObjectCacheType, Object>() {
                    public Object load(ObjectCacheType key) throws EmfException {
                        if (key.equals(ObjectCacheType.LIGHT_DATASET_TYPES_LIST)) {
                            System.out.println("loading client-side object cache -- LIGHT_DATASET_TYPES_LIST");
                            return serviceLocator.dataCommonsService().getLightDatasetTypes(user.getId());
                        } else if (key.equals(ObjectCacheType.PROJECTS_LIST)) {
                            System.out.println("loading client-side object cache -- PROJECTS_LIST");
                            return serviceLocator.dataCommonsService().getProjects();
                        } else if (key.equals(ObjectCacheType.PARAMETER_TYPES_LIST)) {
                            // TODO use a different object cache for this list (5 minutes is to short)
                            System.out.println("loading client-side object cache -- PARAMETER_TYPES_LIST");
                            ParameterType[] parameterTypes = serviceLocator.moduleService().getParameterTypes();
                            TreeMap<String, ParameterType> parameterTypesMap = new TreeMap<String, ParameterType>();
                            for (ParameterType parameterType : parameterTypes) {
                                parameterTypesMap.put(parameterType.getSqlType(), parameterType);
                            }
                            return parameterTypesMap;
                        } else if (key.equals(ObjectCacheType.LITE_MODULES_LIST)) {
                            System.out.println("loading client-side object cache -- LITE_MODULES_LIST");
                            LiteModule[] liteModules = serviceLocator.moduleService().getLiteModules();
                            ConcurrentSkipListMap<Integer, LiteModule> liteModulesMap = new ConcurrentSkipListMap<Integer, LiteModule>();
                            for (LiteModule liteModule : liteModules) {
                                liteModulesMap.put(liteModule.getId(), liteModule);
                            }
                            return liteModulesMap;
                        }
                        return null;
                    }
                  });
    }

    public UserPreference preferences() {
        return preferences;
    }

    public ServiceLocator serviceLocator() {
        return serviceLocator;
    }

    public User user() {
        return user;
    }

    public ExImService eximService() {
        return serviceLocator.eximService();
    }

    public DataService dataService() {
        return serviceLocator.dataService();
    }

    public String getMostRecentExportFolder() {
        return mostRecentExportFolder;
    }

    public void setMostRecentExportFolder(String mostRecentExportFolder) {
        this.mostRecentExportFolder = mostRecentExportFolder;
    }

    public UserService userService() {
        return serviceLocator.userService();
    }

    public LoggingService loggingService() {
        return serviceLocator.loggingService();
    }

    public DataCommonsService dataCommonsService() {
        return serviceLocator.dataCommonsService();
    }

    public DataViewService dataViewService() {
        return serviceLocator.dataViewService();
    }

    public DataEditorService dataEditorService() {
        return serviceLocator.dataEditorService();
    }

    public QAService qaService() {
        return serviceLocator.qaService();
    }

    public ModuleService moduleService() {
        return serviceLocator.moduleService();
    }

    public CaseService caseService() {
        if (caseService == null)
            caseService = serviceLocator.caseService();
        
        return caseService;
    }

    public ControlMeasureService controlMeasureService() {
        return serviceLocator.controlMeasureService();
    }

    public ControlStrategyService controlStrategyService() {
        return serviceLocator.controlStrategyService();
    }

    public ControlMeasureImportService controlMeasureImportService() {
        return serviceLocator.controlMeasureImportService();
    }

    public ControlMeasureExportService controlMeasureExportService() {
        return serviceLocator.controlMeasureExportService();
    }

    public ControlProgramService controlProgramService() {
        return serviceLocator.controlProgramService();
    }

    public SectorScenarioService sectorScenarioService() {
        return serviceLocator.sectorScenarioService();
    }

    public FastService fastService() {
        return serviceLocator.fastService();
    }
    
    public TemporalAllocationService temporalAllocationService() {
        return serviceLocator.temporalAllocationService();
    }
    
    public void setPublicKey(PublicKey pk) {
        this.publicKey = pk;
    }
    
    public void setPublicKey(byte[] encodedPK) throws EmfException {
        X509EncodedKeySpec x509KeySpec = null;
        
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(SecurityConstants.ENCRYPTION_ALGORITHM);
            x509KeySpec = new X509EncodedKeySpec(encodedPK);
            this.publicKey = keyFactory.generatePublic(x509KeySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new EmfException("Encryption algorithm not supported: " + SecurityConstants.ENCRYPTION_ALGORITHM);
        } catch (InvalidKeySpecException e) {
            throw new EmfException("Invalid key specification: " 
                    + (x509KeySpec == null ? X509EncodedKeySpec.class.getSimpleName() : x509KeySpec.getFormat()));
        }
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public byte[] getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(char[] password) throws EmfException {
        try {
            EMFCipher cipher = new EMFCipher();
            encryptedPassword = (publicKey == null) ? null : cipher.encrypt(publicKey, password);
        } catch (InvalidKeyException e) {
            throw new EmfException("Invalid Key");
        } catch (NoSuchAlgorithmException e) {
            throw new EmfException("Encryption algorithm is invalid");
        } catch (NoSuchPaddingException e) {
            throw new EmfException("Encryption padding is invalid");
        } catch (IllegalBlockSizeException e) {
            throw new EmfException("Encryption used an invalid block size");
        } catch (BadPaddingException e) {
            throw new EmfException("Encryption padding is invalid");
        }
    }

    @Override
    public DatasetType[] getLightDatasetTypes() {
        try {
            return (DatasetType[]) objectCache.get(ObjectCacheType.LIGHT_DATASET_TYPES_LIST);
        } catch (ExecutionException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        return new DatasetType[] {};
    }

    @Override
    public DatasetType getLightDatasetType(String name) {
        for (DatasetType datasetType : getLightDatasetTypes()) {
            if (name.equals(datasetType.getName()))
                return datasetType;
        }
        return null;
    }

    @Override
    public Project[] getProjects() {
        try {
            return (Project[]) objectCache.get(ObjectCacheType.PROJECTS_LIST);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return new Project[] {};
    }

    @SuppressWarnings("unchecked")
    @Override
    public TreeMap<String, ParameterType> getParameterTypes() {
        try {
            return (TreeMap<String, ParameterType>) objectCache.get(ObjectCacheType.PARAMETER_TYPES_LIST);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return new TreeMap<String, ParameterType>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ConcurrentSkipListMap<Integer, LiteModule> getLiteModules() {
        try {
            return (ConcurrentSkipListMap<Integer, LiteModule>) objectCache.get(ObjectCacheType.LITE_MODULES_LIST);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return new ConcurrentSkipListMap<Integer, LiteModule>();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ConcurrentSkipListMap<Integer, LiteModule> getFreshLiteModules() {
        objectCache.invalidate(ObjectCacheType.LITE_MODULES_LIST);
        return getLiteModules();
    }
}
