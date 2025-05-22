package hnau.pinfin.model

import hnau.common.model.preferences.Preferences
import hnau.pinfin.model.utils.budget.storage.BudgetsStorage

actual fun createRootModelDependencies(
    preferencesFactory: Preferences.Factory,
    budgetsStorageFactory: BudgetsStorage.Factory,
): RootModel.Dependencies = RootModel.Dependencies.impl(
    preferencesFactory = preferencesFactory,
    budgetsStorageFactory = budgetsStorageFactory,
)