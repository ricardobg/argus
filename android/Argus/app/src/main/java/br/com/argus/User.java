package br.com.argus;

import android.util.Log;

import java.net.CookieStore;
import java.net.HttpCookie;

public class User {
    String username;
    String password;
    int userid;
    int houseid;

    public User (String username, String password)
    {
        this.username = username;
        this.password = password;
    }
    public User (String username, String password, int userid, int houseid){
        this.username = username;
        this.password = password;
        this.userid = userid;
        this.houseid = houseid;
    }
    public void setInfo(CookieStore cookieStore){
        for(HttpCookie cookie : cookieStore.getCookies()){
            String cookieName = cookie.toString().substring(0, cookie.toString().indexOf("="));
            String cookieValue = cookie.toString().substring(cookie.toString().indexOf("=") + 1, cookie.toString().length());
            if(cookieName.equals("house_id"))
                houseid = Integer.parseInt(cookieValue);
            else if(cookieName.equals("user_id"))
                userid = Integer.parseInt(cookieValue);
        }
    }
}
