package dev.decagon.godday.locationtracker.network

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import dev.decagon.godday.locationtracker.ultilities.POKEMON_BASE_URL
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    val gson = GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    val retrofit = Retrofit.Builder().baseUrl(POKEMON_BASE_URL)
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

}