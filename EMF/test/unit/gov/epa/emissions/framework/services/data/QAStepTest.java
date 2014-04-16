package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.data.QAStepTemplate;

import java.util.Date;

import junit.framework.TestCase;

public class QAStepTest extends TestCase {

    public void testShouldObtainAllParamsOfQAStepTemplate() {
        QAStepTemplate template = new QAStepTemplate();
        template.setName("template");
        template.setProgram(new QAProgram("program"));
        template.setProgramArguments("args");
        template.setDescription("description");
        template.setRequired(true);
        template.setOrder((float)1.2);
        
        QAStep step = new QAStep(template, 2);
        
        assertEquals(template.getName(), step.getName());
        assertEquals(template.getProgram(), step.getProgram());
        assertEquals(template.getProgramArguments(), step.getProgramArguments());
        assertEquals(template.isRequired(), step.isRequired());
        assertEquals(2, step.getVersion());
        assertEquals(template.getDescription(), step.getDescription());
    }

    public void testShouldHaveWhenWhoResultAndStatusAsAttributes() {
        QAStep step = new QAStep();
        
        Date date = new Date();
        step.setDate(date);
        
        step.setWho("user");
        step.setComments("result");
        step.setStatus("status");
        step.setDescription("description");
        step.setConfiguration("dataset one");
        
        assertEquals(date, step.getDate());
        assertEquals("user", step.getWho());
        assertEquals("result", step.getComments());
        assertEquals("status", step.getStatus());
        assertEquals("description", step.getDescription());
        assertEquals("dataset one", step.getConfiguration());
    }
}
