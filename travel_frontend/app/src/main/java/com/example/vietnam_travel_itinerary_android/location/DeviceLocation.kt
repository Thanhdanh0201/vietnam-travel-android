package com.example.vietnam_travel_itinerary_android.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private fun Location.isUsableFix(): Boolean =
    (latitude != 0.0 || longitude != 0.0) && isFresh()

/** Bỏ cache vị trí cũ (vd. Hà Nội khi user đã sang TP.HCM). */
private fun Location.isFresh(): Boolean {
    val ageMs = System.currentTimeMillis() - time
    return ageMs in 0..(15 * 60 * 1000)
}

/** Biên giới gần đúng của Việt Nam (để bỏ GPS emulator nước ngoài / tọa độ lỗi). */
fun isInVietnam(lat: Double, lng: Double): Boolean =
    lat in 8.0..24.5 && lng in 102.0..110.0

@SuppressLint("MissingPermission")
suspend fun FusedLocationProviderClient.fetchBestLocation(): Location? {
    return try {
        // Ưu tiên fix mới — lastLocation hay cache tọa độ cũ (vd. Lạng Sơn / Mountain View)
        getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token,
        ).await()?.takeIf { it.isUsableFix() }
            ?: lastLocation.await()?.takeIf { it.isUsableFix() && it.isFresh() }
    } catch (_: Exception) {
        null
    }
}

/** Tỉnh/thành hoặc quận tại Việt Nam (ưu tiên adminArea — TP.HCM, Hà Nội…). */
suspend fun reverseGeocodeRegion(context: Context, lat: Double, lng: Double): String? =
    withContext(Dispatchers.IO) {
        try {
            if (!Geocoder.isPresent()) return@withContext null
            val geocoder = Geocoder(context, Locale.forLanguageTag("vi-VN"))
            @Suppress("DEPRECATION")
            val list = geocoder.getFromLocation(lat, lng, 1)
            list?.firstOrNull()?.let { a ->
                if (a.countryCode?.uppercase(Locale.ROOT) != "VN") return@let null
                a.adminArea?.takeIf { it.isNotBlank() }
                    ?: a.subAdminArea?.takeIf { it.isNotBlank() }
                    ?: a.locality?.takeIf { it.isNotBlank() }
            }
        } catch (_: Exception) {
            null
        }
    }

/** @deprecated Dùng [reverseGeocodeRegion] cho nhãn thời tiết. */
suspend fun reverseGeocodeLocality(context: Context, lat: Double, lng: Double): String? =
    reverseGeocodeRegion(context, lat, lng)
