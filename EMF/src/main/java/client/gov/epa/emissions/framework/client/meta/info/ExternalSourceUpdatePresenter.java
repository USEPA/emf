package gov.epa.emissions.framework.client.meta.info;

import gov.epa.emissions.commons.data.ExternalSource;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.Keywords;

public class ExternalSourceUpdatePresenter {

    private EmfDataset dataset;

    private EmfSession session;

    private InfoTabPresenter sourceTabPresenter;
    
    public ExternalSourceUpdatePresenter(InfoTabPresenter sourceTabPresenter) {
        this.dataset = sourceTabPresenter.getDataset();
        this.session = sourceTabPresenter.getSession();
        this.sourceTabPresenter = sourceTabPresenter;
    }

    public void display(ExternalSourceUpdateWindow view) {
        view.observe(this);
        view.display();
    }

    public void update(String folder, boolean isMassLoc) throws Exception {
        KeyVal[] keys = dataset.getKeyVals();
        Keywords keywords = new Keywords(session.dataCommonsService().getKeywords());
        Keyword massLocKeyword = keywords.get("MASS_STORAGE_LOCATION");
        Keyword prevLocKeyword = keywords.get("PREVIOUS_LOCATION");
        ExternalSource[] sources = sourceTabPresenter.getViewableSources();
        
        if (sources == null || sources.length == 0)
            throw new EmfException("No sources to update.");
        
        String existLoc = getFileInfo(sources[0], false);

        int massLoc = -1;
        int prevLoc = -1;

        for (int i = 0; i < keys.length; i++) {
            if (keys[i].getKeyword().equals(prevLocKeyword))
                prevLoc = i;

            if (keys[i].getKeyword().equals(massLocKeyword))
                massLoc = i;
        }

        updateKeyVals(existLoc, folder, isMassLoc, massLoc, prevLoc, keys, massLocKeyword, prevLocKeyword);

        if (!existLoc.equals(folder))
            updateExternalSources(folder);

        refreshDatasetSource();
    }

    /*
     * NOTE: following implementation reflects this logic: 1. From work location to new work location set
     * PREVIOUS_LOCATION 2. From work location to mass storage location set PREVIOUS_LOCATION set MASS_STORAGE_LOCATION
     * 3. From mass storage location to new mass storage location set MASS_STORAGE_LOCATION 4. From mass storage
     * location to work location set nothing
     */
    private void updateKeyVals(String existLoc, String folder, boolean isMassLoc, int massLoc, int prevLoc,
            KeyVal[] keys, Keyword massLocKeyword, Keyword prevLocKeyword) {
        if (isMassLoc && massLoc == -1)
            fromWork2MassStorage(existLoc, folder, massLoc, prevLoc, keys, massLocKeyword, prevLocKeyword);

        if (isMassLoc && massLoc != -1 && !keys[massLoc].getValue().equals(existLoc))
            fromWork2MassStorage(existLoc, folder, massLoc, prevLoc, keys, massLocKeyword, prevLocKeyword);

        if (isMassLoc && massLoc != -1 && keys[massLoc].getValue().equals(existLoc))
            fromMassStorage2MassStorage(folder, massLoc, keys);

        if (!isMassLoc && massLoc == -1) {
            fromWork2Work(existLoc, prevLoc, keys, prevLocKeyword);
        }

        if (!isMassLoc && massLoc != -1 && !keys[massLoc].getValue().equals(existLoc)) {
            fromWork2Work(existLoc, prevLoc, keys, prevLocKeyword);
        }
    }

    private void fromWork2Work(String existLoc, int prevLoc, KeyVal[] keys, Keyword prevLocKeyword) {
        if (prevLoc == -1) {
            dataset.addKeyVal(new KeyVal(prevLocKeyword, existLoc));
            return;
        }

        keys[prevLoc].setValue(existLoc);
        dataset.setKeyVals(keys);
    }

    private void fromMassStorage2MassStorage(String folder, int massLoc, KeyVal[] keys) {
        keys[massLoc].setValue(folder);
        dataset.setKeyVals(keys);
    }

    private void fromWork2MassStorage(String existLoc, String folder, int massLoc, int prevLoc, KeyVal[] keys,
            Keyword massLocKeyword, Keyword prevLocKeyword) {
        if (prevLoc != -1)
            keys[prevLoc].setValue(existLoc);

        if (massLoc != -1)
            keys[massLoc].setValue(folder);

        dataset.setKeyVals(keys);

        if (prevLoc == -1)
            dataset.addKeyVal(new KeyVal(prevLocKeyword, existLoc));

        if (massLoc == -1)
            dataset.addKeyVal(new KeyVal(massLocKeyword, folder));
    }

    private void updateExternalSources(String folder) throws Exception {
        try {
            session.dataService().updateExternalSources(dataset.getId(), folder);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public void refreshDatasetSource() throws EmfException {
        this.sourceTabPresenter.refreshExternalSources();
    }

    private String getFileInfo(ExternalSource ext, boolean name) {
        String source = ext.getDatasource();
        
        if (source == null)
            return null;
        
        source = source.trim();
        String sep = (source.contains("/") ? "/" : "\\");
        int index = source.lastIndexOf(sep);
        
        if (name)
            return source.substring(index + 1);
        
        if (index < 0)
            index = source.length();
        
        return source.substring(0, index);
    }
}
