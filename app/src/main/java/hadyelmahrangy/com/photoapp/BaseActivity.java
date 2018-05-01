package hadyelmahrangy.com.photoapp;

import android.app.ProgressDialog;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(onRequestLayout());
        ButterKnife.bind(this);
        initProgressDialog();
        onViewReady();
    }

    protected abstract void onViewReady();

    @LayoutRes
    protected abstract int onRequestLayout();

    private void initProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
    }

    protected void showProgressDialog() {
        showProgressDialog(R.string.loading);
    }

    protected void showProgressDialog(int stringRes) {
        progressDialog.setMessage(getString(stringRes));
        progressDialog.show();
    }

    protected void hideProgressDialog() {
        progressDialog.dismiss();
    }

    protected void showMessage(int stringRes) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), getString(stringRes), Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    protected void showMessage(String message) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    protected String getApplicationName() {
        ApplicationInfo applicationInfo = getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : getString(stringId);
    }
}
