package hadyelmahrangy.com.photoapp.imageEditor;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Point;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Visibility;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.OnClick;
import hadyelmahrangy.com.photoapp.BaseActivity;
import hadyelmahrangy.com.photoapp.R;
import hadyelmahrangy.com.photoapp.imageEditor.adapters.EmojisAdapter;
import hadyelmahrangy.com.photoapp.imageEditor.adapters.HajibAdapter;
import hadyelmahrangy.com.photoapp.result.ResultActivity;

public class ImageEditorActivity extends BaseActivity implements EmojisAdapter.EmojisAdapterListener,
        HajibAdapter.BordersAdapterListener {
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

    @BindView(R.id.iv_filters)
    ImageView ivFilters;

    @BindView(R.id.photo_edit_iv)
    ImageView ivPhotoEdit;

    @BindView(R.id.rec_view_emojis)
    RecyclerView recViewEmojis;

    @BindView(R.id.rec_view_hajib)
    RecyclerView recViewHajib;

    private EmojisAdapter emojisAdapter;
    private HajibAdapter hajibAdapter;

    private Uri photoUri;
    int width;
    int height;

    @Override
    protected void onViewReady() {
        getPhoto();
        getScreenSize();
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

    private void selectTab(int tab) {
        switch (tab) {
            case TAB_EMOJI:
                if (recViewEmojis.getVisibility() == View.GONE) {
                    setLayoutsVisibility(View.VISIBLE, View.GONE, View.GONE);
                    initEmojisAdapter();
                } else {
                    setLayoutsVisibility(View.GONE, View.GONE, View.GONE);
                }
                break;
            case TAB_HAJIB:
                if (recViewHajib.getVisibility() == View.GONE) {
                    setLayoutsVisibility(View.GONE, View.VISIBLE, View.GONE);
                    initHajibAdapter();
                } else {
                    setLayoutsVisibility(View.GONE, View.GONE, View.GONE);
                }
                break;
            //TODO FILTERS
        }
    }

    private void setLayoutsVisibility(int tabEmojiVisibility, int tabHajibVisibility, int tabFiltersVisibility) {
        recViewEmojis.setVisibility(tabEmojiVisibility);
        recViewHajib.setVisibility(tabHajibVisibility);
        //TODO FITERS
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
        setLayoutsVisibility(View.GONE, View.GONE, View.GONE);
        //TODO
    }

    @Override
    public void onClick(@NonNull String borderName) {
        setLayoutsVisibility(View.GONE, View.GONE, View.GONE);
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
