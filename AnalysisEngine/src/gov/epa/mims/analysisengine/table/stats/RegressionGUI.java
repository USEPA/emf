package gov.epa.mims.analysisengine.table.stats;
import gov.epa.mims.analysisengine.gui.DefaultUserInteractor;
import gov.epa.mims.analysisengine.gui.GUIUserInteractor;
import gov.epa.mims.analysisengine.gui.OptionDialog;
import gov.epa.mims.analysisengine.gui.StringValuePanel;
import gov.epa.mims.analysisengine.gui.UserInteractor;
import gov.epa.mims.analysisengine.table.SelectColumnsGUI;
import gov.epa.mims.analysisengine.table.filter.FilterCriteria;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class RegressionGUI extends JPanel 
{
   JPanel tabNamePanel;
   JPanel depSelectionPanel;
   JPanel indepSelectionPanel;
   RegressionModel model;
   JButton bDepSelectAll;
   JButton bIndepSelectAll;
   JButton bIndepSelectSame;
   JCheckBox showSLRPreview;
   JTextField IndepVariablesField;
   JLabel IndepVariablesLabel;
   JLabel dependentVariablesLabel;
   JTextField dependentVariablesField;
   JRadioButton rbConcise;
   JRadioButton rbNormalized;
   
   boolean simpleLinearRegression;

   transient Vector depVariables;
   transient Vector indepVariables;
   
   public RegressionGUI(RegressionModel model, boolean simpleLinearRegression)
   {
       this.model=model;
       this.simpleLinearRegression = simpleLinearRegression;
       initialize();
       initGUIModel(model);
       this.setVisible(true);
   }
   
   private void initialize()
   {
      setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createLineBorder(Color.black),""));
      setLayout(new BorderLayout());

      JPanel topPanel = new JPanel();
      topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

      tabNamePanel = new StringValuePanel("Tab Name", false);
      ((StringValuePanel)tabNamePanel).setValue(model.getTabName(simpleLinearRegression));

      JPanel infoPanel = new JPanel();
      infoPanel.setLayout(new GridLayout(1,2));
      infoPanel.setBorder(BorderFactory.createTitledBorder(
       BorderFactory.createLineBorder(Color.black),"Display"));
      
      if(simpleLinearRegression)
      {
         ButtonGroup grp = new ButtonGroup();
         rbConcise = new JRadioButton("Correlation Coefficient "+
"only", true);
         rbConcise.addItemListener(new ItemListener()
         {
            public void itemStateChanged(ItemEvent it)
            {
               String tabn = ((StringValuePanel)tabNamePanel).getValue();
               if(rbConcise.isSelected())
               {
                  model.setConciseReport(true);
                  if(tabn.indexOf("_DCC") > 0)
                     ((StringValuePanel)tabNamePanel).setValue(tabn.substring(0,
tabn.indexOf("_DCC"))+"_CC");
               }
               else
               {
                  model.setConciseReport(false);
                  if(tabn.indexOf("_CC") > 0)
                     ((StringValuePanel)tabNamePanel).setValue(tabn.substring(0,
tabn.indexOf("_CC"))+"_DCC");
               }
            }
         });

         JRadioButton rbDetailed = new JRadioButton("Detailed Correlation "+
         "Analysis", false);

         grp.add(rbConcise);
         grp.add(rbDetailed);
         infoPanel.add(rbConcise);
         infoPanel.add(rbDetailed);
      }
      else
      {
         ButtonGroup grp = new ButtonGroup();
         rbNormalized = new JRadioButton("Normalized Linear Regression ", false);
         rbNormalized.addItemListener(new ItemListener()
         {
            public void itemStateChanged(ItemEvent it)
            {
               String tabn = ((StringValuePanel)tabNamePanel).getValue();
               if(rbNormalized.isSelected())
               {
                  model.setNormalized(true);
                  if(tabn.indexOf("_LR") > 0)
                     ((StringValuePanel)tabNamePanel).setValue(tabn.substring(0,
tabn.indexOf("_LR"))+"_NLR");
               }
               else
               {
                  model.setNormalized(false);
                  if(tabn.indexOf("_NLR") > 0)
                     ((StringValuePanel)tabNamePanel).setValue(tabn.substring(0,
tabn.indexOf("_NLR"))+"_LR");
               }
            }
         });
                                                                                                                                               
         JRadioButton rbRegular = new JRadioButton("Linear Regression", true);
                                                                                                                                               
         grp.add(rbRegular);
         grp.add(rbNormalized);
         infoPanel.add(rbRegular);
         infoPanel.add(rbNormalized);
      }

      topPanel.add(tabNamePanel);
      topPanel.add(infoPanel);
      this.add(topPanel, BorderLayout.NORTH);

      JPanel variablePanel = new JPanel();
      variablePanel.setLayout(new GridLayout(2, 1));

      JPanel depSelectionPanel = createDepVarSelectionPanel();
      variablePanel.add(depSelectionPanel);
      
      indepSelectionPanel = createIndepVarSelectionPanel();
      variablePanel.add(indepSelectionPanel);

      this.add(variablePanel, BorderLayout.CENTER);
   }
   
   private void setSelection(String[] colNames,  Vector depVars, Vector indepVars)
   {
       Vector columnNames = new Vector();
       for(int i=0; i<colNames.length; i++)
           columnNames.add(colNames[i]);
       if(depVars!=null)
       {
          depVariables = (Vector)depVars.clone();
          for(int i=0; i < depVars.size(); i++)
              if(!columnNames.contains(depVars.get(i)))
                 depVariables.remove(depVars.get(i));
          dependentVariablesLabel.setText("["+depVariables.size() + " cols]");
          dependentVariablesField.setText(getStringFromVector(depVariables)); 
       }

       if(indepVars != null)
       {
          indepVariables = (Vector)indepVars.clone();
          for(int i=0; i < indepVars.size(); i++)
              if(!columnNames.contains(indepVars.get(i)))
                 indepVariables.remove(indepVars.get(i));
          IndepVariablesLabel.setText("["+indepVariables.size() + " cols]");
          IndepVariablesField.setText(getStringFromVector(indepVariables));       
       }
   }

   public void initGUIModel(RegressionModel model)
   {
      this.model = model;
      ((StringValuePanel)tabNamePanel).setValue(model.getTabName(simpleLinearRegression));
      String[] colNames = model.getColumnNames();
      if(simpleLinearRegression)
      {
         depVariables = model.getSLRDepVariables();
         indepVariables = model.getSLRIndepVariables();
     }
     else
     {
        depVariables = model.getLRDepVariables();
        indepVariables = model.getLRIndepVariables();
     }
      setSelection(colNames, depVariables, indepVariables);
  }

  public RegressionModel saveToModel()
  {
    if(depVariables.size()==0 || indepVariables.size()==0)
    {
         new GUIUserInteractor().notify(this,"Error", "Select at least one variable "+
            "for both the dependent and independent variables",
UserInteractor.NOTE);
         return null;
    }
    model.setTabName(simpleLinearRegression, ((StringValuePanel)tabNamePanel).getValue());
    model.setValues(simpleLinearRegression, depVariables, indepVariables);
    return model;
   }
      
   private JPanel createIndepVarSelectionPanel()
   {
      JPanel dataColumnPanel = new JPanel();
      dataColumnPanel.setLayout(new BoxLayout(dataColumnPanel, BoxLayout.X_AXIS));

      bIndepSelectAll = new JButton("Select All");
      bIndepSelectAll.setSelected(false);
      bIndepSelectAll.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent ie)
         {
            // select all the variables in the indepVarSelectionPanel
            indepVariables = new Vector();
            String[] filterColNames = model.getColumnNames();
            for(int i=0; i< filterColNames.length; i++)
               indepVariables.add(filterColNames[i]);   
            IndepVariablesLabel.setText("["+indepVariables.size() + " cols]");
            IndepVariablesField.setText(getStringFromVector(indepVariables));
         }
      });

      bIndepSelectSame = new JButton("All but Selected Dependent Variables");
      bIndepSelectSame.addActionListener(new ActionListener()
      {
      	public void actionPerformed(ActionEvent ie)
      	{
            indepVariables = allBut(depVariables);
            IndepVariablesLabel.setText("["+indepVariables.size() + " cols]");
            IndepVariablesField.setText(getStringFromVector(indepVariables));
	      }
      });


      JPanel indepSelEnablerPanel = new JPanel();
      indepSelEnablerPanel.setLayout(new BoxLayout(indepSelEnablerPanel,
BoxLayout.X_AXIS));
      indepSelEnablerPanel.add(Box.createHorizontalStrut(10));
      indepSelEnablerPanel.add(bIndepSelectAll);
      indepSelEnablerPanel.add(Box.createHorizontalStrut(20));
      indepSelEnablerPanel.add(bIndepSelectSame);
      indepSelEnablerPanel.add(Box.createHorizontalGlue());

      IndepVariablesField = new JTextField(20);
      IndepVariablesField.setBackground(Color.white);
      IndepVariablesField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
      IndepVariablesField.setEditable(false);
      JPanel IndepVariablesPanel = new JPanel();
      IndepVariablesPanel.setLayout(new
BoxLayout(IndepVariablesPanel,BoxLayout.X_AXIS));
      IndepVariablesPanel.add(IndepVariablesField);
    
      IndepVariablesLabel = new JLabel("[0 cols]");
      JButton dataButton = new JButton("Select");
      
      dataButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            showColumnSelectionGUI(false);
         }
      });

      JPanel previewPanel = new JPanel();
      previewPanel.setLayout(new BoxLayout(previewPanel, BoxLayout.X_AXIS));
      if(!simpleLinearRegression)
      {
         showSLRPreview = new JCheckBox("Show 1-1 correlation data "+
            "for selection");

         previewPanel.add(Box.createHorizontalStrut(10));
         previewPanel.add(showSLRPreview);
         previewPanel.add(Box.createHorizontalGlue());

         showSLRPreview.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent ie)
            {
               
              // showSLRPreview.setSelected();
            }
         });
      }

      dataColumnPanel.add(Box.createHorizontalStrut(5));
      dataColumnPanel.add(IndepVariablesPanel);
      dataColumnPanel.add(Box.createHorizontalStrut(5));
      dataColumnPanel.add(IndepVariablesLabel);
      dataColumnPanel.add(Box.createHorizontalStrut(5));
      dataColumnPanel.add(dataButton);
      dataColumnPanel.add(Box.createHorizontalStrut(10));

      JPanel dataPanel = new JPanel();
      dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
      dataPanel.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createLineBorder(Color.black),"Independent Variables"));
      dataPanel.add(Box.createVerticalStrut(20));
      dataPanel.add(indepSelEnablerPanel);
      dataPanel.add(Box.createVerticalStrut(20));
      dataPanel.add(dataColumnPanel);
      if(!simpleLinearRegression)
      {
         dataPanel.add(Box.createVerticalStrut(10));
         dataPanel.add(previewPanel);
      }
      dataPanel.add(Box.createVerticalStrut(20));
      return dataPanel;
   }
   
   private Vector allBut(Vector variables) {
	   Vector rest = new Vector();
	   List list = new ArrayList();
       list.addAll(Arrays.asList(model.getColumnNames()));
       
       for(int i = 0; i < variables.size(); i++)
    	   if (list.contains(variables.get(i)))
    		   list.remove(variables.get(i));
       
       rest.addAll(list);
       
       return rest;
   }

private JPanel createDepVarSelectionPanel()
   {
      JPanel dataColumnPanel = new JPanel();
      dataColumnPanel.setLayout(new BoxLayout(dataColumnPanel,
BoxLayout.Y_AXIS));

      bDepSelectAll = new JButton("Select All");
      bDepSelectAll.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent ie)
         {
             // select all the variables in the depVarSelectionPanel
             String[] filterColNames = model.getColumnNames();
             depVariables = new Vector();
             for(int i=0; i< filterColNames.length; i++)
                depVariables.add(filterColNames[i]);
              // update the column data field and col num label
             dependentVariablesLabel.setText("["+depVariables.size() + " cols]");
             dependentVariablesField.setText(getStringFromVector(depVariables));
             return;
         }
      });

      dependentVariablesField = new JTextField(20);
      dependentVariablesField.setBackground(Color.white);
      dependentVariablesField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
      dependentVariablesField.setEditable(false);
      JPanel dependentVariablesPanel = new JPanel();
      dependentVariablesPanel.setLayout(new
BoxLayout(dependentVariablesPanel,BoxLayout.X_AXIS));
      dependentVariablesPanel.add(Box.createHorizontalStrut(5));
      dependentVariablesPanel.add(dependentVariablesField);
      dependentVariablesPanel.add(Box.createHorizontalStrut(5));

      dependentVariablesLabel = new JLabel("[0 cols]");
      JButton dataButton = new JButton("Select");
      
      dataButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            showColumnSelectionGUI(true);
         }
      });

      dependentVariablesPanel.add(dependentVariablesLabel);
      dependentVariablesPanel.add(Box.createHorizontalStrut(5));
      dependentVariablesPanel.add(dataButton);
      dependentVariablesPanel.add(Box.createHorizontalGlue());

      dataColumnPanel.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createLineBorder(Color.black),"Dependent Variables"));
      bDepSelectAll.setHorizontalAlignment(SwingConstants.LEFT);
      dataColumnPanel.add(Box.createVerticalStrut(20));
      dataColumnPanel.add(bDepSelectAll);
      dataColumnPanel.add(Box.createVerticalStrut(20));
      dataColumnPanel.add(dependentVariablesPanel);
      dataColumnPanel.add(Box.createVerticalStrut(20));
      return dataColumnPanel;

   }//createVariableSelectionPanel
   
    private void showColumnSelectionGUI(boolean dep)
    {
      SelectColumnsGUI filterGUI = null;
      String [] filterColNames = model.getColumnNames();
      int numCols = filterColNames.length;
   
      boolean[] showColumns = new boolean[numCols];
   
      Vector vect = new Vector();

      if(dep)
      {
         if(simpleLinearRegression)
            vect = model.getSLRDepVariables();
         else
            vect = model.getLRDepVariables();

         if(depVariables.size() > 0 && vect.size()==0)
            vect = depVariables;
      }
      else
      {
         if(simpleLinearRegression)
            vect = model.getSLRIndepVariables();
         else
            vect = model.getLRIndepVariables();

        if(indepVariables.size() > 0 && vect.size()==0)
           vect = indepVariables;
      }
      
      if(vect.size()>0)
      {
         for(int i=0; i < numCols; i++)
         {
            if(vect.contains(filterColNames[i]))
               showColumns[i]=true;
            else
               showColumns[i]=false;
         }
      }
      else
      {
         for(int i=0; i<numCols; i++)
         {
            showColumns[i]=true;
         }
      }
      
      FilterCriteria filterCriteria = new FilterCriteria( filterColNames , /*null, */ showColumns);
      //Regression Model should implement FormatAndIndexIfc
      filterCriteria.setTableModel(model);
      filterGUI = new SelectColumnsGUI(null, filterCriteria, "Include/Exclude Columns",
         "Include", "Exclude");
      filterGUI.setLocationRelativeTo(this);
      filterGUI.setVisible(true);

//    Apply the filter if the user pressed OK
      if(filterGUI.getResult() != OptionDialog.OK_RESULT)
      {
         return;
      }

      showColumns = filterGUI.getSelectedColumns(); //update the showColumns
      boolean atLeastOneColumnSelected = false;
        //check  whether selected columns are double or integer type else throw error message
        //also checks atleast one column is selected
      Vector result = new Vector();
      for(int i=0; i< showColumns.length; i++)
      {
         if(showColumns[i] == true)
         {
            atLeastOneColumnSelected = true;
     	      result.add(filterColNames[i]);
         }
      }//for(i)
      
      if(dep)
      {
         depVariables.clear();
         depVariables.addAll(result);
         dependentVariablesLabel.setText("["+depVariables.size() + " cols]");
         dependentVariablesField.setText(getStringFromVector(depVariables));
      }
      else
      {
         indepVariables.clear();
         indepVariables.addAll(result);
         IndepVariablesLabel.setText("["+indepVariables.size() + " cols]");
         IndepVariablesField.setText(getStringFromVector(indepVariables));
      }

      if(!atLeastOneColumnSelected)
      {
         DefaultUserInteractor.get().notify(this,"Warning", 
             "You should select at least one column.", UserInteractor.WARNING);
      }

      return;
   }//showColumnSelectioGUI()

   public static String getStringFromVector(Vector input)
   {
      if(input == null || input.size()==0)
         return " ";
      
      StringBuffer buff = new StringBuffer();
      
      for(int i=0; i<input.size()-1; i++)
         buff.append(input.get(i)+", ");
      
      if(input.size()>1)
          buff.append(input.get(input.size()-1));
      else
    	  buff.append(input.get(0));
      
      return buff.toString();
   }

   public static void showGUI(RegressionModel model)
   {
     JFrame.setDefaultLookAndFeelDecorated(true);

      //Create and set up the window.
      JFrame frame = new JFrame("Regression GUI");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      //Create and set up the content pane.
      RegressionGUI gui = new RegressionGUI( model, true);
	   frame.getContentPane().add(gui);

      //Display the window.
      frame.pack();
      frame.setVisible(true);
   }

   public static void main(String[] args)
   {
       RegressionGUI.showGUI(null);
   }

}
