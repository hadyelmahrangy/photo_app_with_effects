package hadyelmahrangy.com.photoapp;

import android.support.multidex.MultiDexApplication;

import com.google.android.gms.ads.MobileAds;

public class PhotoApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        MobileAds.initialize(this, getString(R.string.mobile_ads_key));
    }
}
