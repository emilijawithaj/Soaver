package com.example.soavertriggertracker.vm

import android.content.Context
import com.example.soavertriggertracker.data.DataLoader
import com.example.soavertriggertracker.data.FactorCategory
import com.example.soavertriggertracker.data.FactorRecord
import com.example.soavertriggertracker.data.Log
import com.example.soavertriggertracker.data.Tag
import com.example.soavertriggertracker.viewModel.AllLogsViewModel
import com.example.soavertriggertracker.viewModel.CommonDataProcessing
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class AllLogsViewModelTests {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dataLoader = mockk<DataLoader>()
    private val context = mockk<Context>()
    private val dataProcessor = CommonDataProcessing(context)

    @Before
    fun setup() {
        every { context.getString(any(), *anyVararg()) } answers {
            val args = it.invocation.args[1] as Array<*>
            "Log from ${args[0]} at ${args[1]}"
        }
    }

    @Test
    fun loadLogs() = runTest {
        val rec1 = FactorRecord(
            id = "1",
            factorId = "1",
            wasPresent = true,
            numValue = 1.0,
            isNumeric = true,
            factorName = "test",
            factorCategory = FactorCategory.AUDITORY
        )
        val tag = Tag(
            id = "1",
            value = "test"
        )
        val tag2 = Tag(
            id = "2",
            value = "test2"
        )
        val log1 = Log(
            id = "1",
            datetime = Clock.System.now(),
            factorRecords = arrayListOf(rec1),
            tags = arrayListOf<Tag>(tag, tag2)
        )
        val log2 = Log(
            id = "2",
            datetime = Clock.System.now(),
            factorRecords = arrayListOf(rec1),
            tags = arrayListOf<Tag>(tag, tag2)
        )
        every { dataLoader.getLogs() } returns listOf(log1, log2)
        every { dataLoader.logLoadingError() } returns null
        val viewModel = AllLogsViewModel(dataLoader, dataProcessor)

        advanceUntilIdle()

        val itemsInVM = viewModel.logItems
        assert(!viewModel.error.value)
        assert(itemsInVM.value.size == 2)
        assert(itemsInVM.value[0].records.first() == rec1.factorName)
        assert(itemsInVM.value[1].records.first() == rec1.factorName)
        assert(itemsInVM.value[0].tags.size == 2)
        assert(itemsInVM.value[1].tags.size == 2)
        assert(itemsInVM.value[0].tags.first() == tag.value)
    }

    @Test
    fun loadLogs_error() = runTest {
        every { dataLoader.getLogs() } returns emptyList()
        every { dataLoader.logLoadingError() } returns Exception("test")
        val viewModel = AllLogsViewModel(dataLoader, dataProcessor)

        advanceUntilIdle()

        assert(viewModel.error.value)
    }

    @Test
    fun loadRetry() = runTest {
        every { dataLoader.getLogs() } returns emptyList()
        every { dataLoader.logLoadingError() } returns Exception("test")
        val viewModel = AllLogsViewModel(dataLoader, dataProcessor)
        advanceUntilIdle()
        assert(viewModel.error.value)

        every { dataLoader.getLogs() } returns listOf(Log(id = "1", datetime = Clock.System.now(), factorRecords = arrayListOf(
            FactorRecord(id = "1", factorId = "1", wasPresent = true, numValue = 1.0, isNumeric = true, factorName = "test", factorCategory = FactorCategory.AUDITORY)), tags = arrayListOf()))
        every { dataLoader.logLoadingError() } returns null

        viewModel.loadLogs()
        coVerify(exactly = 2) { dataLoader.getLogs() }
        assert(!viewModel.error.value)
        assert(viewModel.logItems.value.size == 1)
    }

    @Test
    fun searchInRecord() = runTest {
        val rec1 = FactorRecord(
            id = "1",
            factorId = "1",
            wasPresent = true,
            numValue = 1.0,
            isNumeric = true,
            factorName = "test",
            factorCategory = FactorCategory.AUDITORY
        )
        val rec2 = FactorRecord(
            id = "2",
            factorId = "1",
            wasPresent = true,
            numValue = 2.0,
            isNumeric = true,
            factorName = "trial",
            factorCategory = FactorCategory.AUDITORY
        )
        val log1 = Log(
            id = "1",
            datetime = Clock.System.now(),
            factorRecords = arrayListOf(rec1),
            tags = arrayListOf<Tag>()
        )
        val log2 = Log(
            id = "2",
            datetime = Clock.System.now(),
            factorRecords = arrayListOf(rec2),
            tags = arrayListOf<Tag>()
        )
        every { dataLoader.getLogs() } returns listOf(log1, log2)
        every { dataLoader.logLoadingError() } returns null
        val viewModel = AllLogsViewModel(dataLoader, dataProcessor)
        advanceUntilIdle()

        assert(viewModel.logItems.value.size == 2)

        viewModel.onSearchQueryChange("te")
        advanceUntilIdle()

        assert(viewModel.logItems.value.size == 1)
        assert(viewModel.logItems.value[0].records.first() == rec1.factorName)
    }

    @Test
    fun searchInTags() = runTest {
        val rec1 = FactorRecord(
            id = "1",
            factorId = "1",
            wasPresent = true,
            numValue = 1.0,
            isNumeric = true,
            factorName = "test",
            factorCategory = FactorCategory.AUDITORY
        )
        val tag = Tag(
            id = "1",
            value = "test"
        )
        val tag2 = Tag(
            id = "2",
            value = "trial"
        )
        val log1 = Log(
            id = "1",
            datetime = Clock.System.now(),
            factorRecords = arrayListOf(rec1),
            tags = arrayListOf<Tag>(tag)
        )
        val log2 = Log(
            id = "2",
            datetime = Clock.System.now(),
            factorRecords = arrayListOf(rec1),
            tags = arrayListOf<Tag>(tag2)
        )
        every { dataLoader.getLogs() } returns listOf(log1, log2)
        every { dataLoader.logLoadingError() } returns null
        val viewModel = AllLogsViewModel(dataLoader, dataProcessor)
        advanceUntilIdle()

        assert(viewModel.logItems.value.size == 2)
        viewModel.onSearchQueryChange("ial")
        advanceUntilIdle()

        assert(viewModel.logItems.value.size == 1)
        assert(viewModel.logItems.value[0].tags.first() == tag2.value)
    }

    @Test
    fun mappingIsCorrect() = runTest {
        val rec1 = FactorRecord(
            id = "1",
            factorId = "1",
            wasPresent = true,
            numValue = 1.0,
            isNumeric = true,
            factorName = "test",
            factorCategory = FactorCategory.AUDITORY
        )
        val log1 = Log(
            id = "1",
            datetime = Clock.System.now(),
            factorRecords = arrayListOf(rec1),
            tags = arrayListOf<Tag>()
        )
        every { dataLoader.getLogs() } returns listOf(log1)
        every { dataLoader.logLoadingError() } returns null
        val viewModel = AllLogsViewModel(dataLoader, dataProcessor)
        advanceUntilIdle()

        val localDT = log1.datetime.toLocalDateTime(TimeZone.currentSystemDefault())
        val date = "${localDT.dayOfMonth}/${localDT.monthNumber}/${localDT.year}"
        val time = "${localDT.hour}:${localDT.minute}"
        assert(viewModel.logItems.value[0].title == "Log from $date at $time")
        assert(viewModel.logItems.value[0].records.first() == rec1.factorName)
        assert(viewModel.logItems.value[0].tags.isEmpty())
    }

    @Test
    fun onlyPresentFactorsMapped() = runTest {

        val rec1 = FactorRecord(
            id = "1",
            factorId = "1",
            wasPresent = false,
            numValue = 1.0,
            isNumeric = true,
            factorName = "test",
            factorCategory = FactorCategory.AUDITORY
        )
        val rec2 = FactorRecord(
            id = "2",
            factorId = "1",
            wasPresent = true,
            numValue = 2.0,
            isNumeric = true,
            factorName = "trial",
            factorCategory = FactorCategory.AUDITORY
        )
        val log1 = Log(
            id = "1",
            datetime = Clock.System.now(),
            factorRecords = arrayListOf(rec1, rec2),
            tags = arrayListOf<Tag>()
        )
        every { dataLoader.getLogs() } returns listOf(log1)
        every { dataLoader.logLoadingError() } returns null
        val viewModel = AllLogsViewModel(dataLoader, dataProcessor)
        advanceUntilIdle()

        assert(viewModel.logItems.value[0].records.size == 1)
        assert(viewModel.logItems.value[0].records.first() == rec2.factorName)
    }
}