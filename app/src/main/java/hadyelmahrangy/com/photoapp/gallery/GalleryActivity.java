package hadyelmahrangy.com.photoapp.gallery;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hadyelmahrangy.com.photoapp.R;
import hadyelmahrangy.com.photoapp.util.PermissionManager;

public class GalleryActivity extends AppCompatActivity {

    private static final String PERMISSION_STORAGE_READ = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final int RC_STORAGE_READ = 1;

    @BindView(R.id.rec_view)
    RecyclerView recyclerView;

    private GalleryAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        ButterKnife.bind(this);
        initAdapter();

        if (hasStoragePermission()) {
            adapter.setImages(getAllShownImagesPath());
        }
    }

    @OnClick(R.id.iv_back)
    void onBack() {
        onBackPressed();
    }

    private void initAdapter() {
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new GalleryAdapter(new GalleryAdapter.GalleryAdapterCallback() {
            @Override
            public void onImageClick(String imageUrl) {
                Uri uri = Uri.parse(imageUrl);
                Intent intent = new Intent();
                intent.setData(uri);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private ArrayList<String> getAllShownImagesPath() {
        Uri uri;
        Cursor cursor;
        int column_index_data;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        String absolutePathOfImage = null;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        cursor = this.getContentResolver().query(uri, projection, null,
                null, null);

        if (cursor != null) {
            column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            while (cursor.moveToNext()) {
                absolutePathOfImage = cursor.getString(column_index_data);

                listOfAllImages.add(absolutePathOfImage);
            }
        }
        Collections.reverse(listOfAllImages);
        return listOfAllImages;
    }

    private boolean hasStoragePermission() {
        return PermissionManager.hasPermission(this, PERMISSION_STORAGE_READ, RC_STORAGE_READ);
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

            if (requestCode == RC_STORAGE_READ) {
                if (result == PackageManager.PERMISSION_GRANTED) {
                    adapter.setImages(getAllShownImagesPath());
                } else {
                    if (isPermissionNeverAsk(permission)) {
                        PermissionManager.showPermissionNeverAskDialog(GalleryActivity.this, getPermissionName(requestCode));
                    }
                }
            }

        }
    }

    @NonNull
    private String getPermissionName(int requestCode) {
        switch (requestCode) {
            case RC_STORAGE_READ:
                return "Storage";
        }
        return "";
    }
}
