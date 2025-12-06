package hnau.pinfin.data

@Deprecated("Use from EnumValues")
data class AmountDirectionValues<out T>(
    val credit: T,
    val debit: T,
) {
    
    operator fun get(
        amountDirection: AmountDirection,
    ): T = when (amountDirection) {
        AmountDirection.Credit -> credit
        AmountDirection.Debit -> debit
    }
    
    inline fun <R> map(
        transform: (amountDirection: AmountDirection, value: T) -> R,
    ): AmountDirectionValues<R> = AmountDirectionValues(
        credit = transform(AmountDirection.Credit, credit),
        debit = transform(AmountDirection.Debit, debit),
    )
    
    inline fun <R> map(
        transform: (value: T) -> R,
    ): AmountDirectionValues<R> = map { _, value ->
        transform(value)
    }
    
    inline fun <O, R> combineWith(
        other: AmountDirectionValues<O>,
        combine: (amountDirection: AmountDirection, value: T, other: O) -> R,
    ): AmountDirectionValues<R> = AmountDirectionValues(
        credit = combine(AmountDirection.Credit, credit, other.credit),
        debit = combine(AmountDirection.Debit, debit, other.debit),
    )
    
    inline fun <O, R> combineWith(
        other: AmountDirectionValues<O>,
        combine: (value: T, other: O) -> R,
    ): AmountDirectionValues<R> = combineWith(
        other = other,
    ) { _, value, other ->
        combine(value, other)
    }
    
    companion object {
        
        inline fun <T> create(
            createValue: (amountDirection: AmountDirection) -> T,
        ): AmountDirectionValues<T> = AmountDirectionValues(
            credit = createValue(AmountDirection.Credit),
            debit = createValue(AmountDirection.Debit),
        )
    }
}