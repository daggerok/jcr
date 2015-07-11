package com.daggerok.web.manager;

import com.google.common.io.ByteStreams;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.jcr.*;
import java.io.*;
import java.nio.file.Paths;
import java.util.logging.Logger;

import static java.util.logging.Level.*;

@Singleton
@Lock(LockType.READ)
public class ContentRepositoryBean implements ContentRepository {
    private static final Logger logger = Logger.getLogger(ContentRepository.class.getName());

    @Resource(name = "java:/jca/app/repository")
    private Repository repository = null;

    private Credentials credentials = null;

    private String location = "/virtual/path/to/repository";

    private Session session = null;

    private String adminId = "jcr";

    private char[] pass = "jcr".toCharArray();

    private String workspace = "jcr";

    private String binaryType = "jcr:data";

    @PostConstruct
    private void postConstruct() {
        credentials = new SimpleCredentials(adminId, pass);
    }

    @Override
    public void saveOrUpdate(String filename, byte[] data) {
        String identifier = getIdentifier(filename);

        logger.log(INFO, "Save file identifier {0} ({1} bytes)", new Object[] { identifier, data.length });
        try {
            session = login();

            Node folder = session.getRootNode();
            Node file = contains(identifier) ? folder.getNode(identifier) : folder.addNode(identifier);
            Binary binary = session.getValueFactory().createBinary(new ByteArrayInputStream(data));

            file.setProperty(binaryType, binary);
            commit();
            binary.dispose();
        } catch (RepositoryException exception) {
            logger.log(SEVERE, "Could not save file {0}.", filename);
            throw new RuntimeException("Could not save file.", exception);
        } finally {
            logout();
        }
    }

    @Override
    public boolean exists(String filename) {
        String identifier = getIdentifier(filename);

        logger.log(INFO, "Check existence for file identifier {0}.", identifier);
        try {
            session = login();
            return contains(identifier);
        } catch (RepositoryException exception) {
            logger.log(SEVERE, "Could not check file {0}.", filename);
            throw new RuntimeException("Could not check file.", exception);
        } finally {
            logout();
        }
    }

    @Override
    public byte[] read(String filename) {
        String identifier = getIdentifier(filename);

        logger.log(INFO, "Read file identifier {0}.", identifier);
        try {
            session = login();

            if (!contains(identifier)) {
                throw new RuntimeException(String.format("File %s wasn't found.", filename));
            }

            try (InputStream inputStream = session.getRootNode().getNode(identifier).getProperty(binaryType)
                    .getBinary().getStream()) {
                return ByteStreams.toByteArray(inputStream);
            }
        } catch (RepositoryException | IOException exception) {
            logger.log(SEVERE, "Could not read file {0}.", filename);
            throw new RuntimeException("Could not read file.", exception);
        } finally {
            logout();
        }
    }

    @Override
    public boolean delete(String filename) {
        String identifier = getIdentifier(filename);

        logger.log(INFO, "Delete file identifier {0}.", identifier);
        try {
            session = login();

            if (!contains(identifier)) {
                return false; // file wasn't found.
            }

            session.getRootNode().getNode(identifier).remove();
            commit();
        } catch (RepositoryException exception) {
            logger.log(SEVERE, "Could not read file {0}.", filename);
            throw new RuntimeException("Could not read file.", exception);
        } finally {
            logout();
        }
        return true; // file successfully removed.
    }

    private Session login() throws RepositoryException {
        return repository.login(credentials, workspace);
    }

    private String getIdentifier(String filename) {
        // replace not valid characters for node relPath:
        return Paths.get(location, filename).toString().replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }

    private boolean contains(String identifier) throws RepositoryException {
        return session.getRootNode().hasNode(identifier);
    }

    private void commit() { session.save(); }

    private void logout() {
        if (null != session && session.isLive()) {
            session.logout();
        }
    }
}
