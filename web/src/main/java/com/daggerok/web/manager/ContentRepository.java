package com.daggerok.web.manager;

public interface ContentRepository {
    void save(String location, String filename, byte[] data);

    boolean contains(String location, String filename);

    byte[] read(String location, String filename);

    boolean delete(String location, String uid);
}
