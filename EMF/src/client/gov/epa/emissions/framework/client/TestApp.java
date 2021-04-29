package gov.epa.emissions.framework.client;

/*import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JLabel;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
 
public class TestApp extends JPanel {
    private boolean DEBUG = false;
 
    public TestApp() {
        super(new GridLayout(1,0));
 
        String[] columnNames = {"First Name",
                                "Last Name",
                                "Sport",
                                "# of Years",
                                "Vegetarian"};
 
        Object[][] data = {
        {"Kathy", "Smith",
         "Snowboarding", new Integer(5), new Boolean(false)},
        {"John", "Doe",
         "Rowing", new Integer(3), new Boolean(true)},
        {"Sue", "Black",
         "Knitting", new Integer(2), new Boolean(false)},
        {"Jane", "White",
         "Speed reading", new Integer(20), new Boolean(true)},
        {"Joe", "Brown",
         "Pool", new Integer(10), new Boolean(false)}
        };
 
        final JTable table = new JTable(data, columnNames);
        table.getAccessibleContext().setAccessibleName("this is the table");
        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true);
        table.getTableHeader().setToolTipText("table header");
        System.out.println(table.getAccessibleContext().getAccessibleTable().getAccessibleColumnDescription(0));
        table.getAccessibleContext().getAccessibleTable().setAccessibleColumnDescription(0, new JLabel(columnNames[0]));
 
        if (DEBUG) {
            table.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    printDebugData(table);
                }
            });
        }
 
        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);
 
        //Add the scroll pane to this panel.
        add(scrollPane);
    }
 
    private void printDebugData(JTable table) {
        int numRows = table.getRowCount();
        int numCols = table.getColumnCount();
        javax.swing.table.TableModel model = table.getModel();
 
        System.out.println("Value of data: ");
        for (int i=0; i < numRows; i++) {
            System.out.print("    row " + i + ":");
            for (int j=0; j < numCols; j++) {
                System.out.print("  " + model.getValueAt(i, j));
            }
            System.out.println();
        }
        System.out.println("--------------------------");
    }
 
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("SimpleTableDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        //Create and set up the content pane.
        TestApp newContentPane = new TestApp();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
 
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}*/
import java.awt.*;
import javax.swing.*;

public class TestApp {
    public static void main(String args[]) {

        //Creating the Frame
        JFrame frame = new JFrame("Chat Frame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);

        //Creating the MenuBar and adding components
        JMenuBar mb = new JMenuBar();
        JMenu m1 = new JMenu("File");
        JMenu m2 = new JMenu("Help");
        mb.add(m1);
        mb.add(m2);
        JMenuItem m11 = new JMenuItem("Open");
        JMenuItem m22 = new JMenuItem("Save as");
        m1.add(m11);
        m1.add(m22);

        //Creating the panel at bottom and adding components
        JPanel panel = new JPanel(); // the panel is not visible in output
        JLabel label = new JLabel("Enter Text");
        System.out.println("label name: " + label.getAccessibleContext().getAccessibleName());
        System.out.println("label descr: " + label.getAccessibleContext().getAccessibleDescription());
        label.getAccessibleContext().setAccessibleDescription("foo");
        JTextField tf = new JTextField(10); // accepts upto 10 characters
        System.out.println("tf name: " + tf.getAccessibleContext().getAccessibleName());
        System.out.println("tf descr: " + tf.getAccessibleContext().getAccessibleDescription());
        label.setLabelFor(tf);
        System.out.println("tf name 2: " + tf.getAccessibleContext().getAccessibleName());
        System.out.println("tf descr 2: " + tf.getAccessibleContext().getAccessibleDescription());
        tf.setToolTipText("tool tip");
        System.out.println("tf descr 3: " + tf.getAccessibleContext().getAccessibleDescription());
        tf.getAccessibleContext().setAccessibleDescription("text goes here");
        System.out.println("tf descr 4: " + tf.getAccessibleContext().getAccessibleDescription());
        JButton send = new JButton("Send");
        JButton reset = new JButton("Reset");
        panel.add(label); // Components Added using Flow Layout
        panel.add(tf);
        panel.add(send);
        panel.add(reset);
        
        JRadioButton radio = new JRadioButton("a button");
        radio.getAccessibleContext().setAccessibleDescription("it's a radio button");
        System.out.println("radio name: " + radio.getAccessibleContext().getAccessibleName());
        System.out.println("radio descr: " + radio.getAccessibleContext().getAccessibleDescription());
        label.setLabelFor(radio);
        System.out.println("radio name: " + radio.getAccessibleContext().getAccessibleName());
        System.out.println("radio descr: " + radio.getAccessibleContext().getAccessibleDescription());
        
        JLabel label1 = new JLabel("i'm a label");
        label1.setName("yo soy un label");
        label1.setToolTipText("label tool tip");
        System.out.println(label1.getAccessibleContext().getAccessibleName());
        System.out.println(label1.getAccessibleContext().getAccessibleDescription());
        JButton button1 = new JButton("i'm a button");
        System.out.println(button1.getAccessibleContext().getAccessibleName());
        JCheckBox box1 = new JCheckBox("i'm a checkbox");
        System.out.println(box1.getAccessibleContext().getAccessibleName());
        JPasswordField pass1 = new JPasswordField("i'm a password field");
        pass1.setName("yo soy un password field");
        System.out.println(pass1.getAccessibleContext().getAccessibleName());
        String[] petStrings = { "Bird", "Cat", "Dog", "Rabbit", "Pig" };
        JComboBox combo1 = new JComboBox(petStrings);
        combo1.setName("pets");
        System.out.println(combo1.getAccessibleContext().getAccessibleName());
        JMenu menu1 = new JMenu("i'm a menu");
        System.out.println(menu1.getAccessibleContext().getAccessibleName());
        JMenuItem menuItem1 = new JMenuItem("i'm a menu item");
        System.out.println(menuItem1.getAccessibleContext().getAccessibleName());
        

        // Text Area at the Center
        JTextArea ta = new JTextArea();
        ta.getAccessibleContext().setAccessibleName("assigned name");
        ta.getAccessibleContext().setAccessibleDescription("assigned description");

        //Adding Components to the frame.
        frame.getContentPane().add(BorderLayout.SOUTH, panel);
        frame.getContentPane().add(BorderLayout.NORTH, mb);
        frame.getContentPane().add(BorderLayout.CENTER, ta);
        frame.setVisible(true);
    }
}