package gov.epa.emissions.commons.io.importer;

public interface Tokenizer {
    
    static final String SINGLE_QUOTED_TEXT = "('(.)*')";

    static final String DOUBLE_QUOTED_TEXT = "(\"(.)*\")";

    static final String INLINE_COMMENTS = "!(.)*";

    String[] tokens(String input) throws ImporterException;
    
    String delimiter();

}