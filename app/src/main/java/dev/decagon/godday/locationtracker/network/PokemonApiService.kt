package dev.decagon.godday.locationtracker.network

import dev.decagon.godday.locationtracker.ultilities.POKEMON_BASE_URL
import dev.decagon.godday.locationtracker.model.Pokemon
import retrofit2.http.GET
import retrofit2.http.Query
import rx.Observable

interface PokemonApiService {
    @GET(POKEMON_BASE_URL)
    fun getPokemon(@Query("offset") offset: Int, @Query("limit") limit: Int):
            Observable<Pokemon>

    object PokemonApiClient {
        val retrofitService: PokemonApiService by lazy {
            ApiClient.retrofit.create(PokemonApiService::class.java)
        }
    }
}