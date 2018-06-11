package hadyelmahrangy.com.photoapp.eventBus;

import com.squareup.otto.Bus;

public class AppBus {
    private static Bus sBus;

    public static Bus getBus() {
        if (sBus == null)
            sBus = new Bus();
        return sBus;
    }
}
