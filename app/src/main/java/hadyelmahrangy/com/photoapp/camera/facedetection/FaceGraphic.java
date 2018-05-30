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

package hadyelmahrangy.com.photoapp.camera.facedetection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

import com.google.android.gms.vision.CameraSource;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import hadyelmahrangy.com.photoapp.PhotoApplication;
import hadyelmahrangy.com.photoapp.R;
import hadyelmahrangy.com.photoapp.camera.GraphicOverlay;

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
public class FaceGraphic extends GraphicOverlay.Graphic {
    private static final float FACE_POSITION_RADIUS = 10.0f;
    private static final float ID_TEXT_SIZE = 40.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

    private static final int[] COLOR_CHOICES = {
            Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.RED, Color.WHITE, Color.YELLOW
    };
    private static int currentColorIndex = 0;

    private int facing;

    private final Paint facePositionPaint;
    private final Paint idPaint;
    private final Paint boxPaint;
    private double angle;

    private volatile FirebaseVisionFace firebaseVisionFace;

    private PointF pointLeftZero = new PointF();
    private PointF pointRightZero = new PointF();
    private Vector vectorEye = new Vector();
    private Vector vectorZero = new Vector();

    private Bitmap faceBitmap;

    public FaceGraphic(GraphicOverlay overlay) {
        super(overlay);

        currentColorIndex = (currentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[currentColorIndex];

        facePositionPaint = new Paint();
        facePositionPaint.setColor(selectedColor);

        idPaint = new Paint();
        idPaint.setColor(selectedColor);
        idPaint.setTextSize(ID_TEXT_SIZE);

        boxPaint = new Paint();
        boxPaint.setColor(selectedColor);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(BOX_STROKE_WIDTH);

        faceBitmap = BitmapFactory.decodeResource(PhotoApplication.getContext().getResources(), R.drawable.icon_test1);
    }

    /**
     * Updates the face instance from the detection of the most recent frame. Invalidates the relevant
     * portions of the overlay to trigger a redraw.
     */
    public void updateFace(FirebaseVisionFace face, int facing) {
        firebaseVisionFace = face;
        this.facing = facing;
        postInvalidate();
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        FirebaseVisionFace face = firebaseVisionFace;
        if (face == null) {
            return;
        }

        // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(face.getBoundingBox().centerX());
        float y = translateY(face.getBoundingBox().centerY());
//      canvas.drawCircle(x, y, FACE_POSITION_RADIUS, facePositionPaint);
//        canvas.drawText("id: " + face.getTrackingId(), x + ID_X_OFFSET, y + ID_Y_OFFSET, idPaint);
       /*   canvas.drawText(
                "happiness: " + String.format("%.2f", face.getSmilingProbability()),
                x + ID_X_OFFSET * 3,
                y - ID_Y_OFFSET,
                idPaint);
        if (facing == CameraSource.CAMERA_FACING_FRONT) {
            canvas.drawText(
                    "right eye: " + String.format("%.2f", face.getRightEyeOpenProbability()),
                    x - ID_X_OFFSET,
                    y,
                    idPaint);
            canvas.drawText(
                    "left eye: " + String.format("%.2f", face.getLeftEyeOpenProbability()),
                    x + ID_X_OFFSET * 6,
                    y,
                    idPaint);
        } else {
            canvas.drawText(
                    "left eye: " + String.format("%.2f", face.getLeftEyeOpenProbability()),
                    x - ID_X_OFFSET,
                    y,
                    idPaint);
            canvas.drawText(
                    "right eye: " + String.format("%.2f", face.getRightEyeOpenProbability()),
                    x + ID_X_OFFSET * 6,
                    y,
                    idPaint);
        }
*/
        // Draws a bounding box around the face.
        float xOffset = scaleX(face.getBoundingBox().width() / 2.0f);
        float yOffset = scaleY(face.getBoundingBox().height() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
//        canvas.drawRect(left, top, right, bottom, boxPaint);

//        // draw landmarks
     /*   drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.BOTTOM_MOUTH);
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.LEFT_CHEEK);
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.LEFT_EAR);
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.LEFT_MOUTH);

        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.NOSE_BASE);
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.RIGHT_CHEEK);
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.RIGHT_EAR);

        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.RIGHT_MOUTH);*/
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.LEFT_EYE);
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.RIGHT_EYE);

        FirebaseVisionFaceLandmark landmarkLeft = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE);
        FirebaseVisionFaceLandmark landmarkRight = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE);
        if (landmarkLeft != null && landmarkRight != null) {
            FirebaseVisionPoint pointLeft = landmarkLeft.getPosition();
            FirebaseVisionPoint pointRight = landmarkRight.getPosition();

            float minX = Math.min(pointLeft.getX(), pointRight.getX());
            float maxX = Math.max(pointLeft.getX(), pointRight.getX());
            float minY = Math.min(pointLeft.getY(), pointRight.getY());

            pointLeftZero.set(minX, minY);
            pointRightZero.set(maxX, minY);

            vectorEye.set(pointRight.getX() - pointLeft.getX(),
                    pointRight.getY() - pointLeft.getY());
            vectorZero.set(pointRightZero.x - pointLeftZero.x,
                    pointRightZero.y - pointLeftZero.y);

            float multiplyVector = vectorEye.x * vectorZero.x + vectorEye.y * vectorZero.y;

            double absVectorEye = Math.sqrt(Math.pow(vectorEye.x, 2) + Math.pow(vectorEye.y, 2));
            double absVectorZero = Math.sqrt(Math.pow(vectorZero.x, 2) + Math.pow(vectorZero.y, 2));

            double localAlpha = multiplyVector / (absVectorEye * absVectorZero);

            double angleA = Math.acos(localAlpha);
            double angleValue = Math.toDegrees(angleA);
            if (angle != angleValue) {
                angle = angleValue;
            }
            Log.e("ALPHA ", "alphaCos = " + localAlpha + "alphaACos = " + angleA + "angleValue = " + angleValue);
        }

        Matrix matrix = new Matrix();
        matrix.mapRect(new RectF(left + 50, top - 50, right - 50, bottom + 450));
        matrix.setRotate(0, faceBitmap.getWidth() / 2, faceBitmap.getHeight()/2);
        canvas.drawBitmap(faceBitmap, matrix,null);
//        canvas.drawBitmap(faceBitmap, null, new RectF(left+50, top-50, right-50, bottom+450), null);


    }

    private void drawLandmarkPosition(Canvas canvas, FirebaseVisionFace face, int landmarkID) {
        FirebaseVisionFaceLandmark landmark = face.getLandmark(landmarkID);
        if (landmark != null) {
            FirebaseVisionPoint point = landmark.getPosition();
            canvas.drawCircle(
                    translateX(point.getX()),
                    translateY(point.getY()),
                    10f, idPaint);
        }
    }
}
