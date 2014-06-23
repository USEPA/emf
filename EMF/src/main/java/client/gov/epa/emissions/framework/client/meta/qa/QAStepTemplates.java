package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class QAStepTemplates {

    private QAStepTemplate[] templates;

    public QAStepTemplates(QAStepTemplate[] templates) {
        this.templates = templates;
    }

    public String[] namesOfRequired() {
        List names = new ArrayList();
        QAStepTemplate[] required = required();
        for (int i = 0; i < required.length; i++)
            names.add(required[i].getName());

        return (String[]) names.toArray(new String[0]);
    }

    public QAStepTemplate[] optional() {
        List results = new ArrayList();
        for (int i = 0; i < templates.length; i++) {
            if (!templates[i].isRequired())
                results.add(templates[i]);
        }
//      Created by RVA (with help from Alison) to sort the optional QA Step Templates before sending them to the JList
        QAStepTemplate[] sortedArray = (QAStepTemplate[]) results.toArray(new QAStepTemplate[results.size()]);
        Arrays.sort(sortedArray, new QAStepNameComparator());
        return sortedArray;
    }

    public QAStep[] createRequiredSteps(EmfDataset dataset, Version version) {
        List steps = new ArrayList();

        QAStepTemplate[] required = required();
        for (int i = 0; i < required.length; i++) {
            QAStep step = new QAStep(dataset, version, required[i]);
            steps.add(step);
        }

        return (QAStep[]) steps.toArray(new QAStep[0]);
    }

    public QAStepTemplate[] required() {
        List required = new ArrayList();
        for (int i = 0; i < templates.length; i++) {
            if (templates[i].isRequired())
                required.add(templates[i]);
        }
        //Created by RVA to sort the required QA Step Templates before sending them to the JList
        QAStepTemplate[] sortedArray2 = (QAStepTemplate[]) required.toArray(new QAStepTemplate[required.size()]);
        Arrays.sort(sortedArray2, new QAStepNameComparator());
        return sortedArray2;
    }

    public QAStep[] createOptionalSteps(QAStepTemplate[] optionals, EmfDataset dataset, Version version) {
        List steps = new ArrayList();

        for (int i = 0; i < optionals.length; i++) {
            QAStep step = new QAStep(dataset, version, optionals[i]);
            steps.add(step);
        }

        return (QAStep[]) steps.toArray(new QAStep[0]);
    }

    
    public QAStep[] createSteps(QAStepTemplate[] optional, EmfDataset dataset, Version version) {
        List all = new ArrayList();
        
        QAStep[] requiredSteps = createRequiredSteps(dataset, version);
        all.addAll(Arrays.asList(requiredSteps));
        
        QAStep[] optionalSteps = createOptionalSteps(optional, dataset, version);
        all.addAll(Arrays.asList(optionalSteps));
        
        return (QAStep[]) all.toArray(new QAStep[0]);
    }
    
    public QAStepTemplate[] sortByOrder() {
        Arrays.sort(templates, new QAStepOrderComparator());
        return templates;
    }

}
