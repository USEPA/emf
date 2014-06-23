package gov.epa.emissions.framework.services.cost;

import java.io.Serializable;

public class ControlMeasureMonth implements Serializable {
    private int id;

    private short month;
    
    public ControlMeasureMonth() {
        //
    }

    public short getMonth() {
        return month;
    }

    public void setMonth(short month) {
        this.month = month;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ControlMeasureMonth)) {
            return false;
        }

        ControlMeasureMonth other = (ControlMeasureMonth) obj;

        return (id == other.getId() && month == other.getMonth());
    }

    public int hashCode() {
        return toString().hashCode();
    }
    
    public String toString() {
        String name = "";
        switch (this.month) {
        case -1:
            name = "None";
            break;
        case 0:
            name = "All Months";
            break;
        case 1:
            name = "January";
            break;
        case 2:
            name = "February";
            break;
        case 3:
            name = "March";
            break;
        case 4:
            name = "April";
            break;
        case 5:
            name = "May";
            break;
        case 6:
            name = "June";
            break;
        case 7:
            name = "July";
            break;
        case 8:
            name = "August";
            break;
        case 9:
            name = "September";
            break;
        case 10:
            name = "October";
            break;
        case 11:
            name = "November";
            break;
        case 12:
            name = "December";
            break;
        default:
            break;
        }
        return name;
    }
}
