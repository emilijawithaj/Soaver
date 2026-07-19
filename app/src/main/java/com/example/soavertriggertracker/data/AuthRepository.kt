package com.example.soavertriggertracker.data

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface AuthRepository {
    val sessionStatus: Flow<SessionStatus>
    val currentUser: UserInfo?

    suspend fun signInWithEmail(userEmail: String, userPassword: String)
    suspend fun signUpWithEmail(userEmail: String, userPassword: String)
    suspend fun signOut()
}

class AuthRepositoryImpl @Inject constructor(
    private val auth: Auth
) : AuthRepository {
    //log in state exposed as flow
    override val sessionStatus get() = auth.sessionStatus
    override val currentUser get() = auth.currentUserOrNull()

    /**
     * Sign in with email and password
     * @param userEmail email of the user
     * @param userPassword password of the user
     * @throws AuthRestException if invalid credentials
     * @throws Exception http exceptions if network error
     */
    override suspend fun signInWithEmail(userEmail: String, userPassword: String) {
        withContext(Dispatchers.IO) {
            auth.signInWith(Email) {
                email = userEmail
                password = userPassword
            }
        }
    }

    /**
     * Sign up with email and password
     * @param userEmail email of the user
     * @param userPassword password of the user
     */
    override suspend fun signUpWithEmail(userEmail: String, userPassword: String) {
        withContext(Dispatchers.IO) {
            auth.signUpWith(
                provider = Email,
                redirectUrl = "https://soaver.space"
            ) {
                email = userEmail
                password = userPassword
            }
        }
    }

    /**
     * sign out current session
     */
    override suspend fun signOut() {
        withContext(Dispatchers.IO) {
            auth.signOut()
        }
    }
}