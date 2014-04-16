package gov.epa.emissions.googleearth.kml.gui;

import gov.epa.emissions.googleearth.CSVRecordReader;
import gov.epa.emissions.googleearth.kml.ConfigurationManager;
import gov.epa.emissions.googleearth.kml.KMZGeneratorException;
import gov.epa.emissions.googleearth.kml.PropertiesManager;
import gov.epa.emissions.googleearth.kml.PropertiesManager.PropertyKey;
import gov.epa.emissions.googleearth.kml.generator.BinnedPointSourceGenerator;
import gov.epa.emissions.googleearth.kml.generator.BinningAlgorithmType;
import gov.epa.emissions.googleearth.kml.generator.OverlayPosition;
import gov.epa.emissions.googleearth.kml.generator.color.ColorPalette;
import gov.epa.emissions.googleearth.kml.generator.color.ColorPaletteGenerator;
import gov.epa.emissions.googleearth.kml.generator.color.ColorPaletteGeneratorImpl;
import gov.epa.emissions.googleearth.kml.generator.image.ImageGeneratorImpl;
import gov.epa.emissions.googleearth.kml.generator.preprocessor.PreProcessorImpl;
import gov.epa.emissions.googleearth.kml.generator.writer.DocumentWriterImpl;
import gov.epa.emissions.googleearth.kml.gui.action.LoadDataFileAction;
import gov.epa.emissions.googleearth.kml.gui.action.LoadFileAction;
import gov.epa.emissions.googleearth.kml.gui.action.LoadPropertiesFileAction;
import gov.epa.emissions.googleearth.kml.gui.action.SavePropertiesFileAction;
import gov.epa.emissions.googleearth.kml.utils.Utils;
import gov.epa.emissions.googleearth.kml.version.Version;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

@SuppressWarnings("serial")
public class PointSourceGeneratorFrame extends JFrame implements
		KMLGeneratorView {

	private JTextField dataFileField;
	private JTextField propertiesFileField;
	private JTextField outputFileField;
	private GenerateAction generateAction;
	private OpenAction openAction;
	private SavePropertiesFileAction savePropertiesFileAction;
	private LoadFileAction loadDataFileAction;
	private LoadPropertiesFileAction loadPropertiesFileAction;
	private File outputFile;
	private boolean generated;
	private List<Integer> rgbs = new ArrayList<Integer>();

	private Dimension buttonDim = new Dimension(90, 24);

	private HashMap<PropertiesManager.PropertyKey, PropertyField> keyToFieldMap;
	private CSVRecordReader recordReader;

	private ColorPaletteGenerator colorPaletteGenerator;

	public PointSourceGeneratorFrame() {
		this((File) null);
	}

	public PointSourceGeneratorFrame(String title) {
		this(null, title);
	}

	public PointSourceGeneratorFrame(File dataFile) {
		this(dataFile, "KMZ File Generator (Version: "
				+ new Version().getVersion() + ")");
	}

	public PointSourceGeneratorFrame(File dataFile, String title) {

		this.setTitle(title);
		this.setLayout(new BorderLayout());
		// super.setIconImage(Toolkit.getDefaultToolkit().getImage(Object.class.getClass().getResource("/images/logo.JPG")));

		this.keyToFieldMap = new HashMap<PropertyKey, PropertyField>();

		this.setPreferredSize(new Dimension(700, 730));

		this.colorPaletteGenerator = new ColorPaletteGeneratorImpl();

		if (dataFile != null) {
			try {
				this.handleDataFile(dataFile);
			} catch (KMZGeneratorException e) {
				throw new RuntimeException(e);
			}
		}

		this.initFields(dataFile);

		this.add(this.createHeaderComponent(), BorderLayout.NORTH);
		this.add(this.createMainComponent(), BorderLayout.CENTER);
		this.add(this.createFooterComponent(), BorderLayout.SOUTH);
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

		JPanel panel = new JPanel(new BorderLayout());
		panel.setPreferredSize(new Dimension(0, 0));
		return panel;
	}

	protected JComponent createMainComponent() {

		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		/*
		 * labels
		 */
		GridBagConstraints labelConstraints = new GridBagConstraints();
		labelConstraints.anchor = GridBagConstraints.NORTHEAST;
		labelConstraints.gridy = 0;
		labelConstraints.insets = new Insets(5, 5, 5, 5);

		JLabel dataFileLabel = new JLabel("Data File");
		panel.add(dataFileLabel, labelConstraints);

		/*
		 * input fields
		 */
		GridBagConstraints inputConstraints = new GridBagConstraints();
		inputConstraints.anchor = GridBagConstraints.NORTHWEST;
		inputConstraints.weightx = 1;
		inputConstraints.gridx = 1;
		inputConstraints.gridy = 0;
		inputConstraints.fill = GridBagConstraints.HORIZONTAL;
		inputConstraints.insets = new Insets(5, 5, 5, 5);

		panel.add(this.dataFileField, inputConstraints);

		GridBagConstraints buttonConstraints = new GridBagConstraints();
		buttonConstraints.anchor = GridBagConstraints.NORTHEAST;
		buttonConstraints.gridx = 2;
		buttonConstraints.gridy = 0;
		buttonConstraints.insets = new Insets(2, 5, 5, 15);

		JButton browseButton = new JButton(this.loadDataFileAction);
		browseButton.setToolTipText(this.loadDataFileAction.getToolTipText());
		browseButton.setPreferredSize(this.buttonDim);
		browseButton.setMaximumSize(this.buttonDim);
		browseButton.setMinimumSize(this.buttonDim);

		panel.add(browseButton, buttonConstraints);

		labelConstraints.gridy = 1;

		JLabel inputFileLabel = new JLabel("Properties File");
		panel.add(inputFileLabel, labelConstraints);

		inputConstraints.gridy = 1;

		panel.add(this.propertiesFileField, inputConstraints);

		buttonConstraints.gridy = 1;

		browseButton = new JButton(this.loadPropertiesFileAction);
		browseButton.setToolTipText(this.loadPropertiesFileAction
				.getToolTipText());
		browseButton.setPreferredSize(this.buttonDim);
		browseButton.setMaximumSize(this.buttonDim);
		browseButton.setMinimumSize(this.buttonDim);
		panel.add(browseButton, buttonConstraints);

		GridBagConstraints tableConstraints = new GridBagConstraints();
		tableConstraints.anchor = GridBagConstraints.NORTHWEST;
		tableConstraints.weightx = 1;
		tableConstraints.weighty = 1;
		tableConstraints.gridx = 0;
		tableConstraints.gridy = 3;
		tableConstraints.gridwidth = 3;
		tableConstraints.fill = GridBagConstraints.BOTH;
		tableConstraints.insets = new Insets(5, 5, 5, 5);

		panel.add(this.createPropertiesComponent(), tableConstraints);

		buttonConstraints.gridy = 4;
		JButton generateButton = new JButton(this.generateAction);
		generateButton.setToolTipText(this.generateAction.getToolTipText());
		generateButton.setPreferredSize(this.buttonDim);
		generateButton.setMaximumSize(this.buttonDim);
		generateButton.setMinimumSize(this.buttonDim);
		panel.add(generateButton, buttonConstraints);

		generateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				PointSourceGeneratorFrame.this.outputFileField.setText("");
				PointSourceGeneratorFrame.this.openAction.setEnabled(false);
				generated = false;
			}
		});

		labelConstraints.gridy = 5;

		JLabel outputFileLabel = new JLabel("Output File");
		panel.add(outputFileLabel, labelConstraints);

		inputConstraints.gridy = 5;

		panel.add(this.outputFileField, inputConstraints);

		buttonConstraints.gridy = 5;

		JButton openButton = new JButton(this.openAction);
		openButton.setToolTipText(this.openAction.getToolTipText());
		openButton.setPreferredSize(this.buttonDim);
		openButton.setMaximumSize(this.buttonDim);
		openButton.setMinimumSize(this.buttonDim);
		panel.add(openButton, buttonConstraints);

		return panel;
	}

	protected JComponent createPropertiesComponent() {

		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Properties"));

		GridBagConstraints labelConstraints = new GridBagConstraints();
		labelConstraints.anchor = GridBagConstraints.NORTHEAST;
		labelConstraints.gridy = 0;
		labelConstraints.insets = new Insets(3, 5, 3, 5);

		int cells = 14;

		GridBagConstraints inputConstraints = new GridBagConstraints();
		inputConstraints.anchor = GridBagConstraints.NORTHWEST;
		inputConstraints.weightx = 1;
		inputConstraints.gridx = 1;
		inputConstraints.gridwidth = cells - 1;
		inputConstraints.gridy = 0;
		inputConstraints.fill = GridBagConstraints.HORIZONTAL;
		inputConstraints.insets = new Insets(3, 5, 3, 5);

		GridBagConstraints spinnerConstraints = new GridBagConstraints();
		spinnerConstraints.anchor = GridBagConstraints.NORTHWEST;
		spinnerConstraints.weightx = .1;
		spinnerConstraints.gridx = 1;
		spinnerConstraints.gridwidth = 1;
		spinnerConstraints.gridy = 0;
		spinnerConstraints.fill = GridBagConstraints.HORIZONTAL;
		spinnerConstraints.insets = new Insets(3, 5, 3, 5);

		GridBagConstraints spinnerEmptyConstraints = new GridBagConstraints();
		spinnerEmptyConstraints.anchor = GridBagConstraints.NORTHWEST;
		spinnerEmptyConstraints.weightx = 1;
		spinnerEmptyConstraints.gridx = 2;
		spinnerEmptyConstraints.gridwidth = cells - 2;
		spinnerEmptyConstraints.gridy = 0;
		spinnerEmptyConstraints.fill = GridBagConstraints.HORIZONTAL;
		spinnerEmptyConstraints.insets = new Insets(3, 5, 3, 5);

		String[] columnHeader = new String[0];
		if (this.recordReader != null) {
			columnHeader = this.recordReader.getColumnHeader();
		}

		PropertyCheckBox diffCheckBox = null;
		PropertyComboBox paletteComboBox = null;
		PropertySpinner binCountSpinner = null;
		for (PropertyKey propertyKey : PropertyKey.values()) {

			if (!propertyKey.isHidden()) {

				JLabel label = new JLabel(propertyKey.getDisplayName());
				panel.add(label, labelConstraints);

				PropertiesManager propertiesManager = PropertiesManager
						.getInstance();
				if (propertyKey.isColumn()) {

					PropertyComboBox field = new PropertyComboBox(propertyKey,
							propertiesManager);
					field.setModel(new DefaultComboBoxModel(columnHeader));

					if (propertyKey.isFilterColumn()) {

						for (String string : columnHeader) {

							if (string.toUpperCase().startsWith("POLL")) {

								field.setSelectedItem(string);
								break;
							}
						}
					}

					panel.add(field, inputConstraints);
					Object selectedItem = field.getSelectedItem();

					if (selectedItem == null) {
						selectedItem = "";
					}

					propertiesManager.setValue(propertyKey.getKey(),
							selectedItem.toString());
					this.keyToFieldMap.put(propertyKey, field);
				} else if (PropertyKey.LEGEND_POSITION.equals(propertyKey)
						|| PropertyKey.TITLE_POSITION.equals(propertyKey)) {

					PropertyComboBox field = new PropertyComboBox(propertyKey,
							propertiesManager);
					OverlayPosition[] values = OverlayPosition.values();
					field.setModel(new DefaultComboBoxModel(values));
					String value = propertiesManager.getValue(propertyKey);
					OverlayPosition overlayPosition = OverlayPosition
							.getByDisplayName(value);
					field.setSelectedItem(overlayPosition);

					panel.add(field, inputConstraints);
					Object selectedItem = field.getSelectedItem();

					if (selectedItem == null) {
						selectedItem = values[0];
					}

					propertiesManager.setValue(propertyKey.getKey(),
							selectedItem.toString());
					this.keyToFieldMap.put(propertyKey, field);
				} else if (PropertyKey.BINNING_ALGORITHM.equals(propertyKey)) {

					PropertyComboBox field = new PropertyComboBox(propertyKey,
							propertiesManager);
					BinningAlgorithmType[] values = BinningAlgorithmType
							.values();
					field.setModel(new DefaultComboBoxModel(values));
					String value = propertiesManager.getValue(propertyKey);
					BinningAlgorithmType binningAlgorithmType = BinningAlgorithmType
							.getByDisplayName(value);
					field.setSelectedItem(binningAlgorithmType);

					panel.add(field, inputConstraints);
					Object selectedItem = field.getSelectedItem();

					if (selectedItem == null) {
						selectedItem = values[0];
					}

					propertiesManager.setValue(propertyKey.getKey(),
							selectedItem.toString());
					this.keyToFieldMap.put(propertyKey, field);
				} else if (PropertyKey.COLOR_PALETTE.equals(propertyKey)) {

					PropertyComboBox field = new PropertyComboBox(propertyKey,
							propertiesManager);
					paletteComboBox = field;

					panel.add(field, inputConstraints);

					this.keyToFieldMap.put(propertyKey, field);
				} else if (PropertyKey.BIN_COUNT.equals(propertyKey)) {

					PropertySpinner field = new PropertySpinner(propertyKey,
							propertiesManager, 3);
					binCountSpinner = field;

					panel.add(field, spinnerConstraints);

					JLabel emptyLabel = new JLabel();
					emptyLabel.setOpaque(false);
					panel.add(emptyLabel, spinnerEmptyConstraints);

					this.keyToFieldMap.put(propertyKey, field);
				} else if (PropertyKey.PLOT_DIFF.equals(propertyKey)) {

					diffCheckBox = new PropertyCheckBox(propertyKey,
							propertiesManager);
					panel.add(diffCheckBox, inputConstraints);

					this.keyToFieldMap.put(propertyKey, diffCheckBox);
				} else if (PropertyKey.DATA_DISCARD_BLANK.equals(propertyKey)) {

					PropertyCheckBox blankPolicyCheckBox = new PropertyCheckBox(propertyKey,
							propertiesManager);
					panel.add(blankPolicyCheckBox, inputConstraints);

					this.keyToFieldMap.put(propertyKey, blankPolicyCheckBox);
				} else {

					PropertyTextField field = new PropertyTextField(
							propertyKey, propertiesManager);
					panel.add(field, inputConstraints);
					this.keyToFieldMap.put(propertyKey, field);
				}

				labelConstraints.gridy += 1;
				inputConstraints.gridy += 1;
				spinnerConstraints.gridy += 1;
				spinnerEmptyConstraints.gridy += 1;
			}
		}

		GridBagConstraints buttonConstraints = new GridBagConstraints();
		buttonConstraints.anchor = GridBagConstraints.NORTHEAST;
		buttonConstraints.gridx = cells;
		buttonConstraints.gridy = 0;
		buttonConstraints.gridheight = 2;
		buttonConstraints.insets = new Insets(2, 5, 5, 5);

		JButton saveButton = new JButton(this.savePropertiesFileAction);
		saveButton.setToolTipText(this.savePropertiesFileAction
				.getToolTipText());
		saveButton.setPreferredSize(this.buttonDim);
		saveButton.setMaximumSize(this.buttonDim);
		saveButton.setMinimumSize(this.buttonDim);
		panel.add(saveButton, buttonConstraints);

		final PropertyCheckBox finalDiffCheckBox = diffCheckBox;
		final PropertyComboBox finalPaletteComboBox = paletteComboBox;
		final PropertySpinner finalBinCountSpinner = binCountSpinner;
		diffCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				initColorPalette(finalDiffCheckBox, finalPaletteComboBox,
						finalBinCountSpinner);
			}
		});

		binCountSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				initColorPalette(finalDiffCheckBox, finalPaletteComboBox,
						finalBinCountSpinner);
			}
		});

		this.initColorPalette(finalDiffCheckBox, finalPaletteComboBox,
				finalBinCountSpinner);

		return panel;
	}

	private void initColorPalette(PropertyCheckBox diffCheckBox,
			PropertyComboBox paletteComboBox, PropertySpinner binCountSpinner) {

		PropertiesManager propertiesManager = PropertiesManager.getInstance();

		PropertyKey palettePropertyKey = paletteComboBox.getPropertyKey();
		PropertyKey binCountPropertyKey = binCountSpinner.getPropertyKey();

		int oldBinCount = propertiesManager.getValueAsInt(binCountPropertyKey);
		int binCount = (Integer) binCountSpinner.getValue();

		try {
			this.colorPaletteGenerator.createColorPalettes(binCount);
		} catch (KMZGeneratorException e) {
			binCountSpinner.setValue(oldBinCount);
		}

		List<ColorPalette> palettes = null;
		if (diffCheckBox.isSelected()) {
			palettes = this.colorPaletteGenerator.getDiffColorPalettes();
		} else {
			palettes = this.colorPaletteGenerator.getRegularColorPalettes();
		}

		List<String> paletteStrings = new ArrayList<String>();
		for (ColorPalette brewerPalette : palettes) {
			paletteStrings.add(brewerPalette.getName());
		}

		paletteComboBox.setModel(new DefaultComboBoxModel(paletteStrings
				.toArray(new String[0])));

		String value = propertiesManager.getValue(palettePropertyKey);
		paletteComboBox.setSelectedItem(value);

		Object selectedItem = paletteComboBox.getSelectedItem();

		if (selectedItem == null) {
			selectedItem = paletteStrings.get(0);
		}

		propertiesManager.setValue(palettePropertyKey.getKey(), selectedItem
				.toString());
	}

	public void updatePropertiesFields(PropertiesManager propertiesManager) {

		for (PropertyKey propertyKey : PropertyKey.values()) {
			if (!propertyKey.isHidden()) {
				PropertyField propertyField = this.keyToFieldMap
						.get(propertyKey);
				propertyField.updateProperty(propertiesManager);
			}
		}
	}

	protected void initFields(File dataFile) {

		this.dataFileField = new JTextField();
		this.dataFileField.setToolTipText("Source data file to be processed.");
		this.dataFileField.setEditable(false);
		this.dataFileField.setBackground(Color.WHITE);
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
		this.propertiesFileField.addFocusListener(new FocusAdapter() {

			@Override
			public void focusGained(FocusEvent e) {
				((JTextField) e.getSource()).selectAll();
			}
		});

		this.outputFileField = new JTextField();
		this.outputFileField
				.setToolTipText("Output file to be used by Google Earth.");
		this.outputFileField.setEditable(false);
		this.outputFileField.setBackground(Color.WHITE);

		// PropertiesManager propertiesManager =
		// PropertiesManager.getInstance();
		// if (dataFile != null) {
		// propertiesManager.initProperties(dataFile);
		// }

		this.loadDataFileAction = new LoadDataFileAction(this.dataFileField,
				this);
		this.loadDataFileAction
				.setToolTipText("Load data file to be processed.");
		this.loadPropertiesFileAction = new LoadPropertiesFileAction(
				this.propertiesFileField, this);
		this.generateAction = new GenerateAction();

		this.openAction = new OpenAction();
		this.openAction.setEnabled(false);

		this.savePropertiesFileAction = new SavePropertiesFileAction(this,
				PropertiesManager.getInstance());

		this.initForDebug();
	}

	protected void initForDebug() {
	}

	public void handleDataFile(File dataFile) throws KMZGeneratorException {

		try {
			this.recordReader = new CSVRecordReader(dataFile);
			String[] columnHeader = this.recordReader.getColumnHeader();

			for (PropertyKey propertyKey : this.keyToFieldMap.keySet()) {

				PropertyField propertyField = this.keyToFieldMap
						.get(propertyKey);

				if (PropertyKey.LEGEND_POSITION.equals(propertyKey)
						|| PropertyKey.TITLE_POSITION.equals(propertyKey)
						|| PropertyKey.BINNING_ALGORITHM.equals(propertyKey)
						|| PropertyKey.COLOR_PALETTE.equals(propertyKey)) {
					/*
					 * no-op
					 */
				} else if (propertyField instanceof PropertyComboBox) {

					PropertyComboBox propertyComboBox = (PropertyComboBox) propertyField;
					propertyComboBox.setModel(new DefaultComboBoxModel(
							this.recordReader.getColumnHeader()));
					PropertiesManager.getInstance().setValue(
							propertyKey.getKey(),
							propertyComboBox.getSelectedItem().toString());

					if (propertyKey.isFilterColumn()) {

						for (String string : columnHeader) {

							if (string.toUpperCase().startsWith("POLL")) {

								propertyComboBox.setSelectedItem(string);
								break;
							}
						}
					}

				}
			}

		} catch (FileNotFoundException e) {

			throw new KMZGeneratorException(
					KMZGeneratorException.ERROR_CODE_DATA_FILE_DOESNT_EXIST,
					"Data file " + dataFile.getAbsolutePath());
		}
	}

	protected void setEnabledAll(boolean enabled) {

		this.dataFileField.setEnabled(enabled);
		this.propertiesFileField.setEnabled(enabled);
		this.outputFileField.setEnabled(enabled);

		this.loadDataFileAction.setEnabled(enabled);
		this.loadPropertiesFileAction.setEnabled(enabled);
		this.generateAction.setEnabled(enabled);
		this.savePropertiesFileAction.setEnabled(enabled);

		this.openAction.setEnabled(enabled && this.generated);

		for (PropertyKey propertyKey : PropertyKey.values()) {

			if (!propertyKey.isHidden()) {
				this.keyToFieldMap.get(propertyKey).setEnabled(enabled);
			}
		}
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
					PointSourceGeneratorFrame.this.setCursor(Cursor
							.getPredefinedCursor(Cursor.WAIT_CURSOR));

					try {
						// FIXME This is doing no validation right now
						BinnedPointSourceGenerator
								.validateGUIInput(new String[] {});

						File tempFile = File.createTempFile("kml", ".kml");

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

						PropertiesManager propertiesManager = PropertiesManager
								.getInstance();

						GenerateAction.this
								.validateProperties(propertiesManager);

						PointSourceGeneratorFrame.this.recordReader
								.initReader();
						BinnedPointSourceGenerator generator = new BinnedPointSourceGenerator(
								PointSourceGeneratorFrame.this.recordReader,
								dataFile, tempFile, propertiesManager,
								new PreProcessorImpl(),
								new ImageGeneratorImpl(),
								new DocumentWriterImpl(), colorPaletteGenerator);

						if (rgbs != null && !rgbs.isEmpty()) {
							generator.setImageColors(rgbs);
						}

						generator.generate();

						String outputFileName = BinnedPointSourceGenerator
								.createDefaultFileName(propertiesManager,
										dataFile.getName());

						outputFile = new File(dataFile.getParentFile()
								.getAbsolutePath()
								+ "/" + outputFileName);

						boolean isSafeOutputLocation = !outputFile.exists();
						while (!isSafeOutputLocation) {

							/*
							 * prompt user if its okay to overwrite
							 */
							int result = JOptionPane
									.showConfirmDialog(
											PointSourceGeneratorFrame.this,
											"Output file "
													+ outputFile
															.getAbsolutePath()
													+ "\nalready exists, do you wish to overwrite it?",
											"File Exists",
											JOptionPane.YES_NO_OPTION);
							if (result == JOptionPane.YES_OPTION) {
								isSafeOutputLocation = true;
							} else {

								JFileChooser fileChooser = new JFileChooser(
										outputFile.getParent());
								fileChooser
										.setFileFilter(new FileNameExtensionFilter(
												"KMZ File", "kmz"));

								int returnVal = fileChooser
										.showSaveDialog(PointSourceGeneratorFrame.this);
								if (returnVal == JFileChooser.APPROVE_OPTION) {

									outputFile = fileChooser.getSelectedFile();
									isSafeOutputLocation = !outputFile.exists();
								}
							}

						}

						generator.zipResults(tempFile, outputFile, generator
								.getImages(), generator.getLegend(), generator
								.getTitleLegend(), generator.getStatsLegend());

						PointSourceGeneratorFrame.this.outputFileField
								.setText(outputFile.getAbsolutePath());
						PointSourceGeneratorFrame.this.generated = true;
						PointSourceGeneratorFrame.this.openAction
								.setEnabled(true);
					} catch (IOException e) {

						String message = "Error while generating kmz file: "
								+ e.getLocalizedMessage();
						if (ConfigurationManager
								.getInstance()
								.getValueAsBoolean(
										ConfigurationManager.PropertyKey.SHOW_OUTPUT
												.getKey())) {
							System.err.println(message);
						}

						JOptionPane.showMessageDialog(
								PointSourceGeneratorFrame.this, Utils.wrapLine(
										message, 80),
								"KMZ File Generation Error",
								JOptionPane.ERROR_MESSAGE);

					} catch (KMZGeneratorException e) {

						String message = "Illegal input: "
								+ e.getLocalizedMessage();
						if (ConfigurationManager
								.getInstance()
								.getValueAsBoolean(
										ConfigurationManager.PropertyKey.SHOW_OUTPUT
												.getKey())) {
							System.err.println(message);
						}

						JOptionPane.showMessageDialog(
								PointSourceGeneratorFrame.this, Utils.wrapLine(
										message, 80), "Input Error",
								JOptionPane.ERROR_MESSAGE);

					} catch (Exception e) {

						e.printStackTrace();
						String message = Utils.generateStackTrace(e);

						if (ConfigurationManager
								.getInstance()
								.getValueAsBoolean(
										ConfigurationManager.PropertyKey.SHOW_OUTPUT
												.getKey())) {
							System.err.println(message);
						}

						JOptionPane.showMessageDialog(
								PointSourceGeneratorFrame.this, message,
								"Unknown Error", JOptionPane.ERROR_MESSAGE);

					} finally {
						PointSourceGeneratorFrame.this.setCursor(Cursor
								.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						setEnabledAll(true);
					}
				}
			});

			generationThread.setName("Generation Thread");
			generationThread.start();
		}

		private void validateProperties(PropertiesManager propertiesManager)
				throws KMZGeneratorException {
			String filterColName = propertiesManager
					.getValue(PropertyKey.FILTER_COLUMNNAME);
			String dataColName = propertiesManager
					.getValue(PropertyKey.DATA_COLUMNNAME);

			if (filterColName.equals(dataColName))
				throw new KMZGeneratorException(
						KMZGeneratorException.ERROR_CODE_INCORRECT_VALUE_TYPE,
						"Filter column and data column cannot be the same.");
		}

		public String getToolTipText() {
			return "Generate file for use in Google Earth.";
		}
	}

	class OpenAction extends AbstractAction {

		public OpenAction() {
			super("Open");
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {

			Thread openThread = new Thread(new Runnable() {

				@Override
				public void run() {

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
								PointSourceGeneratorFrame.this, Utils.wrapLine(
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
										.getKey(), Boolean.TRUE.toString());

						PointSourceGeneratorFrame frame = new PointSourceGeneratorFrame();

						// List<Integer> rgbs = new ArrayList<Integer>();
						// rgbs.add(Color.WHITE.getRGB());
						// rgbs.add(Color.LIGHT_GRAY.getRGB());
						// rgbs.add(Color.DARK_GRAY.getRGB());
						// rgbs.add(Color.BLACK.getRGB());
						// frame.setImageColors(rgbs);

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
