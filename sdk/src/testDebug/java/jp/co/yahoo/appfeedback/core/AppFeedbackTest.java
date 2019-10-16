package jp.co.yahoo.appfeedback.core;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import jp.co.yahoo.appfeedback.R;
import jp.co.yahoo.appfeedback.TestUtil;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 * Created by taicsuzu on 2017/04/26.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({AppFeedbackActivity.class, Build.VERSION.class, AppFeedback.class})
public class AppFeedbackTest {

    AppFeedbackContext appFeedbackContext;

    /**
     * Mock化したContext
     * @return
     */
    private Context mockContext() {
        Context mockContext = mock(Context.class);
        doReturn("dev-%d").when(mockContext).getString(R.string.appfeedback_api_dev);
        doReturn("alpha").when(mockContext).getString(R.string.appfeedback_api_alpha);
        doReturn("internal").when(mockContext).getString(R.string.appfeedback_api_internal);
        doReturn("external").when(mockContext).getString(R.string.appfeedback_api_external);
        return mockContext;
    }

    /**
     * Mock化したMediaProjection
     */
    private Intent mockMediaProjectionData() {
        Intent data = mock(Intent.class);
        return data;
    }

    private void startWith(int apiLevel, String token, String slackChannel) throws Exception{
        // static finalなフィールドの値をセット
        TestUtil.setFinalStatic(Build.VERSION.class.getDeclaredField("SDK_INT"), apiLevel);

        // staticなメソッドをモック化
        mockStatic(AppFeedbackActivity.class);
        doNothing().when(AppFeedbackActivity.class, "launch", Mockito.any());

        appFeedbackContext = mock(AppFeedbackContext.class);
        // コンストラクタをモック化
        whenNew(AppFeedbackContext.class).withAnyArguments().thenReturn(appFeedbackContext);

        AppFeedback.reset();
        AppFeedback.start(null, token, slackChannel);
    }

    @Test
    public void start成功() throws Exception {
        startWith(18, "token", "@taicsuzu");
        assertEquals(AppFeedback.getToken(), "token");
        assertEquals(AppFeedback.getSlackChannel(), "@taicsuzu");
    }

    @Test
    public void start失敗_Channelが設定されていない() throws Exception{
        // gitHostがnull
        startWith(23, null, null);
        assertEquals(AppFeedback.getToken(), null);
        assertEquals(AppFeedback.getSlackChannel(), null);
    }

    @TargetApi(21)
    @Test
    public void setMediaProjection() throws Exception {
        Intent data = mockMediaProjectionData();
        AppFeedback.setMediaProjection(data, true);
        assertEquals(data, AppFeedback.getMediaProjectionData());
        assertEquals(true, AppFeedback.canUseMediaProjectionAPI());
    }

    @Test
    public void getAppFeedbackContext() throws Exception {
        // 正常に起動
        startWith(23, "token", "@taicsuzu");
        assertEquals(AppFeedback.getAppFeedbackContext(), appFeedbackContext);
    }

    @Test
    public void canUseMediaProjectionAPI() throws Exception {
        Intent data = mockMediaProjectionData();
        AppFeedback.setMediaProjection(data, true);
        assertEquals(true, AppFeedback.canUseMediaProjectionAPI());
    }

    @Test
    public void getMediaProjectionData() throws Exception {
        Intent data = mockMediaProjectionData();
        AppFeedback.setMediaProjection(data, true);
        assertEquals(data, AppFeedback.getMediaProjectionData());
    }
}
