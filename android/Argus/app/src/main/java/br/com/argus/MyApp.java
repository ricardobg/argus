package br.com.argus;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;

public class MyApp{
    private static MyApp instance;

    // Global variable
    private CookieManager cookieManager;
    private CookieStore cookieStore;

    // Restrict the constructor from being instantiated
    private MyApp(){
        cookieManager = new CookieManager(cookieStore, CookiePolicy.ACCEPT_ALL );
    }

    public void setCookieStore(CookieStore store){
        this.cookieStore = store;
    }
    public CookieStore getCookieStore(){
        return this.cookieStore;
    }

    public static synchronized MyApp getInstance(){
        if(instance==null){
            instance=new MyApp();
        }
        return instance;
    }
}