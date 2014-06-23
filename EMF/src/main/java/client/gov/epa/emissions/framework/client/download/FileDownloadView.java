package gov.epa.emissions.framework.client.download;

import gov.epa.emissions.framework.client.EmfView;
import gov.epa.emissions.framework.services.basic.FileDownload;

public interface FileDownloadView extends EmfView {

    void update(FileDownload[] fileDownloads);

    void notifyError(String message);

    void observe(FileDownloadPresenter presenter);

    void clear();

}
