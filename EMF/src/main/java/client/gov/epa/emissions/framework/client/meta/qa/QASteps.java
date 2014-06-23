package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.services.data.QAStep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class QASteps {

    private List sources;

    public QASteps(QAStep[] sources) {
        this.sources = new ArrayList();
        this.sources.addAll(Arrays.asList(sources));
    }

    QAStep[] filterDuplicates(QAStep[] steps) {
        List newSteps = new ArrayList();

        for (int i = 0; i < steps.length; i++) {
            if (!contains(sources, steps[i]))
                newSteps.add(steps[i]);
        }

        return (QAStep[]) newSteps.toArray(new QAStep[0]);
    }

    private boolean contains(List sources, QAStep step) {
        return contains((QAStep[]) sources.toArray(new QAStep[0]), step);
    }

    boolean contains(QAStep[] sources, QAStep step) {
        for (int i = 0; i < sources.length; i++) {
            if (sources[i].getName().equals(step.getName()) && sources[i].getVersion() == step.getVersion()) {
                return true;
            }
        }

        return false;
    }

    public void add(QAStep step) {
        sources.add(step);
    }

    public QAStep get(int index) {
        return (QAStep) sources.get(index);
    }

    public int size() {
        return sources.size();
    }

    public QAStep[] all() {
        return (QAStep[]) sources.toArray(new QAStep[0]);
    }

    public String namesList() {
        StringBuffer buf = new StringBuffer();
        for (Iterator iter = sources.iterator(); iter.hasNext();) {
            QAStep element = (QAStep) iter.next();
            buf.append(element.getName());
            if (iter.hasNext())
                buf.append(", ");
        }

        return buf.toString();
    }
}
