package com.example.vietnam_travel_itinerary_android

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

object SupabaseObject {
    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = "https://jfaxmpasqkzztnvckqjl.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImpmYXhtcGFzcWt6enRudmNrcWpsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzQzMjIxMTgsImV4cCI6MjA4OTg5ODExOH0.ZBD2zc-u1mjFDdqaMHKHBiiXBtJBydNBSPAS4Y_XzSU"
    ) {
        // Install modules
        install(Auth) {
            val isAndroid = System.getProperty("java.runtime.name")?.contains("Android", ignoreCase = true) == true
            if (!isAndroid) {
                sessionManager = io.github.jan.supabase.auth.MemorySessionManager()
                codeVerifierCache = io.github.jan.supabase.auth.MemoryCodeVerifierCache()
            }
        }
        install(Postgrest)
        install(Storage)
        install(Realtime)
    }
}