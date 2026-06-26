package com.example.soavertriggertracker.data.dataTransferObjs

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant


/**
 * Log Data Transfer Object (has same structure as Log)
 */
@Serializable
data class LogDTOout(

    @SerialName("id")
    val id: String? = null,

    @SerialName("user_id")
    val userId: String? = null,

    @SerialName("datetime")
    val datetime: Instant,

    @SerialName("FactorRecords")
    val factorRecords: List<FactorRecordDTOout> = emptyList(),

    @SerialName("Tags")
    val tags: List<TagDTOout> = emptyList()
)