package gov.epa.emissions.framework.services.data;


public class DatasetNote {

    private int id;

    private int datasetId;
    
    private int version;

    private Note note;

    public DatasetNote() {// No argument constructor needed for hibernate mapping
    }
    
    public DatasetNote(int datasetId, Note note){
        this.datasetId = datasetId;
        this.note = note;
    }

    public String toString() {
        return note.toString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof DatasetNote)) {
            return false;
        }
        DatasetNote other = (DatasetNote) obj;
        return ((this.datasetId == other.datasetId) && (note.equals(other.getNote())) );
 //       return (id == other.getId()) ;
    }

    public void setDatasetId(int datasetId) {
        this.datasetId = datasetId;
    }

    public int getDatasetId() {
        return datasetId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
    
}
