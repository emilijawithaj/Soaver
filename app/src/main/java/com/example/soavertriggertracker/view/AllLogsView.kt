package com.example.soavertriggertracker.view

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.example.soavertriggertracker.MainNavigationBar
import com.example.soavertriggertracker.MainNavigationRail
import com.example.soavertriggertracker.R
import com.example.soavertriggertracker.ui.theme.SoaverTriggerTrackerTheme

class AllLogsView {

    @Composable
    fun AllLogsScreen(
        titles: List<String> = listOf("test1", "test2", "test3"), //todo real data
        logFactorRecords: List<String> = listOf("test1", "test2", "test3"), //todo real data
        windowSize: WindowSizeClass
    ) {
        when (windowSize.widthSizeClass) {
            WindowWidthSizeClass.Compact ->
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { MainNavigationBar() }) { padding ->
                    LogsScreen(
                        modifier = Modifier.padding(padding),
                        titles = titles,
                        logFactorRecords = logFactorRecords
                    )
                }

            else -> {
                Row() {
                    MainNavigationRail()
                    LogsScreen(
                        modifier = Modifier.weight(1f),
                        titles = titles,
                        logFactorRecords = logFactorRecords
                    )
                }

            }
        }
    }

    @Composable
    fun LogsScreen(
        modifier: Modifier = Modifier,
        titles: List<String>,
        logFactorRecords: List<String>
    ) {

        Column(
            modifier = modifier
                .padding(10.dp)
        ) {
            SearchBar(modifier = Modifier.fillMaxWidth())
            LogCardColumn(
                modifier = Modifier.weight(1f),
                titles = titles,
                logFactorRecords = logFactorRecords
            )
        }

    }

    /**
     * Lazy column of all logs.
     * Holds and controls setting expanding.
     */
    @Composable
    fun LogCardColumn(
        modifier: Modifier = Modifier,
        titles: List<String>,
        logFactorRecords: List<String>
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
                items(titles) { title -> //items = items in the column, any component must be item
                    LogCard(
                        modifier = Modifier,
                        text = title,
                        expanded = expanded.contains(title),
                        onCardClick = {
                            if (expanded.contains(title)) {
                                expanded -= title //(remove)
                            } else {
                                expanded += title
                            }
                        },
                        logFactorRecords = logFactorRecords,
                        editButtonOnClick = { /*TODO*/ }
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
        editButtonOnClick: () -> Unit
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
                    Column() {
                        for (i in logFactorRecords) {
                            Text(
                                text = i,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
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
                style = MaterialTheme.typography.headlineMedium
            )
            ElevatedButton(
                onClick = { editButtonOnClick() }) {
                Text(text = stringResource(R.string.edit))
            }
        }
    }

    /**
     * Search bar for filtering logs TODO implement functionality
     */
    @Composable
    fun SearchBar(modifier: Modifier = Modifier) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = stringResource(R.string.empty_string),
            placeholder = { Text(stringResource(R.string.search_logs_field)) }, //placeholder needs a Text built within it
            onValueChange = {},
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            }
        )
    }


    //previewer
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
    @Composable
    fun AllLogsPreview() {
        SoaverTriggerTrackerTheme {
            AllLogsView().AllLogsScreen(
                windowSize =
                    WindowSizeClass.calculateFromSize(DpSize(411.dp, 891.dp))
            )
        }
    }
}
