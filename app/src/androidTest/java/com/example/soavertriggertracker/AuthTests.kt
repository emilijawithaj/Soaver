package com.example.soavertriggertracker

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.soavertriggertracker.data.AuthRepositoryImpl
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.createSupabaseClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Various auth tests. Tests involving sign up require manual removal of accounts from supabase before/after running.
 * (ie. only I can run them)
 * no other test emails to be used to avoid pollution
 */
@RunWith(AndroidJUnit4::class)
class AuthTests {

    private lateinit var client: SupabaseClient
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setup() {
        client = createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY,
        ) {
            install(Auth.Companion)
        }

        repository = AuthRepositoryImpl(client.auth)

    }

    @Test
    fun signInWithCorrectCredentials() = runBlocking {
        repository.sessionStatus.first { it !is SessionStatus.Initializing }
        assert(repository.sessionStatus.value is SessionStatus.NotAuthenticated)

        repository.signInWithEmail(BuildConfig.SUPABASE_TEST_USER_EMAIL, BuildConfig.SUPABASE_TEST_USER_PASSWORD)
        assert(repository.sessionStatus.value is SessionStatus.Authenticated)
        assert(repository.currentUser != null)
        assert(repository.currentUser!!.email == BuildConfig.SUPABASE_TEST_USER_EMAIL)
    }

    @Test (expected = AuthRestException::class)
    fun signInWithIncorrectCredentials() = runBlocking {
        repository.sessionStatus.first { it !is SessionStatus.Initializing }
        repository.signOut()
        assert(repository.sessionStatus.value is SessionStatus.NotAuthenticated)

        repository.signInWithEmail("incorrect", "incorrect")
        assert(repository.sessionStatus.value is SessionStatus.NotAuthenticated)
        assert(repository.currentUser == null)
    }

    @Test
    fun signOut() = runBlocking {
        client.auth.signInWith(Email) {
            email = BuildConfig.SUPABASE_TEST_USER_EMAIL
            password = BuildConfig.SUPABASE_TEST_USER_PASSWORD
        }
        assert(repository.sessionStatus.value is SessionStatus.Authenticated)
        assert(repository.currentUser != null)

        repository.signOut()
        assert(repository.sessionStatus.value is SessionStatus.NotAuthenticated)
        assert(repository.currentUser == null)
    }

    @Test
    fun signInWhenSignedIn() = runBlocking {
        client.auth.signInWith(Email) {
            email = BuildConfig.SUPABASE_TEST_USER_EMAIL
            password = BuildConfig.SUPABASE_TEST_USER_PASSWORD
        }

        assert(repository.sessionStatus.value is SessionStatus.Authenticated)
        assert(repository.currentUser != null)
        assert(repository.currentUser!!.email == BuildConfig.SUPABASE_TEST_USER_EMAIL)

        repository.signInWithEmail(BuildConfig.SUPABASE_TEST_EMAIL_TWO, BuildConfig.SUPABASE_TEST_PASSWORD_TWO)
        assert(repository.sessionStatus.value is SessionStatus.Authenticated)
        assert(repository.currentUser != null)
        assert(repository.currentUser!!.email == BuildConfig.SUPABASE_TEST_EMAIL_TWO)
    }

    @Test
    fun signOutWhenSignedOut() = runBlocking {
        repository.sessionStatus.first { it !is SessionStatus.Initializing }
        repository.signOut()
        assert(repository.sessionStatus.value is SessionStatus.NotAuthenticated)

        repository.signOut()
        assert(repository.sessionStatus.value is SessionStatus.NotAuthenticated)
    }

    @Test
    fun signUp() = runBlocking {
        repository.signUpWithEmail(BuildConfig.SUPABASE_THROWAWAY_EMAIL_ONE, BuildConfig.SUPABASE_THROWAWAY_PASSWORD )
    }

    @Test
    fun signUpExistingUser() = runBlocking {
        repository.signUpWithEmail(BuildConfig.SUPABASE_TEST_USER_EMAIL, BuildConfig.SUPABASE_TEST_USER_PASSWORD)
        assert(repository.sessionStatus.value is SessionStatus.NotAuthenticated)
        assert(repository.currentUser == null)
    }

    @Test(expected = AuthRestException::class)
    fun signUpWithInvalidEmail() = runBlocking {
        repository.signUpWithEmail("not-an-email", "password123")
    }

    @Test(expected = AuthRestException::class)
    fun signUpWithShortPassword() = runBlocking {
        repository.signUpWithEmail(BuildConfig.SUPABASE_THROWAWAY_EMAIL_TWO, BuildConfig.SUPABASE_THROWAWAY_PASSWORD)
    }

    @Test
    fun signUpNotAutoAuthenticating() = runBlocking {
        repository.signOut()
        repository.signUpWithEmail(BuildConfig.SUPABASE_THROWAWAY_EMAIL_ONE, BuildConfig.SUPABASE_THROWAWAY_PASSWORD)
        assert(repository.sessionStatus.value is SessionStatus.NotAuthenticated)
    }
}