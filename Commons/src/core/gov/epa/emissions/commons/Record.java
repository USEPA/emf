package gov.epa.emissions.commons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Record {

    private List<String> tokens;

    public Record() {
        this.tokens = new ArrayList<String>();
    }
    
    public Record(String[] tokens) {
        this();
        setTokens(tokens);
    }

    public String token(int position) {
        return tokens.get(position);
    }

    public int size() {
        return tokens.size();
    }

    public void add(String token) {
        tokens.add(token);
    }

    public void add(List<String> list) {
        tokens.addAll(list);
    }

    public boolean isEnd() {
        return false;
    }

    public List<String> tokens() {
        return tokens;
    }

    public String toString() {
        return tokens.toString();
    }

    public String[] getTokens() {
        return tokens.toArray(new String[0]);
    }

    public void setTokens(String[] tokensList) {
        tokens.clear();
        if (tokensList != null) tokens.addAll(Arrays.asList(tokensList));
    }

    /**
     * Replace the token at the position with the new token
     */
    public void replace(int position, String newToken) {
        tokens.add(position, newToken);
        tokens.remove(position + 1);
    }

}
