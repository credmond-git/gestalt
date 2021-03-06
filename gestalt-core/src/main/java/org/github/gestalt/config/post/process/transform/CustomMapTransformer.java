package org.github.gestalt.config.post.process.transform;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.utils.ValidateOf;

import java.util.HashMap;
import java.util.Map;

/**
 * Allows you to provide a custom map to inject into leaf values that match ${map:key}, where the key is used to lookup into the map.
 *
 * @author Colin Redmond
 */
public class CustomMapTransformer implements Transformer {
    private final Map<String, String> replacementVars;

    /**
     * Default CustomMapTransformer that will not replace any values as it will have an empty map.
     */
    public CustomMapTransformer() {
        this.replacementVars = new HashMap<>();
    }

    /**
     * CustomMapTransformer that will replace any values in the map while processing.
     *
     * @param replacementVars values to replace
     */
    public CustomMapTransformer(Map<String, String> replacementVars) {
        this.replacementVars = replacementVars;
    }

    @Override
    public String name() {
        return "map";
    }

    @Override
    public ValidateOf<String> process(String path, String key) {
        if (!replacementVars.containsKey(key)) {
            return ValidateOf.inValid(new ValidationError.NoCustomPropertyFoundPostProcess(path, key));
        } else {
            return ValidateOf.valid(replacementVars.get(key));
        }
    }
}
