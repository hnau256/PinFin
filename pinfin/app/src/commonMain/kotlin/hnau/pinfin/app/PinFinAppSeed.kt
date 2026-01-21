package hnau.pinfin.app

import hnau.common.app.model.app.AppSeed
import hnau.common.app.model.file.plus
import hnau.common.app.model.preferences.impl.FileBasedPreferences
import hnau.common.app.model.theme.ThemeBrightness
import hnau.common.app.model.utils.Hue
import hnau.pinfin.model.RootModel
import hnau.pinfin.model.impl
import hnau.pinfin.model.utils.budget.storage.BudgetsStorage
import hnau.pinfin.model.utils.budget.storage.impl.files

fun createPinFinAppSeed(
    dependencies: PinFinAppDependencies,
    defaultBrightness: ThemeBrightness? = null,
): AppSeed<RootModel, RootModel.Skeleton> = AppSeed(
    fallbackHue = Hue(240),
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
                    sha256 = dependencies.sha256,
                ),
                inetAddressesProvider = dependencies.inetAddressesProvider,
            ),
            skeleton = skeleton,
        )
    },
)