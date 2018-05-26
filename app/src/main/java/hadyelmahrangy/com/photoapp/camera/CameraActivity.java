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
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.media.ExifInterface;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.google.android.gms.common.annotation.KeepName;

import java.io.ByteArrayInputStream;
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
    private boolean isFlash = false;

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
            isFlash = false;
            cameraSource.setFlashCamera(false);
            ivFlash.setChecked(false);
        }
        preview.stop();
        startCameraSource();
    }

    @OnClick(R.id.iv_flash)
    public void swapFlashClick() {
        if (cameraSource != null) {
            boolean wasChanged = cameraSource.setFlashCamera(!isFlash);
            if (wasChanged) {
                isFlash = !isFlash;
                ivFlash.setChecked(isBackMode);
            }
        }
    }

    @OnClick(R.id.iv_create_photo)
    public void makePhotoClick() {
        cameraSource.takePicture(null, new CameraSource.PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                Bitmap face = BitmapFactory.decodeByteArray(data, 0, data.length, options);

                int rotationDegrees = getRotateDegrees(new ByteArrayInputStream(data));

                preview.setDrawingCacheEnabled(true);
                Bitmap overlay = preview.getDrawingCache();

                Bitmap result = mergeBitmaps(face, overlay);
                result = rotate(result, rotationDegrees);


                CapturePhotoUtils.savePhotoToFile(CameraActivity.this, result, new CapturePhotoUtils.SavePhotoToFileCallback() {
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

    private int getRotateDegrees(ByteArrayInputStream byteArrayInputStream) {
        ExifInterface exifInterface = null;
        int rotationDegrees = 0;
        try {
            exifInterface = new ExifInterface(byteArrayInputStream);

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

        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotationDegrees;
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    public static Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        //       mtx.postRotate(degree);
        mtx.setRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }


    public Bitmap mergeBitmaps(Bitmap face, Bitmap overlay) {
        int width = face.getWidth();
        int height = face.getHeight();
        Bitmap newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Rect faceRect = new Rect(0,0,width,height);
        Rect overlayRect = new Rect(0,0,overlay.getWidth(),overlay.getHeight());

        // Draw face and then overlay (Make sure rects are as needed)
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawBitmap(face, faceRect, faceRect, null);
        canvas.drawBitmap(overlay, overlayRect, faceRect, null);
        return newBitmap;
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
