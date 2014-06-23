package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.cost.controlmeasure.io.CMExportPresenter;
import gov.epa.emissions.framework.client.cost.controlmeasure.io.CMExportView;
import gov.epa.emissions.framework.client.cost.controlmeasure.io.CMExportWindow;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureService;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.ui.RefreshObserver;

public class ControlMeasuresManagerPresenter implements RefreshObserver {

    private ControlMeasuresManagerView view;

    private EmfSession session;
    private CostYearTable costYearTable;

    public ControlMeasuresManagerPresenter(EmfSession session) {
        this.session = session;
        try {
            this.costYearTable = populateCostYearTable();
        } catch (EmfException e) {
            e.printStackTrace();
        }
    }

    public void doDisplay(ControlMeasuresManagerView view) throws EmfException {
        this.view = view;
        view.observe(this);

        view.display(new ControlMeasure[0]);
    }

    public void doClose() {
        view.disposeView();
    }

    public void doRefresh() throws EmfException {
        view.clearMessage();
        view.refresh(service().getSummaryControlMeasures(""));
    }

    private ControlMeasureService service() {
        return session.controlMeasureService();
    }
    
//View control measures
    
   public void doView(EmfConsole parent, ControlMeasure measure, DesktopManager desktopManager) throws EmfException {
        
        ControlMeasureView editor = new ViewControlMeasureWindow(parent, session, desktopManager, costYearTable);
        ControlMeasurePresenter presenter = new ViewControlMeasurePresenterImpl(
                measure, editor, session, this);
        presenter.doDisplay();
    }
    
    
    public void doEdit(EmfConsole parent, ControlMeasure measure, DesktopManager desktopManager) throws EmfException {
        
        ControlMeasureView editor = new EditControlMeasureWindow(parent, session, desktopManager, costYearTable);
        ControlMeasurePresenter presenter = new EditorControlMeasurePresenterImpl(
                measure, editor, session, this);
        presenter.doDisplay();
    }

    public void doCreateNew(EmfConsole parent, ControlMeasure measure, DesktopManager desktopManager) throws EmfException {
        ControlMeasureView window = new NewControlMeasureWindow(parent, session, desktopManager, costYearTable);
        ControlMeasurePresenter presenter = new NewControlMeasurePresenterImpl(
                measure, window, session, this);
        presenter.doDisplay();
    }
    
    public ControlMeasure[] doFilterEfficiencyAndCost(ControlMeasure[] measures) {
        //List filteredMeasures = new ArrayList();
        
        for(int i = 0; i < measures.length; i++) {
           // me
        }
        
        return null;
    }

    public void doExport(ControlMeasure[] measures, DesktopManager desktopManager, int totalMeasuers, EmfConsole parentConsole, boolean bySector) {
        CMExportView exportView = new CMExportWindow(measures, desktopManager, totalMeasuers, session, parentConsole, bySector);
        CMExportPresenter exportPresenter = new CMExportPresenter(session);
        exportPresenter.display(exportView);
    }

    public void doSaveCopiedControlMeasure(int controlMeasureId) throws EmfException {
        service().copyMeasure(controlMeasureId, session.user());
    }

    public Scc[] getSCCs(int controlMeasureId) throws EmfException {
        return service().getSccs(controlMeasureId);
    }

    public ControlMeasure[] getControlMeasures(Pollutant pollutant, boolean getDetails, String nameContains) throws EmfException {
        
        ControlMeasure[] controlMeasures = new ControlMeasure[0];

        if (!pollutant.getName().equalsIgnoreCase("Select one")) {

            StringBuilder whereFilteSB = new StringBuilder();
            if (nameContains != null && nameContains.length() > 0) {

                String escapedNameContains = getPattern(nameContains.toLowerCase().trim());
                whereFilteSB.append(" lower(cm.name) like ").append(escapedNameContains).append(" ");
            }

            String whereFilterString = whereFilteSB.toString();
            if (pollutant.getName().equals("ALL")) {
                controlMeasures = (getDetails ? service().getSummaryControlMeasures(whereFilterString) : service()
                        .getControlMeasures(whereFilterString));
            } else {
                controlMeasures = (getDetails ? service().getSummaryControlMeasures(pollutant.getId(),
                        whereFilterString) : service().getControlMeasures(pollutant.getId(), whereFilterString));
            }
        }

        return controlMeasures;
    }
    
    public ControlMeasure[] getControlMeasures(Pollutant pollutant, Scc[] sccs, boolean getDetails, String nameContains) throws EmfException {

        if (sccs.length==0 )
            return getControlMeasures(pollutant, getDetails, nameContains);
        
        String scc="";
        for (int i=0; i<sccs.length-1; i++)
            scc +="'"+sccs[i].getCode()+"'" + ",";
        scc +="'"+sccs[sccs.length-1].getCode()+"'";
//        System.out.println(scc);
        
        String whereFilter = "cm.id in (select control_measures_id" 
                + " from emf.control_measure_sccs where "
                + "name in (" + scc + ")) ";
        if (nameContains != null && nameContains.trim().length() > 0) {

            String escapedNameContains = getPattern(nameContains.toLowerCase().trim());
            whereFilter += " and lower(cm.name) like " + escapedNameContains + " ";
        }

        if (pollutant.getName().equals("ALL") || pollutant.getName().equalsIgnoreCase("Select one"))
            return (getDetails ? service().getSummaryControlMeasures(whereFilter) : service().getControlMeasures(whereFilter));
        return (getDetails ? service().getSummaryControlMeasures(pollutant.getId(), whereFilter) : service().getControlMeasures(pollutant.getId(), whereFilter));
        
    }

    private String getPattern(String name) {
        name = name.replaceAll("\\*", "%%");
        name = name.replaceAll("!", "!!");
        name = name.replaceAll("'", "''");
        name = name.replaceAll("_", "!_");
        
        return "'%%" + name + "%%'" + (name.contains("!") ? " ESCAPE '!'" : "");
    }

    public CostYearTable getCostYearTable() {
        return costYearTable;
    }

    private CostYearTable populateCostYearTable() throws EmfException {
        return session.controlMeasureService().getCostYearTable(CostYearTable.REFERENCE_COST_YEAR);
    }

    public void generatePDFReport(int[] controlMeasureIds) throws EmfException {
        service().generateControlMeasurePDFReport(session.user(), controlMeasureIds);
    }
}
