package gov.epa.emissions.commons.data;

import java.io.Serializable;
import java.util.Date;

public interface Dataset extends Serializable {
    
    public static final String inline_comment_key = "EXPORT_INLINE_COMMENTS";
    
    public static final String header_comment_key = "EXPORT_HEADER_COMMENTS";
    
    public static final String inline_comment_char = "INLINE_COMMENT_CHAR";
    
    public static final String header_comment_char = "HEADER_COMMENT_CHAR";
    
    public static final String csv_header_line = "EXPORT_COLUMN_LABEL";
    
    public static final String head_required = "REQUIRED_HEADER";
    
    public static final String csv_header_line_value = "ALL_CAPS";
    
    public static final int upper_case = 2;
    
    public static final int lower_case = 1;
    
    public static final int no_head_line = 0;
    
    public int getId();

    public void setId(int id);

    public String getName();

    public void setName(String name);

    // bean-style properties
    void setCreator(String creator);// TODO: use User instead

    String getCreator();

    String getDatasetTypeName();

    void setDatasetType(DatasetType datasetType);

    DatasetType getDatasetType();

    void setUnits(String units);

    String getUnits();

    void setRegion(Region region);

    Region getRegion();

    Country getCountry();

    void setCountry(Country country);

    void setProject(Project project);

    Project getProject();

    void setYear(int year);

    int getYear();

    // FIXME: use the TemporalResolution instead
    void setTemporalResolution(String name);

    String getTemporalResolution();

    void setStartDateTime(Date time);

    Date getStartDateTime();

    void setStopDateTime(Date time);

    Date getStopDateTime();

    String getDescription();

    void setDescription(String description);

    // Datasets for non-external files will have a list of
    // Internal Source objects. An internal source object will
    // contain details of each table name/table type/ source/ size
    InternalSource[] getInternalSources();

    void setInternalSources(InternalSource[] internalSources);

    void addInternalSource(InternalSource source);

    void setSummarySource(InternalSource summary);

    public InternalSource getSummarySource();
    
    boolean getInlineCommentSetting();
    
    boolean getHeaderCommentsSetting();

    int getCSVHeaderLineSetting();
    
    String getInlineCommentChar();
    
    String getHeaderCommentChar();

}
