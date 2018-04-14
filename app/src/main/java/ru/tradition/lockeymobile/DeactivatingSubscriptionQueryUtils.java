package ru.tradition.lockeymobile;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import ru.tradition.lockeymobile.auth.AuthQueryUtils;

import static ru.tradition.lockeymobile.auth.AuthQueryUtils.authCookieManager;

/**
 * Created by Caelestis on 14.04.2018.
 */

public final class DeactivatingSubscriptionQueryUtils {
    public static final String LOG_TAG = ActivatingSubscriptionQueryUtils.class.getName();

    //Stores the response code for the request
    public static int deactivatingSubscriptionUrlResponseCode;

    //Stores the response message for the request
    public static String deactivatingSubscriptionUrlResponseMessage;

    private DeactivatingSubscriptionQueryUtils() {}

    public static String extractData(String jsonResponse) {

        String message = "";

        try {
            JSONObject rootObject = new JSONObject(jsonResponse);
            message = rootObject.getString("Message");
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the JSON results", e);
        }

        // Return the list of assets
        return message;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl, int sid) {
        URL url = null;
        try {
            Uri builtUri = Uri.parse(stringUrl)
                    .buildUpon()
                    .appendQueryParameter("sid", String.valueOf(sid))
                    .appendQueryParameter("key", FirebaseInstanceId.getInstance().getToken())
                    .build();
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    public static String fetchResponseMessage(String requestUrl, int sid) {
        // Create URL object
        URL url = createUrl(requestUrl, sid);
        Log.v(LOG_TAG, "fetch response Message");

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url, sid);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing input stream", e);
        }

        // Extract relevant fields from the JSON response and create an {@link Event} object

        String message = extractData(jsonResponse);

        // Return the {@link Event}
        return message;
    }


    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    public synchronized static String makeHttpRequest(URL url, int sid) throws IOException {
        String jsonResponse = "";
        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        if (AppData.needToken) {
            AuthQueryUtils.makeHttpRequest(new URL(AppData.AUTH_REQUEST_URL), AppData.pwd, AppData.usr);
            AppData.needToken = false;
        }

        StringBuilder sb = new StringBuilder();
        HttpURLConnection urlConnection = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("DELETE");

            if (authCookieManager.getCookieStore().getCookies().size() > 0) {
                urlConnection.setRequestProperty("Cookie",
                        TextUtils.join(";", authCookieManager.getCookieStore().getCookies()));
            }
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(15000);
            urlConnection.connect();

            deactivatingSubscriptionUrlResponseCode = urlConnection.getResponseCode();
            deactivatingSubscriptionUrlResponseMessage = urlConnection.getResponseMessage();
            if (deactivatingSubscriptionUrlResponseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        urlConnection.getInputStream(), "utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();

                return sb.toString();

            } else {
                return String.valueOf(deactivatingSubscriptionUrlResponseCode);
                //return urlConnection.getResponseMessage();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "error1";

        } catch (IOException e) {
            e.printStackTrace();
            return "error2";

        }  finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
    }
}
