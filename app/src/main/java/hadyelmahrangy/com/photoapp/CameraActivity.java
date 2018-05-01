package hadyelmahrangy.com.photoapp;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hadyelmahrangy.com.photoapp.camera.CameraFaceHelper;
import hadyelmahrangy.com.photoapp.camera.CameraSource;
import hadyelmahrangy.com.photoapp.camera.CameraSourcePreview;
import hadyelmahrangy.com.photoapp.camera.GraphicOverlay;
import hadyelmahrangy.com.photoapp.gallery.GalleryActivity;
import hadyelmahrangy.com.photoapp.util.PermissionManager;

public class CameraActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);
        if (hasCameraPermission()) {
            createCamera();
        }
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
        mCameraSource.takePicture(null, new CameraSource.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data) {
                Log.e("", "onPictureTaken: ");
            }
        });
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
                .setRequestedPreviewSize(metrics.heightPixels, metrics.widthPixels)
                .setRequestedFps(30.0f)
                .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
                .setFlashMode(mFlashState);

        mCameraSource = cameraBuilder.build();
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
                        Log.d("", "Loaded photo from gallery");
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
            return new CameraFaceHelper.GraphicFaceTracker(mGraphicOverlay);
        }
    }


}
