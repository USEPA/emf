package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public class StrategySummarySelectionDialog extends JDialog implements StrategySummarySelectionView {

//    private EmfConsole parent;

//    private StrategySummarySelectionPresenter presenter;

    private JList strategySummaryList;
    
    private StrategyResultType[] strategyResultTypes = new StrategyResultType[] {};
    
    public StrategySummarySelectionDialog(EmfConsole parent) {
        super(parent);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
//        this.parent = parent;
        setModal(true);
    }

    public void display(StrategyResultType[] strategyResultTypes) {
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(5, 5));
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(buildList(strategyResultTypes), BorderLayout.CENTER);
        panel.add(buttonPanel(), BorderLayout.SOUTH);
        contentPane.add(panel);
//        if (strategyResultTypes.length == 1)
//        {
//            setTitle("Select "+strategyResultTypes[0].getName()+" Strategy Summary");
//        }
//        else
//        {
           setTitle("Select Strategy Summaries To Generate");
//        }   
        this.pack();
        this.setSize(500, 400);
        this.setLocation(ScreenUtils.getPointToCenter(this));
        this.setVisible(true);
    }

    public StrategyResultType[] getStrategyResultTypes() {
        return strategyResultTypes;
    }
    
    private JPanel buildList(StrategyResultType[] strategyResultTypes) {
        strategySummaryList = new JList(strategyResultTypes);
        strategySummaryList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollPane = new JScrollPane(strategySummaryList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(500, 300));
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(scrollPane);
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
                if (strategySummaryList.getSelectedValues() == null || strategySummaryList.getSelectedValues().length == 0) 
                    strategyResultTypes = new StrategyResultType[]{}; 
                else {
                    // get selected datasets
                    List<StrategyResultType> list = new ArrayList<StrategyResultType>(strategySummaryList.getSelectedValues().length);
                    for (int i = 0; i < strategySummaryList.getSelectedValues().length; i++)
                        list.add((StrategyResultType) strategySummaryList.getSelectedValues()[i]);
                    strategyResultTypes = list.toArray(new StrategyResultType[0]);
                }
                setVisible(false);
                dispose();
            }
        };
    }

//    public void observe(StrategySummarySelectionPresenter presenter) {
//        this.presenter = presenter;
//    }
    
    public void clearMessage() {
        // NOTE Auto-generated method stub
        
    }


}