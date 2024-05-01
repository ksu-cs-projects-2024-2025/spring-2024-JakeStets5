package jakestets5.ksu.heatstressapp.adapters.recycler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import jakestets5.ksu.heatstressapp.data.`object`.LocationData
import jakestets5.ksu.heatstressapp.activities.MainActivity
import jakestets5.ksu.heatstressapp.R

/**
 * Adapter for managing city locations within a RecyclerView. Supports day/night themes and provides interaction capabilities.
 *
 * @param mainActivity The context of the MainActivity where this adapter is being used.
 * @param isDay Boolean indicating if the theme should be set for daytime (true) or nighttime (false).
 * @param locationList List of LocationData objects representing the locations to be displayed.
 */
class CitiesRecyclerAdapter(private var mainActivity: MainActivity, private var isDay: Boolean, private var locationList: List<LocationData>) : RecyclerView.Adapter<CitiesRecyclerAdapter.LocationViewHolder>() {

    /**
     * Provides a reference to the type of views that you are using (custom ViewHolder).
     */
    inner class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val cityTextView: TextView = itemView.findViewById(R.id.city_text_rv)
        val stateTextView: TextView = itemView.findViewById(R.id.state_text_rv)
        val countryTextView: TextView = itemView.findViewById(R.id.country_text_rv)
        val save_btn: ImageButton = itemView.findViewById(R.id.save_btn)
        val itemLayout: LinearLayout = itemView.findViewById(R.id.main_item_layout)

        /**
         * Binds layout properties such as background based on the current theme (day/night).
         */
        fun bind(){
            if(isDay){
                itemLayout.background = ContextCompat.getDrawable(mainActivity,
                    R.drawable.info_border_black
                )
            }
            else{
                itemLayout.background = ContextCompat.getDrawable(mainActivity,
                    R.drawable.info_border_white
                )
            }
        }

        /**
         * Initializes click listeners for saving and item selection, ensuring actions are only taken if the adapter position is valid.
         */
        init {
            save_btn.setOnClickListener{
                val position = bindingAdapterPosition
                if(position != RecyclerView.NO_POSITION) {
                    saveButtonListener?.onButtonClick(locationList[position])
                }
            }
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    itemListener?.onItemClick(locationList[position])
                }
            }
        }
    }

    // Adding variables for the itemListener and saveButtonListener
    private var itemListener: OnItemClickListener? = null
    private var saveButtonListener: OnSaveClickListener? = null

    /**
     * Custom listener interface for handling clicks on individual items.
     */
    interface OnItemClickListener {
        fun onItemClick(locationData: LocationData)
    }

    /**
     * Custom listener interface for handling clicks on the save button associated with an item.
     */
    interface OnSaveClickListener {
        fun onButtonClick(locationData: LocationData)
    }

    /**
     * Registers a callback to be invoked when an item in this adapter has been clicked.
     * @param listener The callback that will run
     */
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.itemListener = listener
    }

    /**
     * Registers a callback to be invoked when the save button in this adapter has been clicked.
     * @param listener The callback that will run
     */
    fun setOnSaveClickListener(listener: OnSaveClickListener){
        this.saveButtonListener = listener
    }

    /**
     * Updates the adapter's data set and refreshes the RecyclerView.
     * @param locationList The new list of locations to display.
     */
    fun setFilteredList(locationList: List<LocationData>){
        this.locationList = locationList
        notifyDataSetChanged()
    }

    /**
     * Inflates the layout from XML when needed, providing a new ViewHolder.
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.location_recycler_view_row, parent, false)
        return LocationViewHolder(view)
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     * @return The total number of items in this adapter.
     */
    override fun getItemCount(): Int {
        return locationList.size
    }

    /**
     * Replaces the contents of a view (invoked by the layout manager).
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.cityTextView.text = locationList[position].city + ", "
        holder.countryTextView.text = locationList[position].country
        if(locationList[position].country == "United States"){
            holder.stateTextView.text = locationList[position].state + ", "
        }
        else{
            holder.stateTextView.text = ""
        }
        holder.bind()
    }

    /**
     * Updates the theme background of the RecyclerView items based on time of day.
     * @param isDay Boolean indicating whether it is day (true) or night (false).
     */
    fun updateBackground(isDay: Boolean) {
        this.isDay = isDay
        notifyDataSetChanged()
    }
}