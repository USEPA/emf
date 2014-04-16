package gov.epa.emissions.commons.util;

public class StringTools {
    public final static String EMF_DOUBLE_QUOTE = "%%EmfDoubleQuote%%";
    public final static String EMF_SINGLE_QUOTE = "%%EmfSingleQuote%%";
    public final static String EMF_NEW_LINE = "%%EmfNewLine%%";
    public final static String SYS_NEW_LINE = System.getProperty("line.separator");

    public static String replaceNoneLetterDigit(String string, char substitute) {
        for (int i = 0; i < string.length(); i++) {
            if (!Character.isLetterOrDigit(string.charAt(i))) {
                string = string.replace(string.charAt(i), substitute);
            }
        }

        return string;
    }
    
    public static String escapeBackSlash4jdbc(String col) {
        return col.replaceAll("\\\\", "\\\\\\\\");
    }
    
    public static String escapeBackSlash(String string) {
        return string.replaceAll("\\\\", "\\\\");
    }
}
