package org.github.gestalt.config.reload;

/**
 * Listener for core reload events. Core reload events are triggered when one or more of the config reloads.
 *
 * @author Colin Redmond
 */
public interface CoreReloadListener {
    /**
     * Called when the core configs have been reloaded.
     */
    void reload();
}
