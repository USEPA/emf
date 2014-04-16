package gov.epa.emissions.framework.services.basic;

import java.io.Serializable;
import java.util.Date;

public class FileDownload implements Serializable {

    public enum DownloadStatus {
        COMPLETED, DOWNLOADING, ERROR, IDLE;
    }
    
    private int id;

    private int userId;

    private String type;

    private String message = "";

    private String absolutePath;

    private boolean read;
    
    private boolean overwrite;
    
    private Date timestamp;

    private String url;
    private int progress;
    private long size ;

    public FileDownload() {// needed for serialization
    }

    public boolean getRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean getOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String messageType) {
        this.type = messageType;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

//    public String toString() {
//        return "Message : " + message + " for user: " + username;
//    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getFileName() {
        return url.substring(url.lastIndexOf("/")+1, url.length());
    }

    @Override
    public String toString() {
        return id + ";" + type + ";" + absolutePath + ";" + url;// + ";" + status;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof FileDownload) {
            FileDownload fileDownload = (FileDownload)obj;
            
            if (id == fileDownload.getId()) {
                return true;
            }
            return false;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return (id + "").hashCode();
    }

    public Integer getProgress() {
        // NOTE Auto-generated method stub
        return progress;
    }

    public void setProgress(Integer progress) {
        // NOTE Auto-generated method stub
        this.progress = progress;
    }

    public long getSize() {
        return this.size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
