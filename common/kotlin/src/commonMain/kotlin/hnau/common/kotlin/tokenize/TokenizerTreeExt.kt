package hnau.common.kotlin.tokenize

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import hnau.common.kotlin.ifNull

internal fun <T> Tokenizer.OptionFactory.Companion.stringsTree(
    variants: NonEmptyList<Pair<String, T>>,
): Tokenizer.OptionFactory<Char, T> = tree(
    variants = variants.map { (key, value) ->
        val items = key
            .toList()
            .toNonEmptyListOrNull()
            ?: throw IllegalArgumentException("Unable create tree TokenizerOptionFactory with empty string key")
        items to value
    }
)

internal fun <I, T> Tokenizer.OptionFactory.Companion.tree(
    variants: NonEmptyList<Pair<NonEmptyList<I>, T>>,
): Tokenizer.OptionFactory<I, T> {
    val tree = TreeItem.create(variants)
    return Tokenizer.OptionFactory { firstItem ->
        TreeTokenizer
            .createOrValue(
                tree = tree,
                items = listOf(firstItem),
                variants = variants,
                item = firstItem,
                ifNoNextTreeItem = { return@OptionFactory null }
            )
            .fold(
                ifRight = { it },
                ifLeft = Tokenizer.Companion::alreadyCompleted,
            )
    }
}

private sealed interface TreeItem<out I, out T> {

    data class Value<out T>(
        val value: T,
    ) : TreeItem<Nothing, T>

    data class Subtree<I, out T>(
        val items: Map<I, TreeItem<I, T>>,
    ) : TreeItem<I, T>

    companion object {

        fun <I, T> create(
            variants: List<Pair<NonEmptyList<I>, T>>,
        ): Subtree<I, T> = create(
            variants = variants,
            extractFirst = { (head), _ -> head },
            extractTail = { list, _ -> list.tail },
        )

        private fun <I, T> createAny(
            variants: List<Pair<List<I>, T>>,
        ): TreeItem<I, T> {
            return create(
                variants = variants,
                extractFirst = { items, value ->
                    when (items.isNotEmpty()) {
                        false -> return Value(value)
                        true -> items[0]
                    }
                },
                extractTail = { list, _ -> list.drop(1) },
            )
        }

        private inline fun <I, IL : List<I>, T> create(
            variants: List<Pair<IL, T>>,
            extractFirst: (IL, T) -> I,
            extractTail: (IL, T) -> List<I>,
        ): Subtree<I, T> {
            return variants
                .groupBy { (items, value) ->
                    extractFirst(items, value)
                }
                .mapValues { (_, subvariants) ->
                    subvariants
                        .map { (items, value) ->
                            extractTail(items, value) to value
                        }
                        .let(::createAny)
                }
                .let(::Subtree)
        }
    }
}

private class TreeTokenizer<I, T>(
    private val tree: TreeItem.Subtree<I, T>,
    private val previousItems: List<I>,
    private val variants: NonEmptyList<Pair<NonEmptyList<I>, T>>,
) : CompletionAwareTokenizer<I, T>() {

    override fun consumeOrCollect(
        nextItem: I,
    ): Either<T, Tokenizer<I, T>> {
        val items = previousItems + nextItem
        return createOrValue(
            tree = tree,
            items = items,
            variants = variants,
            item = nextItem,
            ifNoNextTreeItem = { throw IllegalArgumentException("Unable find variant for '$items' in $variants") },
        )
    }

    companion object {

        inline fun <I, T> createOrValue(
            tree: TreeItem.Subtree<I, T>,
            items: List<I>,
            variants: NonEmptyList<Pair<NonEmptyList<I>, T>>,
            item: I,
            ifNoNextTreeItem: () -> Nothing,
        ): Either<T, TreeTokenizer<I, T>> = tree
            .items[item]
            .ifNull { ifNoNextTreeItem() }
            .let { treeItem ->
                when (treeItem) {
                    is TreeItem.Subtree -> Either.Right(
                        TreeTokenizer(
                            tree = treeItem,
                            previousItems = items,
                            variants = variants,
                        )
                    )

                    is TreeItem.Value -> Either.Left(treeItem.value)
                }
            }
    }

}