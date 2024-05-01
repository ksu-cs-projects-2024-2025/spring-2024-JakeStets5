package jakestets5.ksu.heatstressapp.helpers.database

import android.content.ContentValues
import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import jakestets5.ksu.heatstressapp.data.`object`.SettingsData
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Helper class for managing the settings database in an Android application. This class handles creating,
 * opening, and upgrading the database.
 *
 * @param context The context of the caller, used to access application-specific resources.
 */
class SettingsDatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "HeatStressDatabaseSmall.db" // Database name
        private const val DATABASE_VERSION = 1 // Database version
        private val TAG = "SettingsDatabaseHelper" // Tag just for the LogCat window
    }

    private val dbFilePath: String = context.getDatabasePath(DATABASE_NAME).path
    private var mDataBase: SQLiteDatabase? = null

    /**
     * Initializes the database helper, attempting to create the database if it doesn't already exist.
     */
    init {
        createDataBase()
    }

    override fun onCreate(db: SQLiteDatabase) {
        // This method would be called to create tables in the database if not copying from assets.
    }

    /**
     * Adds or updates settings in the database. This involves inserting a new row of settings values.
     *
     * @param bA Breed adjustment factor.
     * @param cA Color adjustment factor.
     * @param aA Acclimation adjustment factor.
     * @param hA Health adjustment factor.
     * @param sA Shade adjustment factor.
     * @param fA Feed adjustment factor.
     * @param mA Manure adjustment factor.
     * @param wA Water adjustment factor.
     * @return True if the operation was successful, false otherwise.
     */
    fun addSettings(bA: Int, cA: Int, aA: Int, hA: Int, sA: Int, fA: Int, mA: Int, wA: Int): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put("breed", bA)
            put("color", cA)
            put("acclimation", aA)
            put("health", hA)
            put("shade", sA)
            put("feed", fA)
            put("manure", mA)
            put("water", wA)
        }
        val result = db.insert("settings", null, contentValues)
        db.close()
        return result != -1L // Return 'true' if insert is successful, 'false' otherwise
    }

    /**
     * Clears all settings from the database.
     *
     * @return True if any rows were deleted, false otherwise.
     */
    fun clearSettings(): Boolean {
        val db = this.writableDatabase
        val deletedRows = db.delete("settings", null, null)
        db.close()
        return deletedRows > 0
    }

    /**
     * Retrieves the current settings from the database.
     *
     * @return A SettingsData object containing all current settings.
     */
    fun getSettings(): SettingsData {
        var settings = SettingsData(0, 0, 0, 0, 0, 0, 0, 0)
        val db = this.readableDatabase
        val selectQuery = "SELECT * FROM settings"
        val cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                val breedIndex = cursor.getColumnIndex("breed")
                val colorIndex = cursor.getColumnIndex("color")
                val acclimationIndex = cursor.getColumnIndex("acclimation")
                val healthIndex = cursor.getColumnIndex("health")
                val shadeIndex = cursor.getColumnIndex("shade")
                val feedIndex = cursor.getColumnIndex("feed")
                val manureIndex = cursor.getColumnIndex("manure")
                val waterIndex = cursor.getColumnIndex("water")
                if (breedIndex >= 0) {
                    val breed = cursor.getInt(breedIndex)
                    val color = cursor.getInt(colorIndex)
                    val acclimation = cursor.getInt(acclimationIndex)
                    val health = cursor.getInt(healthIndex)
                    val shade = cursor.getInt(shadeIndex)
                    val feed = cursor.getInt(feedIndex)
                    val manure = cursor.getInt(manureIndex)
                    val water = cursor.getInt(waterIndex)
                    settings = SettingsData(breed, color, acclimation, health, shade, feed, manure, water)
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return settings
    }

    /**
     * Extracts settings data from a cursor object.
     *
     * @param cursor The cursor pointing to the query result.
     * @return A SettingsData object populated from the cursor.
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
     * Copies the database from the application's assets directory to the data directory.
     *
     * @throws IOException If there is an error reading from the assets or writing to the file system.
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
     * Checks if the database file already exists in the application's file system.
     *
     * @return True if the file exists, false otherwise.
     */
    private fun checkDataBase(): Boolean {
        return File(dbFilePath).exists()
    }

    /**
     * Opens the database for reading or writing.
     *
     * @return True if the database was successfully opened, false otherwise.
     * @throws SQLException If there is an error opening the database.
     */
    @Throws(SQLException::class)
    fun openDataBase(): Boolean {
        mDataBase = SQLiteDatabase.openDatabase(dbFilePath, null, SQLiteDatabase.CREATE_IF_NECESSARY)
        return mDataBase != null
    }

    /**
     * Closes the database and releases any open resources.
     */
    @Synchronized
    override fun close() {
        mDataBase?.close()
        super.close()
    }

    /**
     * Handles upgrading the database when the application version changes.
     *
     * @param db The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Upgrade database if structure is changed
        db.execSQL("DROP TABLE IF EXISTS saved_locations")
        onCreate(db)
    }
}