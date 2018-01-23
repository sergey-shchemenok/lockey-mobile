package ru.tradition.lockeymobile.auth;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by Caelestis on 23.01.2018.
 */

public final class AuthQueryUtils {
    public static final String LOG_TAG = AuthQueryUtils.class.getName();

    public static java.net.CookieManager authCookieManager = new java.net.CookieManager();

    private AuthQueryUtils() {}

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
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    public static String fetchAuthData(String requestUrl, String pwd, String usr) {
        // Create URL object
        URL url = createUrl(requestUrl);
        Log.v(LOG_TAG, "fetchAuthData");

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url, pwd, usr);
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
    public static String makeHttpRequest(URL url, String pwd, String usr) throws IOException {
        StringBuilder sb = new StringBuilder();
        HttpURLConnection urlConnection = null;

        try {
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("Usr", usr);
            jsonParam.put("Pwd", pwd);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(15000);
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setFixedLengthStreamingMode(jsonParam.toString().getBytes().length);
            urlConnection.setRequestProperty("Content-Type", "application/json;charset=utf-8"); //;charset=utf-8
            //urlConnection.setRequestProperty("Host", "my.lockey.ru");
            urlConnection.connect();

            OutputStream os = new BufferedOutputStream(urlConnection.getOutputStream());
            os.write(jsonParam.toString().getBytes());
            os.flush();

            Map<String, List<String>> headerFields = urlConnection.getHeaderFields();
            List<String> cookiesHeader = headerFields.get("Set-Cookie");

            if (cookiesHeader != null) {
                for (String cookie : cookiesHeader) {
                    authCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                }
            }

            int httpResult = urlConnection.getResponseCode();
            if (httpResult == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        urlConnection.getInputStream(), "utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();

                return sb.toString();

            } else {
                return String.valueOf(httpResult);
                //return urlConnection.getResponseMessage();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "error1";

        } catch (IOException e) {
            e.printStackTrace();
            return "error2";

        } catch (JSONException e) {
            e.printStackTrace();
            return "error3";

        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
    }

    }
