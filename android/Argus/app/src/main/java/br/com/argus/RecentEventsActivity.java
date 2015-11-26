package br.com.argus;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.webkit.WebView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class RecentEventsActivity extends BaseActivity {
    User user;
    static UserLocalStore userLS;
    static List<String> cookiesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        userLS = new UserLocalStore(this);
        user = userLS.getLoggedInUser();
        cookiesList = userLS.retrieveUserSessionCookies();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_events);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        JSONObject response = new JSONObject();

        try {
            response = getJSONObject("https://argus-adrianodennanni.c9.io/update_info");
        } catch (Exception e) {
            e.printStackTrace();
        }

        WebView web1 = (WebView) findViewById(R.id.webView);
        web1.loadUrl("https://argus-adrianodennanni.c9.io/update_info");

    }

    public String getJSON(String url, int timeout) {
        HttpURLConnection c = null;
        try {
            URL u = new URL(url);
            c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.setRequestProperty("Content-length", "0");
            c.setUseCaches(false);
            c.setAllowUserInteraction(false);
            c.setConnectTimeout(timeout);
            c.setReadTimeout(timeout);
            c.connect();
            int status = c.getResponseCode();

            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();
                    return sb.toString();
            }

        } catch (MalformedURLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (c != null) {
                try {
                    c.disconnect();
                } catch (Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return null;
    }


    private static JSONObject getJSONObject(String _url) throws Exception {
        if (_url.equals(""))
            throw new Exception("URL can't be empty");

        URL url = new URL(_url);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Cookie", "user_id=3;house_id=3");
        conn.connect();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setDoInput(true);
        conn.setRequestProperty("User-Agent", "android");
        conn.setRequestProperty("Accept", "application/json");
        conn.addRequestProperty("Content-Type", "application/json");
        for (String cookie : cookiesList) {
            cookie = cookie.substring(0, cookie.indexOf(";"));
            Log.d("Testando", cookie);
        }

        BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));

        if (!url.getHost().equals(conn.getURL().getHost())) {
            conn.disconnect();
            return new JSONObject();
        }
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        conn.disconnect();

        return new JSONObject(response.toString());

    }

}


