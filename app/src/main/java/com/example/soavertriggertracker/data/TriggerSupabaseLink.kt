package com.example.soavertriggertracker.data

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import javax.inject.Inject

/**
 * Execute CRUD operations for Logs in Supabase, using DTOs
 */
interface TriggerSupabaseLink {
    suspend fun getTriggers(): List<String>
    suspend fun putTrigger(trigger: String)
    suspend fun deleteTrigger(trigger: String)
}


class TriggerSupabaseLinkImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val auth: Auth
) : TriggerSupabaseLink {


    /**
     * Gets all triggers from db. May return empty list
     * @return List<String> Triggers as Strings
     */
    override suspend fun getTriggers(): List<String> {
        return withContext(Dispatchers.IO) {
            postgrest.from("Triggers").select(
                columns = Columns.raw("value")
            )
                //fetch as and map from JsonObjects
                .decodeList<JsonObject>()
                .map { it["value"]!!.jsonPrimitive.content }
        }
    }

    /**
     * Attempts to insert trigger into db, does not return anything
     * @throws IllegalStateException if user is not logged in
     * @throws IllegalArgumentException if trigger is empty string
     * @throws Exception if trigger cannot be inserted (incl. if identical already present)
     */
    override suspend fun putTrigger(trigger: String) {
        val uid = auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("User is not logged in when adding trigger")
        if (trigger == "") {
            throw IllegalArgumentException("Trigger cannot be empty string")
        }

        withContext(Dispatchers.IO) {
            postgrest.from("Triggers").insert(
                buildJsonObject {
                    put("user_id", uid)
                    put("value", trigger)
                }
            )
        }
    }

    /**
     * Removes trigger by value if present
     * @throws IllegalStateException if user is not logged in
     */
    override suspend fun deleteTrigger(trigger: String) {
        val uid = auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("User is not logged in when deleting trigger")

        withContext(Dispatchers.IO) {
            postgrest.from("Triggers").delete {
                filter {
                    eq("value", trigger)
                    eq("user_id", uid)
                }
            }
        }
    }
}