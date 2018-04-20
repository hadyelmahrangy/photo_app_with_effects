package hadyelmahrangy.com.photoapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.google.android.cameraview.CameraView;

import butterknife.BindView;
import butterknife.ButterKnife;
import hadyelmahrangy.com.photoapp.util.PermissionManager;

public class CameraActivity extends AppCompatActivity {

    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private static final int RC_CAMERA_PERMISSION = 1;

    @BindView(R.id.iv_flash)
    ImageView ivFlash;

    @BindView(R.id.iv_swap_camera)
    ImageView ivSwapCamera;

    @BindView(R.id.iv_create_photo)
    ImageView ivCreatePhoto;

    @BindView(R.id.iv_open_gallery)
    ImageView ivOpenGallery;

    @BindView(R.id.camera)
    CameraView camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);
        initCamera();
        startCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideStatusBar();
        //TODO IF HAVENT PERMISSION
    }

    @Override
    protected void onPause() {
        super.onPause();
        finishCamera();
    }

    private void initCamera() {
        if (camera != null) {
            camera.addCallback(cameraCallback);
            camera.setFlash(CameraView.FLASH_OFF);
        }
    }

    private void startCamera() {
        if (camera != null) {
            if (!hasCameraPermission()) return;
            camera.start();
        }
    }

    private void finishCamera() {
        if (camera != null) {
            camera.stop();
        }
    }

    private void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    CameraView.Callback cameraCallback = new CameraView.Callback() {
        @Override
        public void onCameraOpened(CameraView cameraView) {
            super.onCameraOpened(cameraView);
            //TODO
        }

        @Override
        public void onCameraClosed(CameraView cameraView) {
            super.onCameraClosed(cameraView);
            //TODO
        }

        @Override
        public void onPictureTaken(CameraView cameraView, byte[] data) {
            super.onPictureTaken(cameraView, data);
            //TODO
        }
    };

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
                    startCamera();
                } else {
                    if (isPermissionNeverAsk(permission)) {
                        PermissionManager.showPermissionNeverAskDialog(CameraActivity.this, getPermissionName(requestCode));
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
}
