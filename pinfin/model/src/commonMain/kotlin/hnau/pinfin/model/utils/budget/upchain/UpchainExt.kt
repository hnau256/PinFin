package hnau.pinfin.model.utils.budget.upchain

operator fun Upchain.plus(
    updates: Iterable<Update>,
): Upchain = updates.fold(
    initial = this,
    operation = Upchain::plus,
)