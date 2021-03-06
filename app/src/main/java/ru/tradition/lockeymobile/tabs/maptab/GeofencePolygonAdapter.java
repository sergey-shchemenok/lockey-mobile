/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.tradition.lockeymobile.tabs.maptab;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import ru.tradition.lockeymobile.R;

public class GeofencePolygonAdapter extends RecyclerView.Adapter<GeofencePolygonAdapter.PolygonNameViewHolder> {

    private static final String LOG_TAG = GeofencePolygonAdapter.class.getSimpleName();

    /*
     * An on-click handler that we've defined to make it easy for an Activity to interface with
     * our RecyclerView
     */
    final private ListItemClickListener mOnClickListener;
    final private ListItemLongClickListener mOnLongClickListener;

    private static int viewHolderCount;

    //list of polygons for geofencing
    private List<GeofencePolygon> mGeofencePolygons;

    /**
     * The interface that receives onClick messages.
     */
    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }

    public interface ListItemLongClickListener {
        void onListItemLongClick(int clickedItemIndex);
    }


    /**
     * Constructor for GeofencePolygonAdapter that accepts a number of items to display and the specification
     * for the ListItemClickListener.
     *
     * @param numberOfItems Number of items to display in list
     * @param listener      Listener for list item clicks
     */
    public GeofencePolygonAdapter(List<GeofencePolygon> geofencePolygons, ListItemClickListener listener, ListItemLongClickListener longListener) {
        mGeofencePolygons = geofencePolygons;
        mOnClickListener = listener;
        mOnLongClickListener = longListener;
        viewHolderCount = 0;
    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType  If your RecyclerView has more than one type of item (which ours doesn't) you
     *                  can use this viewType integer to provide a different layout. See
     *                  {@link android.support.v7.widget.RecyclerView.Adapter#getItemViewType(int)}
     *                  for more details.
     * @return A new PolygonNameViewHolder that holds the View for each list item
     */
    @Override
    public PolygonNameViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.geofence_polygon_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        PolygonNameViewHolder viewHolder = new PolygonNameViewHolder(view);

//                .getViewHolderBackgroundColorFromInstance(context, viewHolderCount);
//        viewHolder.itemView.setBackgroundColor(backgroundColorForViewHolder);

        //viewHolder.polygonViewHolder.setBackgroundColor(Color.WHITE);


        viewHolderCount++;
        Log.d(LOG_TAG, "onCreateViewHolder: number of ViewHolders created: "
                + viewHolderCount);
        return viewHolder;
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the correct
     * indices in the list for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(PolygonNameViewHolder holder, int position) {
        Log.d(LOG_TAG, "#" + position);
        if (holder != null) {
            if (position == MapFragmentTab.polygonNamesNumber||position == MapFragmentTabOSM.polygonNamesNumber)
                holder.setColorRed();
            else
                holder.setColorDarkGray();
            holder.bind(position);
        }
    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available
     */
    @Override
    public int getItemCount() {
        if (mGeofencePolygons != null && !mGeofencePolygons.isEmpty())
            return mGeofencePolygons.size();
        else return 0;
    }


    // COMPLETED (5) Implement OnClickListener in the PolygonNameViewHolder class

    /**
     * Cache of the children views for a list item.
     */
    class PolygonNameViewHolder extends RecyclerView.ViewHolder
            implements OnClickListener, View.OnLongClickListener {

        LinearLayout polygonViewHolder;

        // Will display the position in the list, ie 0 through getItemCount() - 1
        TextView polygonNameView;

        /**
         * Constructor for our ViewHolder. Within this constructor, we get a reference to our
         * TextViews and set an onClickListener to listen for clicks. Those will be handled in the
         * onClick method below.
         *
         * @param itemView The View that you inflated in
         *                 {@link GeofencePolygonAdapter#onCreateViewHolder(ViewGroup, int)}
         */
        public PolygonNameViewHolder(View itemView) {
            super(itemView);
            polygonNameView = (TextView) itemView.findViewById(R.id.polygon_name);
            polygonViewHolder = (LinearLayout) itemView.findViewById(R.id.polygon_viewholder);
            polygonViewHolder.setOnClickListener(this);
            polygonViewHolder.setOnLongClickListener(this);
        }

        /**
         * A method we wrote for convenience. This method will take an integer as input and
         * use that integer to display the appropriate text within a list item.
         *
         * @param listIndex Position of the item in the list
         */
        void bind(int position) {
            polygonNameView.setText(mGeofencePolygons.get(position).getPolygonName());
        }

        void setColorRed() {
            //polygonViewHolder.setBackgroundColor(Color.LTGRAY);
            polygonNameView.setTextColor(Color.RED);
        }

        void setColorDarkGray() {
            //polygonViewHolder.setBackgroundColor(Color.WHITE);
            polygonNameView.setTextColor(Color.parseColor("#2B3D4D"));
        }


        // COMPLETED (6) Override onClick, passing the clicked item's position (getAdapterPosition()) to mOnClickListener via its onListItemClick method

        /**
         * Called whenever a user clicks on an item in the list.
         *
         * @param v The View that was clicked
         */
        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(clickedPosition);
            if (clickedPosition == MapFragmentTab.polygonNamesNumber ||
                    clickedPosition == MapFragmentTabOSM.polygonNamesNumber)
                setColorRed();
            else
                setColorDarkGray();
//            MapFragmentTab.mAdapter.onBindViewHolder(this, clickedPosition++);
        }

        @Override
        public boolean onLongClick(View view) {
            // Handle long click
            // Return true to indicate the click was handled
            int clickedPosition = getAdapterPosition();
            mOnLongClickListener.onListItemLongClick(clickedPosition);
            setColorRed();
            return true;
        }

    }
}
