package org.hnau.pinfin.app

import org.hnau.commons.app.model.app.AppFilesDirProvider
import org.hnau.commons.app.model.app.AppSeed
import org.hnau.commons.app.model.file.absolutePath
import org.hnau.commons.app.model.file.plus
import org.hnau.commons.app.model.preferences.impl.FileBasedPreferences
import org.hnau.commons.app.model.theme.ThemeBrightness
import org.hnau.commons.app.model.theme.color.Hue
import org.hnau.pinfin.model.RootModel
import org.hnau.pinfin.model.impl
import org.hnau.pinfin.model.utils.budget.storage.BudgetsStorage
import org.hnau.pinfin.model.utils.budget.storage.impl
import org.hnau.pinfin.model.utils.budget.storage.impl.files

fun createPinFinAppSeed(
    dependencies: PinFinAppDependencies,
    appFilesDirProvider: AppFilesDirProvider,
): AppSeed<RootModel, RootModel.Skeleton> = AppSeed(
    skeletonSerializer = RootModel.Skeleton.serializer(),
    createDefaultSkeleton = { RootModel.Skeleton() },
    createModel = { scope, skeleton ->
        val appFilesDir = appFilesDirProvider.getAppFilesDir()
        RootModel(
            scope = scope,
            dependencies = RootModel.Dependencies.impl(
                preferencesFactory = FileBasedPreferences.Factory(
                    preferencesFile = appFilesDir + "preferences.txt",
                ),
                budgetsStorageFactory = BudgetsStorage.Factory.files(
                    budgetsDir = (appFilesDir + "budgets").absolutePath,
                    dependencies = BudgetsStorage.Factory.Dependencies.impl(
                        currency = dependencies.currency,
                    ),
                ),
                currency = dependencies.currency,
            ),
            skeleton = skeleton,
        )
    },
)