package com.example.vietnam_travel_itinerary_android.data.local

import android.content.Context
import com.example.vietnam_travel_itinerary_android.data.model.WeatherCityPresets

private const val PREFS_NAME = "weather_favorites"
private const val KEY_FAVORITE_CITY_ID = "favorite_city_id"

class WeatherFavoritePreferences(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getFavoriteCityId(): String {
        val saved = prefs.getString(KEY_FAVORITE_CITY_ID, null)?.trim().orEmpty()
        if (saved.isNotEmpty() && WeatherCityPresets.cities.any { it.id == saved }) {
            return saved
        }
        return WeatherCityPresets.DEFAULT_FAVORITE_CITY_ID
    }

    fun setFavoriteCityId(cityId: String) {
        prefs.edit().putString(KEY_FAVORITE_CITY_ID, cityId).apply()
    }
}
