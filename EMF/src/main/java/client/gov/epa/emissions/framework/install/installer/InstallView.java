package gov.epa.emissions.framework.install.installer;

import java.awt.Cursor;


public interface InstallView {
    void observe(InstallPresenter presenter);

    void close();

    void display();
    
    void setStatus(String status);
    
    void setCursor(Cursor cursor);
    
    void displayErr(String err);
    
    void setFinish();

}
