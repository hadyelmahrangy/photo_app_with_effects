package hadyelmahrangy.com.photoapp.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;

public class CapturePhotoUtils {

    public interface ImageLoaderCallback {
        void onLoadSuccess(String path, Uri uri);

        void onLoadFail(String error);
    }

    public static void saveImageToGallery(@NonNull final Activity activity, @NonNull final Bitmap bitmap, @NonNull final String packageName, @NonNull final ImageLoaderCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                File myDir = new File(root + "/" + packageName);
                myDir.mkdirs();
                String fname = "image-" + System.currentTimeMillis() + ".jpg";
                File file = new File(myDir, fname);
                if (file.exists())
                    file.delete();
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onLoadFail(e.getMessage());
                }

                // Tell the media scanner about the new file so that it is
                // immediately available to the user.
                MediaScannerConnection.scanFile(activity, new String[]{file.toString()}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(final String path, final Uri uri) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        callback.onLoadSuccess(path, uri);
                                    }
                                });
                            }
                        });
            }
        }).start();
    }
}