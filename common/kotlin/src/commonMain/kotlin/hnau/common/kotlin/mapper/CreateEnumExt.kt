package hnau.common.kotlin.mapper

inline fun <T, reified E : Enum<E>> Mapper.Companion.toEnum(
    crossinline extractValue: E.() -> T,
    noinline handleNotFoundItem: ((T) -> E)? = null,
): Mapper<T, E> {
    val classOfE = E::class.java
    val byValue = enumValues<E>().associateBy(extractValue)
    return Mapper(
        direct = { value ->
            when {
                byValue.containsKey(value) -> byValue.getValue(value)
                handleNotFoundItem != null -> handleNotFoundItem(value)
                else -> error("Unable to find item of $classOfE for '$value'")
            }
        },
        reverse = { enum ->
            enum.extractValue()
        },
    )
}

inline fun <T, reified E : Enum<E>> Mapper.Companion.toEnum(
    default: E,
    crossinline extractValue: E.() -> T,
) = toEnum(
    handleNotFoundItem = { default },
    extractValue = extractValue,
)

inline fun <reified E : Enum<E>> Mapper.Companion.nameToEnum(
    noinline handleNotFoundItem: ((String) -> E)? = null,
) = toEnum(
    handleNotFoundItem = handleNotFoundItem,
    extractValue = Enum<E>::name,
)

inline fun <reified E : Enum<E>> Mapper.Companion.nameToEnum(
    default: E,
) = toEnum(
    default = default,
    extractValue = Enum<E>::name,
)

inline fun <reified E : Enum<E>> Mapper.Companion.ordinalToEnum(
    noinline handleNotFoundItem: ((Int) -> E)? = null,
) = toEnum(
    handleNotFoundItem = handleNotFoundItem,
    extractValue = Enum<E>::ordinal,
)

inline fun <reified E : Enum<E>> Mapper.Companion.ordinalToEnum(
    default: E,
) = toEnum(
    default = default,
    extractValue = Enum<E>::ordinal,
)
