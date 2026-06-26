package com.example.soavertriggertracker.data

import com.example.soavertriggertracker.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import javax.inject.Singleton

/**
 * Supabase DI configurations. Supabase client instance to be injected
 * across whole app.
 */
@InstallIn(SingletonComponent::class)
@Module
object Supabase {

    /**
     * Singleton supabase client setup, enables both auth and db
     */
    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY
        ) {
            install(Postgrest)
            install(Auth) {
                flowType = FlowType.PKCE //auth flow for mobile
                scheme = "app"
                host = "supabase.com"
            }
        }
    }

    /**
     * Supabase DB instance, extracted from the supabase client
     */
    @Provides
    @Singleton
    fun provideSupabaseDatabase(client: SupabaseClient): Postgrest {
        return client.postgrest
    }

    /**
     * Supabase Auth instance, extracted from the supabase client
     */
    @Provides
    @Singleton
    fun provideSupabaseAuth(client: SupabaseClient): Auth {
        return client.auth
    }
}