package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.Record;

import java.util.List;

public class TerminatorRecord extends Record {

    public boolean isEnd() {
        return true;
    }

    public void add(List list) {
        // No Op
    }

    public void add(String token) {
        // No Op
    }

    public int size() {
        return 0;
    }

    public String token(int position) {
        return null;
    }

}
