package dev.decagon.godday.locationtracker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.decagon.godday.locationtracker.ultilities.POKEMON_IMAGE_URL
import dev.decagon.godday.locationtracker.R
import dev.decagon.godday.locationtracker.model.PokemonCharacters
import dev.decagon.godday.locationtracker.ultilities.Methods.bindImage
import dev.decagon.godday.locationtracker.ultilities.Methods.getIndexFromUrl
import java.util.*

/**
 * This is the recyclerview adapter class that display the images of all pokemon characters
 * retrieved from the API using a grid layout manager. It also uses a custom click listener to
 * respond to click events, when a view is clicked.
 */

class PokemonApiServiceAdapter(private val onClickListener: OnClickListener) :
    RecyclerView.Adapter<PokemonApiServiceAdapter.PokemonCharactersViewHolder>() {
    private var pokemonCharacters = mutableListOf<PokemonCharacters>()
    lateinit var view: View


    class PokemonCharactersViewHolder(private val view: View) :
        RecyclerView.ViewHolder(view) {

        // This method binds views with the appropriate data. It uses glide library in
        // loading images into image views.
        fun bind(pokemonCharacter: PokemonCharacters) {
            val name: TextView = view.findViewById(R.id.pokemon_name)
            val image: ImageView = view.findViewById(R.id.pokemon_image)
            name.text = pokemonCharacter.name.capitalize(Locale.ROOT)
            val pokemonIndex = getIndexFromUrl(pokemonCharacter.url)
            val pokemonImgUrl = "$POKEMON_IMAGE_URL$pokemonIndex.png"
            pokemonCharacter.imageUrl = pokemonImgUrl
            pokemonCharacter.index = pokemonIndex
            bindImage(pokemonImgUrl, image)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonCharactersViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view, parent, false)
        view = adapterLayout
        return PokemonCharactersViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: PokemonCharactersViewHolder, position: Int) {
        val pokemon = pokemonCharacters[position]
        holder.itemView.setOnClickListener { onClickListener.onClick(pokemon) }
        holder.bind(pokemon)
    }

    override fun getItemCount(): Int = pokemonCharacters.size

    // This method is used to supply the adapter class with the data retrieved from the API.
    fun loadPokemonCharacters(pokemon: List<PokemonCharacters>) {
        this.pokemonCharacters = pokemon as MutableList<PokemonCharacters>
        notifyDataSetChanged()
    }

    // Custom clickListener that handles click events
    class OnClickListener(val clickListener: (pokemon: PokemonCharacters) -> Unit) {
        fun onClick(pokemon: PokemonCharacters) = clickListener(pokemon)
    }
}