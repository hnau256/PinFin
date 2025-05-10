@file:Suppress("JSON_FORMAT_REDUNDANT")

package hnau.generateicons

import arrow.core.NonEmptyList
import arrow.core.NonEmptySet
import arrow.core.toNonEmptyListOrNull
import arrow.core.toNonEmptySetOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStreamReader
import java.net.URL

@Serializable
data class Icons(
    val icons: List<Icon>,
)

@Serializable
data class Icon(
    val name: String,
    val popularity: Int,
    val categories: List<String>,
    val tags: List<String>,
)

fun main() {

    val iconNamesToExclude = setOf("medication_liquid")

    val iconsJson = URL("https://fonts.google.com/metadata/icons")
        .openStream()
        .let(::InputStreamReader)
        .use(InputStreamReader::readText)

    val icons = Json { ignoreUnknownKeys = true }
        .decodeFromString(Icons.serializer(), iconsJson.dropWhile { it != '{' })
        .icons
        .filter { it.name !in iconNamesToExclude }
        .sortedByDescending { it.popularity }
        .take(1024)

    val categories = icons.map(Icon::category).toSet()
    println(categories)

    val imports = icons
        .map { it.propertyName }
        .sorted()
        .joinToString(
            separator = "\n        ",
        ) { name ->
            "import androidx.compose.material.icons.filled.$name"
        }

    val entries = icons
        .sortedByDescending { it.popularity }
        .joinToString(
            separator = ",\n",
        ) { icon ->
            """
            IconInfo(
                key = Icon("${icon.name}"),
                name = "${icon.prettyName}",
                icon = Icons.Default.${icon.propertyName},
                tags = nonEmptySetOf(${icon.nonEmptyTags.joinToString { "\"$it\"" }}),
                category = Category.${icon.category},
                popularity = ${icon.popularity},
            )
        """.replaceIndent("                    ")
        }

    val content = """
        //This file is generated
        package hnau.pinfin.projector.utils

        import androidx.compose.material.icons.Icons
        $imports
        import androidx.compose.ui.graphics.vector.ImageVector
        import arrow.core.NonEmptySet
        import arrow.core.nonEmptySetOf
        import hnau.pinfin.data.Icon

        data class IconInfo(
            val key: Icon,
            val name: String,
            val icon: ImageVector,
            val tags: NonEmptySet<String>,
            val category: Category,
            val popularity: Int,
        ) {
    
            enum class Category {
                ${categories.joinToString(separator = ",\n                ")}
            }
            
            companion object {
                
                val list: List<IconInfo> = listOf(
$entries,
                )

                val byKey: Map<Icon, IconInfo> = 
                    list.associateBy(IconInfo::key)
           }
        }
    """.trimIndent()

    File("pinfin/projector/src/commonMain/kotlin/hnau/pinfin/projector/utils/IconInfo.kt").writeText(
        content
    )

    println(icons.size)
}

private val Icon.category: String
    get() = categories.single().uppercaseFirst

private val Icon.propertyName: String
    get() = name
        .split('_')
        .joinToString(
            separator = "",
        ) { word ->
            word
                .lowercase()
                .uppercaseFirst
        }
        .let { rawName ->
            when (rawName.first().isLetter()) {
                true -> rawName
                false -> "_$rawName"
            }
        }

private val Icon.prettyName: String
    get() = name
        .split('_')
        .mapIndexed { index, word ->
            word.lowercase().let { lowercased ->
                when (index) {
                    0 -> lowercased.uppercaseFirst
                    else -> lowercased
                }
            }
        }
        .joinToString(
            separator = " ",
        )

private val String.uppercaseFirst: String
    get() = replaceFirstChar(Char::uppercase)

private val Icon.nonEmptyTags: NonEmptySet<String>
    get() = run {
        val prepareList: (List<String>) -> NonEmptyList<String>? = {
            it.filter { it.length > 1 }.toNonEmptyListOrNull()
        }
        val nonEmptyTags = tags.map { it.split(' ').first() }.let(prepareList)
        val tagsOrNameParts = nonEmptyTags ?: name.split('_').let(prepareList)!!
        tagsOrNameParts
            .sorted()
            .map(String::lowercase)
            .toNonEmptySetOrNull()!!
    }