package org.config.gestalt.kotlin.decoder

import org.config.gestalt.decoder.DecoderRegistry
import org.config.gestalt.entity.ValidationLevel
import org.config.gestalt.exceptions.GestaltException
import org.config.gestalt.kotlin.reflect.kTypeCaptureOf
import org.config.gestalt.lexer.SentenceLexer
import org.config.gestalt.node.ConfigNodeService
import org.config.gestalt.node.LeafNode
import org.config.gestalt.reflect.TypeCapture
import org.config.gestalt.utils.ValidateOf
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.nio.charset.Charset
import java.util.*

internal class ByteDecoderTest {
    var configNodeService: ConfigNodeService? = null
    var lexer: SentenceLexer? = null

    @BeforeEach
    fun setup() {
        configNodeService = Mockito.mock(ConfigNodeService::class.java)
        lexer = Mockito.mock(SentenceLexer::class.java)
    }

    @Test
    fun name() {
        val decoder = ByteDecoder()
        Assertions.assertEquals("Byte", decoder.name())
    }

    @Test
    fun matches() {
        val decoder = ByteDecoder()
        Assertions.assertTrue(decoder.matches(kTypeCaptureOf<Byte>()))
        Assertions.assertFalse(decoder.matches(object : TypeCapture<Byte?>() {}))
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Byte::class.java)))
        Assertions.assertFalse(decoder.matches(kTypeCaptureOf<Int>()))
        Assertions.assertFalse(decoder.matches(kTypeCaptureOf<String>()))
        Assertions.assertFalse(decoder.matches(kTypeCaptureOf<Date>()))
        Assertions.assertFalse(decoder.matches(kTypeCaptureOf<List<Byte>>()))
    }

    @Test
    @Throws(GestaltException::class)
    fun decodeByte() {
        val decoder = ByteDecoder()
        val validate: ValidateOf<Byte> = decoder.decode(
            "db.port", LeafNode("a"), TypeCapture.of(
                Byte::class.java
            ),
            DecoderRegistry(listOf(decoder), configNodeService, lexer)
        )
        Assertions.assertTrue(validate.hasResults())
        Assertions.assertFalse(validate.hasErrors())
        Assertions.assertEquals("a".toByteArray(Charset.defaultCharset())[0], validate.results())
        Assertions.assertEquals(0, validate.errors.size)
    }

    @Test
    @Throws(GestaltException::class)
    fun notAByteTooLong() {
        val decoder = ByteDecoder()
        val validate: ValidateOf<Byte> = decoder.decode(
            "db.port", LeafNode("aaa"), TypeCapture.of(
                Byte::class.java
            ),
            DecoderRegistry(listOf(decoder), configNodeService, lexer)
        )
        Assertions.assertFalse(validate.hasResults())
        Assertions.assertTrue(validate.hasErrors())
        Assertions.assertNull(validate.results())
        Assertions.assertNotNull(validate.errors)
        Assertions.assertEquals(ValidationLevel.WARN, validate.errors[0].level())
        Assertions.assertEquals(
            "Expected a Byte on path: db.port, decoding node: LeafNode{value='aaa'} received the wrong size",
            validate.errors[0].description()
        )
    }

    @Test
    @Throws(GestaltException::class)
    fun notAByteTooShort() {
        val decoder = ByteDecoder()
        val validate: ValidateOf<Byte> = decoder.decode(
            "db.port", LeafNode(""), TypeCapture.of(
                Byte::class.java
            ),
            DecoderRegistry(listOf(decoder), configNodeService, lexer)
        )
        Assertions.assertFalse(validate.hasResults())
        Assertions.assertTrue(validate.hasErrors())
        Assertions.assertNull(validate.results())
        Assertions.assertNotNull(validate.errors)
        Assertions.assertEquals(ValidationLevel.WARN, validate.errors[0].level())
        Assertions.assertEquals(
            "Expected a Byte on path: db.port, decoding node: LeafNode{value=''} received the wrong size",
            validate.errors[0].description()
        )
    }
}
