package gov.epa.emissions.framework.services.cost.controlmeasure;

import java.util.ArrayList;
import java.util.List;

public class Sccs {
    
    private List sccs;
    
    public Sccs(){
        sccs = new ArrayList();
    }
    
    public void addScc(Scc scc){
        sccs.add(scc);
    }
    
    public Scc[] getSccs(){
        return (Scc[]) sccs.toArray(new Scc[0]);
    }

    public int size() {
        return sccs.size();
    }

    public Scc get(int index) {
        return (Scc) sccs.get(index);
    }
}
