package gov.epa.emissions.commons.gui;

public class EmptyStrings {

    public static String create(int num) {
        String temp = "";
        
        for (int i = 0; i < num; i++)
            temp += " ";
        
        return temp;
    }
}
