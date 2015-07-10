package com.daggerok.web.services;

import com.daggerok.web.manager.ContentRepository;
import com.google.common.io.ByteStreams;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.io.*;
import java.nio.file.Paths;
import java.util.logging.Logger;

@Stateless
@Path("files")
public class ContentRepositoryServiceBean implements ContentRepositoryService {
    private static final Logger logger = Logger.getLogger(ContentRepositoryService.class.getName());

    @Inject
    private ContentRepository repository;

    @Override
    @GET // jcr/api/files
    public String saveAndReadStaticContent() {
        String location = "/path/to/location/";
        String filename = "some.filename";

        try {
            byte[] data = ByteStreams.toByteArray(new FileInputStream(Paths.get(location, filename).toString()));
            // save:
            repository.save(location, filename, data);
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
        // exists: contentRepository.exists(location, filename);
        // delete: contentRepository.delete(location, filename);
        // read:
        byte[] result = repository.read(location, filename);
        return new String(result);
    }
}
