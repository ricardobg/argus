package br.com.argus;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.widget.DialogTitle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

public class BaseActivity extends AppCompatActivity {

    @Override
    public void setContentView(int layoutResID) {
        DrawerLayout fullView = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_base, null);
        FrameLayout activityContainer = (FrameLayout) fullView.findViewById(R.id.activity_content);
        getLayoutInflater().inflate(layoutResID, activityContainer, true);
        super.setContentView(fullView);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (useToolbar()) {
            setSupportActionBar(toolbar);
            setTitle(this.getClass().getSimpleName().replace("Activity", ""));
        } else {
            toolbar.setVisibility(View.GONE);
        }
    }

    public void loadAlarmsActivity(MenuItem item){

        startActivity(new Intent(this, AlarmsActivity.class));
        finish();
    }

    public void logout(MenuItem item){
        userLS.clearUserData();
        userLS.setUserLoggedIn(false);
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    protected boolean useToolbar()
    {
        return true;
    }

    User user;
    UserLocalStore userLS;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userLS = new UserLocalStore(this);
        user = userLS.getLoggedInUser();



    }



}
