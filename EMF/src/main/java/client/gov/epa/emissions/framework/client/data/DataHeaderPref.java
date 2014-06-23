package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.client.preference.DefaultUserPreferences;

import java.io.IOException;

@SuppressWarnings("serial")
public class  DataHeaderPref {

    private Boolean resetView = false;
    private String dstName;
    private String dtOrderName;
    private String dtHideName;
    private static String orderValue = "";
    private static String hideValue = "";
    private String[] orderedColNames;
    private String[] hideColNames;
    private DefaultUserPreferences userPref;
//    private String hideKey;

    public DataHeaderPref(Boolean resetView, String dstName){
        this.resetView = resetView;
        this.dstName = dstName;
        this.dtOrderName = dstName.replace(" ", "_") + "_column_order";
        this.dtHideName = dstName.replace(" ", "_") + "_column_hidden";
        iniPref(); 
    }
    
    private void iniPref(){
        try {
            this.userPref = new DefaultUserPreferences();
        } catch (IOException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();                  
        }
        
        orderValue = userPref.property(dtOrderName);
        hideValue = userPref.property(dtHideName);
        orderedColNames = setupNames(orderValue);
        hideColNames = setupNames(hideValue);
    }
    
    private String[] setupNames(String value){
        if (value != null && value != "")
            return value.split(",");
        return null;
    }
    
    public boolean getResetView() {
        return this.resetView;
    }
    
    public void setHideCols(Boolean resetView) {
        this.resetView = resetView;
    }
    
    public String getDstName() {
        return this.dstName;
    }
    
    public void setDstName(String dstName) {
        this.dstName = dstName;
    }
    
    public String[] getOCols() {
        return this.orderedColNames;
    }
    
    public void setOrderCols(String[] colNames) {
        this.orderedColNames = colNames; 
        
        String outstring = "";
        if ( colNames != null )
            outstring = colsToString(orderedColNames);
        try {
            userPref.setPreference(dtOrderName, outstring);
        } catch (IOException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
    }

    
    public String[] getHCols() {
        return this.hideColNames;
    }
    
    public void setHCols(String[] colNames) {
        this.hideColNames = colNames;
        
        String outstring = ""; 
        if ( colNames != null )
            outstring = colsToString(hideColNames);
        try {
            userPref.setPreference(dtHideName, outstring);
        } catch (IOException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }     
    }
    
    public static String colsToString(String[] colNames){
        String outString = "";
        if ( colNames != null && colNames.length > 0 ){
            for ( int i =0; i < colNames.length; i++ ){
                outString = outString + "," + colNames[i].trim();
            }
            outString = outString.substring(1, outString.length());
        }
        return outString;
    }
    
}
