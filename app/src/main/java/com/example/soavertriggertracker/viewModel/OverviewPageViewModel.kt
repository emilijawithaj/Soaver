package com.example.soavertriggertracker.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soavertriggertracker.data.DataLoader
import com.example.soavertriggertracker.viewModel.uiDataItems.CommonFactorUIModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OverviewPageViewModel @Inject constructor(
    private val dataLoader: DataLoader
) : ViewModel() {
    private var _triggers = MutableStateFlow<List<String>>(listOf())
    private var _noOfLogs = MutableStateFlow(0)
    private var _commonFactors = MutableStateFlow<List<CommonFactorUIModel>>(listOf())
    private var _triggerError = MutableStateFlow(false)
    private var _factorError = MutableStateFlow(false)


    //exposed
    val triggers: StateFlow<List<String>> = _triggers.asStateFlow()
    val noOfLogs: StateFlow<Int> = _noOfLogs.asStateFlow()
    val commonFactors: StateFlow<List<CommonFactorUIModel>> = _commonFactors.asStateFlow()
    val triggerError: StateFlow<Boolean> = _triggerError.asStateFlow()
    val factorError: StateFlow<Boolean> = _factorError.asStateFlow()
    fun onReload() {
        viewModelScope.launch {
            dataLoader.loadAll()
            load()
        }
    }
    fun onEditTriggers() {
        /* TODO */
    }
    fun onReloadTriggers() {
        viewModelScope.launch {
            dataLoader.reloadTriggers()
            load()
        }
    }
    fun onReloadFactors() {
        viewModelScope.launch {
            dataLoader.reloadFactors()
            load()
        }
    }



    /**
     * load data on init
     */
    init {
        load()
    }

    /**
     * load data from data loader and call to calculate and set common factors
     */
    private fun load() {
        _triggerError.value = dataLoader.triggerLoadingError() != null
        _factorError.value =
            dataLoader.factorLoadingError() != null || dataLoader.logLoadingError() != null
        _triggers.value = dataLoader.getTriggers()
        _noOfLogs.value = dataLoader.getLogs().size
        setCommonFactors()
    }

    /**
     * filters the 6 most common factors (>0% frequency)
     * todo could include tags?
     */
    private fun setCommonFactors() {
        val allFactors = dataLoader.getFactors()
        val logs = dataLoader.getLogs()

        //map logs to factors
        val factorMap = mutableMapOf<String, Int>()
        for (factor in allFactors) {
            factorMap[factor.name] = 0
        }

        //count factor occurences by sweeping each record in each log
        for (log in logs) {
            for (factorRec in log.factorRecords) {
                if (factorRec.wasPresent) { //add only if wasPresent
                    factorMap[factorRec.factorName] = factorMap[factorRec.factorName]!! + 1
                }
            }
        }

        //convert in map values to percentage
        for (factor in allFactors) {
            if (factorMap[factor.name]!! > 0 && logs.isNotEmpty()) {
                factorMap[factor.name] = (factorMap[factor.name]!! * 100) / logs.size
            }
        }

        //order and select top 8 todo could be over x% instead?
        _commonFactors.value = allFactors.map { factor ->
            CommonFactorUIModel(
                name = factor.name,
                frequency = factorMap[factor.name]!!,
                category = factor.category
            )
        }.filter { it.frequency > 0 }
            .sortedByDescending { it.frequency }.take(8)
    }
}