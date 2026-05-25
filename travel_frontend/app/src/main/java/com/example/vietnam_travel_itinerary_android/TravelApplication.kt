package com.example.vietnam_travel_itinerary_android

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory

class TravelApplication : Application(), SingletonImageLoader.Factory {

    override fun newImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory())
            }
            .build()
    }
}
