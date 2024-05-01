package jakestets5.ksu.heatstressapp.adapters.recycler

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import jakestets5.ksu.heatstressapp.activities.ForecastActivity
import jakestets5.ksu.heatstressapp.R

/**
 * Adapter for populating the forecast data in a RecyclerView.
 *
 * @property forecastActivity The parent activity of the adapter.
 * @property isDay Indicates whether it's daytime or nighttime.
 * @property dayOfWeek List of day names.
 * @property precipitation List of precipitation data.
 * @property threatLevel List of threat level data.
 * @property humidity List of humidity data.
 * @property windSpeed List of wind speed data.
 * @property highTemp List of high temperature data.
 * @property lowTemp List of low temperature data.
 */
class ForecastRecyclerAdapter(private val forecastActivity: ForecastActivity, private var isDay: Boolean, private var dayOfWeek: MutableList<String>, private var precipitation: MutableList<String>,
                              private var threatLevel: MutableList<String>, private var humidity: MutableList<String>, private var windSpeed: MutableList<String>, private var highTemp: MutableList<String>, private var lowTemp: MutableList<String>)
    : RecyclerView.Adapter<ForecastRecyclerAdapter.ForecastViewHolder>() {

    /**
     * ViewHolder for holding the views of each item in the RecyclerView.
     *
     * @param itemView The inflated view for an item in the RecyclerView.
     */
    inner class ForecastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Views for displaying precipitation data
        val precipitation: TextView = itemView.findViewById(R.id.precipitation_textView_rv)
        val precipitationTitle: TextView = itemView.findViewById(R.id.precipitation_title_textView_rv)
        val precipitationImage: ImageView = itemView.findViewById(R.id.precipitation_imageView_rv)

        // Views for displaying threat level data
        val forecastThreatLevel: TextView = itemView.findViewById(R.id.threat_level_textView_rv)
        val threatTitle: TextView = itemView.findViewById(R.id.threat_title_textView_rv)
        val threatImage: ImageView = itemView.findViewById(R.id.threat_image_rv)

        // Views for displaying humidity data
        val forecastHumidity: TextView = itemView.findViewById(R.id.humidity_textView_rv)
        val humidityTitle: TextView = itemView.findViewById(R.id.humidity_title_textView_rv)
        val humidityImage: ImageView = itemView.findViewById(R.id.humidity_image_rv)

        // Views for displaying wind speed data
        val windSpeed: TextView = itemView.findViewById(R.id.wind_speed_textView_rv)
        val windTitle: TextView = itemView.findViewById(R.id.wind_title_textView_rv)
        val windImage: ImageView = itemView.findViewById(R.id.wind_image_rv)

        // Views for displaying high temperature data
        val highTemp: TextView = itemView.findViewById(R.id.high_temp_textView_rv)
        val highTempTitle: TextView = itemView.findViewById(R.id.high_temp_title_textView_rv)
        val highTempImage: ImageView = itemView.findViewById(R.id.high_temp_image_rv)

        // Views for displaying low temperature data
        val lowTemp: TextView = itemView.findViewById(R.id.low_temp_textView_rv)
        val lowTempTitle: TextView = itemView.findViewById(R.id.low_temp_title_textView_rv)
        val lowTempImage: ImageView = itemView.findViewById(R.id.low_temp_image_rv)

        // View for displaying day of the week
        val dayOfWeek: TextView = itemView.findViewById(R.id.day_of_week_textView_rv)

        // Layouts for setting background borders
        val precipitationLayout: LinearLayout = itemView.findViewById(R.id.forecast_precipitation_layout_rv)
        val lowLayout: LinearLayout = itemView.findViewById(R.id.forecast_low_layout_rv)
        val highLayout: LinearLayout = itemView.findViewById(R.id.forecast_high_layout_rv)
        val windLayout: LinearLayout = itemView.findViewById(R.id.forecast_wind_layout_rv)
        val threatLayout: LinearLayout = itemView.findViewById(R.id.forecast_threat_layout_rv)
        val humidityLayout: LinearLayout = itemView.findViewById(R.id.forecast_humidity_layout_rv)
    }

    /**
     * Inflates the layout for the RecyclerView item.
     *
     * @param parent The parent ViewGroup.
     * @param viewType The type of view.
     * @return The ForecastViewHolder instance.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.forecast_recycler_view_row, parent, false)
        return ForecastViewHolder(view)
    }

    /**
     * Binds data to the views in the RecyclerView item.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the item in the RecyclerView.
     */
    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        // Determine text and border color based on whether it's day or night
        val textColor = if (isDay) Color.parseColor("#000000") else Color.parseColor("#FFFFFF")
        val blackBorder = ContextCompat.getDrawable(forecastActivity, R.drawable.info_border_black)
        val whiteBorder = ContextCompat.getDrawable(forecastActivity, R.drawable.info_border_white)
        val borderColor = if(isDay) blackBorder else whiteBorder

        // Set background color for day of the week TextView
        if(isDay){
            holder.dayOfWeek.background = ContextCompat.getDrawable(forecastActivity,
                R.drawable.info_border_black
            )
        }
        else{
            holder.dayOfWeek.background = ContextCompat.getDrawable(forecastActivity,
                R.drawable.info_border_white
            )
        }

        // Set text color for all TextViews
        holder.precipitation.setTextColor(textColor)
        holder.precipitationTitle.setTextColor(textColor)
        holder.precipitationImage.setColorFilter(textColor)
        holder.forecastThreatLevel.setTextColor(textColor)
        holder.threatTitle.setTextColor(textColor)
        holder.threatImage.setColorFilter(textColor)
        holder.forecastHumidity.setTextColor(textColor)
        holder.humidityTitle.setTextColor(textColor)
        holder.humidityImage.setColorFilter(textColor)
        holder.windSpeed.setTextColor(textColor)
        holder.windTitle.setTextColor(textColor)
        holder.windImage.setColorFilter(textColor)
        holder.lowTemp.setTextColor(textColor)
        holder.lowTempTitle.setTextColor(textColor)
        holder.lowTempImage.setColorFilter(textColor)
        holder.highTemp.setTextColor(textColor)
        holder.highTempTitle.setTextColor(textColor)
        holder.highTempImage.setColorFilter(textColor)
        holder.dayOfWeek.setTextColor(textColor)

        // Set background borders for various layouts
        holder.precipitationLayout.background = borderColor
        holder.lowLayout.background = borderColor
        holder.highLayout.background = borderColor
        holder.humidityLayout.background = borderColor
        holder.threatLayout.background = borderColor
        holder.windLayout.background = borderColor

        // Bind data to views
        holder.precipitation.text = precipitation[position]
        holder.forecastThreatLevel.text = threatLevel[position]
        holder.forecastHumidity.text = humidity[position]
        holder.windSpeed.text = windSpeed[position]
        holder.lowTemp.text = lowTemp[position]
        holder.highTemp.text = highTemp[position]
        holder.dayOfWeek.text = dayOfWeek[position] + ":"
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items.
     */
    override fun getItemCount(): Int{
        return precipitation.size
    }
}