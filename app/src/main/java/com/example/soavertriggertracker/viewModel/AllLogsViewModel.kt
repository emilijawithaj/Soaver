package com.example.soavertriggertracker.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soavertriggertracker.R
import com.example.soavertriggertracker.data.Log
import com.example.soavertriggertracker.data.LogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

/**
 * ViewModel for the AllLogsView (Page list of logs)
 */
@HiltViewModel
class AllLogsViewModel @Inject constructor(
    private val repository: LogRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private var logs: List<Log> = listOf()
    private val _logItems = MutableStateFlow<List<LogOverviewUIModel>>(emptyList())
    val logItems: StateFlow<List<LogOverviewUIModel>> =
        _logItems.asStateFlow()//read only stateflow list

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    fun onSearchQueryChange(newText: String) {
        _searchQuery.value = newText
        siftLogs(newText)
    }

    private val _error = MutableStateFlow(false)
    val error: StateFlow<Boolean> = _error.asStateFlow()


    init { //load logs on init
        loadLogs()
    }


    /**
     * get logs from db
     */
    fun loadLogs() {
        viewModelScope.launch {
            try {
                logs = repository.getLogs()
                _logItems.value = logToUIItemMapper(logs)
                _error.value = false
            } catch (e: Exception) {
                _error.value = true
            }
        }
    }

    fun logToUIItemMapper(logsToConvert: List<Log>): List<LogOverviewUIModel> {
        return logsToConvert.map { log ->
            val localDateTime = log.datetime.toLocalDateTime(TimeZone.currentSystemDefault())
            val dateNice =
                "${localDateTime.day}/${localDateTime.month.number}/${localDateTime.year}"
            val timeNice = "${localDateTime.hour}:${localDateTime.minute}"
            LogOverviewUIModel(
                title = "${context.getString(R.string.log_from)} $dateNice at $timeNice",
                records = log.factorRecords.filter { it.wasPresent }
                    .map { it.factorName }, //add only present factors
                tags = log.tags.map { it.value },
                logID = log.id!!
            )
        }
    }

    fun siftLogs(search: String) {
        _logItems.value = logToUIItemMapper(logs).filter {
            it.title.uppercase().contains(search.uppercase())
                    || it.records.any { rec -> rec.uppercase().contains(search.uppercase()) }
                    || it.tags.any { rec -> rec.uppercase().contains(search.uppercase()) }
        }
    }
}