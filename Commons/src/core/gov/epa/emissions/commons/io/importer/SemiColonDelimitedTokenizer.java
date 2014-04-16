package gov.epa.emissions.commons.io.importer;

public class SemiColonDelimitedTokenizer implements Tokenizer {

    private DelimitedTokenizer delegate;
    
    private int numOfDelimiter;
    
    private boolean initialized = false;

    public SemiColonDelimitedTokenizer() {
        String pattern =  "[^;]+";
        delegate = new DelimitedTokenizer(pattern);
    }

    public String[] tokens(String input) throws ImporterException {
        String[] tokens = delegate.doTokenize(padding(input));
        
        if (!initialized) {
            numOfDelimiter = tokens.length;
            initialized = true;
            return tokens;
        }
            
        if (initialized && tokens.length != numOfDelimiter && tokens.length < 2) {
            throw new ImporterException("Could not find " + --numOfDelimiter + " of \';\' delimiters on the line.");
        }
        
        return tokens;
    }

    private String padding(String input) {
        input = input.trim();
        if (input.startsWith(";"))
            input = ";" + input;
        if (input.endsWith(";"))
            input = input + ";";
        input = input.replaceAll(";;", "; ;");
        input = input.replaceAll(";;", "; ;");// called for second time if odd number of delimiter present

        return input;
    }

    public String delimiter() {
        return ";";
    }
    
}
