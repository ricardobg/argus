package br.com.argus;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;

public class StartActivity extends AppCompatActivity {

    UserLocalStore userLS;
    MyApp mApp = MyApp.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        userLS = new UserLocalStore(this);
        final CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        if(userLS.isLoggedIn()){

            login(userLS.getLoggedInUser());
            mApp.setCookieStore(cookieManager.getCookieStore());
            startActivity(new Intent(this, AlarmsActivity.class));
            finish();
        }
        else{
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

    }

    public void login(User user) {
        URL url;
        HttpURLConnection urlConnection = null;
        String message="";
        try {
            url = new URL("https://argus-adrianodennanni.c9.io/login?user="+user.username+"&password="+user.password);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            InputStream in = urlConnection.getInputStream();
            InputStreamReader isw = new InputStreamReader(in);



            int data = isw.read();
            while (data != -1) {
                char current = (char) data;
                data = isw.read();
                message+=current;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                urlConnection.disconnect();
            } catch (Exception e) {
                e.printStackTrace(); //If you want further info on failure...
            }
        }
        if(message != "")
        {
            userLS.setUserLoggedIn(true);
            userLS.storeUserData(user);
            startActivity(new Intent(this, AlarmsActivity.class));
        }
        else{
            AlertDialog alertDialog = new AlertDialog.Builder(StartActivity.this).create();
            alertDialog.setTitle("Server Response");
            alertDialog.setMessage("Falha Login");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }
}
