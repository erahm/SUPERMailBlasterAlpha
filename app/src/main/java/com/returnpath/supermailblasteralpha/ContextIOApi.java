package com.returnpath.supermailblasteralpha;

import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

class ContextIOApi extends AsyncTask<String, Void, String> {

    private Exception exception;

    protected String doInBackground(String... urls) {
        OkHttpClient client = new OkHttpClient();
//        OAuthService service = new ServiceBuilder()
//                .provider(DefaultApi10a.class)
//                .apiKey("")
//                .apiSecret("")
//                .build();

        // TODO: modify url.
        String url = urls[0];
//        String url = "https://api.context.io/2.0/accounts/55ba5f4ddfc24787518b4567/contacts/hyrum.toth%40returnpath.com";
        Request request = new Request.Builder()
                .url(url)
                .build();

        String answer = "no answer";
        try {
            Response response = client.newCall(request).execute();
            answer = response.body().string();
            Log.d("ContextIOApi", "response = " + answer);
        } catch (IOException e) {
            Log.e("ContextIOApi", "Failed to get request from URL");
            e.printStackTrace();
        }

        return answer;
    }

}