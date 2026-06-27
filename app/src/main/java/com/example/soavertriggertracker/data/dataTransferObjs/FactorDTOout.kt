package com.example.soavertriggertracker.data.dataTransferObjs

import com.example.soavertriggertracker.data.FactorCategory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Factor DTO for sending to db
 */
@Serializable
data class FactorDTOout(
    @SerialName("id") //JSON key
    val id: String? = null,

    @SerialName("name")
    val name: String,

    @SerialName("is_numeric")
    val isNumeric: Boolean,

    @SerialName("category")
    val category: FactorCategory
)
