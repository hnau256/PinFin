package hnau.pinfin.model.transaction.utils

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import hnau.common.kotlin.coroutines.flow.state.flatMapWithScope
import hnau.common.kotlin.coroutines.flow.state.mapState
import hnau.common.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

internal sealed interface Editable<out T> {

    data object Incorrect : Editable<Nothing>

    data class Value<out T>(
        val value: T,
        val changed: Boolean,
    ) : Editable<T> {

        companion object {

            fun <T> create(
                value: T,
                initialValueOrNone: Option<T>,
            ): Value<T> = Value(
                value = value,
                changed = initialValueOrNone.fold(
                    ifEmpty = { true },
                    ifSome = { it != value },
                ),
            )

            fun <T> create(
                scope: CoroutineScope,
                value: StateFlow<T>,
                initialValueOrNone: Option<T>,
            ): StateFlow<Value<T>> = value.mapState(
                scope = scope,
            ) { value ->
                create(
                    value = value,
                    initialValueOrNone = initialValueOrNone,
                )
            }
        }
    }

    companion object {

        fun <T> create(
            valueOrNone: Option<T>,
            initialValueOrNone: Option<T>,
        ): Editable<T> = valueOrNone.fold(
            ifEmpty = { Incorrect },
            ifSome = { currentValue ->
                Value.create(
                    value = currentValue,
                    initialValueOrNone = initialValueOrNone,
                )
            }
        )

        fun <T> create(
            scope: CoroutineScope,
            valueOrNone: StateFlow<Option<T>>,
            initialValueOrNone: Option<T>,
        ): StateFlow<Editable<T>> = valueOrNone.mapState(
            scope = scope,
        ) { valueOrNone ->
            create(
                valueOrNone = valueOrNone,
                initialValueOrNone = initialValueOrNone,
            )
        }
    }
}

internal val <T> Editable<T>.valueOrNone: Option<T>
    get() = when (this) {
        Editable.Incorrect -> None
        is Editable.Value<T> -> Some(value)
    }


internal inline fun <A, B, Z> Editable.Value<A>.combineEditableValueWith(
    other: Editable.Value<B>,
    crossinline combine: (A, B) -> Z,
): Editable<Z> = Editable.Value(
    value = combine(value, other.value),
    changed = changed || other.changed,
)

internal inline fun <A, B, Z> Editable<A>.combineEditableWith(
    other: Editable<B>,
    crossinline combine: (A, B) -> Z,
): Editable<Z> = when (this) {
    Editable.Incorrect -> Editable.Incorrect
    is Editable.Value<A> -> when (other) {
        Editable.Incorrect -> Editable.Incorrect
        is Editable.Value<B> -> combineEditableValueWith(
            other = other,
            combine = combine,
        )
    }
}

internal inline fun <A, B, Z> StateFlow<Editable<A>>.combineEditableWith(
    scope: CoroutineScope,
    other: StateFlow<Editable<B>>,
    crossinline combine: (A, B) -> Z,
): StateFlow<Editable<Z>> = flatMapWithScope(scope) { scope, a ->
    when (a) {
        Editable.Incorrect -> Editable.Incorrect.toMutableStateFlowAsInitial()
        is Editable.Value<A> -> other.mapState(scope) { b ->
            when (b) {
                Editable.Incorrect -> Editable.Incorrect
                is Editable.Value<B> -> a.combineEditableValueWith(
                    other = b,
                    combine = combine,
                )
            }
        }
    }
}

internal inline fun <I, O> Editable<I>.flatMap(
    transform: (I) -> Editable<O>,
): Editable<O> = when (this) {
    Editable.Incorrect -> Editable.Incorrect
    is Editable.Value<I> -> transform(value).let { transformed ->
        when (transformed) {
            Editable.Incorrect -> Editable.Incorrect
            is Editable.Value<O> -> Editable.Value(
                changed = changed || transformed.changed,
                value = transformed.value,
            )
        }
    }
}

internal inline fun <I, O> Editable.Value<I>.map(
    transform: (I) -> O,
): Editable.Value<O> = Editable.Value(
    value = transform(value),
    changed = false,
)

internal inline fun <I, O> Editable<I>.map(
    transform: (I) -> O,
): Editable<O> = flatMap { value ->
    Editable.Value(
        value = transform(value),
        changed = false,
    )
}