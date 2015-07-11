package com.daggerok.web.manager;

public interface ContentRepository {
    void saveOrUpdate(String filename, byte[] data);

    boolean exists(String filename);

    byte[] read(String filename);

    boolean delete(String filename);
}
