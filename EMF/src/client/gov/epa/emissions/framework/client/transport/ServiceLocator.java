package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.basic.LoggingService;
import gov.epa.emissions.framework.services.basic.UserService;
import gov.epa.emissions.framework.services.casemanagement.CaseAssistService;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.cost.ControlMeasureService;
import gov.epa.emissions.framework.services.cost.ControlProgramService;
import gov.epa.emissions.framework.services.cost.ControlStrategyService;
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

public interface ServiceLocator {
    
    UserService userService();

    ExImService eximService();

    DataService dataService();

    LoggingService loggingService();

    DataCommonsService dataCommonsService();

    DataEditorService dataEditorService();

    DataViewService dataViewService();

    QAService qaService();

    ModuleService moduleService();

    CaseService caseService();
    
    CaseAssistService caseAssistService();

	ControlMeasureService controlMeasureService();
	    
    ControlStrategyService controlStrategyService();

    ControlProgramService controlProgramService();

    ControlMeasureImportService controlMeasureImportService();

    ControlMeasureExportService controlMeasureExportService();

    SectorScenarioService sectorScenarioService();

    FastService fastService();
    
    TemporalAllocationService temporalAllocationService();

    void setEmfSession(EmfSession emfSession);

    String getBaseUrl();
}

