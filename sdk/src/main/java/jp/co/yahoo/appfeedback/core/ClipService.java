package jp.co.yahoo.appfeedback.core;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import jp.co.yahoo.appfeedback.R;
import jp.co.yahoo.appfeedback.utils.PreferencesWrapper;
import jp.co.yahoo.appfeedback.views.TapViewParent;

/**
 * Clipを表示し続けるためのService
 * ClipServiceはAppFeedbackActivityが起動する
 *
 * Created by taicsuzu on 2016/09/15.
 */
class ClipService extends Service {
    private static final int SEC = 1000;
    // 0.5秒ごとにアプリの状態をチェックする
    private static final int CHECK_INTERVAL = SEC/5;
    private static int NOTIFICATION_ID = 24943;

    private Clip clip;
    private TapViewParent tapViewParent;
    private NotificationHelper notificationHelper;
    private Notification notification;

    private PreferencesWrapper preferencesWrapper;

    // AppFeedbackSDKを導入しているアプリが画面の前面にいる
    private boolean isForeground = false;

    // アプリの状態を定期的にチェックする
    // 状態を受け取るHandler
    private Handler statusChecker = new Handler();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        preferencesWrapper = PreferencesWrapper.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        // Clipの初期化
        init();
        // Notificationの表示
        updateNotification();
        // アプリの状態確認の開始
        statusChecker.postDelayed(checkStatus, CHECK_INTERVAL);

        return START_NOT_STICKY;
    }

    /**
     * Clipの初期化
     */
    private void init() {
        if(clip == null) {
            clip = new Clip(this);
        }

        if (notificationHelper == null) {
            notificationHelper = new NotificationHelper(this);
        }
    }

    /**
     * Notificationの表示
     */
    protected void updateNotification() {
        Intent clipIntent = new Intent(this, ClipIntent.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, clipIntent, 0);

        if (preferencesWrapper.showClip()) {
            notification = notificationHelper.createNotification(
                    this,
                    getString(R.string.appfeedback_notification_title),
                    getString(R.string.appfeedback_notification_mode_off),
                    R.drawable.appfeedback_notification,
                    pendingIntent
            );
        } else {
            notification = notificationHelper.createNotification(
                    this,
                    getString(R.string.appfeedback_notification_title),
                    getString(R.string.appfeedback_notification_mode_on),
                    R.drawable.appfeedback_notification,
                    pendingIntent
            );
        }

        startForeground(NOTIFICATION_ID, notification);
    }

    /**
     * Clipの表示
     */
    protected void showFeedbackClip() {
        if(clip != null && preferencesWrapper.showClip()) {
            clip.updateView();
            clip.addViewOnWindow();
        }
    }

    /**
     * Clipの非表示
     */
    protected void removeFeedbackClip() {
        if(clip != null) {
            clip.removeFromWindowIfShowing();
        }
    }

    /**
     * アプリの状態をチェックするHandler
     */
    private Runnable checkStatus = new Runnable() {
        @Override
        public void run() {
            // 前面にいるActivtyを取得
            // ただし、他のアプリが前面にいる場合はnullが入る
            Activity activity = AppFeedback.getAppFeedbackContext().getCurrentActivity();

            // 前面にいるのはFeedbackActivity
            boolean isFeedbackActivity = activity != null && FeedbackActivity.class.getName().equals(activity.getClass().getName());
            boolean isDrawActivity = activity != null && DrawActivity.class.getName().equals(activity.getClass().getName());

            if(isFeedbackActivity || isDrawActivity) {
                // FeedbackActivityもしくはDrawActivityが前面にいたらClipを消す
                removeFeedbackClip();
            }else{
                // Clipに関して
                // アプリが前面にいて設定は"表示"
                if(activity != null && preferencesWrapper.showClip()) {
                    // にもかかわらず表示されていない
                    if(!clip.isShowing()) {
                        // 表示する
                        showFeedbackClip();
                        updateNotification();
                    }
                }else{
                    // アプリが前面にいない、もしくは設定が"非表示"
                    // にもかかわらず表示されている
                    if(clip.isShowing()) {
                        // 非表示にする
                        removeFeedbackClip();
                        updateNotification();
                    }
                }
            }

            if(Build.VERSION.SDK_INT >= 21 && AppFeedback.canUseMediaProjectionAPI()) {

                ScreenShot screenShot = ScreenShot.getInstance();

                // アプリが前面にいる
                if (activity != null) {
                    // ScreenShotが動いていない
                    if (!screenShot.isRunning()) {
                        screenShot.start(getBaseContext());
                    }
                } else {
                    // アプリが前面にいない
                    // にもかかわらずScreenshotが動いている
                    if (screenShot.isRunning()) {
                        screenShot.stop();
                    }
                }

                ScreenRecorder screenRecorder = ScreenRecorder.getInstance();

                // 録画中
                if(screenRecorder.isRecording()) {
                    // タップ位置を表示していない
                    if(tapViewParent == null) {
                        tapViewParent = new TapViewParent(ClipService.this);
                        tapViewParent.addViewOnWindow();
                    }
                } else {
                    // 録画中ではない
                    // にもかかわらずタップ位置を表示している
                    if(tapViewParent != null) {
                        tapViewParent.removeFromWindowIfShowing();
                        tapViewParent = null;
                    }
                }
            }

            // Notificationに関して
            // アプリが前面にいる
            if(activity != null) {
                // にもかかわらず表示されていない
                if(!isForeground) {
                    // 表示する
                    startForeground(NOTIFICATION_ID, notification);
                    isForeground = true;
                }
            }else{
                // アプリが前面にいないにもかかわらず表示されている
                if(isForeground) {
                    // 非表示にする
                    stopForeground(true);
                    isForeground = false;
                }
            }

            // 一定のインターバルを経て状態確認
            statusChecker.postDelayed(this, CHECK_INTERVAL);
        }
    };

    @Override
    public void onDestroy() {
        stopSelf(NOTIFICATION_ID);
        removeFeedbackClip();
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        clip.updateOrientation();
        ScreenShot.getInstance().stop();
    }
}
