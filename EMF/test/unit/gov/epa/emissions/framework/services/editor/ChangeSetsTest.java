package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.framework.services.editor.ChangeSets.ChangeSetsIterator;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class ChangeSetsTest extends MockObjectTestCase {

    public void testShouldReturnListOfChangeSetsOnGet() {
        ChangeSets sets = new ChangeSets();

        Mock set1 = mock(ChangeSet.class);
        sets.add((ChangeSet) set1.proxy());

        assertEquals(1, sets.size());
    }

    public void testShouldSumNetIncreaseOfAllChangeSetsOnNetIncrease() {
        ChangeSets sets = new ChangeSets();

        Mock set1 = mock(ChangeSet.class);
        set1.stubs().method("netIncrease").will(returnValue(8));
        sets.add((ChangeSet) set1.proxy());

        Mock set2 = mock(ChangeSet.class);
        set2.stubs().method("netIncrease").will(returnValue(-2));
        sets.add((ChangeSet) set2.proxy());

        assertEquals(6, sets.netIncrease());
    }

    public void testShouldConfirmChangesIfAnyChangeSetHasChanges() {
        ChangeSets sets1 = new ChangeSets();
        Mock set1 = mock(ChangeSet.class);
        set1.stubs().method("hasChanges").will(returnValue(Boolean.TRUE));
        sets1.add((ChangeSet) set1.proxy());
        assertTrue("Should confirm changes if total net increase of all ChangeSets is greater than zero", sets1
                .hasChanges());

        ChangeSets sets2 = new ChangeSets();
        Mock set21 = mock(ChangeSet.class);
        set21.stubs().method("hasChanges").will(returnValue(Boolean.FALSE));
        sets2.add((ChangeSet) set21.proxy());
        Mock set22 = mock(ChangeSet.class);
        set22.stubs().method("hasChanges").will(returnValue(Boolean.TRUE));
        sets2.add((ChangeSet) set22.proxy());
        assertTrue("Should confirm changes if total net increase of all ChangeSets is less than zero", sets2
                .hasChanges());
    }

    public void testShouldBeAbleToIterateThroughContents() {
        ChangeSets sets = new ChangeSets();

        Mock set1 = mock(ChangeSet.class);
        set1.stubs().method("netIncrease").will(returnValue(8));
        ChangeSet set1Proxy = (ChangeSet) set1.proxy();
        sets.add(set1Proxy);

        Mock set2 = mock(ChangeSet.class);
        set2.stubs().method("netIncrease").will(returnValue(-2));
        ChangeSet set2Proxy = (ChangeSet) set2.proxy();
        sets.add(set2Proxy);

        ChangeSetsIterator iter = sets.iterator();
        assertEquals(set1Proxy, iter.next());
        assertEquals(set2Proxy, iter.next());
    }

    public void testShouldBeAbleLookupContents() {
        ChangeSets sets = new ChangeSets();

        Mock set1 = mock(ChangeSet.class);
        set1.stubs().method("netIncrease").will(returnValue(8));
        ChangeSet set1Proxy = (ChangeSet) set1.proxy();
        sets.add(set1Proxy);

        Mock set2 = mock(ChangeSet.class);
        set2.stubs().method("netIncrease").will(returnValue(-2));
        ChangeSet set2Proxy = (ChangeSet) set2.proxy();
        sets.add(set2Proxy);

        assertEquals(set1Proxy, sets.get(0));
        assertEquals(set2Proxy, sets.get(1));
    }

    public void testShouldBeAbleAddAnotherChangeSets() {
        ChangeSets sets0 = new ChangeSets();

        ChangeSets sets1 = new ChangeSets();

        Mock set1 = mock(ChangeSet.class);
        set1.stubs().method("netIncrease").will(returnValue(8));
        ChangeSet set1Proxy = (ChangeSet) set1.proxy();
        sets1.add(set1Proxy);

        Mock set2 = mock(ChangeSet.class);
        set2.stubs().method("netIncrease").will(returnValue(-2));
        ChangeSet set2Proxy = (ChangeSet) set2.proxy();
        sets1.add(set2Proxy);

        sets0.add(sets1);

        assertEquals(set1Proxy, sets0.get(0));
        assertEquals(set2Proxy, sets0.get(1));
    }
}
