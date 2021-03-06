package org.github.gestalt.config.post.process.transform;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.utils.ValidateOf;

/**
 * Allows you to inject System Properties into leaf values that match ${envVar:key},
 * where the key is used to lookup into the Environment Variables.
 *
 * @author Colin Redmond
 */
public class SystemPropertiesTransformer implements Transformer {
    @Override
    public String name() {
        return "sys";
    }

    @Override
    public ValidateOf<String> process(String path, String key) {
        if (!System.getProperties().containsKey(key)) {
            return ValidateOf.inValid(new ValidationError.NoSystemPropertyFoundPostProcess(path, key));
        } else {
            return ValidateOf.valid(System.getProperties().get(key).toString());
        }
    }
}
