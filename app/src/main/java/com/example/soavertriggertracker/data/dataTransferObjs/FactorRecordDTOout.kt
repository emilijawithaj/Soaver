package com.example.soavertriggertracker.data.dataTransferObjs

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Factor Record DTO, with Factor linked
 */
@Serializable
data class FactorRecordDTOout(
    @SerialName("id")
    val id: String? = null,

    @SerialName("log_id")
    val logId: String? = null,

    @SerialName("factor_id")
    val factorId: String, //never null as cannot create new Factor with FactorRecord

    @SerialName("bool_value")
    val boolVal: Boolean,

    @SerialName("num_value")
    val numVal: Double? = null,
)

//TODO throw catch on missing links?