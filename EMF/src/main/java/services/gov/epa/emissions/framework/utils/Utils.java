package gov.epa.emissions.framework.utils;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.InputName;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.GeoRegion;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;

public class Utils {

    public static final InputComparator G_TO_L_INPUT_COMPARATOR = new InputComparator();

    public static final ParameterComparator G_TO_L_PARAMETER_COMPARATOR = new ParameterComparator();

    public static void sortInputs(List<CaseInput> inputs) {
        Collections.sort(inputs, G_TO_L_INPUT_COMPARATOR);
    }

    public static void sortParameters(List<CaseParameter> parameters) {
        Collections.sort(parameters, G_TO_L_PARAMETER_COMPARATOR);
    }
    
    public static String getPattern(String name) {
        name = name.replaceAll("\\*", "%%");
        name = name.replaceAll("!", "!!");
        name = name.replaceAll("'", "''");
        name = name.replaceAll("_", "!_");
        
        return "'%%" + name + "%%'" + (name.contains("!") ? " ESCAPE '!'" : "");
    }

    public static boolean areEqualOrBothNull(Object o1, Object o2) {

        boolean equal = true;

        if ((o1 == null && o2 != null) || (o1 != null && o2 == null)) {
            equal = false;
        } else {
            equal = o1.equals(o2);
        }

        return equal;
    }

    static class InputComparator implements Comparator<CaseInput> {

        enum Order {
            GREATEST_TO_LEAST(-1), LEAST_TO_GREATEST(1);

            private int factor = 1;

            Order(int factor) {
                this.factor = factor;
            }

            public int getFactor() {
                return factor;
            }
        }

        private Order order;

        public InputComparator() {
            this(Order.GREATEST_TO_LEAST);
        }

        public InputComparator(Order order) {
            this.order = order;
        }

        /**
         * null and null --> equal !null and !null --> equal
         */
        public int compare(CaseInput o1, CaseInput o2) {

            int retval = 0;

            if (DebugLevels.DEBUG_9()) {
                
                System.out.println("Comparing:");
                String o1String = "o1=" + stringify(o1);
                System.out.println("  " + o1String);
                String o2String = "o2=" + stringify(o2);
                System.out.println("  " + o2String);
            }

            int factor = order.getFactor();

            int id1 = this.getJobID(o1);
            int id2 = this.getJobID(o2);
            if (id1 != 0 && id2 == 0) {
                retval = 1 * factor;
            } else if (id1 == 0 && id2 != 0) {
                retval = -1 * factor;
            } else {

                Sector sector1 = this.getSector(o1);
                Sector sector2 = this.getSector(o2);
                if (sector1 != null && sector2 == null) {
                    retval = 1 * factor;
                } else if (sector1 == null && sector2 != null) {
                    retval = -1 * factor;
                } else {

                    GeoRegion region1 = this.getRegion(o1);
                    GeoRegion region2 = this.getRegion(o2);
                    if (region1 != null && region2 == null) {
                        retval = 1 * factor;
                    } else if (region1 == null && region2 != null) {
                        retval = -1 * factor;
                    }
                }
            }

            if (DebugLevels.DEBUG_9()) {
                
                if (retval == 1) {
                    System.out.println("o1>o2");
                } else if (retval == -1) {
                    System.out.println("o2>o1");
                } else {
                    System.out.println("o1==o2");
                }
            }

            return retval;
        }

        int getJobID(CaseInput o) {
            return o.getCaseJobID();
        }

        GeoRegion getRegion(CaseInput o) {
            return o.getRegion();
        }

        Sector getSector(CaseInput o) {
            return o.getSector();
        }
    }

    static class ParameterComparator implements Comparator<CaseParameter> {

        enum Order {
            GREATEST_TO_LEAST(-1), LEAST_TO_GREATEST(1);

            private int factor = 1;

            Order(int factor) {
                this.factor = factor;
            }

            public int getFactor() {
                return factor;
            }
        }

        private Order order;

        public ParameterComparator() {
            this(Order.GREATEST_TO_LEAST);
        }

        public ParameterComparator(Order order) {
            this.order = order;
        }

        /**
         * null and null --> equal !null and !null --> equal
         */
        public int compare(CaseParameter o1, CaseParameter o2) {

            int retval = 0;

            if (DebugLevels.DEBUG_9()) {
                
                System.out.println("Comparing:");
                String o1String = "o1=" + stringify(o1);
                System.out.println("  " + o1String);
                String o2String = "o2=" + stringify(o2);
                System.out.println("  " + o2String);
            }

            int factor = order.getFactor();

            int id1 = this.getJobID(o1);
            int id2 = this.getJobID(o2);
            if (id1 != 0 && id2 == 0) {
                retval = 1 * factor;
            } else if (id1 == 0 && id2 != 0) {
                retval = -1 * factor;
            } else {

                Sector sector1 = this.getSector(o1);
                Sector sector2 = this.getSector(o2);
                if (sector1 != null && sector2 == null) {
                    retval = 1 * factor;
                } else if (sector1 == null && sector2 != null) {
                    retval = -1 * factor;
                } else {

                    GeoRegion region1 = this.getRegion(o1);
                    GeoRegion region2 = this.getRegion(o2);
                    if (region1 != null && region2 == null) {
                        retval = 1 * factor;
                    } else if (region1 == null && region2 != null) {
                        retval = -1 * factor;
                    }
                }
            }

            if (DebugLevels.DEBUG_9()) {
                
                if (retval == 1) {
                    System.out.println("o1>o2");
                } else if (retval == -1) {
                    System.out.println("o2>o1");
                } else {
                    System.out.println("o1==o2");
                }
            }

            return retval;
        }

        int getJobID(CaseParameter o) {
            return o.getJobId();
        }

        GeoRegion getRegion(CaseParameter o) {
            return o.getRegion();
        }

        Sector getSector(CaseParameter o) {
            return o.getSector();
        }
    }

    public static String stringify(CaseInput input) {

        int id1 = input.getCaseJobID();
        Sector sector1 = input.getSector();
        GeoRegion region1 = input.getRegion();

        String jobId1 = "all";
        if (id1 != 0) {
            jobId1 = Integer.toString(id1);
        }

        String sectorName1 = "all";
        if (sector1 != null) {
            sectorName1 = sector1.getName();
        }

        String regionName1 = "all";
        if (region1 != null) {
            regionName1 = region1.getName();
        }

        return regionName1 + ", " + sectorName1 + ", " + jobId1;
    }

    public static String stringify(CaseParameter parameter) {

        int id1 = parameter.getJobId();
        Sector sector1 = parameter.getSector();
        GeoRegion region1 = parameter.getRegion();

        String jobId1 = "all";
        if (id1 != 0) {
            jobId1 = Integer.toString(id1);
        }

        String sectorName1 = "all";
        if (sector1 != null) {
            sectorName1 = sector1.getName();
        }

        String regionName1 = "all";
        if (region1 != null) {
            regionName1 = region1.getName();
        }

        return regionName1 + ", " + sectorName1 + ", " + jobId1;
    }

    public static void main(String[] args) {

        List<CaseInput> inputs = new ArrayList<CaseInput>();

        GeoRegion region1 = new GeoRegion("r1");
        // GeoRegion region2 = new GeoRegion("r2");
        Sector sector1 = new Sector("s1", "s1");
        // Sector sector2 = new Sector("s2", "s2");

        CaseInput input = new CaseInput();
        input.setInputName(new InputName("null, null, j1"));
        input.setCaseJobID(1);
        inputs.add(input);

        // input = new CaseInput();
        // input.setInputName(new InputName("r2, null, null"));
        // input.setRegion(region2);
        // inputs.add(input);

        input = new CaseInput();
        input.setInputName(new InputName("r1, s1, null"));
        input.setRegion(region1);
        input.setSector(sector1);
        inputs.add(input);

        // input = new CaseInput();
        // input.setInputName(new InputName("r2, s1, null"));
        // input.setRegion(region2);
        // input.setSector(sector1);
        // inputs.add(input);

        input = new CaseInput();
        input.setInputName(new InputName("null, s1, null"));
        input.setSector(sector1);
        inputs.add(input);

        // input = new CaseInput();
        // input.setInputName(new InputName("null, s2, null"));
        // input.setSector(sector2);
        // inputs.add(input);

        // input = new CaseInput();
        // input.setInputName(new InputName("r1, s2, null"));
        // input.setRegion(region1);
        // input.setSector(sector2);
        // inputs.add(input);

        // input = new CaseInput();
        // input.setInputName(new InputName("r2, s2, null"));
        // input.setRegion(region2);
        // input.setSector(sector1);
        // inputs.add(input);

        input = new CaseInput();
        input.setInputName(new InputName("null, null, null"));
        inputs.add(input);

        input = new CaseInput();
        input.setInputName(new InputName("r1, null, j1"));
        input.setCaseJobID(1);
        input.setRegion(region1);
        inputs.add(input);

        // input = new CaseInput();
        // input.setInputName(new InputName("r2, null, j1"));
        // input.setCaseJobID(1);
        // input.setRegion(region2);
        // inputs.add(input);

        input = new CaseInput();
        input.setInputName(new InputName("r1, s1, j1"));
        input.setCaseJobID(1);
        input.setRegion(region1);
        input.setSector(sector1);
        inputs.add(input);

        // input = new CaseInput();
        // input.setInputName(new InputName("r2, s1, j1"));
        // input.setCaseJobID(1);
        // input.setRegion(region2);
        // input.setSector(sector1);
        // inputs.add(input);

        input = new CaseInput();
        input.setInputName(new InputName("null, s1, j1"));
        input.setCaseJobID(1);
        input.setSector(sector1);
        inputs.add(input);

        input = new CaseInput();
        input.setInputName(new InputName("r1, null, null"));
        input.setRegion(region1);
        inputs.add(input);

        // input = new CaseInput();
        // input.setInputName(new InputName("null, s2, j1"));
        // input.setCaseJobID(1);
        // input.setSector(sector2);
        // inputs.add(input);

        // input = new CaseInput();
        // input.setInputName(new InputName("r1, s2, j1"));
        // input.setCaseJobID(1);
        // input.setRegion(region1);
        // input.setSector(sector2);
        // inputs.add(input);

        // input = new CaseInput();
        // input.setInputName(new InputName("r2, s2, j1"));
        // input.setCaseJobID(1);
        // input.setRegion(region2);
        // input.setSector(sector1);
        // inputs.add(input);

        System.out.println("Before:");
        for (CaseInput caseInput : inputs) {
            System.out.println(caseInput);
        }

        sortInputs(inputs);

        System.out.println();
        System.out.println("After:");
        for (CaseInput caseInput : inputs) {
            System.out.println(caseInput);
        }

    }
    
    public static void addVersionEntryToVersionsTable(
            HibernateSessionFactory sessionFactory, 
            User user, 
            int datasetId,
            int version,
            String versionName,
            String path,
            boolean isFinal,
            String description) throws Exception {
        Version defaultZeroVersion = new Version(version);
        defaultZeroVersion.setName(versionName);
        defaultZeroVersion.setPath(path);
        defaultZeroVersion.setCreator(user);
        defaultZeroVersion.setDatasetId(datasetId);
        defaultZeroVersion.setLastModifiedDate(new Date());
//        defaultZeroVersion.setNumberRecords(version.getNumberRecords());
        defaultZeroVersion.setFinalVersion(isFinal);
        defaultZeroVersion.setDescription(description);
        
        Session session = sessionFactory.getSession();

        try {
            new DatasetDAO().add(defaultZeroVersion, session);
        } catch (Exception e) {
            throw new EmfException("Could not add version (" + version + "): " + e.getMessage());
        } finally {
            session.close();
        }
    }
}
