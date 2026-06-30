package com.example.soavertriggertracker.data

import com.example.soavertriggertracker.data.dataTransferObjs.FactorDTO
import com.example.soavertriggertracker.data.dataTransferObjs.FactorRecordDTO
import com.example.soavertriggertracker.data.dataTransferObjs.LogDTO
import com.example.soavertriggertracker.data.dataTransferObjs.TagDTO
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.UUID
import kotlin.time.Clock

class RepositoryTest {
    //mock things
    private val mockLink = mockk<LogSupabaseLink>() //mockk supabase link
    private val repository = LogRepositoryImpl(mockLink)

    //common components
    val fac1DTO = FactorDTO(
        id = "poi",
        name = "testFac",
        isNumeric = true,
        category = FactorCategory.VISUAL
    )
    val recdto = FactorRecordDTO(
        id = "abc",
        factorId = "poi",
        boolVal = true,
        numVal = 1.0,
        factor = fac1DTO
    )
    val rec2dto = FactorRecordDTO(
        id = "def",
        factorId = "poi",
        boolVal = false,
        numVal = null,
        factor = fac1DTO
    )
    val tagdto = TagDTO(
        id = "abc",
        value = "testTag1"
    )

    val logdto = LogDTO(
        id = "123",
        datetime = Clock.System.now(),
        factorRecords = listOf(recdto, rec2dto),
        tags = listOf(tagdto)
    )
    val log2 = LogDTO(
        id = "456",
        datetime = Clock.System.now(),
        factorRecords = listOf(recdto),
        tags = listOf()
    )

    @Before
    fun setup() {
        coEvery { mockLink.getLogsDTO() } returns listOf(logdto, log2)
    }

    /**
     * tests get logs and ensures deep details are as expected
     */
    @Test
    fun getLogs() = runTest {
        val logs = repository.getLogs()
        assert(logs.size == 2)
        assert(logs[0].id == logdto.id)
        assert(logs[1].factorRecords[0].factorId == recdto.factor.id)
    }

    @Test
    fun getLog() = runTest {
        coEvery { mockLink.getLogDTO("123") } returns logdto

        val log = repository.getLog("123")
        assert(log != null)
        assert(log!!.id == logdto.id)
    }

    @Test
    fun getLogNull() = runTest {
        coEvery { mockLink.getLogDTO("456") } returns null
        val log = repository.getLog("456")
        assert(log == null)
    }

    @Test
    fun putLog() = runTest {
        val log = Log(
            id = null,
            datetime = log2.datetime,
            factorRecords = logdto.factorRecords.map { it.toDomain() } as ArrayList<FactorRecord>,
            tags = arrayListOf()
        )
        val logDtoOut = log.toDTO()
        coEvery { mockLink.putLogDTO(logDtoOut) } returns "123"

        val id = repository.putLog(log)
        assert(id == "123")
    }

    @Test
    fun deleteLog() = runTest {
        coEvery { mockLink.deleteLogDTO("123") } returns Unit
        repository.deleteLog("123")
        coVerify(exactly = 1) { mockLink.deleteLogDTO("123") }
    }

    @Test
    fun getFactors() = runTest {
        coEvery { mockLink.getFactorsDTO() } returns listOf(fac1DTO)
        val factors = repository.getFactors()
        assert(factors.size == 1)
        assert(factors[0].name == fac1DTO.name)
    }

    @Test
    fun getFactor() = runTest {
        coEvery { mockLink.getFactorDTO("poi") } returns fac1DTO
        val factor = repository.getFactor("poi")
        assert(factor != null)
        assert(factor!!.name == fac1DTO.name)
    }

    @Test
    fun getFactorNull() = runTest {
        coEvery { mockLink.getFactorDTO("def") } returns null
        val factor = repository.getFactor("def")
        assert(factor == null)
    }

    @Test
    fun putFactor() = runTest {
        val factor = Factor(
            id = null,
            name = "testFac",
            isNumeric = true,
            category = FactorCategory.VISUAL
        )
        val factorDtoOut = factor.toDTOout()
        coEvery { mockLink.putFactorDTO(factorDtoOut) } returns "poi"

        val id = repository.putFactor(factor)
        assert(id == "poi")
        coVerify(exactly = 1) { mockLink.putFactorDTO(factorDtoOut) }
    }

    @Test(expected = Exception::class)
    fun exceptionInGetLogsSurfaces() = runTest {
        coEvery { mockLink.getLogsDTO() } throws Exception("test")
        repository.getLogs()
    }

    @Test(expected = IllegalStateException::class)
    fun insertLogWithNoFactorRecords(): Unit = runTest {
        val log = Log(
            id = UUID.randomUUID().toString(),
            datetime = Clock.System.now(),
            factorRecords = arrayListOf(),
            tags = arrayListOf()
        )
        coEvery { mockLink.putLogDTO(log.toDTO()) } throws IllegalArgumentException("FactorRecords of a Log cannot be empty")

        repository.putLog(log)
    }
}