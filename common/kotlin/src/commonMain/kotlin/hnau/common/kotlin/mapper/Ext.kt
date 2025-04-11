package hnau.common.kotlin.mapper

fun <T> Mapper.Companion.takeIf(
    predicate: (T) -> Boolean,
    restore: () -> T,
): Mapper<T, T?> = Mapper(
    direct = { it.takeIf(predicate) },
    reverse = { it ?: restore() },
)

val <I, O> Mapper<I, O>.nullable: Mapper<I?, O?>
    get() = Mapper(
        direct = { i -> i?.let(direct) },
        reverse = { o -> o?.let(reverse) },
    )

fun <T> Mapper.Companion.nullable(
    nullPlaceholder: T,
): Mapper<T, T?> = takeIf(
    predicate = { it != nullPlaceholder },
    restore = { nullPlaceholder },
)

operator fun <A, B, C> Mapper<A, B>.plus(
    other: Mapper<B, C>,
): Mapper<A, C> = Mapper<A, C>(
    direct = { direct(it).let(other.direct) },
    reverse = { reverse(other.reverse(it)) },
)

fun <I, O> Mapper<I, O>.swap(): Mapper<O, I> = Mapper(
    direct = reverse,
    reverse = direct,
)

fun <I, O> Mapper<I, O>.toListMapper(): Mapper<List<I>, List<O>> = Mapper(
    direct = { i -> i.map(direct) },
    reverse = { o -> o.map(reverse) },
)
