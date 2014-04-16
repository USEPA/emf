package gov.epa.mims.analysisengine.table.db;

import gov.epa.mims.analysisengine.gui.DefaultUserInteractor;
import gov.epa.mims.analysisengine.gui.OptionDialog;
import gov.epa.mims.analysisengine.gui.ScreenUtils;
import gov.epa.mims.analysisengine.gui.UserInteractor;

import java.awt.Container;
import java.awt.Font;
import java.awt.BorderLayout;
import javax.swing.*;

/**
 * 
 * @author parthee
 */
public class DBQueryViewer extends OptionDialog {
	/** The string for the query */
	private String query = "";

	private String tabName = "";

	/** Name of the db */
	private String dbName = "";

	private JTextField tabNameTF;

	private JTextArea queryTA;

	private JTextField dbNameTF;

	/** Creates a new instance of DBQuery */
	public DBQueryViewer(JFrame owner) {
		super(owner);
		setTitle("Database Query");
		init();
		pack();
		setLocation(ScreenUtils.getPointToCenter(this));
		setModal(true);
		setSize(400, 150);
		show();

	}

	private void init() {
		JLabel tabNameL = new JLabel("Tab Name: ");
		tabNameTF = new JTextField(tabName);
		JPanel tabNamePanel = new JPanel();
		tabNamePanel.setLayout(new BoxLayout(tabNamePanel, BoxLayout.X_AXIS));
		tabNamePanel.add(tabNameL);
		tabNamePanel.add(tabNameTF);

		JLabel dbNameL = new JLabel("DB Name: ");
		dbNameTF = new JTextField();
		JPanel dbNamePanel = new JPanel();
		dbNamePanel.setLayout(new BoxLayout(dbNamePanel, BoxLayout.X_AXIS));
		dbNamePanel.add(dbNameL);
		dbNamePanel.add(Box.createHorizontalStrut(3));
		dbNamePanel.add(dbNameTF);

		JLabel queryL = new JLabel("Query: ");
		queryTA = new JTextArea(query);
		queryTA.setText(query);
		queryTA.setEditable(true);
		queryTA.setFont(new Font("Arial", Font.BOLD, 12));
		queryTA.setLineWrap(true);
		queryTA.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(queryTA, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		JPanel queryPanel = new JPanel();
		queryPanel.setLayout(new BoxLayout(queryPanel, BoxLayout.X_AXIS));
		queryPanel.add(queryL);
		queryPanel.add(Box.createHorizontalStrut(20));
		queryPanel.add(scrollPane);

		JPanel subPanel = new JPanel();
		subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.Y_AXIS));
		subPanel.add(tabNamePanel);
		subPanel.add(Box.createVerticalStrut(3));
		subPanel.add(dbNamePanel);
		subPanel.add(Box.createVerticalStrut(3));
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(subPanel, BorderLayout.NORTH);
		mainPanel.add(queryPanel);

		Container container = getContentPane();
		container.setLayout(new BorderLayout());
		container.add(mainPanel, BorderLayout.CENTER);
		container.add(getButtonPanel(), BorderLayout.SOUTH);
	}

	public void initGUIFromModel() {
		// Empty
	}

	public void saveGUIValuesToModel() {
		tabName = tabNameTF.getText();
		if (tabName.trim().length() == 0) {
			shouldContinueClosing = false;
			DefaultUserInteractor.get().notify(this, "Error", "Please enter a tab name", UserInteractor.ERROR);
			return;
		}

		dbName = dbNameTF.getText();
		if (dbName.trim().length() == 0) {
			shouldContinueClosing = false;
			DefaultUserInteractor.get().notify(this, "Error", "Please enter a database name", UserInteractor.ERROR);
			return;
		}
		query = queryTA.getText();
		if (query.trim().length() == 0) {
			shouldContinueClosing = false;
			DefaultUserInteractor.get().notify(this, "Error", "Please enter a query", UserInteractor.ERROR);
			return;
		}
	}

	public String getQuery() {
		return query;
	}

	public String getDBName() {
		return dbName;
	}

	public String getTabName() {
		return tabName;
	}

}
