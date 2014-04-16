package gov.epa.mims.analysisengine.table;

import gov.epa.mims.analysisengine.gui.ScreenUtils;
import gov.epa.mims.analysisengine.table.db.ResultSetTableModel;
import gov.epa.mims.analysisengine.table.filter.FilterCriteria;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/*
 * SortFilterTableDialog.java
 *
 * Created on April 25, 2005, 11:07 AM
 * @author  Parthee R Partheepan
 * @version $Id: SortFilterTableDialog.java,v 1.4 2006/11/01 15:33:37 parthee Exp $
 */
public class SortFilterTableDialog extends JFrame
{
   private SortFilterTablePanel tablePanel ;
   
   /** Creates a new instance of SortFilterTableDialog */
   private SortFilterTableDialog(ResultSet rs,String title)
   {
      setTitle(title);
      ResultSetTableModel model = new ResultSetTableModel(rs);
      tablePanel = new SortFilterTablePanel(this, model);
      JButton okButton = new JButton("Close");
      okButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            SortFilterTableDialog.this.dispose();
            SortFilterTableDialog.this.setVisible(false);
         }
      });
      JPanel buttonPanel = new JPanel();
      buttonPanel.add(okButton);
     
      Container container = this.getContentPane();
      container.setLayout(new BorderLayout());
      container.add(tablePanel,BorderLayout.CENTER);
      container.add(buttonPanel,BorderLayout.SOUTH);
      this.pack();
      this.setLocation(ScreenUtils.getPointToCenter(this));
   }
   
    /** Creates a new instance of SortFilterTableDialog */
   public SortFilterTableDialog(ResultSet rs,String title
           ,boolean modal,FilterCriteria criteria)
   {
       this(rs,title);
       filterRows(criteria);
       this.setVisible(true);
   }
   
   public void filterRows(FilterCriteria criteria)
   {
       tablePanel.filterRows(criteria);
   }
   
//   /**
//    * @param args the command line arguments
//    */
//   public static void main(String[] args)
//   {
//   }
   
}
