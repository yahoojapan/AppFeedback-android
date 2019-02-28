package jp.co.yahoo.appfeedback.views;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import jp.co.yahoo.appfeedback.R;

/**
 * Created by taicsuzu on 2017/05/08.
 */

public class TapViewParent extends FloatingWindow implements View.OnTouchListener{

    public TapViewParent(Context context) {
        super(context);
    }

    @Override
    protected WindowManager.LayoutParams setLayoutParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(0,0,
                Build.VERSION.SDK_INT >= 26 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        return params;
    }

    @Override
    protected View onCreateView() {
        View view = getLayoutInflater().inflate(R.layout.appfeedback_tap_view_parent, null);
        view.setOnTouchListener(this);
        return view;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // 録画中にアプリがバックグラウンドにいったらタッチ位置は表示しない
        if((int)event.getRawX() == 0 && (int)event.getRawY() == 0)
            return false;

        new TapView(getContext()).startAnimation((int)event.getRawX(), (int)event.getRawY());
        return false;
    }
}
