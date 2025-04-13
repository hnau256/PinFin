package hnau.pinfin.client.projector

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import hnau.common.compose.uikit.backbutton.BackButtonDelegate
import hnau.common.compose.uikit.backbutton.BackButtonWidthProvider
import hnau.common.compose.uikit.bubble.BubblesShower
import hnau.common.compose.uikit.bubble.Content
import hnau.common.compose.uikit.bubble.SharedBubblesHolder
import hnau.common.compose.uikit.state.LoadableContent
import hnau.common.compose.uikit.state.TransitionSpec
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.pinfin.client.data.budget.AccountInfoResolver
import hnau.pinfin.client.data.budget.CategoryInfoResolver
import hnau.pinfin.client.model.InitModel
import hnau.pinfin.client.projector.mainstack.MainStackProjector
import hnau.pinfin.client.projector.utils.AmountFormatter
import hnau.pinfin.client.projector.utils.DateTimeFormatter
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class InitProjector(
    scope: CoroutineScope,
    model: InitModel,
    private val dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        fun mainStack(
            bubblesShower: BubblesShower,
            backButtonWidthProvider: BackButtonWidthProvider,
            dateTimeFormatter: DateTimeFormatter,
            amountFormatter: AmountFormatter,
            accountInfoResolver: AccountInfoResolver,
            categoryInfoResolver: CategoryInfoResolver,
        ): MainStackProjector.Dependencies
    }

    private val bubblesHolder = SharedBubblesHolder(
        scope = scope,
    )


    private val backButtonDelegate: BackButtonDelegate = BackButtonDelegate(
        goBackHandler = model.goBackHandler,
    )

    private val mainSackProjector: StateFlow<Loadable<MainStackProjector>> = model
        .mainStackModel
        .mapWithScope(scope) { scope, mainStackOrLoading ->
            mainStackOrLoading.map { mainStack ->
                MainStackProjector(
                    scope = scope,
                    model = mainStack,
                    dependencies = dependencies.mainStack(
                        bubblesShower = bubblesHolder,
                        backButtonWidthProvider = backButtonDelegate,
                        dateTimeFormatter = DateTimeFormatter.test, //TODO
                        amountFormatter = AmountFormatter.test,
                        accountInfoResolver = mainStack.budgetRepository.account,
                        categoryInfoResolver = mainStack.budgetRepository.category,
                    )
                )
            }
        }

    @Composable
    fun Content() {
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onBackground,
            //LocalDensity provides Density(LocalDensity.current.density * 1.1f),
        ) {
            mainSackProjector
                .collectAsState()
                .value
                .LoadableContent(
                    modifier = Modifier.fillMaxSize(),
                    transitionSpec = TransitionSpec.both(),
                ) { mainStackProjector ->
                    mainStackProjector.Content()
                }
            backButtonDelegate.Content()
            bubblesHolder.Content()
        }
    }
}