package ru.tradition.lockeymobile.tabs.assetstab;

import android.text.TextUtils;
import android.util.Log;

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
import java.util.Date;
import java.util.TimeZone;
import java.util.TreeMap;

import ru.tradition.lockeymobile.AppData;
import ru.tradition.lockeymobile.auth.AuthQueryUtils;

import static ru.tradition.lockeymobile.auth.AuthQueryUtils.authCookieManager;


/**
 * Created by Caelestis on 22.01.2018.
 */

public final class AssetsQueryUtils {
    public static final String LOG_TAG = AssetsQueryUtils.class.getName();

    //Stores the response message for the request
    public static String assetsUrlResponseMessage;

    public static int assetsUrlResponseCode;


    /**
     * Create a private constructor because no one should ever create a {@link AssetsQueryUtils} object.
     */
    private AssetsQueryUtils() {
    }

    public static TreeMap<Integer, AssetsData> extractAssets(String jsonResponse) {
        // Create an empty ArrayList that we can start adding assets to
        TreeMap<Integer, AssetsData> assets = new TreeMap<>();

        // Try to parse the SAMPLE_JSON_RESPONSE. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {
            // build up a list of objects with the corresponding data.
            JSONArray rootArray = new JSONArray(jsonResponse);
            for (int i = 0; i < rootArray.length(); i++) {
                JSONObject asset = rootArray.getJSONObject(i);
                int cid = asset.getInt("CarID");
                int id = asset.getInt("ID");
                String name = asset.getString("Name");
                String model = asset.getString("Model");
                if (model.contains(";")) {
                    model = model.substring(model.indexOf(";") + 1).trim();
                }
                String regNumber = asset.getString("RegNumber");
                int lastSignalTime = getLastTimeInMilli(asset.getString("PositionTime"));
                double latitude = asset.getDouble("Latitude");
                double longitude = asset.getDouble("Longitude");
                assets.put(id, new AssetsData(cid, id, name, model, regNumber, lastSignalTime, latitude, longitude));
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the JSON results", e);
        }

        // Return the list of assets
        return assets;
    }

    private static int getLastTimeInMilli(String posTime) {
        int lastSignalTime = 1440;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Instant now = Instant.now();
            Instant then = Instant.parse(posTime);
            return (int) ((now.toEpochMilli() - then.toEpochMilli()) / 60000);
        } else {
            long now = new Date().getTime();
            Log.e(LOG_TAG, "try to get the last position time...");

            java.text.SimpleDateFormat simpleDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            try {
                Date then = simpleDateFormat.parse(posTime);
                long thenL = then.getTime();
                return (int) ((now - thenL) / 60000);

            } catch (java.text.ParseException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Problem with getting the last position time...", e);
            }
        }
        return lastSignalTime;
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

    public static TreeMap<Integer, AssetsData> fetchAssetsData(String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);
        Log.v(LOG_TAG, "fetchAssetsData");

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

        TreeMap<Integer, AssetsData> assetsList = extractAssets(jsonResponse);

        // Return the {@link Event}
        return assetsList;
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

        //todo process here
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

            assetsUrlResponseCode = urlConnection.getResponseCode();
            assetsUrlResponseMessage = urlConnection.getResponseMessage();
            if (assetsUrlResponseCode == HttpURLConnection.HTTP_OK) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
                //jsonResponse = "[{\"ID\":2670,\"Name\":\"х808рт77\",\"Model\":\"седельный   тягач; MAN\",\"RegNumber\":\"х808рт77\"},{\"ID\":5800,\"Name\":\"х108мо77\",\"Model\":\"fh; Volvo\",\"RegNumber\":\"х108мо77\"},{\"ID\":5801,\"Name\":\"с580км777\",\"Model\":\"FH; Вольво\",\"RegNumber\":\"с580км777\"},{\"ID\":6317,\"Name\":\"с416км777\",\"Model\":\"FH; Volvo\",\"RegNumber\":\"с416км777\"},{\"ID\":5807,\"Name\":\"с415км777\",\"Model\":\"FH; Volvo\",\"RegNumber\":\"с415км777\"},{\"ID\":116208,\"Name\":\"о901хк77\",\"Model\":\"седельный   тягач; MAN\",\"RegNumber\":\"о901хк77\"},{\"ID\":116237,\"Name\":\"х807рт77\",\"Model\":\"седельный   тягач; MAN\",\"RegNumber\":\"х807рт77\"},{\"ID\":120387,\"Name\":\"х109мо77\",\"Model\":\"FH; Volvo\",\"RegNumber\":\"х109мо77\"}]";
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the assets JSON results.", e);
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
