package gov.epa.emissions.commons.io.ida;

import gov.epa.emissions.commons.io.importer.ImporterException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IDAPollutantParser {

    public static final String POLLUTANT_TAG1 = "#DATA";

    public static final String POLLUTANT_TAG2 = "#POLID";

    private List comments;

    private String[] pollutants;

    public IDAPollutantParser() {
        comments = new ArrayList();
    }

    public String[] pollutants() {
        return pollutants;
    }

    public void processComments(String[] lines) throws ImporterException {
        for (int i = 0; i < lines.length; i++) {
            comments.add(lines[i]);
            if (isPollutantLine(lines[i])) {
                pollutants = parsePollutants(lines[i]);
            }
        }
        pollutantFound();
    }

    private String[] parsePollutants(String line) throws ImporterException {
        Pattern pattern = Pattern.compile("\\S+");
        Matcher matcher = pattern.matcher(line);
        List tokens = new ArrayList();
        matcher.find();// skip the pollutant tag
        while (matcher.find()) {
            String token = line.substring(matcher.start(), matcher.end());
            tokens.add(token);
        }
        if (tokens.isEmpty())
            throw new ImporterException("No pollutants specified");
        return (String[]) tokens.toArray(new String[0]);
    }

    private boolean isPollutantLine(String line) {
        return (line.startsWith(POLLUTANT_TAG1) || (line.startsWith(POLLUTANT_TAG2)));
    }
    
    private void pollutantFound() throws ImporterException {
        if (pollutants == null || pollutants.length == 0)
            throw new ImporterException("Could not find pollutant tag either '" + IDAPollutantParser.POLLUTANT_TAG1 + "' or '"
                    + IDAPollutantParser.POLLUTANT_TAG2 + "'");

    }

    public List comments() {
        return comments;
    }

}
