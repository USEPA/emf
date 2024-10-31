package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.security.User;

import java.util.Date;

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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "notes", schema = "emf")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name", updatable = true, insertable = true, nullable = false, length = 10)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name="creator")
    private User creator;

    @Temporal(TemporalType.DATE)
    @Column(name = "date_time", updatable = true, insertable = true, nullable = false)
    private Date date;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name="type")
    private NoteType noteType;

    @Column(name = "details", updatable = true, insertable = true, nullable = false)
    private String details;

    @Column(name = "refers_notes", updatable = true, insertable = true, nullable = false)
    private String references;

    public Note() {// No argument constructor needed for hibernate mapping
    }

    public String toString() {
        return name + " (" + id + ")";
    }

    public Note(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Note(User creator, int datasetId, Date date, String details, String name, NoteType type, String references,
            int version) {
        this.creator = creator;
        this.date = date;
        this.details = details;
        this.name = name;
        noteType = type;
        this.references = references;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NoteType getNoteType() {
        return noteType;
    }

    public void setNoteType(NoteType noteType) {
        this.noteType = noteType;
    }

    public String getReferences() {
        return references;
    }

    public void setReferences(String references) {
        this.references = references;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Note)) {
            return false;
        }
        Note other = (Note) obj;
        return (id == other.getId()) ;
    }
}
