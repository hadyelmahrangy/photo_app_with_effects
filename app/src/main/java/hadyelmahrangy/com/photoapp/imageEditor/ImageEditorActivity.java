package hadyelmahrangy.com.photoapp.imageEditor;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.zomato.photofilters.imageprocessors.Filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;
import hadyelmahrangy.com.photoapp.BaseActivity;
import hadyelmahrangy.com.photoapp.R;
import hadyelmahrangy.com.photoapp.imageEditor.adapters.emoji.EmojisAdapter;
import hadyelmahrangy.com.photoapp.imageEditor.adapters.filters.EditImageFragment;
import hadyelmahrangy.com.photoapp.imageEditor.adapters.filters.FiltersListFragment;
import hadyelmahrangy.com.photoapp.imageEditor.adapters.filters.ViewPagerFiltersAdapter;
import hadyelmahrangy.com.photoapp.imageEditor.adapters.hajib.HajibAdapter;
import hadyelmahrangy.com.photoapp.imageEditor.sdk.OnPhotoEditorSDKListener;
import hadyelmahrangy.com.photoapp.imageEditor.sdk.PhotoEditorSDK;
import hadyelmahrangy.com.photoapp.imageEditor.sdk.ViewType;
import hadyelmahrangy.com.photoapp.result.ResultActivity;

public class ImageEditorActivity extends BaseActivity implements EmojisAdapter.EmojisAdapterListener,
        HajibAdapter.BordersAdapterListener,
        FiltersListFragment.FiltersListFragmentListener,
        EditImageFragment.EditImageFragmentListener,
        OnPhotoEditorSDKListener {

    // load native image filters library
    static {
        System.loadLibrary("NativeImageProcessor");
    }

    FiltersListFragment filtersListFragment;
    EditImageFragment editImageFragment;

    private static final String KEY_IMAGE_URI = "key_image_uri";
    public static final String ASSETS_HAJIB = "hajib";

    private static final int TAB_EMOJI = 0;
    private static final int TAB_HAJIB = 1;
    private static final int TAB_FITERS = 2;

    public static void launch(@NonNull AppCompatActivity appCompatActivity, @NonNull Uri imageUri) {
        Intent intent = new Intent(appCompatActivity, ImageEditorActivity.class);
        intent.putExtra(KEY_IMAGE_URI, imageUri);
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
        getPhoto();
        getScreenSize();
        initSDK();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideStatusBar();
    }

    @Override
    protected int onRequestLayout() {
        return R.layout.activity_image_editor;
    }

    @OnClick(R.id.iv_next_screen)
    void onNextClick() {
        //TODO SAVE CHANGES
        ResultActivity.launch(this, photoUri);
        finish();
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

    private void getPhoto() {
        photoUri = getIntent().getParcelableExtra(KEY_IMAGE_URI);
        ivPhotoEdit.setImageURI(photoUri);
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
}
