package ru.tradition.lockeymobile;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import ru.tradition.lockeymobile.tabs.assetstab.AssetsData;

/**
 * Created by Caelestis on 13.04.2018.
 */

public class SubscriptionDataAdapter extends ArrayAdapter<SubscriptionData> {

    public SubscriptionDataAdapter(Activity context, List<SubscriptionData> data) {
        super(context, 0, data);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if the existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.subscription_list_item, parent, false);
        }

        SubscriptionData currentSubscriptionsData = getItem(position);

        //root view
        LinearLayout rootView = (LinearLayout) listItemView.findViewById(R.id.list_subscription_root);
        rootView.setBackgroundColor(Color.WHITE);

        if (AppData.selectedSubscription.contains(currentSubscriptionsData.getSid())) {
            rootView.setBackgroundColor(Color.LTGRAY);
        } else
            rootView.setBackgroundColor(Color.WHITE);

        TextView subscriptionName = (TextView) listItemView.findViewById(R.id.subscription_name);
        subscriptionName.setText(String.valueOf(currentSubscriptionsData.getTitle()));

        TextView zoneName = (TextView) listItemView.findViewById(R.id.zone_name);
        zoneName.setText(currentSubscriptionsData.getZoneTitle());

        TextView subscriptionStatus = (TextView) listItemView.findViewById(R.id.subscription_status);
        boolean isSubscribed = currentSubscriptionsData.isSubscribed();

        if (AppData.activatingSubscription.contains(currentSubscriptionsData.getSid())) {
            subscriptionStatus.setText("Активация");
        } else if (AppData.deactivatingSubscription.contains(currentSubscriptionsData.getSid())) {
            subscriptionStatus.setText("Деактивация");
        } else if (isSubscribed)
            subscriptionStatus.setText("Активна");
        else
            subscriptionStatus.setText("Неактивна");

        TextView kitsNumbers = (TextView) listItemView.findViewById(R.id.kits_numbers);
        int[] cars = currentSubscriptionsData.getCars();
        String carString = "";
        for (int x : cars)
            carString += x + ", ";
        kitsNumbers.setText(carString.substring(0, (carString.length() - 2)));

        return listItemView;
    }

}
