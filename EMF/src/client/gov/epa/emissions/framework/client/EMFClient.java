package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.client.login.LoginPresenter;
import gov.epa.emissions.framework.client.login.LoginWindow;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.client.transport.ServiceLocator;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;

public class EMFClient {
    private static final String DEFAULT_URL = "http://localhost:8080/emf/services";// default

    public static void main(final String[] args) throws Exception {
        if (args.length == 1 && args[0].equalsIgnoreCase("Help")) {
            displayHelp();
            return;
        }

               run(args);
        }

    private static void displayHelp() {
        System.out.println("Usage\njava "
                        + EMFClient.class.getName()
                        + " [url]"
                        + "\n\turl - location of EMF Services. Defaults to "
                        + DEFAULT_URL
                        + "\n\tspecify '-DUSER_PREFERENCES=<full path to EMFPrefs.txt>' to override location of User Preferences");
    }

    private static void run(final String[] args) throws Exception {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
       try
      {
        String url = DEFAULT_URL;
        System.setProperty("emf.remote.host", "localhost");
        UIManager.put("Button.focus", new Color(51, 82, 107));

        if (args.length == 1) {
            url = args[0];
            setHost(url);
        }

        System.out.println("Starting EMF Client");
        ServiceLocator serviceLocator = new RemoteServiceLocator(url);

        System.out.println("Creating Login Window");
        LoginWindow view = new LoginWindow(serviceLocator);
        view.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        LoginPresenter presenter = new LoginPresenter(serviceLocator.userService());
        System.out.println("Displaying Login Window");
        presenter.display(view);
      }
      catch (Exception exc)
      {
          System.out.println("Exception starting client: "+exc.getMessage());
          exc.printStackTrace();
//          throw exc;
      }
            }
        });
    }
    
    private static void setHost(String url) {
        int start = url.indexOf("://");
        String temp = url.substring(start+3);
        int end = temp.indexOf(":");
        
        if (end < 0)
            end = temp.indexOf("/");
        
        System.setProperty("emf.remote.host", temp.substring(0, end));
    }

}
