package jakestets5.ksu.heatstressapp.helpers.database

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast
import jakestets5.ksu.heatstressapp.data.`object`.LocationData
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Helper class to manage access to an SQLite database for cities. This class handles creating, opening,
 * and querying the cities database.
 *
 * @param context The context used to access application assets and resources.
 * @param state The default state used for filtering or categorizing city data when necessary.
 */
class CitiesDatabaseHelper(private val context: Context, val state: String) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "HeatStressDatabaseSmall.db" // Database name
        private const val DATABASE_VERSION = 1 // Database version
        private val TAG = "LocationDatabaseHelper" // Tag just for the LogCat window
    }

    private val dbFilePath: String = context.getDatabasePath(DATABASE_NAME).path
    private var mDataBase: SQLiteDatabase? = null

    /**
     * Initializes the database. Attempts to create the database by copying it from the assets folder.
     */
    init {
        createDataBase()
    }

    /**
     * Returns location data for a specific latitude and longitude.
     *
     * @param latitude The latitude of the location.
     * @param longitude The longitude of the location.
     * @param context The context for database operations and toasting messages.
     * @return The location data object containing detailed information about the city.
     */
    fun getExactCity(latitude: Double, longitude: Double, context: Context): LocationData {
        var cityInfo = LocationData("Manhattan", "United States", "Kansas", 39.195, -96.599, "US")
        try{
            val roundedLat = String.format("%.3f", latitude).toDouble()
            val roundedLong = String.format("%.3f", longitude).toDouble()
            val db = this.readableDatabase
            val selectQuery = "SELECT * FROM cities WHERE lat BETWEEN $roundedLat - 0.001 AND $roundedLat + 0.001 AND lng BETWEEN $roundedLong - 0.001 AND $roundedLong + 0.001"
            val cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    val cityIndex = cursor.getColumnIndex("city")
                    val countryIndex = cursor.getColumnIndex("country")
                    val latIndex = cursor.getColumnIndex("lat")
                    val lonIndex = cursor.getColumnIndex("lng")
                    val countryCodeIndex = cursor.getColumnIndex("iso2")
                    if (cityIndex >= 0 && countryIndex >= 0) {
                        val cityName = cursor.getString(cityIndex)
                        val countryName = cursor.getString(countryIndex)
                        val lat = cursor.getString(latIndex).toDouble()
                        val long = cursor.getString(lonIndex).toDouble()
                        val countryCode = cursor.getString(countryCodeIndex)
                        cityInfo = LocationData(cityName, countryName, state, lat, long, countryCode)
                    }
                } while (cursor.moveToNext())
            }
            cursor.close()
            db.close()
        }catch (e: Exception){
            Toast.makeText(context, "City not in database", Toast.LENGTH_SHORT).show()
        }
        return cityInfo
    }

    /**
     * Searches cities that match the provided query string.
     *
     * @param cityQuery The query string for the city search.
     * @param context The context for database operations and toasting messages.
     * @return A list of LocationData matching the query.
     */
    fun searchCities(cityQuery: String, context: Context): List<LocationData>{
        val cityList = mutableListOf<LocationData>()
        try{
            val db = this.readableDatabase
            val selectQuery = "SELECT * FROM cities WHERE city LIKE '$cityQuery%'"
            val cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    val cityIndex = cursor.getColumnIndex("city")
                    val countryIndex = cursor.getColumnIndex("country")
                    val latIndex = cursor.getColumnIndex("lat")
                    val lonIndex = cursor.getColumnIndex("lng")
                    val countryCodeIndex = cursor.getColumnIndex("iso2")
                    if (cityIndex >= 0 && countryIndex >= 0) {
                        val cityName = cursor.getString(cityIndex)
                        val countryName = cursor.getString(countryIndex)
                        val lat = cursor.getString(latIndex).toDouble()
                        val long = cursor.getString(lonIndex).toDouble()
                        val countryCode = cursor.getString(countryCodeIndex)
                        cityList.add(LocationData(cityName, countryName, state, lat, long, countryCode))
                    }
                } while (cursor.moveToNext())
            }
            else{
                Toast.makeText(context, "City not in database", Toast.LENGTH_SHORT).show()
            }
            cursor.close()
            db.close()
        }
        catch (e: Exception){
            Toast.makeText(context, "City not in database", Toast.LENGTH_SHORT).show()
        }
        return cityList
    }

    /**
     * Creates the database if it does not exist by copying it from the application's assets.
     */
    @Throws(IOException::class)
    fun createDataBase() {
        if (!checkDataBase()) {
            this.readableDatabase.close()
            try {
                copyDataBase()
                Log.e(TAG, "createDatabase database created")
            } catch (e: IOException) {
                Log.e(TAG, "Error copying database", e)
                throw Error("ErrorCopyingDataBase")
            }
        }
    }

    /**
     * Checks if the database already exists.
     *
     * @return true if it exists, false if it does not.
     */
    private fun checkDataBase(): Boolean {
        return File(dbFilePath).exists()
    }

    /**
     * Copies the database from the assets folder to the application's data directory.
     */
    @Throws(IOException::class)
    private fun copyDataBase() {
        context.assets.open(DATABASE_NAME).use { inputStream ->
            FileOutputStream(dbFilePath).use { outputStream ->
                val buffer = ByteArray(1024)
                var length: Int
                while (inputStream.read(buffer).also { length = it } > 0) {
                    outputStream.write(buffer, 0, length)
                }
                outputStream.flush()
            }
        }
    }

    /**
     * Opens the database.
     *
     * @return true if the database was successfully opened, false otherwise.
     */
    @Throws(SQLException::class)
    fun openDataBase(): Boolean {
        mDataBase = SQLiteDatabase.openDatabase(dbFilePath, null, SQLiteDatabase.CREATE_IF_NECESSARY)
        return mDataBase != null
    }

    /**
     * Closes the database and releases any occupied resources.
     */
    @Synchronized
    override fun close() {
        mDataBase?.close()
        super.close()
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Database is created by copying it from assets, not created from scratch
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Handle database version upgrades here
    }
}

