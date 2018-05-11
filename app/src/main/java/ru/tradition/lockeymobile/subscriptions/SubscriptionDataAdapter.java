package ru.tradition.lockeymobile.subscriptions;

import android.app.Activity;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ru.tradition.lockeymobile.AppData;
import ru.tradition.lockeymobile.R;

/**
 * Created by Caelestis on 13.04.2018.
 */

public class SubscriptionDataAdapter extends ArrayAdapter<SubscriptionData> {

    public SubscriptionDataAdapter(Activity context, List<SubscriptionData> data) {
        super(context, 0, data);
    }

    public void swapItems(List<SubscriptionData> newList) {
        List<SubscriptionData> oldList = this.getAllAdapterList();
        List<SubscriptionData> swapList;
        if (newList.size() >= oldList.size()) {
            swapList = newList;
        } else {
            swapList = oldList;
        }
        for (int i = 0; i < swapList.size(); i++) {
            if (i < newList.size()) {
                this.insert(newList.get(i), i);
            }
            if (i < oldList.size()) {
                this.remove(oldList.get(i));
            }

        }

        this.notifyDataSetChanged();
    }

    public List<SubscriptionData> getAllAdapterList() {
        int count = this.getCount();
        List<SubscriptionData> objVal = new ArrayList<SubscriptionData>();

        for (int i = 0; i < count; i++) {
            objVal.add(this.getItem(i));
        }
        return objVal;
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
            subscriptionStatus.setText("Активация подписки");
            subscriptionStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.assetTableHeaderColor));
        } else if (AppData.deactivatingSubscription.contains(currentSubscriptionsData.getSid())) {
            subscriptionStatus.setText("Деактивация подписки");
            subscriptionStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.assetTableHeaderColor));
        } else if (isSubscribed) {
            subscriptionStatus.setText("Подписан");
            subscriptionStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.assetTableContentColor));
        } else {
            subscriptionStatus.setText("Не подписан");
            subscriptionStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.assetTableContentColor));
        }

        TextView kitsNumbers = (TextView) listItemView.findViewById(R.id.kits_numbers);
        int[] cars = currentSubscriptionsData.getCars();
        if (cars.length > 0) {
            String carString = "";
            for (int x : cars)
                carString += x + ", ";
            kitsNumbers.setText(carString.substring(0, (carString.length() - 2)));
        } else {
            kitsNumbers.setText("Получение списка объектов");
        }

        return listItemView;
    }

}
