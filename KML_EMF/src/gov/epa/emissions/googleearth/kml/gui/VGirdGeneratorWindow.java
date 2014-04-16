package gov.epa.emissions.googleearth.kml.gui;

import gov.epa.emissions.googleearth.kml.ConfigurationManager;
import gov.epa.emissions.googleearth.kml.KMZGeneratorException;
import gov.epa.emissions.googleearth.kml.PropertiesManager;
import gov.epa.emissions.googleearth.kml.PropertiesManager.PropertyKey;
import gov.epa.emissions.googleearth.kml.generator.BinnedPointSourceGenerator;
import gov.epa.emissions.googleearth.kml.gui.PointSourceGeneratorFrame.GenerateAction;
import gov.epa.emissions.googleearth.kml.gui.action.LoadDataFileAction;
import gov.epa.emissions.googleearth.kml.gui.action.LoadFileAction;
import gov.epa.emissions.googleearth.kml.gui.action.LoadGridsInfoFileAction;
import gov.epa.emissions.googleearth.kml.gui.action.LoadPropertiesFileAction;
import gov.epa.emissions.googleearth.kml.gui.action.SavePropertiesFileAction;
import gov.epa.emissions.googleearth.kml.utils.Utils;
import gov.epa.emissions.googleearth.kml.version.Version;
import gov.epa.emissions.googleearth.kml.vgrid.WritePolygons;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

@SuppressWarnings("serial")
public class VGirdGeneratorWindow extends JFrame implements KMLGeneratorView {

	private JTextField dataFileField;
	private JTextField propertiesFileField;
	private JTextField outputFileField;
	private JTextField bins;
	private JTextField minValue;
	private JTextField maxValue;
	private JTextField title;
	private JFormattedTextField gridLineWidth;
	private JFormattedTextField multiple;
	
	private JLabel minUnitLabel;
	private JLabel maxUnitLabel;
	
	private JRadioButton times;
	private JRadioButton divide;

	private JComboBox varNames;
	private JComboBox layers;

	private JCheckBox showGrids;
	private JCheckBox convertUnit;

	private GenerateAction generateAction;
	private OpenAction openAction;
	private LoadFileAction loadDataFileAction;
	private LoadGridsInfoFileAction loadPropertiesFileAction;
	private File outputFile;
	private boolean generated;
	private Integer[] layerValues;
	private List<Integer> rgbs = new ArrayList<Integer>();

	private Dimension buttonDim = new Dimension(90, 24);
	private Dimension textDim = new Dimension(400, 24);
	private Dimension shortTextDim = new Dimension(200, 24);

	private WritePolygons polygonWriter;
	private LoadFileAction browseOutputFileAction;

	public VGirdGeneratorWindow() {
		this((File) null);
	}

	public VGirdGeneratorWindow(String title) {
		this(null, title);
	}

	public VGirdGeneratorWindow(File dataFile) {
		this(dataFile, "KMZ File Generator (Version: "
				+ new Version().getVersion() + ")");
	}

	public VGirdGeneratorWindow(File dataFile, String title) {

		this.setTitle(title);
		// super.setIconImage(Toolkit.getDefaultToolkit().getImage(Object.class.getClass().getResource("/images/unc.JPG")));

		this.setPreferredSize(new Dimension(700, 580));
		this.setResizable(false);

		if (dataFile != null) {
			try {
				this.handleDataFile(dataFile);
			} catch (KMZGeneratorException e) {
				throw new RuntimeException(e);
			}
		}

		this.initFields(dataFile);

		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		panel.add(createHeaderComponent(), BorderLayout.PAGE_START);
		panel.add(createMainComponent(), BorderLayout.CENTER);
		panel.add(createFooterComponent(), BorderLayout.PAGE_END);

		this.getContentPane().add(panel);
	}

	public void setImageColors(List<Integer> rgbs) {
		this.rgbs = rgbs;
	}

	public void centerFrame() {

		Dimension frameSize = this.getSize();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		int xLoc = (screenSize.width - frameSize.width) / 2;
		int yLoc = (screenSize.height - frameSize.height) / 2;

		this.setLocation(xLoc, yLoc);
	}

	protected JComponent createHeaderComponent() {

		JPanel panel = new JPanel(new BorderLayout());
		panel.setPreferredSize(new Dimension(0, 20));
		return panel;
	}

	protected JComponent createFooterComponent() {
		JPanel panel = new JPanel(new BorderLayout(5, 10));
		panel.add(createControlPanel(), BorderLayout.PAGE_END);
		return panel;
	}

	protected JComponent createMainComponent() {
		JPanel panel = new JPanel(new SpringLayout());
		panel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

		JButton dataBrowseButton = new JButton(this.loadDataFileAction);
		dataBrowseButton.setToolTipText(this.loadDataFileAction
				.getToolTipText());
		dataBrowseButton.setPreferredSize(this.buttonDim);
		dataBrowseButton.setMaximumSize(this.buttonDim);
		dataBrowseButton.setMinimumSize(this.buttonDim);

		JPanel dataPanel = new JPanel(new BorderLayout(10, 5));
		dataPanel.add(this.dataFileField, BorderLayout.LINE_START);
		dataPanel.add(dataBrowseButton, BorderLayout.LINE_END);
		layoutGenerator.addLabelWidgetPair("Data File:", dataPanel, panel);

		JButton gridBrowseButton = new JButton(this.loadPropertiesFileAction);
		gridBrowseButton.setToolTipText(this.loadPropertiesFileAction
				.getToolTipText());
		gridBrowseButton.setPreferredSize(this.buttonDim);
		gridBrowseButton.setMaximumSize(this.buttonDim);
		gridBrowseButton.setMinimumSize(this.buttonDim);

		JPanel gridInfoPanel = new JPanel(new BorderLayout(10, 5));
		gridInfoPanel.add(this.propertiesFileField, BorderLayout.LINE_START);
		gridInfoPanel.add(gridBrowseButton, BorderLayout.LINE_END);
		layoutGenerator.addLabelWidgetPair("Grid Info File:", gridInfoPanel,
				panel);

		this.varNames = new JComboBox();
		varNames.setPreferredSize(textDim);
		varNames.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					setVarRelatedInfo();
				} catch (Exception e) {
					showMessageDialog("Variable Error", e.getMessage());
				}
			}
		});

		layoutGenerator.addLabelWidgetPair("Variable Names:", varNames, panel);

		this.layers = new JComboBox();
		layers.setPreferredSize(textDim);
		layers.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					setMinMaxFields();
				} catch (Exception e) {
					showMessageDialog("Variable Layer Error", e.getMessage());
				}
			}
		});
		layoutGenerator.addLabelWidgetPair("Vertical Layers:", layers, panel);

		JPanel minValuePanel = new JPanel();
		minValuePanel.add(minValue);
		minValuePanel.add(minUnitLabel);
		layoutGenerator.addLabelWidgetPair("Minimum Value:", minValuePanel, panel);
		
		JPanel maxValuePanel = new JPanel();
		maxValuePanel.add(maxValue);
		maxValuePanel.add(maxUnitLabel);
		layoutGenerator.addLabelWidgetPair("Maximum Value:", maxValuePanel, panel);

		layoutGenerator.addLabelWidgetPair("Show Grids?", showGrids, panel);
		layoutGenerator.addLabelWidgetPair("Grid Line Width:", gridLineWidth,
				panel);
		layoutGenerator.addLabelWidgetPair("Convert Units?", convertUnit, panel);
		
		
		JPanel buttons = new JPanel();
		buttons.add(times);
		buttons.add(divide);
		
		JPanel convEffPanel = new JPanel(new BorderLayout());
		convEffPanel.add(multiple, BorderLayout.LINE_START);
		convEffPanel.add(buttons);
		convEffPanel.setPreferredSize(new Dimension(400,25));
		layoutGenerator.addLabelWidgetPair("Conv. Efficient:", convEffPanel, panel);
		
		layoutGenerator.addLabelWidgetPair("Bin Values:", bins, panel);
		layoutGenerator.addLabelWidgetPair("Map Title:", title, panel);

		JButton outputFileBrowser = new JButton(this.browseOutputFileAction);
		outputFileBrowser.setToolTipText(this.browseOutputFileAction
				.getToolTipText());
		outputFileBrowser.setPreferredSize(this.buttonDim);
		outputFileBrowser.setMaximumSize(this.buttonDim);
		outputFileBrowser.setMinimumSize(this.buttonDim);

		JPanel outputPanel = new JPanel(new BorderLayout(10, 5));
		outputPanel.add(this.outputFileField, BorderLayout.LINE_START);
		outputPanel.add(outputFileBrowser, BorderLayout.LINE_END);
		layoutGenerator.addLabelWidgetPair("Output File:", outputPanel, panel);

		// Lay out the panel.
		layoutGenerator.makeCompactGrid(panel, 13, 2, // rows, cols
				10, 5, // initialX, initialY
				10, 10);// xPad, yPad

		return panel;
	}

	private void setVarRelatedInfo() throws Exception {
		if (layerValues == null) {
			layerValues = polygonWriter.getLayers();
			layers.setModel(new DefaultComboBoxModel(layerValues));
		}

		try {
			setMinMaxFields();
		} catch (Exception e) {
			layerValues = null;
			layers.setModel(new DefaultComboBoxModel(new Integer[0]));
			this.maxUnitLabel.setText("");
			this.minUnitLabel.setText("");
		}
	}

	protected void setMinMaxFields() throws Exception {
		this.polygonWriter.readMinMaxValues(varNames.getSelectedItem()
				.toString(), ((Integer) layers.getSelectedItem()).intValue());
		this.maxValue.setText(polygonWriter.getMaxValue() + "");
		this.minValue.setText(polygonWriter.getMinValue() + "");
		this.maxUnitLabel.setText(polygonWriter.getVarUnit());
		this.minUnitLabel.setText(polygonWriter.getVarUnit());
	}

	private JPanel createControlPanel() {
		JPanel buttonsPanel = new JPanel();

		JButton generate = new JButton(generateAction);
		generate.setToolTipText(generateAction.getToolTipText());
		buttonsPanel.add(generate);

		JButton viewKMZFileButton = new JButton(this.openAction);
		viewKMZFileButton.setToolTipText(this.openAction.getToolTipText());
		buttonsPanel.add(viewKMZFileButton);

		JButton close = new JButton("Close");
		close.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent event) {
				closeWindow();
			}
		});
		getRootPane().setDefaultButton(close);
		buttonsPanel.add(close);

		return buttonsPanel;
	}

	public void updatePropertiesFields(PropertiesManager propertiesManager) {
		// no-op
	}

	protected void initFields(File dataFile) {

		this.dataFileField = new JTextField();
		this.dataFileField.setToolTipText("Source data file to be processed.");
		this.dataFileField.setPreferredSize(textDim);
		this.dataFileField.addFocusListener(new FocusAdapter() {

			@Override
			public void focusGained(FocusEvent e) {
				((JTextField) e.getSource()).selectAll();
			}
		});

		if (dataFile != null) {
			this.dataFileField.setText(dataFile.getAbsolutePath());
		}

		this.propertiesFileField = new JTextField();
		this.propertiesFileField.setToolTipText("Saved properties file.");
		this.propertiesFileField.setPreferredSize(textDim);
		this.propertiesFileField.addFocusListener(new FocusAdapter() {

			@Override
			public void focusGained(FocusEvent e) {
				((JTextField) e.getSource()).selectAll();
			}
		});

		this.outputFileField = new JTextField();
		this.outputFileField
				.setToolTipText("Output file to be used by Google Earth.");
		this.outputFileField.setPreferredSize(textDim);
		this.outputFileField.addFocusListener(new FocusAdapter() {

			@Override
			public void focusGained(FocusEvent e) {
				openAction.setEnabled(true);
			}
		});

		this.minValue = new JTextField();
		this.minValue
				.setToolTipText("Minimum numerical value for the selected layer.");
		this.minValue.setPreferredSize(shortTextDim);
		this.minValue.setEditable(false);
		this.minValue.setBackground(Color.WHITE);
		this.minUnitLabel = new JLabel("");

		this.maxValue = new JTextField();
		this.maxValue
				.setToolTipText("Maximum numerical value for the selected layer.");
		this.maxValue.setPreferredSize(shortTextDim);
		this.maxUnitLabel = new JLabel("");
		;
		this.maxValue.setEditable(false);
		this.maxValue.setBackground(Color.WHITE);

		this.gridLineWidth = new JFormattedTextField(NumberFormat
				.getIntegerInstance());
		this.gridLineWidth.setValue(new Integer(1));
		this.gridLineWidth.setToolTipText("Line width for the grids to show.");
		this.gridLineWidth.setPreferredSize(shortTextDim);
		this.gridLineWidth.setEditable(false);
		this.gridLineWidth.setBackground(Color.WHITE);
		
		this.multiple = new JFormattedTextField(NumberFormat.getIntegerInstance());
		this.multiple.setToolTipText("Multiple to convert a unit.");
		this.multiple.setPreferredSize(shortTextDim);
		this.multiple.setEditable(false);
		this.multiple.setBackground(Color.WHITE);

		this.bins = new JTextField();
		this.bins
				.setToolTipText("Data bin values. For example: 0,100,1000,10000,100000,1000000,10000000.");
		this.bins.setPreferredSize(textDim);
		this.bins.setBackground(Color.WHITE);

		this.title = new JTextField();
		this.title.setToolTipText("Title for the google earth map.");
		this.title.setPreferredSize(textDim);

		this.showGrids = new JCheckBox();
		this.showGrids.setSelected(false);
		this.showGrids.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gridLineWidth.setEditable(showGrids.isSelected());
			}
		});
		
		this.convertUnit = new JCheckBox();
		this.convertUnit.setSelected(false);
		this.convertUnit.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				multiple.setEnabled(convertUnit.isSelected());
				multiple.setEditable(convertUnit.isSelected());
				times.setEnabled(convertUnit.isSelected());
				divide.setEnabled(convertUnit.isSelected());
				try {
					setMinMaxFields();
				} catch (Exception e) {
					//e.printStackTrace();
				}
			}
		});
		
		this.times = new JRadioButton("Multiply by");
		this.times.setSelected(false);
		this.times.setEnabled(false);
		this.divide = new JRadioButton("Divide by");
		this.divide.setSelected(true);
		this.divide.setEnabled(false);
		
		ButtonGroup group = new ButtonGroup();
	    group.add(times);
	    group.add(divide);
		
		this.loadDataFileAction = new LoadDataFileAction(this.dataFileField,
				this);
		this.loadDataFileAction
				.setToolTipText("Load data file to be processed.");
		this.loadPropertiesFileAction = new LoadGridsInfoFileAction(
				this.propertiesFileField, this);

		this.browseOutputFileAction = new LoadFileAction("Browse...",
				this.outputFileField, new FileNameExtensionFilter(
						"Output Files", "kmz"));
		this.browseOutputFileAction
				.setToolTipText("Select an output file to write kml data.");

		this.generateAction = new GenerateAction();
		this.openAction = new OpenAction();
		this.openAction.setEnabled(false);
	}

	public void handleDataFile(File dataFile) throws KMZGeneratorException {

		try {
			this.polygonWriter = new WritePolygons(dataFile.getCanonicalPath());
			String[] vars = this.polygonWriter.getVarNames().toArray(
					new String[0]);
			varNames.setModel(new DefaultComboBoxModel(vars));
		} catch (FileNotFoundException e) {
			throw new KMZGeneratorException(
					KMZGeneratorException.ERROR_CODE_DATA_FILE_DOESNT_EXIST,
					"Data file " + dataFile.getAbsolutePath());
		} catch (Exception e) {
			throw new KMZGeneratorException(
					KMZGeneratorException.ERROR_CODE_READING_DATA_FILE,
					"Data file " + dataFile.getAbsolutePath());
		}
	}

	protected void setEnabledAll(boolean enabled) {

		this.dataFileField.setEnabled(enabled);
		this.propertiesFileField.setEnabled(enabled);
		this.outputFileField.setEnabled(enabled);
		this.title.setEnabled(enabled);
		this.bins.setEnabled(enabled);
		this.showGrids.setEnabled(enabled);
		this.gridLineWidth.setEnabled(enabled);
		
		this.convertUnit.setEnabled(enabled);
		this.multiple.setEnabled(convertUnit.isSelected() && enabled);
		this.divide.setEnabled(convertUnit.isSelected() && enabled);
		this.times.setEnabled(convertUnit.isSelected() && enabled);

		this.varNames.setEnabled(enabled);
		this.layers.setEnabled(enabled);

		this.loadDataFileAction.setEnabled(enabled);
		this.loadPropertiesFileAction.setEnabled(enabled);
		this.browseOutputFileAction.setEnabled(enabled);
		this.generateAction.setEnabled(enabled);

		this.openAction.setEnabled(enabled && this.generated);
	}

	class GenerateAction extends AbstractAction {

		public GenerateAction() {
			super("Generate");
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			Thread generationThread = new Thread(new Runnable() {

				@Override
				public void run() {

					setEnabledAll(false);
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

					try {
						String dataFileName = dataFileField.getText().trim();

						if (dataFileName == null || dataFileName.length() == 0) {
							throw new KMZGeneratorException(
									KMZGeneratorException.ERROR_CODE_DATA_FILE_DOESNT_EXIST,
									"Data file field cannot be empty.");
						}

						File dataFile = new File(dataFileName);

						if (!dataFile.exists()) {
							throw new KMZGeneratorException(
									KMZGeneratorException.ERROR_CODE_DATA_FILE_DOESNT_EXIST,
									"Data file '" + dataFileName
											+ "' does not exist.");
						}

						String gridsInfoFileName = propertiesFileField
								.getText().trim();

						if (gridsInfoFileName == null
								|| gridsInfoFileName.length() == 0) {
							throw new KMZGeneratorException(
									KMZGeneratorException.ERROR_CODE_DATA_FILE_DOESNT_EXIST,
									"Grids info file field cannot be empty.");
						}

						File gridsInfoFile = new File(gridsInfoFileName);

						if (!gridsInfoFile.exists()) {
							throw new KMZGeneratorException(
									KMZGeneratorException.ERROR_CODE_DATA_FILE_DOESNT_EXIST,
									"Grids info file '" + gridsInfoFileName
											+ "' does not exist.");
						}

						if (layers.getSelectedItem() == null)
							throw new KMZGeneratorException(
									KMZGeneratorException.ERROR_CODE_NULL_INPUT,
									"Vertical layer is not selected.");

						String outputFileName = outputFileField.getText()
								.trim();

						if (outputFileName == null
								|| outputFileName.length() == 0) {
							outputFileName = dataFile.getCanonicalPath()
									+ ".kmz";
							int option = showConfirmDialog("Output Files",
									"Output file is not specified. Would you like to use the default output file: "
											+ outputFileName + "?");

							if (option == JOptionPane.YES_OPTION
									|| option == JOptionPane.OK_OPTION)
								outputFileField.setText(outputFileName);

							return;
						}

						if (outputFileName.lastIndexOf(".kmz") < 0)
							outputFileName += ".kmz";

						outputFile = new File(outputFileName);

						if (outputFile.exists()) {
							int result = showConfirmDialog("File Exists",
									"Would you like to overwrite the selected output file?");
							if (result != JOptionPane.YES_OPTION
									&& result != JOptionPane.OK_OPTION)
								return;
						}

						boolean showgrids = showGrids.isSelected();

						if (showgrids && gridLineWidth.getValue() == null) {
							showMessageDialog("Grids Line Width",
									"Please specify an integer value (> 0, ex. 1) for grid line width.");
							return;
						}

						float[] binValues = polygonWriter
								.parseUserSpecifiedBins(bins.getText());
						int lineWidth = showgrids ? ((Number) gridLineWidth
								.getValue()).intValue() : 0;
						String selectedVar = varNames.getSelectedItem()
								.toString();
						int layer = Integer.parseInt(layers.getSelectedItem()
								.toString()) - 1;
						
						String titleString = title.getText();
						float convEff = (multiple.getValue() != null) ? ((Number) multiple.getValue()).floatValue() : 0;
						boolean multiply = times.isSelected();
						boolean division = divide.isSelected();
						
						if (!convertUnit.isSelected()) {
							convEff = 0;
							multiply = false;
							division = false;
						}
						
						polygonWriter.writePolygons(gridsInfoFile, outputFile,
								selectedVar, layer, binValues, lineWidth,
								showgrids, titleString, convEff, multiply, division);
						generated = true;
						openAction.setEnabled(true);
					} catch (IOException e) {
						String message = "Error while generating kmz file: "
								+ e.getLocalizedMessage();

						showMessageDialog("KMZ File Generation Error", message);
					} catch (KMZGeneratorException e) {
						showMessageDialog("Input Error", "Illegal input: "
								+ e.getLocalizedMessage());
					} catch (Exception e) {
						e.printStackTrace();
						showMessageDialog("Unknown Error", Utils
								.generateStackTrace(e));
					} finally {
						setCursor(Cursor
								.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						setEnabledAll(true);
					}
				}
			});

			generationThread.setName("Generation Thread");
			generationThread.start();
		}

		public String getToolTipText() {
			return "Generate .kmz file for use in Google Earth.";
		}
	}

	public int showConfirmDialog(String type, String message) {
		return JOptionPane.showConfirmDialog(this, Utils.wrapLine(message, 60),
				type, JOptionPane.WARNING_MESSAGE);
	}

	public void showMessageDialog(String type, String message) {
		JOptionPane.showMessageDialog(this, Utils.wrapLine(message, 60), type,
				JOptionPane.ERROR_MESSAGE);
	}

	private void closeWindow() {
		super.dispose();
		System.exit(0);
	}

	class OpenAction extends AbstractAction {

		public OpenAction() {
			super("View KMZ");
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {

			Thread openThread = new Thread(new Runnable() {

				@Override
				public void run() {
					String outputFileName = outputFileField.getText();

					if (outputFileName == null || outputFileName.isEmpty())
						return;

					outputFile = new File(outputFileName.trim());

					if (!outputFile.exists()) {
						showMessageDialog("File Does Not Exist", "File "
								+ outputFileName + " cannot be found.");
						return;
					}

					String outputFilePath = outputFile.getAbsolutePath();
					String[] commands = { "cmd", "/c", "start",
							"\"DummyTitle\"", "/WAIT", outputFilePath };
					Process p;
					try {
						p = Runtime.getRuntime().exec(commands);
						p.waitFor();
					} catch (Exception e) {

						String message = "Error while opening file "
								+ outputFilePath + "\n\n"
								+ e.getLocalizedMessage();
						if (ConfigurationManager
								.getInstance()
								.getValueAsBoolean(
										ConfigurationManager.PropertyKey.SHOW_OUTPUT
												.getKey())) {
							System.err.println(message);
						}

						JOptionPane.showMessageDialog(
								VGirdGeneratorWindow.this, Utils.wrapLine(
										message, 80), "Open Error",
								JOptionPane.ERROR_MESSAGE);

					}
				}
			});

			openThread.setName("Open Thread");
			openThread.start();
		}

		public String getToolTipText() {
			return "Open file with default application (i.e., Google Earth).";
		}
	}

	public static void main(String[] args) {

		try {
			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {

					try {

						ConfigurationManager configManager = ConfigurationManager
								.getInstance();
						configManager.setValue(
								ConfigurationManager.PropertyKey.SHOW_OUTPUT
										.getKey(), Boolean.FALSE.toString());

						VGirdGeneratorWindow frame = new VGirdGeneratorWindow();
						frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
						frame.pack();
						frame.centerFrame();
						frame.setVisible(true);
					} catch (Exception e) {

						String message = e.getLocalizedMessage();
						if (ConfigurationManager
								.getInstance()
								.getValueAsBoolean(
										ConfigurationManager.PropertyKey.SHOW_OUTPUT
												.getKey())) {
							System.err.println(message);
						}

						JOptionPane.showMessageDialog(null, Utils.wrapLine(
								message, 80), "Application Launch Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			});
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.out.println("Error launching application:\n"
					+ Utils.generateStackTrace(e));
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			System.out.println("Error launching application:\n"
					+ Utils.generateStackTrace(e));
		}
	}
}
