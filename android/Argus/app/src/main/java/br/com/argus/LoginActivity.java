package br.com.argus;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    User user;
    UserLocalStore userLS;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        userLS = new UserLocalStore(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        final Button loginButton = (Button) findViewById(R.id.loginButton);
        final EditText usernameText = (EditText) findViewById(R.id.usernameText);
        final EditText passwordText = (EditText) findViewById(R.id.passwordText);

        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                user = new User(usernameText.getText().toString(), passwordText.getText().toString(), 1, 1);
                login(user);
            }
        });


    }




    public void login(User user) {
        URL url;
        HttpURLConnection urlConnection = null;
        String message="";
        try {
            url = new URL("https://argus-adrianodennanni.c9.io/login?user="+user.username+"&password="+user.password);


            urlConnection = (HttpURLConnection) url
                    .openConnection();

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

        AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
        alertDialog.setTitle("Server Response");
        if(message != "")
        {
            alertDialog.setMessage("Login OK");
            userLS.setUserLoggedIn(true);
            userLS.storeUserData(user);
            startActivity(new Intent(this, AlarmsActivity.class));
        }
        else
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
