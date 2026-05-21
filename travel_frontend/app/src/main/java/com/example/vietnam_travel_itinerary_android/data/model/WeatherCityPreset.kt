package com.example.vietnam_travel_itinerary_android.data.model

/** Điểm xem thời tiết cố định — vuốt ngang trên trang chủ. */
data class WeatherCityPreset(
    val id: String,
    val displayName: String,
    val subtitle: String?,
    val lat: Double,
    val lng: Double,
)

data class WeatherCitySlide(
    val id: String,
    val displayName: String,
    val subtitle: String?,
    val lat: Double,
    val lng: Double,
    val weather: WeatherData? = null,
    val isLoading: Boolean = false,
    val loadFailed: Boolean = false,
)

object WeatherCityPresets {
    val cities: List<WeatherCityPreset> = listOf(
        WeatherCityPreset("hanoi", "Hà Nội", "Thủ đô", 21.0285, 105.8542),
        WeatherCityPreset("hcmc", "Hồ Chí Minh", "TP.HCM", 10.7769, 106.7009),
        WeatherCityPreset("mui_ne", "Mũi Né", "Bình Thuận", 10.9574, 108.2987),
        WeatherCityPreset("da_lat", "Đà Lạt", "Lâm Đồng", 11.9404, 108.4583),
        WeatherCityPreset("da_nang", "Đà Nẵng", "Miền Trung", 16.0544, 108.2022),
    )

    /** Mặc định khi chưa chọn yêu thích (TP.HCM). */
    const val DEFAULT_FAVORITE_CITY_ID = "hcmc"

    fun pageIndexForCity(cityId: String): Int {
        val idx = cities.indexOfFirst { it.id == cityId }
        return if (idx >= 0) idx else cities.indexOfFirst { it.id == DEFAULT_FAVORITE_CITY_ID }.coerceAtLeast(0)
    }

    fun initialSlides(): List<WeatherCitySlide> = cities.map { p ->
        WeatherCitySlide(
            id = p.id,
            displayName = p.displayName,
            subtitle = p.subtitle,
            lat = p.lat,
            lng = p.lng,
            isLoading = true,
        )
    }
}
