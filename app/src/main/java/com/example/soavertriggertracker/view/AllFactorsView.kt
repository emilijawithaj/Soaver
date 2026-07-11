package com.example.soavertriggertracker.view

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BackHand
import androidx.compose.material.icons.filled.Boy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterVintage
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.soavertriggertracker.MainNavigationBar
import com.example.soavertriggertracker.MainNavigationRail
import com.example.soavertriggertracker.R
import com.example.soavertriggertracker.data.FactorCategory
import com.example.soavertriggertracker.ui.theme.SoaverTriggerTrackerTheme
import com.example.soavertriggertracker.view.AllFactorsView.FactorsScreen
import com.example.soavertriggertracker.view.AllFactorsView.FilterRow
import com.example.soavertriggertracker.viewModel.AllFactorsViewModel
import com.example.soavertriggertracker.viewModel.uiDataItems.FactorOverviewUIModel
import com.example.soavertriggertracker.viewModel.uiDataItems.LogOverviewUIModel

object AllFactorsView {
    @Composable
    fun AllFactorsScreen(
        viewModel: AllFactorsViewModel = hiltViewModel(),
        windowSize: WindowSizeClass
    ) {
        val factors by viewModel.factors.collectAsStateWithLifecycle()
        val searchText by viewModel.searchQuery.collectAsStateWithLifecycle()
        val onSearchTextChange = viewModel::onSearchQueryChange
        val loadingError by viewModel.error.collectAsStateWithLifecycle()
        val onRetryLoad = viewModel::reloadData
        val filtersSelected by viewModel.selectedFilters.collectAsStateWithLifecycle()
        val onFilterChange = viewModel::onFilterChange

        when (windowSize.widthSizeClass) {
            WindowWidthSizeClass.Compact ->
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { MainNavigationBar() }) { padding ->
                    FactorsScreen(
                        modifier = Modifier.padding(padding),
                        factors = factors,
                        searchText = searchText,
                        onSearchTextChange = onSearchTextChange,
                        loadingError = loadingError,
                        onRetryLoad = onRetryLoad,
                        filtersSelected = filtersSelected,
                        onFilterChange = onFilterChange
                    )
                }

            else -> {
                Row() {
                    MainNavigationRail()
                    FactorsScreen(
                        modifier = Modifier.weight(1f),
                        factors = factors,
                        searchText = searchText,
                        onSearchTextChange = onSearchTextChange,
                        loadingError = loadingError,
                        onRetryLoad = onRetryLoad,
                        filtersSelected = filtersSelected,
                        onFilterChange = onFilterChange
                    )
                }
            }
        }
    }

    @Composable
    fun FactorsScreen(
        modifier: Modifier = Modifier,
        factors: List<FactorOverviewUIModel> = listOf(),
        searchText: String,
        onSearchTextChange: (String) -> Unit,
        loadingError: Boolean,
        onRetryLoad: () -> Unit,
        filtersSelected: List<FactorCategory>,
        onFilterChange: (FactorCategory) -> Unit
    ) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = modifier
                    .padding(10.dp),
                horizontalAlignment = CenterHorizontally,
            ) {
                SearchBar(
                    modifier = Modifier.fillMaxWidth(),
                    searchText = searchText,
                    onSearchTextChange = onSearchTextChange,
                    searchPlaceholder = stringResource(R.string.search_factors)
                )
                FilterRow(
                    modifier = Modifier.padding(top = 10.dp),
                    selected = filtersSelected,
                    onSelectedChange = onFilterChange
                )
                if (loadingError) {
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
                            onClick = onRetryLoad
                        ) {
                            Text(text = stringResource(R.string.retry))
                        }
                    }
                } else if (factors.isEmpty()) {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = stringResource(R.string.no_factors_found_error),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                } else {
                    FactorCardColumn(
                        modifier = Modifier.weight(1f),
                        factors = factors
                    )
                }
            }
        }

    }

    @Composable
    fun FactorCardColumn(
        modifier: Modifier = Modifier,
        factors: List<FactorOverviewUIModel>,
    ) {
        var expanded by rememberSaveable { mutableStateOf(setOf<String>()) }

        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(factors, key = { it.name }) {
                    FactorCard(
                        facName = it.name,
                        facFrequency = it.frequency,
                        logs = it.logsPresentIn,
                        modifier = Modifier,
                        expanded = expanded.contains(it.name),
                        category = it.category,
                        onCardClick = {
                            if (expanded.contains(it.name)) {
                                expanded -= it.name
                            } else {
                                expanded += it.name
                            }
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun FactorCard(
        modifier: Modifier = Modifier,
        expanded: Boolean,
        onCardClick: () -> Unit,
        facName: String,
        facFrequency: Int,
        category: FactorCategory,
        logs: List<LogOverviewUIModel>
    ) {
        Surface(
            modifier = modifier
                .semantics {
                    contentDescription = if (!expanded) { //description
                        "Expand"
                    } else {
                        "Shrink"
                    }
                    role = Role.Button
                },
            color = MaterialTheme.colorScheme.secondary,
            shape = MaterialTheme.shapes.extraSmall,
            onClick = { onCardClick() },
        ) {
            Column(
                modifier = Modifier
                    .animateContentSize()
            ) {
                FactorCardTitle(
                    factorName = facName,
                    frequency = facFrequency,
                    expanded = expanded,
                    category = category
                )

                //details
                if (expanded) {
                    FactorCardDetails(logs = logs)
                }
            }
        }
    }

    @Composable
    fun FactorCardTitle(
        modifier: Modifier = Modifier,
        factorName: String,
        category: FactorCategory,
        frequency: Int,
        expanded: Boolean
    ) {
        Row(
            modifier = modifier
                .padding(16.dp)
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (!expanded) {
                    Icons.Filled.ExpandMore
                } else {
                    Icons.Filled.ExpandLess
                },
                contentDescription = null
            )
            Icon(
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
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                text = factorName,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = stringResource(R.string.frequency_in_factors, frequency),
                style = MaterialTheme.typography.titleLarge
            )
        }
    }

    @Composable
    fun FactorCardDetails(
        modifier: Modifier = Modifier,
        logs: List<LogOverviewUIModel>,

        ) {
        Row(
            modifier = modifier
                .padding(horizontal = 10.dp)
                .horizontalScroll(enabled = true, state = rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            logs.forEach {
                LogSummaryCard(
                    logText = it.title,
                    logPresent = it.factorsPresent + it.tags
                )
            }
        }
    }

    /**
     * Cards of each log
     */
    @Composable
    fun LogSummaryCard(
        modifier: Modifier = Modifier,
        logText: String,
        logPresent: List<String>
    ) {
        Surface(
            modifier = modifier
                .padding(bottom = 12.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shape = MaterialTheme.shapes.small,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            onClick = {/*TODO map to edit*/ }
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .padding(vertical = 2.dp)
            ) {
                Text(
                    text = logText,
                    modifier = Modifier
                        .align(CenterHorizontally)
                        .padding(4.dp),
                    style = MaterialTheme.typography.titleMedium
                )
                LogDetails(
                    modifier = Modifier.align(Alignment.Start),
                    logPresent = logPresent
                )
            }
        }
    }

    /**
     * Log factors
     */
    @Composable
    fun LogDetails(
        modifier: Modifier = Modifier,
        logPresent: List<String>
    ) {
        Column(
            modifier = modifier
                .padding(8.dp)
        ) {
            for (i in logPresent) {
                Text(
                    text = i,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }

    @Composable
    fun FilterRow(
        modifier: Modifier = Modifier,
        selected: List<FactorCategory>,
        onSelectedChange: (FactorCategory) -> Unit
    ) {
        //row of icons for each type, selected split out here
        Row(modifier = modifier) {
            FilterIcon(
                modifier = Modifier.weight(1f),
                type = FactorCategory.VISUAL,
                selected = selected.contains(FactorCategory.VISUAL),
                onClick = onSelectedChange
            )
            FilterIcon(
                modifier = Modifier.weight(1f),
                type = FactorCategory.AUDITORY,
                selected = selected.contains(FactorCategory.AUDITORY),
                onClick = onSelectedChange
            )
            FilterIcon(
                modifier = Modifier.weight(1f),
                type = FactorCategory.OLFACTORY,
                selected = selected.contains(FactorCategory.OLFACTORY),
                onClick = onSelectedChange
            )
            FilterIcon(
                modifier = Modifier.weight(1f),
                type = FactorCategory.GUSTATORY,
                selected = selected.contains(FactorCategory.GUSTATORY),
                onClick = onSelectedChange
            )
            FilterIcon(
                modifier = Modifier.weight(1f),
                type = FactorCategory.TACTILE,
                selected = selected.contains(FactorCategory.TACTILE),
                onClick = onSelectedChange
            )
            FilterIcon(
                modifier = Modifier.weight(1f),
                type = FactorCategory.INTERNAL,
                selected = selected.contains(FactorCategory.INTERNAL),
                onClick = onSelectedChange
            )
        }
    }

    /**
     * Cards for the factor category filters, set by passing in factor type
     */
    @Composable
    fun FilterIcon(
        modifier: Modifier = Modifier,
        type: FactorCategory,
        selected: Boolean,
        onClick: (FactorCategory) -> Unit
    ) {
        //surface on click and selected style changes
        Surface(
            modifier = modifier
                .padding(2.dp)
                .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
                .semantics { contentDescription = "toggle filter by ${type.name}" },
            color = when (selected) {
                true -> MaterialTheme.colorScheme.primary
                false -> MaterialTheme.colorScheme.background
            },
            shape = MaterialTheme.shapes.small,
            onClick = { onClick(type) }
        )
        {
            Column(
                modifier = Modifier,
                horizontalAlignment = CenterHorizontally
            ) {

                //Icon and text for a factor type
                Icon(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .background(
                            when (selected) {
                                true -> MaterialTheme.colorScheme.primary
                                false -> MaterialTheme.colorScheme.primaryContainer
                            },
                            MaterialTheme.shapes.extraLarge
                        )
                        .size(32.dp),
                    imageVector = when (type) {
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
                    }, //icon setting
                    contentDescription = when (type) {
                        FactorCategory.VISUAL ->
                            "Visual factor filter"

                        FactorCategory.AUDITORY ->
                            "Auditory factor filter"

                        FactorCategory.TACTILE ->
                            "Tactile factor filter"

                        FactorCategory.OLFACTORY ->
                            "Olfactory factor filter"

                        FactorCategory.GUSTATORY ->
                            "Gustatory factor filter"

                        FactorCategory.INTERNAL ->
                            "Internal factor filter"
                    } //content description setting
                )
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = when (type) {
                        FactorCategory.VISUAL ->
                            "Sight"

                        FactorCategory.AUDITORY ->
                            "Sound"

                        FactorCategory.TACTILE ->
                            "Touch"

                        FactorCategory.OLFACTORY ->
                            "Smell"

                        FactorCategory.GUSTATORY ->
                            "Taste"

                        FactorCategory.INTERNAL ->
                            "Other"
                    },
                    style = MaterialTheme.typography.bodySmall
                ) //title of factor category setting
            }
        }
    }
}

//filter working preview
@Preview
@Composable
fun CategorySortPreview() {
    SoaverTriggerTrackerTheme {
        FilterRow(
            selected = listOf(
                FactorCategory.AUDITORY,
                FactorCategory.VISUAL,
                FactorCategory.GUSTATORY
            ),
            onSelectedChange = {}
        )
    }
}

//Full screen preview
@Preview
@Composable
fun Preview() {
    SoaverTriggerTrackerTheme {
        FactorsScreen(
            factors = listOf(
                FactorOverviewUIModel(
                    "test",
                    80,
                    listOf(
                        LogOverviewUIModel(
                            "22/09/2026 18:00",
                            listOf("test factor long"),
                            listOf("test tag"),
                            "123"
                        ),
                        LogOverviewUIModel(
                            "22/09/2026 18:00",
                            listOf("test factor long"),
                            listOf("test tag"),
                            "123"
                        ),
                        LogOverviewUIModel(
                            "22/09/2026 18:00",
                            listOf("test factor long"),
                            listOf("test tag"),
                            "123"
                        )
                    ),
                    category = FactorCategory.AUDITORY
                )
            ),
            searchText = "",
            onSearchTextChange = {},
            loadingError = false,
            onRetryLoad = {},
            filtersSelected = listOf(FactorCategory.AUDITORY),
            onFilterChange = {}
        )
    }
}