package com.example.soavertriggertracker.data

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
    suspend fun getLogsDTO(): List<LogDTO>?
    suspend fun putLogDTO(log: LogDTOout): String
    suspend fun getLogDTO(id: String): LogDTO?
    suspend fun deleteLogDTO(id: String)
    //suspend fun updateLogDTO(log: LogDTO)
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
                            is_numeric
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
                            is_numeric
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

    override suspend fun deleteLogDTO(id: String) {
        return withContext(Dispatchers.IO) {
            postgrest.from("Logs").delete {
                filter {
                    eq("id", id)
                }
            }
        }
    }

    /* TODO Implement UPDATE
    /**
     * Updates a LogDTO in the db by deleting and reinserting it
     * @throws IllegalArgumentException if called on Log with no id
     */
    override suspend fun updateLogDTO(log: LogDTO){
        if (log.id == null) {
            throw IllegalArgumentException("Cannot call update on new Log (LogDTO id is null)")
        }
        try {
            withContext(Dispatchers.IO) {
                postgrest.from("Logs").upsert(log)
            }
        } catch (e: Exception) {
            throw e
        }
    }
    */
}