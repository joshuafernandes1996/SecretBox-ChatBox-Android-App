package secretbox.alisha.joshua.secretbox;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;
    // shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "welcome";
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";

    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    public void setMainFirst(boolean isFirstTime){
        editor.putBoolean("firstTime", isFirstTime);
        editor.commit();
    }

    public boolean isMainFirst(){
        return pref.getBoolean("firstTime", true);
    }

    public void setBluetoothFirst(boolean isFirstTime){
        editor.putBoolean("firstBT", isFirstTime);
        editor.commit();
    }

    public boolean isBluetoothFirst(){
        return pref.getBoolean("firstBT", true);
    }

    public void setChatFirst(boolean isFirstTime){
        editor.putBoolean("firstChat", isFirstTime);
        editor.commit();
    }

    public boolean isChatFirst(){
        return pref.getBoolean("firstChat", true);
    }


}
