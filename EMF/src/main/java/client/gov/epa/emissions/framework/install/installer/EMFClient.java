package gov.epa.emissions.framework.install.installer;

import javax.swing.JFrame;

public class EMFClient {
    public static void main(String[] args) throws Exception {
        InstallWindow view = new InstallWindow();
        view.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        InstallPresenter presenter = new InstallPresenter();
        presenter.display(view);
    }
}
