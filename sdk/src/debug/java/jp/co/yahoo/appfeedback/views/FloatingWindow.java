package jp.co.yahoo.appfeedback.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

/**
 * 画面上を浮遊させるためのWindow
 *
 * Created by taicsuzu on 2015/08/02.
 */
abstract public class FloatingWindow {
    private View view;
    private Context context;
    private LayoutInflater layoutInflater;
    private WindowManager wm;
    private WindowManager.LayoutParams params;
    private boolean isShowing = false;

    public FloatingWindow(Context context) {
        this.context   = context;
        layoutInflater = LayoutInflater.from(context);
        wm             = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        params         = setLayoutParams();
        view           = onCreateView();
    }

    protected Context getContext() {
        return context;
    }

    protected LayoutInflater getLayoutInflater() {
        return layoutInflater;
    }

    protected WindowManager getWindowManager() {
        return wm;
    }

    protected WindowManager.LayoutParams getLayoutParams() {
        return params;
    }

    /**
     * Viewを返す
     * @return View
     */
    protected View getView() {
        return view;
    }

    /**
     * WindowにViewを追加
     */
    public void addViewOnWindow() {
        if(isShowing)
            return;

        isShowing = true;
        wm.addView(view, params);
    }

    /**
     * WindowからViewを削除<br>
     */
    public void removeFromWindowIfShowing() {
        if (!isShowing)
            return;

        wm.removeViewImmediate(view);
        isShowing = false;
    }

    /**
     * 現在View表示中かどうかを返す
     * @return boolean(trueなら表示中)
     */
    public boolean isShowing() {
        return isShowing;
    }

    abstract protected WindowManager.LayoutParams setLayoutParams();
    abstract protected View onCreateView();
}
