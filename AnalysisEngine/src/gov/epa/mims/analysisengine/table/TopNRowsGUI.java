package gov.epa.mims.analysisengine.table;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;

import gov.epa.mims.analysisengine.gui.*;
import gov.epa.mims.analysisengine.table.filter.FilterCriteria;

/**
 * <p>Description: Get an integer from the user to use for showing the
 *  top/bottom N rows in the table. </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Daniel Gatti - UNC - CEP
 * @version $Id: TopNRowsGUI.java,v 1.3 2006/11/01 15:33:38 parthee Exp $
 */
public class TopNRowsGUI extends OptionDialog
{
   /** The percentage of rows that we show. */
   protected IntegerValuePanel rowsPanel = null;

   /** The number of rows that we show. */
   protected IntegerValuePanel percentPanel = null;

   /** Radio button for show N rows. */
   protected JRadioButton showRowsBtn = null;

   /** Radio button for show N % rows. */
   protected JRadioButton showPercentBtn = null;

   /** The button group for the radio buttons. */
   protected ButtonGroup buttonGroup = new ButtonGroup();

   /** The number of rows to show. */
   protected int number = 0;

   /** Boolean that is true if we are showing N rows and false if we are
    * showing the N percent. */
   protected boolean showRows = true;
   
  /** to indicate whether data selection gui is required
    */
   protected boolean showDataSelection = false;
   
   /** a text field to indicate the name of the column selected
    */
   private JTextField dataMessageField;
   
    /** a text label to indicate the name of the column selected
    */
   private JLabel dataMessageLabel;
   
   /** an array of booleans to indicate whether columns are selected or not
    *  true value indicates that the column selected and vice versa
    */
   private boolean [] showColumns;
   
   /** selected data column
    */
   private int selectedCoumn = -1;

   /** The maximum number of rows to allow the user to enter. */
   protected int maximumRows = Integer.MAX_VALUE;
   
   /** a data model for the gui with the data selction */
   //note: no model for the popup menu gui
   private TopNRowsModel model;

   private JFrame parent;

   /**
    * Constructor.
    * @param columnName String that is the name of the column we are sorting
    *    and filtering.
    * @param isTop boolean that is true if we are showing the top N numbers.
    * @param maximumRows int that is the largest number of rows to accept from
    *    the user.
    */
   public TopNRowsGUI(JFrame parent, String columnName, boolean isTop, int maximumRows)
   {
      super(parent);
      this.maximumRows = maximumRows;
      String topOrBottom = ((isTop)? "largest" : "smallest");
      setTitle("Show " + topOrBottom +  " rows for column " + columnName);
      initialize(topOrBottom);
      pack();
      setModal(true);
   } // TopNRowsGUI()
   
   /**
    * Constructor.
    * @param columnName String that is the name of the column we are sorting
    *    and filtering.
    * @param isTop boolean that is true if we are showing the top N numbers.
    * @param maximumRows int that is the largest number of rows to accept from
    *    the user.
    */
   public TopNRowsGUI(JFrame parent, TopNRowsModel model ,boolean dataSelection, int maximumRows)
   {
      super(parent);
      this.model = model;
      boolean isTop = model.isTopNRows();
      if(model.getNoOfRows() <= 0)
         model.setNoOfRows(maximumRows);
      this.showDataSelection = dataSelection;
      this.maximumRows = maximumRows;
      String topOrBottom = ((isTop)? "largest" : "smallest");
      setTitle("Show " + topOrBottom +  " rows ");
      initialize(topOrBottom);
      initGUIFromModel();
      pack();
      setModal(true);
   } // TopNRowsGUI()

   /** helper method to initialize the gui
    */
   private void initialize(String topOrBottom)
   {
      JPanel dataPanel = null;
      if(showDataSelection)
      {
         dataPanel = createDataPanel();
      }
      rowsPanel    = new IntegerValuePanel("Number of rows    ", false, 0,
                     maximumRows);
      percentPanel = new IntegerValuePanel("Percentage of rows", false, 0, 100);

      showRowsBtn    = new JRadioButton("Show " + topOrBottom + " Rows");
      showPercentBtn = new JRadioButton("Show " + topOrBottom + " Percent");
      buttonGroup.add(showRowsBtn);
      buttonGroup.add(showPercentBtn);
      showRowsBtn.addActionListener(
            new ActionListener()
            {
               public void actionPerformed(ActionEvent e)
               {
                  enableInputFields();
               }
            }
      );

      showPercentBtn.addActionListener(
            new ActionListener()
            {
               public void actionPerformed(ActionEvent e)
               {
                  enableInputFields();
               }
            }
      );
      showRowsBtn.setSelected(true);
      percentPanel.setEnabled(false);

      JPanel inputPanel = new JPanel();
      inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
      inputPanel.add(rowsPanel);
      inputPanel.add(percentPanel);

      JPanel radioPanel = new JPanel();
      radioPanel.setLayout(new BoxLayout(radioPanel,BoxLayout.Y_AXIS));
      radioPanel.add(showRowsBtn);
      radioPanel.add(showPercentBtn);

      JPanel mainPanel = new JPanel();
      Border outsideBorder = BorderFactory.createEmptyBorder(4,4,4,4);
      Border insideOutBorder = BorderFactory.createEmptyBorder(2,2,2,2);
      Border insideInBorder = BorderFactory.createLoweredBevelBorder();
      Border insideBorder = BorderFactory.createCompoundBorder(
         insideOutBorder, insideInBorder);
      mainPanel.setBorder(BorderFactory.createCompoundBorder(
         outsideBorder,insideBorder));
      //mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.X_AXIS));
      mainPanel.setLayout(new BorderLayout());

      if(dataPanel != null)
      {
         mainPanel.add(dataPanel,BorderLayout.NORTH);
      }
      mainPanel.add(radioPanel,BorderLayout.WEST);
      mainPanel.add(inputPanel, BorderLayout.EAST);

      Container contentPane = getContentPane();
      contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
      contentPane.add(mainPanel);
      contentPane.add(getButtonPanel());
   }
   
   /*** create a panel to insert the data
    */
   private JPanel createDataPanel()
   {
      JLabel columnLabel = new JLabel("Data Column: ");
      
      dataMessageField = new JTextField(10);
      dataMessageField.setBackground(Color.white);
      dataMessageField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
      dataMessageField.setEditable(false);
      
      dataMessageLabel = new JLabel("[0 cols]");
      JPanel dsPanel = new JPanel();
      dsPanel.add(dataMessageLabel);
      JButton dataButton = new JButton("Select");
      dataButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            showColumnSelectionGUI();
         }
      });
      JPanel dataColumnPanel = new JPanel();
      dataColumnPanel.add(columnLabel);
      dataColumnPanel.add(dataMessageField);
      dataColumnPanel.add(dsPanel);
      dataColumnPanel.add(dataButton);
      return dataColumnPanel;
   }//createDataPanel()
   
   /**
    * Return true if we are showing rows and false if we ar showing
    * percentage.
    */
   public boolean getShowRows()
   {
      return showRows;
   } // getShowRows()



   /**
    * Get the number of rows or percent that should be shown.
    */
   public int getNumRows()
   {
      return number;
   }

   /** get the index of the column selected
    */
   public int getSelectedColumn()
   {
      return selectedCoumn;
   }

   /**
    * Since this is currently implemented as a sort followed by a filter,
    * we are not trying to show the current state in the GUI.
    */
   protected void initGUIFromModel()
   {
      if(model != null)
      {
         String selCol = model.getSelectedColName();
         if(selCol != null)
         {
            OverallTableModel overallModel = model.getOverallTableModel();

            int numCol = overallModel.getColumnCount();
            showColumns = new boolean[--numCol];//deduting one for the first column

            int index = overallModel.getColumnNameIndex(selCol);
            if(index!= -1)
            {
               showColumns[index] = true;  //deducting one for to discount the first col
               selectedCoumn = index;
            }//if
            dataMessageLabel.setText("["+ 1 + " cols]");
            dataMessageField.setText(selCol);
         }//if

         boolean rows = model.isRows();
         if(rows)
         {
            showRowsBtn.setSelected(rows);
            rowsPanel.setValue(model.getNoOfRows());
         }//if
         else
         {
            showPercentBtn.setSelected(!rows);
            percentPanel.setValue(model.getPercentage());
         }//else
         enableInputFields();
      }//if(model != null)
   }


   /**
    * When the radio button selection changes, enable and disable the
    * appropriate text boxes.
    */
   protected void enableInputFields()
   {
      boolean rowsSelected = showRowsBtn.isSelected();
      rowsPanel.setEnabled(rowsSelected);
      percentPanel.setEnabled(!rowsSelected);
   } // enableInputFields()


   /**
    * Save the GUI values to the data model.
    * @throws Exception
    */
   protected void saveGUIValuesToModel() throws Exception
   {
      showRows = (showRowsBtn.isSelected());
      if(showDataSelection)
      {
         if(selectedCoumn == -1)
         {
            shouldContinueClosing = false;
            DefaultUserInteractor.get().notify(this,"Error", "Please select a column.",
                  UserInteractor.ERROR);
            return;
         }
         String selectedColName = model.getOverallTableModel().getColumnName(selectedCoumn);
         model.setSelectedColName(selectedColName);
         model.setRows(showRows);
      }//if

      if (showRows)
      {
         number = rowsPanel.getValue();
         if(model != null)
         {
            model.setNoOfRows(number);
         }
      }
      else
      {
         number = percentPanel.getValue();
         if(model != null)
         {
            model.setPercentage(number);
         }
      }
      if(number == Integer.MIN_VALUE)
      {
         shouldContinueClosing = false;
         String selectedOption = "rows";
         if(!showRows)
         {
            selectedOption ="percent";
         }
         DefaultUserInteractor.get().notify(this,"Error", "Please input a value for "
            + selectedOption, UserInteractor.ERROR);
         return;
      }
      
   }
   
   /** a helper method to show the column selection gui
    */
   private void showColumnSelectionGUI()
   {
      SelectColumnsGUI filterGUI = null;
      FilterCriteria filterCriteria;
      OverallTableModel overallModel = model.getOverallTableModel();
      int numCols = overallModel.getColumnCount();
//System.out.println("numCols="+numCols);
      if(showColumns == null)
      {
         showColumns = new boolean[--numCols]; //deducting one for the first column
      }
      //Arrays.fill(selected, true);
      String [] filterColNames = new String[showColumns.length];//deducting one for the first column
      for(int i=0; i< filterColNames.length; i++)
      {
//System.out.println("overallModel.getColumnName(i+1)="+overallModel.getColumnName(i+1));            
         filterColNames[i] = overallModel.getColumnName(i+1);//adding one to avoid the first col
      }//for(i)
      filterCriteria = new FilterCriteria(filterColNames ,/* null,*/ showColumns);
      filterCriteria.setTableModel(overallModel);
      filterGUI = new SelectColumnsGUI(parent, filterCriteria, "Include/Exclude Columns",
         "Include", "Exclude");
      filterGUI.setLocationRelativeTo(this);
      filterGUI.setVisible(true);

      // Apply the filter if the user pressed OK.
      if (filterGUI.getResult() != OptionDialog.OK_RESULT)
      {
         return;  
      }
      
      showColumns = filterGUI.getSelectedColumns();
      int count =0;
      
      for(int i=0; i< showColumns.length; i++)
      {
         if(showColumns[i] == true)
         {
            selectedCoumn =i+1;//adding one to avoid the first col
            count ++;
         }
         
      }//for(i)
      if(count ==1)
      {
         dataMessageLabel.setText("["+1+" cols]");
         dataMessageField.setText(overallModel.getColumnName(selectedCoumn));
      }
      else if(count >=2)
      {
         DefaultUserInteractor.get().notify(this,"Error", "Please select only one column!",
            UserInteractor.ERROR);
         return;
      }//if
      else //count ==0
      {
         dataMessageLabel.setText("["+ 0 +" cols]");
         dataMessageField.setText(" ");
         selectedCoumn = -1;
         model.setSelectedColName(null);
         DefaultUserInteractor.get().notify(this,"Error", "Please select a column!",
               UserInteractor.ERROR);
         return;
      }//else
   }//showColumnSelectioGUI()
} // class TopNRowsGUI

