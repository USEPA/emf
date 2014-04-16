package gov.epa.emissions.commons.io.importer;

public class DelimiterIdentifyingTokenizer implements Tokenizer {

    private int minTokens;
    
    private int numOfDelimiters;
    
    private String delimiter;

    private Tokenizer tokenizer;

    private boolean initialized = false;
    
    public DelimiterIdentifyingTokenizer(int minTokens) {
        this.minTokens = minTokens;
    }
    
    public String delimiter() {
        return this.delimiter;
    }

    public String[] tokens(String input) throws ImporterException {
        if (!initialized)
            identifyTokenizer(input);
        
        if (initialized)
            checkDelimiters(input);
            
        return tokenizer.tokens(input);
    }
    
    private void identifyTokenizer(String input) throws ImporterException {
        initialized = true;

        Tokenizer commaTokenizer = commaTokenizer(input);
        if (commaTokenizer != null) {
            tokenizer = commaTokenizer;
            return;
        }

        Tokenizer semiColonTokenizer = semiColonTokenizer(input);
        if (semiColonTokenizer != null) {
            tokenizer = semiColonTokenizer;
            return;
        }

        Tokenizer whiteSpaceTokenizer = whitespaceTokenizer(input);
        if (whiteSpaceTokenizer != null) {
            tokenizer = whiteSpaceTokenizer;
            return;
        }

        throw new ImporterException(" Could not identify the delimiter. " +
        		"The number of columns should be more than: " + minTokens  );
//        		"\nInput line: " + (input.length()>20? input.substring(0, 20)+"...":input) );

    }

    private Tokenizer commaTokenizer(String input) throws ImporterException {
        Tokenizer commaTokenizer = new CommaDelimitedTokenizer();
        try {
            String[] tokens = commaTokenizer.tokens(input);
            if (tokens.length >= minTokens) {
                delimiter = ",";
                numOfDelimiters = tokens.length;
                return commaTokenizer;
            }
            return null;
        } catch (IllegalStateException e) {
            return null;
        }
    }

    private Tokenizer semiColonTokenizer(String input) throws ImporterException {
        Tokenizer semiColonTokenizer = new SemiColonDelimitedTokenizer();
        try {
            String[] tokens = semiColonTokenizer.tokens(input);
            if (tokens.length >= minTokens) {
                delimiter = ";";
                numOfDelimiters = tokens.length;
                return semiColonTokenizer;
            }
            return null;
        } catch (IllegalStateException e) {
            return null;
        }
    }

    private Tokenizer whitespaceTokenizer(String input) throws ImporterException {
        Tokenizer whiteSpaceTokenizer = new WhitespaceDelimitedTokenizer();
        try {
            String[] tokens = whiteSpaceTokenizer.tokens(input);
            if (tokens.length >= minTokens) {
                delimiter = " ";
                numOfDelimiters = tokens.length;
                return whiteSpaceTokenizer;
            }
            return null;
        } catch (IllegalStateException e) {
            return null;
        }
    }
    
    private void checkDelimiters(String input) throws ImporterException {
        String[] tokens = tokenizer.tokens(input);
        
        if (tokens.length != numOfDelimiters && tokens.length < minTokens)
            throw new ImporterException("Could not find " + --numOfDelimiters + " of  \'"
                    + delimiter + "\' delimiters on the line.");
    }
}
