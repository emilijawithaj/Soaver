package com.example.soavertriggertracker.data

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataLoader @Inject constructor(
    private val repository: LogRepository,
    private val triggerDBLink: TriggerSupabaseLink
) {
    private var logs: List<Log> = emptyList()
    private var factors: List<Factor> = emptyList()
    private var logError: Exception? = null
    private var factorError: Exception? = null
    private var triggers: List<String> = emptyList()
    private var triggerError: Exception? = null

    /**
     * attempts to load logs, factors and triggers from the repository,
     * puts any exceptions thrown in respective errors
     */
    suspend fun loadAll() {
        try {
            logs = repository.getLogs()
            logError = null
        } catch (e: Exception) {
            logError = e
        }

        try {
            factors = repository.getFactors()
            factorError = null
        } catch (e: Exception) {
            factorError = e
        }

        try {
            triggers = triggerDBLink.getTriggers()
            triggerError = null
        } catch (e: Exception) {
            triggerError = e
        }
    }


    /**
     * attempts to load logs from the repository, putting any exceptions in logError
     */
    suspend fun reloadLogs() {
        try {
            logs = repository.getLogs()
            logError = null
        } catch (e: Exception) {
            logError = e
        }
    }

    /**
     * attempts to load factors from the repository, putting any exceptions in factorError
     */
    suspend fun reloadFactors() {
        try {
            factors = repository.getFactors()
            factorError = null
        } catch (e: Exception) {
            factorError = e
        }
    }

    /**
     * attempts to load triggers from the repository, putting any exceptions in triggerError
     */
    suspend fun reloadTriggers() {
        try {
            triggers = triggerDBLink.getTriggers()
            triggerError = null
        } catch (e: Exception) {
            triggerError = e
        }
    }

    /**
     * returns loaded logs
     */
    fun getLogs(): List<Log> {
        return logs
    }

    /**
     * returns loaded factors
     */
    fun getFactors(): List<Factor> {
        return factors
    }

    /**
     * returns loaded triggers
     */
    fun getTriggers(): List<String> {
        return triggers
    }

    /**
     * returns exception thrown on last log load attempt, or null if none thrown
     */
    fun logLoadingError(): Exception? {
        return logError
    }

    /**
     * returns exception thrown on last factor load attempt, or null if none thrown
     */
    fun factorLoadingError(): Exception? {
        return factorError
    }

    /**
     * returns exception thrown on last trigger load attempt, or null if none thrown
     */
    fun triggerLoadingError(): Exception? {
        return triggerError
    }
}