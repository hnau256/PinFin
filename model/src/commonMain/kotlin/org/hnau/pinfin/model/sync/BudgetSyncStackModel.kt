package org.hnau.pinfin.model.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.preferences.Preference
import org.hnau.commons.app.model.preferences.Preferences
import org.hnau.commons.app.model.preferences.mapOption
import org.hnau.commons.app.model.stack.NonEmptyStack
import org.hnau.commons.app.model.stack.SkeletonWithModel
import org.hnau.commons.app.model.stack.goBackHandler
import org.hnau.commons.app.model.stack.modelsOnly
import org.hnau.commons.app.model.stack.push
import org.hnau.commons.app.model.stack.tryDropLast
import org.hnau.commons.app.model.stack.withModels
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.gen.sealup.annotations.SealUp
import org.hnau.commons.gen.sealup.annotations.Variant
import org.hnau.commons.kotlin.foldNullable
import org.hnau.commons.kotlin.mapper.Mapper
import org.hnau.commons.kotlin.mapper.option
import org.hnau.commons.kotlin.mapper.optionToNullable
import org.hnau.commons.kotlin.mapper.plus
import org.hnau.commons.kotlin.mapper.toMapper
import org.hnau.pinfin.data.BudgetId

class BudgetSyncStackModel(
    scope: CoroutineScope,
    private val skeleton: Skeleton,
    dependencies: Dependencies,
) {


    @Serializable
    data class Skeleton(
        val stack: MutableStateFlow<NonEmptyStack<BudgetSyncStackElementSkeleton>> = MutableStateFlow(
            NonEmptyStack(
                ElementSkeleton.config(
                    config = BudgetSyncConfigModel.Skeleton.createForNew(),
                )
            )
        ),
    )

    @Pipe
    interface Dependencies {

        val id: BudgetId

        val preferences: Preferences
    }

    @SealUp(
        variants = [
            Variant(
                type = BudgetSyncMainModel::class,
                identifier = "main",
            ),
            Variant(
                type = BudgetSyncConfigModel::class,
                identifier = "config",
            ),
        ],
        wrappedValuePropertyName = "model",
        sealedInterfaceName = "BudgetSyncStackElementModel",
    )
    interface Element {

        val goBackHandler: GoBackHandler

        companion object
    }

    @SealUp(
        variants = [
            Variant(
                type = Unit::class,
                identifier = "main",
            ),
            Variant(
                type = BudgetSyncConfigModel.Skeleton::class,
                identifier = "config",
            ),
        ],
        wrappedValuePropertyName = "skeleton",
        sealedInterfaceName = "BudgetSyncStackElementSkeleton",
        serializable = true,
    )
    interface ElementSkeleton {

        companion object
    }


    private val configPreference: Preference<SyncConfig?> = dependencies
        .preferences["budget_${dependencies.id.let(BudgetId.stringMapper.reverse)}_sync"]
        .mapOption(
            scope = scope,
            mapper = Json
                .toMapper(SyncConfig.serializer())
                .let(Mapper.Companion::option) + Mapper.optionToNullable(),
        )

    private val stackWithModels: StateFlow<NonEmptyStack<SkeletonWithModel<BudgetSyncStackElementSkeleton, BudgetSyncStackElementModel>>> =
        skeleton
            .stack
            .withModels(
                scope = scope,
                getKey = BudgetSyncStackElementSkeleton::ordinal,
            ) { modelScope, skeleton ->
                createModel(
                    scope = modelScope,
                    elementSkeleton = skeleton,
                )
            }

    private fun createModel(
        scope: CoroutineScope,
        elementSkeleton: BudgetSyncStackElementSkeleton,
    ): BudgetSyncStackElementModel = elementSkeleton.fold(
        ifMain = {
            Element.main(
                scope = scope,
                config = configPreference.value,
                removeConfig = { configPreference.update(null) },
                openConfig = {
                    skeleton.stack.push(
                        ElementSkeleton.config(
                            configPreference.value.value.foldNullable(
                                ifNull = BudgetSyncConfigModel.Skeleton::createForNew,
                                ifNotNull = BudgetSyncConfigModel.Skeleton::createForConfig
                            )
                        )
                    )
                }
            )
        },
        ifConfig = { configSkeleton ->
            Element.config(
                scope = scope,
                skeleton = configSkeleton,
                close = { skeleton.stack.tryDropLast() },
                save = { newConfig -> configPreference.update(newConfig) },
            )
        },
    )

    val stack: StateFlow<NonEmptyStack<BudgetSyncStackElementModel>> =
        stackWithModels.modelsOnly(scope)

    val goBackHandler: GoBackHandler = stackWithModels.goBackHandler(
        scope = scope,
        extractGoBackHandler = BudgetSyncStackElementModel::goBackHandler,
        updateSkeletonStack = skeleton.stack::value::set,
    )
}