package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

import java.util.ArrayList;
import java.util.List;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

public class LocaleFilter {

    public boolean acceptLocale(String locale, String fips) {
        //shouln't happen, locale should always be shorter
        if (locale.length() > fips.length())
            return false;

        //non-locale specific...
        if (locale.length() == 0)
            return true;

        //see if locale includes country and region/state and no county info...
        if (locale.length() == 3 || locale.length() == 2) {
//            Pattern pat = 
//                Pattern.compile("^" + locale + "*");
//            Matcher matcher = pat.matcher(fips);
//            return matcher.find();//fips.matches(pattern);
            boolean foo = fips.substring(0, locale.length()).equals(locale);
            return foo;//fips.substring(0, locale.length()).equals(locale);
        }

        //see if locale includes country and region/state and county info...
        if (locale.length() > 4) {
            return fips.equals(locale);
        }

        return false;

//        return locale + "\\d{" + (length - localeLength) + "}";
//        return fips.regionMatches(fips.length() - locale.length(), locale, 0, locale.length());

//        String pattern = pattern(locale, fips.length());
//        return fips.matches(pattern);
    }

//    private String pattern(String locale, int length) {
//        int localeLength = locale.length();
//        return locale + "\\d{" + (length - localeLength) + "}";
//    }

    public EfficiencyRecord[] closestRecords(List records) {
        if (records.isEmpty())
            return new EfficiencyRecord[0];

        List closesRecords = new ArrayList();
        EfficiencyRecord closeRecord = (EfficiencyRecord) records.get(0);
        closesRecords.add(closeRecord);

        for (int i = 1; i < records.size(); i++) {
            EfficiencyRecord record = (EfficiencyRecord) records.get(i);
            if (isCloser(closesRecords, record, closeRecord))
                closeRecord = record;
        }
        return (EfficiencyRecord[]) closesRecords.toArray(new EfficiencyRecord[0]);
    }

    private boolean isCloser(List closerRecords, EfficiencyRecord record, EfficiencyRecord closeRecord) {
        if (record.getLocale().length() > closeRecord.getLocale().length()) {
            closerRecords.clear();
            closerRecords.add(record);
            return true;
        }

        if (record.getLocale().length() == closeRecord.getLocale().length())
            closerRecords.add(closeRecord);

        return false;
    }

}