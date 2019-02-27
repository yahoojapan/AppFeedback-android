package jp.co.yahoo.appfeedback.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import jp.co.yahoo.appfeedback.R;
import jp.co.yahoo.appfeedback.utils.SecCounter;
import jp.co.yahoo.appfeedback.views.FloatingWindow;
import jp.co.yahoo.appfeedback.views.MovableFrameLayout;

/**
 * 画面上に表示するボタン<br>
 * ボタンがタッチされる・動く・表示する・消すなどを行う
 *
 * Created by taicsuzu on 2015/08/08.
 */
class Clip extends FloatingWindow implements View.OnTouchListener {
    private static final int SUCCESS_MP = 1, FAILURE_MP = -2;
    private int downX, downY;
    private long startRecordingTime = 0;
    private boolean move = false;
    // スクリーンショットを撮影中かどうか
    // 撮影中ならaddViewOnWindow()が呼ばれても無視する
    private boolean capturing = false;
    private ScreenShot screenShot;

    Clip(Context context) {
        super(context);

        if(Build.VERSION.SDK_INT >= 21 && AppFeedback.canUseMediaProjectionAPI()) {
            // MediaProjectionを開始する
            screenShot = ScreenShot.getInstance();
        }
    }

    /**
     * ClipのLayoutParamsをここで指定する
     * @return LayoutParams
     */
    @Override
    protected WindowManager.LayoutParams setLayoutParams() {

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                (int)getContext().getResources().getDimension(R.dimen.appfeedback_fb_clip_width),
                (int)getContext().getResources().getDimension(R.dimen.appfeedback_fb_clip_height),
                Build.VERSION.SDK_INT >= 26 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        if(Build.VERSION.SDK_INT >= 17) {
            Point display = new Point();
            getWindowManager().getDefaultDisplay().getRealSize(display);
            params.x = display.x / 3;
            params.y = -display.y / 3;
        }

        return params;
    }

    /**
     * ClipのViewを作成<br>
     * @return 作成したView
     */
    @Override
    protected View onCreateView() {
        View view;

        view = getLayoutInflater().inflate(R.layout.appfeedback_floating_clip, null);
        view.setOnTouchListener(this);

        ((MovableFrameLayout)view).setWindowManagerLayoutParams(
                getWindowManager(),
                getLayoutParams()
        );

        ((MovableFrameLayout)view).setOrientation(getContext().getResources().getConfiguration().orientation);
        return view;
    }

    /**
     * 録画中の秒数カウントを受け取るHandler
     */
    private Handler progressHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int sec = msg.arg1;

            // ProgressBarを進める
            ((ProgressBar)getView().findViewById(R.id.appfeedback_clip_progress)).setProgress(sec);

            SecCounter secCounter = SecCounter.getInstance();

            // 最大値に到達
            if(sec == secCounter.getMax()) {

                ScreenRecorder screenRecorder = ScreenRecorder.getInstance();

                // もし画面を録画中なら、mp4保存してからFeedbackActivityを立ち上げる
                if (screenRecorder.isRecording()) {
                    removeFromWindowIfShowing();
                    screenRecorder.stopRecording();
                    launchFeedbackActivity();
                }
            }
        }
    };

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch(motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                actionDown(motionEvent);
                return false;
            case MotionEvent.ACTION_MOVE:
                actionMove(motionEvent);
                return false;
            case MotionEvent.ACTION_UP:
                actionUp(motionEvent);
                return false;
        }
        return false;
    }

    /**
     * スクリーンの向きに応じてアップデートする
     */
    void updateOrientation() {
        ((MovableFrameLayout)getView()).setOrientation(getContext().getResources().getConfiguration().orientation);
    }

    /**
     * Viewをアップデートする
     */
    void updateView() {
        ScreenRecorder screenRecorder = ScreenRecorder.getInstance();

        View view = getView();

        if(screenRecorder.isRecording()) {
            // 録画中のView
            setViewRecording();
            // 秒数カウンタを取得
            SecCounter secCounter = SecCounter.getInstance();
            // 秒数カウントを反映させるために、HandlerをSecCounterにアタッチさせる
            secCounter.attachHandler(progressHandler);
            // 開始時間が異なっていたら、新しいカウンティングなのでProgressBarを0から始める
            if(startRecordingTime != secCounter.startTime()) {
                ((ProgressBar)view.findViewById(R.id.appfeedback_clip_progress)).setProgress(0);
                startRecordingTime = secCounter.startTime();
            }
        }else{
            // 通常のView
            setViewNormal();
        }
    }

    private void setViewRecording() {
        View view = getView();

        view.findViewById(R.id.appfeedback_clip_frame).setBackgroundResource(R.drawable.appfeedback_clip_recording);
        ((ImageView)view.findViewById(R.id.appfeedback_clip_image)).setImageResource(R.drawable.appfeedback_icon_stop_video);
        view.findViewById(R.id.appfeedback_clip_progress).setVisibility(View.VISIBLE);
    }

    private void setViewNormal() {
        View view = getView();

        view.findViewById(R.id.appfeedback_clip_frame).setBackgroundResource(R.drawable.appfeedback_clip);
        ((ImageView)view.findViewById(R.id.appfeedback_clip_image)).setImageResource(R.drawable.appfeedback_icon_feedback);
        view.findViewById(R.id.appfeedback_clip_progress).setVisibility(View.INVISIBLE);
    }

    /**
     * Clipが押されている時
     * @param event MotionEvent
     */
    private void actionDown(MotionEvent event) {
        downX = (int)event.getRawX();
        downY = (int)event.getRawY();
        move = false;
    }

    /**
     * Clipが動いている時
     * 8dpを超えたら動きだす、それまではタッチ判定
     * @param event MotionEvent
     */
    private void actionMove(MotionEvent event) {
        if((Math.abs(downY-event.getRawY()) > getPixelFromDp(8) && Math.abs(downX-event.getRawX()) > getPixelFromDp(8)) || move) {
            int nowMoveX = (int)event.getRawX();
            int nowMoveY = (int)event.getRawY() - (int)getContext().getResources().getDimension(R.dimen.appfeedback_fb_clip_height)/2;
            ((MovableFrameLayout)getView()).move(nowMoveX, nowMoveY);
            move = true;
        }
    }

    /**
     * dpからpixelに変換
     * @return pixel
     */
    private int getPixelFromDp(float dp) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    /**
     * Clipから指が離れた時
     * @param event MotionEvent
     */
    private void actionUp(MotionEvent event) {

        // Clipがタッチ(クリック)された
        if(!move && isShowing()) {

            ScreenRecorder screenRecorder = ScreenRecorder.getInstance();

            // もし画面を録画中なら、mp4保存してからFeedbackActivityを立ち上げる
            if(screenRecorder.isRecording()) {
                screenRecorder.stopRecording();
                launchFeedbackActivity();
            }else if(Build.VERSION.SDK_INT >= 21 && AppFeedback.canUseMediaProjectionAPI() && screenShot != null) {
                // スクリーンショットを取得できる状態であれば、取得してからFeedbackActivityを立ち上げる
                // 画面からClipを消す
                removeFromWindowIfShowing();
                // 撮影中である
                capturing = true;
                // Clipが消えた状態で0.5秒後にスクリーンショットを取得
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (screenShot != null && screenShot.saveScreenShot()) {
                            // スクリーンショットの取得に成功
                            capturedHandler.sendEmptyMessage(SUCCESS_MP);
                        } else {
                            // 失敗
                            capturedHandler.sendEmptyMessage(FAILURE_MP);
                        }
                    }
                }, 500);
            }else{
                // スクリーンショットなしでFeedbackActivityを立ち上げる
                launchFeedbackActivity();
            }

            move = false;
        }
    }

    /**
     * スクリーンショットを取得した結果を受け取るHandler
     */
    private final Handler capturedHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // スクリーンショットの取得に失敗していてもそのままFeedbackActivityを立ち上げる
            if(msg.what == FAILURE_MP) {
                // スクリーンショットの取得に失敗しているが、特に何もしない
            }

            launchFeedbackActivity();

            // 撮影は終了
            capturing = false;
        }
    };

    /**
     * FeedbackActivityを立ち上げる
     * FLAG_ACTIVITY_NEW_TASKがないと立ち上げられない
     */
    private void launchFeedbackActivity() {
        Intent intent = new Intent(getContext(), FeedbackActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);
    }

    /**
     * WindowにViewを追加
     */
    @Override
    public void addViewOnWindow() {
        if(getCurrentActivity() != null &&
                !FeedbackActivity.class.getName().equals(getCurrentActivity().getClass().getName()) &&
                !capturing) {
            superAddViewOnWindow();
        }
    }

    // テストが書きやすいように分離
    private void superAddViewOnWindow() {
        super.addViewOnWindow();
    }

    /**
     * WindowからViewを削除
     */
    @Override
    public void removeFromWindowIfShowing() {
        superRemoveFromWindowIfShowing();
    }

    // テストが書きやすいように分離
    private void superRemoveFromWindowIfShowing() {
        super.removeFromWindowIfShowing();
    }

    /**
     * 現在のActivityを取得
     * ただし、AppFeedbackを導入しているアプリでないアプリのActivityが前面にいる時はnullが帰る
     * @return 現在のActivity
     */
    private static Activity getCurrentActivity() {
        return AppFeedback.getAppFeedbackContext().getCurrentActivity();
    }
}
