package com.example.okhttp3;

import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public enum NetworkService implements NetworkInterface {
    INSTANCE;

    private static final long CONNECT_TIMEOUT = 20000;   // 2 seconds
    private static final long READ_TIMEOUT = 20000;      // 2 seconds
    private static OkHttpClient okHttpClient = null;
    private static final String SEARCH_URL = "https://api.myjson.com/bins/hoh4j/";


    /**
     * Method to build and return an OkHttpClient so we can set/get
     * headers quickly and efficiently.
     * @return OkHttpClient
     */
    private OkHttpClient buildClient() {
        if (okHttpClient != null) return okHttpClient;

        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS);

        // Logging interceptor
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        okHttpClientBuilder.addInterceptor(httpLoggingInterceptor);

        // custom interceptor for adding header and NetworkMonitor sliding window
        okHttpClientBuilder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                // Add whatever we want to our request headers.
                Request request = chain.request().newBuilder().addHeader("Accept", "application/json").build();
                Response response;
                try {
                    response = chain.proceed(request);
                } catch (SocketTimeoutException | UnknownHostException e) {
                    e.printStackTrace();
                    throw new IOException(e);
                }
                return response;
            }
        });

        return  okHttpClientBuilder.build();
    }

    private Request.Builder buildRequest(URL url) {
        return new Request.Builder()
                .url(url);
    }

    private Request.Builder buildRequest(URL url, String credential) {
        return buildRequest(url).header("Authorization", credential);
    }

    private URL buildURL(Uri builtUrl) {
        if (builtUrl == null) return null;
        try {
            String urlStr = builtUrl.toString();
            return new URL(urlStr);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private URL buildURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getData(Request request) {
        OkHttpClient client = buildClient();
        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public String getString(String endpoint, String username, String password) {
        Log.d("NetworkService", "getString by username and password from " + endpoint);
        String credentials = username + ":" + password;
        final String basicAuth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        Request request = buildRequest(buildURL(endpoint), basicAuth).build();
        return getData(request);
    }

    @Override
    public String getString(String endpoint, String token) {
        Log.d("NetworkService", "getString by Bearer token from " + endpoint);
        String credentials = "Bearer " + token;
        Request request = buildRequest(buildURL(endpoint), credentials).build();
        return getData(request);
    }

    @Override
    public String search(String query) {
        Uri uri = Uri.parse(SEARCH_URL)
                .buildUpon()
                .appendQueryParameter("name", query)
                .appendQueryParameter("category", "student")
                .build();
        URL url = buildURL(uri);

        Log.d("NetworkService","built search url: " + url.toString());

        Request request = buildRequest(url).build();

        return getData(request);
    }

}
