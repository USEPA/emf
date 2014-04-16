package gov.epa.emissions.commons.db.version;

public class ScrollableResultSetIndex {
    private int start = 0;

    private int FETCH_SIZE;
    private int PAGE_SIZE;

    public ScrollableResultSetIndex(int batchSize, int pageSize){
        this.FETCH_SIZE= batchSize; 
        this.PAGE_SIZE=pageSize;
    }
    public int start() {
        return start;
    }

    public int newStart(int index) {
        int steps = (index / FETCH_SIZE);
        start = (steps * FETCH_SIZE);
        return start;
    }

    public int end() {
        return (start + FETCH_SIZE + PAGE_SIZE);  // batch size is 10300 from N*, emf.properties
    }

    public boolean inRange(int index) {
        return (start <= index) && (index < end());
    }

    public int relative(int index) {
        return index % FETCH_SIZE;
    }

}
