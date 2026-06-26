package com.example.soavertriggertracker

import com.example.soavertriggertracker.data.FactorRecord
import com.example.soavertriggertracker.data.Log
import com.example.soavertriggertracker.data.Tag
import com.example.soavertriggertracker.data.dataTransferObjs.FactorDTO
import com.example.soavertriggertracker.data.dataTransferObjs.FactorRecordDTO
import com.example.soavertriggertracker.data.dataTransferObjs.LogDTO
import com.example.soavertriggertracker.data.dataTransferObjs.TagDTO
import com.example.soavertriggertracker.data.toDTO
import com.example.soavertriggertracker.data.toDomain
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Clock

class DTOMapperTests {
    //components
    //DTO
    val fac1DTO = FactorDTO(
        id = "poi",
        name = "testFac",
        isNumeric = true
    )
    val rec1DTO = FactorRecordDTO(
        id = "abc",
        factorId = "poi",
        boolVal = true,
        numVal = 1.0,
        factor = fac1DTO
    )
    val rec2DTO = FactorRecordDTO(
        id = "def",
        factorId = "poi",
        boolVal = false,
        numVal = null,
        factor = fac1DTO
    )
    val tag1DTO = TagDTO(
        id = "abc",
        value = "testTag1"
    )
    val tag2DTO = TagDTO(
        id = "def",
        value = "testTag2"
    )

    //Domain



    /**
     * Simple test of a logDTO with 2 records (of same factor) and 2 tags
     */
    @Test
    fun simple_logDTO_toDomain() {
        //log
        val dto = LogDTO(
            id = "123",
            datetime = Clock.System.now(),
            factorRecords = listOf(rec1DTO, rec2DTO),
            tags = listOf(tag1DTO, tag2DTO)
        )

        //tests
        val domain = dto.toDomain()
        //general on log
        assertEquals(dto.id, domain.id)
        assertEquals(dto.datetime, domain.datetime)
        assertEquals(dto.factorRecords.size, domain.factorRecords.size)
        assertEquals(dto.tags.size, domain.tags.size)

        //factor record details on domain log vs original defined rec1 component
        assertEquals(rec1DTO.id, domain.factorRecords[0].id)
        assertEquals(rec1DTO.factorId, domain.factorRecords[0].factorId)
        assertEquals(rec1DTO.boolVal, domain.factorRecords[0].wasPresent)
        assertEquals(rec1DTO.numVal, domain.factorRecords[0].numValue)

        //factor details on log vs original defined fac1 component
        assertEquals(fac1DTO.id, domain.factorRecords[0].factorId)
        assertEquals(fac1DTO.name, domain.factorRecords[0].factorName)
        assertEquals(fac1DTO.isNumeric, domain.factorRecords[0].isNumeric)

        //tag details on log vs original defined tag2 component
        assertEquals(tag2DTO.id, domain.tags[1].id)
        assertEquals(tag2DTO.value, domain.tags[1].value)
    }

    /**
     * Test successful build of a log with no tags
     */
    @Test
    fun missing_tags_toDomain() {
        val logDTO = LogDTO(
            id = "123",
            datetime = Clock.System.now(),
            factorRecords = listOf(rec1DTO, rec2DTO),
            tags = emptyList()
        )

        val domain = logDTO.toDomain()
        assertEquals(0, domain.tags.size)
    }

    /**
     * Test refusal to build log with no factor records
     */
    @Test
    fun missing_factor_record_toDomain() {
        val logDTO = LogDTO(
            id = "123",
            datetime = Clock.System.now(),
            factorRecords = emptyList(),
            tags = listOf(tag1DTO, tag2DTO)
        )

        try {
            logDTO.toDomain()
            assert(false)
        } catch (e: IllegalStateException) {
            assertEquals("FactorRecords of a Log cannot be empty", e.message)
        }
    }

    /**
     * Test a valid new log conversion to DTO
     */
    @Test
    fun new_log_to_DTO() {
        //components
        val factR = FactorRecord(
            id = null,
            factorId = "poi",
            factorName = "testFac",
            isNumeric = true,
            wasPresent = true,
            numValue = 1.0
        )
        val factR2 = FactorRecord(
            id = null,
            factorId = "poi",
            factorName = "testFac",
            isNumeric = false,
            wasPresent = false,
            numValue = null
        )
        val tag = Tag(
            id = null,
            value = "testTag"
        )
        val log = Log(
            id = null,
            factorRecords = arrayListOf(factR, factR2),
            tags = arrayListOf(tag),
            datetime = Clock.System.now()
        )

        //test
        val dto = log.toDTO()
        assertEquals(log.id, dto.id)
        assertEquals(log.datetime, dto.datetime)
        assertEquals(log.factorRecords.size, dto.factorRecords.size)
        assertEquals(log.tags.size, dto.tags.size)

        //factor record details on log vs original defined factR2 component
        assertEquals(factR2.id, dto.factorRecords[1].id)
        assertEquals(factR2.factorId, dto.factorRecords[1].factorId)
        assertEquals(factR2.wasPresent, dto.factorRecords[1].boolVal)
        assertEquals(factR2.numValue, dto.factorRecords[1].numVal)

        //factor details
        assertEquals(factR2.factorId, dto.factorRecords[1].factorId)

        //tag details on log vs original defined tag component
        assertEquals(tag.id, dto.tags[0].id)
        assertEquals(tag.value, dto.tags[0].value)
    }

    /**
     * Test refusal to build logDTO with no factor records
     */
    @Test
    fun missing_factor_record_toDTO() {
        val log = Log(
            id = null,
            factorRecords = arrayListOf(),
            tags = arrayListOf(),
            datetime = Clock.System.now()
        )

        try {
            log.toDTO()
            assert(false)
        } catch (e: IllegalStateException) {
            assertEquals("FactorRecords of a Log cannot be empty", e.message)
        }
    }
}