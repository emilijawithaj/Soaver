package com.example.soavertriggertracker.viewModel

/**
 * Data template for log passing to the UI
 */
data class LogOverviewUIModel(
    val title: String,
    val records: List<String>,
    val tags: List<String>,
    val logID: String,
)