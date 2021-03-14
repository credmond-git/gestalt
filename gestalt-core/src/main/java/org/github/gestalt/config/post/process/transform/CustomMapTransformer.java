package org.github.gestalt.config.post.process.transform;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CustomMapTransformer implements Transformer {
    private final Map<String, String> replacementVars;

    public CustomMapTransformer() {
        this.replacementVars = new HashMap<>();
    }

    public CustomMapTransformer(Map<String, String> replacementVars) {
        this.replacementVars = replacementVars;
    }

    @Override
    public String name() {
        return "map";
    }

    @Override
    public Optional<String> process(String path, String key) {
        if(!replacementVars.containsKey(key)) {
            return Optional.empty();
        } else {
            return Optional.of(replacementVars.get(key));
        }
    }
}
