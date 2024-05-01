package jakestets5.ksu.heatstressapp.helpers.formula

import android.util.Log
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * A helper class that calculates solar radiation and the comprehensive climate index (CCI)
 * based on weather details gathered from an API.
 */
class RadiationFormulaHelper() {

    /**
     * Converts a Unix timestamp to a Julian date.
     *
     * @param date The date in Unix timestamp format.
     * @return The Julian date as an integer.
     */
    private fun getJulianDate(date: Long): Int {
        // Unix epoch (January 1, 1970) corresponds to Julian Day Number 2440587.5
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date * 1000  // Convert seconds to milliseconds
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        return (1461 * (year + 4800 + (month - 14)/12))/4 +(367 * (month - 2 - 12 * ((month - 14)/12)))/12 - (3 * ((year + 4900 + (month - 14)/12)/100))/4 + day - 32075
    }

    /**
     * Calculates the declination coefficient used in solar radiation calculations.
     *
     * @param julianDay The Julian day of the year.
     * @param lat The latitude of the location in degrees.
     * @return The declination coefficient as a Double.
     */
    private fun calculateCoefficient(julianDay: Int, lat: Double): Double {
        return if(lat > 0){
            asin(0.39795 * cos(0.2163108 + 2 * atan(0.9671396 * tan(0.0086 * (julianDay - 186)))))
        } else{
            asin(0.39795 * cos(0.2163108 + 2 * atan(0.9671396 * tan(0.0086 * (julianDay)))))
        }
    }

    /**
     * Calculates the day length from sunrise to sunset based on latitude and the declination coefficient.
     *
     * @param lat The latitude of the location in degrees.
     * @param coefficient The declination coefficient.
     * @return Day length in hours as a Double.
     */
    private fun calculateDayLength(lat: Double, coefficient: Double): Double {
        val radiansLat = lat * PI / 180
        //val radiansCoefficient = coefficient * PI / 180

        return 24 - (24 / PI) * acos(
            (sin(0.8333 * PI / 180) + sin(radiansLat) * sin(coefficient)) /
                    (cos(radiansLat) * cos(coefficient))
        )
    }

    /**
     * Calculates solar radiation in kcal/m²/day based on the day length and latitude.
     *
     * @param dayLength The length of the day in hours.
     * @param lat The latitude of the location in degrees.
     * @return Solar radiation as a Double.
     */
    private fun SRkcal(dayLength: Double, lat: Double): Double {
        val baseValue = 31 * dayLength - 140
        val adjustment = 0.01 * baseValue * (23 - lat)
        return baseValue + adjustment
    }

    /**
     * Retrieves solar radiation based on latitude and date.
     *
     * @param lat Latitude of the location in degrees.
     * @param date Date in Unix timestamp format.
     * @return Solar radiation value as a Double.
     */
    fun getSolarRadiation(lat: Double, date: Long): Double{
        val coefficient = calculateCoefficient(getJulianDate(date), lat)
        val dayLength = calculateDayLength(lat, coefficient)
        return SRkcal(dayLength, lat)
    }

    /**
     * Calculates the comprehensive climate index (CCI) which integrates temperature, humidity,
     * wind speed, and solar radiation into a single index.
     *
     * @param temp Temperature in degrees Celsius.
     * @param humidity Relative humidity percentage.
     * @param wind Wind speed in m/s.
     * @param radiation Solar radiation in kcal/m²/day.
     * @return The calculated CCI value as a Double.
     */
    fun calculateCCI(temp: Double, humidity: Double, wind: Double, radiation: Double): Double{
        val humidityFactor = -0.09 * humidity + 0.000035 * (humidity - 30) * temp.pow(2) + 2.8
        val windFactor = if (wind < 1) {
            5.0
        } else {
            21 - 14.7 * sqrt(wind + 0.5) + 1.7 * wind - 0.009 * wind.pow(2)
        }
        val tempFactor = 0.06 * temp + 0.0175 * radiation + 0.00053 * temp.pow(2) - 6.4 + temp
        return humidityFactor + windFactor + tempFactor
    }
}