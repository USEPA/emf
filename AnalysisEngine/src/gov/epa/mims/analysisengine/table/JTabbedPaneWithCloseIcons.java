package gov.epa.mims.analysisengine.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.io.Serializable;

import javax.swing.Icon;
import javax.swing.JTabbedPane;

/**
 * TOOK IT FROM JAVA FORUM A JTabbedPane which has a close ('X') icon on each tab.
 * 
 * To add a tab, use the method addTab(String, Component)
 * 
 * To have an extra icon on each tab (e.g. like in JBuilder, showing the file type) use the method addTab(String,
 * Component, Icon). Only clicking the 'X' closes the tab.
 * 
 * @version $Id: JTabbedPaneWithCloseIcons.java,v 1.4 2007/05/31 14:29:31 qunhe Exp $
 */
public class JTabbedPaneWithCloseIcons extends JTabbedPane {

	public JTabbedPaneWithCloseIcons() {
		super();
		// this.setUI(new BasicTabbedPaneUI()
		// {
		// protected void layoutLabel(int tabPlacement, FontMetrics metrics,
		// int tabIndex, String title, Icon icon, Rectangle tabRect,
		// Rectangle iconRect, Rectangle textRect, boolean isSelected)
		// {
		//
		// textRect.x = 0; textRect.y = 0;
		// iconRect.x = 0; iconRect.y = 0;
		// SwingUtilities.layoutCompoundLabel((JComponent) tabPane, metrics,
		// title, icon, SwingUtilities.CENTER, SwingUtilities.CENTER,
		// SwingUtilities.CENTER, SwingUtilities.LEFT, tabRect, iconRect,
		// textRect, textIconGap + 2);
		//
		// }
		// });
		// addMouseListener(new MouseAdapter()
		// {
		// public void mouseClicked(MouseEvent e)
		// {
		// removeTab(e);
		// }
		// });

	}

	/**
	 * to remove a tab if String is null then tab is not removed
	 */
	public String removeTab(MouseEvent e) {
		int tabNo = this.getUI().tabForCoordinate(this, e.getX(), e.getY());
		if (tabNo < 0) {
			return null;
		}
		Rectangle rect = ((CloseTabIcon) getIconAt(tabNo)).getBounds();
		if (rect.contains(e.getX(), e.getY())) {
			String removeTabTitle = this.getTitleAt(tabNo);
			// System.out.println("TabNo="+tabNo);
			// System.out.println("removeTabTitle="+removeTabTitle);
			// the tab is being closed
			this.removeTabAt(tabNo);
			return removeTabTitle;
		}
		return null;
	}

	/**
	 * to remove a tab
	 * 
	 * @param MouseEvebt
	 * @return int index of the tab in which user clicked on the X else returns -1
	 * 
	 */
	public int toRemoveTabIndex(MouseEvent e) {
		int tabNo = this.getUI().tabForCoordinate(this, e.getX(), e.getY());
		if (tabNo < 0) {
			return -1;
		}
		Rectangle rect = ((CloseTabIcon) getIconAt(tabNo)).getBounds();
		if (rect.contains(e.getX(), e.getY())) {
			return tabNo;
		}
		return -1;
	}// toRemoveTabIndex(MouseEvent e)

	public void addTab(String title, Component component) {
		this.addTab(title, component, null);
	}

	public void addTab(String title, Component component, Icon extraIcon) {
		this.addTab(title, new CloseTabIcon(extraIcon), component);
	}

	public void addTabToRight(String title, Icon extraIcon, Component component, String tip) {
		int tabCount = this.getTabCount();
		super.insertTab(title, new CloseTabIcon(extraIcon), component, tip, tabCount);
	}

}

/**
 * The class which generates the 'X' icon for the tabs. The constructor accepts an icon which is extra to the 'X' icon,
 * so you can have tabs like in JBuilder. This value is null if no extra icon is required.
 */
class CloseTabIcon implements Icon, Serializable {

	static final long serialVersionUID = 1;
	
	private int x_pos;

	private int y_pos;

	private int width;

	private int height;

	private Icon fileIcon;

	public CloseTabIcon(Icon fileIcon) {
		// this.fileIcon=fileIcon;
		width = 16;
		height = 16;
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
		this.x_pos = x;
		this.y_pos = y;
		Color col = g.getColor();
		g.setColor(Color.black);
		int y_p = y + 2;
		g.drawLine(x + 1, y_p, x + 12, y_p);
		g.drawLine(x + 1, y_p + 13, x + 12, y_p + 13);
		g.drawLine(x, y_p + 1, x, y_p + 12);
		g.drawLine(x + 13, y_p + 1, x + 13, y_p + 12);
		g.drawLine(x + 3, y_p + 3, x + 10, y_p + 10);
		g.drawLine(x + 3, y_p + 4, x + 9, y_p + 10);
		g.drawLine(x + 4, y_p + 3, x + 10, y_p + 9);
		g.drawLine(x + 10, y_p + 3, x + 3, y_p + 10);
		g.drawLine(x + 10, y_p + 4, x + 4, y_p + 10);
		g.drawLine(x + 9, y_p + 3, x + 3, y_p + 9);
		g.setColor(col);
		if (fileIcon != null) {
			fileIcon.paintIcon(c, g, x + width, y_p);
		}
	}

	public int getIconWidth() {
		return width + (fileIcon != null ? fileIcon.getIconWidth() : 0);
	}

	public int getIconHeight() {
		return height;
	}

	public Rectangle getBounds() {
		return new Rectangle(x_pos, y_pos, width, height);
	}

}
