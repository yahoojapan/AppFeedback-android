package jp.co.yahoo.appfeedback.core;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.content.ContextCompat;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;

import jp.co.yahoo.appfeedback.utils.SecCounter;

/**
 * 動画キャプチャの開始・取得・保存を行う
 *
 * Created by taicsuzu on 2017/04/17.
 */
class ScreenRecorder {
    private HandlerThread handlerThread;
    private AppFeedbackContext appFeedbackContext;
    private VirtualDisplay virtualDisplay;
    private MediaRecorder mediaRecorder;

    private boolean isRecording = false;
    private boolean hasData = false;
    private String dataFilePath;

    private static ScreenRecorder screenRecorder;

    static ScreenRecorder getInstance() {
        if(screenRecorder == null) {
            screenRecorder = new ScreenRecorder();
        }

        return screenRecorder;
    }

    ScreenRecorder() {
        appFeedbackContext = AppFeedback.getAppFeedbackContext();
    }

    /**
     * 動画キャプチャが撮れるかどうかを返す
     * @return boolean(trueなら録画可能)
     */
    boolean canRecord(Context context) {
        return
                // MediaProjectionが利用可能
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && AppFeedback.canUseMediaProjectionAPI()) &&
                        // File Writeが可能
                        (Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                        // 録画中ではない
                        !isRecording;
    }

    /**
     * 動画キャプチャを録画中かどうか
     * @return boolean(trueなら録画中)
     */
    boolean isRecording() {
        return isRecording;
    }

    /**
     * 動画キャプチャの録画開始
     */
    void startRecording(Context context) {

        if(!canRecord(context))
            return;

        hasData = false;

        handlerThread = new HandlerThread(getClass().getSimpleName(), android.os.Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();

        Handler handler = new Handler(handlerThread.getLooper());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 正常にMediaRecorderの準備が完了したら
            if(prepareMediaRecorder(context)) {
                virtualDisplay =  appFeedbackContext.setupVirtualDisplay(context, "AppFeedback_ScreenRecorder", mediaRecorder.getSurface(), handler);
                isRecording = true;

                // FeedbackActivityを閉じるところが動画に移らないよう、0.5秒ディレイさせる
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mediaRecorder.start();
                        // 秒数カウントを始める
                        SecCounter.getInstance().startCount();
                    }
                }, 500);
            }
        }
    }

    /**
     * 動画キャプチャの録画終了
     */
    void stopRecording() {
        hasData = true;

        // 秒数カウントを止める
        SecCounter.getInstance().stopCount();

        // MediaRecorderを解放する
        if(mediaRecorder != null) {
            try {
                mediaRecorder.stop();
            }catch (Exception e) {
                // ここに来た時は0.5秒より前に停止が押されている
                hasData = false;
            }
            mediaRecorder.reset();
            mediaRecorder = null;
        }

        // VirtualDisplayを解放する
        if(virtualDisplay != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                virtualDisplay.release();
            }
            virtualDisplay = null;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            handlerThread.quitSafely();
        }

        isRecording = false;
    }

    /**
     * ScreenRecorderにデータが存在するか
     * @return boolean(trueならデータが存在する)
     */
    boolean hasData() {
        return hasData;
    }

    /**
     * データファイル(.mp4)へのパス
     * @return ファイルパス
     */
    String getDataFilePath() {
        return dataFilePath;
    }

    /**
     * 既存のデータがあれば削除
     */
    void clean() {
        if(dataFilePath != null) {
            new File(dataFilePath).delete();
            dataFilePath = null;
        }
        hasData = false;
    }

    /**
     * MediaRecorderの初期化
     * Audio(録音)は使わない
     * @return boolean(trueなら正常にprepare完了)
     */
    private boolean prepareMediaRecorder(Context context) {
        dataFilePath = outputFilePath();

        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        int orientation = ORIENTATIONS.get(rotation + 90);

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(dataFilePath);
        mediaRecorder.setVideoSize(appFeedbackContext.getScreenWidth(), appFeedbackContext.getScreenHeight());
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setVideoEncodingBitRate(5 * 1024 * 1024);
        mediaRecorder.setVideoFrameRate(30);
        mediaRecorder.setOrientationHint(orientation);

        try{
            mediaRecorder.prepare();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            dataFilePath = null;
            return false;
        }
    }

    /**
     * データファイルパス
     * アプリのキャッシュ使用領域に保存する
     * @return ファイルパス
     */
    private String outputFilePath() {
        return appFeedbackContext.getCacheDir().getAbsolutePath() + "/" + System.currentTimeMillis() + ".mp4";
    }

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }
}
