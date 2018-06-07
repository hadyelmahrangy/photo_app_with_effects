package hadyelmahrangy.com.photoapp.imageEditor.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import hadyelmahrangy.com.photoapp.R;

public class HajibAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface BordersAdapterListener {
        void onClick(@NonNull String borderName);
    }

    private Context context;
    private List<String> items;
    private int screenWidth;

    @NonNull
    private BordersAdapterListener listener;

    public HajibAdapter(@NonNull Context context, @NonNull List<String> items, @NonNull BordersAdapterListener listener, int screenWidth) {
        this.context = context;
        this.items = items;
        this.listener = listener;
        this.screenWidth = screenWidth;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hajib, parent, false);
        RecyclerView.ViewHolder holder = new HajibViewHolder(view, context, listener, screenWidth);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((HajibViewHolder) holder).bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
