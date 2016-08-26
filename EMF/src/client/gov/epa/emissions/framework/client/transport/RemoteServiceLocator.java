package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.LoggingService;
import gov.epa.emissions.framework.services.basic.UserService;
import gov.epa.emissions.framework.services.casemanagement.CaseAssistService;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.cost.ControlProgramService;
import gov.epa.emissions.framework.services.cost.ControlStrategyService;
import gov.epa.emissions.framework.services.cost.ControlMeasureService;
import gov.epa.emissions.framework.services.sms.SectorScenarioService;
import gov.epa.emissions.framework.services.tempalloc.TemporalAllocationService;
import gov.epa.emissions.framework.services.cost.controlmeasure.ControlMeasureExportService;
import gov.epa.emissions.framework.services.cost.controlmeasure.ControlMeasureImportService;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.editor.DataEditorService;
import gov.epa.emissions.framework.services.editor.DataViewService;
import gov.epa.emissions.framework.services.exim.ExImService;
import gov.epa.emissions.framework.services.fast.FastService;
import gov.epa.emissions.framework.services.module.ModuleService;
import gov.epa.emissions.framework.services.qa.QAService;

public class RemoteServiceLocator implements ServiceLocator {
    private String baseUrl;

    // Note: Each session-based service needs to create it's own Call object
    private EmfCall viewCall;

    private EmfCall editCall;

    private EmfCall eximCall;
    
    private CaseService caseService;
    
    private QAService qaService;
    
    private ModuleService moduleService;
    
    private UserService userService;
    
    private LoggingService loggingService;
    
    private DataService dataService;
    
    private DataCommonsService dataCommonsService;
    
    private ControlMeasureService controlMeasureService;
    
    private ControlStrategyService controlStrategyService;
    
    private ControlProgramService controlProgramService;
    
    private ControlMeasureImportService controlMeasureImportService;
    
    private ControlMeasureExportService controlMeasureExportService;
    
    private SectorScenarioService sectorScenarioService;
    
    private FastService fastService;
    
    private TemporalAllocationService temporalAllocationService;
    
    private EmfSession emfSession;
    
    public RemoteServiceLocator(String baseUrl) throws Exception {
        this.baseUrl = baseUrl;
        editCall = this.createSessionEnabledCall("DataEditor Service", baseUrl
                + "/gov.epa.emf.services.editor.DataEditorService");
        viewCall = this.createSessionEnabledCall("DataView Service", baseUrl
                + "/gov.epa.emf.services.editor.DataViewService");
        eximCall = this.createSessionEnabledCall("ExIm Service", baseUrl + "/gov.epa.emf.services.exim.ExImService");
    }

    public UserService userService() {
        if (userService == null)
            userService = new UserServiceTransport(baseUrl + "/gov.epa.emf.services.basic.UserService");
        
        return userService;
    }

    public ExImService eximService() {
        return new ExImServiceTransport(eximCall);
    }

    public DataService dataService() {
        if (dataService == null)
            dataService = new DataServiceTransport(baseUrl + "/gov.epa.emf.services.data.DataService");
        
        return dataService;
    }

    public LoggingService loggingService() {
        if (loggingService == null)
            loggingService = new LoggingServiceTransport(baseUrl + "/gov.epa.emf.services.basic.LoggingService");
        
        return loggingService;
    }

    public QAService qaService() {
        if (qaService == null)
            qaService = new QAServiceTransport(baseUrl + "/gov.epa.emf.services.qa.QAService");
        
        return qaService;
    }

    public ModuleService moduleService() {
        if (moduleService == null)
            moduleService = new ModuleServiceTransport(baseUrl + "/gov.epa.emf.services.module.ModuleService",
                                                       emfSession);
        
        return moduleService;
    }

    public DataCommonsService dataCommonsService() {
        if (dataCommonsService == null)
            dataCommonsService = new DataCommonsServiceTransport(baseUrl + "/gov.epa.emf.services.data.DataCommonsService",
                                                                 emfSession);
        
        return dataCommonsService;
    }

    public DataEditorService dataEditorService() {
        return new DataEditorServiceTransport(editCall);
    }

    public DataViewService dataViewService() {
        return new DataViewServiceTransport(viewCall);
    }

    public CaseService caseService() {
        if (caseService == null)
            caseService = new CaseServiceTransport(baseUrl + "/gov.epa.emissions.framework.services.casemanagement.CaseService");
        
        return caseService;
    }

    public CaseAssistService caseAssistService() {
       return new CaseAssistServiceTransport(baseUrl + "/gov.epa.emissions.framework.services.casemanagement.CaseAssistService");
    }
    
    public ControlMeasureService controlMeasureService() {
        if (controlMeasureService == null)
            controlMeasureService = new ControlMeasureServiceTransport(baseUrl + "/gov.epa.emf.services.cost.ControlMeasureService");
        
        return controlMeasureService;
    }
    
    public ControlStrategyService controlStrategyService() {
        if (controlStrategyService == null)
            controlStrategyService = new ControlStrategyServiceTransport(baseUrl + "/gov.epa.emissions.framework.services.cost.ControlStrategyService");
        
        return controlStrategyService;
    }
    
    public ControlProgramService controlProgramService() {
        if (controlProgramService == null)
            controlProgramService = new ControlProgramServiceTransport(baseUrl + "/gov.epa.emissions.framework.services.cost.ControlProgramService");
        
        return controlProgramService;
    }

    public ControlMeasureImportService controlMeasureImportService() {
        if (controlMeasureImportService == null)
            controlMeasureImportService = new ControlMeasureImportServiceTransport(baseUrl + "/gov.epa.emf.services.cost.controlmeasure.ControlMeasureImportService");
        
        return controlMeasureImportService;
    }

    public ControlMeasureExportService controlMeasureExportService() {
        if (controlMeasureExportService == null)
            controlMeasureExportService = new ControlMeasureExportServiceTransport(baseUrl + "/gov.epa.emf.services.cost.controlmeasure.ControlMeasureExportService");
        
        return controlMeasureExportService;
    }
    
    public SectorScenarioService sectorScenarioService() {
        if (sectorScenarioService == null)
            sectorScenarioService = new SectorScenarioServiceTransport(baseUrl + "/gov.epa.emf.services.sms.SectorScenarioService");
        
        return sectorScenarioService;
    }
    
    public FastService fastService() {
        if (fastService == null)
            fastService = new FastServiceTransport(baseUrl + "/gov.epa.emf.services.fast.FastService");
        
        return fastService;
    }
    
    public TemporalAllocationService temporalAllocationService() {
        if (temporalAllocationService == null)
            temporalAllocationService = new TemporalAllocationServiceTransport(baseUrl + "/gov.epa.emissions.framework.services.tempalloc.TemporalAllocationService");
        
        return temporalAllocationService;
    }
    
    /*
     * Create a singleton reference to the Axis Service Call object This call object will be universal to all client
     * transport objects and will be passed in to via the transport object's constructor
     * 
     */
    private EmfCall createSessionEnabledCall(String service, String url) throws EmfException {
        CallFactory callFactory = new CallFactory(url);
        return callFactory.createSessionEnabledCall(service);
    }


    @Override
    public void setEmfSession(EmfSession emfSession) {
        this.emfSession = emfSession;
    }
}
