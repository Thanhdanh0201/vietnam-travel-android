package com.example.vietnam_travel_itinerary_android.ui.auth

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.vietnam_travel_itinerary_android.data.api.RetrofitInstance
import com.example.vietnam_travel_itinerary_android.SupabaseObject

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            RegisterViewModel(
                supabase = SupabaseObject.client,
                api = RetrofitInstance.api
            )
        }

        initializer {
            OtpViewModel(
                supabase = SupabaseObject.client,
                api = RetrofitInstance.api
            )
        }

        initializer {
            LoginViewModel(
                supabase = SupabaseObject.client
            )
        }

        initializer {
            ForgotPasswordViewModel(
                supabase = SupabaseObject.client
            )
        }

        initializer {
            ResetPasswordViewModel(
                supabase = SupabaseObject.client
            )
        }

    }
}