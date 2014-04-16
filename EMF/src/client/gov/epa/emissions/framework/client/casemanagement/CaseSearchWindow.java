package gov.epa.emissions.framework.client.casemanagement;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;

import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseCategory;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

public class CaseSearchWindow extends JDialog implements CaseSearchView {
    
    private EmfConsole parent;
    
    private CaseSearchPresenter presenter;
    
    private ComboBox caseCategoryCombo;
    
    private JList caseList;
    
    private Case[] cases = new Case[] {};
    
    private boolean shouldCreate = false;

    private TextField nameFilter;
    
    String lastNameContains;
    
    private static CaseCategory lastCaseCategory = null;

    public CaseSearchWindow(EmfConsole parent) {
        super(parent);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
        this.parent = parent;
        setModal(true);
    }
    

    public void display(CaseCategory[] categories) {
        display(categories, null, false);
    }
    
    public void display(CaseCategory[] caseCategories, CaseCategory defaultCategory, boolean selectSingle) {

      if ((defaultCategory != null) && (lastCaseCategory != null) && 
         !defaultCategory.getName().equals(lastCaseCategory.getName()))
      {
          lastNameContains = null;
          lastCaseCategory = null;
      }
      Container contentPane = getContentPane();
      contentPane.setLayout(new BorderLayout(5, 5));
      JPanel panel = new JPanel(new BorderLayout(10, 10));
      panel.add(buildTopPanel(caseCategories, defaultCategory), BorderLayout.NORTH);
      panel.add(buildCasesPanel(selectSingle), BorderLayout.CENTER);
      panel.add(buttonPanel(), BorderLayout.SOUTH);
      contentPane.add(panel);
      if (caseCategories.length == 1)
      {
          setTitle("Select "+caseCategories[0].getName()+" Cases");           
      }
      else
      {
         setTitle("Select Cases");
      }   
      this.pack();
      this.setSize(500, 400);
      this.setLocation(ScreenUtils.getPointToCenter(this));
      this.setVisible(true);
  }
    
    private JPanel buildTopPanel(CaseCategory[] caseCatogeries, CaseCategory defaultCategory){
        JPanel panel = new JPanel ();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(buildNameContains());
        panel.add(buildCaseCategoryCombo(caseCatogeries, defaultCategory));
        return panel; 
    }
    
    private JPanel buildNameContains(){
        JPanel panel = new JPanel(new SpringLayout()); 
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        
        String defaultName = ((lastNameContains == null) ? "" : lastNameContains);
        nameFilter= new TextField("Case name contains", defaultName, 25);
        nameFilter.setEditable(true);
        nameFilter.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                lastNameContains = nameFilter.getText();
                refresh();
            }
        });
        
        layoutGenerator.addLabelWidgetPair("Case name contains:  ", nameFilter, panel);
        layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
                25, 10, // initialX, initialY
                5, 5);// xPad, yPad
        return panel; 
    }
    
    private void refresh(){
        try {
            if (caseCategoryCombo.getSelectedItem() == null || ((CaseCategory)caseCategoryCombo.getSelectedItem()).getName().equals("All")) {
                if (caseCategoryCombo.getSelectedItem() == null) {
                    refreshCases(new Case[] {});
                } else {
                    refreshCases(new Case[] { new Case("All") });
                }
                return; 
            }
            presenter.refreshCases((CaseCategory) caseCategoryCombo.getSelectedItem(), nameFilter.getText());
        } catch (EmfException e1) {
            e1.printStackTrace();
        }
    }
    
    private JPanel buildCaseCategoryCombo(CaseCategory[] caseCategories, CaseCategory defaultCategory) {
        JPanel panel = new JPanel(new BorderLayout());
        caseCategoryCombo = new ComboBox("Choose a case category", caseCategories);
        if (defaultCategory != null )
            caseCategoryCombo.setSelectedItem(defaultCategory); 

        caseCategoryCombo.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                lastCaseCategory = (CaseCategory)caseCategoryCombo.getSelectedItem();
                refresh();
            }
        });

        panel.add(caseCategoryCombo, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(1, 20, 5, 20));
        return panel;
    }
    
    private JPanel buildCasesPanel(boolean selectSingle) {
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

    private Action cancelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        };
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (caseList.getSelectedValues() == null || caseList.getSelectedValues().length == 0) { 
                    cases = new Case[]{}; 
                    JOptionPane.showMessageDialog(parent, 
                            "Please choose some cases", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    // get selected cases
                    List<Case> list = new ArrayList<Case>(caseList.getSelectedValues().length);
                    for (int i = 0; i < caseList.getSelectedValues().length; i++)
                        list.add((Case) caseList.getSelectedValues()[i]);
                    cases = list.toArray(new Case[0]);
                    shouldCreate = true; 
                    setVisible(false);
                    dispose();
                    lastCaseCategory = (CaseCategory)caseCategoryCombo.getSelectedItem();
                }
            }
        };
    }

    public void observe(CaseSearchPresenter presenter) {
        // NOTE Auto-generated method stub
        this.presenter = presenter;
    }

    public void refreshCases(Case[] cases) {
        // NOTE Auto-generated method stub
        caseList.setListData(cases);
    }

    public Case[] getCases() {
        // NOTE Auto-generated method stub
        return cases;
    }

    public boolean shouldCreate() {
        // NOTE Auto-generated method stub
        return this.shouldCreate;
    }

    public void clearMessage() {
        // NOTE Auto-generated method stub
        
    }
}
