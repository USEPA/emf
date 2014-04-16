package gov.epa.mims.analysisengine.help;

import gov.epa.mims.analysisengine.gui.DefaultUserInteractor;
import gov.epa.mims.analysisengine.gui.ScreenUtils;
import gov.epa.mims.analysisengine.gui.UserInteractor;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

/*
 * HelpMenu.java
 * Resources used by multiple classes to implement help menus
 *
 * @author Steve Howard
 * @version $Id: HelpMenu.java,v 1.3 2005/09/27 14:02:01 parthee Exp $
 * Created on October 15, 2004, 11:14 AM
 */

public class HelpMenu
{


/******************************************************
 *
 * methods
 *
 *****************************************************/


/******************************************************
 *
 * static methods
 *
 *****************************************************/

   protected static URL hsURL = null;
   static
   {
      try
      {
         hsURL = ClassLoader.getSystemResource(
            "gov/epa/mims/help/sua_tool_help.hs");
            //"gov/epa/mims/help/MIMS_Help.hs");
      }
      catch(Exception e)
      {
         DefaultUserInteractor.get().notify(null,"Error","The sua_tool_help.hs "
            + "file has failed to load",UserInteractor.ERROR);
      }
   }
   protected static HelpBroker helpBroker = null;
   protected static String view = null;
   protected static String id = null;

   // help views
   public static final String TABLE_OF_CONTENTS = "TOC";
   public static final String INDEX = "Index";
   public static final String SEARCH = "Search";

   // page ids
   public static final String TABLEAPP_WINDOW = "sensitivity_intro";

   /**
    * return a help menu
    *
    * @param  view is the helpset view to make active
    * @param  id is the helpset page id to make active
    *
    * note:  see hs file "MIMS_Help.hs" to find list of views
    *        see jh map file "MIMS_Help_map.xml" to find list of ids
    **/
   public static JMenu createStandardMenu(String view, String id, Component parent)
   {
      // create Help Menu
      JMenu helpMenu = new JMenu();
      helpMenu.setText("Help");
      helpMenu.setMnemonic('H');

      // create a helpBroker for a user guide menu item
      if(helpBroker==null)
      {
         try
         {
            // create helpbroker from hsURL file
            HelpSet helpset = new HelpSet(null, hsURL);
            helpBroker = helpset.createHelpBroker();
         }
         catch (Exception ee)
         {
            System.out.println("Help Broker not generated for user guide");
         }
      }

      // if helpBroker is valid, then add Menu Item for user Guide
      if( helpBroker!=null )
      {
         // AME: commented out until the help is more finalized
         //helpMenu.add(createHelpMenuItem(view, id,parent));
      }

      // add an about menu item to helpMenu
//      JMenuItem aboutItem = new JMenuItem();
//      aboutItem.setText("About S & U Tool");
//      helpMenu.add(aboutItem);
//      aboutItem.addActionListener( new ActionListener()
//      {
//         public void actionPerformed(ActionEvent evt)
//         {
//            displayAboutDialog();
//         }
//      });

      return helpMenu;
   }

   /**
    * return a help menu item
    *
    * @param  view is the helpset view to make active
    * @param  id is the helpset page id to make active
    *
    * note:  see hs file "MIMS_Help.hs" to find list of views
    *        see jh map file "MIMS_Help_map.xml" to find list of ids
    **/
   public static JMenuItem createHelpMenuItem(String view, String id,Component parent)
   {
      HelpMenu.view = view;
      HelpMenu.id = id;
      final Component finalParent = parent;
      // create Help Menu
      JMenuItem userGuideItem = new JMenuItem("User Guide");

      // create a helpBroker for a user guide menu item
      if(helpBroker==null)
      {
         try
         {
            // create helpbroker from hsURL file
            HelpSet helpset = new HelpSet(null, hsURL);
            helpBroker = helpset.createHelpBroker();
         }
         catch (Exception ee)
         {
            System.out.println("Help Broker not generated for user guide");
         }
      }

      // if helpBroker is valid, then add Menu Item userGuideItem
      if( helpBroker!=null )
      {
         userGuideItem.addActionListener(new ActionListener()
           {
              String view = HelpMenu.view;
              String id = HelpMenu.id;
              public void actionPerformed( ActionEvent ev )
              {
                 try
                 {
                    helpBroker.setCurrentView(view);
                    helpBroker.setCurrentID(id);
                 }
                 catch(Exception ex)
                 {
                    System.out.println("Help view ["+view+":"+id+"] not found.");
                 }
                 //centering the location needs more work
                 helpBroker.setLocation(ScreenUtils.getPointToCenter(finalParent));
                 helpBroker.setDisplayed(true);
              }
           });
      }

      return userGuideItem;
   }

//   /**
//    * return a help JButton
//    *
//    * @param  view is the helpset view to make active
//    * @param  id is the helpset page id to make active
//    *
//    * note:  see hs file "MIMS_Help.hs" to find list of views
//    *        see jh map file "MIMS_Help_map.xml" to find list of ids
//    **/
//   public static JButton createHelpButton(String view, String id)
//   {
//      HelpMenu.view = view;
//      HelpMenu.id = id;
//
//      // create Help Button
//      JButton helpButton = new JButton("Help");
//
//      // create a helpBroker for a user guide menu item
//      if(helpBroker==null)
//      {
//         try
//         {
//            // create helpbroker from hsURL file
//            HelpSet helpset = new HelpSet(null, hsURL);
//            helpBroker = helpset.createHelpBroker();
//         }
//         catch (Exception ee)
//         {
//            System.out.println("Help Broker not generated for user guide");
//         }
//      }
//
//      // if helpBroker is valid, then add help action tp button
//      if( helpBroker!=null )
//      {
//         helpButton.addActionListener(new ActionListener()
//           {
//              String view = HelpMenu.view;
//              String id = HelpMenu.id;
//              public void actionPerformed( ActionEvent ev )
//              {
//                 try
//                 {
//                    helpBroker.setCurrentView(view);
//                    helpBroker.setCurrentID(id);
//                 }
//                 catch(Exception ex)
//                 {
//                    System.out.println("Help view ["+view+":"+id+"] not found.");
//                 }
//                 helpBroker.setDisplayed(true);
//              }
//           });
//      }
//
//      return helpButton;
//   }


//   /**
//    * method displays an "About Dialog"
//    **/
//   public static void displayAboutDialog()
//   {
//        // MIMS URLs
//        final LabeledPair[] urls = new LabeledPair[]
//            { new LabeledPair("Home Page", "http://www.epa.gov/asmdnerl/mims"),
//              new LabeledPair("User Support", "http://sf.net/tracker/?atid=390710&group_id=27492"),
//              new LabeledPair("Report/Review Bugs", "http://sf.net/tracker/?group_id=27492&atid=390709"),
//              new LabeledPair("Add/Review Feature Requests", "http://sf.net/tracker/?atid=390712&group_id=27492") };
//
//        // create edit dialog
//        final MDialog dialog = new MDialog(new Frame(), "About MIMS", true);
//
//        // epa Image
//        final Image epaImage = Toolkit.getDefaultToolkit().getImage(dialog.getClass().
//                                        getResource("/gov/epa/mims/help/epa.gif"));
//
//        // place dialog in center of screen
//        Dimension scrnSize = Toolkit.getDefaultToolkit().getScreenSize();
//        int w = (int)(scrnSize.getWidth()/1.5);
//        int h = (int)(scrnSize.getHeight()/2.5);
//        int x = (int)((scrnSize.getWidth()-w)/2.0);
//        int y = (int)((scrnSize.getHeight()-h)/2.0);
//        dialog.setBounds(x,y,w,h);
//        dialog.setResizable(true);
//
//        // set screen Layout
//        java.awt.Container contentPane = dialog.getContentPane();
//        contentPane.setLayout(new BorderLayout());
//
//        // add dialog Window listener
//        dialog.addWindowListener(new java.awt.event.WindowAdapter()
//          {
//             public void windowClosing(WindowEvent ev)
//             {
//                dialog.setVisible(false);
//             }
//          });
//
//        // set version String
//        String version = "<Development Version>";
//        String versionStr = MIMSProperties.getVersion();
//        if(!versionStr.equals("<RELEASE_DATE>"))
//        {
//           version = "Release Date: " + versionStr;
//        }
//
//        // create message panel and add to dialog
//        Box messagePanel = new Box(BoxLayout.Y_AXIS)
//        {
//           public void paint(Graphics g)
//           {
//              super.paint(g);
//              g.drawImage(epaImage, 0, 0, (int)(getHeight()*1.4), getHeight(), this);
//           }
//        };
//        messagePanel.add( new TextPanel("Multimedia Integrated Modeling System Framework", FlowLayout.CENTER) );
//        messagePanel.add( new TextPanel(version, FlowLayout.CENTER) );
//        contentPane.add(messagePanel, BorderLayout.NORTH);
//
//        // create table panel to display MIMS URLs and add to dialog
//        // create the data model
//        TableModel dataModel = new AbstractTableModel()
//        {
//            public int getColumnCount() { return 1; }
//            public int getRowCount() { return urls.length; }
//            public Object getValueAt( int row, int col )
//            {
//               JLabel link = new JLabel( (String)(urls[row].getValue()) )
//               {
//                  public void paint(Graphics g)
//                  {
//                     // underline text in label
//                     g.drawLine(0, getHeight()-2, getWidth(), getHeight()-2);
//                     super.paint(g);
//                  }
//               };
//
//               link.setForeground(Color.blue);
//               return new LabeledField(urls[row].getLabel()+ ":", link);
//            }
//            public Class getColumnClass(int col){ return JPanel.class; }
//            public String getColumnName(int col){ return "MIMS URLs"; }
//        };
//
//        // create the table
//        final JTable table = new JTable(dataModel);
//        table.setBackground( MIMSProperties.getNonHighLightColor() );
//        table.setSelectionBackground( MIMSProperties.getNonHighLightColor() );
//        table.setRowSelectionAllowed(true);
//        table.setShowGrid(false);
//        table.setRowHeight( (int)(1.6*table.getRowHeight()) );
//        table.setDefaultRenderer(JPanel.class, new DefaultTableCellRenderer()
//        {
//           public java.awt.Component getTableCellRendererComponent(
//                                               JTable table, Object value,
//                                               boolean isSelected, boolean hasFocus,
//                                               int row, int column)
//           {
//              return (JPanel) value;
//           }
//        });
//
//        contentPane.add(new JScrollPane(table), BorderLayout.CENTER);
//
//        // add table listener
//        table.addMouseListener( new MouseListener()
//        {
//           public void mouseClicked(MouseEvent ev)
//           {
//              // find row and column of mouse position
//              int row = table.rowAtPoint( new Point(ev.getX(), ev.getY()) );
//
//              // view url with viewHTML
//              try
//              {
//                  Cursor saveCursor = dialog.getCursor();
//                  dialog.setCursor(new Cursor(Cursor.WAIT_CURSOR));
//                  viewHTML( (String)(urls[row].getValue()) );
//                  dialog.setCursor(saveCursor);
//              }
//              catch(Exception ex)
//              {
//                  DefaultUserInteractor.get().notifyOfException(
//                       "Cannot locate matching viewer for URL:"+urls[row].getValue(),
//                       ex, UserInteractor.ERROR);
//              }
//           }
//           public void mouseEntered(MouseEvent ev) { }
//           public void mouseExited(MouseEvent ev) { }
//           public void mousePressed(MouseEvent ev) { }
//           public void mouseReleased(MouseEvent ev) { }
//        });
//
//        // create Button panel and add to dialog
//        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
//        JButton OKButton = new JButton("Close");
//        OKButton.addActionListener(new ActionListener()
//        {
//           public void actionPerformed( ActionEvent ev )
//           {
//              dialog.setVisible(false);
//           }
//        });
//        buttonPanel.add(OKButton);
//        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        // show dialog
//        dialog.setVisible(true);
//        dialog.dispose();
//   }

//   /**
//    * static method to view url using associated file viewers from the AdminInfo's viewerList
//    */
//    static public void viewHTML(String urlName) throws Exception
//    {
//       String defaultBrowser = (System.getProperty("os.name").toUpperCase().startsWith("WIN"))
//                                ? "iexplore.exe %s" : "netscape %s";
//
//       // check for viewer type for html
//       String cmd = AdminInfo.getViewerCmd("html");
//
//       // check to use default viewer
//       if(cmd!=null && cmd.equalsIgnoreCase("default") )
//       {
//          cmd = defaultBrowser;
//       }
//
//       // if cmd == blank string, set it to null
//       if(cmd!=null && cmd.trim().length()==0) cmd = null;
//
//       // set useWinAssociation flag
//       boolean useWinAssociation = false;
//       if( System.getProperty("os.name").toUpperCase().startsWith("WIN") )
//       {
//          useWinAssociation = AdminInfo.useWinAssociation("html");
//       }
//
//       // use useWinAssociation flag is set, submit local command using "start"
//       if( useWinAssociation )
//       {
//          // define Windows Start command
//          String winStartCmd = System.getProperty("os.name").startsWith("Windows 9")
//                             ? "start " : "cmd /C start /B ";
//          // if cmd is not null, remove %s and append it to winStartCmd
//          if(cmd!=null)
//          {
//             if( cmd.indexOf("%s")>0 ) cmd = cmd.substring(0, cmd.indexOf("%s"));
//             winStartCmd += cmd;
//          }
//          winStartCmd += "\""+urlName+"\"";
//          Runtime.getRuntime().exec(winStartCmd);
//          return;
//       }
//
//       // if cmd not defined, prompt user
//       if( cmd==null )
//       {
//           cmd = DefaultUserInteractor.get().getString(
//                  "No Viewer found for file:"+urlName,
//                  "Enter Viewer Command:",defaultBrowser);
//           if( cmd !=null && cmd.indexOf("%s")<0 ) cmd += " %s";
//       }
//
//       //  build and execute view command
//       if( cmd!=null )
//       {
//          // replace %s with filename in cmd
//          while( cmd.indexOf("%s") >= 0 )
//          {
//             int pos = cmd.indexOf("%s");
//             cmd = cmd.substring(0,pos) + "\""+urlName+"\"" + cmd.substring(pos+2);
//          }
//
//          // execute the view command using the LocalExecutor
//          Runtime.getRuntime().exec(cmd);
//       }
//    }


   /**
    * inner class for layout a String
    */
   protected static class TextPanel extends JPanel
   {
      protected TextPanel( String s, int just )
      {
         setLayout( new FlowLayout( just ) );
         add( new JLabel(s) );
      }
   }

}


