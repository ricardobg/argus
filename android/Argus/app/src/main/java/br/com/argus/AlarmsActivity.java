package br.com.argus;

import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class AlarmsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarms);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Switch switch1 = (Switch) findViewById(R.id.switch1);
        Switch switch2 = (Switch) findViewById(R.id.switch2);
        Switch switch3 = (Switch) findViewById(R.id.switch3);

        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.v("Switch State=", "" + isChecked);


                URL url;
                HttpURLConnection urlConnection = null;
                try {
                    if (isChecked) {
                        url = new URL("https://argus-adrianodennanni.c9.io/alarm_switch?house_id=1&active=1");
                    } else {
                        url = new URL("https://argus-adrianodennanni.c9.io/alarm_switch?house_id=1&active=0");
                    }


                    urlConnection = (HttpURLConnection) url
                            .openConnection();

                    InputStream in = urlConnection.getInputStream();

                    InputStreamReader isw = new InputStreamReader(in);

                    int data = isw.read();
                    while (data != -1) {
                        char current = (char) data;
                        data = isw.read();
                        System.out.print(current);
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


            }

        });



        switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.v("Switch State=", "" + isChecked);


                URL url;
                HttpURLConnection urlConnection = null;
                try {
                    if (isChecked) {
                        url = new URL("https://argus-adrianodennanni.c9.io/alarm_switch?house_id=2&active=1");
                    } else {
                        url = new URL("https://argus-adrianodennanni.c9.io/alarm_switch?house_id=2&active=0");
                    }


                    urlConnection = (HttpURLConnection) url
                            .openConnection();

                    InputStream in = urlConnection.getInputStream();

                    InputStreamReader isw = new InputStreamReader(in);

                    int data = isw.read();
                    while (data != -1) {
                        char current = (char) data;
                        data = isw.read();
                        System.out.print(current);
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


            }

        });



        switch3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.v("Switch State=", "" + isChecked);


                URL url;
                HttpURLConnection urlConnection = null;
                try {
                    if (isChecked) {
                        url = new URL("https://argus-adrianodennanni.c9.io/alarm_switch?house_id=3&active=1");
                    } else {
                        url = new URL("https://argus-adrianodennanni.c9.io/alarm_switch?house_id=3&active=0");
                    }


                    urlConnection = (HttpURLConnection) url
                            .openConnection();

                    InputStream in = urlConnection.getInputStream();

                    InputStreamReader isw = new InputStreamReader(in);

                    int data = isw.read();
                    while (data != -1) {
                        char current = (char) data;
                        data = isw.read();
                        System.out.print(current);
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


            }

        });


    }


}
