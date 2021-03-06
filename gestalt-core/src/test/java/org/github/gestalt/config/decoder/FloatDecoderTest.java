package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

class FloatDecoderTest {

    ConfigNodeService configNodeService;
    SentenceLexer lexer;

    @BeforeEach
    void setup() {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
    }

    @Test
    void name() {
        FloatDecoder decoder = new FloatDecoder();
        Assertions.assertEquals("Float", decoder.name());
    }

    @Test
    void priority() {
        FloatDecoder decoder = new FloatDecoder();
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority());
    }

    @Test
    void matches() {
        FloatDecoder floatDecoder = new FloatDecoder();

        Assertions.assertTrue(floatDecoder.matches(TypeCapture.of(Float.class)));
        Assertions.assertTrue(floatDecoder.matches(new TypeCapture<Float>() {
        }));
        Assertions.assertTrue(floatDecoder.matches(TypeCapture.of(float.class)));

        Assertions.assertFalse(floatDecoder.matches(TypeCapture.of(String.class)));
        Assertions.assertFalse(floatDecoder.matches(TypeCapture.of(Integer.class)));
        Assertions.assertFalse(floatDecoder.matches(new TypeCapture<List<Float>>() {
        }));
    }

    @Test
    void decodeFloat() throws GestaltException {
        FloatDecoder floatDecoder = new FloatDecoder();

        ValidateOf<Float> validate = floatDecoder.decode("db.timeout", new LeafNode("124.5"), TypeCapture.of(Float.class),
            new DecoderRegistry(Collections.singletonList(floatDecoder), configNodeService, lexer));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());
        Assertions.assertEquals(124.5f, validate.results());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void decodeFloat2() throws GestaltException {
        FloatDecoder floatDecoder = new FloatDecoder();

        ValidateOf<Float> validate = floatDecoder.decode("db.timeout", new LeafNode("124"), TypeCapture.of(Float.class),
            new DecoderRegistry(Collections.singletonList(floatDecoder), configNodeService, lexer));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());
        Assertions.assertEquals(124, validate.results());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void notAFloat() throws GestaltException {
        FloatDecoder floatDecoder = new FloatDecoder();

        ValidateOf<Float> validate = floatDecoder.decode("db.timeout", new LeafNode("12s4"), TypeCapture.of(Float.class),
            new DecoderRegistry(Collections.singletonList(floatDecoder), configNodeService, lexer));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.ERROR, validate.getErrors().get(0).level());
        Assertions.assertEquals("Unable to parse a number on Path: db.timeout, from node: LeafNode{value='12s4'} " +
                "attempting to decode Float",
            validate.getErrors().get(0).description());
    }
}
