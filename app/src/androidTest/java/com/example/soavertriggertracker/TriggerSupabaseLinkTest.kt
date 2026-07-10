package com.example.soavertriggertracker

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.soavertriggertracker.data.TriggerSupabaseLinkImpl
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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID


@RunWith(AndroidJUnit4::class)
class TriggerSupabaseLinkTest {
    private lateinit var repository: TriggerSupabaseLinkImpl
    private lateinit var client: SupabaseClient

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

        repository = TriggerSupabaseLinkImpl(client.postgrest, client.auth)
    }


    @Test
    fun insertFullCorrect() = runTest {
        val triggersNo = repository.getTriggers().size
        //random text to ensure no duplicate
        val trigger = UUID.randomUUID().toString().take(8)

        repository.putTrigger(trigger)
        assert(repository.getTriggers().size == triggersNo + 1)
    }


    /**
     * quick test that fetchall fetches SOMETHING
     */
    @Test
    fun fetchAll() = runTest {
        val triggers = repository.getTriggers()
        assert(triggers.isNotEmpty())
    }

    @Test
    fun deleteTriggers() = runTest {
        val trigger = UUID.randomUUID().toString().take(8)
        repository.putTrigger(trigger)
        assert(repository.getTriggers().contains(trigger))
        repository.deleteTrigger(trigger)
        assert(!repository.getTriggers().contains(trigger))
    }

    @Test(expected = PostgrestRestException::class)
    fun insertDuplicateTrigger() = runTest {
        val trigger = "test"
        repository.putTrigger(trigger)
        repository.putTrigger(trigger)
    }

    @Test(expected = IllegalArgumentException::class)
    fun insertEmptyTrigger() = runTest {
        repository.putTrigger("")
    }

    @Test
    fun deleteNonExistentTrigger() = runTest {
        val noOfTriggers = repository.getTriggers().size
        repository.deleteTrigger(UUID.randomUUID().toString().take(12))
        assert(repository.getTriggers().size == noOfTriggers)
    }
}