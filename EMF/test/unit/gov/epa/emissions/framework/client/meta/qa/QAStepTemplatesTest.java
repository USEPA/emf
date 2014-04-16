package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import junit.framework.TestCase;

public class QAStepTemplatesTest extends TestCase {

    private QAStepTemplates templates;

    private QAStepTemplate req1;

    private QAStepTemplate req2;

    private QAStepTemplate optional;

    protected void setUp() throws Exception {
        req1 = required("req1");
        req1.setOrder(0.001f);
        req2 = required("req2");
        req2.setOrder(0.002f);
        optional = optional("opt");
        optional.setOrder(123.123f);
        
        QAStepTemplate[] list = { optional, req1, req2 };
        templates = new QAStepTemplates(list);
    }

    public void testGetNamesOfRequired() {
        String[] names = templates.namesOfRequired();
        assertEquals(2, names.length);
        assertEquals("req1", names[0]);
        assertEquals("req2", names[1]);
    }

    public void testGetRequired() {
        QAStepTemplate[] required = templates.required();
        assertEquals(2, required.length);
        assertSame(req1, required[0]);
        assertSame(req2, required[1]);
    }

    public void testGetOptional() {
        QAStepTemplate[] optionalList = templates.optional();
        assertEquals(1, optionalList.length);
        assertSame(optional, optionalList[0]);
    }

    private QAStepTemplate required(String name) {
        QAStepTemplate template = new QAStepTemplate();
        template.setName(name);
        template.setRequired(true);

        return template;
    }

    private QAStepTemplate optional(String name) {
        QAStepTemplate template = new QAStepTemplate();
        template.setName(name);
        template.setRequired(false);

        return template;
    }

    public void testCreateRequiredSteps() {
        EmfDataset dataset = new EmfDataset();
        dataset.setId(2);
        
        Version version = new Version();
        version.setVersion(3);
        
        QAStep[] results = templates.createRequiredSteps(dataset, version);
        assertEquals(2, results.length);
        
        QAStep step1 = results[0];
        assertEquals(dataset.getId(), step1.getDatasetId());
        assertEquals(version.getVersion(), step1.getVersion());
        assertEquals(req1.getName(), step1.getName());

        QAStep step2 = results[1];
        assertEquals(dataset.getId(), step2.getDatasetId());
        assertEquals(version.getVersion(), step2.getVersion());
        assertEquals(req2.getName(), step2.getName());
    }

    public void testCreateOptionalSteps() {
        EmfDataset dataset = new EmfDataset();
        dataset.setId(2);
        
        Version version = new Version();
        version.setVersion(3);
        
        QAStepTemplate[] optionals = {optional};
        QAStep[] results = templates.createOptionalSteps(optionals, dataset, version);
        assertEquals(1, results.length);
        
        QAStep step = results[0];
        assertEquals(dataset.getId(), step.getDatasetId());
        assertEquals(version.getVersion(), step.getVersion());
        assertEquals(optional.getName(), step.getName());
    }
    
    public void testCreateAllSteps() {
        EmfDataset dataset = new EmfDataset();
        dataset.setId(2);
        
        Version version = new Version();
        version.setVersion(3);
        
        QAStepTemplate[] optionals = {optional};
        QAStep[] results = templates.createSteps(optionals, dataset, version);
        assertEquals(3, results.length);
        
        assertEquals(req1.getName(), results[0].getName());
        assertEquals(req2.getName(), results[1].getName());
        assertEquals(optional.getName(), results[2].getName());
    }
    
    public void testShouldSortByOrder() {
        QAStepTemplate[] localTemplates = templates.sortByOrder();
        
        assertEquals(req1.getName(), localTemplates[0].getName());
        assertEquals(req1.getOrder() + "", localTemplates[0].getOrder() + "");
        assertEquals(req2.getName(), localTemplates[1].getName());
        assertEquals(req2.getOrder() + "", localTemplates[1].getOrder() + "");
        assertEquals(optional.getName(), localTemplates[2].getName());
        assertEquals(optional.getOrder() + "", localTemplates[2].getOrder() + "");
        
    }
}
