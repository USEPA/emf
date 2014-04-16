package gov.epa.emissions.framework;

import gov.epa.emissions.framework.services.EmfException;
import junit.framework.TestCase;

public class UtilsTestCase extends TestCase {

    public void testMonthNotZeroOr13() {

        try {
            String dateStr = "00/01/2010";
            Utils.validateDate(dateStr);
            fail(dateStr + " is not a valid date.");
        } catch (EmfException e) {

            String localizedMessage = e.getLocalizedMessage();
            assertTrue(localizedMessage.startsWith("Invalid date"));
        }

        try {
            String dateStr = "13/01/2010";
            Utils.validateDate(dateStr);
            fail(dateStr + " is not a valid date.");
        } catch (EmfException e) {

            String localizedMessage = e.getLocalizedMessage();
            assertTrue(localizedMessage.startsWith("Invalid date"));
        }
    }

    public void testDayNotZeroOr32() {

        try {
            String dateStr = "01/00/2010";
            Utils.validateDate(dateStr);
            fail(dateStr + " is not a valid date.");
        } catch (EmfException e) {

            String localizedMessage = e.getLocalizedMessage();
            assertTrue(localizedMessage.startsWith("Invalid date"));
        }

        try {
            String dateStr = "01/32/2010";
            Utils.validateDate(dateStr);
            fail(dateStr + " is not a valid date.");
        } catch (EmfException e) {

            String localizedMessage = e.getLocalizedMessage();
            assertTrue(localizedMessage.startsWith("Invalid date"));
        }
    }

    public void testDaysOf31() {

        try {
            String dateStr = "01/31/2010";
            Utils.validateDate(dateStr);
        } catch (EmfException e) {

            String localizedMessage = e.getLocalizedMessage();
            fail("Should not have thrown exception: " + localizedMessage);
        }

        try {
            String dateStr = "02/31/2010";
            Utils.validateDate(dateStr);
            fail(dateStr + " is not a valid date.");
        } catch (EmfException e) {

            String localizedMessage = e.getLocalizedMessage();
            assertTrue(localizedMessage.startsWith("Invalid date"));
        }

        try {
            String dateStr = "03/31/2010";
            Utils.validateDate(dateStr);
        } catch (EmfException e) {

            String localizedMessage = e.getLocalizedMessage();
            fail("Should not have thrown exception: " + localizedMessage);
        }

        try {
            String dateStr = "04/31/2010";
            Utils.validateDate(dateStr);
            fail(dateStr + " is not a valid date.");
        } catch (EmfException e) {

            String localizedMessage = e.getLocalizedMessage();
            assertTrue(localizedMessage.startsWith("Invalid date"));
        }

        try {
            String dateStr = "05/31/2010";
            Utils.validateDate(dateStr);
        } catch (EmfException e) {

            String localizedMessage = e.getLocalizedMessage();
            fail("Should not have thrown exception: " + localizedMessage);
        }

        try {
            String dateStr = "06/31/2010";
            Utils.validateDate(dateStr);
            fail(dateStr + " is not a valid date.");
        } catch (EmfException e) {

            String localizedMessage = e.getLocalizedMessage();
            assertTrue(localizedMessage.startsWith("Invalid date"));
        }

        try {
            String dateStr = "07/31/2010";
            Utils.validateDate(dateStr);
        } catch (EmfException e) {

            String localizedMessage = e.getLocalizedMessage();
            fail("Should not have thrown exception: " + localizedMessage);
        }

        try {
            String dateStr = "08/31/2010";
            Utils.validateDate(dateStr);
        } catch (EmfException e) {

            String localizedMessage = e.getLocalizedMessage();
            fail("Should not have thrown exception: " + localizedMessage);
        }

        try {
            String dateStr = "09/31/2010";
            Utils.validateDate(dateStr);
            fail(dateStr + " is not a valid date.");
        } catch (EmfException e) {

            String localizedMessage = e.getLocalizedMessage();
            assertTrue(localizedMessage.startsWith("Invalid date"));
        }

        try {
            String dateStr = "10/31/2010";
            Utils.validateDate(dateStr);
        } catch (EmfException e) {

            String localizedMessage = e.getLocalizedMessage();
            fail("Should not have thrown exception: " + localizedMessage);
        }
        try {
            String dateStr = "11/31/2010";
            Utils.validateDate(dateStr);
            fail(dateStr + " is not a valid date.");
        } catch (EmfException e) {

            String localizedMessage = e.getLocalizedMessage();
            assertTrue(localizedMessage.startsWith("Invalid date"));
        }

        try {
            String dateStr = "12/31/2010";
            Utils.validateDate(dateStr);
        } catch (EmfException e) {

            String localizedMessage = e.getLocalizedMessage();
            fail("Should not have thrown exception: " + localizedMessage);
        }
    }

    public void testDayFeb() {

        try {
            String dateStr = "02/28/2010";
            Utils.validateDate(dateStr);
        } catch (EmfException e) {

            String localizedMessage = e.getLocalizedMessage();
            fail("Should not have thrown exception: " + localizedMessage);
        }

        try {
            String dateStr = "02/29/2010";
            Utils.validateDate(dateStr);
            fail(dateStr + " is not a valid date.");
        } catch (EmfException e) {

            String localizedMessage = e.getLocalizedMessage();
            assertTrue(localizedMessage.startsWith("Invalid date"));
        }

        try {
            String dateStr = "02/30/2010";
            Utils.validateDate(dateStr);
            fail(dateStr + " is not a valid date.");
        } catch (EmfException e) {

            String localizedMessage = e.getLocalizedMessage();
            assertTrue(localizedMessage.startsWith("Invalid date"));
        }

        try {
            String dateStr = "02/31/2010";
            Utils.validateDate(dateStr);
            fail(dateStr + " is not a valid date.");
        } catch (EmfException e) {

            String localizedMessage = e.getLocalizedMessage();
            assertTrue(localizedMessage.startsWith("Invalid date"));
        }

        try {
            String dateStr = "02/32/2010";
            Utils.validateDate(dateStr);
            fail(dateStr + " is not a valid date.");
        } catch (EmfException e) {

            String localizedMessage = e.getLocalizedMessage();
            assertTrue(localizedMessage.startsWith("Invalid date"));
        }

        try {
            String dateStr = "02/28/2008";
            Utils.validateDate(dateStr);
        } catch (EmfException e) {

            String localizedMessage = e.getLocalizedMessage();
            fail("Should not have thrown exception: " + localizedMessage);
        }

        try {
            String dateStr = "02/28/2008";
            Utils.validateDate(dateStr);
        } catch (EmfException e) {

            String localizedMessage = e.getLocalizedMessage();
            fail("Should not have thrown exception: " + localizedMessage);
        }

        try {
            String dateStr = "02/30/2008";
            Utils.validateDate(dateStr);
            fail(dateStr + " is not a valid date.");
        } catch (EmfException e) {

            String localizedMessage = e.getLocalizedMessage();
            assertTrue(localizedMessage.startsWith("Invalid date"));
        }

        try {
            String dateStr = "02/31/2008";
            Utils.validateDate(dateStr);
            fail(dateStr + " is not a valid date.");
        } catch (EmfException e) {

            String localizedMessage = e.getLocalizedMessage();
            assertTrue(localizedMessage.startsWith("Invalid date"));
        }

        try {
            String dateStr = "02/32/2008";
            Utils.validateDate(dateStr);
            fail(dateStr + " is not a valid date.");
        } catch (EmfException e) {

            String localizedMessage = e.getLocalizedMessage();
            assertTrue(localizedMessage.startsWith("Invalid date"));
        }
    }

    public void testDateForm() {

        try {
            String dateStr = "1/1/2010";
            Utils.validateDate(dateStr);
            fail(dateStr + " is not a valid date string.");
        } catch (EmfException e) {

            String localizedMessage = e.getLocalizedMessage();
            assertTrue(localizedMessage.contains("not of the form"));
        }

        try {
            String dateStr = "1/01/2010";
            Utils.validateDate(dateStr);
            fail(dateStr + " is not a valid date string.");
        } catch (EmfException e) {

            String localizedMessage = e.getLocalizedMessage();
            assertTrue(localizedMessage.contains("not of the form"));
        }

        try {
            String dateStr = "01/1/2010";
            Utils.validateDate(dateStr);
            fail(dateStr + " is not a valid date string.");
        } catch (EmfException e) {

            String localizedMessage = e.getLocalizedMessage();
            assertTrue(localizedMessage.contains("not of the form"));
        }

        try {
            String dateStr = "01/01/10";
            Utils.validateDate(dateStr);
            fail(dateStr + " is not a valid date string.");
        } catch (EmfException e) {

            String localizedMessage = e.getLocalizedMessage();
            assertTrue(localizedMessage.contains("not of the form"));
        }

        try {
            String dateStr = "01/01/20100";
            Utils.validateDate(dateStr);
            fail(dateStr + " is not a valid date string.");
        } catch (EmfException e) {

            String localizedMessage = e.getLocalizedMessage();
            assertTrue(localizedMessage.contains("not of the form"));
        }
    }
}
