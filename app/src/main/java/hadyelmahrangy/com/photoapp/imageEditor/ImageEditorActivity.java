package hadyelmahrangy.com.photoapp.imageEditor;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ahmedadeltito.photoeditorsdk.PhotoEditorSDK;

import java.io.IOException;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.OnClick;
import hadyelmahrangy.com.photoapp.BaseActivity;
import hadyelmahrangy.com.photoapp.R;
import hadyelmahrangy.com.photoapp.imageEditor.adapters.emoji.EmojisAdapter;
import hadyelmahrangy.com.photoapp.imageEditor.adapters.filters.ImageFilterClickListener;
import hadyelmahrangy.com.photoapp.imageEditor.adapters.filters.ImageFiltersAdapter;
import hadyelmahrangy.com.photoapp.imageEditor.adapters.hajib.HajibAdapter;
import hadyelmahrangy.com.photoapp.result.ResultActivity;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

public class ImageEditorActivity extends BaseActivity implements EmojisAdapter.EmojisAdapterListener,
        HajibAdapter.BordersAdapterListener,
        ImageFilterClickListener {
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

    @BindView(R.id.rec_view_filters)
    RecyclerView recViewFilters;

    private EmojisAdapter emojisAdapter;
    private HajibAdapter hajibAdapter;
    private ImageFiltersAdapter filtersAdapter;

    private Uri photoUri;
    int width;
    int height;

    private PhotoEditorSDK photoEditorSDK;

    @Override
    protected void onViewReady() {
        getPhoto();
        getScreenSize();

        photoEditorSDK = new PhotoEditorSDK.PhotoEditorSDKBuilder(ImageEditorActivity.this)
                .parentView(rlPhotoParent)
//add parent image view
                .childView(ivPhotoEdit)
//add the desired image view
                .deleteView(ivDeleteViewFromPhoto)
// add the brush drawing view that is responsible for drawing on the image view
                .buildPhotoEditorSDK();
// build photo editor sdk
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
                    setLayoutsVisibility(View.VISIBLE, View.GONE, View.GONE, View.VISIBLE, View.VISIBLE);
                    initEmojisAdapter();
                } else {
                    setLayoutsVisibility(View.GONE, View.GONE, View.GONE, View.VISIBLE, View.VISIBLE);
                }
                break;
            case TAB_HAJIB:
                if (recViewHajib.getVisibility() == View.GONE) {
                    setLayoutsVisibility(View.GONE, View.VISIBLE, View.GONE, View.VISIBLE, View.VISIBLE);
                    initHajibAdapter();
                } else {
                    setLayoutsVisibility(View.GONE, View.GONE, View.GONE, View.VISIBLE, View.VISIBLE);
                }
                break;
            case TAB_FITERS:
                if (filtersContainer.getVisibility() == View.GONE) {
                    setLayoutsVisibility(View.GONE, View.GONE, View.VISIBLE, View.GONE, View.GONE);
                    initFiltersAdapter();
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

    private void initFiltersAdapter() {
        if (filtersAdapter == null) {
            recViewFilters.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            filtersAdapter = new ImageFiltersAdapter(this, this);
            recViewFilters.setAdapter(filtersAdapter);
        }
    }

    @Override
    public void onEmojisClick(@NonNull String unicode) {
        setLayoutsVisibility(View.GONE, View.GONE, View.GONE, View.VISIBLE, View.VISIBLE);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_camera);
        photoEditorSDK.addImage( bitmap);
    }

    @Override
    public void onClick(@NonNull String borderName) {
        setLayoutsVisibility(View.GONE, View.GONE, View.GONE, View.VISIBLE, View.VISIBLE);
        //TODO
    }

    @Override
    public void onFilterClick(GPUImageFilter filter) {
        setLayoutsVisibility(View.GONE, View.GONE, View.GONE, View.VISIBLE, View.VISIBLE);
        //TODO
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

}