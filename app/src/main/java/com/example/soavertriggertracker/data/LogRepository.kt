package com.example.soavertriggertracker.data

import javax.inject.Inject

interface LogRepository {
    suspend fun getLogs(): List<Log>
    suspend fun putLog(log: Log): String
    suspend fun getLog(id: String): Log?
    suspend fun deleteLog(id: String)

    //suspend fun updateLog(log: Log)
    suspend fun putFactor(factor: Factor): String
    suspend fun getFactor(id: String): Factor?
    suspend fun getFactors(): List<Factor>
    suspend fun deleteFactor(id: String)
}

class LogRepositoryImpl @Inject constructor(
    private val supabaseLink: LogSupabaseLink //default singleton due to link in BindingModule
) : LogRepository {

    /**
     * Gets all logs or empty list if none found
     */
    override suspend fun getLogs(): List<Log> {
        val logDTOs = supabaseLink.getLogsDTO()
        return logDTOs.map { it.toDomain() }
        //catch here?
    }

    /**
     * Attempts to insert a log into the db
     * @return id of inserted log
     */
    override suspend fun putLog(log: Log): String {
        val logDTO = log.toDTO()
        return supabaseLink.putLogDTO(logDTO)
    }

    /**
     * Gets a specific log from db by id
     */
    override suspend fun getLog(id: String): Log? {
        val logDTO = supabaseLink.getLogDTO(id) ?: return null
        return logDTO.toDomain()
    }

    /**
     * Removes log by id if present
     */
    override suspend fun deleteLog(id: String) {
        supabaseLink.deleteLogDTO(id)
    }

    /**
     * Attempts to insert a Factor into the db
     * @return id of inserted factor
     */
    override suspend fun putFactor(factor: Factor): String {
        val factorDTO = factor.toDTOout()
        return supabaseLink.putFactorDTO(factorDTO)
    }

    /**
     * Gets a specific factor from db by id
     */
    override suspend fun getFactor(id: String): Factor? {
        val factorDTO = supabaseLink.getFactorDTO(id) ?: return null
        return factorDTO.toDomain()
    }

    /**
     * Gets all factors from db. May return empty list
     */
    override suspend fun getFactors(): List<Factor> {
        val factorDTOs = supabaseLink.getFactorsDTO()
        return factorDTOs.map { it.toDomain() }
    }

    /**
     * Removes factor by id if present
     */
    override suspend fun deleteFactor(id: String) {
        supabaseLink.deleteFactorDTO(id)
    }
}