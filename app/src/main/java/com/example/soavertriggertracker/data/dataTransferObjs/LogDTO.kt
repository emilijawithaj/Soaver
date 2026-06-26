package com.example.soavertriggertracker.data.dataTransferObjs

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant


/**
 * Log Data Transfer Object (has same structure as Log)
 */
@Serializable
data class LogDTO(

    @SerialName("id")
    val id: String,

    @SerialName("datetime")
    val datetime: Instant,

    @SerialName("FactorRecords")
    val factorRecords: List<FactorRecordDTO> = emptyList(),

    @SerialName("Tags")
    val tags: List<TagDTO> = emptyList()
)