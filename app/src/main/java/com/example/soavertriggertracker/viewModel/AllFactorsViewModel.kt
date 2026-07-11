package com.example.soavertriggertracker.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soavertriggertracker.R
import com.example.soavertriggertracker.data.DataLoader
import com.example.soavertriggertracker.data.Factor
import com.example.soavertriggertracker.data.FactorCategory
import com.example.soavertriggertracker.viewModel.uiDataItems.FactorOverviewUIModel
import com.example.soavertriggertracker.viewModel.uiDataItems.LogOverviewUIModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for All Factors overview page
 */
@HiltViewModel
class AllFactorsViewModel @Inject constructor(
    private val dataLoader: DataLoader,
    private val dataProcessor: CommonDataProcessing
) : ViewModel() {
    private var definedFactors: List<Factor> = listOf() //Factors from DB
    private val logsPerFactor =
        mutableMapOf<String, MutableList<LogOverviewUIModel>>() //map of logs to each factor, middle step
    private var allFactorModels = MutableStateFlow<List<FactorOverviewUIModel>>(emptyList())
    private val _selectedFilters =
        MutableStateFlow<List<FactorCategory>>(listOf<FactorCategory>())

    //exposed vals
    private val _error = MutableStateFlow(false)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val error: StateFlow<Boolean> = _error.asStateFlow()
    val selectedFilters: StateFlow<List<FactorCategory>> = _selectedFilters.asStateFlow()


    //combine search and category filters to filter stream when changes to either are made
    val factors: StateFlow<List<FactorOverviewUIModel>> = combine(
        allFactorModels,
        _searchQuery,
        _selectedFilters
    ) { factors, search, filters ->
        factors.filter { factor ->
            val searchMatches = factor.name.uppercase().contains(search.uppercase())
            val categoryMatches = filters.isEmpty() || filters.contains(factor.category)
            searchMatches && categoryMatches
        }
    }.stateIn(//state settings
        scope = viewModelScope, //lifecycle
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(newText: String) {
        _searchQuery.value = newText
    }

    fun reloadData() {
        viewModelScope.launch {
            dataLoader.reloadFactors()
            dataLoader.reloadLogs()
            loadData()
        }
    }

    //setup - load factors and start mapping logs
    init {
        logsPerFactor.clear()
        loadData()

    }


    /**
     * Fetch all factors and logs and sort them in a single pass through logs
     */
    private fun loadData() {
        definedFactors = dataLoader.getFactors()
        //create a list for each factor (ensures factors with lo logs still show)
        for (factor in definedFactors) {
            logsPerFactor[factor.name] = mutableListOf()
        }

        val logs = dataLoader.getLogs()
        val logUImodels = dataProcessor.logToUIItemMap(
            logs,
            R.string.logs_in_factor_card_title
        )

        //sort logs into factors
        for (log in logUImodels) {
            for (factorName in log.factorsPresent) {
                logsPerFactor.getOrPut(factorName) { mutableListOf() }
                    .add(log)//adds new list if not present, adds to factor list
            }
        }

        //set the full list
        allFactorModels.value = mapToFactorUIModels(logs.size)
        _error.value =
            dataLoader.factorLoadingError() != null || dataLoader.logLoadingError() != null

    }

    /**
     * Manages currently selected categories
     */
    fun onFilterChange(category: FactorCategory) {
        _selectedFilters.update {
            if (_selectedFilters.value.contains(category)) {
                _selectedFilters.value - category
            } else {
                _selectedFilters.value + category
            }
        }
    }

    /**
     * attaches mapped logUI in logsPerFactor to factors and returns a list of factorUI models
     */
    private fun mapToFactorUIModels(
        noOfLogs: Int
    ): List<FactorOverviewUIModel> {
        return definedFactors.map { factor ->
            val factorLogs = logsPerFactor[factor.name]
                ?: emptyList() //gets existing list of logs or makes empty

            //calculate frequency as int percentage, set 0 if no logs
            val frequency = if (factorLogs.isNotEmpty() && noOfLogs > 0) {
                (factorLogs.size * 100) / noOfLogs
            } else 0

            FactorOverviewUIModel(
                name = factor.name,
                frequency = frequency,
                logsPresentIn = factorLogs,
                category = factor.category
            )
        }
    }
}