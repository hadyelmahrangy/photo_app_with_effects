package hadyelmahrangy.com.photoapp.imageEditor;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.squareup.otto.Subscribe;
import com.zomato.photofilters.imageprocessors.Filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;
import hadyelmahrangy.com.photoapp.BaseActivity;
import hadyelmahrangy.com.photoapp.R;
import hadyelmahrangy.com.photoapp.camera.MaskPoint;
import hadyelmahrangy.com.photoapp.eventBus.AppBus;
import hadyelmahrangy.com.photoapp.eventBus.PhotoFromGalleryEvent;
import hadyelmahrangy.com.photoapp.adv.BaseAdvActivity;
import hadyelmahrangy.com.photoapp.imageEditor.adapters.emoji.EmojisAdapter;
import hadyelmahrangy.com.photoapp.imageEditor.adapters.filters.EditImageFragment;
import hadyelmahrangy.com.photoapp.imageEditor.adapters.filters.FiltersListFragment;
import hadyelmahrangy.com.photoapp.imageEditor.adapters.filters.ViewPagerFiltersAdapter;
import hadyelmahrangy.com.photoapp.imageEditor.adapters.hajib.HajibAdapter;
import hadyelmahrangy.com.photoapp.imageEditor.sdk.OnPhotoEditorSDKListener;
import hadyelmahrangy.com.photoapp.imageEditor.sdk.PhotoEditorSDK;
import hadyelmahrangy.com.photoapp.imageEditor.sdk.ViewType;
import hadyelmahrangy.com.photoapp.result.ResultActivity;
import hadyelmahrangy.com.photoapp.util.CapturePhotoUtils;
import hadyelmahrangy.com.photoapp.util.PermissionManager;

public class ImageEditorActivity extends BaseAdvActivity implements EmojisAdapter.EmojisAdapterListener,
        HajibAdapter.BordersAdapterListener,
        FiltersListFragment.FiltersListFragmentListener,
        EditImageFragment.EditImageFragmentListener,
        OnPhotoEditorSDKListener {

    private static final int RC_SAVE_IMAGE = 106;

    private static final String PERMISSION_STORAGE_WRITE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String PERMISSION_STORAGE_READ = Manifest.permission.READ_EXTERNAL_STORAGE;

    // load native image filters library
    static {
        System.loadLibrary("NativeImageProcessor");
    }

    FiltersListFragment filtersListFragment;
    EditImageFragment editImageFragment;

    private static final String KEY_IMAGE_URI = "key_image_uri";
    private static final String KEY_IS_FROM_GALLERY = "key_is_from_gallery";
    private static final String KEY_MASK_POINT = "key_mask_point";
    private static final String KEY_IS_CAMERA_FRONT = "key_is_camera_front";


    public static final String ASSETS_HAJIB = "hajib";

    private static final int TAB_EMOJI = 0;
    private static final int TAB_HAJIB = 1;
    private static final int TAB_FITERS = 2;

    public static void launch(@NonNull AppCompatActivity appCompatActivity, @NonNull Uri imageUri, boolean isFromGallery) {
        Intent intent = new Intent(appCompatActivity, ImageEditorActivity.class);
        intent.putExtra(KEY_IMAGE_URI, imageUri);
        intent.putExtra(KEY_IS_FROM_GALLERY, isFromGallery);
        appCompatActivity.startActivity(intent);
    }


    public static void launch(@NonNull AppCompatActivity appCompatActivity, @NonNull Uri imageUri, @NonNull MaskPoint maskPoint, boolean isSameraFront) {
        Intent intent = new Intent(appCompatActivity, ImageEditorActivity.class);
        intent.putExtra(KEY_IMAGE_URI, imageUri);
        intent.putExtra(KEY_IS_FROM_GALLERY, false);
        intent.putExtra(KEY_MASK_POINT, maskPoint);
        intent.putExtra(KEY_IS_CAMERA_FRONT, isSameraFront);
        appCompatActivity.startActivity(intent);
    }

    @BindView(R.id.bottom_container)
    View bottomContainer;

    @BindView(R.id.filters_container)
    View filtersContainer;

    @BindView(R.id.photo_edit_iv)
    ImageView ivPhotoEdit;

    @BindView(R.id.parent_image_rl)
    RelativeLayout rlPhotoParent;

    @BindView(R.id.trash_iv)
    ImageView ivDeleteViewFromPhoto;

    @BindView(R.id.iv_next_screen)
    ImageView ivNextScreen;

    @BindView(R.id.rec_view_emojis)
    RecyclerView recViewEmojis;

    @BindView(R.id.rec_view_hajib)
    RecyclerView recViewHajib;

    @BindView(R.id.view_pager)
    ViewPager vpFilters;

    @BindView(R.id.tab_layout)
    TabLayout tbFilters;

    private EmojisAdapter emojisAdapter;
    private HajibAdapter hajibAdapter;
    private ViewPagerFiltersAdapter viewPagerFiltersAdapter;

    private Uri photoUri;
    int width;
    int height;

    private PhotoEditorSDK photoEditorSDK;

    @Override
    protected void onViewReady() {
        super.onViewReady();
        AppBus.getBus().register(this);
        init();
        getScreenSize();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideStatusBar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppBus.getBus().unregister(this);
    }

    @Override
    protected int onRequestLayout() {
        return R.layout.activity_image_editor;
    }

    @OnClick(R.id.iv_next_screen)
    void onNextClick() {
        if (photoEditorSDK != null) {
            showProgressDialog(R.string.processing);
            photoEditorSDK.saveImage(this, new CapturePhotoUtils.SavePhotoToFileCallback() {
                @Override
                public void onSaveSuccess(Uri uri) {
                    hideProgressDialog();
                    ResultActivity.launch(ImageEditorActivity.this, uri);
                }

                @Override
                public void onSaveFail(String error) {
                    hideProgressDialog();
                    showMessage(error);
                }
            });
        } else {
            showMessage("Fail to save photo");
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (recViewEmojis.getVisibility() == View.GONE
                && recViewHajib.getVisibility() == View.GONE
                && filtersContainer.getVisibility() == View.GONE) {
            showSaveImageDialog();
        } else {
            setLayoutsVisibility(View.GONE, View.GONE, View.GONE, View.VISIBLE, View.VISIBLE);
        }
    }

    @OnClick(R.id.iv_emojis)
    void onEmojisClick() {
        selectTab(TAB_EMOJI);
    }

    @OnClick(R.id.iv_hijab)
    void onHajibClick() {
        selectTab(TAB_HAJIB);
    }

    @OnClick(R.id.iv_filters)
    void onFiltersClick() {
        selectTab(TAB_FITERS);
    }

    @OnClick(R.id.tv_close_filters)
    void closeFilters() {
        setLayoutsVisibility(View.GONE, View.GONE, View.GONE, View.VISIBLE, View.VISIBLE);
    }

    @Subscribe
    public void onReceiveImage(@NonNull PhotoFromGalleryEvent photo) {
        init(photo.getImage());
    }

    private void selectTab(int tab) {
        switch (tab) {
            case TAB_EMOJI:
                if (recViewEmojis.getVisibility() == View.GONE) {
                    setLayoutsVisibility(View.VISIBLE, View.GONE, View.GONE, View.VISIBLE, View.GONE);
                    initEmojisAdapter();
                } else {
                    setLayoutsVisibility(View.GONE, View.GONE, View.GONE, View.VISIBLE, View.VISIBLE);
                }
                break;
            case TAB_HAJIB:
                if (recViewHajib.getVisibility() == View.GONE) {
                    setLayoutsVisibility(View.GONE, View.VISIBLE, View.GONE, View.VISIBLE, View.GONE);
                    initHajibAdapter();
                } else {
                    setLayoutsVisibility(View.GONE, View.GONE, View.GONE, View.VISIBLE, View.VISIBLE);
                }
                break;
            case TAB_FITERS:
                if (filtersContainer.getVisibility() == View.GONE) {
                    setLayoutsVisibility(View.GONE, View.GONE, View.VISIBLE, View.GONE, View.GONE);
                    initFiltersAdapter();
                    resetControls();
                    if (filtersListFragment != null) {
                        filtersListFragment.scrollToStart();
                    }
                    if (tbFilters != null) {
                        Objects.requireNonNull(tbFilters.getTabAt(0)).select();
                    }
                } else {
                    setLayoutsVisibility(View.GONE, View.GONE, View.GONE, View.VISIBLE, View.VISIBLE);
                }
        }
    }

    private void setLayoutsVisibility(int tabEmojiVisibility,
                                      int tabHajibVisibility,
                                      int tabFiltersVisibility,
                                      int bottomContainerVisibility,
                                      int nextBtnVisibility) {
        recViewEmojis.setVisibility(tabEmojiVisibility);
        recViewHajib.setVisibility(tabHajibVisibility);
        filtersContainer.setVisibility(tabFiltersVisibility);
        bottomContainer.setVisibility(bottomContainerVisibility);
        ivNextScreen.setVisibility(nextBtnVisibility);
    }

    private void setLayoutsVisibility(int bottomContainerVisibility,
                                      int nextBtnVisibility) {
        bottomContainer.setVisibility(bottomContainerVisibility);
        ivNextScreen.setVisibility(nextBtnVisibility);
    }

    private void init() {
        photoUri = getIntent().getParcelableExtra(KEY_IMAGE_URI);
        boolean isFromGallery = getIntent().getBooleanExtra(KEY_IS_FROM_GALLERY, false);
        if (isFromGallery) {
            ivPhotoEdit.setImageURI(photoUri);
            initSDK();
        } else {
            Glide.with(this)
                    .asBitmap()
                    .load(photoUri)
                    .apply(new RequestOptions()
                            .placeholder(R.color.black)
                            .dontTransform()
                            .diskCacheStrategy(DiskCacheStrategy.NONE))
                    .into(new BitmapImageViewTarget(ivPhotoEdit) {
                        @Override
                        public void onResourceReady(Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            if (!ImageEditorActivity.this.isFinishing() && resource != null) {
                                boolean isCameraFront = getIntent().getBooleanExtra(KEY_IS_CAMERA_FRONT, false);
                                if (isCameraFront) {
                                    ivPhotoEdit.setImageBitmap(flip(resource));
                                } else {
                                    ivPhotoEdit.setImageBitmap(resource);
                                }
                                initSDK();

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        final MaskPoint maskPoint = (MaskPoint) getIntent().getSerializableExtra(KEY_MASK_POINT);
                                        if (maskPoint != null) {
                                            if (!(maskPoint.getX1() == -1 && maskPoint.getX2() == -1 && maskPoint.getY1() == -1 && maskPoint.getY2() == -1)) {
                                                if (photoEditorSDK != null) {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            photoEditorSDK.addImage("hijab-1.png", maskPoint);
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    }
                                }).start();
                            }
                        }
                    });
        }
    }

    public static Bitmap flip(Bitmap src) {
        Matrix m = new Matrix();
        m.preScale(-1, 1);
        Bitmap dst = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, false);
        dst.setDensity(DisplayMetrics.DENSITY_DEFAULT);
        return dst;
    }

    private void init(@NonNull Uri uri) {
        photoUri = uri;
        ivPhotoEdit.setImageURI(uri);
        clearSDK();
        initSDK();
//        Glide.with(this)
//                .asBitmap()
//                .load(photoUri)
//                .apply(new RequestOptions()
//                        .placeholder(R.color.black)
//                        .dontTransform()
//                        .diskCacheStrategy(DiskCacheStrategy.NONE))
//                .into(new BitmapImageViewTarget(ivPhotoEdit) {
//                    @Override
//                    public void onResourceReady(Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
//                        if (!ImageEditorActivity.this.isFinishing() && resource != null) {
//                            ivPhotoEdit.setImageBitmap(resource);
//                            clearSDK();
//                            initSDK();
//                        }
//                    }
//                });
    }

    private void getScreenSize() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;
    }

    private void initEmojisAdapter() {
        if (emojisAdapter == null) {
            emojisAdapter = new EmojisAdapter(this, this, width);
            GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4);
            gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recViewEmojis.setLayoutManager(gridLayoutManager);
            recViewEmojis.setAdapter(emojisAdapter);
        }
    }

    private void initHajibAdapter() {
        if (hajibAdapter == null) {
            hajibAdapter = new HajibAdapter(this, Arrays.asList(getAssetFiles(ASSETS_HAJIB)), this, width);
            GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
            gridLayoutManager.setOrientation(GridLayoutManager.VERTICAL);
            recViewHajib.setLayoutManager(gridLayoutManager);
            recViewHajib.setAdapter(hajibAdapter);
        }
    }

    @Override
    public void onEmojisClick(@NonNull String unicode) {
        setLayoutsVisibility(View.GONE, View.GONE, View.GONE, View.VISIBLE, View.VISIBLE);
        photoEditorSDK.addEmoji(unicode);
    }

    @Override
    public void onClick(@NonNull String borderName) {
        setLayoutsVisibility(View.GONE, View.GONE, View.GONE, View.VISIBLE, View.VISIBLE);
        photoEditorSDK.addImage(borderName);
    }

    private String[] getAssetFiles(String folder) {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list(folder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;
    }

    //SDK
    private void initSDK() {
        photoEditorSDK = new PhotoEditorSDK.PhotoEditorSDKBuilder(ImageEditorActivity.this)
                .parentView(rlPhotoParent)
                .childView(ivPhotoEdit)
                .deleteView(ivDeleteViewFromPhoto)
                .buildPhotoEditorSDK();

        photoEditorSDK.setOnPhotoEditorSDKListener(this);
        photoEditorSDK.initFilters();
    }

    private void clearSDK() {
        photoEditorSDK = null;
    }

    @Override
    public void onEditTextChangeListener(String text, int colorCode) {
        //no-op
    }

    @Override
    public void onAddViewListener(ViewType viewType, int numberOfAddedViews) {
        //no-op
    }

    @Override
    public void onRemoveViewListener(int numberOfAddedViews) {
        //no-op
    }

    @Override
    public void onStartViewChangeListener(ViewType viewType) {
        setLayoutsVisibility(View.GONE, View.GONE);

    }

    @Override
    public void onStopViewChangeListener(ViewType viewType) {
        setLayoutsVisibility(View.VISIBLE, View.VISIBLE);

    }

    //Fiters
    private void initFiltersAdapter() {
        if (viewPagerFiltersAdapter == null) {
            setupViewPager(vpFilters);
            tbFilters.setupWithViewPager(vpFilters);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        viewPagerFiltersAdapter = new ViewPagerFiltersAdapter(getSupportFragmentManager());

        // adding filter list fragment
        filtersListFragment = new FiltersListFragment();
        filtersListFragment.setListener(this);

        // adding edit image fragment
        editImageFragment = new EditImageFragment();
        editImageFragment.setListener(this);

        viewPagerFiltersAdapter.addFragment(filtersListFragment, getString(R.string.tab_filters));
        viewPagerFiltersAdapter.addFragment(editImageFragment, getString(R.string.tab_edit));

        viewPager.setAdapter(viewPagerFiltersAdapter);
    }

    /**
     * Resets image edit controls to normal when new filter
     * is selected
     */
    private void resetControls() {
        if (editImageFragment != null) {
            editImageFragment.resetControls();
        }
        if (photoEditorSDK != null) {
            photoEditorSDK.resetFilters();
        }
    }

    @Override
    public void onFilterSelected(Filter filter) {
        // reset image controls
        resetControls();
        if (photoEditorSDK != null) {
            photoEditorSDK.onFilterSelected(filter);
        }
    }

    @Override
    public void onBrightnessChanged(int brightness) {
        if (photoEditorSDK != null) {
            photoEditorSDK.onBrightnessChanged(brightness);
        }
    }

    @Override
    public void onSaturationChanged(float saturation) {
        if (photoEditorSDK != null) {
            photoEditorSDK.onSaturationChanged(saturation);
        }
    }

    @Override
    public void onContrastChanged(float contrast) {
        if (photoEditorSDK != null) {
            photoEditorSDK.onContrastChanged(contrast);
        }
    }

    @Override
    public void onEditStarted() {
        //no-op
    }

    @Override
    public void onEditCompleted() {
        if (photoEditorSDK != null) {
            photoEditorSDK.onEditCompleted();
        }
    }

    @NonNull
    public Bitmap getOriginalImage() {
        if (photoEditorSDK != null) {
            return photoEditorSDK.getOriginalImage();
        } else {
            return ((BitmapDrawable) ivPhotoEdit.getDrawable()).getBitmap();
        }
    }


    private void showSaveImageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ImageEditorActivity.this);
        builder.setMessage(R.string.save_image_question)
                .setCancelable(true)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!hasStorageWritePermission(RC_SAVE_IMAGE)) return;
                        if (!hasStorageReadPermission(RC_SAVE_IMAGE)) return;

                        showProgressDialog(R.string.saving);
                        Bitmap image = loadBitmapFromView(rlPhotoParent);
                        CapturePhotoUtils.saveImageToGallery(ImageEditorActivity.this, image, getResources().getString(R.string.folder_name), new CapturePhotoUtils.SavePhotoToGalleryCallback() {
                            @Override
                            public void onLoadSuccess(String path, Uri uri) {
                                hideProgressDialog();
                                showPhotoSavedDialog();
                            }

                            @Override
                            public void onLoadFail(String error) {
                                hideProgressDialog();
                                showMessage(R.string.saving_fail);
                            }
                        });
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    //permissions
    private boolean hasStorageWritePermission(int requestCode) {
        return PermissionManager.hasPermission(this, PERMISSION_STORAGE_WRITE, requestCode);
    }

    private boolean hasStorageReadPermission(int requestCode) {
        return PermissionManager.hasPermission(this, PERMISSION_STORAGE_READ, requestCode);
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

            switch (requestCode) {
                case RC_SAVE_IMAGE:
                    if (result == PackageManager.PERMISSION_GRANTED) {
                        showSaveImageDialog();
                    } else {
                        if (isPermissionNeverAsk(permission)) {
                            PermissionManager.showPermissionNeverAskDialog(ImageEditorActivity.this, getPermissionName(permission));
                        }
                    }
                    break;
            }
        }
    }

    @NonNull
    private String getPermissionName(String permission) {
        if (permission.equals(PERMISSION_STORAGE_READ)) return "Storage read";
        if (permission.equals(PERMISSION_STORAGE_WRITE)) return "Storage write";
        return "Storage";
    }
}
