package gov.epa.emissions.framework.client.fast.datasets.view;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.fast.MPSDTManagerPresenter;
import gov.epa.emissions.framework.client.fast.datasets.AbstractFastNonPointDatasetPresenterImpl;
import gov.epa.emissions.framework.client.fast.datasets.FastDatasetView;
import gov.epa.emissions.framework.services.fast.FastDatasetWrapper;

public class FastNonPointDatasetViewPresenterImpl extends AbstractFastNonPointDatasetPresenterImpl {

    public FastNonPointDatasetViewPresenterImpl(EmfSession session, FastDatasetView view,
            MPSDTManagerPresenter fastManagerPresenter, FastDatasetWrapper wrapper) {

        super(session, view, fastManagerPresenter);
        this.setWrapper(wrapper);
        // this.fastManagerPresenter = fastManagerPresenter;
    }

    public FastNonPointDatasetViewPresenterImpl(EmfSession session, FastDatasetView view, FastDatasetWrapper wrapper) {

        super(session, view);
        this.setWrapper(wrapper);
    }

    public void doDisplay() {

        this.getView().observe(this);
        this.getView().display(this.getWrapper());
    }

    public void doSave() {
        /*
         * no-op
         */
    }
}
