package gov.epa.mims.analysisengine.gui;

import javax.swing.*;
import java.awt.Event;
import java.awt.event.*;

/**
 *
 *
 * @author Prashant Pai, CEP UNC
 * @version $Id: OKCancelPanel.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 */

public class OKCancelPanel
    extends JPanel
{
  public OKCancelPanel(ActionListener okListener, ActionListener cancelListener,
                       JRootPane rootPane)
  {
    JButton okButton = new JButton("OK");
    okButton.addActionListener(okListener);
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(cancelListener);
    okButton.setMnemonic('O');
    cancelButton.setMnemonic('l');
    add(okButton);
    add(cancelButton);
  }
  
}

