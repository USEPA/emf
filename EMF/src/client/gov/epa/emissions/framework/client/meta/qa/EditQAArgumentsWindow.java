package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import javax.swing.border.EtchedBorder;


public class EditQAArgumentsWindow extends DisposableInteralFrame implements EditQAArgumentsView{
    
    private JTextArea arguments;
    
    private JPanel layout;
    
    private String textAreaArguments;

    private EditQAArgumentsPresenter presenter;
    
    public EditQAArgumentsWindow(DesktopManager desktopManager, String textAreaArguments) {
        
        super("Argument Editor", new Dimension(750, 350), desktopManager);
        this.textAreaArguments = textAreaArguments;
        this.getContentPane().add(createLayout());
    }

  public void display(EmfDataset dataset, QAStep qaStep) {
      super.setTitle("Edit QA Step Arguments: " + qaStep.getName() + "_" + qaStep.getId()+" ("+dataset.getName()+")");
      super.display();
  }
  
  public void observe(EditQAArgumentsPresenter presenter) {
      this.presenter = presenter;
  }
  
    public JPanel createLayout() {
        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        
        JPanel pnlArgument = new JPanel();
        pnlArgument.setLayout(new BoxLayout(pnlArgument, BoxLayout.X_AXIS));
        
        JLabel lblArgument = new JLabel("Arguments:    ");
        pnlArgument.add( lblArgument);
        
        arguments = new JTextArea();
        arguments.setWrapStyleWord(true);
        arguments.setLineWrap(true);
        arguments.setText(textAreaArguments);
        JScrollPane scrollPane = new JScrollPane( arguments);
        scrollPane.setPreferredSize(new Dimension(650,250));
        pnlArgument.add( scrollPane);
        
        pnlArgument.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        //pnlArgument.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));

        layout.add( pnlArgument);
        layout.add(buttonPanel());
        
        return layout;
    }

    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(25);
        panel.setLayout(layout);
        panel.add(new OKButton(okAction()));
        panel.add(new CancelButton(cancelAction()));
        panel.setMaximumSize(new Dimension(1000,80));
        return panel;
    }

    private Action cancelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dispose();
                disposeView();
            }

        };
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                String argumentsText = arguments.getText();
                // Send the modified text from the arguments text area to the one in
                // the QAStepWindow arguments text area.
                presenter.refreshArgs(argumentsText);
                dispose();
                disposeView();
            }
        };
    }
}
