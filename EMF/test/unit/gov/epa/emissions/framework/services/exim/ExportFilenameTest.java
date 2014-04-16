package gov.epa.emissions.framework.services.exim;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

public class ExportFilenameTest extends TestCase {
    public void testShouldGiveCorrectExportFilename() {
        DatasetType type = new DatasetType();
        EmfDataset dataset = new EmfDataset();
        dataset.setDatasetType(type);
        KeyVal[] keyvals = {new KeyVal(), new KeyVal(), new KeyVal(), new KeyVal()};
        keyvals[0].setKeyword(new Keyword("none"));
        keyvals[1].setKeyword(new Keyword("EXPORT_PREFIX"));
        keyvals[2].setKeyword(new Keyword("EXPORT_SUFFIX"));
        keyvals[3].setKeyword(new Keyword("none"));
        keyvals[0].setValue("");
        keyvals[1].setValue("hello");
        keyvals[2].setValue("world");
        keyvals[3].setValue("");
        dataset.setKeyVals(keyvals);
        dataset.setDefaultVersion(1);
        dataset.setName("123Emf^&*data.Set.txt");
        String timeformat = "ddMMMyyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(timeformat);
        String date = sdf.format(new Date());
        
        String expected = "hello123Emf___data_Set_txt_" + date.toLowerCase() + "world";
        assertTrue(expected, expected.equals(getCorrectExportFilename(dataset)));
    }
    
    private String getCorrectExportFilename(EmfDataset dataset) {
        String name = dataset.getName();
        String prefix = "", suffix = "";
        KeyVal[] keyvals = dataset.getKeyVals();
        String timeformat = "ddMMMyyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(timeformat);
        String date = sdf.format(new Date());
        
        for(int i = 0; i < keyvals.length; i++) {
            prefix = keyvals[i].getKeyword().getName().equalsIgnoreCase("EXPORT_PREFIX") ? keyvals[i].getValue():"";
            if(!prefix.equals(""))
                break;
        }
        
        for(int i = 0; i < keyvals.length; i++) {
            suffix = keyvals[i].getKeyword().getName().equalsIgnoreCase("EXPORT_SUFFIX") ? keyvals[i].getValue():"";
            if(!suffix.equals(""))
                break;
        }

        for (int i = 0; i < name.length(); i++) {
            if (!Character.isLetterOrDigit(name.charAt(i))) {
                name = name.replace(name.charAt(i), '_');
            }
        }

        return prefix + name + "_" + date.toLowerCase() + suffix;
    }

}
