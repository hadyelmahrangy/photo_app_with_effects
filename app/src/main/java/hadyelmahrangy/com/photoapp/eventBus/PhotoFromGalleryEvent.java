package hadyelmahrangy.com.photoapp.eventBus;

import android.net.Uri;
import android.support.annotation.NonNull;

public class PhotoFromGalleryEvent {

    @NonNull
    private Uri image;

    public PhotoFromGalleryEvent(@NonNull Uri image) {
        this.image = image;
    }

    @NonNull
    public Uri getImage() {
        return image;
    }
}
