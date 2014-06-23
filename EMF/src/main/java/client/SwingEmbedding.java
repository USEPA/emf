import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 * A demo illustrating the embedding of a slider and a button control into a
 * JTable
 * <p/>
 * Copyright (C) 2010 by Ilya Volodarsky
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
public class SwingEmbedding extends JFrame 
{
    private static final long serialVersionUID = 6788933562312584275L;

    @SuppressWarnings({ "unchecked", "serial" })
    public SwingEmbedding()
    {
        initComponents();
        
        // important to set the columns that will have embedded elements as "Editable"
        DefaultTableModel model = new DefaultTableModel(new String [] {"Slider", "Button", "ComboBox1", "ComboBox2" }, 0) 
        {
                Class[] types = new Class[]
                {
                     java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
                };

                public Class getColumnClass(int columnIndex)
                {
                    return types[columnIndex];
                }

                boolean[] canEdit = new boolean [] {
                    true, true, true, true
                };

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit [columnIndex];
                }
        };
        table.setModel(model);
        
        // sets the table columns to use the button/slider renderer and editor component
        TableColumn sliderColumn = table.getColumnModel().getColumn(0);
        
        TableSliderElement slider = new TableSliderElement(50 /*Default Slider Value out of 100*/);
        slider.addHandler(new TableSliderElement.TableSliderMovedHandler() {
            
            public void onSlide(int row, int column, int value) 
            {
                // handle the slide event
            }
        });

        // set the column's renderer and editor as the control
        sliderColumn.setCellRenderer(slider);
        sliderColumn.setCellEditor(slider);
        
        TableColumn buttonColumn = table.getColumnModel().getColumn(1);

        TableButton button = new TableButton(new TableButton.TableButtonCustomizer()
        {
            public void customize(JButton button, int row, int column)
            {
                button.setIcon(new ImageIcon("mail.gif"));
            }
        });
        button.addHandler(new TableButton.TableButtonPressedHandler() {
            
            public void onButtonPress(int row, int column) {
                // TODO Auto-generated method stub
                
            }
        });
        
        buttonColumn.setCellRenderer(button);
        buttonColumn.setCellEditor(button);

//        TableColumn comboBoxColumn2 = table.getColumnModel().getColumn(3);
//
//        final TableComboBox comboBox2 = new TableComboBox();
//        comboBox2.addHandler(new TableComboBox.TableComboBoxPressedHandler() {
//            
//            public void onButtonPress(int row, int column) {
//                // TODO Auto-generated method stub
//                
//            }
//        });
//        
//        comboBoxColumn2.setCellRenderer(comboBox2);
//        comboBoxColumn2.setCellEditor(comboBox2);
//
//        TableColumn comboBoxColumn = table.getColumnModel().getColumn(2);
//
//        final TableComboBox comboBox = new TableComboBox();
//        comboBox.addHandler(new TableComboBox.TableComboBoxPressedHandler() {
//            
//            public void onButtonPress(int row, int column) {
//                // TODO Auto-generated method stub
//                JComboBox comboBox = comboBox2.getComboBox(row);
//                System.out.println(comboBox);
//                comboBox.removeAll();
//                comboBox.addItem("test4");
//                comboBox.addItem("test5");
//                comboBox.addItem("test6");
//                comboBox.addItem("test7");
////                System.out.println(((TableComboBox)table.getCellEditor(row, column + 1)).getCellEditorValue());
//            }
//        });
//        
//        comboBoxColumn.setCellRenderer(comboBox);
//        comboBoxColumn.setCellEditor(comboBox);

    }

    public void addElement()
    {
        DefaultTableModel model = (DefaultTableModel) table.getModel(); 
        model.addRow(new Object[] { 0.00, "Hello", "Chaka", "SleazeStack" });
    }

    /**
     * Initializes components, code generated by NetBeans GUI editor
     */
    private void initComponents() 
    {
        setTitle("Swing Embedding Demo");
        
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jScrollPane1.setViewportView(table);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
                getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(
                javax.swing.GroupLayout.Alignment.LEADING).addComponent(
                jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400,
                Short.MAX_VALUE));
        layout.setVerticalGroup(layout.createParallelGroup(
                javax.swing.GroupLayout.Alignment.LEADING).addComponent(
                jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300,
                Short.MAX_VALUE));

        pack();
    }

    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable table;

    public static void main(String[] args) 
    {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        } 
        
        SwingEmbedding embedding = new SwingEmbedding();
        embedding.setVisible(true);
        
        embedding.addElement();
        embedding.addElement();
    }

}
