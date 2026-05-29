package com.example.vietnam_travel_itinerary_android

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.memory.MemoryCache
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.request.crossfade

class TravelApplication : Application(), SingletonImageLoader.Factory {

    override fun newImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(100L * 1024 * 1024) // 100MB
                    .build()
            }
            .components {
                add(OkHttpNetworkFetcherFactory())
            }
            .crossfade(true)
            .build()
    }
}
