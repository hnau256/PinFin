package org.hnau.pinfin.app

import org.hnau.commons.app.model.app.AppSeed
import org.hnau.commons.app.model.file.plus
import org.hnau.commons.app.model.preferences.impl.FileBasedPreferences
import org.hnau.commons.app.model.theme.ThemeBrightness
import org.hnau.commons.app.model.utils.Hue
import org.hnau.pinfin.model.RootModel
import org.hnau.pinfin.model.impl
import org.hnau.pinfin.model.utils.budget.storage.BudgetsStorage
import org.hnau.pinfin.model.utils.budget.storage.impl.files

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