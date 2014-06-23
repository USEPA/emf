package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class QAPrograms {

    private List list;
    
    private EmfSession session;

    public QAPrograms(EmfSession session, QAProgram[] programs) {
        this.session = session;
        this.list = new ArrayList(Arrays.asList(programs));
    }

    public QAProgram get(Object selected) throws EmfException {
        if (selected instanceof String) {
            return editProgram(selected);
        }
        
        if (selected == null)
            return null;
        
        int index = list.indexOf(selected);
        return (QAProgram) list.get(index);
    }
    
    private QAProgram editProgram(Object selected) throws EmfException {
        String newProgram = ((String) selected).trim();
        if (newProgram.length() == 0)
            throw new EmfException("QA Program name cannot be an empty string.");

        QAProgram program = new QAProgram(newProgram);
        int index = list.indexOf(program);
        if (index == -1) {// new input name
            QAProgram persistName = persistName(program);
            list.add(persistName);
            return persistName;
        }
        return (QAProgram) list.get(index);
    }
    
    private QAProgram persistName(QAProgram program) throws EmfException {
        if (session == null)
            return program;
        
        return session.qaService().addQAProgram(program);
    }

    public String[] names() {
        List names = new ArrayList();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            QAProgram element = (QAProgram) iter.next();
            names.add(element.getName());
        }

        return (String[]) names.toArray(new String[0]);
    }
}
