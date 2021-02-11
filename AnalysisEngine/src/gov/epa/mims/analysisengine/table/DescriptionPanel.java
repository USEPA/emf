
package gov.epa.mims.analysisengine.table;

import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.*;

/*
 * DescriptionPanel.java
 * A panel that have a text field that displays the first line of the information and
 * have a button. User can view the full description by clicking the button
 * Created on March 17, 2004, 1:46 PM
 * @author  Parthee Partheepan
 * @version $Id: FilterColumnGUI.java,v 1.2 2004/03/19 20:20:57 eyth Exp
 */

public class DescriptionPanel extends JPanel
{
   /** to store the description from the file */
   private String fileDescription;

   /** to store the modified description*/
   private String description;

   /** a text field to show only one line */
   private JTextField oneLineTextField;

   /** a text area */
   private JTextArea textArea;

   /** dialog for the text area to appear
    */
   private JDialog dialog;

   /** Creates a new instance of DescriptionPanel
    * @param description information for the textField
    * @param labelString string for the label
    */
   public DescriptionPanel(String info, String labelString, String fileName)
   {
      if(info == null || info.trim().equalsIgnoreCase("null"))
      {
         info = "";
      }
      this.fileDescription = info;
      this.description = info;
      //this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
      this.setLayout(new BorderLayout());
      JLabel label = new JLabel(labelString);
      this.add(label, BorderLayout.WEST);

      oneLineTextField = new JTextField();
      setText(description);
      oneLineTextField.setHorizontalAlignment(JTextField.LEFT);
      oneLineTextField.setPreferredSize(new Dimension(400, 20));

      this.add(oneLineTextField,BorderLayout.CENTER);

      JButton button = new JButton("Full Description");
      button.setMnemonic(KeyEvent.VK_D);
      this.add(button, BorderLayout.EAST);
      final String titleString = labelString + " Description:"+fileName;
      button.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            createTextAreaDialog(titleString);
         }
      });//addActionListener()
   }


   /** a helper method create a text area with in the dialog
    */
   private void createTextAreaDialog(String titleString)
   {
      dialog = new JDialog();
      dialog.setModal(true);
      dialog.setTitle(titleString);
      dialog.setLocationRelativeTo(this);
      textArea = new JTextArea();
      textArea.setText(description);
      textArea.setEditable(true);
      textArea.setFont(new Font("Arial", Font.BOLD, 12));
      textArea.setLineWrap(true);
      textArea.setWrapStyleWord(true);

      JScrollPane scrollPane = new JScrollPane(textArea,
         JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
         JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      scrollPane.setPreferredSize(new Dimension(500, 200));
      JPanel mainPanel = new JPanel();
      mainPanel.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
      mainPanel.setLayout(new BorderLayout());
      mainPanel.add(scrollPane);

      JPanel okCancelPanel = new JPanel();
      JButton okButton = new JButton("OK");
      okButton.setMnemonic(KeyEvent.VK_O);
      okButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
           description = textArea.getText();
           setText(description);
           dialog.dispose();
         }
      });
      JButton cancelButton = new JButton("Cancel");
      cancelButton.setMnemonic(KeyEvent.VK_L);
      cancelButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
           textArea.setText(description);
           setText(description);
           dialog.dispose();
         }
      });
      JButton resetButton = new JButton("Reset");
      resetButton.setMnemonic(KeyEvent.VK_R);
      resetButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            textArea.setText(fileDescription);
            setText(fileDescription);
         }
      });
      okCancelPanel.add(okButton);
      okCancelPanel.add(cancelButton);
      okCancelPanel.add(resetButton);
      Container contentPane = dialog.getContentPane();
      contentPane.setLayout(new BorderLayout());
      contentPane.add(mainPanel, BorderLayout.CENTER);
      contentPane.add(okCancelPanel, BorderLayout.SOUTH);
      dialog.pack();
      dialog.setLocation(ScreenUtils.getPointToCenter(dialog));
      dialog.setVisible(true);

   }//createTextAreaDialog()

   /** Getter for property description.
    * @return Value of property description.
    *
    */
   public String getDescription()
   {
      return description;
   }

   /** set the text of the textField through this method so that '\n' is replaced
    *with ' '
    */
   private void setText(String description)
   {
      if(description == null)
      {
         throw new IllegalArgumentException("description cannot be null");
      }
      oneLineTextField.setText(description.replace('\n', ' '));
   }

   /**
    * @param args the command line arguments
    */
   public static void main(String[] args)
   {
      //String description = "hsgknhhhhhhhhhhhhhhhhhhh \n jjkgfgkdflgkkkgkkkkkkkkkkkkkkkkk";
      String description = null;
      JFrame f = new JFrame();
      String fileName = "tmp.txt";
      DescriptionPanel panel = new DescriptionPanel(description,"Header",fileName);
      f.getContentPane().add(panel);
      f.pack();
      f.setVisible(true);
   }

}

