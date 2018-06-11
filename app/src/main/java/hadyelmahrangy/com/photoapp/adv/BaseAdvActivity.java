package hadyelmahrangy.com.photoapp.adv;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.Button;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import hadyelmahrangy.com.photoapp.BaseActivity;
import hadyelmahrangy.com.photoapp.R;
import hadyelmahrangy.com.photoapp.imageEditor.ImageEditorActivity;

public abstract class BaseAdvActivity extends BaseActivity {

    private static final String TAG = BaseAdvActivity.class.getSimpleName();

    protected InterstitialAd mInterstitialAd;

    private boolean isShown;

    private boolean needToShow;

    @Override
    protected void onViewReady() {
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.ads_full_screen));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                if (!isShown && needToShow) {
                    mInterstitialAd.show();
                    needToShow = false;
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                if (needToShow)
                    finish();
            }

            @Override
            public void onAdOpened() {
                isShown = true;
                needToShow = false;
            }

            @Override
            public void onAdLeftApplication() {
            }

            @Override
            public void onAdClosed() {
                finish();
            }
        });
    }

    protected void showPhotoSavedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.photo_saved)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        openAds();
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

    private void openAds() {
        needToShow = true;
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else if (this instanceof ImageEditorActivity) {
            finish();
        }
    }
}
