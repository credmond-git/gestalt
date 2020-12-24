package org.config.gestalt;

import org.config.gestalt.exceptions.GestaltException;
import org.config.gestalt.reflect.TypeCapture;

public interface Gestalt {
    void loadConfigs() throws GestaltException;

    <T> T getConfig(String path, Class<T> klass) throws GestaltException;

    <T> T getConfig(String path, TypeCapture<T> klass) throws GestaltException;

    <T> T getConfig(String path, Class<T> klass, T defaultVal);

    <T> T getConfig(String path, TypeCapture<T> klass, T defaultVal);
}
