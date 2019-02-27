package jp.co.yahoo.appfeedback.core;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import jp.co.yahoo.appfeedback.R;
import jp.co.yahoo.appfeedback.TestUtil;
import jp.co.yahoo.appfeedback.views.MovableFrameLayout;
import jp.co.yahoo.appfeedback.views.FloatingWindow;
import jp.co.yahoo.appfeedback.utils.SecCounter;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.verifyPrivate;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import static org.powermock.api.support.membermodification.MemberMatcher.constructor;
import static org.powermock.api.support.membermodification.MemberModifier.suppress;
import static org.powermock.reflect.Whitebox.setInternalState;

/**
 * Created by taicsuzu on 2017/05/01.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({AppFeedback.class, ScreenShot.class, Clip.class, ScreenRecorder.class, SecCounter.class})
public class ClipTest {
    ScreenShot mockScreenShot;

    Clip mockClip() throws Exception {
        Configuration configuration = mock(Configuration.class);
        configuration.orientation = 0;

        Resources resources = mock(Resources.class);
        doReturn(10.0f).when(resources).getDimension(R.dimen.appfeedback_fb_clip_width);
        doReturn(20.0f).when(resources).getDimension(R.dimen.appfeedback_fb_clip_height);
        doReturn(configuration).when(resources).getConfiguration();

        Context context = mock(Context.class);
        doReturn(resources).when(context).getResources();

        mockScreenShot = mock(ScreenShot.class);

        mockStatic(ScreenShot.class);
        doReturn(mockScreenShot).when(ScreenShot.class, "getInstance");

        mockStatic(AppFeedback.class);
        doReturn(true).when(AppFeedback.class, "canUseMediaProjectionAPI");

        suppress(constructor(FloatingWindow.class));

        Clip clip = spy(new Clip(context));
        doReturn(context).when(clip, "getContext");

        return clip;
    }

    @Test
    @TargetApi(17)
    public void setLayoutParams() throws Exception {
        TestUtil.setFinalStatic(Build.VERSION.class.getDeclaredField("SDK_INT"), 23);

        Clip clip = mockClip();

        WindowManager.LayoutParams mockParams = mock(WindowManager.LayoutParams.class);
        whenNew(WindowManager.LayoutParams.class).withAnyArguments().thenReturn(mockParams);

        WindowManager wm = mock(WindowManager.class);
        final Display display = mock(Display.class);

        doReturn(wm).when(clip, "getWindowManager");
        doReturn(display).when(wm).getDefaultDisplay();

        final Point point = mock(Point.class);
        whenNew(Point.class).withNoArguments().thenReturn(point);

        if(Build.VERSION.SDK_INT >= 13) {
            doAnswer(new Answer<Void>() {
                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    point.x = 30;
                    point.y = 30;
                    return null;
                }
            }).when(display).getRealSize((Point) Mockito.any());
        }

        WindowManager.LayoutParams params = clip.setLayoutParams();

        verifyNew(WindowManager.LayoutParams.class).withArguments(
                10,
                20,
                Build.VERSION.SDK_INT >= 26 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        assertEquals(30, point.x);
        assertEquals(30, point.y);
        assertEquals(10, params.x);
        assertEquals(-10, params.y);
    }

    @Test
    public void onCreateView() throws Exception {
        MovableFrameLayout view = mock(MovableFrameLayout.class);
        doNothing().when(view).setOnTouchListener((View.OnTouchListener) Mockito.any());
        doNothing().when(view).setOrientation(0);

        LayoutInflater inflater = mock(LayoutInflater.class);
        doReturn(view).when(inflater).inflate(R.layout.appfeedback_floating_clip, null);

        WindowManager wm = mock(WindowManager.class);
        WindowManager.LayoutParams params = mock(WindowManager.LayoutParams.class);

        Clip clip = mockClip();

        doReturn(inflater).when(clip, "getLayoutInflater");
        doReturn(wm).when(clip, "getWindowManager");
        doReturn(params).when(clip, "getLayoutParams");

        clip.onCreateView();

        verifyPrivate(clip).invoke("getLayoutInflater");
        verifyPrivate(clip).invoke("getWindowManager");
        verifyPrivate(clip).invoke("getLayoutParams");
    }

    @Test
    public void onTouch_actionDown() throws Exception {
        MotionEvent event = mock(MotionEvent.class);
        doReturn(MotionEvent.ACTION_DOWN).when(event).getAction();

        Clip clip = mockClip();
        doNothing().when(clip, "actionDown", event);
        doNothing().when(clip, "actionMove", event);
        doNothing().when(clip, "actionUp", event);

        clip.onTouch(null, event);

        verifyPrivate(clip).invoke("actionDown", event);
    }

    @Test
    public void onTouch_actionMove() throws Exception {
        MotionEvent event = mock(MotionEvent.class);
        doReturn(MotionEvent.ACTION_MOVE).when(event).getAction();

        Clip clip = mockClip();
        doNothing().when(clip, "actionDown", event);
        doNothing().when(clip, "actionMove", event);
        doNothing().when(clip, "actionUp", event);

        clip.onTouch(null, event);

        verifyPrivate(clip).invoke("actionMove", event);
    }

    @Test
    public void onTouch_actionUp() throws Exception {
        MotionEvent event = mock(MotionEvent.class);
        doReturn(MotionEvent.ACTION_UP).when(event).getAction();

        Clip clip = mockClip();
        doNothing().when(clip, "actionDown", event);
        doNothing().when(clip, "actionMove", event);
        doNothing().when(clip, "actionUp", event);

        clip.onTouch(null, event);

        verifyPrivate(clip).invoke("actionUp", event);
    }

    @Test
    public void updateOrientation() throws Exception {
        MovableFrameLayout view = mock(MovableFrameLayout.class);
        doNothing().when(view).setOrientation(0);

        Clip clip = mockClip();
        doReturn(view).when(clip, "getView");

        clip.updateOrientation();

        verify(view).setOrientation(0);
    }

    @Test
    public void updateView_通常() throws Exception {
        SparseIntArray array = mock(SparseIntArray.class);
        doNothing().when(array).append(Mockito.anyInt(), Mockito.anyInt());
        whenNew(SparseIntArray.class).withAnyArguments().thenReturn(array);

        mockStatic(ScreenRecorder.class);

        ScreenRecorder recorder = mock(ScreenRecorder.class);
        doReturn(recorder).when(ScreenRecorder.class, "getInstance");
        doReturn(false).when(recorder).isRecording();

        Clip clip = mockClip();
        doNothing().when(clip, "setViewNormal");

        clip.updateView();

        verifyPrivate(clip).invoke("setViewNormal");
    }

    @Test
    public void updateView_録画を開始() throws Exception {
        SparseIntArray array = mock(SparseIntArray.class);
        doNothing().when(array).append(Mockito.anyInt(), Mockito.anyInt());
        whenNew(SparseIntArray.class).withAnyArguments().thenReturn(array);

        mockStatic(ScreenRecorder.class);
        mockStatic(SecCounter.class);

        ScreenRecorder recorder = mock(ScreenRecorder.class);
        doReturn(recorder).when(ScreenRecorder.class, "getInstance");
        doReturn(true).when(recorder).isRecording();

        SecCounter counter = mock(SecCounter.class);
        doReturn(counter).when(SecCounter.class, "getInstance");
        doNothing().when(counter).attachHandler((Handler) Mockito.any());
        doReturn(1).when(counter).startTime();

        ProgressBar progress = mock(ProgressBar.class);
        doNothing().when(progress).setProgress(Mockito.anyInt());

        MovableFrameLayout view = mock(MovableFrameLayout.class);
        doReturn(progress).when(view).findViewById(Mockito.anyInt());

        Clip clip = mockClip();
        doNothing().when(clip, "setViewRecording");
        doReturn(view).when(clip, "getView");

        setInternalState(clip, "startRecordingTime", 0L);

        clip.updateView();

        verify(progress).setProgress(Mockito.anyInt());
        verifyPrivate(clip).invoke("setViewRecording");
        assertEquals(1L, Whitebox.getInternalState(clip, "startRecordingTime"));
    }

    @Test
    public void updateView_録画中() throws Exception {
        SparseIntArray array = mock(SparseIntArray.class);
        doNothing().when(array).append(Mockito.anyInt(), Mockito.anyInt());
        whenNew(SparseIntArray.class).withAnyArguments().thenReturn(array);

        mockStatic(ScreenRecorder.class);
        mockStatic(SecCounter.class);

        ScreenRecorder recorder = mock(ScreenRecorder.class);
        doReturn(recorder).when(ScreenRecorder.class, "getInstance");
        doReturn(true).when(recorder).isRecording();

        SecCounter counter = mock(SecCounter.class);
        doReturn(counter).when(SecCounter.class, "getInstance");
        doNothing().when(counter).attachHandler((Handler) Mockito.any());
        doReturn(1).when(counter).startTime();

        ProgressBar progress = mock(ProgressBar.class);
        doNothing().when(progress).setProgress(Mockito.anyInt());

        MovableFrameLayout view = mock(MovableFrameLayout.class);
        doReturn(progress).when(view).findViewById(Mockito.anyInt());

        Clip clip = mockClip();
        doNothing().when(clip, "setViewRecording");
        doReturn(view).when(clip, "getView");

        setInternalState(clip, "startRecordingTime", 1L);

        clip.updateView();

        verify(progress, never()).setProgress(Mockito.anyInt());
        verifyPrivate(clip).invoke("setViewRecording");
        assertEquals(1L, Whitebox.getInternalState(clip, "startRecordingTime"));
    }

    @Test
    public void addViewOnWindow_スクリーンショットを撮影中() throws Exception {
        Activity currentActivity = mock(Activity.class);

        mockStatic(Clip.class);
        doReturn(currentActivity).when(Clip.class, "getCurrentActivity");

        Clip clip = mockClip();
        doNothing().when(clip, "superAddViewOnWindow");
        setInternalState(clip, "capturing", true);

        clip.addViewOnWindow();

        verifyPrivate(clip, never()).invoke("superAddViewOnWindow");
    }

    @Test
    public void addViewOnWindow_実行される() throws Exception {
        Activity currentActivity = mock(Activity.class);

        mockStatic(Clip.class);
        doReturn(currentActivity).when(Clip.class, "getCurrentActivity");

        Clip clip = mockClip();
        doNothing().when(clip, "superAddViewOnWindow");
        setInternalState(clip, "capturing", false);

        clip.addViewOnWindow();

        verifyPrivate(clip).invoke("superAddViewOnWindow");
    }

    @Test
    public void removeFromWindowIfShowing() throws Exception {
        Clip clip = mockClip();
        doNothing().when(clip, "superRemoveFromWindowIfShowing");
        clip.removeFromWindowIfShowing();
        verifyPrivate(clip).invoke("superRemoveFromWindowIfShowing");
    }
}
