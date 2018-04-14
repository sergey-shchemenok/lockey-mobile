package ru.tradition.lockeymobile;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.TreeMap;

import ru.tradition.lockeymobile.auth.AuthQueryUtils;

import static ru.tradition.lockeymobile.auth.AuthQueryUtils.authCookieManager;

/**
 * Created by Caelestis on 12.04.2018.
 */

public final class SubscriptionQueryUtils {
    public static final String LOG_TAG = SubscriptionQueryUtils.class.getName();

    //Stores the response message for the request
    public static int subscriptionsUrlResponseCode;
    public static String subscriptionsUrlResponseMessage;

    /**
     * Create a private constructor because no one should ever create a {@link SubscriptionQueryUtils} object.
     */
    private SubscriptionQueryUtils() {
    }

    public static TreeMap<Integer, SubscriptionData> extractSubscriptions(String jsonResponse) {
        // Create an empty ArrayList that we can start adding polygons to
        TreeMap<Integer, SubscriptionData> subscriptions = new TreeMap<>();

        // Try to parse the SAMPLE_JSON_RESPONSE. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {
            // build up a list of objects with the corresponding data.
            JSONArray rootArray = new JSONArray(jsonResponse);
            for (int i = 0; i < rootArray.length(); i++) {
                JSONObject subscription = rootArray.getJSONObject(i);
                int sid = subscription.getInt("SID");
                String title = subscription.getString("Title");
                int zid = subscription.getInt("ZID");
                String zoneTitle = "Empty";
                if (AppData.mPolygonsMap != null && !AppData.mPolygonsMap.isEmpty())
                    zoneTitle = AppData.mPolygonsMap.get(zid).getPolygonName();
                boolean isSubscribed = subscription.getBoolean("Subscribed");
                JSONArray carsArray = subscription.getJSONArray("Cars");
                int[] cars = new int[carsArray.length()];
                for (int j = 0; j < carsArray.length(); j++) {
                    cars[j] = carsArray.getInt(j);
                }
                subscriptions.put(sid, new SubscriptionData(sid, title, zid, zoneTitle, isSubscribed, cars));
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the JSON results", e);
        }

        // Return the list of polygons
        return subscriptions;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            Uri builtUri = Uri.parse(stringUrl)
                    .buildUpon()
                    .appendQueryParameter("key", FirebaseInstanceId.getInstance().getToken())
                    .build();
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    public static TreeMap<Integer, SubscriptionData> fetchSubscriptionsData(String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);
        Log.v(LOG_TAG, "fetchSubscriptions");

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing input stream", e);
        }
        Log.e(LOG_TAG, "here jsonResponse     " + jsonResponse + " ");

        TreeMap<Integer, SubscriptionData> subscriptions = extractSubscriptions(jsonResponse);

        return subscriptions;
    }


    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    public static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";
        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        if (AppData.needToken) {
            AuthQueryUtils.makeHttpRequest(new URL(AppData.AUTH_REQUEST_URL), AppData.pwd, AppData.usr);
            AppData.needToken = false;
        }

        Log.e(LOG_TAG, "url..........." + url + "...");

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");

            if (authCookieManager.getCookieStore().getCookies().size() > 0) {
                urlConnection.setRequestProperty("Cookie",
                        TextUtils.join(";", authCookieManager.getCookieStore().getCookies()));
            }

            urlConnection.connect();
            Log.e(LOG_TAG, "url..........." + urlConnection.getResponseCode() + "......" + urlConnection.getResponseMessage());

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.

            subscriptionsUrlResponseCode = urlConnection.getResponseCode();
            subscriptionsUrlResponseMessage = urlConnection.getResponseMessage();
            if (subscriptionsUrlResponseCode == HttpURLConnection.HTTP_OK) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the polygons JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

}
