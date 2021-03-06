package org.github.gestalt.config.node;

import org.github.gestalt.config.entity.ConfigNodeContainer;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.post.process.PostProcessor;
import org.github.gestalt.config.token.ArrayToken;
import org.github.gestalt.config.token.ObjectToken;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.CollectionUtils;
import org.github.gestalt.config.utils.PathUtil;
import org.github.gestalt.config.utils.ValidateOf;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Holds and manages config nodes.
 *
 * @author Colin Redmond
 */
public class ConfigNodeManager implements ConfigNodeService {
    private final List<ConfigNodeContainer> configNodes = new ArrayList<>();
    private ConfigNode root;

    /**
     * Default constructor for the ConfigNodeManager.
     */
    public ConfigNodeManager() {
    }

    @Override
    public ValidateOf<ConfigNode> addNode(ConfigNodeContainer newNode) throws GestaltException {
        if (newNode == null) {
            throw new GestaltException("No node provided");
        }
        List<ValidationError> errors = new ArrayList<>();

        configNodes.add(newNode);

        if (root == null) {
            root = newNode.getConfigNode();
        } else {
            ValidateOf<ConfigNode> mergedNode = mergeNodes("", root, newNode.getConfigNode());

            if (mergedNode.hasResults()) {
                root = mergedNode.results();
            }

            errors.addAll(mergedNode.getErrors());
        }

        errors.addAll(validateNode(root));
        errors = errors.stream().filter(CollectionUtils.distinctBy(ValidationError::description)).collect(Collectors.toList());

        return ValidateOf.validateOf(root, errors);
    }

    @Override
    public ValidateOf<ConfigNode> postProcess(List<PostProcessor> postProcessors) throws GestaltException {
        if (postProcessors == null) {
            throw new GestaltException("No postProcessors provided");
        }

        if (postProcessors.isEmpty()) {
            return ValidateOf.valid(root);
        }

        ValidateOf<ConfigNode> results = postProcess("", root, postProcessors);

        // If we have results we want to update the root to the new post processed config tree.
        if (results.hasResults()) {
            root = results.results();
        }

        return results;
    }

    private ValidateOf<ConfigNode> postProcess(String path, ConfigNode node, List<PostProcessor> postProcessors) throws GestaltException {
        ConfigNode currentNode = node;
        List<ValidationError> errors = new ArrayList<>();

        // apply the post processors to the node.
        // If there are multiple post processors, apply them in order, each post processor operates on the result node from the
        // last post processor.
        for (PostProcessor it : postProcessors) {
            ValidateOf<ConfigNode> processedNode = it.process(path, currentNode);

            errors.addAll(processedNode.getErrors());
            if (processedNode.hasResults()) {
                currentNode = processedNode.results();
            } else {
                errors.add(new ValidationError.NoResultsFoundForNode(path, node.getClass(), "post processing"));
            }
        }

        // recursively apply post processing to children nodes. If this is a leaf, we can return.
        if (currentNode instanceof ArrayNode) {
            return postProcessArray(path, (ArrayNode) currentNode, postProcessors);
        } else if (currentNode instanceof MapNode) {
            return postProcessMap(path, (MapNode) currentNode, postProcessors);
        } else if (currentNode instanceof LeafNode) {
            return ValidateOf.validateOf(currentNode, errors);
        } else {
            return ValidateOf.inValid(new ValidationError.UnknownNodeTypePostProcess(path, node.getClass().getName()));
        }
    }

    private ValidateOf<ConfigNode> postProcessArray(String path, ArrayNode node, List<PostProcessor> postProcessors)
        throws GestaltException {
        int size = node.size();
        List<ValidationError> errors = new ArrayList<>();
        ConfigNode[] processedNode = new ConfigNode[size];

        for (int i = 0; i < size; i++) {
            Optional<ConfigNode> currentNodeOption = node.getIndex(i);
            if (currentNodeOption.isPresent()) {
                String nextPath = PathUtil.pathForIndex(path, i);
                ValidateOf<ConfigNode> newNode = postProcess(nextPath, currentNodeOption.get(), postProcessors);

                errors.addAll(newNode.getErrors());
                if (newNode.hasResults()) {
                    processedNode[i] = newNode.results();
                } else {
                    errors.add(new ValidationError.NoResultsFoundForNode(path, ArrayNode.class, "post processing"));
                }
            }
        }

        return ValidateOf.validateOf(new ArrayNode(Arrays.asList(processedNode)), errors);
    }

    private ValidateOf<ConfigNode> postProcessMap(String path, MapNode node, List<PostProcessor> postProcessors) throws GestaltException {
        Map<String, ConfigNode> processedNode = new HashMap<>();
        List<ValidationError> errors = new ArrayList<>();

        for (Map.Entry<String, ConfigNode> entry : node.getMapNode().entrySet()) {
            String key = entry.getKey();
            String nextPath = PathUtil.pathForKey(path, key);
            ValidateOf<ConfigNode> newNode = postProcess(nextPath, entry.getValue(), postProcessors);

            errors.addAll(newNode.getErrors());
            if (newNode.hasResults()) {
                processedNode.put(key, newNode.results());
            } else {
                errors.add(new ValidationError.NoResultsFoundForNode(path, MapNode.class, "post processing"));
            }
        }

        return ValidateOf.validateOf(new MapNode(processedNode), errors);
    }


    @Override
    public ValidateOf<ConfigNode> reloadNode(ConfigNodeContainer reloadNode) throws GestaltException {
        ConfigNode newRoot = null;
        List<ValidationError> errors = new ArrayList<>();

        int index = 0;
        for (ConfigNodeContainer nodePair : configNodes) {

            ConfigNode currentNode = nodePair.getConfigNode();
            if (nodePair.getId().equals(reloadNode.getId())) {
                configNodes.set(index, reloadNode);
                currentNode = reloadNode.getConfigNode();
            }

            if (newRoot == null) {
                newRoot = currentNode;
            } else {
                ValidateOf<ConfigNode> mergedNode = mergeNodes("", newRoot, currentNode);

                errors.addAll(mergedNode.getErrors());
                if (mergedNode.hasResults()) {
                    newRoot = mergedNode.results();
                } else {
                    errors.add(new ValidationError.NoResultsFoundForNode("", "reload node"));
                }
            }
            index++;
        }

        errors.addAll(validateNode(newRoot));
        errors = errors.stream().filter(CollectionUtils.distinctBy(ValidationError::description)).collect(Collectors.toList());

        root = newRoot;
        return ValidateOf.validateOf(root, errors);
    }

    private List<ValidationError> validateNode(ConfigNode node) {
        return validateNode("", node);
    }

    private List<ValidationError> validateNode(String path, ConfigNode node) {
        if (node instanceof ArrayNode) {
            return validateArrayNode(path, (ArrayNode) node);
        } else if (node instanceof MapNode) {
            return validateMapNode(path, (MapNode) node);
        } else if (node instanceof LeafNode) {
            return validateLeafNode(path, (LeafNode) node);
        } else {
            return Collections.singletonList(new ValidationError.UnknownNodeType(path, node.getClass().getName()));
        }
    }

    private List<ValidationError> validateArrayNode(String path, ArrayNode node) {
        int size = node.size();
        List<ValidationError> errors = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            if (!node.getIndex(i).isPresent()) {
                errors.add(new ValidationError.ArrayMissingIndex(i, path));
            } else {
                String nextPath = PathUtil.pathForIndex(path, i);
                errors.addAll(validateNode(nextPath, node.getIndex(i).get()));
            }
        }
        return errors;
    }

    private List<ValidationError> validateMapNode(String path, MapNode node) {
        List<ValidationError> errors = new ArrayList<>();

        node.getMapNode().forEach((key, value) -> {
            if (key == null) {
                errors.add(new ValidationError.EmptyNodeNameProvided(path));
            } else if (value == null) {
                errors.add(new ValidationError.EmptyNodeValueProvided(path, key));
            } else {
                String nextPath = PathUtil.pathForKey(path, key);
                errors.addAll(validateNode(nextPath, value));
            }
        });

        return errors;
    }

    private List<ValidationError> validateLeafNode(String path, LeafNode node) {
        List<ValidationError> errors = new ArrayList<>();
        if (node == null) {
            errors.add(new ValidationError.LeafNodesIsNull(path));
        } else if (!node.getValue().isPresent()) {
            errors.add(new ValidationError.LeafNodesHaveNoValues(path));
        }

        return errors;
    }

    private ValidateOf<ConfigNode> mergeNodes(String path, ConfigNode node1, ConfigNode node2) {
        if (node1.getClass() != node2.getClass()) {
            return ValidateOf.inValid(
                new ValidationError.UnableToMergeDifferentNodes(node1.getClass(), node2.getClass()));
        } else {
            if (node1 instanceof ArrayNode) {
                return mergeArrayNodes(path, (ArrayNode) node1, (ArrayNode) node2);
            } else if (node1 instanceof MapNode) {
                return mergeMapNodes(path, (MapNode) node1, (MapNode) node2);
            } else if (node1 instanceof LeafNode) {
                return mergeLeafNodes(path, (LeafNode) node1, (LeafNode) node2);
            } else {
                return ValidateOf.inValid(new ValidationError.UnknownNodeType(path, node1.getClass().getName()));
            }
        }
    }

    private ValidateOf<ConfigNode> mergeArrayNodes(String path, ArrayNode arrayNode1, ArrayNode arrayNode2) {
        // get the maximum array size of both the nodes.
        int maxSize = Math.max(arrayNode1.size(), arrayNode2.size());
        ConfigNode[] values = new ConfigNode[maxSize];
        List<ValidationError> errors = new ArrayList<>();

        // loop though the array to the max size.
        // for each index check if exists in both, then merge the nodes.
        // if it only exists in one or the other, add which ever it exists in.
        // if it exists in neither add a validation error.
        for (int i = 0; i < maxSize; i++) {
            Optional<ConfigNode> array1AtIndex = arrayNode1.getIndex(i);
            Optional<ConfigNode> array2AtIndex = arrayNode2.getIndex(i);
            if (array1AtIndex.isPresent() && array2AtIndex.isPresent()) {
                String nextPath = PathUtil.pathForIndex(path, i);
                ValidateOf<ConfigNode> result = mergeNodes(nextPath, array1AtIndex.get(), array2AtIndex.get());

                // if there are errors, add them to the error list abd do not add the merge results
                errors.addAll(result.getErrors());
                if (result.hasResults()) {
                    values[i] = result.results();
                } else {
                    errors.add(new ValidationError.NoResultsFoundForNode(path, ArrayNode.class, "merging arrays"));
                }
            } else if (array1AtIndex.isPresent()) {
                values[i] = array1AtIndex.get();
            } else if (array2AtIndex.isPresent()) {
                values[i] = array2AtIndex.get();
            } else {
                errors.add(new ValidationError.ArrayMissingIndex(i, path));
            }
        }

        ArrayNode results = new ArrayNode(Arrays.asList(values));
        return ValidateOf.validateOf(results, errors);
    }

    private ValidateOf<ConfigNode> mergeMapNodes(String path, MapNode mapNode1, MapNode mapNode2) {
        Map<String, ConfigNode> mergedNode = new HashMap<>();
        List<ValidationError> errors = new ArrayList<>();

        // First we check all the nodes in mapNode1.
        // If the node also exists in mapNode2, it exists in both. So we need to merge them.
        // if it only exists in mapNode1 then we can add it to the merged Node, as it is not in mapNode2.
        for (Map.Entry<String, ConfigNode> entry : mapNode1.getMapNode().entrySet()) {
            String key = entry.getKey();
            if (key == null) {
                errors.add(new ValidationError.EmptyNodeNameProvided(path));
            } else if (entry.getValue() == null) {
                errors.add(new ValidationError.EmptyNodeValueProvided(path, key));
            } else if (mapNode2.getKey(key).isPresent()) {
                String nextPath = PathUtil.pathForKey(path, key);
                ValidateOf<ConfigNode> result = mergeNodes(nextPath, mapNode1.getKey(key).get(), mapNode2.getKey(key).get());

                // if there are errors, add them to the error list abd do not add the merge results
                errors.addAll(result.getErrors());
                if (result.hasResults()) {
                    mergedNode.putIfAbsent(key, result.results());
                } else {
                    errors.add(new ValidationError.NoResultsFoundForNode(path, MapNode.class, "merging maps"));
                }
            } else {
                mergedNode.putIfAbsent(key, entry.getValue());
            }
        }

        // Do a pass on mapNode2 and add any nodes that were not a intersection with mapNode1.
        // this is simply adding all nodes in mapNode2 using putIfAbsent, so they will only be added if they were missing.
        for (Map.Entry<String, ConfigNode> entry : mapNode2.getMapNode().entrySet()) {
            String key = entry.getKey();
            if (key == null) {
                errors.add(new ValidationError.EmptyNodeNameProvided(path));
            } else if (entry.getValue() == null) {
                errors.add(new ValidationError.EmptyNodeValueProvided(path, key));
            } else {
                mergedNode.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }

        return ValidateOf.validateOf(new MapNode(mergedNode), errors);
    }

    private ValidateOf<ConfigNode> mergeLeafNodes(String path, LeafNode node1, LeafNode node2) {
        if (node2.getValue().isPresent()) {
            return ValidateOf.valid(node2);
        } else if (node1.getValue().isPresent()) {
            return ValidateOf.valid(node1);
        } else {
            return ValidateOf.inValid(new ValidationError.LeafNodesHaveNoValues(path));
        }
    }

    @Override
    public ValidateOf<ConfigNode> navigateToNode(String path, List<Token> tokens) {
        ConfigNode currentNode = root;
        List<ValidationError> errors = new ArrayList<>();

        for (Token token : tokens) {
            ValidateOf<ConfigNode> result = navigateToNextNode(path, token, currentNode);

            // if there are errors, add them to the error list abd do not add the merge results
            if (result.hasErrors()) {
                return ValidateOf.inValid(result.getErrors());
            }

            if (result.hasResults()) {
                currentNode = result.results();
            } else {
                errors.add(new ValidationError.NoResultsFoundForNode(path, MapNode.class, "navigating to node"));
            }
        }

        return ValidateOf.validateOf(currentNode, errors);
    }

    @Override
    public ValidateOf<ConfigNode> navigateToNextNode(String path, Token token, ConfigNode currentNode) {

        ConfigNode node = currentNode;

        if (node == null) {
            return ValidateOf.inValid(new ValidationError.NullNodeForPath(path));
        } else if (token == null) {
            return ValidateOf.inValid(new ValidationError.NullTokenForPath(path));
        } else if (token instanceof ArrayToken) {
            if (node instanceof ArrayNode) {
                Optional<ConfigNode> nextNode = node.getIndex(((ArrayToken) token).getIndex());
                if (nextNode.isPresent()) {
                    node = nextNode.get();
                } else {
                    return ValidateOf.inValid(new ValidationError.NoResultsFoundForNode(path, token.getClass(), "navigating to next node"));
                }
            } else {
                return ValidateOf.inValid(
                    new ValidationError.MismatchedObjectNodeForPath(path, ArrayNode.class, node.getClass()));
            }
        } else if (token instanceof ObjectToken) {
            if (node instanceof MapNode) {
                Optional<ConfigNode> nextNode = node.getKey(((ObjectToken) token).getName());
                if (nextNode.isPresent()) {
                    node = nextNode.get();
                } else {
                    return ValidateOf.inValid(new ValidationError.NoResultsFoundForNode(path, token.getClass(), "navigating to next node"));
                }
            } else {
                return ValidateOf.inValid(new ValidationError.MismatchedObjectNodeForPath(path, MapNode.class, node.getClass()));
            }
        } else {
            return ValidateOf.inValid(new ValidationError.UnsupportedTokenType(path, token));
        }

        return ValidateOf.valid(node);
    }
}
