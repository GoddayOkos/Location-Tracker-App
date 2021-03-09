package dev.decagon.godday.locationtracker.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dev.decagon.godday.locationtracker.R
import dev.decagon.godday.locationtracker.adapter.InfoWindowAdapter
import dev.decagon.godday.locationtracker.model.LocationDetails
import dev.decagon.godday.locationtracker.ui.pokemon.PokemonGalleryActivity
import java.text.SimpleDateFormat
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
        NavigationView.OnNavigationItemSelectedListener {

    private val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val TAG = MapsActivity::class.java.simpleName
    private lateinit var toolbar: Toolbar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    // Firebase initialisation
    private val database = FirebaseDatabase.getInstance()


    private var locationRequest: LocationRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Setup the toolbar and navigation drawer as well as
        // the toggle button to close and open the drawer.
        toolbar = findViewById(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        setSupportActionBar(toolbar)
        setupLocationClient()
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.open_drawer,
                R.string.close_drawer
        )
        navigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        toggle.syncState()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        // Set a custom InfoWindowAdapter to the map. The custom adapter
        // defines the appearance of the content of the marker's info window
        map.setInfoWindowAdapter(InfoWindowAdapter(this))
        getCurrentLocation()
        trackPartner()
    }


    // Setup menu options
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater: MenuInflater = menuInflater
        menuInflater.inflate(R.menu.map_options, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // Define the actions of the menu items/options to display
    // the different types of maps based on user's preferences

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    // Method that checks if permission to access fine location has been granted
    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // Method use to request for permission to access fine location
    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION)
    }


    // Checks the action of the user regarding the permission request
    // If the permission was granted, get the user's current location
    // else log message to the logcat

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                getCurrentLocation()
            } else {
                Log.e(TAG, "Permission denied!")
            }
        }
    }

    // Initialise the fusedLocationClient to get location update
    // from the user
    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    /**
     * This method gets the user's exact location every 10secs and
     * update the firebase database with the current user's location
     * whenever it is generated. If no such user's location is found,
     * the method logs an error message to the logcat.
     */

    @SuppressLint("MissingPermission", "SimpleDateFormat")
    private fun getCurrentLocation() {
        if (!isPermissionGranted()) {
            requestLocationPermissions()
        } else {
            // To get realtime update on user's movement
            if (locationRequest == null) {
                locationRequest = LocationRequest.create()
                locationRequest?.let { locationRequest ->
                    locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                    locationRequest.interval = 10000
                    locationRequest.fastestInterval = 5000
                    val locationCallback = object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult?) {
                            getCurrentLocation()
                        }
                    }
                    fusedLocationClient.requestLocationUpdates(locationRequest,
                            locationCallback, null)
                }
            }
            map.isMyLocationEnabled = true
            // Get the user's last location as a task and send the latitude and
            // longitude to firebase database else log error message to logcat
            fusedLocationClient.lastLocation.addOnCompleteListener {
                val myLocation = it.result
                Log.d(TAG, "MY LOCATION IS: $myLocation")
                if (myLocation != null) {
                    val dateFormat = SimpleDateFormat("dd/MMM/yyyy HH:MM:ss")
                    val myLocationDetails = LocationDetails(myLocation.latitude, myLocation.longitude,
                            dateFormat.format(Date()).toString())

                    // Adding values to firebase
                    database.getReference("Location").child("Godday").child("location").setValue(myLocationDetails)
                } else {
                    Log.d(TAG, "No location found!")
                }
            }
        }
    }

    /**
     * This method is used to track my partner. It receives my partner location details
     * from firebase database and update the map by creating a custom marker with the latitude
     * and longitude of my partner obtained from the firebase database. This method also uses
     * geocoding to display the address of my partner's current location from the latitude and
     * longitude obtained.
     */
    private fun trackPartner() {
        database.getReference("Tolulope").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    return
                }
                val partnerLocation = snapshot.getValue(LocationDetails::class.java)
                val partnerLatLng = partnerLocation?.latitude?.let { partnerLocation.longitude?.let { it1 -> LatLng(it, it1) } }
                val place = Geocoder(applicationContext)
                val address = place.getFromLocation(partnerLatLng!!.latitude, partnerLatLng.longitude, 1)
                map.clear()
                map.addMarker(MarkerOptions().position(partnerLatLng!!)
                        .title("Tolulope")
                        .snippet("Here is Tolulope's current location details:\nLatitude: ${partnerLatLng.latitude}" +
                                "\nLongitude: ${partnerLatLng.longitude}\nAddress: ${address[0].getAddressLine(0)}")
                )//.showInfoWindow()
                val update = CameraUpdateFactory.newLatLngZoom(partnerLatLng, 16.0f)
                map.moveCamera(update)
            }

            // Just needed to be overridden
            override fun onCancelled(error: DatabaseError) {}

        })
    }

    // Method to navigate to the PokemonGalleryActivity using intent when the user
    // clicks on Pokemon gallery from the navigation drawer
    private fun navigateToPokemonGallery() {
        val intent = Intent(this, PokemonGalleryActivity::class.java)
        startActivity(intent)
    }

    // define the actions to be taken when items in the navigation
    // drawer are clicked
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.nav_gallery) {
            navigateToPokemonGallery()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    // Change the behaviour of the back key to close the navigation
    // drawer without finishing the activity when the navigation drawer
    // is opened.
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}