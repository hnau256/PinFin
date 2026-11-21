package org.hnau.pinfin.app

import org.hnau.commons.app.model.app.RootModelFactory
import org.hnau.commons.app.model.file.File
import org.hnau.commons.app.model.file.absolutePath
import org.hnau.commons.app.model.file.mkDirs
import org.hnau.commons.app.model.file.plus
import org.hnau.commons.app.model.preferences.impl.FileBasedPreferences
import org.hnau.pinfin.model.RootModel
import org.hnau.pinfin.model.impl
import org.hnau.pinfin.model.utils.budget.storage.BudgetsStorage
import org.hnau.pinfin.model.utils.budget.storage.impl.files

fun createPinFinRootModelFactory(
    dependencies: PinFinAppDependencies,
    appFilesDir: File,
): RootModelFactory<RootModel.Skeleton, RootModel> = RootModelFactory(
    skeletonSerializer = RootModel.Skeleton.serializer(),
    createDefaultSkeleton = { RootModel.Skeleton() },
    createRootModel = { scope, skeleton ->
        RootModel(
            scope = scope,
            dependencies = RootModel.Dependencies.impl(
                preferencesFactory = FileBasedPreferences.Factory(
                    preferencesFile = appFilesDir + "preferences.txt",
                ),
                budgetsStorageFactory = BudgetsStorage.Factory.files(
                    budgetsDir = (appFilesDir + "budgets")
                        .apply(File::mkDirs)
                        .absolutePath,
                ),
                clipboardAccessor = dependencies.clipboardAccessor,
            ),
            skeleton = skeleton,
        )
    },
    extractGoBackHandler = RootModel::goBackHandler,
)