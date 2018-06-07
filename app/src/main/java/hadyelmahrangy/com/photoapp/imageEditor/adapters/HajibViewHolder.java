package hadyelmahrangy.com.photoapp.imageEditor.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hadyelmahrangy.com.photoapp.R;
import hadyelmahrangy.com.photoapp.imageEditor.ImageEditorActivity;

public class HajibViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.image)
    ImageView ivBorder;

    @BindView(R.id.iv_no_border)
    ImageView ivNoBorder;

    @NonNull
    private Context context;

    @NonNull
    private HajibAdapter.BordersAdapterListener listener;

    @Nullable
    private String borderName;

    public HajibViewHolder(View itemView, @NonNull Context context, @NonNull HajibAdapter.BordersAdapterListener listener, int width) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.context = context;
        this.listener = listener;
        ivBorder.getLayoutParams().height = (width - 30) / 4;
        ivBorder.getLayoutParams().width = (width - 30) / 4;
    }

    public void bind(@NonNull String item) {
        borderName = item;
        checkBorderPossition();
        float density = context.getResources().getDisplayMetrics().density;
        Glide.with(context)
                .load(Uri.parse("file:///android_asset/" + ImageEditorActivity.ASSETS_HAJIB + "/" + item))
                .apply(new RequestOptions()
                        .dontAnimate()
                        .skipMemoryCache(false)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .override((int) (125 * density), (int) (125 * density)))
                .into(ivBorder);
    }

    private void checkBorderPossition() {
        if (getAdapterPosition() == 0) {
            ivNoBorder.setVisibility(View.VISIBLE);
        } else {
            ivNoBorder.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.image)
    void onBorderClick() {
        if (borderName != null) {
            listener.onClick(borderName);
        }
    }
}
