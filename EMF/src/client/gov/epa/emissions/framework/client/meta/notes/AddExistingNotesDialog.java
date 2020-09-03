package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.emissions.framework.client.meta.versions.VersionsSet;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.Note;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;

public class AddExistingNotesDialog extends JDialog{

      private TextField name; 
      
      private EditNotesTabPresenter presenter; 
      
      private ComboBox versionsSelection;
      
      private VersionsSet versionsSet;

      private JList notesList;
      
      private MessagePanel messagePanel; 
      
      private Note[] notes = new Note[] {};
      
      private boolean addExistingNote =false; 

      public AddExistingNotesDialog(EmfConsole parent) {
          super(parent);
          super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
          setModal(true);
      }

      public void display(Note[] notes, Version[] versions) {
          this.versionsSet = new VersionsSet(versions);
          Container contentPane = getContentPane();
          contentPane.setLayout(new BorderLayout(5, 5));
          JPanel panel = new JPanel(new BorderLayout(5, 5));
          panel.add(buildTopPanel(), BorderLayout.NORTH);
          panel.add(buildNotesPanel(notes), BorderLayout.CENTER);
          panel.add(buttonPanel(), BorderLayout.SOUTH);
          contentPane.add(panel);
          setTitle("Add existing notes to dataset");
          this.pack();
          this.setSize(500, 400);
          this.setLocation(ScreenUtils.getPointToCenter(this));
          this.setVisible(true);
      }

      public Note[] getNotes() {
          return notes;
      }
      
      public boolean check(){
          return addExistingNote;
      }
      
      public Version getVersion(){
          return versionsSet.getVersionFromNameAndNumber((String)versionsSelection.getSelectedItem());
      }
      
      private JPanel buildTopPanel(){
          messagePanel = new SingleLineMessagePanel();
          JPanel panel = new JPanel();
          panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
          panel.add(messagePanel);
          panel.add(buildfilterPanel());
          return panel; 
      }
      private JPanel buildfilterPanel(){
          JPanel panel = new JPanel(new SpringLayout());
          SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
          
          versionsSelection = new ComboBox("Select one", versionsSet.nameAndNumbers());
          layoutGenerator.addLabelWidgetPair("Version:  ", versionsSelection, panel);
          // set up name filter
          JPanel rightPanel = new JPanel(new BorderLayout());
          //JPanel container = new JPanel();
          nameField();
          rightPanel.add(name, BorderLayout.LINE_START);

          Button button = new Button("Get", new AbstractAction() {
              public void actionPerformed(ActionEvent e) {
                  refresh();
              }
          });
          button.setMnemonic('G');
          button.setToolTipText("Get notes");
          rightPanel.add(button, BorderLayout.CENTER);

          //rightPanel.add(container, BorderLayout.LINE_START);
          layoutGenerator.addLabelWidgetPair("Note name contains:", rightPanel, panel);
          
          layoutGenerator.makeCompactGrid(panel, 2, 2, // rows, cols
                  10, 5, // initialX, initialY
                  10, 5);// xPad, yPad
          return panel; 
      }


      private void nameField(){
          name= new TextField ("Note name contains", "", 18);
          name.setEditable(true);
      }
      
      private void refresh(){
          
          try {
              notesList.setListData(presenter.getNotes(name.getText().trim()));
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
      }
      
      private JPanel buildNotesPanel(Note[] notes) {
          notesList = new JList();
          notesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
          JScrollPane scrollPane = new JScrollPane(notesList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                  JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
          scrollPane.setPreferredSize(new Dimension(500, 300));
          JPanel panel = new JPanel(new BorderLayout(10, 10));
          panel.add(scrollPane);
//          List<String> noteStrings = new ArrayList();
//          for (Note note : notes){
//              noteStrings.add(note.toString());
//          }
          notesList.setListData(notes); //noteStrings.toArray());
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
                  addExistingNote =false; 
                  dispose();
              }
          };
      }

      private Action okAction() {
          return new AbstractAction() {
              public void actionPerformed(ActionEvent e) {
                  // get selected notes
                  addExistingNotes();
              }
          };
      }
      
      private void addExistingNotes(){
          if (versionsSelection.getSelectedItem() == null){
              messagePanel.setError("Please select a version" );
              return; 
          }
          // get selected notes
          List<Note> list = new ArrayList<Note>(notesList.getSelectedValues().length);
          for (int i = 0; i < notesList.getSelectedValues().length; i++)
              list.add((Note) notesList.getSelectedValues()[i]);
          notes = list.toArray(new Note[0]);
          addExistingNote =true; 
          setVisible(false);
          dispose();
      }

      public void observe(EditNotesTabPresenter presenter) {
          this.presenter = presenter;
      }
}
