package gov.epa.emissions.framework.client.meta;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.ImageIcon;

public class EmfImageTool {

    public static Image createImage(String path) {
        return Toolkit.getDefaultToolkit().getImage(Object.class.getClass().getResource(path));
    }
    
    public static ImageIcon createImageIcon(String path) {
        URL imgURL = Object.class.getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        }
        return null;
    }
}
