package gov.epa.emissions.framework.services.basic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import gov.epa.emissions.commons.io.importer.FilePatternMatcher;
import gov.epa.emissions.commons.io.importer.ImporterException;

public class EmfFilePatternMatcher extends FilePatternMatcher {

    public EmfFilePatternMatcher(File folder, String filePattern) throws ImporterException {
        super(folder, filePattern);
        // NOTE Auto-generated constructor stub
    }

    public EmfFileInfo[] getMatched(EmfFileInfo[] files) {
        List<EmfFileInfo> matchingFiles = new ArrayList<EmfFileInfo>();

        for (int i = 0; i < files.length;i++) {
            Matcher m = pattern.matcher(files[i].getName());
            if (m.matches()) {
                matchingFiles.add(files[i]);
            }
        }
        return matchingFiles.toArray(new EmfFileInfo[0]);
    }
}
