package com.example.vietnam_travel_itinerary_android.ui.auth

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.vietnam_travel_itinerary_android.data.api.RetrofitInstance
import com.example.vietnam_travel_itinerary_android.SupabaseObject
import com.example.vietnam_travel_itinerary_android.ui.itinerary.ItineraryViewModel
import com.example.vietnam_travel_itinerary_android.data.repository.PlaceRepository
import com.example.vietnam_travel_itinerary_android.ui.community.CommunityViewModel
import com.example.vietnam_travel_itinerary_android.data.repository.CommunityRepository

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

        initializer {
            ItineraryViewModel(
                placeRepo = PlaceRepository()
            )
        }

        initializer {
            CommunityViewModel(
                repository = CommunityRepository(SupabaseObject.client),
                supabase = SupabaseObject.client
            )
        }
    }
}