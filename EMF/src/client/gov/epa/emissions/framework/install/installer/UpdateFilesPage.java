package gov.epa.emissions.framework.install.installer;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class UpdateFilesPage extends Box {
    private JPanel parent;
    
    private JTextArea updateTextArea;
    
    private InstallPresenter presenter;
    
    private String downloadPage, dirsPage;
    
    private JButton ok, cancel;
    
    public UpdateFilesPage(JPanel parent) {
        super(BoxLayout.Y_AXIS);
        this.parent = parent;
        initialize();
    }
    
    public void setDownloadPage(String downloadPage) {
        this.downloadPage = downloadPage;
    }
    
    public void setDirsPage(String dirsPage) {
        this.dirsPage = dirsPage;
    }
    
    private void initialize() {
        JPanel upper = new JPanel(new BorderLayout());
        Box buttons = new Box(BoxLayout.X_AXIS); 
        
        updateTextArea = new JTextArea(5, 50);
        updateTextArea.setBackground(new Color(211,211,211));
        updateTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(updateTextArea);
        upper.setPreferredSize(new Dimension(450, 110));
        upper.add(scrollPane, BorderLayout.CENTER);
        
        ok = new JButton("Update");
        ok.setMnemonic(KeyEvent.VK_U);
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CardLayout cl = (CardLayout)(parent.getLayout());
                cl.show(parent, downloadPage);
                presenter.downloadUpdates();
            }
        });
        ok.setEnabled(false);

        cancel = new JButton("Cancel");
        cancel.setMnemonic(KeyEvent.VK_L);
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CardLayout cl = (CardLayout)(parent.getLayout());
                cl.show(parent, dirsPage);
            }
        });
        
        buttons.setBorder(BorderFactory.createEmptyBorder(30, 10, 20, 80));
        buttons.add(Box.createHorizontalGlue());
        buttons.add(ok);
        buttons.add(Box.createRigidArea(new Dimension(20, 0)));
        buttons.add(cancel);

        add(upper);
        add(buttons);
    }
    
    public void display(String text) {
        updateTextArea.setText(text);
    }
    
    public void enableUpdate() {
        ok.setEnabled(true);
    }
    
    public void observe(InstallPresenter presenter) {
        this.presenter = presenter;
    }

}
