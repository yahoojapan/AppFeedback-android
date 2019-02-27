package jp.co.yahoo.appfeedback.core;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.os.Build;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import jp.co.yahoo.appfeedback.R;
import jp.co.yahoo.appfeedback.TestUtil;
import jp.co.yahoo.appfeedback.utils.PreferencesWrapper;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.verifyPrivate;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import static org.powermock.reflect.Whitebox.setInternalState;

/**
 * Created by taicsuzu on 2017/05/01.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({ClipService.class, PendingIntent.class})
public class ClipServiceTest {
    //todo: テストが必要なcheckStatusを分離する

    static int NOTIFICATION_ID = 24943;
    Notification.Builder mockBuilder;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    ClipService mockServiceForUpdateNotification(int apiLevel, boolean showClip) throws Exception {
        TestUtil.setFinalStatic(Build.VERSION.class.getDeclaredField("SDK_INT"), apiLevel);

        ClipService service = spy(new ClipService());

        doReturn("title").when((Context)service).getString(R.string.appfeedback_notification_title);
        doReturn("on").when((Context)service).getString(R.string.appfeedback_notification_mode_on);
        doReturn("off").when((Context)service).getString(R.string.appfeedback_notification_mode_off);
        doNothing().when((Service)service).startForeground(eq(NOTIFICATION_ID), (Notification)anyObject());

        PreferencesWrapper mockPref = mock(PreferencesWrapper.class);
        setInternalState(service, "preferencesWrapper", mockPref);
        doReturn(showClip).when(mockPref).showClip();

        mockBuilder = mock(Notification.Builder.class);
        doReturn(mockBuilder).when(mockBuilder).setSmallIcon(anyInt());
        doReturn(mockBuilder).when(mockBuilder).setContentTitle(anyString());
        doReturn(mockBuilder).when(mockBuilder).setContentText(anyString());
        doReturn(mockBuilder).when(mockBuilder).setContentIntent((PendingIntent)anyObject());
        doReturn(mockBuilder).when(mockBuilder).setWhen(0);
        doReturn(null).when(mockBuilder).build();
        doReturn(null).when(mockBuilder).getNotification();
        whenNew(Notification.Builder.class).withAnyArguments().thenReturn(mockBuilder);

        mockStatic(PendingIntent.class);
        PendingIntent mockIntent = mock(PendingIntent.class);
        doReturn(mockIntent).when(PendingIntent.class, "getActivity", anyObject(), anyInt(), anyObject(), anyInt());

        return service;
    }

    ClipService mockServiceForShowClip(Clip clip, boolean showClip) {
        ClipService service = spy(new ClipService());

        PreferencesWrapper mockPref = mock(PreferencesWrapper.class);
        setInternalState(service, "preferencesWrapper", mockPref);
        doReturn(showClip).when(mockPref).showClip();

        setInternalState(service, "clip", clip);

        return service;
    }

    @Test
    public void removeFeedbackClip() throws Exception {
        Clip clip = mock(Clip.class);
        ClipService service = spy(new ClipService());

        setInternalState(service, "clip", clip);

        service.removeFeedbackClip();

        verify(clip).removeFromWindowIfShowing();;
    }
}
