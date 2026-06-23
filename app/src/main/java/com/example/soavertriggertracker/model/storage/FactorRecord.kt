package com.example.soavertriggertracker.model.storage

data class FactorRecord(
    var factor: Factor,
    var wasPresent: Boolean,
    var numValue: Double? = -1.0
)