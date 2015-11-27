package br.com.argus;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserLocalStore {
    public static final String SP_Name = "userDetails";
    SharedPreferences userLocalDB;
    public UserLocalStore(Context context) {
        userLocalDB = context.getSharedPreferences(SP_Name, 0);
    }
    public void storeUserData(User user)
    {
        SharedPreferences.Editor spEditor = userLocalDB.edit();
        spEditor.putString("username", user.username);
        spEditor.putString("password", user.password);
        spEditor.putInt("userid", user.userid);
        spEditor.putInt("houseid", user.houseid);
        spEditor.commit();
    }
    public User getLoggedInUser(){
        String username = userLocalDB.getString("username", "");
        String password = userLocalDB.getString("password", "");
        int userid = userLocalDB.getInt("userid", -1);
        int houseid = userLocalDB.getInt("houseid", -1);
        User storedUser = new User(username,password, userid, houseid);
        return storedUser;
    }
    public void setUserLoggedIn(boolean loggedIn) {
        SharedPreferences.Editor spEditor = userLocalDB.edit();
        spEditor.putBoolean("loggedIn", loggedIn);
        spEditor.commit();
    }

    public void clearUserData(){
        SharedPreferences.Editor spEditor = userLocalDB.edit();
        spEditor.clear();
        spEditor.commit();
    }

    public boolean isLoggedIn() {
        boolean loggedIn = userLocalDB.getBoolean("loggedIn", false);
        return loggedIn;
    }
    /*public void storeUserSessionCookies(HttpURLConnection urlConnection){
        Map<String, List<String>> headerFields = urlConnection.getHeaderFields();
        Set<String> headerFieldsSet = headerFields.keySet();
        Set<String> cookiesSet = null;
        Iterator<String> hearerFieldsIter = headerFieldsSet.iterator();
        while (hearerFieldsIter.hasNext()) {
            String headerFieldKey = hearerFieldsIter.next();
            if ("set-cookie".equalsIgnoreCase(headerFieldKey)) {
                List<String> headerFieldValue = headerFields.get(headerFieldKey);
                 cookiesSet = new HashSet<>(headerFieldValue);
            }
        }
        SharedPreferences.Editor spEditor = userLocalDB.edit();
        spEditor.putStringSet("CookiesSet", cookiesSet);
        spEditor.commit();
    }

    public List<String> retrieveUserSessionCookies()
    {
        Set<String> cookiesSet = userLocalDB.getStringSet("CookiesSet", null);
        List<String> cookiesList = new ArrayList<>(cookiesSet);
        return cookiesList;
    }*/
}
