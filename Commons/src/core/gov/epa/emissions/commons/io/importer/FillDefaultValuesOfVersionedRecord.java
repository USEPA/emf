package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.io.FileFormatWithOptionalCols;

import java.util.List;

public class FillDefaultValuesOfVersionedRecord implements FillDefaultValues {

    public void fill(FileFormatWithOptionalCols base, List data, long datasetId) {
        addVersionData(data, datasetId, 0);
        addComments(base, data);
        addDefaultsForOptionalCols(base, data);
    }

    private void addVersionData(List data, long datasetId, int version) {
        data.add(0, "");// record id
        data.add(1, datasetId + "");
        data.add(2, version + "");// version
        data.add(3, "");// delete versions
    }

    private void addComments(FileFormatWithOptionalCols base, List data) {
        if (size(base) == data.size())// includes comments
            return;

        String last = (String) data.get(data.size() - 1);
        if (!isComments(last)) 
            data.add(data.size(), "");// empty comment
    }

    private int size(FileFormatWithOptionalCols base) {
        return versionColsCount() + base.cols().length + 1;
    }

    private int versionColsCount() {
        return 4;
    }

    private boolean isComments(String token) {
        return token != null && token.startsWith("!");
    }

    /**
     * pre-condition: dataset id and comments are filled in
     * 
     * @param base
     */
    private void addDefaultsForOptionalCols(FileFormatWithOptionalCols base, List data) {
        int optionalCount = optionalCount(base, data);
        int toAdd = toAdd(base, optionalCount);
        int insertAt = insertAt(base, optionalCount);

        for (int i = 0; i < toAdd; i++)
            data.add(insertAt + i, "");// fillers for missing optional cols
    }

    private int insertAt(FileFormatWithOptionalCols base, int optionalCount) {
        return versionColsCount() + base.minCols().length + optionalCount;
    }

    private int toAdd(FileFormatWithOptionalCols base, int optionalCount) {
        return base.optionalCols().length - optionalCount;
    }

    private int optionalCount(FileFormatWithOptionalCols base, List data) {
        return data.size() - versionColsCount() - base.minCols().length - 1;// 1 - comments
    }
}
