package org.config.gestalt.decoder;

import org.config.gestalt.exceptions.GestaltException;
import org.config.gestalt.node.ArrayNode;
import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.node.LeafNode;
import org.config.gestalt.node.MapNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SetDecoderTest {
    DoubleDecoder doubleDecoder = new DoubleDecoder();
    StringDecoder stringDecoder = new StringDecoder();
    ListDecoder listDecoder = new ListDecoder();
    DecoderRegistry decoderService = new DecoderRegistry(Arrays.asList(doubleDecoder, stringDecoder, listDecoder));

    SetDecoderTest() throws GestaltException {
    }

    @Test
    void name() {
        SetDecoder decoder = new SetDecoder();
        Assertions.assertEquals("Set", decoder.name());
    }

    @Test
    void matches() {
        SetDecoder decoder = new SetDecoder();
        Assertions.assertFalse(decoder.matches(TypeCapture.of(String.class)));
        Assertions.assertFalse(decoder.matches(new TypeCapture<String>() {
        }));

        Assertions.assertFalse(decoder.matches(TypeCapture.of(Long.class)));
        Assertions.assertFalse(decoder.matches(new TypeCapture<Long>() {
        }));

        Assertions.assertFalse(decoder.matches(TypeCapture.of(List.class)));
        Assertions.assertFalse(decoder.matches(new TypeCapture<List<String>>() {
        }));

        Assertions.assertFalse(decoder.matches(TypeCapture.of(Set.class)));
        Assertions.assertTrue(decoder.matches(new TypeCapture<Set<String>>() {
        }));
    }

    @Test
    void arrayDecodeStrings() {

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Steve");
        arrayNode[2] = new LeafNode("Matt");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        SetDecoder decoder = new SetDecoder();

        ValidateOf<Set<String>> values = decoder.decode("", nodes, new TypeCapture<Set<String>>() {
        }, decoderService);

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(3, values.results().size());
        assertThat(values.results())
            .contains("John")
            .contains("Steve")
            .contains("Matt");
    }

    @Test
    void arrayDecodeDoubles() {

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new LeafNode("0.1111");
        arrayNode[1] = new LeafNode("0.222");
        arrayNode[2] = new LeafNode("0.33");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        SetDecoder decoder = new SetDecoder();

        ValidateOf<Set<Double>> values = decoder.decode("", nodes, new TypeCapture<Set<Double>>() {
        }, decoderService);

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(3, values.results().size());

        assertThat(values.results())
            .contains(0.1111)
            .contains(0.222)
            .contains(0.33);
    }

    @Test
    void arrayDecodeDoublesMissingIndex() {

        ConfigNode[] arrayNode = new ConfigNode[4];
        arrayNode[0] = new LeafNode("0.1111");
        arrayNode[1] = new LeafNode("0.222");
        arrayNode[3] = new LeafNode("0.33");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        SetDecoder decoder = new SetDecoder();

        ValidateOf<Set<Double>> values = decoder.decode("db.hosts", nodes, new TypeCapture<Set<Double>>() {
        }, decoderService);

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertTrue(values.hasResults());
        Assertions.assertEquals(3, values.results().size());
        Assertions.assertEquals(1, values.getErrors().size());
        Assertions.assertEquals("Missing array index: 2 for path: db.hosts", values.getErrors().get(0).description());

        assertThat(values.results())
            .contains(0.1111)
            .contains(0.222)
            .contains(0.33);
    }

    @Test
    void arrayDecodeLeaf() {
        SetDecoder decoder = new SetDecoder();

        ValidateOf<Set<Double>> values = decoder.decode("db.hosts", new LeafNode("0.1111, 0.22"), new TypeCapture<Set<Double>>() {
        }, decoderService);

        Assertions.assertFalse(values.hasErrors());
        Assertions.assertTrue(values.hasResults());

        Assertions.assertEquals(2, values.results().size());
        assertThat(values.results())
            .contains(0.1111)
            .contains(0.22);
    }

    @Test
    void arrayDecodeNullLeaf() {
        SetDecoder decoder = new SetDecoder();

        ValidateOf<Set<Double>> values = decoder.decode("", new LeafNode(null), new TypeCapture<Set<Double>>() {
        }, decoderService);

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertFalse(values.hasResults());

        Assertions.assertEquals(1, values.getErrors().size());
        Assertions.assertEquals("Leaf on path: , missing value, LeafNode{value='null'} attempting to decode Set",
            values.getErrors().get(0).description());
    }

    @Test
    void arrayDecodeWrongTypeDoubles() {

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("Matt");
        arrayNode[2] = new LeafNode("Tom");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        SetDecoder decoder = new SetDecoder();

        ValidateOf<Set<Double>> values = decoder.decode("db.hosts", nodes, new TypeCapture<Set<Double>>() {
        }, decoderService);

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertFalse(values.hasResults());

        Assertions.assertEquals(3, values.getErrors().size());
        Assertions.assertEquals("Unable to parse a number on Path: db.hosts[0], from node: LeafNode{value='John'} " +
                "attempting to decode Double",
            values.getErrors().get(0).description());
        Assertions.assertEquals("Unable to parse a number on Path: db.hosts[1], from node: LeafNode{value='Matt'} " +
                "attempting to decode Double",
            values.getErrors().get(1).description());
        Assertions.assertEquals("Unable to parse a number on Path: db.hosts[2], from node: LeafNode{value='Tom'} " +
                "attempting to decode Double",
            values.getErrors().get(2).description());
    }

    @Test
    void arrayDecodeMixedWrongTypeDoubles() {

        ConfigNode[] arrayNode = new ConfigNode[3];
        arrayNode[0] = new LeafNode("John");
        arrayNode[1] = new LeafNode("0.22");
        arrayNode[2] = new LeafNode("Tom");

        ConfigNode nodes = new ArrayNode(Arrays.asList(arrayNode));
        SetDecoder decoder = new SetDecoder();

        ValidateOf<Set<Double>> values = decoder.decode("db.hosts", nodes, new TypeCapture<Set<Double>>() {
        }, decoderService);

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertTrue(values.hasResults());

        Assertions.assertEquals(2, values.getErrors().size());
        Assertions.assertEquals("Unable to parse a number on Path: db.hosts[0], from node: LeafNode{value='John'} " +
                "attempting to decode Double",
            values.getErrors().get(0).description());
        Assertions.assertEquals("Unable to parse a number on Path: db.hosts[2], from node: LeafNode{value='Tom'} " +
                "attempting to decode Double",
            values.getErrors().get(1).description());

        assertThat(values.results())
            .contains(0.22);
    }

    @Test
    void arrayDecodeMapNode() {
        SetDecoder decoder = new SetDecoder();

        ValidateOf<Set<Double>> values = decoder.decode("db.hosts", new MapNode(new HashMap<>()), new TypeCapture<Set<Double>>() {
        }, decoderService);

        Assertions.assertTrue(values.hasErrors());
        Assertions.assertFalse(values.hasResults());

        Assertions.assertEquals(1, values.getErrors().size());
        Assertions.assertEquals("Expected a Array  on path: db.hosts, received node type, received: " +
                "MapNode{mapNode={}} attempting to decode Set",
            values.getErrors().get(0).description());
    }
}
