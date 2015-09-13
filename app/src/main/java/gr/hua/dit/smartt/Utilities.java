package gr.hua.dit.smartt;

import com.google.android.gms.maps.model.TileOverlay;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

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


}
