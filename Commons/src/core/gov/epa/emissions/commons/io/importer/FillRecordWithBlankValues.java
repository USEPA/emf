package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.io.FileFormatWithOptionalCols;

import java.util.List;

public class FillRecordWithBlankValues implements FillDefaultValues {

    public void fill(FileFormatWithOptionalCols base, List data, long datasetId) {
        data.add(0, datasetId + "");
        addComments(base, data);
        addDefaultsForOptionalCols(base, data);
    }

    private void addComments(FileFormatWithOptionalCols base, List data) {
        if (size(base) == data.size())// includes comments
            return;

        String last = (String) data.get(data.size() - 1);
        if (!isComments(last)) 
            data.add(data.size(), "");// empty comment
    }

    private int size(FileFormatWithOptionalCols base) {
        return 1 + base.cols().length + 1;
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
        return 1 + base.minCols().length + optionalCount;// 1 - dataset id
    }

    private int toAdd(FileFormatWithOptionalCols base, int optionalCount) {
        return base.optionalCols().length - optionalCount;
    }

    private int optionalCount(FileFormatWithOptionalCols base, List data) {
        return data.size() - 1 - base.minCols().length - 1;// 1 - dataset id, 1 - comments
    }
}
