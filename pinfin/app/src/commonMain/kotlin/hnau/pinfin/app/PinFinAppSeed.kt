package hnau.pinfin.app

import hnau.common.app.model.ThemeBrightness
import hnau.common.app.model.app.AppSeed
import hnau.common.app.model.color.material.MaterialHue
import hnau.common.app.model.file.plus
import hnau.common.app.model.preferences.impl.FileBasedPreferences
import hnau.pinfin.model.RootModel
import hnau.pinfin.model.impl
import hnau.pinfin.model.utils.budget.storage.BudgetsStorage
import hnau.pinfin.model.utils.budget.storage.impl.files

fun createPinFinAppSeed(
    defaultBrightness: ThemeBrightness? = null,
): AppSeed<RootModel, RootModel.Skeleton> = AppSeed(
    fallbackHue = MaterialHue.LightGreen,
    defaultBrightness = defaultBrightness,
    skeletonSerializer = RootModel.Skeleton.serializer(),
    createDefaultSkeleton = { RootModel.Skeleton() },
    createModel = { scope, appContext, skeleton ->
        RootModel(
            scope = scope,
            dependencies = RootModel.Dependencies.impl(
                preferencesFactory = FileBasedPreferences.Factory(
                    preferencesFile = appContext.filesDir + "preferences.txt",
                ),
                budgetsStorageFactory = BudgetsStorage.Factory.files(
                    budgetsDir = appContext.filesDir + "budgets",
                )
            ),
            skeleton = skeleton,
        )
    },
    extractGoBackHandler = RootModel::goBackHandler,
)