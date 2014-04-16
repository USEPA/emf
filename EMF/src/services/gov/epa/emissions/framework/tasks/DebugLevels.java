package gov.epa.emissions.framework.tasks;

import java.util.Date;

import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

public class DebugLevels {
    
    private static Date previousTime = new Date();
    
    private static final HibernateSessionFactory sessionFactory  = HibernateSessionFactory.get();
    private static final EmfPropertiesDAO propDAO = new EmfPropertiesDAO(sessionFactory);
    private static boolean getProperty(String name) {
        return propDAO.getProperty(name).getValue().trim().equalsIgnoreCase("true");
    }
    
    private static int refreshRate = Integer.parseInt(propDAO.getProperty("DEBUG_LEVEL_REFRESH_RATE").getValue().trim());
    
    private static boolean debug_0 = getProperty("DEBUG_0");
    private static boolean debug_1 = getProperty("DEBUG_1");
    private static boolean debug_2 = getProperty("DEBUG_2");
    private static boolean debug_3 = getProperty("DEBUG_3");
    private static boolean debug_4 = getProperty("DEBUG_4");
    private static boolean debug_5 = getProperty("DEBUG_5");
    private static boolean debug_6 = getProperty("DEBUG_6");
    private static boolean debug_7 = getProperty("DEBUG_7");
    private static boolean debug_8 = getProperty("DEBUG_8");
    private static boolean debug_9 = getProperty("DEBUG_9");
    private static boolean debug_10 = getProperty("DEBUG_10");
    private static boolean debug_11 = getProperty("DEBUG_11");
    private static boolean debug_12 = getProperty("DEBUG_12");
    private static boolean debug_13 = getProperty("DEBUG_13");
    private static boolean debug_14 = getProperty("DEBUG_14");
    private static boolean debug_15 = getProperty("DEBUG_15");
    private static boolean debug_16 = getProperty("DEBUG_16");
    private static boolean debug_17 = getProperty("DEBUG_17");
    private static boolean debug_18 = getProperty("DEBUG_18");
    private static boolean debug_19 = getProperty("DEBUG_19");
    private static boolean debug_20 = getProperty("DEBUG_20");
    private static boolean debug_21 = getProperty("DEBUG_21");
    private static boolean debug_22 = getProperty("DEBUG_22");
    private static boolean debug_23 = getProperty("DEBUG_23");
    private static boolean debug_24 = getProperty("DEBUG_24");
    private static boolean debug_25 = getProperty("DEBUG_25");
    private static boolean debug_26 = getProperty("DEBUG_26");
    
    
    private static synchronized void refresh() {
        Date current = new Date();
        if ( (current.getTime() - previousTime.getTime())/60000 >= refreshRate) {
            previousTime = new Date();
            
            debug_0 = getProperty("DEBUG_0");
            debug_1 = getProperty("DEBUG_1");
            debug_2 = getProperty("DEBUG_2");
            debug_3 = getProperty("DEBUG_3");
            debug_4 = getProperty("DEBUG_4");
            debug_5 = getProperty("DEBUG_5");
            debug_6 = getProperty("DEBUG_6");
            debug_7 = getProperty("DEBUG_7");
            debug_8 = getProperty("DEBUG_8");
            debug_9 = getProperty("DEBUG_9");
            debug_10 = getProperty("DEBUG_10");
            debug_11 = getProperty("DEBUG_11");
            debug_12 = getProperty("DEBUG_12");
            debug_13 = getProperty("DEBUG_13");
            debug_14 = getProperty("DEBUG_14");
            debug_15 = getProperty("DEBUG_15");
            debug_16 = getProperty("DEBUG_16");
            debug_17 = getProperty("DEBUG_17");
            debug_18 = getProperty("DEBUG_18");
            debug_19 = getProperty("DEBUG_19");
            debug_20 = getProperty("DEBUG_20");
            debug_21 = getProperty("DEBUG_21");
            debug_22 = getProperty("DEBUG_22");
            debug_23 = getProperty("DEBUG_23");
            debug_24 = getProperty("DEBUG_24");
            debug_25 = getProperty("DEBUG_25");
            debug_26 = getProperty("DEBUG_26");
            refreshRate = Integer.parseInt(propDAO.getProperty("DEBUG_LEVEL_REFRESH_RATE").getValue().trim());
        }
    }

    public static final boolean DEBUG_0() { refresh(); return debug_0; }
    public static final boolean DEBUG_1() { refresh(); return debug_1; }
    public static final boolean DEBUG_2() { refresh(); return debug_2; }
    public static final boolean DEBUG_3() { refresh(); return debug_3; }
    public static final boolean DEBUG_4() { refresh(); return debug_4; } 
    public static final boolean DEBUG_5() { refresh(); return debug_5; }
    public static final boolean DEBUG_6() { refresh(); return debug_6; }
    public static final boolean DEBUG_7() { refresh(); return debug_7; }
    public static final boolean DEBUG_8() { refresh(); return debug_8; }
    public static final boolean DEBUG_9() { refresh(); return debug_9; } // very detailed job run messages
    public static final boolean DEBUG_10() { refresh(); return debug_10; } //to trace import start messages
    public static final boolean DEBUG_11() { refresh(); return debug_11; } //to trace pattern matching of files
    public static final boolean DEBUG_12() { refresh(); return debug_12; } //to trace DAO classes
    public static final boolean DEBUG_13() { refresh(); return debug_13; }
    public static final boolean DEBUG_14() { refresh(); return debug_14; } //to trace job run issues
    public static final boolean DEBUG_15() { refresh(); return debug_15; } //to trace export job submitter issues
    public static final boolean DEBUG_16() { refresh(); return debug_16; } //to trace dataset deletion
    public static final boolean DEBUG_17() { refresh(); return debug_17; } //to trace new method for case output registration
    public static final boolean DEBUG_18() { refresh(); return debug_18; } //to trace timing in various functions
    public static final boolean DEBUG_19() { refresh(); return debug_19; } //to trace get page issues
    public static final boolean DEBUG_20() { refresh(); return debug_20; } //to trace case ModelToRun issues
    public static final boolean DEBUG_21() { refresh(); return debug_21; }
    public static final boolean DEBUG_22() { refresh(); return debug_22; }  //to trace enhancing flat file 2010 point QA program
    public static final boolean DEBUG_23() { refresh(); return debug_23; } // DEBUG_CMIMPORT
    public static final boolean DEBUG_24() { refresh(); return debug_24; } // debug ExportTask
    public static final boolean DEBUG_25() { refresh(); return debug_25; } // debug ControlStrategy operations
    public static final boolean DEBUG_26() { refresh(); return debug_26; } //to trace case job run parse Queue Id functionality

}
