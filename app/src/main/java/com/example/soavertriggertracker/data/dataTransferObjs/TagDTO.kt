package com.example.soavertriggertracker.data.dataTransferObjs

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Tag DTO.
 */
@Serializable
data class TagDTO(

    @SerialName("id")
    val id: String,

    @SerialName("tag_value")
    val value: String
)