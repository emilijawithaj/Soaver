package com.example.soavertriggertracker.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soavertriggertracker.R
import com.example.soavertriggertracker.data.DataLoader
import com.example.soavertriggertracker.data.Log
import com.example.soavertriggertracker.viewModel.uiDataItems.LogOverviewUIModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the AllLogsView (Page list of logs)
 */
@HiltViewModel
class AllLogsViewModel @Inject constructor(
    private val dataLoader: DataLoader,
    private val dataProcessor: CommonDataProcessing
) : ViewModel() {
    private var logs: List<Log> = listOf()
    private val _logItems = MutableStateFlow<List<LogOverviewUIModel>>(emptyList())

    private val _searchQuery = MutableStateFlow("")
    private val _error = MutableStateFlow(false)

    //exposed vals
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val logItems: StateFlow<List<LogOverviewUIModel>> =
        _logItems.asStateFlow()//read only stateflow list

    fun onSearchQueryChange(newText: String) {
        _searchQuery.value = newText
        siftLogs(newText)
    }

    val error: StateFlow<Boolean> = _error.asStateFlow()


    init { //load logs on init
        loadLogs()
    }


    /**
     * get logs from db
     */
    fun loadLogs() {
        viewModelScope.launch {
            logs = dataLoader.getLogs()
            _logItems.value = dataProcessor.logToUIItemMap(logs, R.string.all_logs_page_log_titles)
            _error.value = dataLoader.logLoadingError() != null
        }
    }


    private fun siftLogs(search: String) {
        _logItems.value =
            dataProcessor.logToUIItemMap(logs, R.string.all_logs_page_log_titles).filter {
                it.title.uppercase().contains(search.uppercase())
                        || it.records.any { rec ->
                    rec.uppercase().contains(search.uppercase())
                }
                        || it.tags.any { rec -> rec.uppercase().contains(search.uppercase()) }
            }
    }
}