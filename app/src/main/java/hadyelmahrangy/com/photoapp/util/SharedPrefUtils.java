package hadyelmahrangy.com.photoapp.util;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class SharedPrefUtils {

    private static String KEY_SHOW_MESSAGE_AGAIN = "show_message_again";

    public static void setShowAgain(Context context, boolean showAgain) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("", MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(KEY_SHOW_MESSAGE_AGAIN, showAgain).apply();
    }

    public static boolean isShowAgain(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("", MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_SHOW_MESSAGE_AGAIN,true);
    }
}
