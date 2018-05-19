// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package hadyelmahrangy.com.photoapp.camera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.google.android.gms.common.annotation.KeepName;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.OnClick;
import hadyelmahrangy.com.photoapp.BaseActivity;
import hadyelmahrangy.com.photoapp.R;
import hadyelmahrangy.com.photoapp.gallery.GalleryActivity;
import hadyelmahrangy.com.photoapp.camera.facedetection.FaceDetectionProcessor;
import hadyelmahrangy.com.photoapp.result.ResultActivity;
import hadyelmahrangy.com.photoapp.util.CapturePhotoUtils;
import hadyelmahrangy.com.photoapp.util.PermissionManager;

@KeepName
public final class CameraActivity extends BaseActivity {

    private static final String TAG = CameraActivity.class.getSimpleName();
    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private static final int RC_CAMERA_PERMISSION = 1;

    private static final int REQUEST_LIBRARY_RESULT = 21;

    @BindView(R.id.firePreview)
    CameraSourcePreview preview;

    @BindView(R.id.fireFaceOverlay)
    GraphicOverlay graphicOverlay;

    @BindView(R.id.iv_flash)
    CheckBox ivFlash;

    @BindView(R.id.iv_swap_camera)
    ImageView ivSwapCamera;

    @BindView(R.id.iv_create_photo)
    ImageView ivCreatePhoto;

    @BindView(R.id.iv_open_gallery)
    ImageView ivOpenGallery;

    private CameraSource cameraSource = null;
    private boolean isBackMode = true;

    @Override
    protected void onViewReady() {
        if (hasCameraPermission()) {
            createCameraSource();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        preview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }

    @Override
    protected int onRequestLayout() {
        return R.layout.activity_camera;
    }

    @OnClick(R.id.iv_swap_camera)
    public void swapCameraClick() {
        Log.d(TAG, "Set facing");
        if (cameraSource != null) {
            isBackMode = !isBackMode;
            if (isBackMode) {
                cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
            } else {
                cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
            }
        }
        preview.stop();
        startCameraSource();
    }

    @OnClick(R.id.iv_flash)
    public void swapFlashClick() {
        //TODO
    }

    @OnClick(R.id.iv_create_photo)
    public void makePhotoClick() {
        cameraSource.takePicture(null, new CameraSource.PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data) {
                CapturePhotoUtils.savePhotoToFile(CameraActivity.this, data, new CapturePhotoUtils.SavePhotoToFileCallback() {
                    @Override
                    public void onSaveSuccess(Uri uri) {
                        ResultActivity.launch(CameraActivity.this, uri);
                    }

                    @Override
                    public void onSaveFail(String error) {
                        showMessage(error);
                    }
                });
            }
        });
    }

    @OnClick(R.id.iv_open_gallery)
    public void openGallery() {
        Intent intent = new Intent(this, GalleryActivity.class);
        startActivityForResult(intent, REQUEST_LIBRARY_RESULT);
    }


    private void createCameraSource() {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
        }
        cameraSource.setMachineLearningFrameProcessor(new FaceDetectionProcessor());
    }

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null");
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null");
                }
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    private boolean hasCameraPermission() {
        return PermissionManager.hasPermission(this, PERMISSION_CAMERA, RC_CAMERA_PERMISSION);
    }

    private boolean isPermissionNeverAsk(String permission) {
        return PermissionManager.isNeverAsk(this, permission);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && permissions.length > 0) {
            int result = grantResults[0];
            String permission = permissions[0];

            if (requestCode == RC_CAMERA_PERMISSION) {
                if (result == PackageManager.PERMISSION_GRANTED) {
                    createCameraSource();

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
}
