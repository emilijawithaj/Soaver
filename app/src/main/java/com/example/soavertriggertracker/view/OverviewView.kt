package com.example.soavertriggertracker.view

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.SpaceAround
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BackHand
import androidx.compose.material.icons.filled.Boy
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.soavertriggertracker.MainNavigationBar
import com.example.soavertriggertracker.MainNavigationRail
import com.example.soavertriggertracker.R
import com.example.soavertriggertracker.data.FactorCategory
import com.example.soavertriggertracker.ui.theme.SoaverTriggerTrackerTheme
import com.example.soavertriggertracker.view.OverviewView.OverviewPage
import com.example.soavertriggertracker.viewModel.OverviewPageViewModel
import com.example.soavertriggertracker.viewModel.uiDataItems.CommonFactorUIModel

object OverviewView {

    /**
     * Complete OverView page
     */
    @Composable
    fun OverviewScreen(
        viewModel: OverviewPageViewModel = hiltViewModel(),
        windowSize: WindowSizeClass
    ) {
        val triggers by viewModel.triggers.collectAsStateWithLifecycle()
        val noOfLogs by viewModel.noOfLogs.collectAsStateWithLifecycle()
        val commonFactors by viewModel.commonFactors.collectAsStateWithLifecycle()
        val triggerError by viewModel.triggerError.collectAsStateWithLifecycle()
        val factorError by viewModel.factorError.collectAsStateWithLifecycle()
        val onRefreshData = viewModel::onReload
        val onEditTriggers = viewModel::onEditTriggers
        val onRetryFactors = viewModel::onReloadFactors
        val onRetryTriggers = viewModel::onReloadTriggers

        when (windowSize.widthSizeClass) {
            WindowWidthSizeClass.Compact ->
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { MainNavigationBar() }) { padding ->
                    OverviewPage(
                        modifier = Modifier.padding(padding),
                        factors = commonFactors,
                        triggers = triggers,
                        onRefreshData = onRefreshData,
                        onEditTriggers = onEditTriggers,
                        noOfLogs = noOfLogs,
                        factorLoadingError = factorError,
                        triggerLoadingError = triggerError,
                        onRetryFactors = onRetryFactors,
                        onRetryTriggers = onRetryTriggers
                    )
                }

            else -> {
                Row() {
                    MainNavigationRail()
                    OverviewPage(
                        modifier = Modifier.weight(1f),
                        factors = commonFactors,
                        triggers = triggers,
                        onRefreshData = onRefreshData,
                        onEditTriggers = onEditTriggers,
                        noOfLogs = noOfLogs,
                        factorLoadingError = factorError,
                        triggerLoadingError = triggerError,
                        onRetryFactors = onRetryFactors,
                        onRetryTriggers = onRetryTriggers
                    )
                }
            }
        }
    }

    /**
     * main page content
     */
    @Composable
    fun OverviewPage(
        modifier: Modifier = Modifier,
        factors: List<CommonFactorUIModel>,
        triggers: List<String>,
        onRefreshData: () -> Unit,
        onEditTriggers: () -> Unit,
        noOfLogs: Int,
        factorLoadingError: Boolean,
        triggerLoadingError: Boolean,
        onRetryFactors: () -> Unit,
        onRetryTriggers: () -> Unit
    ) {
        Surface(
            modifier = modifier.padding(10.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                ReloadRow(
                    onRefreshData = onRefreshData,
                    noOfLogs = noOfLogs
                )
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                    ) {
                        TriggersTitleRow(
                            modifier = Modifier.padding(top = 36.dp),
                            onEditTriggers = onEditTriggers
                        )
                        if (triggerLoadingError) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                            ) {
                                Text(
                                    modifier = Modifier.align(CenterHorizontally),
                                    text = stringResource(R.string.error_loading_trigger_data),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                                OutlinedButton(
                                    modifier = Modifier
                                        .align(CenterHorizontally)
                                        .padding(16.dp),
                                    onClick = onRetryTriggers
                                ) {
                                    Text(text = stringResource(R.string.retry))
                                }
                            }
                        } else if (triggers.isEmpty()) {
                            Text(
                                modifier = Modifier.align(CenterHorizontally),
                                text = stringResource(R.string.no_saved_triggers_yet),
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        } else {
                            TriggerLayout(triggers = triggers)
                        }
                    }

                    Column() {
                        Text(
                            text = stringResource(R.string.most_common_factors),
                            style = MaterialTheme.typography.headlineLarge
                        )
                        if (factorLoadingError) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                            ) {
                                Text(
                                    modifier = Modifier.align(CenterHorizontally),
                                    text = stringResource(R.string.error_loading_factor_data),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                                OutlinedButton(
                                    modifier = Modifier
                                        .align(CenterHorizontally)
                                        .padding(16.dp),
                                    onClick = onRetryFactors
                                ) {
                                    Text(text = stringResource(R.string.retry))
                                }
                            }
                        } else if (factors.isEmpty()) {
                            Text(
                                modifier = Modifier.align(CenterHorizontally),
                                text = stringResource(R.string.no_common_factors_yet),
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        } else {
                            CommonFactorGrid(factors = factors)
                        }
                    }
                    DividerRow()
                }
            }
        }
    }
}

/**
 * deorative divider between sections
 */
@Composable
fun DividerRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {

        Icon(
            modifier = Modifier.size(18.dp),
            imageVector = Icons.Default.NoteAlt,
            contentDescription = null
        )
        Icon(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .size(18.dp),
            imageVector = Icons.Default.Percent,
            contentDescription = null
        )
        Icon(
            modifier = Modifier.size(18.dp),
            imageVector = Icons.Default.NoteAlt,
            contentDescription = null
        )
    }
}

/**
 * header for triggers section
 */
@Composable
fun TriggersTitleRow(
    modifier: Modifier = Modifier,
    onEditTriggers: () -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = CenterVertically
    ) {
        Text(
            modifier = Modifier
                .padding(bottom = 2.dp)
                .weight(1f),
            text = stringResource(R.string.your_triggers),
            style = MaterialTheme.typography.headlineLarge
        )

        OutlinedButton(onClick = onEditTriggers) {
            Text(
                text = stringResource(R.string.edit_triggers)
            )
        }
    }
}

/**
 * triggers content section (trigger display)
 */
@Composable
fun TriggerLayout(
    modifier: Modifier = Modifier,
    triggers: List<String>
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        FlowRow(
            horizontalArrangement = SpaceAround,
            verticalArrangement = spacedBy(8.dp)
        ) {
            for (trigger in triggers) {
                TriggerChip(
                    trigger = trigger
                )
            }
        }
    }
}


/**
 * Top row for data refresh button and no of logs info chip
 */
@Composable
fun ReloadRow(modifier: Modifier = Modifier, onRefreshData: () -> Unit, noOfLogs: Int) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = spacedBy(8.dp)
    ) {
        OutlinedButton(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            onClick = onRefreshData
        ) {
            Text(text = stringResource(R.string.reload_data_button_pt1))
            Icon(
                modifier = Modifier.size(18.dp),
                imageVector = Icons.Default.Refresh,
                contentDescription = null
            )
            Text(text = stringResource(R.string.reload_data_button_pt2))
        }
        NoOfLogsChip(noOfLogs = noOfLogs)
    }
}

/**
 * Saved trigger display item
 */
@Composable
fun TriggerChip(
    modifier: Modifier = Modifier,
    trigger: String
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            modifier = Modifier.padding(12.dp),
            text = trigger,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

/**
 * Most common factors section contents
 */
@Composable
fun CommonFactorGrid(
    modifier: Modifier = Modifier,
    factors: List<CommonFactorUIModel>
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        FlowRow(
            maxItemsInEachRow = 2,
            horizontalArrangement = spacedBy(8.dp),
            verticalArrangement = spacedBy(8.dp)
        ) {
            for (factor in factors) {
                FactorItem(
                    modifier = Modifier.weight(1f),
                    factorName = factor.name,
                    category = factor.category,
                    frequency = factor.frequency
                )
            }
        }
    }
}

/**
 * Card for each factor in the common factosr section
 */
@Composable
fun FactorItem(
    modifier: Modifier = Modifier,
    factorName: String,
    category: FactorCategory,
    frequency: Int
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = when (frequency) {
            in 70..100 -> MaterialTheme.colorScheme.primary
            in 40..69 -> MaterialTheme.colorScheme.secondary
            else -> MaterialTheme.colorScheme.tertiary
        }
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(12.dp),
                    imageVector = when (category) {
                        FactorCategory.VISUAL ->
                            Icons.Default.Visibility

                        FactorCategory.AUDITORY ->
                            Icons.AutoMirrored.Filled.VolumeUp

                        FactorCategory.TACTILE ->
                            Icons.Default.BackHand

                        FactorCategory.OLFACTORY ->
                            Icons.Default.FilterVintage

                        FactorCategory.GUSTATORY ->
                            Icons.Default.Restaurant

                        FactorCategory.INTERNAL ->
                            Icons.Default.Boy
                    },
                    contentDescription = when (category) {
                        FactorCategory.VISUAL ->
                            "Visual factor"

                        FactorCategory.AUDITORY ->
                            "Auditory factor"

                        FactorCategory.TACTILE ->
                            "Tactile factor"

                        FactorCategory.OLFACTORY ->
                            "Olfactory factor"

                        FactorCategory.GUSTATORY ->
                            "Gustatory factor"

                        FactorCategory.INTERNAL ->
                            "Internal factor"
                    }
                )
                Text(
                    modifier = Modifier
                        .padding(horizontal = 8.dp),
                    text = factorName,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(
                text = stringResource(R.string.factor_frequency_template, frequency),
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

/**
 * Display for number of logs loaded
 */
@Composable
fun NoOfLogsChip(
    modifier: Modifier = Modifier,
    noOfLogs: Int
) {
    Surface(
        modifier = modifier.border(
            1.dp,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.shapes.small
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.no_of_logs),
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = noOfLogs.toString(),
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

//PREVIEW
@Preview
@Composable
fun ComponentPreview() {
    SoaverTriggerTrackerTheme() {
        Scaffold(
            bottomBar = { MainNavigationBar() }) { padding ->
            OverviewPage(
                modifier = Modifier.padding(padding),
                factors = listOf(
                    CommonFactorUIModel(
                        name = "test fac 123",
                        category = FactorCategory.GUSTATORY,
                        frequency = 67
                    ),
                    CommonFactorUIModel(
                        name = "tefsefufgeygf",
                        category = FactorCategory.TACTILE,
                        frequency = 45
                    ),
                    CommonFactorUIModel(
                        name = "eihhi egihesghei",
                        category = FactorCategory.GUSTATORY,
                        frequency = 38
                    ),
                    CommonFactorUIModel(
                        name = "dbubdw dbd 4th",
                        category = FactorCategory.OLFACTORY,
                        frequency = 89
                    ),
                    CommonFactorUIModel(
                        name = "test fac 123",
                        category = FactorCategory.GUSTATORY,
                        frequency = 67
                    ),
                    CommonFactorUIModel(
                        name = "tefsefufgeygf",
                        category = FactorCategory.TACTILE,
                        frequency = 45
                    ),
                    CommonFactorUIModel(
                        name = "eihhi egihesghei",
                        category = FactorCategory.GUSTATORY,
                        frequency = 38
                    ),
                    CommonFactorUIModel(
                        name = "dbubdw dbd 4th",
                        category = FactorCategory.OLFACTORY,
                        frequency = 89
                    )
                ),
                triggers = listOf(
                    "Trigger 1",
                    "ej",
                    "trig2",
                    "efgfgsef",
                    "euuehfisuhfseuifhuesihsi"
                ),
                onRefreshData = {},
                onEditTriggers = {},
                noOfLogs = 2,
                factorLoadingError = false,
                triggerLoadingError = false,
                onRetryFactors = {},
                onRetryTriggers = {}
            )
        }
    }
}