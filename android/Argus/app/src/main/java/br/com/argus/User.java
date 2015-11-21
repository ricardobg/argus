package br.com.argus;
public class User {
    String username;
    String password;
    int session;
    int houseid;

    public User (String username, String password, int session, int houseid)
    {
        this.username = username;
        this.password = password;
        this.session = session;
        this.houseid = houseid;
    }
}
