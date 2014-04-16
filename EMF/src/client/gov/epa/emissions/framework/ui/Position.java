package gov.epa.emissions.framework.ui;

public class Position {

    private int x;

    private int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean equals(Object otherObj) {
        if (otherObj == null || !(otherObj instanceof Position))
            return false;

        Position other = (Position) otherObj;
        return this.x == other.x && this.y == other.y;
    }

    public int hashCode() {
        return super.hashCode();
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

}
