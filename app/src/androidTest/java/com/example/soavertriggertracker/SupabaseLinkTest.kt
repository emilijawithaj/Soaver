package com.example.soavertriggertracker

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.soavertriggertracker.data.FactorCategory
import com.example.soavertriggertracker.data.LogSupabaseLinkImpl
import com.example.soavertriggertracker.data.dataTransferObjs.FactorDTO
import com.example.soavertriggertracker.data.dataTransferObjs.FactorDTOout
import com.example.soavertriggertracker.data.dataTransferObjs.FactorRecordDTOout
import com.example.soavertriggertracker.data.dataTransferObjs.LogDTOout
import com.example.soavertriggertracker.data.dataTransferObjs.TagDTOout
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID
import kotlin.time.Clock

/**
 * DESTRUCTIVE TESTS RUN       O N L Y        ON TEST ACCOUNT!!!
 */
@RunWith(AndroidJUnit4::class)
class SupabaseLinkTest {
    private lateinit var repository: LogSupabaseLinkImpl
    private lateinit var client: SupabaseClient

    //common components
    val fac1DTO = FactorDTO(
        id = "9a12bcab-0a13-48d1-bf78-46588c5bdd7b",
        name = "testFac",
        isNumeric = true,
        category = FactorCategory.VISUAL
    )
    val rec1DTO = FactorRecordDTOout(
        id = null,
        factorId = fac1DTO.id,
        boolVal = true,
        numVal = 1.0
    )
    val tag1DTO = TagDTOout(
        id = null,
        value = "testTag1"
    )

    /**
     * Set up test client
     */
    @Before
    fun setup() {
        client = createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY,
        ) {
            install(Postgrest.Companion)
            install(Auth.Companion) {
            }
        }

        //auth log in
        runBlocking {
            client.auth.signInWith(Email) {
                email = BuildConfig.SUPABASE_TEST_USER_EMAIL
                password = BuildConfig.SUPABASE_TEST_USER_PASSWORD
            }
        }

        repository = LogSupabaseLinkImpl(client.postgrest, client.auth)
    }


    @Test
    fun insertFullCorrect(): Unit = runTest {
        val logsNo = repository.getLogsDTO().size
        val rec2DTO = FactorRecordDTOout(
            id = null,
            factorId = fac1DTO.id,
            boolVal = false,
            numVal = 1.0
        )

        val log = LogDTOout(
            id = null,
            datetime = Clock.System.now(),
            factorRecords = listOf(rec2DTO),
            tags = listOf(tag1DTO)
        )

        //execute
        repository.putLogDTO(log)
        assert(repository.getLogsDTO().size == logsNo + 1)
    }

    @Test
    fun insertMultipleLogsAndMissingTags(): Unit = runTest {
        val logsNo = repository.getLogsDTO().size
        val rec2DTO = FactorRecordDTOout(
            id = null,
            factorId = fac1DTO.id,
            boolVal = false,
            numVal = 1.0
        )

        val log = LogDTOout(
            id = null,
            datetime = Clock.System.now(),
            factorRecords = listOf(rec2DTO),
            tags = listOf()
        )

        val log2 = LogDTOout(
            id = null,
            datetime = Clock.System.now(),
            factorRecords = listOf(rec2DTO),
            tags = listOf()
        )

        //execute
        repository.putLogDTO(log)
        repository.putLogDTO(log2)
        assert(repository.getLogsDTO().size == logsNo + 2)
    }

    /**
     * quick test that fetchall fetches SOMETHING
     */
    @Test
    fun fetchAll(): Unit = runTest {
        val logs = repository.getLogsDTO()
        assert(logs.isNotEmpty())
    }

    @Test
    fun fetchOne(): Unit = runTest {
        //make and push a log
        val rec2DTO = FactorRecordDTOout(
            id = null,
            factorId = fac1DTO.id,
            boolVal = false,
            numVal = 1.0
        )

        val log = LogDTOout(
            id = UUID.randomUUID().toString(),
            datetime = Clock.System.now(),
            factorRecords = listOf(rec2DTO),
            tags = listOf()
        )
        repository.putLogDTO(log)

        //fetch
        val logFetched = repository.getLogDTO(log.id!!)
        assert(logFetched != null)
        assert(logFetched!!.id == log.id)
        assert(logFetched.factorRecords.size == log.factorRecords.size)
        assert(logFetched.factorRecords[0].factorId == log.factorRecords[0].factorId)
    }

    @Test
    fun deleteLog(): Unit = runTest {
        //make and push a log
        val rec2DTO = FactorRecordDTOout(
            id = null,
            factorId = fac1DTO.id,
            boolVal = false,
            numVal = 1.0
        )

        val log = LogDTOout(
            id = UUID.randomUUID().toString(),
            datetime = Clock.System.now(),
            factorRecords = listOf(rec2DTO),
            tags = listOf()
        )
        val logId = repository.putLogDTO(log)

        //delete
        assert(repository.getLogDTO(logId) != null)
        repository.deleteLogDTO(logId)
        assert(repository.getLogDTO(logId) == null)
    }

    /*
            FACTORS
     */

    @Test
    fun insertFactor(): Unit = runTest {
        val factorsNo = repository.getFactorsDTO().size
        val factor = FactorDTOout(
            id = null,
            name = UUID.randomUUID().toString().take(8),
            isNumeric = true,
            category = FactorCategory.VISUAL
        )

        repository.putFactorDTO(factor)
        assert(repository.getFactorsDTO().size == factorsNo + 1)
    }


    /**
     * Quick test that fetchall factors fetches SOMETHING
     */
    @Test
    fun fetchAllFactors(): Unit = runTest {
        val factors = repository.getFactorsDTO()
        assert(factors.isNotEmpty())
    }

    @Test
    fun fetchOneFactor(): Unit = runTest {
        //get all factors and select one
        val factors = repository.getFactorsDTO()
        val factor = factors[0]

        //fetch that one
        val factorFetched = repository.getFactorDTO(factor.id)
        assert(factorFetched != null)
        assert(factorFetched!!.id == factor.id)
    }

    @Test
    fun deleteFactor(): Unit = runTest {
        //make and push a factor
        val factor = FactorDTOout(
            id = null,
            name = UUID.randomUUID().toString().take(8),
            isNumeric = true,
            category = FactorCategory.VISUAL
        )
        val fac = repository.putFactorDTO(factor)

        //delete
        assert(repository.getFactorDTO(fac) != null)
        repository.deleteFactorDTO(fac)
        assert(repository.getFactorDTO(fac) == null)
    }

    /*
        DUPLICATE ETC.
     */

    @Test
    fun insertDuplicateIdLog(): Unit = runTest {
        val id = UUID.randomUUID().toString()
        val log = LogDTOout(
            id = id,
            datetime = Clock.System.now(),
            factorRecords = listOf(rec1DTO),
            tags = listOf()
        )
        repository.putLogDTO(log)
        val log2 = LogDTOout(
            id = id,
            datetime = Clock.System.now(),
            factorRecords = listOf(rec1DTO),
            tags = listOf()
        )

        try {
            repository.putLogDTO(log2)
            assert(false)
        } catch (e: Exception) {
            assert(e is PostgrestRestException)
        }
    }

    @Test
    fun insertLogWithDuplicateFactors(): Unit = runTest {
        val log = LogDTOout(
            id = UUID.randomUUID().toString(),
            datetime = Clock.System.now(),
            factorRecords = listOf(rec1DTO, rec1DTO),
            tags = listOf()
        )

        try {
            repository.putLogDTO(log)
            assert(false)
        } catch (e: Exception) {
            assert(e is PostgrestRestException)
        }
    }

    @Test
    fun updateFactor(): Unit = runTest {
        //make and push a factor
        val factor = FactorDTOout(
            id = null,
            name = UUID.randomUUID().toString().take(8),
            isNumeric = true,
            category = FactorCategory.VISUAL
        )
        val fac = repository.putFactorDTO(factor)
        val orgFactor = repository.getFactorDTO(fac)
        assert(orgFactor!!.name == factor.name)

        val factorUpdated = FactorDTOout(
            id = fac,
            name = UUID.randomUUID().toString().take(8),
            isNumeric = false,
            category = FactorCategory.AUDITORY
        )

        repository.putFactorDTO(factorUpdated)
        val factorFetched = repository.getFactorDTO(fac)
        assert(factorFetched!!.name == factorUpdated.name)
        assert(factorFetched.category == factorUpdated.category)
    }

    @Test
    fun insertFactorDuplicateName(): Unit = runTest {
        val name = UUID.randomUUID().toString()
        val factor = FactorDTOout(
            id = null,
            name = name,
            isNumeric = true,
            category = FactorCategory.VISUAL
        )
        repository.putFactorDTO(factor)

        val factor2 = FactorDTOout(
            id = null,
            name = name,
            isNumeric = true,
            category = FactorCategory.AUDITORY
        )

        try {
            repository.putFactorDTO(factor2)
            assert(false)
        } catch (e: Exception) {
            assert(e is PostgrestRestException)
        }
    }

    @Test
    fun insertLogWithNoFactorRecords(): Unit = runTest {
        val log = LogDTOout(
            id = UUID.randomUUID().toString(),
            datetime = Clock.System.now(),
            factorRecords = listOf(),
            tags = listOf()
        )

        try {
            repository.putLogDTO(log)
            assert(false)
        } catch (e: Exception) {
            assert(e is IllegalArgumentException)
        }
    }

    /**
     * Delete added data after test
     */
    @After
    fun cleanup() {
        //todo
    }
}