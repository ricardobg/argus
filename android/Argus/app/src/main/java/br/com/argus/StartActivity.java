package br.com.argus;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class StartActivity extends AppCompatActivity {

    UserLocalStore userLS;
    User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        userLS = new UserLocalStore(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        if(userLS.isLoggedIn())
            startActivity(new Intent(this, AlarmsActivity.class));
        else
            startActivity(new Intent(this, LoginActivity.class));
    }
}
