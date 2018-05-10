package hadyelmahrangy.com.photoapp.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CapturePhotoUtils {

    public interface SavePhotoToGalleryCallback {
        void onLoadSuccess(String path, Uri uri);

        void onLoadFail(String error);
    }


    public interface SavePhotoToFileCallback {
        void onSaveSuccess(Uri uri);

        void onSaveFail(String error);
    }

    public static void savePhotoToFile(@NonNull final Activity activity, final byte[] data, @NonNull final SavePhotoToFileCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OutputStream os = null;
                try {
                    File file = createImageFileName(activity);
                    os = new FileOutputStream(file);
                    os.write(data);
                    os.close();
                    final Uri uri = Uri.fromFile(file);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSaveSuccess(uri);
                        }
                    });
                } catch (IOException e) {
                    callback.onSaveFail(e.getMessage());
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                            // Ignore
                        }
                    }
                }
            }
        }).start();
    }


    public static void saveImageToGallery(@NonNull final Activity activity, @NonNull final Bitmap bitmap, @NonNull final String packageName, @NonNull final SavePhotoToGalleryCallback callback) {
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


    private static File createImageFileName(@NonNull Activity activity) throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String prepend = "IMAGE_" + timestamp;
        return File.createTempFile(prepend, ".jpg", createImageFolder(activity));
    }

    private static File createImageFolder(@NonNull Activity activity) {
        return activity.getCacheDir();
    }
}