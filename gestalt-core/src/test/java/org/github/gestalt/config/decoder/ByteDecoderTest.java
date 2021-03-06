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

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

class ByteDecoderTest {

    ConfigNodeService configNodeService;
    SentenceLexer lexer;

    @BeforeEach
    void setup() {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
    }

    @Test
    void name() {
        ByteDecoder decoder = new ByteDecoder();
        Assertions.assertEquals("Byte", decoder.name());
    }

    @Test
    void priority() {
        ByteDecoder decoder = new ByteDecoder();
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority());
    }

    @Test
    void matches() {
        ByteDecoder decoder = new ByteDecoder();

        Assertions.assertTrue(decoder.matches(TypeCapture.of(Byte.class)));
        Assertions.assertTrue(decoder.matches(new TypeCapture<Byte>() {
        }));
        Assertions.assertTrue(decoder.matches(TypeCapture.of(byte.class)));

        Assertions.assertFalse(decoder.matches(TypeCapture.of(Character.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Float.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(String.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Integer.class)));
        Assertions.assertFalse(decoder.matches(new TypeCapture<List<Float>>() {
        }));
    }

    @Test
    void decodeByte() throws GestaltException {
        ByteDecoder decoder = new ByteDecoder();

        ValidateOf<Byte> validate = decoder.decode("db.port", new LeafNode("a"), TypeCapture.of(Byte.class),
            new DecoderRegistry(Collections.singletonList(decoder), configNodeService, lexer));
        Assertions.assertTrue(validate.hasResults());
        Assertions.assertFalse(validate.hasErrors());
        Assertions.assertEquals("a".getBytes(Charset.defaultCharset())[0], validate.results());
        Assertions.assertEquals(0, validate.getErrors().size());
    }

    @Test
    void notAByteTooLong() throws GestaltException {
        ByteDecoder decoder = new ByteDecoder();

        ValidateOf<Byte> validate = decoder.decode("db.port", new LeafNode("aaa"), TypeCapture.of(Byte.class),
            new DecoderRegistry(Collections.singletonList(decoder), configNodeService, lexer));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.WARN, validate.getErrors().get(0).level());
        Assertions.assertEquals("Expected a Byte on path: db.port, decoding node: LeafNode{value='aaa'} received the wrong size",
            validate.getErrors().get(0).description());
    }

    @Test
    void notAByteTooShort() throws GestaltException {
        ByteDecoder decoder = new ByteDecoder();

        ValidateOf<Byte> validate = decoder.decode("db.port", new LeafNode(""), TypeCapture.of(Byte.class),
            new DecoderRegistry(Collections.singletonList(decoder), configNodeService, lexer));
        Assertions.assertFalse(validate.hasResults());
        Assertions.assertTrue(validate.hasErrors());
        Assertions.assertNull(validate.results());
        Assertions.assertNotNull(validate.getErrors());
        Assertions.assertEquals(ValidationLevel.WARN, validate.getErrors().get(0).level());
        Assertions.assertEquals("Expected a Byte on path: db.port, decoding node: LeafNode{value=''} received the wrong size",
            validate.getErrors().get(0).description());
    }
}
