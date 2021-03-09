package dev.decagon.godday.locationtracker.ui.pokemon

import android.net.NetworkInfo
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair.create
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import dev.decagon.godday.locationtracker.R
import dev.decagon.godday.locationtracker.adapter.PokemonApiServiceAdapter
import dev.decagon.godday.locationtracker.model.Pokemon
import dev.decagon.godday.locationtracker.network.PokemonApiService
import dev.decagon.godday.locationtracker.ultilities.DEFAULT_POKEMON_SIZE
import io.reactivex.disposables.Disposable
import rx.Observer
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action1
import rx.schedulers.Schedulers


class PokemonGalleryActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var statusImage: ImageView
    private lateinit var status: TextView
    private lateinit var adapter: PokemonApiServiceAdapter
    private lateinit var searchBox: EditText
    private lateinit var doneIcon: ImageView
    private lateinit var loading: ProgressBar
    private var subscription: Subscription? = null
    private lateinit var networkDisposable: Disposable
    private val TAG = PokemonGalleryActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pokemon_gallery)
        title = getString(R.string.title_pokemon_gallery)                           // Set the title of the page

        // Initializing the adapter and defining the click listener. The click listener
        // passes properties of the pokemon as extras into the intent and transitions to the
        // next activity using shared element transitions
        adapter = PokemonApiServiceAdapter(PokemonApiServiceAdapter.OnClickListener {
            val pokemonImg: ImageView = adapter.view.findViewById(R.id.pokemon_image)
            val pokemonName: TextView = adapter.view.findViewById(R.id.pokemon_name)
            val imagePair = create(pokemonImg as View, "image")
            val namePair = create(pokemonName as View, "name")
            val options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(this, imagePair, namePair)
            ActivityCompat.startActivity(
                this,
                PokemonDetailsActivity.newIntent(this, it.name, it.imageUrl, it.index),
                options.toBundle()
            )
        })

        // Initialise the view components
        loading = findViewById(R.id.progressBar)
        statusImage = findViewById(R.id.status_image)
        status = findViewById(R.id.error_msg)
        recyclerView = findViewById(R.id.recycler_view_grid)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter
        searchBox = findViewById(R.id.filterNumber)
        doneIcon = findViewById(R.id.done)

        // Define the done icon to set the number of pokemon
        // characters displayed on the screen according to the value
        // entered by the user
        doneIcon.setOnClickListener {
            searchBox.visibility = View.GONE
            doneIcon.visibility = View.GONE

            if (searchBox.text.toString().isEmpty()) {
                Toast.makeText(this, "Please enter a valid number!", Toast.LENGTH_LONG)
                    .show()
                return@setOnClickListener
            }
            val limit = searchBox.text.toString().toInt()
            getPokemonList(limit)
            searchBox.text.clear()
        }
        getPokemonList(DEFAULT_POKEMON_SIZE)
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
                    loading.visibility = View.VISIBLE
                    statusImage.visibility = View.GONE
                    status.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    getPokemonList(DEFAULT_POKEMON_SIZE)
                } else if (state == NetworkInfo.State.DISCONNECTED) {
                    loading.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                    statusImage.visibility = View.VISIBLE
                    status.visibility = View.VISIBLE
                }
            }
    }

    // Setup the menu options
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.filter_item_menu, menu)
        return true
    }

    // Define the actions of the menu items when they are clicked on
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.enterNumber -> {
                searchBox.visibility = View.VISIBLE
                doneIcon.visibility = View.VISIBLE
            }
            R.id.all -> getPokemonList(DEFAULT_POKEMON_SIZE)
        }
        return true
    }

    // Unsubscribe the subscription since its no longer needed at this point
    override fun onDestroy() {
        if (subscription != null && !subscription!!.isUnsubscribed) {
            subscription!!.unsubscribe()
        }
        super.onDestroy()
    }

    // Method to get the pokemon list from the server using rxjava
    private fun getPokemonList(limit: Int) {
        subscription = PokemonApiService.PokemonApiClient.retrofitService
            .getPokemon(0, limit).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<Pokemon> {
                override fun onCompleted() {
                    Log.d(TAG, "In OnCompleted()")
                }

                override fun onError(e: Throwable?) {
                    Log.d("ERROR:", "${e?.message}")
                    statusImage.visibility = View.VISIBLE
                    status.text = e?.message
                }

                override fun onNext(t: Pokemon?) {
                    loading.visibility = View.GONE
                    adapter.loadPokemonCharacters(t!!.results)
                }
            })
    }

}