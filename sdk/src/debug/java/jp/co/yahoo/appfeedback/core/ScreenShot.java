package jp.co.yahoo.appfeedback.core;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;

import java.nio.ByteBuffer;

/**
 * スクリーンショットの保存・取得を行う
 *
 * Created by taicsuzu on 2016/08/28.
 */
@TargetApi(21)
class ScreenShot implements ImageReader.OnImageAvailableListener{
    private HandlerThread handlerThread;
    private Handler handler;

    private ImageReader imageReader;
    private VirtualDisplay virtualDisplay;
    private AppFeedbackContext appFeedbackContext;

    private int screenWidth, screenHeight;
    private Bitmap screenShotBitmap;
    private boolean isRunning = false;

    // スクリーンショットの生データは全てこのstaticなssdに保存されていく
    // 後述するsaveが行われた時はssdをcloneしたものからbitmapを生成する
    private static ScreenShotData ssd;
    private static ScreenShot screenShot;

    static ScreenShot getInstance() {
        if(screenShot == null) {
            screenShot = new ScreenShot();
        }

        return screenShot;
    }

    ScreenShot() {
        appFeedbackContext = AppFeedback.getAppFeedbackContext();
        screenWidth = appFeedbackContext.getScreenWidth();
        screenHeight = appFeedbackContext.getScreenHeight();
        ssd = new ScreenShotData();
    }

    /**
     * ScreenShotを開始する
     * @param context
     */
    void start(Context context) {
        isRunning = true;

        handlerThread = new HandlerThread(getClass().getSimpleName(), android.os.Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        appFeedbackContext.setScreenSize();
        screenWidth = appFeedbackContext.getScreenWidth();
        screenHeight = appFeedbackContext.getScreenHeight();
        imageReader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 2);
        imageReader.setOnImageAvailableListener(this, handler);

        virtualDisplay = appFeedbackContext.setupVirtualDisplay(context, "AppFeedback_ScreenShot", imageReader.getSurface(), handler);
    }

    /**
     * ScreenShotを停止する
     */
    void stop() {
        isRunning = false;

        if(virtualDisplay != null) {
            virtualDisplay.release();
            virtualDisplay = null;
        }

        if(handlerThread != null) {
            handlerThread.quitSafely();
            handlerThread = null;
        }

        if(imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
    }

    /**
     * ScreenShotが動作中か確認する
     * @return boolean(trueなら動作中)
     */
    boolean isRunning() {
        return isRunning;
    }

    @Override
    public void onImageAvailable(final ImageReader imageReader) {
        // 画面の画像が取得可能になるたびにここにくる
        Image img = null;

        try {
            // 最新のイメージを取得
            img = imageReader.acquireLatestImage();

            if(img != null && isRunning) {

                Image.Plane plane = img.getPlanes()[0];
                ByteBuffer buffer = plane.getBuffer();

                int pixelStride = plane.getPixelStride();
                int rowStride = plane.getRowStride();
                int rowPadding = rowStride - pixelStride * screenWidth;

                // staticなssdにデータを差し込む
                ssd.byteBuffer = buffer;
                ssd.pixelStride = pixelStride;
                ssd.rowPadding = rowPadding;
            }
        }catch(Exception e) {
            // ScreenShotとstopした直後にImageReader.acquireLatestImage()が呼ばれると
            // 解放済みのImageを操作しているとしてエラーにになる
            // e.printStackTrace();
        }finally {
            if(img != null) {
                try { img.close(); }catch (Exception e) {}
            }
        }
    }

    /**
     * 呼ばれた時点でのスクリーンショットをbitmapに変換する
     * @return boolean(trueなら保存成功)
     */
    boolean saveScreenShot() {
        if (Build.VERSION.SDK_INT >= 21 && AppFeedback.canUseMediaProjectionAPI()) {
            // ssdをcloneしてbitmapに変換する
            ScreenShotData data = (ScreenShotData) ssd.clone();
            Bitmap bitmap = Bitmap.createBitmap(screenWidth + data.rowPadding / data.pixelStride, screenHeight, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(data.byteBuffer);
            // 余白をトリミングで消す
            screenShotBitmap = Bitmap.createBitmap(bitmap, 0, 0, screenWidth, screenHeight);
            return true;
        }else{
            return false;
        }
    }

    /**
     * bitmapのセッター
     * @param bitmap Bitmap
     */
    void setScreenShotBitmap(Bitmap bitmap) {
        this.screenShotBitmap = bitmap;
    }

    /**
     * 保存済みのbitmapを返す
     * 保存済みのデータがなければnullが返される
     * @return Bitmap(ただし保存済みのデータがなければnull)
     */
    public Bitmap getBitmap() {
        if (Build.VERSION.SDK_INT >= 21 && AppFeedback.canUseMediaProjectionAPI()) {
            if(!screenShotBitmap.isRecycled()) {
                return screenShotBitmap;
            }else{
                return null;
            }
        }
        return null;
    }

    /**
     * スクリーンショットの生データを持つCloneableなクラス
     */
    class ScreenShotData implements Cloneable{
        ByteBuffer byteBuffer;
        int pixelStride;
        int rowPadding;

        @Override
        protected Object clone() {
            ScreenShotData _ssd = new ScreenShotData();
            _ssd.byteBuffer = byteBuffer;
            _ssd.pixelStride = pixelStride;
            _ssd.rowPadding = rowPadding;
            return _ssd;
        }
    }
}
