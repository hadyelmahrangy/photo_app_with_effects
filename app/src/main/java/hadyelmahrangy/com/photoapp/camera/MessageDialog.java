package hadyelmahrangy.com.photoapp.camera;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.TextView;

import hadyelmahrangy.com.photoapp.R;
import hadyelmahrangy.com.photoapp.util.SharedPrefUtils;

public class MessageDialog extends Dialog implements View.OnClickListener {

    TextView okBtn;

    CheckBox showAgain;

    public MessageDialog(@NonNull Context context) {
        super(context);
    }

    public MessageDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected MessageDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_layout);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        showAgain = findViewById(R.id.showAgain);
        okBtn = findViewById(R.id.btn_ok);
        okBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (showAgain.isChecked()) {
            SharedPrefUtils.setShowAgain(getContext(), false);
        }
        dismiss();
    }
}
