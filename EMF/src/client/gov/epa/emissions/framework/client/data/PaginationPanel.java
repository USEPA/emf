package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.data.viewer.TablePresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.IconButton;
import gov.epa.emissions.framework.ui.ImageResources;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.NumberFormattedTextField;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class PaginationPanel extends JPanel implements ObserverPanel {

    private NumberFormattedTextField recordInput;

    private TablePresenter presenter;

    private Label current;

    private MessagePanel messagePanel;

    private JSlider slider;

    private Label filteredRecords;

    private IconButton lastButton;

    private IconButton firstButton;

    private IconButton prevButton;

    private IconButton nextButton;

    private int totalRecords;

    private Label totalRecordsLabel;

    public PaginationPanel(MessagePanel messagePanel) {
        super(new BorderLayout());
        this.messagePanel = messagePanel;
        this.setMinimumSize(new Dimension(300, 300));
    }

    private void doLayout(int totalRecordsCount) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        container.add(recordsPanel(totalRecordsCount));
        container.add(layoutControls(totalRecordsCount));

        super.add(container, BorderLayout.LINE_END);
    }

    private JPanel recordsPanel(int totalRecordsCount) {
        JPanel panel = new JPanel();

        JLabel currentName = new JLabel("Current:");
        panel.add(currentName);
        current = new Label("Current", "               ");
        current.setToolTipText("Range of displayed records");
        currentName.setLabelFor(current);
        panel.add(current);

        JLabel filtered = new JLabel("Filtered:");
        panel.add(filtered);
        filteredRecords = new Label("Filtered", "" + totalRecordsCount);
        filteredRecords.setToolTipText("Number of records in dataset matching the filter");
        filtered.setLabelFor(filteredRecords);
        panel.add(filteredRecords);

        panel.add(new JLabel("of"));
        totalRecordsLabel = new Label("Total records", "" + totalRecordsCount);
        totalRecordsLabel.setToolTipText("Total number of records in dataset");
        panel.add(totalRecordsLabel);

        return panel;
    }

    public void init(TablePresenter presenter) {
        this.presenter = presenter;
        try {
            totalRecords = presenter.totalRecords();
            presenter.setTotalRecords(totalRecords);
            doLayout(totalRecords);

            if (totalRecords == 0)
                disableControlPanel();
        } catch (EmfException e) {
            messagePanel.setError("Could not obtain Total Records." + e.getMessage());
        }
    }

    private JPanel layoutControls(int totalRecords) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JToolBar top = new JToolBar();
        top.setFloatable(false);

        ImageResources res = new ImageResources();
        top.add(firstButton(res));
        top.add(prevButton(res));
        top.add(recordInputField(totalRecords));
        top.add(nextButton(res));
        top.add(lastButton(res));

        JPanel bottom = new JPanel();
        bottom.add(slider(totalRecords));

        panel.add(top);
        panel.add(bottom);

        return panel;
    }

    private JFormattedTextField recordInputField(final int max) {
        recordInput = new NumberFormattedTextField(1, max, 7, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (!verifyInput(slider))
                    return;

                Integer value = (Integer) recordInput.getValue();
                int record = value.intValue();
                displayPage(record);
            }
        });
        recordInput.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if ("value".equals(event.getPropertyName())) {
                    Number record = (Number) event.getNewValue();
                    displayPage(record.intValue());
                }
            }
        });

        recordInput.setToolTipText("Please input 'record number', and press Enter.");
        return recordInput;
    }

    private JSlider slider(int max) {
        if (max == 0)
            slider = new JSlider(JSlider.HORIZONTAL, 0, max, 0);
        else
            slider = new JSlider(JSlider.HORIZONTAL, 1, max, 1);

        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                int val = source.getValue();
                if (!source.getValueIsAdjusting()) { // done adjusting
                    recordInput.setValue(new Integer(val)); // update value
                } else { // value is adjusting; just set the text
                    recordInput.setText(String.valueOf(val));
                }
            }
        });

        slider.setToolTipText("Record position indicator via a slider control");

        return slider;
    }

    public void updateStatus(Page page) {
        if (page.getMax() == 0) {
            current.setText("0");
            return;
        }
        current.setText(page.getMin() + " - " + page.getMax());
        slider.setValue(page.getMin());
    }

    public void updateFilteredRecordsCount(int filtered) {
        totalRecordsLabel.setText("" + totalRecords);
        filteredRecords.setText("" + filtered);
        if (filtered == 0) {
            recordInput.setText("0");
            recordInput.setRange(0, 0);
            disableControlPanel();
            return;
        }

        enableControlPanel();
        slider.setMaximum(filtered);
        recordInput.setRange(1, filtered);
    }

    private void enableControlPanel() {
        slider.setEnabled(true);
        recordInput.setEnabled(true);
        firstButton.setEnabled(true);
        lastButton.setEnabled(true);
        prevButton.setEnabled(true);
        nextButton.setEnabled(true);
    }

    private void disableControlPanel() {
        slider.setEnabled(false);
        recordInput.setEnabled(false);
        firstButton.setEnabled(false);
        lastButton.setEnabled(false);
        prevButton.setEnabled(false);
        nextButton.setEnabled(false);
    }

    private boolean verifyInput(JSlider slider) {
        String val = recordInput.getText();
        try {
            recordInput.commitEdit();
            messagePanel.clear();
            return true;
        } catch (Exception pe) {
            messagePanel.setError("Invalid value: " + val + ". Please use numbers between " + slider.getMinimum()
                    + " and " + slider.getMaximum());
            recordInput.selectAll();
            return false;
        }
    }

    public class NumberVerifier extends InputVerifier {
        public boolean verify(JComponent input) {
            return verifyInput(slider);
        }
    }

    private IconButton lastButton(ImageResources res) {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doDisplayLast();
            }
        };
        lastButton = new IconButton("Last", "Go to Last Page", res.last("Go to Last Page"), action);
        return lastButton;
    }

    private IconButton nextButton(ImageResources res) {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doDisplayNext();
            }
        };
        nextButton = new IconButton("Next", "Go to Next Page", res.next("Go to Next Page"), action);
        return nextButton;
    }

    private IconButton prevButton(ImageResources res) {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doDisplayPrevious();
            }
        };
        prevButton = new IconButton("Prev", "Go to Previous Page", res.prev("Go to Previous Page"), action);
        return prevButton;
    }

    private IconButton firstButton(ImageResources res) {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doDisplayFirst();
            }
        };
        firstButton = new IconButton("First", "Go to First Page", res.first("Go to First Page"), action);
        return firstButton;
    }

    private void doDisplayPrevious() {
        clearMessages();
        try {
            presenter.doDisplayPrevious();
        } catch (EmfException e) {
            setErrorMessage("Could not display Previous Page." + e.getMessage());
        }
    }

    private void doDisplayNext() {
        clearMessages();
        try {
            presenter.doDisplayNext();
        } catch (EmfException e) {
            setErrorMessage("Could not display Next Page." + e.getMessage());
        }
    }

    private void setErrorMessage(String message) {
        messagePanel.setError(message);
    }

    private void clearMessages() {
        messagePanel.clear();
    }

    private void displayPage(int record) {
        clearMessages();
        try {
            presenter.doDisplayPageWithRecord(record);
        } catch (EmfException e) {
            messagePanel.setError("Could not display Page with record: " + record + "." + e.getMessage());
        }
    }

    private void doDisplayLast() {
        clearMessages();
        try {
            presenter.doDisplayLast();
        } catch (EmfException e) {
            setErrorMessage("Could not display Last Page." + e.getMessage());
        }
    }

    private void doDisplayFirst() {
        clearMessages();
        try {
            presenter.doDisplayFirst();
        } catch (EmfException e) {
            messagePanel.setError("Could not display First Page." + e.getMessage());
        }
    }

    public void update(int number) {
        this.totalRecords = totalRecords + number;
        presenter.setTotalRecords(totalRecords);
    }

    public int getPreviousNumber() {
        return this.totalRecords;
    }

    public void refresh(String filter, String sortOrder) throws EmfException {
        presenter.doApplyConstraints(filter, sortOrder);
    }

}
