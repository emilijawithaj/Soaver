package com.example.soavertriggertracker.data.dataTransferObjs

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Tag DTO.
 */
@Serializable
data class TagDTOout(

    @SerialName("id")
    val id: String? = null,

    @SerialName("tag_value")
    val value: String,

    @SerialName("log_id")
    val logId: String? = null,

)

