package com.example.soavertriggertracker.model

import com.example.soavertriggertracker.model.storage.FactorRecord

data class Log (var factorRecords: ArrayList<FactorRecord>, var tags: ArrayList<String>)