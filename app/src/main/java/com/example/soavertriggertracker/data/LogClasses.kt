package com.example.soavertriggertracker.data

import kotlin.time.Instant

enum class FactorCategory { VISUAL, AUDITORY, TACTILE, OLFACTORY, GUSTATORY, INTERNAL }

/**
 * Factor record (part of Log) with Factor details encoded in it
 */
data class FactorRecord(
    var id: String? = null,
    var factorId: String,
    var factorCategory: FactorCategory,
    var factorName: String,
    var isNumeric: Boolean,
    var wasPresent: Boolean,
    var numValue: Double? = null
)

/**
 * Log. Of factor records and tags
 */
data class Log(
    var id: String? = null,
    var factorRecords: ArrayList<FactorRecord>,
    var tags: ArrayList<Tag>,
    var datetime: Instant
)

/**
 * Tag.
 */
data class Tag(
    var id: String? = null,
    var value: String
)
