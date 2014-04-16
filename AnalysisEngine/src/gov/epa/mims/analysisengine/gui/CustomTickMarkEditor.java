package gov.epa.mims.analysisengine.gui;

import java.awt.Container;
import java.awt.GridLayout;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import gov.epa.mims.analysisengine.tree.Axis;

/**
 * A simple Dialog to edit the custom user tick marks.
 * @author Daniel Gatti
 * @version $Id: CustomTickMarkEditor.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 */
public class CustomTickMarkEditor extends OptionDialog
{
   /** The GUI for userTickMarkLabels in Axis. */
   protected StringEditableTablePanel userLabelPnl = null;

   /** The GUI for userTickMarkPositions in Axis. */
   protected DoubleEditableTablePanel userPositionPnl = null;

   /** The Axis in which th custom tick marks reside. */
   protected Axis axis = null;

   /**
    * Constructor.
    * @param axis Axis that this GUI will edit.
    */
   public CustomTickMarkEditor(Axis axis)
   {
      super();
      this.axis = axis;
      userLabelPnl = new StringEditableTablePanel("Custom Tick Mark Labels");
      userPositionPnl = new DoubleEditableTablePanel("Custom Tick Mark Positions");

      JPanel userTickPanel = new JPanel(new GridLayout(1,2));
      userTickPanel.add(userLabelPnl);
      userTickPanel.add(userPositionPnl);

      Container contentPane = getContentPane();
      contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
      contentPane.add(userTickPanel);
      contentPane.add(getButtonPanel());
      setModal(true);
      initGUIFromModel();
      pack();
   }


   /**
    * Take the custom tick mark information from the GUI and save it to the
    * Axis.
    *
    * @throws java.lang.Exception
    */
   protected void saveGUIValuesToModel() throws java.lang.Exception
   {
      axis.setUserTickMarkLabels((String[])userLabelPnl.getValue());
      axis.setUserTickMarkPositions(userPositionPnl.getValueAsPrimitive());
   }


   /**
    * Get the custom tick mark information from the Axis and place it in
    * the GUI.
    */
   protected void initGUIFromModel()
   {
      userLabelPnl.setValue(axis.getUserTickMarkLabels());
      userPositionPnl.setValue(axis.getUserTickMarkPositions());
   }
} // CustomTickMarkEditor

