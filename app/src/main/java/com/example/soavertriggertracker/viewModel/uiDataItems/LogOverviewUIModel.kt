package com.example.soavertriggertracker.viewModel.uiDataItems

/**
 * Data template for log passing to the UI
 */
data class LogOverviewUIModel(
    val title: String,
    val factorsPresent: List<String>,
    val tags: List<String>,
    val logID: String,
)