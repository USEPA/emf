package gov.epa.emissions.commons.gui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.table.DefaultTableModel;

public class SortFilterSelectUsersLauncher {

    public static void main(String[] args) throws Exception {
        SortFilterSelectUsersLauncher launcher = new SortFilterSelectUsersLauncher();

        RefreshableTableModel delegate = launcher.createUserManagementTableModel();
        SortFilterSelectModel model = new SortFilterSelectModel(delegate);

        JFrame frame = new JFrame();
        SortFilterSelectionPanel panel = new SortFilterSelectionPanel(frame, model);

        frame.getContentPane().add(panel);

        frame.setSize(new Dimension(500, 200));
        frame.setLocation(new Point(400, 200));

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent arg0) {
                System.exit(0);
            }
        });

        frame.setVisible(true);
    }

    private RefreshableTableModel createUserManagementTableModel() throws Exception {
        String[][] data = { { "joe", "Joe Fullman", "joef@fullman.com" }, { "mary", "Mary Joe", "mary@wonderful.net" },
                { "kevin", "Kevin Spacey", "kevin@spacey.com" } };

        String[] columns = { "username", "name", "email" };

        return new RefreshableTableModelStub(data, columns);
    }

    public class RefreshableTableModelStub extends DefaultTableModel implements RefreshableTableModel {

        public RefreshableTableModelStub(String[][] data, String[] columns) {
            super(data, columns);
        }

        public void refresh() {// Stub
        }

        public List elements(int[] selected) {
            // TODO Auto-generated method stub
            return null;
        }

    }

}
