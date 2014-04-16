package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.framework.client.UserAcceptanceTestCase;
import gov.epa.emissions.framework.client.console.EmfConsole;

public class ConsoleActions {

    private UserAcceptanceTestCase testcase;

    private EmfConsole console;

    public ConsoleActions(UserAcceptanceTestCase testcase) {
        this.testcase = testcase;
    }

    public EmfConsole open() {
        console = testcase.openConsole();
        return console;
    }

    public void close() {
        console.disposeView();
    }

}
