package gov.epa.emissions.commons.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatabaseRecord {

    private List tokens;

    public DatabaseRecord() {
        this.tokens = new ArrayList();
    }

    public Object token(int position) {
        return tokens.get(position);
    }

    public int size() {
        return tokens.size();
    }

    public void add(Object token) {
        tokens.add(token);
    }

    public List tokens() {
        return tokens;
    }

    public Object[] getTokens() {
        return tokens.toArray();
    }

    public void setTokens(Object[] objects) {
        tokens.clear();
        tokens.addAll(Arrays.asList(objects));
    }

    /**
     * Replace the token at the position with the new token
     */
    public void replace(int position, Object newToken) {
        tokens.add(position, newToken);
        tokens.remove(position + 1);
    }
    
    public void print() {
        for (int i=0; i<size(); i++) {
            Object obj = token(i);
            if ( obj == null)
                System.out.println( i + "> null");
            else
                System.out.println( i + "> class: " + obj.getClass() + ", value: " + obj);
        }
    }

}
