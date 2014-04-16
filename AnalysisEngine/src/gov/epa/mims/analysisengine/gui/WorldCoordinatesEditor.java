package gov.epa.mims.analysisengine.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.Serializable;

/**
 * Editor for world coordinates
 *
 * @author Prashant Pai, Alison Eyth
 * @version $Id: WorldCoordinatesEditor.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 *
 **/
public class WorldCoordinatesEditor extends OptionDialog implements Cloneable
{
  /** a panel to set the minimum x coordinate **/
  private DoubleValuePanel xminPanel = null;

  /** a panel to set the maximum x coordinate **/
  private DoubleValuePanel xmaxPanel = null;

  /** a panel to set the minimum y coordinate **/
  private DoubleValuePanel yminPanel = null;

  /** a panel to set the maximum y coordinate **/
  private DoubleValuePanel ymaxPanel = null;

  /** the world coordinates object to be edited **/
  private WorldCoordinates worldCoordinates = null;

  /******************************************************
   *
   * methods
   *
   *****************************************************/

  /**
   * constructor that edits a WorldCoordinates Object
   * @param aWorldCoordinates the worldcoordinates to be edited
   */
  public WorldCoordinatesEditor(WorldCoordinates aWorldCoordinates)
  {
    super();
    initialize();
    setDataSource(aWorldCoordinates, "");
  }//WorldCoordinatesEditor(WorldCoordinates)

  /**
   * constructor need for class.newInstance
   */
  public WorldCoordinatesEditor()
  {
    this(null);
  }//WorldCoordinatesEditor()

  public void setDataSource(Object dataSource, String optionName)
  {
     this.worldCoordinates = (WorldCoordinates) dataSource;
     // we are storing this object twice
     super.setDataSource(dataSource, optionName);
     if (worldCoordinates != null)
     {
        initGUIFromModel();
     }
     pack();
     this.repaint();
   }

   /**
    * a private method to initialize the GUI
    */
   private void initialize()
   {
     this.setModal(true);
     Container contentPane = getContentPane();
     setLocationRelativeTo(JOptionPane.getRootFrame());
     setTitle("Edit WorldCoordinates Properties");

     contentPane.setLayout(new BorderLayout());

     JPanel mainPanel = new JPanel(new BorderLayout());
     //AME: no more axis editor
     // mainPanel.setBorder(AxisEditor.getCustomBorder(null));
     contentPane.add(mainPanel, BorderLayout.CENTER);

     JPanel xPanel = new JPanel();
     JPanel yPanel = new JPanel();
     mainPanel.add(xPanel, BorderLayout.NORTH);
     mainPanel.add(yPanel, BorderLayout.SOUTH);

     xminPanel = new DoubleValuePanel("min X:", false);
     xmaxPanel = new DoubleValuePanel("max X:", false);
     yminPanel = new DoubleValuePanel("min Y:", false);
     ymaxPanel = new DoubleValuePanel("max Y:", false);

     xPanel.add(xminPanel);
     xPanel.add(xmaxPanel);
     yPanel.add(yminPanel);
     yPanel.add(ymaxPanel);

     contentPane.add(getButtonPanel(), BorderLayout.SOUTH);
     pack();
   }//initialize()

   /**
    * @pre the object i.e. WorldCoordinates is not null
    */
   protected void initGUIFromModel()
   {
      xminPanel.setValue(worldCoordinates.xmin);
      xmaxPanel.setValue(worldCoordinates.xmax);
      yminPanel.setValue(worldCoordinates.ymin);
      ymaxPanel.setValue(worldCoordinates.ymax);
   }//initGUIFromModel()

   protected void saveGUIValuesToModel() throws Exception
   {
      worldCoordinates.xmin = xminPanel.getValue();
      worldCoordinates.xmax = xmaxPanel.getValue();
      worldCoordinates.ymin = yminPanel.getValue();
      worldCoordinates.ymax = ymaxPanel.getValue();
   }//saveGUIValuesToModel()


   public static void main(String[] args)
   {
     DefaultUserInteractor.set(new GUIUserInteractor());
     try
     {
       WorldCoordinatesEditor worldCoordinatesEditor = new WorldCoordinatesEditor(new WorldCoordinates());
       worldCoordinatesEditor.setVisible(true);
     }
     catch(Exception e)
     {
       e.printStackTrace();
     }
   }//main()


}

