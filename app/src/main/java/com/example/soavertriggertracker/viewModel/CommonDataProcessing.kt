package com.example.soavertriggertracker.viewModel

import android.content.Context
import com.example.soavertriggertracker.data.Log
import com.example.soavertriggertracker.viewModel.uiDataItems.LogOverviewUIModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper 'injectable utility' class for common ViewModel data transformation
 * and calculation needs (frequency calculations)
 */
@Singleton
class CommonDataProcessing@Inject
constructor( @ApplicationContext private val context: Context) {

    /**
     * Map logs to UI models.
     * Title format must be a resource id to a string with placeholders for date and time.
     */
    fun logToUIItemMap (logsToConvert: List<Log>, titleFormat: Int): List<LogOverviewUIModel> {
        return logsToConvert.map { log ->
            val localDateTime = log.datetime.toLocalDateTime(TimeZone.currentSystemDefault())
            val dateNice =
                "${localDateTime.day}/${localDateTime.month.number}/${localDateTime.year}"
            val timeNice = "${localDateTime.hour}:${localDateTime.minute}"
            LogOverviewUIModel(
                title = (context.getString(titleFormat, dateNice, timeNice)),
                factorsPresent = log.factorRecords.filter { it.wasPresent }
                    .map { it.factorName }, //add only present factors
                tags = log.tags.map { it.value },
                logID = log.id!!
            )
        }
    }
}