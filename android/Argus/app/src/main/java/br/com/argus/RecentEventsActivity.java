package br.com.argus;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class RecentEventsActivity extends BaseActivity {
    private static final String TAG_EVENTS = "events";
    private static final String TAG_ID = "id";
    private static final String TAG_TIMESTAMP = "timestamp";
    private static final String TAG_TYPE = "type";
    private static final String TAG_SNAP = "snap";

    User user;
    static UserLocalStore userLS;
    MyApp mApp = MyApp.getInstance();
    ImageView iv;
    ListView list;
    Context context;

    ArrayList<HashMap<String, String>> eventlist = new ArrayList<HashMap<String, String>>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;

        userLS = new UserLocalStore(this);
        user = userLS.getLoggedInUser();
        CookieHandler.setDefault(new CookieManager(mApp.getCookieStore(), CookiePolicy.ACCEPT_ALL));
        List<HttpCookie> cookies = mApp.getCookieStore().getCookies();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_events);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        JSONObject response;
        JSONArray events = null;
        try {
            response = getJSONObject("https://argus-adrianodennanni.c9.io/update_info");
            events = response.getJSONArray(TAG_EVENTS);
            for(int i = 0; i < events.length(); i++){
                JSONObject c = events.getJSONObject(i);

                // Storing  JSON item in a Variable
                String id = "ID: " + c.getString(TAG_ID);

                Date time = new Date((long)(c.getInt(TAG_TIMESTAMP))* (long) 1000);
                String timestamp = String.format("Time: %1$tT, %1$tm/%1$td/%1$tY", time);
                String type = "Type: " + c.getString(TAG_TYPE);
                String snap = "";
                String snapfound = "";
                try{
                    JSONObject snapObject = c.getJSONObject(TAG_SNAP);
                    snap = snapObject.getString("link");
                    snapfound = "Touch to view the snap";
                }catch(JSONException e){
                    snap = "";
                    snapfound = "";
                }

                // Adding value HashMap key => value

                HashMap<String, String> map = new HashMap<String, String>();

                map.put(TAG_ID, id);
                map.put(TAG_TIMESTAMP, timestamp);
                map.put(TAG_TYPE, type);
                map.put(TAG_SNAP, snap);
                map.put("snapfound", snapfound);
                eventlist.add(map);
                list=(ListView)findViewById(R.id.listview_events);

                ListAdapter adapter = new SimpleAdapter(RecentEventsActivity.this, eventlist,
                        R.layout.list_v,
                        new String[] { TAG_ID,TAG_TIMESTAMP, TAG_TYPE, "snapfound"}, new int[] {
                        R.id.id,R.id.timestamp, R.id.type, R.id.snapfound});

                list.setAdapter(adapter);
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        String snapUrl = eventlist.get(+position).get(TAG_SNAP);
                        if(snapUrl != ""){
                            iv = (ImageView) findViewById(R.id.image_view);
                            new DownloadImageTask(iv).execute(snapUrl);
                            iv.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    iv.setClickable(false);
                                    iv.setVisibility(View.INVISIBLE);
                                }
                            });
                        }
                    }
                });

            }
            Log.d("Testando",response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }


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
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setDoInput(true);
        conn.setRequestProperty("User-Agent", "android");
        conn.setRequestProperty("Accept", "application/json");
        conn.addRequestProperty("Content-Type", "application/json");
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

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        private ProgressDialog pDialog;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RecentEventsActivity.this);
            pDialog.setMessage("Getting Data ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

        }
        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
                Log.d("Teste", urldisplay);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            pDialog.dismiss();
            bmImage.setImageBitmap(result);
            bmImage.setClickable(true);
            bmImage.setVisibility(View.VISIBLE);
        }
    }
}


