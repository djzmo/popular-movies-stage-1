package com.djzmo.popularmoviesstage1;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import java.net.MalformedURLException;
import java.net.URL;

class NetworkUtils {

    private final static String API_BASE_URL = "https://api.themoviedb.org/3";
    private final static String POPULAR_ENDPOINT = "/movie/popular";
    private final static String TOP_RATED_ENDPOINT = "/movie/top_rated";
    private final static String MOVIE_DETAIL_ENDPOINT = "/movie/{id}";
    public final static String THUMBNAIL_BASE_URL = "http://image.tmdb.org/t/p";
    public final static String API_KEY = "YOUR_API_KEY";

    private final static String API_KEY_QUERY = "api_key";

    // from: https://stackoverflow.com/questions/4238921/detect-whether-there-is-an-internet-connection-available-on-android
    public static boolean isOnline(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private static URL buildCommonApiUrl(String urlString) {
        Uri uri = Uri.parse(urlString).buildUpon()
                .appendQueryParameter(API_KEY_QUERY, API_KEY)
                .build();
        URL url = null;

        try {
            url = new URL(uri.toString());
        }
        catch(MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static URL getPopularUrl() {
        return buildCommonApiUrl(API_BASE_URL + POPULAR_ENDPOINT);
    }

    public static URL getTopRatedUrl() {
        return buildCommonApiUrl(API_BASE_URL + TOP_RATED_ENDPOINT);
    }

    public static URL getMovieDetailUrl(String id) {
        String urlString = API_BASE_URL + MOVIE_DETAIL_ENDPOINT;
        urlString = urlString.replace("{id}", id);
        return buildCommonApiUrl(urlString);
    }

}
