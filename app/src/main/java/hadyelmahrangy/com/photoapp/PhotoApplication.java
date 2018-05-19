package hadyelmahrangy.com.photoapp;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

public class PhotoApplication extends MultiDexApplication {

    public static Context applicationContext;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = this;
    }

    public static Context getContext() {
        return applicationContext;
    }

}
