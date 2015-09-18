package gr.hua.dit.smartt;

import com.google.android.gms.maps.model.TileOverlay;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Patterns;


import java.util.regex.Pattern;

public class Utilities {

    private static Context mContext;

    public Utilities(Context context)
    {
        mContext= context;
    }

    public static SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());
    }

    public static void savePreferences(String key, String value) {
        Editor editor = getSharedPreferences().edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String loadStoredValue(String strStoredValue, String strDefaultValue) {
        return getSharedPreferences().getString(strStoredValue, strDefaultValue);
    }

    public static void clearPreferences() {
        Editor editor = getSharedPreferences().edit();
        editor.clear();
        editor.commit();
    }

    public String getMacAddress() {
        WifiManager wimanager = (WifiManager) mContext.getApplicationContext().getSystemService(mContext.getApplicationContext().WIFI_SERVICE);
        String macAddress = wimanager.getConnectionInfo().getMacAddress();

        if (macAddress==null) {//@todo uncomment this -> ||macAddress.equals("")
            Utilities ut = new Utilities(mContext);
            macAddress = ut.loadStoredValue("mac_address_saved", "");
        }

        return macAddress;
    }

    public String getApplicationEmailAddress() {
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(mContext.getApplicationContext()).getAccounts();
        String possibleEmail="";
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                possibleEmail = account.name;
            }
        }
        return possibleEmail;
    }

    //   returns Email Address
    public String getEmailAddress() {
        Utilities ut = new Utilities(mContext);
        return ut.loadStoredValue("app_email_address", "none");
    }
}
