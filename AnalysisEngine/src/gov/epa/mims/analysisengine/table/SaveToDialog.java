package gov.epa.mims.analysisengine.table;

import gov.epa.mims.analysisengine.gui.GUIUserInteractor;
import gov.epa.mims.analysisengine.gui.ScreenUtils;
import gov.epa.mims.analysisengine.gui.UserInteractor;
import gov.epa.mims.analysisengine.table.io.FileImportGUI;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

/**
 * Class SaveToDialog - dialog displaying file type choice and option to choose file using JFileChooser
 * 
 * @author Krithiga Thangavelu, CEP, UNC CHAPEL HILL.
 * @version $Id: SaveToDialog.java,v 1.6 2006/12/21 16:29:54 parthee Exp $
 */

public class SaveToDialog extends javax.swing.JDialog {
	private JLabel jLabel3;

	private JButton bCancel;

	private JButton bOK;

	private JPanel jPanel3;

	private JButton bBrowse;

	private JTextField tDirName;

	private JLabel lDirName;

	private JPanel jPanel2;

	private JComboBox cbFileTypes;

	private JLabel lFileType;

	private JPanel jPanel1;

	private String[] filetypes = { "JPEG (*.jpg)", "PostScript (*.ps)", "PDF (*.pdf)", "PNG (*.png)",
			"Latex Picture Files (*.ptx)" };

	public static int JPEG = 0;

	public static int PS = 1;

	public static int PDF = 2;

	public static int PNG = 3;

	public static int PTX = 4;

	public static int CANCEL = -1;

	public static int APPROVE = 0;

	private int fileType = 0;

	private String path;

	private int retVal = CANCEL;

	private CurrentDirectory currentDirectory;

	public SaveToDialog(Dialog parent, CurrentDirectory currentDirectory) {
		super(parent);
		this.currentDirectory = currentDirectory;
		initGUI();
		setLocation(ScreenUtils.getPointToCenter(this));
	}

	/**
	 * Initializes the GUI.
	 */
	public void initGUI() {
		try {

			jPanel1 = new JPanel();
			lFileType = new JLabel();
			cbFileTypes = new JComboBox();
			jPanel2 = new JPanel();
			lDirName = new JLabel();
			jLabel3 = new JLabel();
			tDirName = new JTextField();
			bBrowse = new JButton();
			jPanel3 = new JPanel();
			bOK = new JButton();
			bCancel = new JButton();

			BoxLayout thisLayout = new BoxLayout(this.getContentPane(), 1);

			this.getContentPane().setLayout(thisLayout);
			this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			this.setResizable(false);
			this.setTitle("Save Plots to");
			// this.setUndecorated(false);
			this.setModal(true);
			this.setSize(new java.awt.Dimension(344, 187));

			jPanel1.setPreferredSize(new java.awt.Dimension(337, 46));
			this.getContentPane().add(jPanel1);

			lFileType.setText("Select File Type");
			lFileType.setFont(new java.awt.Font("Dialog", 1, 14));
			lFileType.setPreferredSize(new java.awt.Dimension(127, 23));
			jPanel1.add(lFileType);

			cbFileTypes.setPreferredSize(new java.awt.Dimension(181, 28));
			cbFileTypes.setModel(new DefaultComboBoxModel(filetypes));
			jPanel1.add(cbFileTypes);

			jPanel2.setPreferredSize(new java.awt.Dimension(337, 70));
			this.getContentPane().add(jPanel2);

			lDirName.setText("Directory Name");
			lDirName.setVisible(true);
			lDirName.setFont(new java.awt.Font("Dialog", 1, 14));
			lDirName.setPreferredSize(new java.awt.Dimension(169, 21));
			jPanel2.add(lDirName);

			jLabel3.setVisible(true);
			jLabel3.setPreferredSize(new java.awt.Dimension(141, 13));
			jPanel2.add(jLabel3);

			// tDirName.setText();
			tDirName.setPreferredSize(new java.awt.Dimension(235, 27));
			jPanel2.add(tDirName);

			bBrowse.setText("Browse");
			bBrowse.setPreferredSize(new java.awt.Dimension(81, 29));
			jPanel2.add(bBrowse);
			bBrowse.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					bBrowseActionPerformed(evt);
				}
			});

			jPanel3.setPreferredSize(new java.awt.Dimension(336, 36));
			this.getContentPane().add(jPanel3);

			bOK.setText("OK");
			bOK.setPreferredSize(new java.awt.Dimension(88, 25));
			jPanel3.add(bOK);
			bOK.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					bOKActionPerformed(evt);
				}
			});

			bCancel.setText("Cancel");
			bCancel.setPreferredSize(new java.awt.Dimension(96, 25));
			jPanel3.add(bCancel);
			bCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					bCancelActionPerformed(evt);
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * getter for result of the Dialog
	 * 
	 * @return int SaveToDialog.APPROVE or SaveToDialog.CANCEL
	 */
	public int getRetVal() {
		return retVal;
	}


	/**
	 * getter for filename
	 * 
	 * @return String
	 */

	public String getAbsolutePath() {
		return tDirName.getText();
	}

	/**
	 * getter for file type
	 * 
	 * @return int filetype
	 */
	public int getFileType() {
		return fileType;
	}

	protected void bBrowseActionPerformed(ActionEvent evt) {
		File dir = FileImportGUI.getDirFromUser(currentDirectory);
		if (dir != null) {
			this.path = dir.getAbsolutePath();
			tDirName.setText(path);
		}
	}

	protected void bOKActionPerformed(ActionEvent evt) {
		String fileName = tDirName.getText();
		if (fileName.trim().equals("")) {
			new GUIUserInteractor().notify(this, "Error", "Please enter a directory name", UserInteractor.ERROR);
			retVal = -1;
			return;
		}

		File file = new File(tDirName.getText());

		if (file.isDirectory() == false) {
			new GUIUserInteractor().notify(this, "Error", file + " is not a directory", UserInteractor.ERROR);
			retVal = -1;
			return;
		}
		fileType = cbFileTypes.getSelectedIndex();
		retVal = 0;
		this.setVisible(false);
		return;
	}

	protected void bCancelActionPerformed(ActionEvent evt) {
		tDirName.setText("");
		fileType = -1;
		retVal = -1;
		this.setVisible(false);
		return;
	}
}
