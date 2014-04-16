package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.framework.services.ServiceTestCase;

import java.util.List;

public class CaseDAO_AttributesTest extends ServiceTestCase {

    private CaseDAO dao;

    protected void doSetUp() throws Exception {
        dao = new CaseDAO();
    }

    protected void doTearDown() {// no op
    }

    public void testShouldGetAllAbbreviations() {
        int totalBeforeAdd = dao.getAbbreviations(session).size();
        Abbreviation element = new Abbreviation("test" + Math.random());
        add(element);

        session.clear();
        try {
            List list = dao.getAbbreviations(session);
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

    public void testShouldPersistAbbreviationOnAdd() {
        int totalBeforeAdd = dao.getAbbreviations(session).size();
        Abbreviation element = new Abbreviation("test" + Math.random());
        dao.add(element, session);

        session.clear();
        try {
            List list = dao.getAbbreviations(session);
            assertEquals(totalBeforeAdd + 1, list.size());
        } finally {
            remove(element);
        }
    }

    public void testShouldGetAllAirQualityModels() {
        int totalBeforeAdd = dao.getAirQualityModels(session).size();
        AirQualityModel element = new AirQualityModel("test" + Math.random());
        add(element);

        session.clear();
        try {
            List list = dao.getAirQualityModels(session);
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

    public void testShouldPersistAirQualityModelOnAdd() {
        int totalBeforeAdd = dao.getAirQualityModels(session).size();
        AirQualityModel element = new AirQualityModel("test" + Math.random());
        dao.add(element, session);

        session.clear();
        try {
            List list = dao.getAirQualityModels(session);
            assertEquals(totalBeforeAdd + 1, list.size());
        } finally {
            remove(element);
        }
    }

    public void testShouldGetAllCaseCategories() {
        int totalBeforeAdd = dao.getCaseCategories(session).size();
        CaseCategory element = new CaseCategory("test" + Math.random());
        add(element);

        session.clear();
        try {
            List list = dao.getCaseCategories(session);
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

    public void testShouldPersistCaseCategoryOnAdd() {
        int totalBeforeAdd = dao.getCaseCategories(session).size();
        CaseCategory element = new CaseCategory("test" + Math.random());
        dao.add(element, session);

        session.clear();
        try {
            List list = dao.getCaseCategories(session);
            assertEquals(totalBeforeAdd + 1, list.size());
        } finally {
            remove(element);
        }
    }

    public void testShouldPersistEmissionsYearOnAdd() {
        int totalBeforeAdd = dao.getEmissionsYears(session).size();
        EmissionsYear element = new EmissionsYear("test" + Math.random());
        dao.add(element, session);

        session.clear();
        try {
            List list = dao.getEmissionsYears(session);
            assertEquals(totalBeforeAdd + 1, list.size());
        } finally {
            remove(element);
        }
    }

    public void testShouldGetAllEmissionsYears() {
        int totalBeforeAdd = dao.getEmissionsYears(session).size();
        EmissionsYear element = new EmissionsYear("test" + Math.random());
        add(element);

        session.clear();
        try {
            List list = dao.getEmissionsYears(session);
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

    public void testShouldPersistMeteorlogicalYearOnAdd() {
        int totalBeforeAdd = dao.getMeteorlogicalYears(session).size();
        MeteorlogicalYear element = new MeteorlogicalYear("test" + Math.random());
        dao.add(element, session);

        session.clear();
        try {
            List list = dao.getMeteorlogicalYears(session);
            assertEquals(totalBeforeAdd + 1, list.size());
        } finally {
            remove(element);
        }
    }

    public void testShouldGetAllMeteorlogicalYears() {
        int totalBeforeAdd = dao.getMeteorlogicalYears(session).size();
        MeteorlogicalYear element = new MeteorlogicalYear("test" + Math.random());
        add(element);

        session.clear();
        try {
            List list = dao.getMeteorlogicalYears(session);
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

    public void testShouldPersistSpeciationOnAdd() {
        int totalBeforeAdd = dao.getSpeciations(session).size();
        Speciation element = new Speciation("test" + Math.random());
        dao.add(element, session);

        session.clear();
        try {
            List list = dao.getSpeciations(session);
            assertEquals(totalBeforeAdd + 1, list.size());
        } finally {
            remove(element);
        }
    }

    public void testShouldGetAllSpeciations() {
        int totalBeforeAdd = dao.getSpeciations(session).size();
        Speciation element = new Speciation("test" + Math.random());
        add(element);

        session.clear();
        try {
            List list = dao.getSpeciations(session);
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

}
