package gov.epa.emissions.framework.install.installer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilePatternMatcher {

    private Pattern pattern;

    public FilePatternMatcher(File folder, String filePattern) throws ImporterException {
        this.pattern = Pattern.compile(pattern(folder, filePattern));
    }

    private String pattern(File folder, String filePattern) throws ImporterException {
        if(new File(folder, filePattern).exists())
            return filePattern;
        
        String regExPattern = filePattern;
        StringBuffer sb = new StringBuffer(regExPattern);
        int index = regExPattern.indexOf(".");
        String replacePattern = ".*";
        if (index > -1) {
            sb.replace(index, index + 1, "\\.");
            int lastIndex = sb.toString().lastIndexOf(".");
            if (lastIndex > -1 && lastIndex != index + 1) {
                throw new ImporterException("Only one '.' is allowed in the expression for file patterns");
            }
            replacePattern = "[\\w\\-\\.]*";
        }

        for (int i = 0; i < sb.length(); i++) {
            if (sb.charAt(i) == '*') {
                sb.replace(i, i + 1, replacePattern);
                i = i + replacePattern.length() - 1;
            }
        }

        return sb.toString();
    }

    public String[] matchingNames(String[] names) {
        List matchingNames = new ArrayList();
        if (names != null)
        {
            for (int i = 0; i < names.length;i++) {
                Matcher m = pattern.matcher(names[i]);
                if (m.matches()) {
                    matchingNames.add(names[i]);
                }
            }
        }
        return (String[]) matchingNames.toArray(new String[0]);
    }

}
