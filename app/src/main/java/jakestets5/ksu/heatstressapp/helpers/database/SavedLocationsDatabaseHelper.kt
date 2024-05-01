package jakestets5.ksu.heatstressapp.helpers.database

import android.content.ContentValues
import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast
import jakestets5.ksu.heatstressapp.activities.MainActivity
import jakestets5.ksu.heatstressapp.data.`object`.LocationData
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Helper class to manage the SQLite database for saved locations. This class handles creating,
 * accessing, and modifying the saved locations database.
 *
 * @param context The context used to access application assets and resources.
 */
class SavedLocationsDatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "HeatStressDatabaseSmall.db" // Database name
        private const val DATABASE_VERSION = 1 // Database version
        private val TAG = "SavedLocationsDatabaseHelper" // Tag just for the LogCat window
    }

    private val dbFilePath: String = context.getDatabasePath(DATABASE_NAME).path
    private var mDataBase: SQLiteDatabase? = null

    /**
     * Initializes the helper, potentially creating a new database if it does not exist.
     */
    init {
        createDataBase()
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Database is created by copying it from assets, not created from scratch
    }

    /**
     * Attempts to add a location to the database.
     *
     * @param mainActivity The activity context used for displaying Toast messages.
     * @param city The city name to save.
     * @param country The country name.
     * @param state The state or province.
     * @param latitude The latitude as a string.
     * @param longitude The longitude as a string.
     * @param countryCode The ISO country code.
     * @return True if the location was successfully added, false if the location already exists.
     */
    fun addLocation(mainActivity: MainActivity, city: String, country: String, state: String, latitude: String, longitude: String, countryCode: String): Boolean {
        val db = this.writableDatabase

        // Check if the city already exists in the database
        val selectQuery = """
        SELECT * FROM saved_locations
        WHERE lat = ? AND lng = ? AND city = ? AND country = ?
    """
        val cursor = db.rawQuery(selectQuery, arrayOf(latitude, longitude, city, country))
        val exists = cursor.moveToFirst()
        cursor.close()

        if (exists) {
            db.close()
            Toast.makeText(mainActivity, "City already in database", Toast.LENGTH_SHORT).show()
            return false  // Return 'false' if city already exists
        }

        val contentValues = ContentValues().apply {
            put("city", city)
            put("Country", country)
            put("state", state)
            put("lat", latitude)
            put("lng", longitude)
            put("iso2", countryCode)
        }
        val result = db.insert("saved_locations", null, contentValues)
        db.close()
        return result != -1L // Return 'true' if insert is successful, 'false' otherwise
    }

    /**
     * Deletes a location from the database based on the city name.
     *
     * @param city The city name to delete.
     * @return True if one or more rows were deleted, false otherwise.
     */
    fun deleteLocation(city: String): Boolean {
        val db = this.writableDatabase
        val selection = "city = ?"
        val selectionArgs = arrayOf(city)
        val deletedRows = db.delete("saved_locations", selection, selectionArgs)
        db.close()
        return deletedRows > 0
    }

    /**
     * Retrieves a city name based on latitude and longitude.
     *
     * @param lat The latitude.
     * @param long The longitude.
     * @return The city name if found, otherwise a default value.
     */
    fun getCity(lat: Double, long: Double): String {
        var city = "New York"
        val db = this.readableDatabase
        val selectQuery =
            "SELECT * FROM saved_locations WHERE lat LIKE $lat AND lng LIKE $long"
        val cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                val cityIndex = cursor.getColumnIndex("city")
                if (cityIndex >= 0) {
                    city = cursor.getString(cityIndex)
                }
            } while (cursor.moveToNext())
        }
        return city
    }

    /**
     * Retrieves all saved locations from the database.
     *
     * @return A list of LocationData representing all saved locations.
     */
    fun getAllLocations(): MutableList<LocationData> {
        val savedLocations = mutableListOf<LocationData>()
        val db = this.readableDatabase
        val selectQuery = "SELECT * FROM saved_locations"
        val cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                val cityIndex = cursor.getColumnIndex("city")
                val countryIndex = cursor.getColumnIndex("Country")
                val latIndex = cursor.getColumnIndex("lat")
                val lonIndex = cursor.getColumnIndex("lng")
                val countryCodeIndex = cursor.getColumnIndex("iso2")
                val stateIndex = cursor.getColumnIndex("state")
                if (cityIndex >= 0 && countryIndex >= 0) {
                    val cityName = cursor.getString(cityIndex)
                    val countryName = cursor.getString(countryIndex)
                    val lat = cursor.getString(latIndex).toDouble()
                    val long = cursor.getString(lonIndex).toDouble()
                    val countryCode = cursor.getString(countryCodeIndex)
                    val state = cursor.getString(stateIndex)
                    savedLocations.add(LocationData(cityName, countryName, state, lat, long, countryCode))
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return savedLocations
    }

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
     * Copies the preloaded database from assets to the application's data directory, used when the database is not found.
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
     * Checks if the database already exists to avoid re-copying the file each time the application is launched.
     *
     * @return true if it exists, false otherwise.
     */
    private fun checkDataBase(): Boolean {
        return File(dbFilePath).exists()
    }

    /**
     * Opens the database.
     */
    @Throws(SQLException::class)
    fun openDataBase(): Boolean {
        mDataBase = SQLiteDatabase.openDatabase(dbFilePath, null, SQLiteDatabase.CREATE_IF_NECESSARY)
        return mDataBase != null
    }

    /**
     * Closes the database to free up resources and ensure data safety when not being accessed.
     */
    @Synchronized
    override fun close() {
        mDataBase?.close()
        super.close()
    }

    /**
     * Handles upgrading the database when the version number increases. This may involve altering tables or adding new ones.
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Upgrade database if structure is changed
        db.execSQL("DROP TABLE IF EXISTS saved_locations")
        onCreate(db)
    }
}