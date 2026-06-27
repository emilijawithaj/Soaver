package com.example.soavertriggertracker.data

import com.example.soavertriggertracker.data.dataTransferObjs.FactorDTO
import com.example.soavertriggertracker.data.dataTransferObjs.FactorDTOout
import com.example.soavertriggertracker.data.dataTransferObjs.LogDTO
import com.example.soavertriggertracker.data.dataTransferObjs.LogDTOout
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

/**
 * Execute CRUD operations for Logs in Supabase, using DTOs
 */
interface LogSupabaseLink {
    suspend fun getLogsDTO(): List<LogDTO>
    suspend fun putLogDTO(log: LogDTOout): String
    suspend fun getLogDTO(id: String): LogDTO?
    suspend fun deleteLogDTO(id: String)

    //suspend fun updateLogDTO(log: LogDTO)
    suspend fun putFactorDTO(factor: FactorDTOout): String
    suspend fun getFactorDTO(id: String): FactorDTO?
    suspend fun getFactorsDTO(): List<FactorDTO>
    suspend fun deleteFactorDTO(id: String)
}


class LogSupabaseLinkImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val auth: Auth
) : LogSupabaseLink {

    /**
     * Attempts to insert a LogDTO into the db
     * @param log The Log to be inserted as DTO
     * @throws IllegalStateException no user is not logged in
     * @throws Exception if id of log attempted to insert cannot be immediately fetched
     * @throws IllegalArgumentException if log has no FactorRecords
     */
    override suspend fun putLogDTO(log: LogDTOout): String {
        val uid = auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("User is not logged in when adding log") //executes on uid null (?)
        if (log.factorRecords.isEmpty()) {
            throw IllegalArgumentException("Log cannot have no FactorRecords")
        }

        return withContext(Dispatchers.IO) {
            //insert Log table
            val insertedLog = postgrest.from("Logs").insert(
                //add existing log id if present
                if (log.id == null) {
                    mapOf(
                        "user_id" to uid,
                        "datetime" to log.datetime.toString(),
                    )
                } else {
                    mapOf(
                        "id" to log.id,
                        "user_id" to uid,
                        "datetime" to log.datetime.toString(),
                    )
                }
            ) {
                select()
            }.decodeSingle<LogDTO>() //throws if not found

            //insert factor records
            postgrest.from("FactorRecords").insert(
                log.factorRecords.map {
                    buildJsonObject {
                        put("user_id", uid)
                        put("log_id", insertedLog.id)
                        put("factor_id", it.factorId)
                        put("bool_value", it.boolVal)
                        put("num_value", it.numVal)
                    }
                }
            )

            //insert tags
            if (log.tags.isNotEmpty()) {
                postgrest.from("Tags").insert(
                    log.tags.map {
                        buildJsonObject {
                            put("user_id", uid)
                            put("log_id", insertedLog.id)
                            put("tag_value", it.value)
                        }
                    }
                )
            }
            return@withContext insertedLog.id
        }
    }

    /**
     * Gets all user logs from db. May return empty list
     * @return List<LogDTO> Logs as DTOs
     */
    override suspend fun getLogsDTO(): List<LogDTO> {
        return withContext(Dispatchers.IO) {
            postgrest.from("Logs")
                .select(
                    columns = Columns.raw(
                        """
                    id,
                    datetime,
                    FactorRecords (
                        id,
                        factor_id,
                        bool_value,
                        num_value,
                        Factor:Factors (
                            id,
                            name,
                            is_numeric,
                            category
                        )
                    ),
                    Tags (
                        id,
                        tag_value
                    )
                    """.trimIndent()
                    )
                ) {
                    order("datetime", Order.DESCENDING)
                }
                .decodeList<LogDTO>()
        }
    }


    /**
     * Gets a specific log from db by id
     * @return The log as a DTO, null if not found
     */
    override suspend fun getLogDTO(id: String): LogDTO? {
        return withContext(Dispatchers.IO) {
            postgrest.from("Logs").select(
                columns = Columns.raw(
                    """
                    id,
                    datetime,
                    FactorRecords (
                        id,
                        factor_id,
                        bool_value,
                        num_value,
                        Factor:Factors (
                            id,
                            name,
                            is_numeric,
                            category
                        )
                    ),
                    Tags (
                        id,
                        tag_value
                    )
                    """.trimIndent()
                )
            ) {
                filter {
                    eq("id", id)
                }
            }.decodeSingleOrNull<LogDTO>()
        }
    }

    /**
     * Removes log by id if present. Cascade policy removes nested table rows
     */
    override suspend fun deleteLogDTO(id: String) {
        return withContext(Dispatchers.IO) {
            postgrest.from("Logs").delete {
                filter {
                    eq("id", id)
                }
            }
        }
    }

    /**
     * Attempts to insert a Factor into the db
     */
    override suspend fun putFactorDTO(factor: FactorDTOout): String {
        val uid = auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("User is not logged in when adding log") //executes on uid null (?)

        return withContext(Dispatchers.IO) {
            //insert factor
            val insertedFactor = postgrest.from("Factors").upsert(
                buildJsonObject {
                    put("user_id", uid)
                    put("name", factor.name)
                    put("is_numeric", factor.isNumeric)
                    put("category", factor.category.toString())
                    if (factor.id != null) {
                        put("id", factor.id)
                    }
                }
            ) {
                select()
            }.decodeSingle<FactorDTO>()

            return@withContext insertedFactor.id
        }
    }

    /**
     * Gets a specific factor from db by id
     * @return factor as DTO, null if not found
     */
    override suspend fun getFactorDTO(id: String): FactorDTO? {
        return withContext(Dispatchers.IO) {
            postgrest.from("Factors").select(
                columns = Columns.raw(
                    """
                    id,
                    name,
                    is_numeric,
                    category
                    """.trimIndent()
                )
            ) {
                filter {
                    eq("id", id)
                }
            }.decodeSingleOrNull<FactorDTO>()
        }
    }

    /**
     * Gets all factors from db. May return empty list
     */
    override suspend fun getFactorsDTO(): List<FactorDTO> {
        return withContext(Dispatchers.IO) {
            postgrest.from("Factors").select(
                columns = Columns.raw(
                    """
                    id,
                    name,
                    is_numeric,
                    category
                    """.trimIndent()
                )
            ).decodeList<FactorDTO>()
        }
    }

    /**
     * Removes factor by id if present
     */
    override suspend fun deleteFactorDTO(id: String) {
        return withContext(Dispatchers.IO) {
            postgrest.from("Factors").delete {
                filter {
                    eq("id", id)
                }
            }
        }
    }
}