package org.github.gestalt.config.kotlin.decoder

import org.github.gestalt.config.decoder.LeafDecoder
import org.github.gestalt.config.decoder.Priority
import org.github.gestalt.config.entity.ValidationError
import org.github.gestalt.config.kotlin.reflect.KTypeCapture
import org.github.gestalt.config.node.ConfigNode
import org.github.gestalt.config.reflect.TypeCapture
import org.github.gestalt.config.utils.StringUtils
import org.github.gestalt.config.utils.ValidateOf

/**
 * Kotlin Double Decoder.
 *
 * @author Colin Redmond
 */
class DoubleDecoder : LeafDecoder<Double>() {
    override fun name(): String {
        return "kDouble"
    }

    override fun priority(): Priority {
        return Priority.HIGH
    }

    override fun matches(klass: TypeCapture<*>): Boolean {
        return if (klass is KTypeCapture<*>) {
            klass.isAssignableFrom(Double::class)
        } else {
            false
        }
    }

    override fun leafDecode(path: String?, node: ConfigNode): ValidateOf<Double> {
        val results: ValidateOf<Double>
        val value = node.value.orElse("")
        results = if (StringUtils.isReal(value)) {
            try {
                val longVal = value.toDouble()
                ValidateOf.valid(longVal)
            } catch (e: NumberFormatException) {
                ValidateOf.inValid(ValidationError.DecodingNumberFormatException(path, node, name()))
            }
        } else {
            ValidateOf.inValid(ValidationError.DecodingNumberParsing(path, node, name()))
        }
        return results
    }
}
