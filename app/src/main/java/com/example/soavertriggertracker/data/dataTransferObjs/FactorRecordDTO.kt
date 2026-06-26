package com.example.soavertriggertracker.data.dataTransferObjs

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Factor Record DTO, with Factor linked
 */
@Serializable
data class FactorRecordDTO(
    @SerialName("id")
    val id: String,

    @SerialName("factor_id")
    val factorId: String,

    @SerialName("bool_value")
    val boolVal: Boolean,

    @SerialName("num_value")
    val numVal: Double? = null,

    @SerialName("Factor")
    val factor: FactorDTO
)

//TODO throw catch on missing links?