package gov.epa.emissions.framework.client.casemanagement.sensitivity;


public class SetCaseObject{
    private Object object; 
    private String wizardType;
    public static final String WIZARD_PATH = "PATH";
    public static final String WIZARD_INPUT = "INPUT";
    public static final String WIZARD_PARA = "PARAMETER";
    public SetCaseObject(Object object, String wizardType){
        this.object=object;
        this.wizardType=wizardType;
    }
    
    public void setObject(Object object){
        this.object = object;
    }
    
    public void setIsInput(String wizardType){
        this.wizardType = wizardType;
    }
    
    public Object getObject(){
        return object;
    }
    
    public String getWizardType(){
        return wizardType;
    }
    
}