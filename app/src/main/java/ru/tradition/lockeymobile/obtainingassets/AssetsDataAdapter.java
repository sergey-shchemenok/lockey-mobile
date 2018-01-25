package ru.tradition.lockeymobile.obtainingassets;

import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ru.tradition.lockeymobile.R;

/**
 * Created by Caelestis on 22.01.2018.
 */

public class AssetsDataAdapter extends ArrayAdapter<AssetsData> {

    public AssetsDataAdapter(Activity context, List<AssetsData> data) {
        super(context, 0, data);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if the existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_assets_item, parent, false);
        }

        AssetsData currentAssetsData = getItem(position);

        // Find the TextView in the list_assets_item.xml layout with the ID version_name
        ImageView lastSignalTimeView = (ImageView) listItemView.findViewById(R.id.last_signal_time);
        ImageView lastSignalTimeInnerView = (ImageView) listItemView.findViewById(R.id.last_signal_time_inner);


        TextView kitNumberView = (TextView) listItemView.findViewById(R.id.kit_number);
        kitNumberView.setText(String.valueOf(currentAssetsData.getId()));

        TextView regNumberView = (TextView) listItemView.findViewById(R.id.reg_number);
        regNumberView.setText(currentAssetsData.getRegNumber());

        TextView carModelView = (TextView) listItemView.findViewById(R.id.car_model);
        carModelView.setText(currentAssetsData.getModel());

        // Fetch the background from the TextView, which is a GradientDrawable.
        GradientDrawable lastSignalTimeCircle = (GradientDrawable) lastSignalTimeView.getBackground();
        GradientDrawable lastSignalTimeInnerCircle = (GradientDrawable) lastSignalTimeInnerView.getBackground();


        // Get the appropriate background color based on the last time signal
        int lastSignalTimeColor = getlastSignalTimeColor(currentAssetsData.getLastSignalTime());

        // Set the color on the last time signal circle
        lastSignalTimeCircle.setColor(lastSignalTimeColor);
        lastSignalTimeInnerCircle.setColor(lastSignalTimeColor);

        return listItemView;
    }

    private int getlastSignalTimeColor(int lastSignalTime) {
        int lastSignalTimeColorResourceId = R.color.lst5;//серый
        if (lastSignalTime < 15 && lastSignalTime >= 0) {
            lastSignalTimeColorResourceId = R.color.lst1;//зеленый
        } else if (lastSignalTime < 60 && lastSignalTime >= 15) {
            lastSignalTimeColorResourceId = R.color.lst3;//желтый
        }
        else if (lastSignalTime < 1440) {
            lastSignalTimeColorResourceId = R.color.lst2;//красный
        }
        return ContextCompat.getColor(getContext(), lastSignalTimeColorResourceId);
    }

}
