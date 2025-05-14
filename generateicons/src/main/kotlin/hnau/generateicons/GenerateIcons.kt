@file:Suppress("JSON_FORMAT_REDUNDANT")

package hnau.generateicons

import arrow.core.NonEmptyList
import arrow.core.NonEmptySet
import arrow.core.toNonEmptyListOrNull
import arrow.core.toNonEmptySetOrNull
import hnau.common.ktgen.Importable
import hnau.common.ktgen.inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
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
        .take(256)

    val categories = icons.map(Icon::category).toSet()
    println(categories)

    val modelProject = ":pinfin:model"
    val modelSubpackage = "utils.icons"

    val categoryClassName = "IconCategory"

    createKtFile(
        projectName = modelProject,
        subpackage = modelSubpackage,
        name = categoryClassName,
    ) {
        +"enum class $categoryClassName {"
        indent {
            categories.forEach { category ->
                +"$category,"
            }
        }
        +"}"
    }

    val iconImportable = Importable("hnau.pinfin.data", "Icon")

    val iconInfoClassName = "IconInfo"

    createKtFile(
        projectName = modelProject,
        subpackage = modelSubpackage,
        name = iconInfoClassName,
    ) {
        +"enum class $iconInfoClassName("
        indent {
            +"val key: ${inject(iconImportable)},"
            +"val title: String,"
            +"val tags: ${inject("arrow.core", "NonEmptySet")}<String>,"
            +"val category: $categoryClassName,"
            +"val popularity: Int,"
        }
        +") {"
        indent {
            icons.forEach { icon ->
                +"${icon.propertyName}("
                indent {
                    +"key = Icon(\"${icon.name}\"),"
                    +"title = \"${icon.prettyName}\","
                    +"tags = ${
                        inject(
                            "arrow.core",
                            "nonEmptySetOf"
                        )
                    }(${icon.nonEmptyTags.joinToString { "\"$it\"" }}),"
                    +"category = $categoryClassName.${icon.category},"
                    +"popularity = ${icon.popularity},"
                }
                +"),"
            }
        }
        +"}"
    }

    val iconInfoImportable = Importable("hnau.pinfin.model.$modelSubpackage", iconInfoClassName)
    val imageVectorImportable = Importable("androidx.compose.ui.graphics.vector", "ImageVector")
    createKtFile(
        projectName = ":pinfin:projector",
        subpackage = "utils",
        name = "IconInfoExt",
    ) {
        val IconInfo = inject(iconInfoImportable)
        val ImageVector = inject(imageVectorImportable)
        val Icons = inject("androidx.compose.material.icons", "Icons")
        +"private val iconInfoImages: MutableMap<$IconInfo, $ImageVector> = mutableMapOf()"
        +""
        +"val $IconInfo.image: $ImageVector"
        indent {
            +"get() = iconInfoImages.getOrPut(this) {"
            indent {
                +"when (this) {"
                indent {
                    icons.forEach { icon ->
                        val key = icon.propertyName
                        val Key = inject("androidx.compose.material.icons.filled", key)
                        +"$IconInfo.$key -> $Icons.Default.$Key"
                    }
                }
                +"}"
            }
        }
        +"}"
    }
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