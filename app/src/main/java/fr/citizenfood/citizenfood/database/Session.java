package fr.citizenfood.citizenfood.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by eliam on 14/03/2018.
 */

public class Session {

    private SharedPreferences prefs;

    public Session(Context cntx) {
        // TODO Auto-generated constructor stub
        prefs = PreferenceManager.getDefaultSharedPreferences(cntx);
    }

    public void setUidItem(String name)
    {
        prefs.edit().putString("item"+getUserLogin(), name).commit();
    }

    public String getUidItem()
    {
        String uidItem = prefs.getString("item"+getUserLogin(), "");
        return uidItem;
    }
    public void setVoteState( boolean  state)
    {
        prefs.edit().putBoolean("vote", state).commit();
    }

    public boolean getVoteState( )
    {
        boolean state = prefs.getBoolean("vote", false);
        return state;
    }
    public void setUserLogin(String login) {
        prefs.edit().putString("login", login).commit();
    }

    public String getUserLogin() {
        String userLogin = prefs.getString("login","");
        return userLogin;
    }
}
