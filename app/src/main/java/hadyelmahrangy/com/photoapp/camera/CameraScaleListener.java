package hadyelmahrangy.com.photoapp.camera;

import android.view.ScaleGestureDetector;

public class CameraScaleListener implements ScaleGestureDetector.OnScaleGestureListener {

    private CameraSource mCameraSource;

    public void setCameraSource(CameraSource cameraSource) {
        mCameraSource = cameraSource;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        if (mCameraSource != null)
            mCameraSource.doZoom(detector.getScaleFactor());
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
    }
}