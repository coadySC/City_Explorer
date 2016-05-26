package fourthyearp.cian.mapsforge_3;

import android.view.View;

import org.mapsforge.map.android.view.MapView;

/**
 * Created by cian on 19/05/2016.
 */
public abstract class SingleClickListener implements View.OnLongClickListener {
    private boolean processingRoute = false;

    @Override
    public boolean onLongClick(View view) {
        System.out.println("system time milliseconds ->" + System.currentTimeMillis());
        if (!processingRoute) {
            processingRoute = true;
            onOneClick(true);
            System.out.println("Not previously processing route...begin");
        }
        else {
            onOneClick(false);
            System.out.println("Processing route...print toast");
        }
        return true;
    }

    public abstract void onOneClick(boolean beginCalculating);

    public synchronized void enable() {
        processingRoute = false;
    }
}