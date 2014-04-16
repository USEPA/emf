package gov.epa.emissions.framework.client.meta.qa;

public class EditQAArgumentsPresenter {
    
    private EditQAArgumentsView view;
    private EditQAStepView view2;
    
    public EditQAArgumentsPresenter(EditQAArgumentsView view, EditQAStepView view2) {
        this.view = view;
        this.view2 = view2;
    }

    public void display() {
        view.observe(this);
        view.display();
    }
    
    public void refreshArgs(String argText) {
        view2.updateArgumentsTextArea(argText);
    }
}
