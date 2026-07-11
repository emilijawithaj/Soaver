package com.example.soavertriggertracker.view

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.soavertriggertracker.MainNavigationBar
import com.example.soavertriggertracker.MainNavigationRail
import com.example.soavertriggertracker.R
import com.example.soavertriggertracker.ui.theme.SoaverTriggerTrackerTheme
import com.example.soavertriggertracker.viewModel.AllLogsViewModel
import com.example.soavertriggertracker.viewModel.uiDataItems.LogOverviewUIModel

object AllLogsView {
    @Composable
    fun AllLogsScreen(
        viewModel: AllLogsViewModel = hiltViewModel(),
        windowSize: WindowSizeClass
    ) {
        val logs by viewModel.logItems.collectAsStateWithLifecycle()
        val loadingError by viewModel.error.collectAsStateWithLifecycle()
        val searchText by viewModel.searchQuery.collectAsStateWithLifecycle()
        val onSearchTextChange = viewModel::onSearchQueryChange
        val onRetryLoad = viewModel::reloadLogs

        when (windowSize.widthSizeClass) {
            WindowWidthSizeClass.Compact ->
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { MainNavigationBar() }) { padding ->
                    LogsScreen(
                        modifier = Modifier.padding(padding),
                        logs = logs,
                        loadingError = loadingError,
                        searchText = searchText,
                        onSearchTextChange = onSearchTextChange,
                        onRetryLoad = onRetryLoad
                    )
                }

            else -> {
                Row() {
                    MainNavigationRail()
                    LogsScreen(
                        modifier = Modifier.weight(1f),
                        logs = logs,
                        loadingError = loadingError,
                        searchText = searchText,
                        onSearchTextChange = onSearchTextChange,
                        onRetryLoad = onRetryLoad
                    )
                }

            }
        }
    }

    @Composable
    fun LogsScreen(
        modifier: Modifier = Modifier,
        logs: List<LogOverviewUIModel> = listOf(),
        loadingError: Boolean,
        searchText: String,
        onSearchTextChange: (String) -> Unit,
        onRetryLoad: () -> Unit
    ) {

        Column(
            modifier = modifier
                .padding(10.dp),
            horizontalAlignment = CenterHorizontally
        ) {
            SearchBar(
                modifier = Modifier.fillMaxWidth(),
                searchText = searchText,
                onSearchTextChange = onSearchTextChange,
                searchPlaceholder = stringResource(R.string.search_logs_field)
            )
            if (loadingError) {
                Column(
                    modifier = modifier.padding(16.dp),
                ) {
                    Text(
                        modifier = Modifier.align(CenterHorizontally),
                        text = stringResource(R.string.error_loading_logs),
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
            } else if (logs.isEmpty()) {
                Text(
                    modifier = modifier.padding(16.dp),
                    text = stringResource(R.string.no_logs_found),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            } else {
                LogCardColumn(
                    modifier = Modifier.weight(1f),
                    logs = logs
                )
            }
        }

    }

    /**
     * Lazy column of all logs.
     * Holds and controls setting expanding.
     */
    @Composable
    fun LogCardColumn(
        modifier: Modifier = Modifier,
        logs: List<LogOverviewUIModel> = listOf(),
    ) {
        var expanded by
        rememberSaveable { mutableStateOf(setOf<String>()) } //holds currently expanded cards

        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(vertical = 12.dp),
            color = MaterialTheme.colorScheme.background
        ) {

            //list of log cards
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs) { log -> //items = items in the column, any component must be item
                    LogCard(
                        modifier = Modifier,
                        text = log.title,
                        expanded = expanded.contains(log.title),
                        onCardClick = {
                            if (expanded.contains(log.title)) {
                                expanded -= log.title //(remove)
                            } else {
                                expanded += log.title
                            }
                        },
                        logFactorRecords = log.factorsPresent,
                        editButtonOnClick = { /*TODO*/ },
                        logTags = log.tags
                    )
                }
            }
        }
    }


    /**
     * Log card individual expanding log.
     * Expanding controlled and display data fed from above.
     */
    @Composable
    fun LogCard(
        modifier: Modifier = Modifier,
        text: String,
        expanded: Boolean = false,
        onCardClick: () -> Unit,
        logFactorRecords: List<String>,
        editButtonOnClick: () -> Unit,
        logTags: List<String>
    ) {

        //whole thing
        Surface(
            modifier = modifier
                .semantics { //accessibility settings for onClickable
                    contentDescription = if (!expanded) { //description
                        "Expand"
                    } else {
                        "Shrink"
                    }
                    role = Role.Button
                },
            color = MaterialTheme.colorScheme.primary,
            shape = MaterialTheme.shapes.extraSmall,
            onClick = { onCardClick() }
        ) {

            Column(
                modifier = modifier
                    .animateContentSize()
            ) {
                LogCardMainDisplay(
                    text = text,
                    expanded = expanded,
                    editButtonOnClick = editButtonOnClick
                )

                //details
                if (expanded) {
                    LogCardDetails(
                        modifier = Modifier,
                        logFactorRecords = logFactorRecords,
                        logTags = logTags
                    )
                }
            }
        }
    }

    @Composable
    fun LogCardMainDisplay(
        modifier: Modifier = Modifier,
        text: String,
        expanded: Boolean,
        editButtonOnClick: () -> Unit
    ) {
        Row(
            modifier = modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = CenterVertically,
        )
        {

            Icon(
                imageVector = if (!expanded) {
                    Icons.Filled.ExpandMore
                } else {
                    Icons.Filled.ExpandLess
                },
                contentDescription = null
            )
            Text(
                text = text,
                modifier = modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                style = MaterialTheme.typography.titleMedium
            )
            ElevatedButton(
                onClick = { editButtonOnClick() }) {
                Text(text = stringResource(R.string.edit))
            }
        }
    }


    @Composable
    fun LogCardDetails(
        modifier: Modifier = Modifier,
        logFactorRecords: List<String>,
        logTags: List<String>
    ) {
        Column(
            modifier = modifier
                .padding(bottom = 16.dp)
                .padding(horizontal = 16.dp)
        ) {
            for (i in logFactorRecords) {
                Text(
                    text = i,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
            Row(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .horizontalScroll(
                        enabled = true,
                        state = rememberScrollState()
                    )
            ) {
                for (i in logTags) {
                    Card(shape = MaterialTheme.shapes.extraSmall) {
                        Text(
                            text = i,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

//previewer
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun AllLogsPreview() {
    SoaverTriggerTrackerTheme {
        Column {
            OutlinedButton(
                modifier = Modifier.padding(10.dp),
                onClick = {}) {
                Text(text = "Retry")
            }
            FilledTonalButton(
                modifier = Modifier.padding(10.dp),
                onClick = {}
            ) {
                Text(text = "Retry")
            }
        }
    }
}


/**
 * Search bar for filtering logs
 */
@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    searchPlaceholder: String
) {
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = searchText,
        placeholder = { Text(searchPlaceholder) }, //placeholder needs a Text built within it
        onValueChange = onSearchTextChange,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null
            )
        }
    )
}


