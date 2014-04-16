package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureService;
import gov.epa.emissions.framework.services.cost.ControlMeasureServiceImpl;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.util.Date;

public class ControlMeasureServicePerformanceTest extends WebServicesTestCase {
    private static final String DEFAULT_URL = "http://localhost:8080/emf/services";// default

    private ControlMeasureService transport = null;

    private ControlMeasureService cmService;
    
    private HibernateSessionFactory sessionFactory;
    
    protected void doSetUp() throws Exception {
        sessionFactory = sessionFactory(new File("test/webservices.conf"));
        RemoteServiceLocator rl = new RemoteServiceLocator(DEFAULT_URL);
        transport = rl.controlMeasureService();
        cmService = new ControlMeasureServiceImpl(sessionFactory, dbServerFactory());
    }

    public void testShouldGetAllControlMeasures_AtServerSide() throws Exception {
        dumpMemory();
        ControlMeasure[] all = cmService.getMeasures();
        dumpMemory();
        assertEquals(32,all.length);
    }
    
    public void itestShouldGetAllControlMeasures() throws EmfException {
        dumpMemory();
        ControlMeasure[] all = transport.getMeasures();
        dumpMemory();
        assertEquals(1067,all.length);
    }

 

    protected void doTearDown() throws Exception {// no op
    }
    
    protected void dumpMemory() {
        System.out.println("date-"+CustomDateFormat.format_MM_DD_YYYY_HH_mm_ss(new Date())+", "+usedMemory() + " MB");
        
    }

    protected long usedMemory() {
        return (totalMemory() - freeMemory());
    }

    protected long maxMemory() {
        return (Runtime.getRuntime().maxMemory() / megabyte());
    }

    protected long freeMemory() {
        return Runtime.getRuntime().freeMemory() / megabyte();
    }
    

    private int megabyte() {
        return (1024 * 1024);
    }
    

    protected long totalMemory() {
        return Runtime.getRuntime().totalMemory() / megabyte();
    }

}
