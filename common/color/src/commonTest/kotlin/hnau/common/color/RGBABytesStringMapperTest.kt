package hnau.common.color

import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class RGBABytesStringMapperTest : FunSpec({
    val mapper = RGBABytes.getStringMapper(
        useAlpha = true,
    )
    listOf(
        "#12345678" to null,
        "#123456" to null,
        "#123456ff" to "#123456",
        "#aabbccdd" to "#ABCD",
    ).forEach { (source, resultOrNull) ->
        val result = resultOrNull ?: source
        val decoded = mapper.direct(source)
        val encoded = mapper.reverse(decoded)
        encoded.shouldBe(result)
    }
    listOf(
        "",
        "aaa",
        "#qqq",
        "#12345",
    ).forEach { source ->
        shouldThrowAny { mapper.direct(source) }
    }
    val mapperWithoutAlpha = RGBABytes.getStringMapper(
        useAlpha = false,
    )
    listOf(
        "#FFFF",
        "#FFFFAAAA",
    ).forEach { source ->
        shouldThrowAny { mapperWithoutAlpha.direct(source) }
    }
})