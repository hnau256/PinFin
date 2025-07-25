@file:Suppress("JSON_FORMAT_REDUNDANT")

package hnau.generateicons

import arrow.core.NonEmptyList
import arrow.core.NonEmptySet
import arrow.core.toNonEmptyListOrNull
import arrow.core.toNonEmptySetOrNull
import hnau.common.gen.kt.Importable
import hnau.common.gen.kt.inject
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
        .sortedByDescending { it.weight }

    val modelProject = ":pinfin:model"
    val modelSubpackage = "utils.icons"

    val categoryClassName = "IconCategory"

    createKtFile(
        projectName = modelProject,
        subpackage = modelSubpackage,
        name = categoryClassName,
    ) {
        val categories = icons
            .groupBy(Icon::category)
            .toList()
            .sortedByDescending { it.second.sumOf(Icon::weight) }
            .map(Pair<String, *>::first)

        +"enum class $categoryClassName {"
        indent {
            categories.forEach { category ->
                +"$category,"
            }
        }
        +"}"
    }

    val iconImportable = Importable("hnau.pinfin.data", "Icon")

    val iconVariantClassName = "IconVariant"

    createKtFile(
        projectName = modelProject,
        subpackage = modelSubpackage,
        name = iconVariantClassName,
    ) {
        +"@Suppress(\"EnumEntryName\", \"unused\")"
        +"enum class $iconVariantClassName {"
        indent {
            icons.forEach { icon ->
                +"${icon.propertyName},"
            }
        }
        +"}"
    }

    createKtFile(
        projectName = modelProject,
        subpackage = modelSubpackage,
        name = iconVariantClassName + "Ext",
    ) {
        +"val $iconVariantClassName.icon: ${inject(iconImportable)}"
        indent {
            +"get() = Icon(name)"
        }
        +""
        +"private val variantsByIcons: Map<${inject(iconImportable)}, IconVariant> ="
        indent {
            +"$iconVariantClassName.entries.associateBy { Icon(key = it.name) }"
        }
        +""
        +"val ${inject(iconImportable)}.variant: $iconVariantClassName?"
        indent {
            +"get() = variantsByIcons.getValue(this)"
        }
    }

    fun createVariantExtFile(
        propertyName: String,
        extract: Icon.() -> String,
        importables: Iterable<Importable> = emptyList(),
        typeName: String,
    ) {
        val uppercaseName = propertyName.uppercaseFirst

        createKtFile(
            projectName = modelProject,
            subpackage = modelSubpackage,
            name = iconVariantClassName + uppercaseName + "Ext",
        ) {
            val pages: List<List<Pair<Icon, String>>> = icons
                .map { it to it.extract() }
                .windowed(
                    size = 50,
                    step = 50,
                    partialWindows = true,
                )
            importables.forEach { importable -> addImport(importable) }
            +"val $iconVariantClassName.$propertyName: $typeName"
            indent {
                +"get() = variant$uppercaseName[ordinal]"
            }
            +""
            +"private val variant$uppercaseName: List<$typeName> = buildList {"
            indent {
                pages.indices.forEach { i ->
                    +"addAll(get$uppercaseName$i())"
                }
            }
            +"}"
            pages.forEachIndexed { i, page ->
                +""
                +"private fun get$uppercaseName$i(): List<$typeName> = listOf("
                indent {
                    page.forEach { (icon, value) ->
                        +("$value, //${icon.propertyName}")
                    }
                }
                +")"
            }
        }
    }

    createVariantExtFile(
        propertyName = "tags",
        importables = listOf(
            Importable("arrow.core", "NonEmptySet"),
            Importable("arrow.core", "nonEmptySetOf"),
        ),
        typeName = "NonEmptySet<String>",
        extract = {
            val tags = nonEmptyTags.joinToString { "\"$it\"" }
            "nonEmptySetOf($tags)"
        },
    )

    createVariantExtFile(
        propertyName = "title",
        typeName = "String",
        extract = { "\"$prettyName\"" },
    )

    createVariantExtFile(
        propertyName = "weight",
        typeName = "Int",
        extract = { weight.toString() },
    )

    createVariantExtFile(
        propertyName = "category",
        typeName = "IconCategory",
        extract = { "IconCategory.$category" },
    )

    val iconVariantImportable =
        Importable("hnau.pinfin.model.$modelSubpackage", iconVariantClassName)
    val imageVectorImportable = Importable("androidx.compose.ui.graphics.vector", "ImageVector")
    createKtFile(
        projectName = ":pinfin:projector",
        subpackage = "utils",
        name = "IconInfoExt",
    ) {
        val IconVariant = inject(iconVariantImportable)
        val ImageVector = inject(imageVectorImportable)
        val Icons = inject("androidx.compose.material.icons", "Icons")
        +"private val iconInfoImages: MutableMap<$IconVariant, $ImageVector> = mutableMapOf()"
        +""
        +"val $IconVariant.image: $ImageVector"
        indent {
            +"get() = iconInfoImages.getOrPut(this) {"
            indent {
                +"when (this) {"
                indent {
                    icons.forEach { icon ->
                        val key = icon.propertyName
                        val Key = inject("androidx.compose.material.icons.filled", key)
                        +"$IconVariant.${icon.propertyName} -> $Icons.Default.$Key"
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

private val Icon.weight: Int
    get() = popularity + (if (propertyName in bestIcons) 1000000 else 0)

private val bestIcons: Set<String> = setOf(
    "Home",
    "Payment",
    "AccountBalanceWallet",
    "Store",
    "Savings",
    "AccountBalance",
    "Checkroom",
    "ShoppingCart",
    "Redeem",
    "Toys",
    "LocalFlorist",
    "Restaurant",
    "Liquor",
    "Storefront",
    "LunchDining",
    "Coffee",
    "BakeryDining",
    "BusinessCenter",
    "DirectionsBus",
    "DirectionsCar",
    "Flight",
    "DirectionsRun",
    "Hiking",
    "School",
    "SportsEsports",
    "Celebration",
    "SportsBasketball",
    "FitnessCenter",
    "MedicalServices",
    "HealthAndSafety",
    "Call",
    "AlternateEmail",
    "ElectricalServices",
    "Outlet",
)