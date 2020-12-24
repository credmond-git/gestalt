package org.config.gestalt.decoder;

import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.ValidateOf;

public class BooleanDecoder extends LeafDecoder {

    @Override
    public String name() {
        return "Boolean";
    }

    @Override
    public boolean matches(TypeCapture<?> klass) {
        return klass.isAssignableFrom(Boolean.class) || klass.isAssignableFrom(boolean.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected ValidateOf<Boolean> leafDecode(String path, ConfigNode node) {
        String value = node.getValue().orElse("");
        return ValidateOf.valid(Boolean.parseBoolean(value));
    }
}
