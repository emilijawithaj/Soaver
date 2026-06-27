package com.example.soavertriggertracker.data

import com.example.soavertriggertracker.data.dataTransferObjs.FactorRecordDTO
import com.example.soavertriggertracker.data.dataTransferObjs.FactorRecordDTOout
import com.example.soavertriggertracker.data.dataTransferObjs.LogDTO
import com.example.soavertriggertracker.data.dataTransferObjs.LogDTOout
import com.example.soavertriggertracker.data.dataTransferObjs.TagDTO
import com.example.soavertriggertracker.data.dataTransferObjs.TagDTOout

/**
 * Maps incoming Logs (DTO) to domain class Logs.
 * @return Log the Log
 * @throws IllegalStateException if a Log with no FactorRecords is processed
 */
fun LogDTO.toDomain(): Log {
    //attached factorRecords as DTOs toDomained and mapped to an arrayList
    val mappedFRecords = factorRecords.map { it.toDomain() } as ArrayList<FactorRecord>

    if (mappedFRecords.isEmpty()) {
        throw IllegalStateException("FactorRecords of a Log cannot be empty")
    }

    return Log(
        id = id,
        datetime = datetime,
        factorRecords = mappedFRecords,
        tags = tags.map { it.toDomain() } as ArrayList<Tag>
    )
}


/**
 * Process FactorRecordDTO to FactorRecords, merging in attached Factor details
 * @return FactorRecord the domain class FactorRecord
 */
fun FactorRecordDTO.toDomain(): FactorRecord {
    val factorDto = factor //attached Factor

    return FactorRecord(
        id = id,
        factorId = factorDto.id,
        factorCategory = factorDto.category,
        factorName = factorDto.name,
        isNumeric = factorDto.isNumeric,
        wasPresent = boolVal,
        numValue = numVal
    )
}

/**
 * Processes TagDTO to Tag
 * @return Tag
 */
fun TagDTO.toDomain(): Tag {
    return Tag(
        id = id,
        value = value
    )
}

/**
 * Processes a Log to a LogDTO
 * @return LogDTO
 * @throws IllegalStateException if a Log with no FactorRecords is processed
 */
fun Log.toDTO(): LogDTOout {
    if (factorRecords.isEmpty()) {
        throw IllegalStateException("FactorRecords of a Log cannot be empty")
    }

    return LogDTOout(
        id = id,
        datetime = datetime,
        factorRecords = factorRecords.map { it.toDTO() },
        tags = tags.map { it.toDTO() }
    )
}

/**
 * Processes a FactorRecord to a FactorRecordDTO
 * @return FactorRecordDTO
 */
fun FactorRecord.toDTO(): FactorRecordDTOout {
    return FactorRecordDTOout(
        id = id,
        logId = null,
        factorId = factorId,
        boolVal = wasPresent,
        numVal = numValue,
    )
}

/**
 * Processes a Tag to a TagDTO
 * @return TagDTO
 */
fun Tag.toDTO(): TagDTOout {
    return TagDTOout(
        id = id,
        value = value,
        logId = null
    )
}
