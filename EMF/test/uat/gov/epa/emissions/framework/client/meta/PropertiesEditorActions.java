package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.client.UserAcceptanceTestCase;
import gov.epa.emissions.framework.client.meta.summary.EditableSummaryTab;

import javax.swing.JTabbedPane;

import abbot.tester.JTabbedPaneTester;

public class PropertiesEditorActions {

    private DatasetPropertiesEditor editor;

    private UserAcceptanceTestCase test;

    public PropertiesEditorActions(DatasetPropertiesEditor editor, UserAcceptanceTestCase test) {
        this.editor = editor;
        this.test = test;
    }

    public String[] tabs() {
        JTabbedPaneTester tester = new JTabbedPaneTester();
        JTabbedPane tabbedPane = tabbedPane();

        return tester.getTabs(tabbedPane);
    }

    private JTabbedPane tabbedPane() {
        return (JTabbedPane) test.findByName(editor, "tabbedPane");
    }

    public SummaryTabActions summary() {
        EditableSummaryTab summary = (EditableSummaryTab) test.findByName(tabbedPane(), "summary");
        return new SummaryTabActions(summary, test);
    }

}
