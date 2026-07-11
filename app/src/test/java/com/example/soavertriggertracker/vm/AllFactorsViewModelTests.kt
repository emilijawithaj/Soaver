package com.example.soavertriggertracker.vm

import android.content.Context
import com.example.soavertriggertracker.data.DataLoader
import com.example.soavertriggertracker.data.Factor
import com.example.soavertriggertracker.data.FactorCategory
import com.example.soavertriggertracker.data.FactorRecord
import com.example.soavertriggertracker.data.Log
import com.example.soavertriggertracker.data.Tag
import com.example.soavertriggertracker.viewModel.AllFactorsViewModel
import com.example.soavertriggertracker.viewModel.CommonDataProcessing
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class AllFactorsViewModelTests {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dataLoader = mockk<DataLoader>()
    private val context = mockk<Context>()
    private val dataProcessor = CommonDataProcessing(context)


    @Before
    fun setup() {
        every { context.getString(any(), *anyVararg()) } answers {
            val args = it.invocation.args[1] as Array<*>
            "${args[0]} ${args[1]}"
        }
    }

    @Test
    fun logsMapToFactors() = runTest {
        every { dataLoader.getLogs() } returns listOf(log1, log2)
        every { dataLoader.getFactors() } returns listOf(factor1, factor2, factor3)
        every { dataLoader.logLoadingError() } returns null
        every { dataLoader.factorLoadingError() } returns null

        val viewModel = AllFactorsViewModel(dataLoader, dataProcessor)
        //subscribe to stream to trigger loading
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.factors.collect {}
        }

        advanceUntilIdle()

        val itemsInVM = viewModel.factors
        assert(itemsInVM.value.size == 3)
        assert(itemsInVM.value[1].name == "trial2")
        assert(itemsInVM.value[1].logsPresentIn.size == 2)
        assert(itemsInVM.value[2].logsPresentIn.size == 1)
    }

    @Test
    fun mappingWithEmptyLogs() = runTest {
        every { dataLoader.getLogs() } returns listOf()
        every { dataLoader.getFactors() } returns listOf(factor1, factor2, factor3)
        every { dataLoader.logLoadingError() } returns null
        every { dataLoader.factorLoadingError() } returns null

        val viewModel = AllFactorsViewModel(dataLoader, dataProcessor)
        //subscribe to stream to trigger loading
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.factors.collect {}
        }

        advanceUntilIdle()

        val itemsInVM = viewModel.factors
        assert(itemsInVM.value.size == 3)
        assert(itemsInVM.value[1].name == "trial2")
        assert(itemsInVM.value[1].logsPresentIn.isEmpty())
    }

    @Test
    fun logLoadErrorFlags() = runTest {
        //log exception
        every { dataLoader.getLogs() } returns listOf(log1, log2)
        every { dataLoader.getFactors() } returns listOf(factor1, factor2, factor3)
        every { dataLoader.logLoadingError() } returns Exception("test")
        every { dataLoader.factorLoadingError() } returns null

        val viewModel = AllFactorsViewModel(dataLoader, dataProcessor)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.factors.collect {}
        }

        advanceUntilIdle()

        assert(viewModel.error.value)
    }

    @Test
    fun factorLoadErrorFlags() = runTest {
        every { dataLoader.getLogs() } returns listOf(log1, log2)
        every { dataLoader.getFactors() } returns listOf(factor1, factor2, factor3)
        every { dataLoader.logLoadingError() } returns null
        every { dataLoader.factorLoadingError() } returns Exception("test")

        val viewModel = AllFactorsViewModel(dataLoader, dataProcessor)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.factors.collect {}
        }
        advanceUntilIdle()
        assert(viewModel.error.value)
    }

    @Test
    fun searchFiltering() = runTest {
        every { dataLoader.getLogs() } returns listOf(log1, log2)
        every { dataLoader.getFactors() } returns listOf(factor1, factor2, factor3)
        every { dataLoader.logLoadingError() } returns null
        every { dataLoader.factorLoadingError() } returns null

        val viewModel = AllFactorsViewModel(dataLoader, dataProcessor)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.factors.collect {}
        }
        advanceUntilIdle()
        assert(viewModel.factors.value.size == 3)

        viewModel.onSearchQueryChange("2")
        advanceUntilIdle()
        assert(viewModel.factors.value.size == 1)
        assert(viewModel.factors.value[0].name == "trial2")

        viewModel.onSearchQueryChange("")
        advanceUntilIdle()
        assert(viewModel.factors.value.size == 3)
        assert(viewModel.factors.value[0].name == "test")
    }

    @Test
    fun categoryAndSearchFiltering() = runTest {
        every { dataLoader.getLogs() } returns listOf(log1, log2)
        every { dataLoader.getFactors() } returns listOf(factor1, factor2, factor3)
        every { dataLoader.logLoadingError() } returns null
        every { dataLoader.factorLoadingError() } returns null

        val viewModel = AllFactorsViewModel(dataLoader, dataProcessor)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.factors.collect {}
        }
        advanceUntilIdle()
        assert(viewModel.factors.value.size == 3)

        viewModel.onFilterChange(FactorCategory.AUDITORY)
        advanceUntilIdle()
        assert(viewModel.factors.value.size == 2)
        assert(viewModel.factors.value[0].name == "test")
        assert(viewModel.factors.value[1].name == "trial2")

        viewModel.onSearchQueryChange("test")
        advanceUntilIdle()
        assert(viewModel.factors.value.size == 1)
        assert(viewModel.factors.value[0].name == "test")

        viewModel.onFilterChange(FactorCategory.VISUAL)
        advanceUntilIdle()
        assert(viewModel.factors.value.size == 2)
        assert(viewModel.factors.value[0].name == "test")
        assert(viewModel.factors.value[1].name == "test3")

        //all unselected = no filtering
        viewModel.onFilterChange(FactorCategory.AUDITORY)
        viewModel.onFilterChange(FactorCategory.VISUAL)
        advanceUntilIdle()
        assert(viewModel.factors.value.size == 2)
    }

    //common constructs
    val factor1 = Factor(
        id = "1",
        name = "test",
        isNumeric = false,
        category = FactorCategory.AUDITORY
    )
    val factor2 = Factor(
        id = "2",
        name = "trial2",
        isNumeric = false,
        category = FactorCategory.AUDITORY
    )
    val factor3 = Factor(
        id = "3",
        name = "test3",
        isNumeric = false,
        category = FactorCategory.VISUAL
    )
    val record1 = FactorRecord(
        id = "abc",
        factorId = "1",
        wasPresent = true,
        numValue = null,
        isNumeric = false,
        factorName = "test",
        factorCategory = FactorCategory.AUDITORY
    )
    val record2 = FactorRecord(
        id = "def",
        factorId = "2",
        wasPresent = true,
        numValue = null,
        isNumeric = false,
        factorName = "trial2",
        factorCategory = FactorCategory.AUDITORY
    )
    val record3 = FactorRecord(
        id = "ghi",
        factorId = "3",
        wasPresent = true,
        numValue = null,
        isNumeric = false,
        factorName = "test3",
        factorCategory = FactorCategory.VISUAL
    )
    val record12 = FactorRecord(
        id = "jkl",
        factorId = "1",
        wasPresent = true,
        numValue = null,
        isNumeric = false,
        factorName = "test",
        factorCategory = FactorCategory.AUDITORY
    )
    val record22 = FactorRecord(
        id = "mno",
        factorId = "2",
        wasPresent = true,
        numValue = null,
        isNumeric = false,
        factorName = "trial2",
        factorCategory = FactorCategory.AUDITORY
    )
    val log1 = Log(
        id = "123",
        datetime = Clock.System.now(),
        factorRecords = arrayListOf(record1, record2),
        tags = arrayListOf<Tag>()
    )
    val log2 = Log(
        id = "456",
        datetime = Clock.System.now(),
        factorRecords = arrayListOf(record12, record22, record3),
        tags = arrayListOf<Tag>()
    )
}