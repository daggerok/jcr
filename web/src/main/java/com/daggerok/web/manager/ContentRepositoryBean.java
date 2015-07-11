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

    private static final String binaryType = "jcr:data";

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
            Node file = folder.hasNode(identifier) ? folder.getNode(identifier) : folder.addNode(identifier);
            Binary binary = session.getValueFactory().createBinary(new ByteArrayInputStream(data));

            file.setProperty(binaryType, binary);
            session.save();
            binary.dispose();
        } catch (RepositoryException exception) {
            logger.log(SEVERE, "Could not save file {0}.", filename);
            throw new RuntimeException("Could not save file.", exception);
        } finally {
            logout(session);
        }
    }

    @Override
    public boolean contains(String filename) {
        String identifier = getIdentifier(filename);

        logger.log(INFO, "Check existence of identifier {0}.", identifier);
        try {
            return contains(session = login(), identifier);
        } catch (RepositoryException exception) {
            logger.log(SEVERE, "Could not check file {0}.", filename);
            throw new RuntimeException("Could not check file.", exception);
        } finally {
            logout(session);
        }
    }

    @Override
    public byte[] read(String filename) {
        String identifier = getIdentifier(filename);

        logger.log(INFO, "Read file identifier {0}.", identifier);
        try {
            session = login();

            if (!contains(session, identifier)) {
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
            logout(session);
        }
    }

    @Override
    public boolean delete(String filename) {
        String identifier = getIdentifier(filename);

        logger.log(INFO, "Delete file identifier {0}.", identifier);
        try {
            session = login();

            if (!contains(session, identifier)) {
                return false;
            }

            session.getRootNode().getNode(identifier).remove();
            session.save();
        } catch (RepositoryException exception) {
            logger.log(SEVERE, "Could not read file {0}.", filename);
            throw new RuntimeException("Could not read file.", exception);
        } finally {
            logout(session);
        }
        return true;
    }

    private Session login() throws RepositoryException {
        return repository.login(credentials, workspace);
    }

    private String getIdentifier(String filename) {
        // replace not valid characters for node relPath:
        return Paths.get(location, filename).toString().replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }

    private boolean contains(Session session, String identifier) throws RepositoryException {
        return session.getRootNode().hasNode(identifier);
    }

    private void logout(Session session) {
        if (null != session && session.isLive()) {
            session.logout();
        }
    }
}
