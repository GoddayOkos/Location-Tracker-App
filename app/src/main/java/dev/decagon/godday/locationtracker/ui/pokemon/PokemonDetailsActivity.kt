package dev.decagon.godday.locationtracker.ui.pokemon

import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.Transition
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import dev.decagon.godday.locationtracker.R
import dev.decagon.godday.locationtracker.model.PokemonDetails
import dev.decagon.godday.locationtracker.network.PokemonDetailApiService
import dev.decagon.godday.locationtracker.ultilities.DEFAULT_POKEMON_SIZE
import dev.decagon.godday.locationtracker.ultilities.Methods
import dev.decagon.godday.locationtracker.ultilities.Methods.bindImage
import io.reactivex.disposables.Disposable
import rx.Observer
import rx.Scheduler
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.*
import java.util.concurrent.Flow

class PokemonDetailsActivity : AppCompatActivity() {

    private lateinit var image: ImageView
    private lateinit var name: TextView
    private lateinit var features: TextView
    private lateinit var features2: TextView
    private lateinit var statusImage: ImageView
    private lateinit var status: TextView
    private var subscription: Subscription? = null
    private lateinit var image1: ImageView
    private lateinit var image2: ImageView
    private lateinit var image3: ImageView
    private lateinit var image4: ImageView
    private lateinit var networkDisposable: Disposable
    private val TAG = PokemonDetailsActivity::class.java.simpleName

    companion object {
        private const val POKEMON_NAME = "pokemonName"
        private const val POKEMON_IMG_URL = "pokemonImgUrl"
        private const val POKEMON_ID = "pokemonId"

        // Intent to navigate to this activity and capture the necessary
        // data needed to setup the page
        fun newIntent(context: Context, pokemonName: String, pokemonImgUrl: String, pokemonId: String): Intent {
            return Intent(context, PokemonDetailsActivity::class.java).also {
                it.putExtra(POKEMON_NAME, pokemonName)
                it.putExtra(POKEMON_IMG_URL, pokemonImgUrl)
                it.putExtra(POKEMON_ID, pokemonId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pokemon_details)

        // Set the title of the page with the name of the pokemon
        // character that is displayed
        title = intent.getStringExtra(POKEMON_NAME).toString().capitalize(Locale.ROOT) + " Details"

        // Initialise the view components
        image = findViewById(R.id.pokemon_image)
        name = findViewById(R.id.pokemon_name)
        features = findViewById(R.id.items)
        features2 = findViewById(R.id.items2)
        statusImage = findViewById(R.id.status_image)
        status = findViewById(R.id.error_msg)
        image1 = findViewById(R.id.pokemon_image1)
        image2 = findViewById(R.id.pokemon_image2)
        image3 = findViewById(R.id.pokemon_image3)
        image4 = findViewById(R.id.pokemon_image4)

        setupViews()
        fetchDetails(intent.getStringExtra(POKEMON_ID).toString()) // Make a call to the API to get the details of the pokemon

        // Set the duration of the shared element transition animation
        window.sharedElementEnterTransition = enterTransition()
        window.sharedElementExitTransition = exitTransition()

    }

    // Reacting to network changes
    override fun onResume() {
        super.onResume()
        networkDisposable = ReactiveNetwork.observeNetworkConnectivity(applicationContext)
            .subscribeOn(io.reactivex.schedulers.Schedulers.io())
            .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
            .subscribe { connectivity: Connectivity ->
                Log.d(TAG, connectivity.toString())
                val state = connectivity.state()
                if (state == NetworkInfo.State.CONNECTED) {
                    statusImage.visibility = View.GONE
                    status.visibility = View.GONE
                    features.visibility = View.VISIBLE
                    features2.visibility = View.VISIBLE
                    image.visibility = View.VISIBLE
                    name.visibility = View.VISIBLE
                    image1.visibility = View.VISIBLE
                    image2.visibility = View.VISIBLE
                    image3.visibility = View.VISIBLE
                    image4.visibility = View.VISIBLE
                    findViewById<TextView>(R.id.gallery).visibility = View.VISIBLE
                    findViewById<TextView>(R.id.features).visibility = View.VISIBLE
                } else if (state == NetworkInfo.State.DISCONNECTED) {
                    features.visibility = View.GONE
                    features2.visibility = View.GONE
                    statusImage.visibility = View.VISIBLE
                    status.visibility = View.VISIBLE
                    image.visibility = View.GONE
                    name.visibility = View.GONE
                    image1.visibility = View.GONE
                    image2.visibility = View.GONE
                    image3.visibility = View.GONE
                    image4.visibility = View.GONE
                    findViewById<TextView>(R.id.gallery).visibility = View.GONE
                    findViewById<TextView>(R.id.features).visibility = View.GONE
                }
            }
    }

    // Setup the views
    private fun setupViews() {
        bindImage(intent.getStringExtra(POKEMON_IMG_URL), image)
        name.text = intent.getStringExtra(POKEMON_NAME).toString().capitalize(Locale.ROOT)
    }

    private fun enterTransition(): Transition = ChangeBounds().apply { duration = 1000 }     // Define the entrance duration of the animation

    // Define the exit duration of the animation
    private fun exitTransition(): Transition {
        return ChangeBounds().apply {
            interpolator = DecelerateInterpolator()
            duration = 1000
        }
    }

    // Unsubscribe the subscription since its no longer needed at this point
    override fun onDestroy() {
        if (subscription != null && !subscription!!.isUnsubscribed) {
            subscription!!.unsubscribe()
        }
        super.onDestroy()
    }

    // Get the details of the pokemon character by making a network call to the server
    // using rxjava
    private fun fetchDetails(id: String) {
        subscription = PokemonDetailApiService.PokemonDetailApiClient.retrofitService
            .getPokemonDetails(id).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<PokemonDetails> {
                override fun onCompleted() {
                    Log.d(TAG, "In OnCompleted()")
                }

                override fun onError(e: Throwable?) {
                    Log.d("ERROR:", "${e!!.message}")
                    statusImage.visibility = View.VISIBLE
                    status.text = e.message
                }

                // Populate the UI with the details retrieved from the API
                override fun onNext(it: PokemonDetails) {
                    bindImage(it.sprites.frontDefault, image1)
                    bindImage(it.sprites.backDefault, image2)
                    bindImage(it.sprites.frontShiny, image3)
                    bindImage(it.sprites.backShiny, image4)

                    features.text = getString(R.string.features_text, it.abilities[0].ability.name.capitalize(),
                        it.abilities[0].isHidden.toString(), it.abilities[0].slot.toString(), it.abilities[0].ability.name.capitalize(),
                        it.abilities[0].isHidden.toString(), it.abilities[0].slot.toString(), it.baseExperience.toString())

                    features2.text = getString(R.string.features_text_2, it.gameIndices[0].gameIndex.toString(),
                        it.height.toString(), it.id.toString(),
                        it.moves[0].move.name.capitalize(), it.moves[1].move.name.capitalize(), it.moves[2].move.name.capitalize(),
                        it.moves[3].move.name.capitalize(), it.moves[4].move.name.capitalize(), it.moves[0].move.name.capitalize(),
                        it.moves[2].move.name.capitalize(), it.moves[4].move.name.capitalize(), it.moves[1].move.name.capitalize(),
                        it.moves[3].move.name.capitalize(), it.stats[0].baseStat.toString(), it.stats[0].effort.toString(),
                        it.stats[0].stat.name,
                        it.stats[1].baseStat.toString(), it.stats[1].effort.toString(), it.stats[1].stat.name,
                        it.stats[2].baseStat.toString(), it.stats[2].effort.toString(),
                        it.stats[2].stat.name, it.stats[3].baseStat.toString(), it.stats[3].effort.toString(), it.stats[3].stat.name,
                        it.stats[4].baseStat.toString(),
                        it.stats[4].effort.toString(), it.stats[4].stat.name, it.stats[5].baseStat.toString(),
                        it.stats[5].effort.toString(), it.stats[5].stat.name,
                        it.types[0].slot.toString(), it.types[0].type.name, it.weight.toString())
                }
            })
    }
}