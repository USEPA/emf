package gov.epa.emissions.commons.gui;

import java.util.ArrayList;
import java.util.List;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class ChangeablesTest extends MockObjectTestCase {

    private DefaultChangeables model;

    protected void setUp() {
        model = new DefaultChangeables(null);
    }

    public void testShouldConfirmFalseIfNoChangesInChangables() {
        model.add((Changeable) changeable(false).proxy());
        model.add((Changeable) changeable(false).proxy());
        model.add((Changeable) changeable(false).proxy());

        assertFalse("No changes, since none of the Changeables have changes", model.hasChanges());
    }

    private Mock changeable(boolean hasChanges) {
        Mock changeable = mock(Changeable.class);
        changeable.stubs().method("hasChanges").withNoArguments().will(returnValue(hasChanges));
        changeable.expects(once()).method("observe").with(same(model));

        return changeable;
    }
    
    private Mock resetChangeable() {
        Mock changeable = mock(Changeable.class);
        changeable.expects(once()).method("observe").with(same(model));
        changeable.expects(once()).method("clear").withNoArguments();
        changeable.stubs().method("hasChanges").withNoArguments().will(returnValue(false));
        
        return changeable;
    }

    public void testShouldConfirmTrueIfOneChangesInChangables() {
        model.add((Changeable) changeable(false).proxy());
        model.add((Changeable) changeable(false).proxy());
        model.add((Changeable) changeable(false).proxy());
        model.add((Changeable) changeable(true).proxy());

        assertTrue("Should have changes, since one of the Changeables has changes", model.hasChanges());
    }
    
    public void testShouldAddListOfChangeables() {
        List list = new ArrayList();
        list.add(changeable(false).proxy());
        list.add(changeable(false).proxy());
        list.add(changeable(true).proxy());
        list.add(changeable(false).proxy());
        list.add(changeable(false).proxy());
        list.add(changeable(true).proxy());
        
        model.add(list);
        assertTrue(list.size() == 6);
        assertTrue("Should have changes, since two of the Changeables has changes", model.hasChanges());
    }
    
    public void testShouldResetChanges() {
        List list = new ArrayList();
        list.add(resetChangeable().proxy());
        list.add(resetChangeable().proxy());
        list.add(resetChangeable().proxy());
        list.add(resetChangeable().proxy());
        list.add(resetChangeable().proxy());
        list.add(resetChangeable().proxy());
        
        model.add(list);
        assertTrue(list.size() == 6);
        model.resetChanges();
        assertFalse("Should have no changes, since they are reset", model.hasChanges());
    }
}
