package com.example.soavertriggertracker.data

import android.content.Context
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class DataLoaderTests {

    private val repository = mockk<LogRepository>()
    private val context = mockk<Context>()

    @Test
    fun loadAllReloadsData() = runTest {
        val loader = DataLoader(repository)

        //start with one log and one factor
        coEvery { repository.getLogs() } returns listOf(Log(id = "1", datetime = Clock.System.now(), factorRecords = arrayListOf(
            FactorRecord(id = "1", factorId = "1", wasPresent = true, numValue = 1.0, isNumeric = true, factorName = "test", factorCategory = FactorCategory.AUDITORY)
        ), tags = arrayListOf()))
        coEvery { repository.getFactors() } returns listOf(Factor(id = "1", name = "test", isNumeric = true, category = FactorCategory.AUDITORY))

        //ensure correct loading
        loader.loadAll()
        advanceUntilIdle()

        assert(loader.getLogs().size == 1)
        assert(loader.getFactors().size == 1)
        assert(loader.logLoadingError() == null)
        assert(loader.factorLoadingError() == null)

        //'remove' logs from repository
        coEvery { repository.getLogs() } returns listOf()
        coEvery { repository.getFactors() } returns listOf()

        //reload
        loader.loadAll()
        advanceUntilIdle()

        assert(loader.getLogs().isEmpty())
        assert(loader.getFactors().isEmpty())
        assert(loader.logLoadingError() == null)
        assert(loader.factorLoadingError() == null)
    }

    @Test
    fun loadingSetsAndResetsExceptions() = runTest {
        val logException = Exception("test")
        val factorException = Exception("test")
        coEvery { repository.getLogs() } throws logException
        coEvery { repository.getFactors() } throws factorException
        val loader = DataLoader(repository)

        loader.loadAll()
        advanceUntilIdle()

        assert(loader.logLoadingError() == logException)
        assert(loader.factorLoadingError() == factorException)

        //reset
        coEvery { repository.getLogs() } returns listOf()
        coEvery { repository.getFactors() } returns listOf()

        loader.loadAll()
        advanceUntilIdle()

        assert(loader.logLoadingError() == null)
        assert(loader.factorLoadingError() == null)
    }

    @Test
    fun partiallySuccessFulLoadPartiallyCompletes() = runTest {
        coEvery { repository.getLogs() } throws Exception("test")
        coEvery { repository.getFactors() } returns listOf(Factor(id = "1", name = "test", isNumeric = true, category = FactorCategory.AUDITORY))
        val loader = DataLoader(repository)
        loader.loadAll()
        advanceUntilIdle()

        assert(loader.logLoadingError() != null)
        assert(loader.factorLoadingError() == null)
        assert(loader.getFactors().size == 1)
    }
}