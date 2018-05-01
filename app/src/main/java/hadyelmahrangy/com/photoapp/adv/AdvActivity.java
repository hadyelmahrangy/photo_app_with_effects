package hadyelmahrangy.com.photoapp.adv;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import butterknife.OnClick;
import hadyelmahrangy.com.photoapp.BaseActivity;
import hadyelmahrangy.com.photoapp.R;

public class AdvActivity extends BaseActivity {

    public static void launch(@NonNull AppCompatActivity activity) {
        Intent intent = new Intent(activity, AdvActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onViewReady() {
        //no-op
    }

    @Override
    protected int onRequestLayout() {
        return R.layout.activity_adv;
    }

    @OnClick(R.id.iv_close)
    void onClose() {
        finish();
    }
}
