package hadyelmahrangy.com.photoapp.camera;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.AudioManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;

import java.io.IOException;

import butterknife.BindView;
import butterknife.OnClick;
import hadyelmahrangy.com.photoapp.BaseActivity;
import hadyelmahrangy.com.photoapp.R;
import hadyelmahrangy.com.photoapp.gallery.GalleryActivity;
import hadyelmahrangy.com.photoapp.imageEditor.ImageEditorActivity;
import hadyelmahrangy.com.photoapp.util.CapturePhotoUtils;
import hadyelmahrangy.com.photoapp.util.PermissionManager;
import hadyelmahrangy.com.photoapp.util.SharedPrefUtils;

public class CameraActivity extends BaseActivity {

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

    @BindView(R.id.flash_view_back)
    View flashView;

    private CameraSource mCameraSource;

    private int mCameraFacing = CameraSource.CAMERA_FACING_BACK;

    private String mFlashState = Camera.Parameters.FLASH_MODE_OFF;

    private ScaleGestureDetector scaleGestureDetector;
    private CameraScaleListener mCameraScaleListener;

    private CameraFaceHelper.GraphicFaceTracker graphicFaceTracker;

    @Override
    protected void onViewReady() {
        mCameraScaleListener = new CameraScaleListener();
        scaleGestureDetector = new ScaleGestureDetector(this, mCameraScaleListener);
        if (hasCameraPermission()) {
            createCamera();
        }
        showHintDialog();
    }

    private void showHintDialog() {
        if (SharedPrefUtils.isShowAgain(this)) {
            MessageDialog hintDialog = new MessageDialog(CameraActivity.this);
            hintDialog.show();
            if (hintDialog.getWindow() != null) {
                hintDialog
                        .getWindow()
                        .setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
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
        startFlashAnimation();
        MaskPoint maskPoint = new MaskPoint(-1, -1, -1, -1);
        if (graphicFaceTracker != null) {
            FaceGraphic faceGraphic = graphicFaceTracker.getFaceGraphic();
            if (faceGraphic != null) {
                maskPoint = faceGraphic.getMaskPoint();
            }
        }
        final MaskPoint finalMaskPoint = maskPoint;
        muteAudio(true);
        mCameraSource.takePicture(null, new CameraSource.PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data) {
                CapturePhotoUtils.savePhotoToFile(CameraActivity.this, data, new CapturePhotoUtils.SavePhotoToFileCallback() {
                    @Override
                    public void onSaveSuccess(Uri uri) {
                        muteAudio(false);
                        //    ResultActivity.launch(CameraActivity.this, uri);
                        int cameraFacing = mCameraSource.getCameraFacing();
                        if (cameraFacing == CameraSource.CAMERA_FACING_FRONT) {
                            ImageEditorActivity.launch(CameraActivity.this, uri, finalMaskPoint, true);
                        } else {
                            ImageEditorActivity.launch(CameraActivity.this, uri, finalMaskPoint, false);
                        }
                    }

                    @Override
                    public void onSaveFail(String error) {
                        muteAudio(false);
                        showMessage(error);
                    }
                });
            }
        });
    }

    private void muteAudio(boolean isMute) {
        AudioManager mgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (mgr != null)
            mgr.setStreamMute(AudioManager.STREAM_SYSTEM, isMute);
    }

    private void startFlashAnimation() {
        ObjectAnimator anim = ObjectAnimator.ofFloat(flashView, "alpha", 1f);
        anim.setDuration(150);
        anim.setRepeatMode(ValueAnimator.REVERSE);
        anim.setRepeatCount(1);
        anim.start();
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

/*        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());*/

        graphicFaceTracker = new CameraFaceHelper.GraphicFaceTracker(mGraphicOverlay, this);

        detector.setProcessor(
                new LargestFaceFocusingProcessor.Builder(detector,
                        graphicFaceTracker)
                        .build());

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        CameraSource.Builder cameraBuilder = new CameraSource.Builder(context, detector)
                .setFacing(mCameraFacing)
                .setRequestedPreviewSize(metrics.heightPixels, metrics.widthPixels)
                .setRequestedFps(60.0f)
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
                        //ResultActivity.launch(this, uri);
                        ImageEditorActivity.launch(this, uri, true);
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
