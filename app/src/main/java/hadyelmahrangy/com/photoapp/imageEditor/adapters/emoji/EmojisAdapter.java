package hadyelmahrangy.com.photoapp.imageEditor.adapters.emoji;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hadyelmahrangy.com.photoapp.R;

public class EmojisAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface EmojisAdapterListener {
        void onEmojisClick(@NonNull String unicode);
    }

    @NonNull
    private List<String> emojis;

    @NonNull
    private EmojisAdapterListener listener;

    private int screenWidth;

    public EmojisAdapter(@NonNull Context context, @NonNull EmojisAdapterListener listener, int screenWidth) {
        this.listener = listener;
        this.screenWidth = screenWidth;

        //TODO IMPLEMENT FOR <=23
        if (Build.VERSION.SDK_INT >= 23) {
            emojis = Arrays.asList(context.getResources().getStringArray(R.array.emoji));
        } else {
            emojis = new ArrayList<>();
        }
    }

    @NonNull
    EmojisTextViewHolder.EmojisTextViewHolderListener emojisTextViewHolderListener = new EmojisTextViewHolder.EmojisTextViewHolderListener() {
        @Override
        public void onClick(@NonNull String unicode) {
            listener.onEmojisClick(unicode);
        }
    };

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_emoji_text, parent, false);
        return new EmojisTextViewHolder(view, screenWidth, emojisTextViewHolderListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof EmojisTextViewHolder) {
            ((EmojisTextViewHolder) holder).bind(emojis.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return emojis.size();
    }
}
