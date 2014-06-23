package gov.epa.emissions.framework.services.data;

/**
 * This class keeps track of the date/time a user initiated an export of a particular version of a dataset to a
 * repository (location).
 */
public class NoteType {

    private int id;

    private String type;

    public NoteType() {// No argument constructor needed for hibernate mapping
    }

    public NoteType(String type) {
        super();
        setType(type);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
