package gov.epa.emissions.framework.services.cost.controlmeasure;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name="control_measure_sccs", schema="emf")
public class Scc implements Serializable  {

    @Id
//    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "control_measure_scc_generator")
    @SequenceGenerator(name="control_measure_scc_generator", sequenceName = "control_measure_sccs_id_seq", allocationSize=1)
    private Integer id;

    @Column(name="control_measures_id", nullable=false, unique=false)
    private Integer controlMeasureId;

    @Column(name="name", nullable=false, unique=false)
    private String code;

    @Transient
    private String description;

    @Column(name="status", nullable=true, unique=false)
    private String status;
    
    @Transient
    private String sector,ei_category,scc_l1,scc_l2,scc_l3,scc_l4,last_inventory_year,map_to,created_date,revised_date,option_group,option_set,short_name;
    
    @Column(name="combustion_efficiency", nullable=true, unique=false)
    private Float combustionEfficiency;
    		
    public Scc() {
        // Empty
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getControlMeasureId() {
        return controlMeasureId;
    }

    public void setControlMeasureId(Integer controlMeasureId) {
        this.controlMeasureId = controlMeasureId;
    }

    public Scc(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public Scc(String code, String description, String sector, String eiCategory, String sccL1,
            String sccL2, String sccL3, String sccL4, String lastInventoryYear, String mapTo, String createdDate,
            String revisedDate, String optionGroup, String optionSet, String shortName) {
        this(code, description);
        this.sector = sector;
        ei_category = eiCategory;
        scc_l1 = sccL1;
        scc_l2 = sccL2;
        scc_l3 = sccL3;
        scc_l4 = sccL4;
        last_inventory_year = lastInventoryYear;
        map_to = mapTo;
        created_date = createdDate;
        revised_date = revisedDate;
        option_group = optionGroup;
        option_set = optionSet;
        short_name = shortName;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // scc id field is not compared=> id initial values are zero so two new sccs with different codes will be equal
    // before persisting
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Scc))
            return false;
        Scc other = (Scc) obj;
        return id == other.getId() 
                || (code.equals(other.getCode()) && controlMeasureId == other.getControlMeasureId());
    }

    public int hashCode() {
        return code.hashCode();
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getSector() {
        return sector;
    }

    public void setEi_category(String ei_category) {
        this.ei_category = ei_category;
    }

    public String getEi_category() {
        return ei_category;
    }

    public void setScc_l1(String scc_l1) {
        this.scc_l1 = scc_l1;
    }

    public String getScc_l1() {
        return scc_l1;
    }

    public void setScc_l2(String scc_l2) {
        this.scc_l2 = scc_l2;
    }

    public String getScc_l2() {
        return scc_l2;
    }

    public void setScc_l3(String scc_l3) {
        this.scc_l3 = scc_l3;
    }

    public String getScc_l3() {
        return scc_l3;
    }

    public void setScc_l4(String scc_l4) {
        this.scc_l4 = scc_l4;
    }

    public String getScc_l4() {
        return scc_l4;
    }

    public void setLast_inventory_year(String last_inventory_year) {
        this.last_inventory_year = last_inventory_year;
    }

    public String getLast_inventory_year() {
        return last_inventory_year;
    }

    public void setMap_to(String map_to) {
        this.map_to = map_to;
    }

    public String getMap_to() {
        return map_to;
    }

    public void setCreated_date(String created_date) {
        this.created_date = created_date;
    }

    public String getCreated_date() {
        return created_date;
    }

    public void setRevised_date(String revised_date) {
        this.revised_date = revised_date;
    }

    public String getRevised_date() {
        return revised_date;
    }

    public void setOption_group(String option_group) {
        this.option_group = option_group;
    }

    public String getOption_group() {
        return option_group;
    }

    public void setOption_set(String option_set) {
        this.option_set = option_set;
    }

    public String getOption_set() {
        return option_set;
    }

    public void setShort_name(String short_name) {
        this.short_name = short_name;
    }

    public String getShort_name() {
        return short_name;
    }

    public Float getCombustionEfficiency() {
        return combustionEfficiency;
    }

    public void setCombustionEfficiency(Float combustionEfficiency) {
        this.combustionEfficiency = combustionEfficiency;
    }

}
