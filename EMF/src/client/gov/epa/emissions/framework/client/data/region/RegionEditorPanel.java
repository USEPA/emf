package gov.epa.emissions.framework.client.data.region;

import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.services.data.GeoRegion;
import gov.epa.emissions.framework.services.data.RegionType;

import java.awt.*;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

public class RegionEditorPanel extends JPanel {
    private TextField name;
    private TextField abbreviation;
    private TextField description;
    private TextField ioapiName;
    private TextField mapProjection;
    private TextField resolution;
    private TextField xorig;
    private TextField yorig;
    private TextField xcell;
    private TextField ycell;
    private TextField ncols;
    private TextField nrows;
    private TextField nthik;
    private ComboBox regionType;
    private ManageChangeables changeablesList;
    private GeoRegion region;
    private RegionType[] regionTypes;
    
    private Dimension preferredSize = new Dimension(222, 20);
    
    public RegionEditorPanel(ManageChangeables changeablesList, GeoRegion region, RegionType[] types){
        this.changeablesList = changeablesList;
        this.region = region;
        this.regionTypes = types;
    }
    
    public JPanel createPanel() {
        JPanel upper = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        
        regionType = new ComboBox(regionTypes, "Region type");
        regionType.setSelectedItem(region.getType());
        regionType.setPreferredSize(preferredSize);
        regionType.addActionListener(new AbstractAction(){
            public void actionPerformed(ActionEvent arg0) {
                turnOffOnFields();
            }
        });
        changeablesList.addChangeable(regionType);
        layoutGenerator.addLabelWidgetPair("Region Type:", regionType, upper);
        
        name = new TextField("name", 20, "Region name");
        name.setText(region.getName() == null ? "" : region.getName());
        changeablesList.addChangeable(name);
        layoutGenerator.addLabelWidgetPair("Name:", name, upper);
        
        abbreviation = new TextField("abbreviation", 20, "Region abbreviation");
        abbreviation.setText(region.getAbbreviation() == null ? "" : region.getAbbreviation());
        changeablesList.addChangeable(abbreviation);
        layoutGenerator.addLabelWidgetPair("Abbreviation:", abbreviation, upper);
        
        description = new TextField("description", 20, "Region description");
        description.setText(region.getDescription() == null ? "" : region.getDescription());
        changeablesList.addChangeable(description);
        layoutGenerator.addLabelWidgetPair("Description:", description, upper);
        
        ioapiName = new TextField("ioapiName", 20, "Region IOAPI name");
        ioapiName.setText(region.getIoapiName() == null ? "" : region.getIoapiName());
        changeablesList.addChangeable(ioapiName);
        layoutGenerator.addLabelWidgetPair("IOAPI Name:", ioapiName, upper);
        
        resolution = new TextField("resolution", 20, "Region resolution");
        resolution.setText(region.getResolution() == null ? "" : region.getResolution());
        changeablesList.addChangeable(resolution);
        layoutGenerator.addLabelWidgetPair("Resolution:", resolution, upper);
    
        mapProjection = new TextField("mapProjection", 20, "Region map projection");
        mapProjection.setText(region.getMapProjection() == null ? "" : region.getMapProjection());
        changeablesList.addChangeable(mapProjection);
        layoutGenerator.addLabelWidgetPair("Map Projection:", mapProjection, upper);
        
        layoutGenerator.makeCompactGrid(upper, 7, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10);// xPad, yPad

        Dimension upperDimension = new Dimension(350, 225);
        upper.setPreferredSize(upperDimension);
        upper.setMinimumSize(upperDimension);
        upper.setMaximumSize(upperDimension);

        JPanel left = new JPanel(new SpringLayout());
        JPanel right = new JPanel(new SpringLayout());
        SpringLayoutGenerator leftLayoutGenerator = new SpringLayoutGenerator();
        SpringLayoutGenerator rightLayoutGenerator = new SpringLayoutGenerator();

        xorig = new TextField("xorig", 8, "Region x orig");
        xorig.setText(region.getXorig() + "");
        leftLayoutGenerator.addLabelWidgetPair("XORIG:", xorig, left);
        changeablesList.addChangeable(xorig);

        yorig = new TextField("yorig", 8, "Region y orig");
        yorig.setText(region.getYorig() + "");
        rightLayoutGenerator.addLabelWidgetPair("YORIG:", yorig, right);
        changeablesList.addChangeable(yorig);

        xcell = new TextField("xcell", 8, "Region x cell");
        xcell.setText(region.getXcell() + "");
        leftLayoutGenerator.addLabelWidgetPair("XCELL:", xcell, left);
        changeablesList.addChangeable(xcell);

        ycell = new TextField("ycell", 8, "Region y cell");
        ycell.setText(region.getYcell() + "");
        rightLayoutGenerator.addLabelWidgetPair("YCELL:", ycell, right);
        changeablesList.addChangeable(ycell);

        ncols = new TextField("ncols", 8, "Region n cols");
        ncols.setText(region.getNcols() + "");
        leftLayoutGenerator.addLabelWidgetPair("NCOLS:", ncols, left);
        changeablesList.addChangeable(ncols);

        nrows = new TextField("nrows", 8, "Region n rows");
        nrows.setText(region.getNrows() + "");
        rightLayoutGenerator.addLabelWidgetPair("NROWS:", nrows, right);
        changeablesList.addChangeable(nrows);

        nthik = new TextField("nthik", 8, "Region n thik");
        nthik.setText(region.getNthik() + "");
        leftLayoutGenerator.addLabelWidgetPair("NTHIK:", nthik, left);
        changeablesList.addChangeable(nthik);

        leftLayoutGenerator.makeCompactGrid(left, 4, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10);// xPad, yPad
        rightLayoutGenerator.makeCompactGrid(right, 3, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10);// xPad, yPad

        JPanel bottomPanel = new JPanel();
        //bottomPanel.setLayout(new BorderLayout());//bottomPanel, BoxLayout.X_AXIS

        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));

        Dimension leftDimension = new Dimension(175, 130);
        left.setPreferredSize(leftDimension);
        left.setMinimumSize(leftDimension);
        left.setMaximumSize(leftDimension);
        left.setAlignmentY(TOP_ALIGNMENT);
        bottomPanel.add(left);
        Dimension rightDimension = new Dimension(175, 100);
//        dimension.setSize(right.getMinimumSize().getWidth(), 3 / 4 * left.getMinimumSize().getHeight());
        right.setMinimumSize(rightDimension);
        right.setPreferredSize(rightDimension);
        right.setMaximumSize(rightDimension);
        right.setAlignmentY(TOP_ALIGNMENT);
        bottomPanel.add(right);


/*
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.insets = new Insets(1, 1, 7, 6);
        JPanel lower = new JPanel(gridbag);
        
        JLabel xorigLabel = new JLabel("XORIG:", SwingConstants.RIGHT);
        JLabel yorigLabel = new JLabel("YORIG:", SwingConstants.RIGHT);
        JLabel blank1 = new JLabel();
        xorig = new TextField("xorig", 4, "Region x orig");
        xorig.setText(region.getXorig() + "");
        changeablesList.addChangeable(xorig);
        yorig = new TextField("yorig", 4, "Region y orig");
        yorig.setText(region.getYorig() + "");
        yorigLabel.setLabelFor(yorig);
        changeablesList.addChangeable(yorig);
        gridbag.setConstraints(xorigLabel, c);
        gridbag.setConstraints(yorigLabel, c);
        gridbag.setConstraints(xorig, c);
        gridbag.setConstraints(yorig, c);
        c.gridwidth = GridBagConstraints.REMAINDER; // end row
        gridbag.setConstraints(blank1, c);
        lower.add(xorigLabel);
        lower.add(xorig);
        lower.add(yorigLabel);
        lower.add(yorig);
        lower.add(blank1);
        xorigLabel.setLabelFor(xorig);

        JLabel xcellLabel = new JLabel("XCELL:", SwingConstants.RIGHT);
        JLabel ycellLabel = new JLabel("YCELL:", SwingConstants.RIGHT);
        JLabel blank2 = new JLabel();
        xcell = new TextField("xcell", 4, "Region x cell");
        xcell.setText(region.getXcell() + "");
        xcellLabel.setLabelFor(xcell);
        changeablesList.addChangeable(xcell);
        ycell = new TextField("ycell", 4, "Region y cell");
        ycell.setText(region.getYcell() + "");
        ycellLabel.setLabelFor(ycell);
        changeablesList.addChangeable(ycell);
        c.gridwidth = 1; // next-to-last in row
        gridbag.setConstraints(xcellLabel, c);
        gridbag.setConstraints(ycellLabel, c);
        gridbag.setConstraints(xcell, c);
        gridbag.setConstraints(ycell, c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(blank2, c);
        lower.add(xcellLabel);
        lower.add(xcell);
        lower.add(ycellLabel);
        lower.add(ycell);
        lower.add(blank2);

        JLabel ncolsLabel = new JLabel("NCOLS:", SwingConstants.RIGHT);
        JLabel nrowsLabel = new JLabel("NROWS:", SwingConstants.RIGHT);
        JLabel blank3 = new JLabel();
        ncols = new TextField("ncols", 4, "Region n cols");
        ncols.setText(region.getNcols() + "");
        ncolsLabel.setLabelFor(ncols);
        changeablesList.addChangeable(ncols);
        nrows = new TextField("nrows", 4, "Region n rows");
        nrows.setText(region.getNrows() + "");
        nrowsLabel.setLabelFor(nrows);
        changeablesList.addChangeable(nrows);
        c.gridwidth = 1; // next-to-last in row
        gridbag.setConstraints(ncolsLabel, c);
        gridbag.setConstraints(nrowsLabel, c);
        gridbag.setConstraints(ncols, c);
        gridbag.setConstraints(nrows, c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(blank3, c);
        lower.add(ncolsLabel);
        lower.add(ncols);
        lower.add(nrowsLabel);
        lower.add(nrows);
        lower.add(blank3);

        JLabel nthikLabel = new JLabel("NTHIK:", SwingConstants.RIGHT);
        JLabel label1 = new JLabel();
        JLabel label2 = new JLabel();
        JLabel label3 = new JLabel();
        nthik = new TextField("nthik", 4, "Region n thik");
        nthik.setText(region.getNthik() + "");
        nthikLabel.setLabelFor(nthik);
        changeablesList.addChangeable(nthik);
        c.gridwidth = 1; // next-to-last in row
        gridbag.setConstraints(nthikLabel, c);
        gridbag.setConstraints(label1, c);
        gridbag.setConstraints(nthik, c);
        gridbag.setConstraints(label2, c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(label3, c);
        lower.add(nthikLabel);
        lower.add(nthik);
        lower.add(label1);
        lower.add(label2);
        lower.add(label3);
        
*/
        upper.setAlignmentX(CENTER_ALIGNMENT);
        bottomPanel.setAlignmentX(CENTER_ALIGNMENT);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(upper);
        panel.add(bottomPanel);   //lower bottomPanel
        
        turnOffOnFields();
        return panel;
    }
    
    private void turnOffOnFields() {
        RegionType type = (RegionType) regionType.getSelectedItem();
        
        if (type == null || !type.getName().toUpperCase().contains("I/O API")) {
            ioapiName.setEnabled(false);
            mapProjection.setEnabled(false);
            ncols.setEnabled(false);
            nrows.setEnabled(false);
            xorig.setEnabled(false);
            yorig.setEnabled(false);
            xcell.setEnabled(false);
            ycell.setEnabled(false);
            nthik.setEnabled(false);
            
            return;
        }
        
        ioapiName.setEnabled(true);
        mapProjection.setEnabled(true);
        ncols.setEnabled(true);
        nrows.setEnabled(true);
        xorig.setEnabled(true);
        yorig.setEnabled(true);
        xcell.setEnabled(true);
        ycell.setEnabled(true);
        nthik.setEnabled(true);
    }

    public void setFields() {
        region.setType((RegionType)regionType.getSelectedItem());
        region.setName(name.getText() == null ? null : name.getText().trim());
        region.setAbbreviation(abbreviation.getText() == null ? null : abbreviation.getText().trim());
        region.setDescription(description.getText() == null ? null : description.getText().trim());
        region.setResolution(resolution.getText() == null ? null : resolution.getText().trim());
        
        if (ioapiName.isEnabled())
            region.setIoapiName(ioapiName.getText() == null ? null : ioapiName.getText().trim());
        
        if (mapProjection.isEnabled())
            region.setMapProjection(mapProjection.getText() == null ? null : mapProjection.getText().trim());
        
        if (ncols.isEnabled())
            region.setNcols(Integer.parseInt(ncols.getText()));
        
        if (nrows.isEnabled())
            region.setNrows(Integer.parseInt(nrows.getText()));
        
        if (xcell.isEnabled())
            region.setXcell(Float.parseFloat(xcell.getText()));
        
        if (ycell.isEnabled())
            region.setYcell(Float.parseFloat(ycell.getText()));
        
        if (xorig.isEnabled())
            region.setXorig(Float.parseFloat(xorig.getText()));
        
        if (yorig.isEnabled())
            region.setYorig(Float.parseFloat(yorig.getText()));
        
        if (nthik.isEnabled())
            region.setNthik(Integer.parseInt(nthik.getText()));
    }

}
