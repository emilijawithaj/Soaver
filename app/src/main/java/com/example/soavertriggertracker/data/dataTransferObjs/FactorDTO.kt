package com.example.soavertriggertracker.data.dataTransferObjs

import com.example.soavertriggertracker.data.FactorCategory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Factor Data Transfer Object. Data from this is folded into FactorRecord obj.
 */
@Serializable //Reads from/to JSON
data class FactorDTO(
    @SerialName("id") //JSON key
    val id: String,

    @SerialName("name")
    val name: String,

    @SerialName("is_numeric")
    val isNumeric: Boolean,

    @SerialName("category")
    val category: FactorCategory
)
