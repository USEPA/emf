package gov.epa.mims.analysisengine.table.persist;

import gov.epa.mims.analysisengine.table.SortFilterTablePanel;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

public class SaveConfigAction extends AbstractAction {
	
	SortFilterTablePanel parent = null;
	
	AnalysisConfiguration config;

	public SaveConfigAction(SortFilterTablePanel parent, ImageIcon configIcon) {
		super("Analysis Configuration", configIcon);
		this.parent = parent;
	}

	public void actionPerformed(ActionEvent e) {
		parent.showSaveConfigGUI();
	} 
} 
