package gov.epa.emissions.commons.io.importer;

public class WhitespaceDelimitedTokenizer implements Tokenizer {

    private static final String ANY_CHAR_EXCEPT_WHITESPACE = "[\\S]+";
    
    private DelimitedTokenizer delegate;
    
    private int numOfDelimiter;
    
    private boolean initialized = false;

    public WhitespaceDelimitedTokenizer() {
        //String pattern = INLINE_COMMENTS + "|" + ANY_CHAR_EXCEPT_WHITESPACE;
        String pattern = ANY_CHAR_EXCEPT_WHITESPACE;
        delegate = new DelimitedTokenizer(pattern);
    }

    // whitespace includes space & tabs
    public String[] tokens(String input) throws ImporterException {
        String[] tokens = delegate.doTokenize(input);
        
        if (!initialized) {
            numOfDelimiter = tokens.length;
            initialized = true;
            return tokens;
        }
            
        if (initialized && tokens.length != numOfDelimiter && tokens.length < 2) {
            throw new ImporterException("Could not find " + --numOfDelimiter + " of \' \' delimiters on the line.");
        }
        
        return tokens;
    }

    public String delimiter() {
        return " ";
    }

}
