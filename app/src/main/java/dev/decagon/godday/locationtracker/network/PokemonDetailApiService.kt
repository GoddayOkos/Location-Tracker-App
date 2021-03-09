package dev.decagon.godday.locationtracker.network

import dev.decagon.godday.locationtracker.model.PokemonDetails
import retrofit2.http.GET
import retrofit2.http.Path
import rx.Observable

interface PokemonDetailApiService {
    @GET("{pokemonId}")
    fun getPokemonDetails(@Path("pokemonId") pokemonId: String): Observable<PokemonDetails>

    object PokemonDetailApiClient {
        val retrofitService: PokemonDetailApiService by lazy {
            ApiClient.retrofit.create(PokemonDetailApiService::class.java)
        }
    }
}