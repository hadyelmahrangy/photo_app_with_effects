package hadyelmahrangy.com.photoapp.gallery;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hadyelmahrangy.com.photoapp.R;

public class GalleryViewHolder extends RecyclerView.ViewHolder {

    public interface GalleryViewHolderCallback {
        void onImageClick(String imageUrl);
    }

    @BindView(R.id.iv_image)
    ImageView ivImage;

    private String imageUrl;

    private GalleryViewHolderCallback callback;

    public GalleryViewHolder(View itemView, @NonNull GalleryViewHolderCallback callback) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.callback = callback;
    }

    public void bind(@NonNull String image) {
        this.imageUrl = image;
        loadImage();
    }

    @OnClick(R.id.iv_image)
    void onClick() {
        if (callback != null) {
            callback.onImageClick(imageUrl);
        }
    }

    private void loadImage() {
        Glide.with(itemView.getContext())
                .load(imageUrl)
                .apply(new RequestOptions()
                        .centerCrop()
                        .error(R.drawable.image_placeholder)
                        .placeholder(R.drawable.image_placeholder))
                .into(ivImage);
    }
}
