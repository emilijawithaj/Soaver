package com.example.soavertriggertracker.data

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
    private val triggerDBLink = mockk<TriggerSupabaseLink>()

    @Test
    fun loadAllReloadsData() = runTest {
        val loader = DataLoader(repository, triggerDBLink)

        //start with one log and one factor
        coEvery { repository.getLogs() } returns listOf(Log(id = "1", datetime = Clock.System.now(), factorRecords = arrayListOf(
            FactorRecord(id = "1", factorId = "1", wasPresent = true, numValue = 1.0, isNumeric = true, factorName = "test", factorCategory = FactorCategory.AUDITORY)
        ), tags = arrayListOf()))
        coEvery { repository.getFactors() } returns listOf(Factor(id = "1", name = "test", isNumeric = true, category = FactorCategory.AUDITORY))
        coEvery { triggerDBLink.getTriggers() } returns listOf("test")

        //ensure correct loading
        loader.loadAll()
        advanceUntilIdle()

        assert(loader.getLogs().size == 1)
        assert(loader.getFactors().size == 1)
        assert(loader.getTriggers().size == 1)
        assert(loader.logLoadingError() == null)
        assert(loader.factorLoadingError() == null)
        assert(loader.triggerLoadingError() == null)

        //'remove' from repository
        coEvery { repository.getLogs() } returns listOf()
        coEvery { repository.getFactors() } returns listOf()
        coEvery { triggerDBLink.getTriggers() } returns listOf()

        //reload
        loader.loadAll()
        advanceUntilIdle()

        assert(loader.getLogs().isEmpty())
        assert(loader.getFactors().isEmpty())
        assert(loader.getTriggers().isEmpty())
        assert(loader.logLoadingError() == null)
        assert(loader.factorLoadingError() == null)
        assert(loader.triggerLoadingError() == null)
    }

    @Test
    fun loadingSetsAndResetsExceptions() = runTest {
        val logException = Exception("test")
        val factorException = Exception("test")
        val triggerException = Exception("test")
        coEvery { repository.getLogs() } throws logException
        coEvery { repository.getFactors() } throws factorException
        coEvery { triggerDBLink.getTriggers() } throws triggerException
        val loader = DataLoader(repository, triggerDBLink)

        loader.loadAll()
        advanceUntilIdle()

        assert(loader.logLoadingError() == logException)
        assert(loader.factorLoadingError() == factorException)
        assert(loader.triggerLoadingError() == triggerException)

        //reset
        coEvery { repository.getLogs() } returns listOf()
        coEvery { repository.getFactors() } returns listOf()
        coEvery { triggerDBLink.getTriggers() } returns listOf()

        loader.loadAll()
        advanceUntilIdle()

        assert(loader.logLoadingError() == null)
        assert(loader.factorLoadingError() == null)
        assert(loader.triggerLoadingError() == null)
    }

    @Test
    fun partiallySuccessFulLoadPartiallyCompletes() = runTest {
        coEvery { repository.getLogs() } throws Exception("test")
        coEvery { repository.getFactors() } returns listOf(Factor(id = "1", name = "test", isNumeric = true, category = FactorCategory.AUDITORY))
        coEvery { triggerDBLink.getTriggers() } returns listOf("test")
        val loader = DataLoader(repository, triggerDBLink)
        loader.loadAll()
        advanceUntilIdle()

        assert(loader.logLoadingError() != null)
        assert(loader.factorLoadingError() == null)
        assert(loader.triggerLoadingError() == null)
        assert(loader.getFactors().size == 1)
        assert(loader.getTriggers().size == 1)
    }
}