package jp.co.yahoo.appfeedback.views;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.view.View;
import android.view.WindowManager;

import jp.co.yahoo.appfeedback.R;

/**
 * Created by taicsuzu on 2017/05/08.
 */

class TapView extends FloatingWindow {

    private Point displaySize = new Point();
    private int viewWidth, viewHeight;

    TapView(Context context) {
        super(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            getWindowManager().getDefaultDisplay().getSize(displaySize);
        }
    }

    @Override
    protected WindowManager.LayoutParams setLayoutParams() {
        viewWidth = (int)getContext().getResources().getDimension(R.dimen.appfeedback_tap_view_width);
        viewHeight = (int)getContext().getResources().getDimension(R.dimen.appfeedback_tap_view_height);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                viewWidth,
                viewHeight,
                Build.VERSION.SDK_INT >= 26 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);

        return params;
    }

    @Override
    protected View onCreateView() {
        return getLayoutInflater().inflate(R.layout.appfeedback_tap_view, null);
    }

    public void startAnimation(int x, int y) {
        if (getView() == null)
            return;

        // タップ位置に変更
        WindowManager.LayoutParams params = getLayoutParams();
        params.x = x - displaySize.x/2;
        params.y = y - displaySize.y/2 - viewHeight/2;

        addViewOnWindow();

        ViewCompat.animate(getView())
                .alphaBy(1.0f)
                .alpha(0.0f)
                .setDuration(500)
                .setListener(new ViewPropertyAnimatorListener() {
                    @Override
                    public void onAnimationEnd(View view) {
                        removeFromWindowIfShowing();
                    }

                    @Override
                    public void onAnimationStart(View view) {/*なにもしない*/}

                    @Override
                    public void onAnimationCancel(View view) {/*なにもしない*/}
                }).start();
    }
}
