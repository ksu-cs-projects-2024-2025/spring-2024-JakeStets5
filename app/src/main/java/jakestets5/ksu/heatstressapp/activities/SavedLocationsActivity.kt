package jakestets5.ksu.heatstressapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jakestets5.ksu.heatstressapp.R
import jakestets5.ksu.heatstressapp.adapters.recycler.SavedLocationsAdapter
import jakestets5.ksu.heatstressapp.data.`object`.LocationData
import jakestets5.ksu.heatstressapp.helpers.database.CitiesDatabaseHelper
import jakestets5.ksu.heatstressapp.helpers.database.SavedLocationsDatabaseHelper
import jakestets5.ksu.heatstressapp.helpers.ui.SavedLocationsUIHelper

/**
 * Activity for managing and displaying saved locations. Allows users to view, select, and remove saved locations.
 */
class SavedLocationsActivity: AppCompatActivity(), SavedLocationsAdapter.OnRemoveClickListener {

    // Variables for storing user's location and settings
    var userCity: String = "New York"
    var userCountry: String = "United States"
    var userCountryCode: String = "US"
    var userLat = 40.7128
    var userLong = -74.0060
    var userState: String = "New York"
    var isDay = true

    // Database helpers for managing cities and saved locations
    private lateinit var cdbHelper: CitiesDatabaseHelper
    private lateinit var sldbHelper: SavedLocationsDatabaseHelper

    /**
     * Initializes the activity, sets up UI components, and prepares data handlers.
     *
     * @param savedInstanceState Contains data supplied in onSaveInstanceState(Bundle) or null if no data was supplied.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_locations)

        // Extract data passed from another activity
        val inBundle = intent.extras
        if (inBundle != null){
            userCity = "${inBundle.getString("city")}"
            userCountry = "${inBundle.getString("country")}"
            userLong = "${inBundle.getString("longitude")}".toDouble()
            userLat = "${inBundle.getString("latitude")}".toDouble()
            userCountryCode = "${inBundle.getString("countryCode")}"
            userState = "${inBundle.getString("state")}"
            isDay = inBundle.getBoolean("dayStatus")
        }

        // Setup UI helper to adjust appearance based on day/night
        val savedLocationsUIHelper = SavedLocationsUIHelper(this)
        savedLocationsUIHelper.nightAndDay(isDay)

        // Initialize database helpers and configure RecyclerView for displaying saved locations
        sldbHelper = SavedLocationsDatabaseHelper(this)
        val recyclerView: RecyclerView = findViewById(R.id.saved_locations_recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val savedLocationsAdapter = SavedLocationsAdapter(sldbHelper.getAllLocations())
        recyclerView.adapter = savedLocationsAdapter

        // Configure back navigation button
        val backBtn = findViewById<ImageButton>(R.id.saved_locations_back_btn)
        backBtn.setOnClickListener {
            // Navigate back to main activity
            navigateBack()
        }

        // Sets the listener for an item click
        savedLocationsAdapter.setOnItemClickListener(object : SavedLocationsAdapter.OnItemClickListener{
            override fun onItemClick(locationData: LocationData) {
                // Sets the city variables based on the city clicked
                cdbHelper = CitiesDatabaseHelper(this@SavedLocationsActivity, userState)
                val query = cdbHelper.getExactCity(locationData.latitude, locationData.longitude, this@SavedLocationsActivity)
                userCity = query.city
                userCountry = query.country
                userLat = query.latitude
                userLong = query.longitude
                userCountryCode = query.countryCode

                // Navigate back to main activity
                navigateBack()
            }
        })

        // Sets the listener for the remove click
        savedLocationsAdapter.setOnRemoveClickListener(object : SavedLocationsAdapter.OnRemoveClickListener{
            override fun onRemoveClick(position: Int) {
                val locationList = sldbHelper.getAllLocations()
                val itemToDelete = locationList[position]
                // Delete the item from database
                val isSuccess = sldbHelper.deleteLocation(itemToDelete.city)
                if (isSuccess) {
                    savedLocationsAdapter.removeItem(position)
                }
                else{
                    Toast.makeText(this@SavedLocationsActivity, "Failed to add ity to saved locations", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    /**
     * Navigates back to the MainActivity, passing back the updated location data.
     */
    private fun navigateBack() {
        val outBundle = Bundle().apply {
            putString("latitude", userLat.toString())
            putString("longitude", userLong.toString())
            putString("city", userCity)
            putString("countryCode", userCountryCode)
            putString("state", userState)
            putBoolean("dayStatus", isDay)
        }
        startActivity(Intent(this, MainActivity::class.java).putExtras(outBundle))
    }

    /**
     * Implemented from SavedLocationsAdapter.OnRemoveClickListener. Does nothing, actual logic handled elsewhere.
     */
    override fun onRemoveClick(position: Int) {
        // This method is a placeholder to satisfy interface requirements.
    }
}