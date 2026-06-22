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
import com.example.vietnam_travel_itinerary_android.data.repository.ItineraryRepository
import com.example.vietnam_travel_itinerary_android.data.repository.ProfileRepository
import com.example.vietnam_travel_itinerary_android.data.repository.AdminRepository
import com.example.vietnam_travel_itinerary_android.data.repository.PlaceSuggestionRepository
import com.example.vietnam_travel_itinerary_android.data.repository.SearchRepository
import com.example.vietnam_travel_itinerary_android.data.repository.EventRepository
import com.example.vietnam_travel_itinerary_android.ui.profile.EditProfileViewModel
import com.example.vietnam_travel_itinerary_android.ui.profile.FollowListViewModel
import com.example.vietnam_travel_itinerary_android.ui.profile.ProfileViewModel
import com.example.vietnam_travel_itinerary_android.ui.notification.NotificationViewModel
import com.example.vietnam_travel_itinerary_android.ui.search.SearchViewModel
import com.example.vietnam_travel_itinerary_android.ui.places.AllPlacesViewModel
import com.example.vietnam_travel_itinerary_android.ui.events.AllEventsViewModel
import com.example.vietnam_travel_itinerary_android.ui.suggestion.PlaceSuggestionViewModel
import com.example.vietnam_travel_itinerary_android.ui.admin.AdminPlaceSuggestionsViewModel
import com.example.vietnam_travel_itinerary_android.ui.admin.AdminReportsViewModel

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
                supabase = SupabaseObject.client,
                profileRepository = ProfileRepository(SupabaseObject.client),
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
                placeRepo = PlaceRepository(),
                itineraryRepo = ItineraryRepository(SupabaseObject.client),
                profileRepo = ProfileRepository(SupabaseObject.client),
            )
        }

        initializer {
            CommunityViewModel(
                repository = CommunityRepository(SupabaseObject.client),
                supabase = SupabaseObject.client
            )
        }

        initializer {
            ProfileViewModel(
                repository = ProfileRepository(SupabaseObject.client),
                supabase = SupabaseObject.client,
            )
        }

        initializer {
            FollowListViewModel(
                repository = ProfileRepository(SupabaseObject.client),
                supabase = SupabaseObject.client,
            )
        }

        initializer {
            EditProfileViewModel(
                repository = ProfileRepository(SupabaseObject.client),
                supabase = SupabaseObject.client,
            )
        }

        initializer {
            NotificationViewModel(
                repository = CommunityRepository(SupabaseObject.client),
                profileRepository = ProfileRepository(SupabaseObject.client),
                itineraryRepository = ItineraryRepository(SupabaseObject.client),
                supabase = SupabaseObject.client,
            )
        }

        initializer {
            SearchViewModel(
                placeRepository = PlaceRepository(),
                itineraryRepository = ItineraryRepository(SupabaseObject.client),
                communityRepository = CommunityRepository(SupabaseObject.client),
                searchRepository = SearchRepository(SupabaseObject.client),
                supabase = SupabaseObject.client,
            )
        }

        initializer {
            PlaceSuggestionViewModel(
                repository = PlaceSuggestionRepository(SupabaseObject.client)
            )
        }

        initializer {
            AdminPlaceSuggestionsViewModel(
                repository = AdminRepository(SupabaseObject.client)
            )
        }

        initializer {
            AdminReportsViewModel(
                repository = AdminRepository(SupabaseObject.client)
            )
        }

        initializer {
            AllPlacesViewModel(
                placeRepository = PlaceRepository()
            )
        }

        initializer {
            AllEventsViewModel(
                eventRepository = EventRepository()
            )
        }
    }
}