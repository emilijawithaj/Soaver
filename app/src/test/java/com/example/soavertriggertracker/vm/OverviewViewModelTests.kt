package com.example.soavertriggertracker.vm

import com.example.soavertriggertracker.data.DataLoader
import com.example.soavertriggertracker.data.Factor
import com.example.soavertriggertracker.data.FactorCategory
import com.example.soavertriggertracker.data.FactorRecord
import com.example.soavertriggertracker.data.Log
import com.example.soavertriggertracker.data.Tag
import com.example.soavertriggertracker.viewModel.OverviewPageViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.Clock

class OverviewViewModelTests {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    val dataLoader = mockk<DataLoader>()

    @Before
    fun setup() {
    }

    //given 9 logs >0% only return 8 and in correct order
    @Test
    fun factorCapAndOrderCorrect() {
        every { dataLoader.getFactors() } returns listOf(
            factor7,
            factor4,
            factor9,
            factor2,
            factor5,
            factor6,
            factor1,
            factor8,
            factor3
        )
        every { dataLoader.getLogs() } returns listOf(log1, log2)
        every { dataLoader.logLoadingError() } returns null
        every { dataLoader.factorLoadingError() } returns null
        every { dataLoader.triggerLoadingError() } returns null
        every { dataLoader.getTriggers() } returns listOf()

        val viewModel = OverviewPageViewModel(dataLoader)

        assert(viewModel.commonFactors.value.size == 8)
        assert(viewModel.commonFactors.value[0].frequency == 100)
        assert(viewModel.commonFactors.value[1].frequency == 100)
        assert(viewModel.commonFactors.value[2].frequency == 100)
        assert(viewModel.commonFactors.value[3].frequency == 100)
        assert(viewModel.commonFactors.value[4].frequency == 50)
        assert(viewModel.commonFactors.value[5].frequency == 50)
        assert(viewModel.commonFactors.value[6].frequency == 50)
        assert(viewModel.commonFactors.value[7].frequency == 50)
    }

    @Test
    fun correctTriggers() {
        every { dataLoader.getTriggers() } returns listOf("trigger1", "trigger2")
        every { dataLoader.getLogs() } returns listOf()
        every { dataLoader.getFactors() } returns listOf()
        every { dataLoader.logLoadingError() } returns null
        every { dataLoader.factorLoadingError() } returns null
        every { dataLoader.triggerLoadingError() } returns null

        val viewModel = OverviewPageViewModel(dataLoader)
        assert(viewModel.triggers.value.size == 2)
        assert(viewModel.triggers.value[0] == "trigger1")
        assert(viewModel.triggers.value[1] == "trigger2")
    }

    @Test
    fun triggerLoadingError() {
        every { dataLoader.getTriggers() } returns listOf()
        every { dataLoader.getLogs() } returns listOf()
        every { dataLoader.getFactors() } returns listOf()
        every { dataLoader.logLoadingError() } returns null
        every { dataLoader.factorLoadingError() } returns null
        every { dataLoader.triggerLoadingError() } returns Exception("test")

        val viewModel = OverviewPageViewModel(dataLoader)
        assert(viewModel.triggerError.value)
    }

    @Test
    fun factorLoadingError() = runTest {
        //error shown on fator load fail only
        every { dataLoader.getTriggers() } returns listOf()
        every { dataLoader.getLogs() } returns listOf()
        every { dataLoader.getFactors() } returns listOf()
        every { dataLoader.logLoadingError() } returns null
        every { dataLoader.factorLoadingError() } returns Exception("test")
        every { dataLoader.triggerLoadingError() } returns null

        val viewModel = OverviewPageViewModel(dataLoader)
        assert(viewModel.factorError.value)

        //reset error
        coEvery { dataLoader.loadAll() } returns Unit
        every { dataLoader.factorLoadingError() } returns null
        every { dataLoader.logLoadingError() } returns null
        viewModel.onReload()
        assert(!viewModel.factorError.value)

        //error shown on log load fail
        every { dataLoader.factorLoadingError() } returns null
        every { dataLoader.logLoadingError() } returns Exception("test")

        viewModel.onReload()
        assert(viewModel.factorError.value)
    }

    @Test
    fun reloadReloadsBothCorrectly() {
        every { dataLoader.getTriggers() } returns listOf()
        every { dataLoader.getLogs() } returns listOf()
        every { dataLoader.getFactors() } returns listOf()
        every { dataLoader.logLoadingError() } returns null
        every { dataLoader.factorLoadingError() } returns null
        every { dataLoader.triggerLoadingError() } returns null
        val viewModel = OverviewPageViewModel(dataLoader)

        assert(viewModel.triggers.value.isEmpty())
        assert(viewModel.commonFactors.value.isEmpty())
        assert(viewModel.noOfLogs.value == 0)

        coEvery { dataLoader.loadAll() } returns Unit
        every { dataLoader.getTriggers() } returns listOf("trigger1", "trigger2")
        every { dataLoader.getLogs() } returns listOf(log1, log2)
        every { dataLoader.getFactors() } returns listOf(
            factor7,
            factor4,
            factor9,
            factor2,
            factor5,
            factor6,
            factor1,
            factor8,
            factor3
        )

        viewModel.onReload()
        assert(viewModel.triggers.value.size == 2)
        assert(viewModel.commonFactors.value.size == 8)
        assert(viewModel.noOfLogs.value == 2)
    }

    @Test
    fun onlyFactorsOverZeroDisplayed() {
        every { dataLoader.getFactors() } returns listOf(
            factor7,
            factor4,
            factor9,
            factor2,
            factor5,
            factor6,
            factor1,
            factor8,
            factor3
        )
        every { dataLoader.getLogs() } returns listOf(log2)
        every { dataLoader.logLoadingError() } returns null
        every { dataLoader.factorLoadingError() } returns null
        every { dataLoader.triggerLoadingError() } returns null
        every { dataLoader.getTriggers() } returns listOf()

        //only factors appearing in log1 shown
        val viewModel = OverviewPageViewModel(dataLoader)
        assert(viewModel.commonFactors.value.size == 4)
    }

    /*
     * common constructs
     */
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
    val factor4 = Factor(
        id = "4",
        name = "trial4",
        isNumeric = false,
        category = FactorCategory.INTERNAL
    )
    val factor5 = Factor(
        id = "5",
        name = "test5",
        isNumeric = false,
        category = FactorCategory.TACTILE
    )
    val factor6 = Factor(
        id = "6",
        name = "trial6",
        isNumeric = false,
        category = FactorCategory.OLFACTORY
    )
    val factor7 = Factor(
        id = "7",
        name = "test7",
        isNumeric = false,
        category = FactorCategory.GUSTATORY
    )
    val factor8 = Factor(
        id = "8",
        name = "trial8",
        isNumeric = false,
        category = FactorCategory.GUSTATORY
    )
    val factor9 = Factor(
        id = "9",
        name = "test9",
        isNumeric = false,
        category = FactorCategory.GUSTATORY
    )
    val log1 = Log(
        id = "123",
        datetime = Clock.System.now(),
        factorRecords = arrayListOf(
            FactorRecord(
                id = "abc",
                factorId = factor1.id!!,
                wasPresent = true,
                numValue = null,
                isNumeric = false,
                factorName = factor1.name,
                factorCategory = factor1.category
            ),
            FactorRecord(
                id = "abc",
                factorId = factor2.id!!,
                wasPresent = true,
                numValue = null,
                isNumeric = false,
                factorName = factor2.name,
                factorCategory = factor2.category
            ),
            FactorRecord(
                id = "abc",
                factorId = factor3.id!!,
                wasPresent = true,
                numValue = null,
                isNumeric = false,
                factorName = factor3.name,
                factorCategory = factor3.category
            ),
            FactorRecord(
                id = "abc",
                factorId = factor4.id!!,
                wasPresent = true,
                numValue = null,
                isNumeric = false,
                factorName = factor4.name,
                factorCategory = factor4.category
            ),
            FactorRecord(
                id = "abc",
                factorId = factor5.id!!,
                wasPresent = true,
                numValue = null,
                isNumeric = false,
                factorName = factor5.name,
                factorCategory = factor5.category
            ),
            FactorRecord(
                id = "abc",
                factorId = factor6.id!!,
                wasPresent = true,
                numValue = null,
                isNumeric = false,
                factorName = factor6.name,
                factorCategory = factor6.category
            ),
            FactorRecord(
                id = "abc",
                factorId = factor7.id!!,
                wasPresent = true,
                numValue = null,
                isNumeric = false,
                factorName = factor7.name,
                factorCategory = factor7.category
            ), FactorRecord(
                id = "abc",
                factorId = factor8.id!!,
                wasPresent = true,
                numValue = null,
                isNumeric = false,
                factorName = factor8.name,
                factorCategory = factor8.category
            ),
            FactorRecord(
                id = "abc",
                factorId = factor9.id!!,
                wasPresent = true,
                numValue = null,
                isNumeric = false,
                factorName = factor9.name,
                factorCategory = factor9.category
            )
        ),
        tags = arrayListOf<Tag>()
    )
    val log2 = Log(
        id = "123",
        datetime = Clock.System.now(),
        factorRecords = arrayListOf(
            FactorRecord(
                id = "abc",
                factorId = factor1.id!!,
                wasPresent = true,
                numValue = null,
                isNumeric = false,
                factorName = factor1.name,
                factorCategory = factor1.category
            ),
            FactorRecord(
                id = "abc",
                factorId = factor2.id!!,
                wasPresent = true,
                numValue = null,
                isNumeric = false,
                factorName = factor2.name,
                factorCategory = factor2.category
            ),
            FactorRecord(
                id = "abc",
                factorId = factor3.id!!,
                wasPresent = true,
                numValue = null,
                isNumeric = false,
                factorName = factor3.name,
                factorCategory = factor3.category
            ),
            FactorRecord(
                id = "abc",
                factorId = factor4.id!!,
                wasPresent = true,
                numValue = null,
                isNumeric = false,
                factorName = factor4.name,
                factorCategory = factor4.category
            ),
            FactorRecord(
                id = "abc",
                factorId = factor5.id!!,
                wasPresent = false,
                numValue = null,
                isNumeric = false,
                factorName = factor5.name,
                factorCategory = factor5.category
            ),
            FactorRecord(
                id = "abc",
                factorId = factor6.id!!,
                wasPresent = false,
                numValue = null,
                isNumeric = false,
                factorName = factor6.name,
                factorCategory = factor6.category
            ),
            FactorRecord(
                id = "abc",
                factorId = factor7.id!!,
                wasPresent = false,
                numValue = null,
                isNumeric = false,
                factorName = factor7.name,
                factorCategory = factor7.category
            ), FactorRecord(
                id = "abc",
                factorId = factor8.id!!,
                wasPresent = false,
                numValue = null,
                isNumeric = false,
                factorName = factor8.name,
                factorCategory = factor8.category
            ),
            FactorRecord(
                id = "abc",
                factorId = factor9.id!!,
                wasPresent = false,
                numValue = null,
                isNumeric = false,
                factorName = factor9.name,
                factorCategory = factor9.category
            )
        ),
        tags = arrayListOf<Tag>()
    )
}