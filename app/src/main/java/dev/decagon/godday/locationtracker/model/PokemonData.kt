package dev.decagon.godday.locationtracker.model

import com.squareup.moshi.Json

/**
 * This class/file contains all the classes that are used in modeling the json data format of the
 * pokeAPI. The classes model some of the relevant information necessary for the completion of this task
 * and skips all other information from the API.
 */

// A model of the list of all pokemon characters. This is use in making the first API call.
data class Pokemon(val results: List<PokemonCharacters>)

// A model of each pokemon character

data class PokemonCharacters(val name: String, val url: String) {
    var imageUrl: String = ""
    var index: String = ""
}

// A model of the details of each pokemon character. This is used in making the second API call and
// updating the details page of each pokemon character, when they are clicked on.

data class PokemonDetails(
    val abilities: List<Abilities>,
    @Json(name = "base_experience") val baseExperience: Long,
    @Json(name = "game_indices") val gameIndices: List<GameIndex>,
    val height: Long,
    val id: Long,
    val moves: List<MoveStyle>,
    val species: General,
    val sprites: Images,
    val stats: List<Stats>,
    val types: List<Types>,
    val weight: Long
)


// A model of version group section
data class VersionGroup(
    @Json(name = "level_learned_at") val levelLearnedAt: Long,
    @Json(name = "move_learn_method") val moveLearnMethod: General,
    @Json(name = "version_group") val versionGroup: General
)

// A model of the name property of any of the pokemon.
// This is used whenever only the name property needs to taken from the json data format
// hence it's called General because, it models no specific class/object
data class General(val name: String)

// Model's the abilities object of each pokemon character
data class Abilities(val ability: General, @Json(name = "is_hidden") val isHidden: Boolean, val slot: Long)

// Model of the game index of each pokemon character
data class GameIndex(@Json(name = "game_index") val gameIndex: Long, val version: General)

// Model's the move styles of each pokemon character
data class MoveStyle(val move: General, @Json(name = "version_group_details") val versionGroupDetails: List<VersionGroup>)

// Model of the sprites properties of each pokemon. This is the source of the little images used
// in populating the UI of the details page of each pokemon character.
data class Images(
    @Json(name = "back_default") val backDefault: String,
    @Json(name = "back_shiny") val backShiny: String,
    @Json(name = "front_default") val frontDefault: String,
    @Json(name = "front_shiny") val frontShiny: String
)

// Model's the stats of each pokemon character
data class Stats(@Json(name = "base_stat") val baseStat: Long, val effort: Long, val stat: General)

// Model's the types of each pokemon character
data class Types(val slot: Long, val type: General)
