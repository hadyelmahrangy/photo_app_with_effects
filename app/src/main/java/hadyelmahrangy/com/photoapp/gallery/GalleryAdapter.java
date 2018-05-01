package hadyelmahrangy.com.photoapp.gallery;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import hadyelmahrangy.com.photoapp.R;

public class GalleryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface GalleryAdapterCallback {
        void onImageClick(String imageUrl);
    }

    private List<String> images = new ArrayList<>();

    private GalleryAdapterCallback callback;

    public GalleryAdapter(@NonNull GalleryAdapter.GalleryAdapterCallback callback) {
        this.callback = callback;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_callery_image, parent, false);
        return new GalleryViewHolder(view, new GalleryViewHolder.GalleryViewHolderCallback() {
            @Override
            public void onImageClick(String imageUrl) {
                callback.onImageClick(imageUrl);
            }
        });
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof GalleryViewHolder) {
            ((GalleryViewHolder) holder).bind(images.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public void setImages(@NonNull List<String> images) {
        this.images.clear();
        this.images.addAll(images);
        notifyDataSetChanged();
    }
}
