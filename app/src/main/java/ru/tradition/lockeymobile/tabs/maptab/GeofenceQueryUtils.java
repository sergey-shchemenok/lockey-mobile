package ru.tradition.lockeymobile.tabs.maptab;

import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.TreeMap;

import ru.tradition.lockeymobile.AppData;
import ru.tradition.lockeymobile.auth.AuthQueryUtils;
import ru.tradition.lockeymobile.tabs.assetstab.AssetsData;
import ru.tradition.lockeymobile.tabs.assetstab.AssetsQueryUtils;

import static ru.tradition.lockeymobile.auth.AuthQueryUtils.authCookieManager;

/**
 * Created by Caelestis on 11.04.2018.
 */

public final class GeofenceQueryUtils {
    public static final String LOG_TAG = GeofenceQueryUtils.class.getName();

    //Stores the response message for the request
    public static String zonesUrlResponseMessage;

    public static int zonesUrlResponseCode;


    /**
     * Create a private constructor because no one should ever create a {@link GeofenceQueryUtils} object.
     */
    private GeofenceQueryUtils() {
    }

    public static TreeMap<Integer, GeofencePolygon> extractPolygons(String jsonResponse) {
        // Create an empty ArrayList that we can start adding polygons to
        TreeMap<Integer, GeofencePolygon> polygons = new TreeMap<>();

        // Try to parse the SAMPLE_JSON_RESPONSE. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {
            // build up a list of objects with the corresponding data.
            JSONArray rootArray = new JSONArray(jsonResponse);
            for (int i = 0; i < rootArray.length(); i++) {
                JSONObject zone = rootArray.getJSONObject(i);
                int id = zone.getInt("ID");
                String name = zone.getString("Name");
                boolean isPrivate = zone.getBoolean("Private");
                JSONArray pointsArray = zone.getJSONArray("Points");
                LatLng[] polygon = new LatLng[pointsArray.length()];
                for (int j = 0; j < pointsArray.length(); j++) {
                    JSONArray pointsLatLng = pointsArray.getJSONArray(j);
                    polygon[j] = new LatLng(pointsLatLng.getDouble(0), pointsLatLng.getDouble(1));
                }
                polygons.put(id, new GeofencePolygon(id, name, isPrivate, polygon));
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the JSON results", e);
        }

        // Return the list of polygons
        return polygons;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    public static TreeMap<Integer, GeofencePolygon> fetchZonesData(String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);
        Log.v(LOG_TAG, "fetchZones");

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing input stream", e);
        }
        Log.e(LOG_TAG, "here jsonResponse     " + jsonResponse + " ");

        // Extract relevant fields from the JSON response and create an {@link Event} object
        //jsonResponse = "[{\"ID\":2670,\"Name\":\"х808рт77\",\"Model\":\"седельный   тягач; MAN\",\"RegNumber\":\"х808рт77\"},{\"ID\":5800,\"Name\":\"х108мо77\",\"Model\":\"fh; Volvo\",\"RegNumber\":\"х108мо77\"},{\"ID\":5801,\"Name\":\"с580км777\",\"Model\":\"FH; Вольво\",\"RegNumber\":\"с580км777\"},{\"ID\":6317,\"Name\":\"с416км777\",\"Model\":\"FH; Volvo\",\"RegNumber\":\"с416км777\"},{\"ID\":5807,\"Name\":\"с415км777\",\"Model\":\"FH; Volvo\",\"RegNumber\":\"с415км777\"},{\"ID\":116208,\"Name\":\"о901хк77\",\"Model\":\"седельный   тягач; MAN\",\"RegNumber\":\"о901хк77\"},{\"ID\":116237,\"Name\":\"х807рт77\",\"Model\":\"седельный   тягач; MAN\",\"RegNumber\":\"х807рт77\"},{\"ID\":120387,\"Name\":\"х109мо77\",\"Model\":\"FH; Volvo\",\"RegNumber\":\"х109мо77\"}]";

        TreeMap<Integer, GeofencePolygon> polygonsMap = extractPolygons(jsonResponse);

        // Return the {@link Event}
        return polygonsMap;
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

            zonesUrlResponseCode = urlConnection.getResponseCode();
            zonesUrlResponseMessage = urlConnection.getResponseMessage();
            if (zonesUrlResponseCode == HttpURLConnection.HTTP_OK) {
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
