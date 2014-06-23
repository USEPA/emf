package gov.epa.emissions.framework.ui;

import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

public class ImageResources {

    private ResourceBundle bundle;

    public ImageResources() {
        bundle = ResourceBundle.getBundle("images");
    }

    public ImageIcon refresh(String tooltip, int size) {
        return image("refresh"+size, tooltip);
    }

    public ImageIcon trash(String tooltip) {
        return image("trash", tooltip);
    }

    public ImageIcon open(String tooltip) {
        return image("open", tooltip);
    }

    private ImageIcon image(String alias, String tooltip) {
        URL url = ImageResources.class.getResource(bundle.getString(alias));
        return new ImageIcon(url, tooltip);
    }

    public ImageIcon prev(String tooltip) {
        return image("prev", tooltip);
    }

    public ImageIcon next(String tooltip) {
        return image("next", tooltip);
    }

    public ImageIcon first(String tooltip) {
        return image("first", tooltip);
    }

    public ImageIcon last(String tooltip) {
        return image("last", tooltip);
        }
}
