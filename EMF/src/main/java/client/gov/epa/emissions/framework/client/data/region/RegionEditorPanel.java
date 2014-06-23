package gov.epa.emissions.framework.client.data.region;

import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.services.data.GeoRegion;
import gov.epa.emissions.framework.services.data.RegionType;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
        
        regionType = new ComboBox(regionTypes);
        regionType.setSelectedItem(region.getType());
        regionType.setPreferredSize(preferredSize);
        regionType.addActionListener(new AbstractAction(){
            public void actionPerformed(ActionEvent arg0) {
                turnOffOnFields();
            }
        });
        changeablesList.addChangeable(regionType);
        layoutGenerator.addLabelWidgetPair("Region Type:", regionType, upper);
        
        name = new TextField("name", 20);
        name.setText(region.getName() == null ? "" : region.getName());
        changeablesList.addChangeable(name);
        layoutGenerator.addLabelWidgetPair("Name:", name, upper);
        
        abbreviation = new TextField("abbreviation", 20);
        abbreviation.setText(region.getAbbreviation() == null ? "" : region.getAbbreviation());
        changeablesList.addChangeable(abbreviation);
        layoutGenerator.addLabelWidgetPair("Abbreviation:", abbreviation, upper);
        
        description = new TextField("description", 20);
        description.setText(region.getDescription() == null ? "" : region.getDescription());
        changeablesList.addChangeable(description);
        layoutGenerator.addLabelWidgetPair("Description:", description, upper);
        
        ioapiName = new TextField("ioapiName", 20);
        ioapiName.setText(region.getIoapiName() == null ? "" : region.getIoapiName());
        changeablesList.addChangeable(ioapiName);
        layoutGenerator.addLabelWidgetPair("IOAPI Name:", ioapiName, upper);
        
        resolution = new TextField("resolution", 20);
        resolution.setText(region.getResolution() == null ? "" : region.getResolution());
        changeablesList.addChangeable(resolution);
        layoutGenerator.addLabelWidgetPair("Resolution:", resolution, upper);
    
        mapProjection = new TextField("mapProjection", 20);
        mapProjection.setText(region.getMapProjection() == null ? "" : region.getMapProjection());
        changeablesList.addChangeable(mapProjection);
        layoutGenerator.addLabelWidgetPair("Map Projection:", mapProjection, upper);
        
        layoutGenerator.makeCompactGrid(upper, 7, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10);// xPad, yPad
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.insets = new Insets(1, 1, 7, 6);
        JPanel lower = new JPanel(gridbag);
        
        JLabel xorigLabel = new JLabel("XORIG:", SwingConstants.RIGHT);
        JLabel yorigLabel = new JLabel("YORIG:", SwingConstants.RIGHT);
        JLabel blank1 = new JLabel();
        xorig = new TextField("xorig", 4);
        xorig.setText(region.getXorig() + "");
        changeablesList.addChangeable(xorig);
        yorig = new TextField("yorig", 4);
        yorig.setText(region.getYorig() + "");
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

        JLabel xcellLabel = new JLabel("XCELL:", SwingConstants.RIGHT);
        JLabel ycellLabel = new JLabel("YCELL:", SwingConstants.RIGHT);
        JLabel blank2 = new JLabel();
        xcell = new TextField("xcell", 4);
        xcell.setText(region.getXcell() + "");
        changeablesList.addChangeable(xcell);
        ycell = new TextField("ycell", 4);
        ycell.setText(region.getYcell() + "");
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
        ncols = new TextField("ncols", 4);
        ncols.setText(region.getNcols() + "");
        changeablesList.addChangeable(ncols);
        nrows = new TextField("nrows", 4);
        nrows.setText(region.getNrows() + "");
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
        nthik = new TextField("nthik", 4);
        nthik.setText(region.getNthik() + "");
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
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(upper);
        panel.add(lower);
        
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
