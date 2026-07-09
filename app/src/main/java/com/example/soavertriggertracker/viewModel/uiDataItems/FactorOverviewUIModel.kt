package com.example.soavertriggertracker.viewModel.uiDataItems

import com.example.soavertriggertracker.data.FactorCategory

data class FactorOverviewUIModel(
    val name: String,
    val frequency: Int,
    val logsPresentIn: List<LogOverviewUIModel>,
    val category: FactorCategory
)