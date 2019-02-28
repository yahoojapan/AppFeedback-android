package jp.co.yahoo.appfeedback.core;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;

/**
 * パーミッションの確認を行うActivity<br>
 * Viewは表示しないが、一時的に画面を覆うため、必ずfinish()する必要がある
 *
 * Created by taicsuzu on 2017/04/25.
 */

public class AppFeedbackActivity extends FragmentActivity {
    private static final int REQUEST_OL_PERMISSION = 10042;
    private static final int REQUEST_FL_PERMISSION = 10041;
    private static final int REQUEST_MP_PERMISSION = 10040;

    // ClipServiceがスタートしているか
    private boolean isStarted = false;

    /**
     * AppFeedbackActivityを立ち上げる
     * @param context Context
     */
    static void launch(Context context) {
        context.startActivity(new Intent(context, AppFeedbackActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startCheckingPermission();
    }

    /**
     * 確認を開始する
     */
    private void startCheckingPermission() {
        checkPermissionMediaProjection();
    }

    /**
     * Overlayでフィードバックボタンをフィードバックボタンを表示して良いか
     */
    private void checkPermissionMediaProjection() {
        // Android 6.0 ~
        // Overlay
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                //Need to acquire overlay permission
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_OL_PERMISSION);
            }else{
                checkPermissionMediaProjectionNow();
            }
        }else{
            checkPermissionMediaProjectionNow();
        }
    }

    /**
     * MediaProjectionは利用可能か
     */
    private void checkPermissionMediaProjectionNow() {
        // Android 5.0 ~
        // MediaProjectionAPI
        if (Build.VERSION.SDK_INT >= 21) {
            if (AppFeedback.canUseMediaProjectionAPI() && AppFeedback.getMediaProjectionData() == null) {
                MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_MP_PERMISSION);
            }else{
                checkPermissionFileAccess();
            }
        } else {
            AppFeedback.setMediaProjection(null, false);
            checkPermissionFileAccess();
        }
    }

    /**
     * Fileアクセスは可能か
     */
    private void checkPermissionFileAccess() {
        // Android 6.0 ~
        // READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        }, REQUEST_FL_PERMISSION);
            }else{
                start();
            }
        }else{
            start();
        }
    }

    /**
     * ClipServiceをスタート
     */
    private void start() {
        // ClipServiceを立ち上げる
        // 重複起動しないようにする
        if(!isStarted) {
            Intent clipService = new Intent(this, ClipService.class);

            // Oreo対応
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                startForegroundService(clipService);
            } else {
                startService(clipService);
            }

            isStarted = true;
        }

        finish();
    }

    /**
     * キャンセル(ClipServiceはスタートしない)
     */
    private void cancel() {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // OverlayとMediaProjectionのリクエスト以外は弾く
        if(requestCode != REQUEST_MP_PERMISSION &&
                requestCode != REQUEST_OL_PERMISSION)
            return;

        if(requestCode == REQUEST_OL_PERMISSION && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(Settings.canDrawOverlays(this)) {
                checkPermissionMediaProjectionNow();
            }else{
                // フィードバックボタンを表示できないのでキャンセル
                cancel();
            }
        }

        if(requestCode == REQUEST_MP_PERMISSION) {
            if(resultCode == Activity.RESULT_OK) {
                AppFeedback.setMediaProjection(data, true);
            }else{
                AppFeedback.setMediaProjection(null, false);
            }
            // MediaProjectionが使えなくてもフィードバックボタンが表示できれば続行する
            checkPermissionFileAccess();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_FL_PERMISSION) {
            //FileのREAD/WRITEは使えなくてもフィードバックボタンを立ち上げる
            start();
        }
    }
}
