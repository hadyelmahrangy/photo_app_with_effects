package hadyelmahrangy.com.photoapp.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import hadyelmahrangy.com.photoapp.R;

public class PermissionManager {

    public static boolean hasPermission(@NonNull AppCompatActivity activity, @NonNull String permission, int requestCode) {
        if (!hasPermissionInternal(activity, permission)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermission(activity, permission, requestCode);
            }
            return false;
        }
        return true;
    }

    public static boolean isNeverAsk(Activity activity, String... permissions) {
        return !ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[0]);
    }

    private static boolean hasPermissionInternal(@NonNull Context context, @NonNull String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static void requestPermission(@NonNull AppCompatActivity appCompatActivity, @NonNull String permission, int requestCode) {
        appCompatActivity.requestPermissions(new String[]{permission}, requestCode);
    }

    public static void showPermissionNeverAskDialog(@NonNull final Context context, String permission) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.no_permissions)
                .setMessage(context.getString(R.string.enable_permission, permission))
                .setPositiveButton(R.string.open_settings, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startAppSettingsActivity(context);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private static void startAppSettingsActivity(@NonNull Context context) {
        final Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        appSettingsIntent.addCategory(Intent.CATEGORY_DEFAULT);
        appSettingsIntent.setData(Uri.parse("package:" + context.getPackageName()));
        appSettingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        appSettingsIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        appSettingsIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(appSettingsIntent);
    }
}
