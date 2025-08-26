package hnau.pinfin.projector.transaction.pageable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hnau.common.app.projector.uikit.state.StateContent
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.Icon
import hnau.common.app.projector.utils.copy
import hnau.common.kotlin.coroutines.mapReusable
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.pinfin.model.transaction.pageable.RecordsModel
import hnau.pinfin.model.transaction.utils.RecordId
import hnau.pinfin.model.utils.ZipList
import hnau.pinfin.projector.transaction.utils.createPagesTransitionSpec
import hnau.pinfin.projector.utils.CategoryButton
import hnau.pinfin.projector.utils.CategoryViewMode
import hnau.pinfin.projector.utils.SlideOrientation
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class RecordsProjector(
    scope: CoroutineScope,
    private val model: RecordsModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun record(): RecordProjector.Dependencies
    }

    class Page(
        scope: CoroutineScope,
        private val model: RecordsModel.Page,
        dependencies: Dependencies,
    ) {

        @Pipe
        interface Dependencies {

            fun record(): RecordProjector.Dependencies

            fun recordPage(): RecordProjector.Page.Dependencies
        }

        private val records: StateFlow<List<Item>> = Item.create(
            scope = scope,
            dependencies = dependencies.record(),
            items = model.items,
        )

        @Composable
        fun Content(
            modifier: Modifier = Modifier,
            contentPadding: PaddingValues,
        ) {
            Box(
                modifier = modifier.padding(horizontal = Dimens.separation),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
                ) {
                    Tabs(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = contentPadding.copy(bottom = 0.dp),
                    )
                    Record(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = contentPadding.copy(top = 0.dp),
                    )
                }
            }
        }

        @Composable
        private fun Tabs(
            modifier: Modifier = Modifier,
            contentPadding: PaddingValues,
        ) {
            val records by records.collectAsState()
            LazyRow(
                modifier = modifier,
                contentPadding = contentPadding,
                horizontalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
            ) {
                items(
                    items = records,
                    key = { it.id.id },
                ) { item ->
                    item.projector.Content()
                }
                item {
                    Button(
                        onClick = model.addNewRecord,
                    ) {
                        Icon(Icons.Default.Add)
                    }
                }
            }
        }

        private val currentRecord: StateFlow<Triple<Int, RecordId, RecordProjector.Page>> = model
            .currentRecord
            .mapWithScope(scope) { scope, (i, id, record) ->
                val projector = RecordProjector.Page(
                    scope = scope,
                    dependencies = dependencies.recordPage(),
                    model = record,
                )
                Triple(i, id, projector)
            }

        @Composable
        private fun Record(
            modifier: Modifier = Modifier,
            contentPadding: PaddingValues,
        ) {
            currentRecord
                .collectAsState()
                .value
                .StateContent(
                    modifier = modifier,
                    label = "SelectedRecord",
                    contentKey = Triple<*, RecordId, *>::second,
                    transitionSpec = createPagesTransitionSpec(
                        orientation = SlideOrientation.Horizontal,
                        extractIndex = Triple<Int, *, *>::first,
                    )
                ) { (_, _, projector) ->
                    projector.Content(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = contentPadding,
                    )
                }
        }
    }

    private data class Item(
        val id: RecordId,
        val projector: RecordProjector,
    ) {

        companion object {

            fun create(
                scope: CoroutineScope,
                items: StateFlow<ZipList<RecordsModel.Item>>,
                dependencies: RecordProjector.Dependencies,
            ): StateFlow<List<Item>> = items
                .mapReusable(
                    scope = scope,
                ) { items ->
                    items.mapIndexed { i, item ->
                        getOrPutItem(item.id) { scope ->
                            val projector = RecordProjector(
                                scope = scope,
                                model = item.model,
                                dependencies = dependencies,
                            )
                            Item(
                                id = item.id,
                                projector = projector,
                            )
                        }

                    }
                }
        }
    }

    @Composable
    fun Content(
        modifier: Modifier = Modifier,
    ) {
        val isFocused by model.isFocused.collectAsState()
        val categories by model.categories.collectAsState()
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            categories.forEach { category ->
                CategoryButton(
                    info = category,
                    viewMode = CategoryViewMode.Icon,
                    onClick = model.requestFocus,
                    selected = isFocused,
                )
            }
        }
    }
}