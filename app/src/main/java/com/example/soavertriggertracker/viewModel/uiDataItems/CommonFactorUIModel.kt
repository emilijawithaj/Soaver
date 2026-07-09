package com.example.soavertriggertracker.viewModel.uiDataItems

import com.example.soavertriggertracker.data.FactorCategory

data class CommonFactorUIModel(
    val name: String,
    val frequency: Int,
    val category: FactorCategory
)