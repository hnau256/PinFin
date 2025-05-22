package hnau.pinfin.app

import hnau.common.model.app.AppSeed
import hnau.common.model.color.material.MaterialHue
import hnau.common.model.preferences.Preferences
import hnau.common.model.preferences.impl.FileBasedPreferences
import hnau.pinfin.model.RootModel
import hnau.pinfin.model.utils.budget.storage.BudgetsStorage
import hnau.pinfin.model.utils.budget.storage.impl.files
import java.io.File

fun createPinFinAppSeed(): AppSeed<RootModel, RootModel.Skeleton> = AppSeed(
    fallbackHue = MaterialHue.LightGreen,
    skeletonSerializer = RootModel.Skeleton.serializer(),
    createDefaultSkeleton = { RootModel.Skeleton() },
    createModel = { scope, appContext, skeleton ->
        RootModel(
            scope = scope,
            dependencies = createRootModelDependencies(
                preferencesFactory = FileBasedPreferences.Factory(
                    preferencesFile = File(appContext.filesDir, "preferences.txt"),
                ),
                budgetsStorageFactory = BudgetsStorage.Factory.files(
                    budgetsDir = File(appContext.filesDir, "budgets"),
                )
            ),
            skeleton = skeleton,
        )
    },
    extractGoBackHandler = RootModel::goBackHandler,
)

expect fun createRootModelDependencies(
    preferencesFactory: Preferences.Factory,
    budgetsStorageFactory: BudgetsStorage.Factory,
): RootModel.Dependencies