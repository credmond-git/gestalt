package org.github.gestalt.config.post.process;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.utils.ValidateOf;

/**
 * Interface for the Post Processing of Config nodes. This will be run against every node in the tree.
 *
 * @author Colin Redmond
 */
public interface PostProcessor {

    /**
     * run the post process the current node.
     *
     * @param path current path
     * @param currentNode current node to post process.
     * @return the node after running though the post processor.
     * @throws GestaltException any exceptions
     */
    ValidateOf<ConfigNode> process(String path, ConfigNode currentNode) throws GestaltException;

    /**
     * Apply the PostProcessorConfig to the Post Processor. Needed when building via the ServiceLoader
     * It is a default method as most Post Processor don't need to apply configs.
     *
     * @param config GestaltConfig to update the Post Processor
     */
    default void applyConfig(PostProcessorConfig config) {
    }
}
