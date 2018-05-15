package hadyelmahrangy.com.photoapp.result;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.OnClick;
import hadyelmahrangy.com.photoapp.BaseActivity;
import hadyelmahrangy.com.photoapp.R;
import hadyelmahrangy.com.photoapp.adv.AdvActivity;
import hadyelmahrangy.com.photoapp.gallery.GalleryActivity;
import hadyelmahrangy.com.photoapp.util.CapturePhotoUtils;
import hadyelmahrangy.com.photoapp.util.PermissionManager;

public class ResultActivity extends BaseActivity {

    private static final String KEY_IMAGE_URI = "key_image_uri";
    private static final int REQUEST_LIBRARY_RESULT = 31;

    private static final String PERMISSION_STORAGE_WRITE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String PERMISSION_STORAGE_READ = Manifest.permission.READ_EXTERNAL_STORAGE;

    private static final int RC_MESSENGER = 101;
    private static final int RC_INSTAGRAM = 102;
    private static final int RC_FACEBOOK = 103;
    private static final int RC_WHATSAPP = 104;
    private static final int RC_MORE = 105;

    private static final int RC_SAVE_IMAGE = 106;

    public static void launch(@NonNull AppCompatActivity appCompatActivity, @NonNull Uri imageUri) {
        Intent intent = new Intent(appCompatActivity, ResultActivity.class);
        intent.putExtra(KEY_IMAGE_URI, imageUri);
        appCompatActivity.startActivity(intent);
    }

    @BindView(R.id.iv_photo)
    ImageView ivPhoto;

    @BindView(R.id.sharing_container)
    View sharingContainer;

    @BindView(R.id.bottom_container)
    View bottomContainer;

    @BindView(R.id.image_container)
    View imageContainer;

    private Uri photoUri;

    @Override
    protected void onViewReady() {
        getPhoto();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideStatusBar();
    }

    @Override
    protected int onRequestLayout() {
        return R.layout.activity_result;
    }

    @OnClick(R.id.iv_gallery)
    void onGalleryClick() {
        Intent intent = new Intent(this, GalleryActivity.class);
        startActivityForResult(intent, REQUEST_LIBRARY_RESULT);
        onCloseSharing();
    }

    @OnClick(R.id.iv_save)
    void onSaveClick() {
        if (!hasStorageWritePermission(RC_SAVE_IMAGE)) return;
        if (!hasStorageReadPermission(RC_SAVE_IMAGE)) return;

        showProgressDialog(R.string.saving);
        Bitmap image = loadBitmapFromView(imageContainer);
        CapturePhotoUtils.saveImageToGallery(this, image, getResources().getString(R.string.folder_name), new CapturePhotoUtils.SavePhotoToGalleryCallback() {
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

    private void showPhotoSavedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ResultActivity.this);
        builder.setTitle(R.string.photo_saved)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AdvActivity.launch(ResultActivity.this);
                    }
                })
                .setCancelable(false);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button btnPositive = alertDialog.getButton(Dialog.BUTTON_POSITIVE);
                btnPositive.setTextSize(18);
            }
        });
        alertDialog.show();
    }

    @OnClick(R.id.iv_camera)
    void onCameraClick() {
        finish();
    }

    @OnClick(R.id.iv_share)
    void onShareClick() {
        showSharingContainer();
    }

    private void getPhoto() {
        photoUri = getIntent().getParcelableExtra(KEY_IMAGE_URI);
        ivPhoto.setImageURI(photoUri);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LIBRARY_RESULT) {
            if (resultCode == RESULT_OK) {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    photoUri = imageUri;
                    ivPhoto.setImageURI(photoUri);
                } else {
                    showMessage("Fail to load image");
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (sharingContainer.getVisibility() == View.VISIBLE) {
            onCloseSharing();
        } else {
            super.onBackPressed();
        }
    }

    //sharing
    @OnClick(R.id.sharing_container)
    void onSharingContainerClick() {
        //no-op
    }

    @OnClick(R.id.root)
    void onRootClick() {
        if (sharingContainer.getVisibility() == View.VISIBLE) {
            onCloseSharing();
        }
    }

    @OnClick(R.id.messenger)
    void onSnapchatShare() {
        if (!hasStorageReadPermission(RC_MESSENGER)) return;
        if (!hasStorageWritePermission(RC_MESSENGER)) return;

        shareToSocial(getString(R.string.messenger_package));
    }

    @OnClick(R.id.instagram)
    void onInstaShare() {
        if (!hasStorageReadPermission(RC_INSTAGRAM)) return;
        if (!hasStorageWritePermission(RC_INSTAGRAM)) return;

        shareToSocial(getString(R.string.instagram_package));
    }

    @OnClick(R.id.facebook)
    void onFacebookShare() {
        if (!hasStorageReadPermission(RC_FACEBOOK)) return;
        if (!hasStorageWritePermission(RC_FACEBOOK)) return;

        shareToSocial(getString(R.string.facebook_package));
    }

    @OnClick(R.id.whatsapp)
    void onWhatsAppShare() {
        if (!hasStorageReadPermission(RC_WHATSAPP)) return;
        if (!hasStorageWritePermission(RC_WHATSAPP)) return;

        shareToSocial(getString(R.string.whatsapp_package));
    }

    @OnClick(R.id.more)
    void onMoreShare() {
        if (!hasStorageReadPermission(RC_MORE)) return;
        if (!hasStorageWritePermission(RC_MORE)) return;

        shareToSocial();
    }

    @OnClick(R.id.ic_close_sharing)
    void onCloseSharing() {
        sharingContainer.setVisibility(View.GONE);
    }

    private void showSharingContainer() {
        sharingContainer.setVisibility(View.VISIBLE);
    }

    private void shareToSocial(@NonNull String appPackage) {
        Intent intent = getPackageManager().getLaunchIntentForPackage(appPackage);
        if (intent != null) {
            final Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setPackage(appPackage);

            showProgressDialog();
            Bitmap bitmap = loadBitmapFromView(imageContainer);
            CapturePhotoUtils.saveImageToGallery(this, bitmap, getResources().getString(R.string.folder_name), new CapturePhotoUtils.SavePhotoToGalleryCallback() {
                @Override
                public void onLoadSuccess(String path, Uri uri) {
                    shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    shareIntent.setType("image/*");
                    startActivity(shareIntent);
                    hideProgressDialog();
                }

                @Override
                public void onLoadFail(String error) {
                    hideProgressDialog();
                    showMessage("Fail to load image");
                }
            });
        } else {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("market://details?id=" + appPackage));
            startActivity(intent);
        }
    }

    private void shareToSocial() {
        final Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);

        showProgressDialog();
        Bitmap bitmap = loadBitmapFromView(imageContainer);
        CapturePhotoUtils.saveImageToGallery(this, bitmap, getResources().getString(R.string.folder_name), new CapturePhotoUtils.SavePhotoToGalleryCallback() {
            @Override
            public void onLoadSuccess(String path, Uri uri) {
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.setType("image/*");
                startActivity(Intent.createChooser(shareIntent, "Share picture with..."));
                hideProgressDialog();
            }

            @Override
            public void onLoadFail(String error) {
                hideProgressDialog();
                showMessage("Fail to load image");
            }
        });
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
                case RC_MESSENGER:
                    if (result == PackageManager.PERMISSION_GRANTED) {
                        onSnapchatShare();
                    } else {
                        if (isPermissionNeverAsk(permission)) {
                            PermissionManager.showPermissionNeverAskDialog(ResultActivity.this, getPermissionName(permission));
                        }
                    }
                    break;
                case RC_INSTAGRAM:
                    if (result == PackageManager.PERMISSION_GRANTED) {
                        onInstaShare();
                    } else {
                        if (isPermissionNeverAsk(permission)) {
                            PermissionManager.showPermissionNeverAskDialog(ResultActivity.this, getPermissionName(permission));
                        }
                    }
                    break;
                case RC_FACEBOOK:
                    if (result == PackageManager.PERMISSION_GRANTED) {
                        onFacebookShare();
                    } else {
                        if (isPermissionNeverAsk(permission)) {
                            PermissionManager.showPermissionNeverAskDialog(ResultActivity.this, getPermissionName(permission));
                        }
                    }
                    break;
                case RC_WHATSAPP:
                    if (result == PackageManager.PERMISSION_GRANTED) {
                        onWhatsAppShare();
                    } else {
                        if (isPermissionNeverAsk(permission)) {
                            PermissionManager.showPermissionNeverAskDialog(ResultActivity.this, getPermissionName(permission));
                        }
                    }
                    break;
                case RC_MORE:
                    if (result == PackageManager.PERMISSION_GRANTED) {
                        onMoreShare();
                    } else {
                        if (isPermissionNeverAsk(permission)) {
                            PermissionManager.showPermissionNeverAskDialog(ResultActivity.this, getPermissionName(permission));
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

    private Bitmap loadBitmapFromView(View v) {
        Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);
        return b;
    }
}
