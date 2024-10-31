package gov.epa.emissions.framework.services.data;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "datasets_notes", schema = "emf")
public class DatasetNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "dataset_id", updatable = false, insertable = true, nullable = false)
    private int datasetId;
    
    @Column(name = "version", updatable = false, insertable = true, nullable = false)
    private int version;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST, optional = false)
    @JoinColumn(name="note_id")
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
