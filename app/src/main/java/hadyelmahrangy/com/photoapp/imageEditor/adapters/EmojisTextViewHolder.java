package hadyelmahrangy.com.photoapp.imageEditor.adapters;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hadyelmahrangy.com.photoapp.R;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;

public class EmojisTextViewHolder extends RecyclerView.ViewHolder {

    public interface EmojisTextViewHolderListener {
        void onClick(@NonNull String unicode);
    }

    @BindView(R.id.emoji)
    EmojiconTextView tvEmoji;

    @Nullable
    private String emoji;

    @NonNull
    private EmojisTextViewHolderListener listener;

    public EmojisTextViewHolder(View itemView, int width, @NonNull EmojisTextViewHolderListener listener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.listener = listener;
        tvEmoji.getLayoutParams().height = (width - 30) / 5;
        tvEmoji.getLayoutParams().width = (width - 30) / 5;
    }

    public void bind(@NonNull String emoji) {
        this.emoji = emoji;
        tvEmoji.setText(emoji);
    }

    @OnClick(R.id.emoji)
    void onClick() {
        listener.onClick(emoji);
    }
}
