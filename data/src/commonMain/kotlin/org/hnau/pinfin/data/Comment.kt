package org.hnau.pinfin.data

import kotlinx.serialization.Serializable
import org.hnau.commons.kotlin.mapper.Mapper
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class Comment(
    val text: String,
) {

    companion object {

        val stringMapper: Mapper<String, Comment> = Mapper(
            direct = ::Comment,
            reverse = Comment::text,
        )
    }
}