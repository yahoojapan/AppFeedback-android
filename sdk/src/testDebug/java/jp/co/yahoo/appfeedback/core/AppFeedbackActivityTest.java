package jp.co.yahoo.appfeedback.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import jp.co.yahoo.appfeedback.TestUtil;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.verifyPrivate;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

/**
 * Created by taicsuzu on 2017/05/01.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({AppFeedbackActivity.class, Settings.class, AppFeedback.class})
public class AppFeedbackActivityTest {
    static final int REQUEST_OL_PERMISSION = 10042;
    static final int REQUEST_FL_PERMISSION = 10041;
    static final int REQUEST_MP_PERMISSION = 10040;

    AppFeedbackActivity mockActivity() throws Exception{
        AppFeedbackActivity activity = spy(new AppFeedbackActivity());
        doNothing().when(activity, "checkPermissionMediaProjectionNow");
        doNothing().when(activity, "checkPermissionFileAccess");
        doNothing().when(activity, "cancel");
        doNothing().when(activity, "start");
        return activity;
    }

    @Test
    public void launch() throws Exception {
        Context mockContext = mock(Context.class);
        AppFeedbackActivity.launch(mockContext);
        verify(mockContext).startActivity((Intent) Mockito.any());
    }

    @Test
    public void onActivityResult_APIレベルが低いものは無視() throws Exception {
        TestUtil.setFinalStatic(Build.VERSION.class.getDeclaredField("SDK_INT"), 10);
        AppFeedbackActivity activity = mockActivity();
        activity.onActivityResult(REQUEST_OL_PERMISSION, 0, null);

        // 以下のメソッドは呼ばれない
        verifyPrivate(activity, never()).invoke("checkPermissionMediaProjectionNow");
        verifyPrivate(activity, never()).invoke("checkPermissionFileAccess");
    }

    @Test
    public void onActivityResult_OverlayとMediaProjection以外からの呼び出しは無視() throws Exception {
        TestUtil.setFinalStatic(Build.VERSION.class.getDeclaredField("SDK_INT"), 23);
        AppFeedbackActivity activity = mockActivity();
        activity.onActivityResult(100, 0, null);

        // 以下のメソッドは呼ばれない
        verifyPrivate(activity, never()).invoke("checkPermissionMediaProjectionNow");
        verifyPrivate(activity, never()).invoke("checkPermissionFileAccess");
    }

    @Test
    public void onActivityResult_Overlayのパーミッション獲得成功() throws Exception {
        TestUtil.setFinalStatic(Build.VERSION.class.getDeclaredField("SDK_INT"), 23);
        AppFeedbackActivity activity = mockActivity();

        // 獲得成功
        mockStatic(Settings.class);
        doReturn(true).when(Settings.class, "canDrawOverlays", activity);

        activity.onActivityResult(REQUEST_OL_PERMISSION, 0, null);

        // MediaProjectionのパーミッション要求が呼ばれる
        verifyPrivate(activity).invoke("checkPermissionMediaProjectionNow");
        // キャンセルされない
        verifyPrivate(activity, never()).invoke("cancel");
    }

    @Test
    public void onActivityResult_Overlayのパーミッション獲得失敗() throws Exception {
        TestUtil.setFinalStatic(Build.VERSION.class.getDeclaredField("SDK_INT"), 23);
        AppFeedbackActivity activity = mockActivity();

        // 獲得失敗
        mockStatic(Settings.class);
        doReturn(false).when(Settings.class, "canDrawOverlays", activity);

        activity.onActivityResult(REQUEST_OL_PERMISSION, 0, null);

        // MediaProjectionのパーミッション要求は呼ばれない
        verifyPrivate(activity, never()).invoke("checkPermissionMediaProjectionNow");
        // キャンセルされる
        verifyPrivate(activity).invoke("cancel");
    }

    @Test
    public void onActivityResult_MediaProjectionのパーミッション獲得成功() throws Exception {
        TestUtil.setFinalStatic(Build.VERSION.class.getDeclaredField("SDK_INT"), 23);
        AppFeedbackActivity activity = mockActivity();

        Intent data = mock(Intent.class);

        mockStatic(AppFeedback.class);
        doNothing().when(AppFeedback.class, "setMediaProjection", data, true);

        activity.onActivityResult(REQUEST_MP_PERMISSION, Activity.RESULT_OK, data);

        // ファイルのパーミッション要求を行う
        verifyPrivate(activity, times(1)).invoke("checkPermissionFileAccess");
        // setMediaProjectionが一回呼ばれる
        verifyStatic(times(1));
    }

    @Test
    public void onActivityResult_MediaProjectionのパーミッション獲得失敗() throws Exception {
        TestUtil.setFinalStatic(Build.VERSION.class.getDeclaredField("SDK_INT"), 23);
        AppFeedbackActivity activity = mockActivity();

        mockStatic(AppFeedback.class);
        doNothing().when(AppFeedback.class, "setMediaProjection", null, false);

        activity.onActivityResult(REQUEST_MP_PERMISSION, Activity.RESULT_CANCELED, null);

        // ファイルのパーミッション要求を行う(MediaProjectionが使えなくてもファイルアクセスの要求は行う)
        verifyPrivate(activity).invoke("checkPermissionFileAccess");
        // setMediaProjectionが一回呼ばれる
        verifyStatic(times(1));
    }

    @Test
    public void onRequestPermissionsResult_パーミッションの獲得に成功していても失敗していてもstartが呼ばれる() throws Exception {
        TestUtil.setFinalStatic(Build.VERSION.class.getDeclaredField("SDK_INT"), 23);
        AppFeedbackActivity activity = mockActivity();

        activity.onRequestPermissionsResult(REQUEST_FL_PERMISSION, new String[0], new int[0]);

        // start()が呼ばれる
        verifyPrivate(activity).invoke("start");
    }
}
