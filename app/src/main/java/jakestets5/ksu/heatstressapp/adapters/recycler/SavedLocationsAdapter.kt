package jakestets5.ksu.heatstressapp.adapters.recycler

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import jakestets5.ksu.heatstressapp.data.`object`.LocationData
import jakestets5.ksu.heatstressapp.R
import jakestets5.ksu.heatstressapp.activities.SavedLocationsActivity

/**
 * Adapter for displaying saved locations in a RecyclerView. This adapter provides functionality for item clicks and removing items.
 *
 * @param savedLocations A mutable list of LocationData, representing saved location entries.
 */
class SavedLocationsAdapter(private var savedLocations: MutableList<LocationData>): RecyclerView.Adapter<SavedLocationsAdapter.SavedLocationsViewHolder>()  {

    /**
     * ViewHolder class for saved locations. Provides binding for the city, country, state text views and a remove button.
     */
    inner class SavedLocationsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val city: TextView = itemView.findViewById(R.id.city_text_rv)
        val country: TextView = itemView.findViewById(R.id.country_text_rv)
        val state: TextView = itemView.findViewById(R.id.state_text_rv)
        val removeBtn: ImageButton = itemView.findViewById(R.id.remove_btn)

        init {
            // Setup the remove button click listener.
            removeBtn.setOnClickListener {
                val position = bindingAdapterPosition
                removeListener?.onRemoveClick(position)
            }

            // Setup the item click listener.
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                itemListener?.onItemClick(savedLocations[position])
            }
        }
    }

    // Adding variables for the itemListener and saveButtonListener
    private var itemListener: OnItemClickListener? = null
    private var removeListener: OnRemoveClickListener? = null

    /**
     * Interface for handling removal of an item from the RecyclerView.
     */
    interface OnRemoveClickListener {
        fun onRemoveClick(position: Int)
    }

    /**
     * Interface for handling item clicks within the RecyclerView.
     */
    interface OnItemClickListener{
        fun onItemClick(locationData: LocationData)
    }

    /**
     * Sets the listener for item clicks.
     * @param listener Instance of OnItemClickListener.
     */
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.itemListener = listener
    }

    /**
     * Sets the listener for remove button clicks.
     * @param listener Instance of OnRemoveClickListener.
     */
    fun setOnRemoveClickListener(listener: OnRemoveClickListener){
        this.removeListener = listener
    }

    /**
     * Removes an item from the list at the specified position.
     * @param position The position of the item in the list.
     */
    fun removeItem(position: Int) {
        savedLocations.removeAt(position)
        notifyItemRemoved(position)
    }

    /**
     * Inflates the layout for individual list items.
     * @param parent The ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedLocationsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.saved_locations_recycler_row, parent, false)
        return SavedLocationsViewHolder(view)
    }

    /**
     * Binds the data to the views in the specified ViewHolder.
     * @param holder The ViewHolder into which the data should be bound.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: SavedLocationsViewHolder, position: Int) {

        holder.city.text = savedLocations[position].city + ", "
        if(savedLocations[position].country == "United States"){
            holder.state.text = savedLocations[position].state + ", "
        }
        else{
            holder.state.text = ""
        }
        holder.country.text = savedLocations[position].country

    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     * @return The total number of items in this adapter.
     */
    override fun getItemCount(): Int{
        return savedLocations.size
    }
}