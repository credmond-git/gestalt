package org.config.gestalt.decoder;

import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.ValidateOf;

/**
 * Interface for decoders so we can tell which classes this decoder supports and functionality to decode them.
 *
 * @param <T> the generic type of the decoder.
 * @author Colin Redmond
 */
public interface Decoder<T> {

    /**
     * Priority for the decoder. Allows us to sort encoders when we have multiple matches.
     *
     * @return Priority
     */
    Priority priority();

    /**
     * Name of the encoder
     * @return encoder name
     */
    String name();

    /**
     * true if this decoder matches the type capture
     * @param klass TypeCapture we are looking for a decoder.
     * @return true if this decoder matches the type capture
     */
    boolean matches(TypeCapture<?> klass);

    /**
     * Decode the current node. If the current node is a class or list we may need to decode sub nodes.
     *
     * @param path the current path
     * @param node the current node we are decoding.
     * @param type the type of object we are decoding.
     * @param decoderService decoder Service used to decode members if needed. Such as class fields.
     * @return ValidateOf the current node with details of either success or failures.
     */
    ValidateOf<T> decode(String path, ConfigNode node, TypeCapture<?> type, DecoderService decoderService);
}
