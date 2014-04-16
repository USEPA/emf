package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;

public class CaseSelectionDialog extends JDialog implements CaseSelectionView {
    private TextField name; 
    private JList caseList;
    private String[] filteredCases;
    private String[] allCases;
    private String[] selectedCases;
    private boolean shouldCopy = false; 
    
    private EmfConsole parent;
//    private DesktopManager desktopManager;
    
    public CaseSelectionDialog(EmfConsole parent, String[] nameIds){           
        super(parent);
        super.setIconImage(EmfImageTool.createImage("/toolbarButtonGraphics/general/Copy24.gif"));
        setModal(true);
//        this.desktopManager = desktopManager;
        this.parent = parent; 
        this.allCases=nameIds;
    }
    
    public void display(String title, boolean selectSingle) {
        this.filteredCases =allCases;
        //caseList.setListData(nameIds);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(5, 5));
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(buildTopPanel(), BorderLayout.NORTH);
        panel.add(buildCasePanel(selectSingle), BorderLayout.CENTER);
        panel.add(buttonPanel(), BorderLayout.SOUTH);
        contentPane.add(panel);
           setTitle(title);  
        this.pack();
        this.setSize(500, 400);
        this.setLocation(ScreenUtils.getPointToCenter(this));
        this.setVisible(true);
    }
    
    private JPanel buildTopPanel(){
        JPanel panel = new JPanel ();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(buildNameContains());
        return panel; 
    }
    private JPanel buildNameContains(){
        JPanel panel = new JPanel(new SpringLayout()); 
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        name= new  TextField ("Case name contains", "", 25);
        name.setEditable(true);
        name.setToolTipText("Case name filter: Press enter to refresh");
        name.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                refresh();
            }
        });
        
        layoutGenerator.addLabelWidgetPair("Name contains:  ", name, panel);
        layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
                25, 10, // initialX, initialY
                5, 5);// xPad, yPad
        return panel; 
    }
    
    private JPanel buildCasePanel(boolean selectSingle) {
        caseList = new JList();
        if (selectSingle)
            caseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        else
            caseList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollPane = new JScrollPane(caseList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(500, 300));
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(scrollPane);
        refresh();
        return panel;
    }

    
    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        panel.add(new OKButton(okAction()));
        panel.add(new CancelButton(cancelAction()));
        return panel;
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (caseList.getSelectedValues() == null || caseList.getSelectedValues().length == 0) {
                    selectedCases = new String[]{}; 
                    JOptionPane.showMessageDialog(parent, 
                        "Please choose a case", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }else {
                    setSelectedCases();
                }
                if (selectedCases.length>0)
                    shouldCopy = true; 
                setVisible(false);
                dispose();
            }
        };
    }
    
    private void setSelectedCases(){
        List<String> list = new ArrayList<String>(caseList.getSelectedValues().length);
        for (int i = 0; i < caseList.getSelectedValues().length; i++)
            list.add((String) caseList.getSelectedValues()[i]);
        selectedCases = list.toArray(new String[0]);
    }
    
    public String[] getCases(){
        return selectedCases;
    }
    
    private void refresh(){
        setFiltedCases();
        caseList.setListData(filteredCases);
    }
    
    private void setFiltedCases(){
        List<String> list = new ArrayList<String>();
        String nameContains=name.getText().trim();
        if (nameContains.equals(""))
            filteredCases=allCases;
        else {
            for (int i = 0; i < allCases.length; i++){
                if (allCases[i].toLowerCase().contains(nameContains.toLowerCase()))
                    list.add(allCases[i]);
            }
            filteredCases = list.toArray(new String[0]);
        }
    }

    private Action cancelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        };
    }
    
    public boolean shouldCopy() {
        return shouldCopy;
    }
}
