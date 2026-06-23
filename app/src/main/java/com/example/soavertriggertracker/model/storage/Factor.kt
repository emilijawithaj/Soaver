package com.example.soavertriggertracker.model.storage

enum class FactorCategory {VISUAL, AUDITORY, TACTILE, OLFACTORY, GUSTATORY, INTERNAL}

data class Factor (
    var name: String,
    var category: FactorCategory,
    var isNumeric: Boolean)