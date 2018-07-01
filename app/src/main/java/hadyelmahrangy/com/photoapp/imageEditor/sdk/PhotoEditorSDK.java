package hadyelmahrangy.com.photoapp.imageEditor.sdk;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubfilter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import hadyelmahrangy.com.photoapp.R;
import hadyelmahrangy.com.photoapp.camera.MaskPoint;
import hadyelmahrangy.com.photoapp.imageEditor.ImageEditorActivity;
import hadyelmahrangy.com.photoapp.util.CapturePhotoUtils;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;

public class PhotoEditorSDK implements MultiTouchListener.OnMultiTouchListener {

    private Context context;
    private RelativeLayout parentView;
    private ImageView imageView;
    private View deleteView;
    private List<View> addedViews;
    private OnPhotoEditorSDKListener onPhotoEditorSDKListener;

    //filters
    //filters
    private Bitmap originalImage;
    // to backup image with filter applied
    private Bitmap filteredImage;

    // the final image after applying
    // brightness, saturation, contrast
    private Bitmap finalImage;

    // modified image values
    private int brightnessFinal = 0;
    private float saturationFinal = 1.0f;
    private float contrastFinal = 1.0f;

    private PhotoEditorSDK(PhotoEditorSDKBuilder photoEditorSDKBuilder) {
        this.context = photoEditorSDKBuilder.context;
        this.parentView = photoEditorSDKBuilder.parentView;
        this.imageView = photoEditorSDKBuilder.imageView;
        this.deleteView = photoEditorSDKBuilder.deleteView;
        addedViews = new ArrayList<>();
    }

    public void addImage(String hajibName) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View imageRootView = inflater.inflate(R.layout.photo_editor_sdk_image_item_list, null);
        ImageView imageView = imageRootView.findViewById(R.id.photo_editor_sdk_image_iv);
        float density = context.getResources().getDisplayMetrics().density;
        loadImage(Uri.parse("file:///android_asset/" + ImageEditorActivity.ASSETS_HAJIB + "/" + hajibName), imageView, (int) density * 200);

        imageView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
        MultiTouchListener multiTouchListener = new MultiTouchListener(deleteView,
                parentView, this.imageView, onPhotoEditorSDKListener);
        multiTouchListener.setOnMultiTouchListener(this);
        imageRootView.setOnTouchListener(multiTouchListener);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        parentView.addView(imageRootView, params);
        addedViews.add(imageRootView);
        if (onPhotoEditorSDKListener != null)
            onPhotoEditorSDKListener.onAddViewListener(ViewType.IMAGE, addedViews.size());
    }

    public void addImage(String hajibName, @NonNull MaskPoint maskPoint) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View imageRootView = inflater.inflate(R.layout.photo_editor_sdk_image_item_list, null);
        ImageView imageView = imageRootView.findViewById(R.id.photo_editor_sdk_image_iv);
        loadImage(Uri.parse("file:///android_asset/" + ImageEditorActivity.ASSETS_HAJIB + "/" + hajibName), imageView, maskPoint.getX2() - maskPoint.getX1(), maskPoint.getY2() - maskPoint.getY1());

        imageView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
        MultiTouchListener multiTouchListener = new MultiTouchListener(deleteView,
                parentView, this.imageView, onPhotoEditorSDKListener);
        multiTouchListener.setOnMultiTouchListener(this);
        imageRootView.setOnTouchListener(multiTouchListener);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = maskPoint.getX1();
        params.topMargin = maskPoint.getY1();

        parentView.addView(imageRootView, params);
        addedViews.add(imageRootView);
        if (onPhotoEditorSDKListener != null)
            onPhotoEditorSDKListener.onAddViewListener(ViewType.IMAGE, addedViews.size());
    }

    public void addEmoji(String emojiName) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View emojiRootView = inflater.inflate(R.layout.photo_editor_sdk_emoji_item_list, null);

        EmojiconTextView emojiTextView = emojiRootView.findViewById(R.id.emoji);
        emojiTextView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        emojiTextView.setText(emojiName);

        MultiTouchListener multiTouchListener = new MultiTouchListener(deleteView,
                parentView, this.imageView, onPhotoEditorSDKListener);
        multiTouchListener.setOnMultiTouchListener(this);
        emojiRootView.setOnTouchListener(multiTouchListener);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        parentView.addView(emojiRootView, params);
        addedViews.add(emojiRootView);
        if (onPhotoEditorSDKListener != null)
            onPhotoEditorSDKListener.onAddViewListener(ViewType.EMOJI, addedViews.size());

    }


    private void viewUndo(View removedView) {
        if (addedViews.size() > 0) {
            if (addedViews.contains(removedView)) {
                parentView.removeView(removedView);
                addedViews.remove(removedView);
                if (onPhotoEditorSDKListener != null)
                    onPhotoEditorSDKListener.onRemoveViewListener(addedViews.size());
            }
        }
    }

    public String saveImage(String folderName, String imageName) {
        String selectedOutputPath = "";
        if (isSDCARDMounted()) {
            File mediaStorageDir = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), folderName);
            // Create a storage directory if it does not exist
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("PhotoEditorSDK", "Failed to create directory");
                }
            }
            // Create a media file name
            selectedOutputPath = mediaStorageDir.getPath() + File.separator + imageName;
            Log.d("PhotoEditorSDK", "selected camera path " + selectedOutputPath);
            File file = new File(selectedOutputPath);
            try {
                FileOutputStream out = new FileOutputStream(file);
                if (parentView != null) {
                    parentView.setDrawingCacheEnabled(true);
                    parentView.getDrawingCache().compress(Bitmap.CompressFormat.JPEG, 80, out);
                }
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return selectedOutputPath;
    }

    //filters
    public void initFilters() {
        originalImage = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
        finalImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
    }

    public void resetFilters() {
        brightnessFinal = 0;
        saturationFinal = 1.0f;
        contrastFinal = 1.0f;
    }

    public void onFilterSelected(Filter filter) {
        // applying the selected filter
        filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
        // preview filtered image
        imageView.setImageBitmap(filter.processFilter(filteredImage));
        finalImage = filteredImage.copy(Bitmap.Config.ARGB_8888, true);
    }

    public void onBrightnessChanged(int brightness) {
        brightnessFinal = brightness;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new BrightnessSubFilter(brightness));
        imageView.setImageBitmap(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)));
    }

    public void onSaturationChanged(float saturation) {
        saturationFinal = saturation;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new SaturationSubfilter(saturation));
        imageView.setImageBitmap(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)));
    }

    public void onContrastChanged(float contrast) {
        contrastFinal = contrast;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new ContrastSubFilter(contrast));
        imageView.setImageBitmap(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)));
    }

    public void onEditCompleted() {
        // once the editing is done i.e seekbar is drag is completed,
        // apply the values on to filtered image
        final Bitmap bitmap = filteredImage.copy(Bitmap.Config.ARGB_8888, true);

        Filter myFilter = new Filter();
        myFilter.addSubFilter(new BrightnessSubFilter(brightnessFinal));
        myFilter.addSubFilter(new ContrastSubFilter(contrastFinal));
        myFilter.addSubFilter(new SaturationSubfilter(saturationFinal));
        finalImage = myFilter.processFilter(bitmap);
    }

    public void saveImage(Activity activity, @NonNull CapturePhotoUtils.SavePhotoToFileCallback callback) {
        parentView.setDrawingCacheEnabled(true);
        parentView.buildDrawingCache();
        Bitmap image = parentView.getDrawingCache(true).copy(Bitmap.Config.RGB_565, false);
        parentView.setDrawingCacheEnabled(false);
        parentView.destroyDrawingCache();

        CapturePhotoUtils.savePhotoToFile(activity, image, callback);
    }

    public Bitmap getOriginalImage() {
        return originalImage;
    }

    private boolean isSDCARDMounted() {
        String status = Environment.getExternalStorageState();
        return status.equals(Environment.MEDIA_MOUNTED);
    }

    private void loadImage(@NonNull Uri uri, @NonNull ImageView imageView, int size) {
        Glide.with(context)
                .load(uri)
                .apply(new RequestOptions()
                        .dontAnimate()
                        .skipMemoryCache(false)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .override(size, size))
                .into(imageView);
    }

    private void loadImage(@NonNull String url, @NonNull ImageView imageView, int size) {
        Glide.with(context)
                .load(url)
                .apply(new RequestOptions().dontAnimate()
                        .skipMemoryCache(false)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .override(size, size))
                .into(imageView);
    }

    private void loadImage(@NonNull Uri uri, @NonNull ImageView imageView, int width, int heigth) {
        Glide.with(context)
                .load(uri)
                .apply(new RequestOptions()
                        .dontAnimate()
                        .skipMemoryCache(false)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .override(width, heigth))
                .into(imageView);
    }


    public void setOnPhotoEditorSDKListener(OnPhotoEditorSDKListener onPhotoEditorSDKListener) {
        this.onPhotoEditorSDKListener = onPhotoEditorSDKListener;
    }

    @Override
    public void onEditTextClickListener(String text, int colorCode) {
        //no-op
    }

    @Override
    public void onRemoveViewListener(View removedView) {
        viewUndo(removedView);
    }

    public static class PhotoEditorSDKBuilder {

        private Context context;
        private RelativeLayout parentView;
        private ImageView imageView;
        private View deleteView;

        public PhotoEditorSDKBuilder(Context context) {
            this.context = context;
        }

        public PhotoEditorSDKBuilder parentView(RelativeLayout parentView) {
            this.parentView = parentView;
            return this;
        }

        public PhotoEditorSDKBuilder childView(ImageView imageView) {
            this.imageView = imageView;
            return this;
        }

        public PhotoEditorSDKBuilder deleteView(View deleteView) {
            this.deleteView = deleteView;
            return this;
        }

        public PhotoEditorSDK buildPhotoEditorSDK() {
            return new PhotoEditorSDK(this);
        }
    }
}
