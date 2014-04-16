package gov.epa.emissions.commons.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomDateFormat {

    public static final String PATTERN_yyyyMMddHHmm = "yyyy/MM/dd HH:mm";
    
    public static final String PATTERN_MMddYYYY_HHmm = "MM/dd/yyyy HH:mm";

    private static SimpleDateFormat dateFormatter = new SimpleDateFormat();
    

    public static String format_YYYY_MM_DD_HH_MM(Date date) {
        dateFormatter.applyPattern(PATTERN_yyyyMMddHHmm);
        return date == null ? "" : dateFormatter.format(date);
    }

    public static String format_YYYYMMDDHHMM(Date date) {
        dateFormatter.applyPattern("yyyyMMddHHmm");
        return date == null ? "" : dateFormatter.format(date);
    }

    public static String format_MM_DD_YYYY(Date date) {
        dateFormatter.applyPattern("MM/dd/yyyy");
        return date == null ? "" : dateFormatter.format(date);
    }

    public static String format_MM_DD_YYYY_HH_mm(Date date) {
        dateFormatter.applyPattern(PATTERN_MMddYYYY_HHmm);
        return date == null ? "" : dateFormatter.format(date);
    }

    public static Date parse_MM_DD_YYYY_HH_mm(String date) throws ParseException {
        dateFormatter.applyPattern(PATTERN_MMddYYYY_HHmm);
        return date == null || date.trim().isEmpty() ? null : dateFormatter.parse(date);
    }
    
    public static String format_YYYY(Date date) {
        dateFormatter.applyPattern("yyyy");
        return date == null ? "" : dateFormatter.format(date);
    }

    public static Date parse_YYYY(String date) throws ParseException {
        dateFormatter.applyPattern("yyyy");
        return date == null || date.trim().isEmpty()? null : dateFormatter.parse(date);
    }

    public static Date parse_MMddyyyy(String date) throws ParseException {
        dateFormatter.applyPattern("MM/dd/yyyy");
        return date == null || date.trim().isEmpty()? null : dateFormatter.parse(date);
    }
    
    public static String format_ddMMMyyyy(Date date) {
        dateFormatter.applyPattern("ddMMMyyyy");
        return date == null ? "" : dateFormatter.format(date);
    }

    public static String format_MM_DD_YYYY_HH_mm_ss(Date date) {
        dateFormatter.applyPattern("MM/dd/yyyy HH:mm:ss");
        return date == null ? "" : dateFormatter.format(date);
    }

    public static String format_MMDDYYYYHHmmss(Date date) {
        dateFormatter.applyPattern("MM-dd-yyyy-HHmmss");
        return date == null ? "" : dateFormatter.format(date);
    }

    public static Date parse_YYYY_MM_DD_HH_MM(String date) throws ParseException {
        dateFormatter.applyPattern("yyyy/MM/dd HH:mm");
        return date == null || date.trim().isEmpty()? null : dateFormatter.parse(date);
    }

    public static String format_YYYY_MM_DD_HH_MM_ss_SS(Date date) {
        dateFormatter.applyPattern("yyyy/MM/dd HH:mm:ss:SS");
        return date == null ? "" : dateFormatter.format(date);
    }

    public static String format_yyyy_MM_dd_HHmmssSS(Date date) {
        dateFormatter.applyPattern("yyyy-MM-dd-HH:mm:ss:SS");
        return date == null ? "" : dateFormatter.format(date);
    }
    
    public static String format_yyyy_MM_dd_HHmmssSSS(Date date) {
        dateFormatter.applyPattern("yyyy-MM-dd HH:mm:ss.SSS");
        return date == null ? "" : dateFormatter.format(date);
    }
    
    public static String format_yyyy_MM_dd_HHmmss(Date date) {
        dateFormatter.applyPattern("yyyy-MM-dd HH:mm:ss");
        return date == null ? "" : dateFormatter.format(date);
    }

    public static Date format_yyyy_MM_dd_HHmmssSS(String date) throws ParseException {
        dateFormatter.applyPattern("yyyy-MM-dd-HH:mm:ss:SS");
        return date == null || date.trim().isEmpty()? null : dateFormatter.parse(date);
//        return date == null ? "" : dateFormatter.format(date);
    }

    public static Date format_yyyy_MM_dd_HHmmss(String date) throws ParseException {
        dateFormatter.applyPattern("yyyy-MM-dd HH:mm:ss");
        return date == null || date.trim().isEmpty()? null : dateFormatter.parse(date);
//        return date == null ? "" : dateFormatter.format(date);
    }

    public static String format_YYYYMMDDHHMMSS(Date date) {
        dateFormatter.applyPattern("yyyyMMddHHmmss");
        return date == null ? "" : dateFormatter.format(date);
    }


    public static String format_YYYYMMDDHHMMSSSS(Date date) {
        dateFormatter.applyPattern("yyyyMMddHHmmssSS");
        return date == null ? "" : dateFormatter.format(date);
    }

    public static String format_YYDDHHMMSS(Date date) {
        dateFormatter.applyPattern("yyddHHmmss");
        return date == null ? "" : dateFormatter.format(date);
    }

    public static String format_HHMM(Date date) {
        dateFormatter.applyPattern("HHmm");
        return date == null ? "" : dateFormatter.format(date);
    }

    public static String format_HHMMSSSS(Date date) {
        dateFormatter.applyPattern("HHmmssSS");
        return date == null ? "" : dateFormatter.format(date);
    }
    
    public static String format_h_m_a_MMM_DD(Date date) {
        dateFormatter.applyPattern("hh:mm a MMM dd");
        return date == null ? "" : dateFormatter.format(date);
    }
    
    public static void main(String[] args) {
        System.out.println(CustomDateFormat.format_h_m_a_MMM_DD(new Date())); 
    }
}
