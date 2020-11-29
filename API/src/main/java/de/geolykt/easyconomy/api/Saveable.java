package de.geolykt.easyconomy.api;

import java.io.IOException;

/**
 * An interface to mark that a given object can be saved to a file.
 * @author Geolykt
 * @since 1.1.0
 */
public interface Saveable {

    /**
     * Saves the object onto it's designated file.
     *   Implementations should be aware of thread safety.
     * @throws IOException If an IOExceptiion occurs
     * @since 1.1.0
     */
    public void save() throws IOException;
}
