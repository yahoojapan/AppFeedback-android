package jp.co.yahoo.appfeedback.core;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.Surface;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * 実行中に必要なObjectやPropertyを保持する
 *
 * Created by taicsuzu on 2016/09/15.
 */

class AppFeedbackContext {
    private MediaProjection mediaProjection;
    private File cacheDir;
    private int screenWidth, screenHeight;
    private Display display;

    AppFeedbackContext(Activity activity) {
        setActivity(activity);
        setScreenSize();
    }

    private void setActivity(Activity activity) {
        cacheDir = activity.getCacheDir();
        display = activity.getWindowManager().getDefaultDisplay();
    }

    /**
     * Cacheに使用するディレクトリを返す
     * @return Directory
     */
    File getCacheDir() {
        return cacheDir;
    }

    /**
     * Screenの幅を返す
     * @return screenWidth
     */
    int getScreenWidth() {
        return screenWidth;
    }

    /**
     * Screenの高さを返す
     * @return screenHeight
     */
    int getScreenHeight() {
        return screenHeight;
    }

    /**
     * Screenのサイズをセットする
     */
    @TargetApi(17)
    public void setScreenSize() {
        Point point = new Point();
        display.getRealSize(point);
        this.screenWidth = point.x;
        this.screenHeight = point.y;
    }

    /**
     * MediaProjectionを返す
     * ScreenRecorderとScreenShotで使用している
     * @return MediaProjection
     */
    @TargetApi(21)
    private MediaProjection getMediaProjection(Context context) {
        if(mediaProjection == null) {
            Intent resultData = AppFeedback.getMediaProjectionData();
            MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            mediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, resultData);
        }

        return mediaProjection;
    }

    /**
     * VirtualDisplayのセットアップを行う
     * @param name VirtualDisplayの名前
     * @param surface 取得するSurface
     * @param handler Handler
     * @return VirtualDisplay
     */
    @TargetApi(21)
    VirtualDisplay setupVirtualDisplay(Context context,
                                       String name,
                                       Surface surface,
                                       Handler handler) {
        return getMediaProjection(context).createVirtualDisplay(
                name,
                getScreenWidth(),
                getScreenHeight(),
                context.getResources().getDisplayMetrics().densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR|DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                surface,
                null,
                handler);
    }

    @Nullable
    static Activity getCurrentActivity() {
        Class activityThreadClass;
        Object activityThread;
        Field activitiesField;
        try {
            activityThreadClass = Class.forName("android.app.ActivityThread");
            activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
            activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);
            Map<Object, Object> activities = (Map<Object, Object>) activitiesField.get(activityThread);
            if (activities == null)
                return null;
            for (Object activityRecord : activities.values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);
                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    Activity activity = (Activity) activityField.get(activityRecord);
                    return activity;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }
}
