package br.com.argus;

import android.content.Context;
import android.content.SharedPreferences;
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
        spEditor.putInt("session", user.session);
        spEditor.putInt("houseid", user.houseid);
        spEditor.commit();
    }
    public User getLoggedInUser(){
        String username = userLocalDB.getString("username", "");
        String password = userLocalDB.getString("password", "");
        int session = userLocalDB.getInt("session", -1);
        int houseid = userLocalDB.getInt("houseid", -1);
        User storedUser = new User(username,password,session,houseid);
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
}
