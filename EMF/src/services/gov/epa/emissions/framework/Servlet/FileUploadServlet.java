package gov.epa.emissions.framework.Servlet;

import gov.epa.emissions.framework.services.basic.EmfProperty;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.JpaEntityManagerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * A Java servlet that handles file upload from client.
 *
 * @author www.codejava.net
 */
public class FileUploadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // location to store file uploaded
    private static final String UPLOAD_DIRECTORY = "upload";

    // upload settings
    private static final int MEMORY_THRESHOLD   = 1024 * 1024 * 3;  // 3MB

    private EntityManagerFactory entityManagerFactory;
    private String tempDirectory;
    private Integer maxFileSize;
    private Integer maxRequestSize;

    public FileUploadServlet() {
        this.entityManagerFactory = JpaEntityManagerFactory.get();;
    }

    /**
     * Upon receiving file upload submission, parses the request to read
     * upload data and saves the file on disk.
     */
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {
        // checks if the request actually contains upload file
        if (!ServletFileUpload.isMultipartContent(request)) {
            // if not, we stop here
            PrintWriter writer = response.getWriter();
            writer.println("Error: Form must has enctype=multipart/form-data.");
            writer.flush();
            return;
        }

        // configures upload settings
        DiskFileItemFactory factory = new DiskFileItemFactory();
        // sets memory threshold - beyond which files are stored in disk
        factory.setSizeThreshold(MEMORY_THRESHOLD);
        // sets temporary location to store files
        factory.setRepository(new File(System.getProperty("java.io.tmpdir")));

        ServletFileUpload upload = new ServletFileUpload(factory);

        // sets maximum size of upload file
        upload.setFileSizeMax(getMaxFileSize());

        // sets maximum size of request (include file + form data)
        upload.setSizeMax(maxRequestSize);

        // constructs the directory path to store upload file
        // this path is relative to application's directory
//        String uploadPath = getServletContext().getRealPath("")
//                + File.separator + UPLOAD_DIRECTORY;
        String uploadPath = getTempDirectory()
                + File.separator + UPLOAD_DIRECTORY + File.separator + request.getHeader("User-Name");

        // creates the directory if it does not exist
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdir();
        }

        try {
            // parses the request's content to extract file data
            @SuppressWarnings("unchecked")
            List<FileItem> formItems = upload.parseRequest(request);

            if (formItems != null && formItems.size() > 0) {
                // iterates over form's fields
                for (FileItem item : formItems) {
                    // processes only fields that are not form fields
                    if (!item.isFormField()) {
                        String fileName = new File(item.getName()).getName();
                        String filePath = uploadPath + File.separator + fileName;
                        File storeFile = new File(filePath);

                        // saves the file on disk
                        item.write(storeFile);
                        request.setAttribute("message",
                                "Upload has been done successfully!");
                    }
                }
            }
        } catch (Exception ex) {
            request.setAttribute("message",
                    "There was an error: " + ex.getMessage());
            throw new IOException(ex);
        }
    }

    private String getTempDirectory() {
        if (tempDirectory == null) {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            try {
                EmfProperty eximTempDir = new EmfPropertiesDAO().getProperty("ImportExportTempDir", entityManager);

                if (eximTempDir != null) {
                    tempDirectory = eximTempDir.getValue();
                }
            } finally {
                entityManager.close();
            }
        }
        return tempDirectory;
    }

    private int getMaxFileSize() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            EmfProperty emfProperty = new EmfPropertiesDAO().getProperty(EmfProperty.MAX_FILE_UPLOAD_SIZE, entityManager);

            if (emfProperty != null) {
                maxFileSize = 1024 * 1024 * Integer.parseInt(emfProperty.getValue());
                maxRequestSize = 1024 * 1024 * (Integer.parseInt(emfProperty.getValue()) + 10);
            }
        } finally {
            entityManager.close();
        }
        return maxFileSize;
    }
//Integer.parseInt(entityManager.userService().getPropertyValue(EmfProperty.MAX_FILE_UPLOAD_SIZE));
}