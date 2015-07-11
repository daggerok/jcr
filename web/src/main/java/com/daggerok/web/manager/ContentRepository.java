package com.daggerok.web.manager;

public interface ContentRepository {
    void save(String filename, byte[] data);

    boolean contains(String filename);

    byte[] read(String filename);

    boolean delete(String filename);
}
