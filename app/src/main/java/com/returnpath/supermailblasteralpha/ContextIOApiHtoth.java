package com.returnpath.supermailblasteralpha;

import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.DefaultApi10a;
import org.scribe.oauth.OAuthService;

import com.thirdparty.contextio.ContextIOApi;
import com.thirdparty.contextio.ContextIO_V20;
import com.thirdparty.contextio.ContextIOResponse;

//import com.thirdparty.tomtasche.contextio.ContextIO;
//import com.thirdparty.tomtasche.contextio.ContextIOApi;
//import com.thirdparty.tomtasche.contextio.ContextIOResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class ContextIOApiHtoth extends AsyncTask<String, Void, String> {

    private Exception exception;

    protected String doInBackground(String... urls) {
        String answer = "no answer";
        ContextIO_V20 c = new ContextIO_V20("y1jt7fm9", "VfQnrIELqPkSe3Ft");
        ContextIOResponse r = c.getFolders("55ba5f4ddfc24787518b4567");
        org.scribe.model.Response response = r.getRawResponse();
        Log.d("htoth-reponse", "response is::::");
        Log.d("asdf", response.getBody());
//
//        // TODO: get key and secret from Settings/Preferences
//        ContextIO dokdok = new ContextIO("y1jt7fm9", "VfQnrIELqPkSe3Ft");
//
//        Map<String, String> params = new HashMap<String, String>();
//        params.put("since", "0");
//
//        ContextIOResponse cResponse = dokdok.addresses("55ba5f4ddfc24787518b4567");
//        cResponse.

//        System.out.println(dokdok.allMessages("hyrum.toth@returnpath.com", params).rawResponse.getBody());
        return answer;
    }
    protected String doInBackgroundHtoth(String... urls) {
        OkHttpClient client = new OkHttpClient();
        OAuthService service = new ServiceBuilder()
                .provider(DefaultApi10a.class)
                .apiKey("")
                .apiSecret("")
                .build();

        // TODO: modify url.
        String url = urls[0];
//        String url = "https://api.context.io/2.0/accounts/55ba5f4ddfc24787518b4567/contacts/hyrum.toth%40returnpath.com";
        Request request = new Request.Builder()
                .url(url)
//                .url("https://api.context.io/2.0/accounts/55ba5f4ddfc24787518b4567/contacts/hyrum.toth%40returnpath.com")
                .build();

        String answer = "no answer";
        try {
            Response response = client.newCall(request).execute();
            answer = response.body().string();
            Log.d("ContextIOApiHtoth", "response = " + answer);
        } catch (IOException e) {
            Log.e("ContextIOApiHtoth", "Failed to get request from URL");
            e.printStackTrace();
        }

        return answer;
    }

}