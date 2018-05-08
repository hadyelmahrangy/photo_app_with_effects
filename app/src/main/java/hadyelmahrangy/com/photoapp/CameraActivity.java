package hadyelmahrangy.com.photoapp;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.media.ExifInterface;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hadyelmahrangy.com.photoapp.camera.CameraFaceHelper;
import hadyelmahrangy.com.photoapp.camera.CameraScaleListener;
import hadyelmahrangy.com.photoapp.camera.CameraSource;
import hadyelmahrangy.com.photoapp.camera.CameraSourcePreview;
import hadyelmahrangy.com.photoapp.camera.GraphicOverlay;
import hadyelmahrangy.com.photoapp.gallery.GalleryActivity;
import hadyelmahrangy.com.photoapp.result.ResultActivity;
import hadyelmahrangy.com.photoapp.util.CapturePhotoUtils;
import hadyelmahrangy.com.photoapp.util.PermissionManager;

public class CameraActivity extends BaseActivity {

    private static final String TAG = CameraActivity.class.getSimpleName();

    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private static final int RC_CAMERA_PERMISSION = 1;

    private static final int REQUEST_LIBRARY_RESULT = 21;

    @BindView(R.id.iv_flash)
    CheckBox ivFlash;

    @BindView(R.id.iv_swap_camera)
    ImageView ivSwapCamera;

    @BindView(R.id.iv_create_photo)
    ImageView ivCreatePhoto;

    @BindView(R.id.iv_open_gallery)
    ImageView ivOpenGallery;

    @BindView(R.id.preview)
    CameraSourcePreview mPreview;

    @BindView(R.id.faceOverlay)
    GraphicOverlay mGraphicOverlay;

    private CameraSource mCameraSource;

    private int mCameraFacing = CameraSource.CAMERA_FACING_FRONT;

    private String mFlashState = Camera.Parameters.FLASH_MODE_OFF;

    private ScaleGestureDetector scaleGestureDetector;
    private CameraScaleListener mCameraScaleListener;

    private Handler mBackgroundHandler;

    @Override
    protected void onViewReady() {
        mCameraScaleListener = new CameraScaleListener();
        scaleGestureDetector = new ScaleGestureDetector(this, mCameraScaleListener);
        if (hasCameraPermission()) {
            createCamera();
        }
    }

    @Override
    protected int onRequestLayout() {
        return R.layout.activity_camera;
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideStatusBar();
        startCameraSource();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finishCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCameraSource();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        boolean b = scaleGestureDetector.onTouchEvent(e);
        return b || super.onTouchEvent(e);
    }

    @OnClick(R.id.iv_swap_camera)
    public void swapCameraClick() {
        finishCamera();
        swapCamera();
        startCameraSource();
    }

    @OnClick(R.id.iv_flash)
    public void swapFlashClick() {
        swapFlash();
    }

    @OnClick(R.id.iv_create_photo)
    public void makePhotoClick() {
        showProgressDialog();
        mCameraSource.takePicture(null, new CameraSource.PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                int rotationDegrees = getRotate(data);
                CapturePhotoUtils.saveImageToGallery(CameraActivity.this, rotateBitmap(bitmap, rotationDegrees), "hitjabi", new CapturePhotoUtils.ImageLoaderCallback() {
                    @Override
                    public void onLoadSuccess(String path, Uri uri) {
                        hideProgressDialog();
                        ResultActivity.launch(CameraActivity.this, uri);
                    }

                    @Override
                    public void onLoadFail(String error) {
                        hideProgressDialog();
                        showMessage(error);
                    }
                });
            }
        });
    }

    private Bitmap rotateBitmap(Bitmap source, int angle) {
        if (angle == 0) {
            return source;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private int getRotate(byte[] data) {
        int rotationDegrees = 0;
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(new ByteArrayInputStream(data));
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotationDegrees = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotationDegrees = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotationDegrees = 270;
                    break;
            }
            return rotationDegrees;
        } catch (IOException e) {
            e.printStackTrace();
            return rotationDegrees;
        }
    }

    @OnClick(R.id.iv_open_gallery)
    public void openGallery() {
        Intent intent = new Intent(this, GalleryActivity.class);
        startActivityForResult(intent, REQUEST_LIBRARY_RESULT);
    }

    private void swapCamera() {
        mCameraFacing = mCameraSource.getCameraFacing() == CameraSource.CAMERA_FACING_FRONT ?
                CameraSource.CAMERA_FACING_BACK :
                CameraSource.CAMERA_FACING_FRONT;
        mFlashState = Camera.Parameters.FLASH_MODE_OFF;
        ivFlash.setChecked(false);
        createCamera();
    }

    private void swapFlash() {
        if (mCameraSource.getFlashMode() != null) {
            boolean isOn = mCameraSource.getFlashMode().equals(Camera.Parameters.FLASH_MODE_ON);
            mFlashState = isOn
                    ? Camera.Parameters.FLASH_MODE_OFF
                    : Camera.Parameters.FLASH_MODE_ON;

            if (mCameraFacing == CameraSource.CAMERA_FACING_BACK) {
                mCameraSource.setFlashMode(mFlashState);
            }
            ivFlash.setChecked(!isOn);
        }
    }

    private void createCamera() {
        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setMode(FaceDetector.ACCURATE_MODE)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        CameraSource.Builder cameraBuilder = new CameraSource.Builder(context, detector)
                .setFacing(mCameraFacing)
//                .setRequestedPreviewSize(metrics.heightPixels, metrics.widthPixels)
                .setRequestedFps(30.0f)
                .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
                .setFlashMode(mFlashState);

        mCameraSource = cameraBuilder.build();
        mCameraScaleListener.setCameraSource(mCameraSource);
    }

    private void startCameraSource() {
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_CAMERA_PERMISSION);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);

            } catch (IOException e) {
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    private void finishCamera() {
        mPreview.stop();
    }

    private void releaseCameraSource() {
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    private void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    private boolean hasCameraPermission() {
        return PermissionManager.hasPermission(this, PERMISSION_CAMERA, RC_CAMERA_PERMISSION);
    }

    private boolean isPermissionNeverAsk(String permission) {
        return PermissionManager.isNeverAsk(this, permission);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && permissions.length > 0) {
            int result = grantResults[0];
            String permission = permissions[0];

            if (requestCode == RC_CAMERA_PERMISSION) {
                if (result == PackageManager.PERMISSION_GRANTED) {
                    createCamera();

                } else {
                    if (isPermissionNeverAsk(permission)) {
                        PermissionManager.showPermissionNeverAskDialog(CameraActivity.this, getPermissionName(requestCode));
                    }
                }
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_LIBRARY_RESULT) {
                if (intent != null && intent.getData() != null) {
                    Uri uri = intent.getData();
                    if (uri != null) {
                        ResultActivity.launch(this, uri);
                    }
                }
            }
        }
    }

    @NonNull
    private String getPermissionName(int requestCode) {
        switch (requestCode) {
            case RC_CAMERA_PERMISSION:
                return "Camera";
        }
        return "";
    }

    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new CameraFaceHelper.GraphicFaceTracker(mGraphicOverlay, CameraActivity.this);
        }
    }

    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }

    private File createImageFileName() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String prepend = "IMAGE_" + timestamp;
        return File.createTempFile(prepend, ".jpg", createImageFolder());
    }

    private File createImageFolder() {
        return CameraActivity.this.getCacheDir();
    }
}
