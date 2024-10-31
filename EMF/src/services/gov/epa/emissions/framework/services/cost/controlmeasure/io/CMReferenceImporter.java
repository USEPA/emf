package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.data.Reference;
import gov.epa.emissions.commons.io.csv.CSVReader;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.cost.ReferencesDAO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public class CMReferenceImporter {

    private File file;

    private CMReferenceRecordReader referenceReader;

    private User user;

    private EntityManagerFactory entityManagerFactory;

    private ReferencesDAO referencesDAO;

    public CMReferenceImporter(File file, CMReferenceFileFormat fileFormat, User user,
            EntityManagerFactory entityManagerFactory) {

        this.file = file;
        this.user = user;
        this.entityManagerFactory = entityManagerFactory;
        this.referencesDAO = new ReferencesDAO();
        this.referenceReader = new CMReferenceRecordReader(fileFormat, user, entityManagerFactory);
    }

    public void run(Map<Integer, Reference> referenceMap, Map<Integer, Integer> idMap) throws ImporterException, FileNotFoundException {

        addStatus("Reading reference file...");

        Map<Integer, Reference> initialReferenceMap = new HashMap<Integer, Reference>();

        CSVReader reader = new CSVReader(new FileReader( file));
        //read the first header line...
        reader.read();
        for (Record record = reader.read(); reader.hasNext(); record = reader.read()) {
//        CMCSVFileReader reader = new CMCSVFileReader(file);
//        for (Record record = reader.read(); !record.isEnd(); record = reader.read()) {
            this.referenceReader.parse(initialReferenceMap, record, reader.lineNumber());
        }

        int errorCount = this.referenceReader.getErrorCount();
        if (errorCount > 0) {
            String message = "Failed to import reference records, " + errorCount + " errors were found.";
            addStatus(message);
            throw new ImporterException(message);
        }

        addStatus("Finished reading reference file");

        this.generateCorrectedReferenceMap(referenceMap, idMap, initialReferenceMap);
    }

    private void generateCorrectedReferenceMap(Map<Integer, Reference> referenceMap, Map<Integer, Integer> idMap,
            Map<Integer, Reference> initialReferenceMap) {

        addStatus("Correcting reference ids...");

        Set<Integer> keySet = initialReferenceMap.keySet();

        EntityManager entityManager = this.referencesDAO.getSession();
        List<Reference> existingReferences = this.referencesDAO.getReferences(entityManager);
        for (Integer id : keySet) {

            Reference newReference = initialReferenceMap.get(id);
            String description = newReference.getDescription();
            /*
             * check case where reference already exists
             */
            if (this.referencesDAO.descriptionUsed(description, entityManager)) {

                /*
                 * we know it exists, so find the right one
                 */
                for (Reference existingReference : existingReferences) {

                    if (existingReference.equals(newReference)) {

                        /*
                         * add the right one to the map
                         */
                        referenceMap.put(existingReference.getId(), existingReference);

                        /*
                         * save the mapping from the new reference id to the existing reference id
                         */
                        idMap.put(id, existingReference.getId());
                        break;
                    }
                }
            } else {

                /*
                 * it didn't already exist, so add it to the database
                 */
                this.referencesDAO.addReference(newReference, entityManager);

                /*
                 * use the generated id for the map
                 */
                referenceMap.put(newReference.getId(), newReference);

                /*
                 * save the mapping from the new reference id to the generated reference id
                 */
                idMap.put(id, newReference.getId());
            }
        }

        addStatus("Finished correcting reference ids");
    }

    private void addStatus(String message) {
        setStatus(message);
    }

    private void setStatus(String message) {

        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("CMImportDetailMsg");
        endStatus.setMessage(message + "\n");
        endStatus.setTimestamp(new Date());

        new StatusDAO(entityManagerFactory).add(endStatus);
    }

    public String getFileName() {

        String fileName = null;
        if (this.file != null) {
            try {
                fileName = this.file.getCanonicalPath();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return fileName;
    }
}
