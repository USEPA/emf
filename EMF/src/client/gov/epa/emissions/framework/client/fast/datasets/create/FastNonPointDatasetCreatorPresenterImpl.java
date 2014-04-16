package gov.epa.emissions.framework.client.fast.datasets.create;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.fast.MPSDTManagerPresenter;
import gov.epa.emissions.framework.client.fast.datasets.AbstractFastNonPointDatasetPresenterImpl;
import gov.epa.emissions.framework.client.fast.datasets.FastDatasetView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.fast.FastDataset;
import gov.epa.emissions.framework.services.fast.FastDatasetWrapper;
import gov.epa.emissions.framework.services.fast.FastNonPointDataset;

public class FastNonPointDatasetCreatorPresenterImpl extends AbstractFastNonPointDatasetPresenterImpl {

    public FastNonPointDatasetCreatorPresenterImpl(EmfSession session, FastDatasetView view,
            MPSDTManagerPresenter fastManagerPresenter) {
        super(session, view, fastManagerPresenter);
    }

    public FastNonPointDatasetCreatorPresenterImpl(EmfSession session, FastDatasetView view) {
        super(session, view);
    }

    public void doDisplay() {

        this.getView().observe(this);

        FastDataset fastDataset = new FastDataset();
        fastDataset.setFastNonPointDataset(new FastNonPointDataset());
        FastDatasetWrapper wrapper = new FastDatasetWrapper(fastDataset);
        this.setWrapper(wrapper);
        this.getView().display(this.getWrapper());
    }

    public void doSave() throws EmfException {

        this.getView().save(this.getWrapper());
        FastNonPointDataset nonPointDataset = this.getWrapper().getNonPointDataset();
        getService().addFastNonPointDataset(nonPointDataset, this.getSession().user());

        /*
         * Don't allow editing once it has been saved
         */
        this.closeView();
    }
}
