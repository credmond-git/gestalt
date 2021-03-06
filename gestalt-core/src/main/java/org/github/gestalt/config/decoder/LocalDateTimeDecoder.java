package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.utils.ValidateOf;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Decode a LocalDateTime.
 *
 * @author Colin Redmond
 */
public class LocalDateTimeDecoder extends LeafDecoder<LocalDateTime> {

    private final DateTimeFormatter formatter;

    /**
     * Default local date time decoder using ISO_DATE_TIME.
     */
    public LocalDateTimeDecoder() {
        this.formatter = DateTimeFormatter.ISO_DATE_TIME;
    }

    /**
     * Local Date time decode that takes a formatter.
     *
     * @param formatter DateTimeFormatter pattern
     */
    public LocalDateTimeDecoder(String formatter) {
        if (formatter != null && !formatter.isEmpty()) {
            this.formatter = DateTimeFormatter.ofPattern(formatter);
        } else {
            this.formatter = DateTimeFormatter.ISO_DATE_TIME;
        }
    }

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "LocalDateTime";
    }

    @Override
    public boolean matches(TypeCapture<?> klass) {
        return klass.isAssignableFrom(LocalDateTime.class);
    }

    @Override
    protected ValidateOf<LocalDateTime> leafDecode(String path, ConfigNode node) {
        ValidateOf<LocalDateTime> results;

        String value = node.getValue().orElse("");
        try {
            results = ValidateOf.valid(LocalDateTime.parse(value, formatter));
        } catch (DateTimeParseException e) {
            results = ValidateOf.inValid(new ValidationError.ErrorDecodingException(path, node, name()));
        }
        return results;
    }
}
