

package gov.epa.mims.analysisengine.gui;
import javax.swing.JRootPane;
import javax.swing.JButton;
import java.awt.event.ActionListener;

/*
 * OKCancelHelpPanel.java
 * @author  parthee
 */
public class OKCancelHelpPanel extends OKCancelPanel
{
   
   /** Creates a new instance of OKCancelHelpPanel */
   public OKCancelHelpPanel(ActionListener okListener, 
      ActionListener cancelListener, ActionListener helpListener, 
      JRootPane rootPane)
   {
       super(okListener, cancelListener, rootPane);
       JButton helpButton = new JButton("Help");
       helpButton.addActionListener(helpListener);
       add(helpButton);
   }
   
}
