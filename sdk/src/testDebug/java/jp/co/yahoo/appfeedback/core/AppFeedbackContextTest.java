package jp.co.yahoo.appfeedback.core;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.media.projection.MediaProjection;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 * Created by taicsuzu on 2017/04/27.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({AppFeedbackContext.class})
public class AppFeedbackContextTest {
    Activity activity;
    WindowManager wm;
    File cacheDir;
    Display display;

    @TargetApi(17)
    private AppFeedbackContext initAppFeedbackContext() throws Exception{
        final Point point = mock(Point.class);
        point.x = 10;
        point.y = 20;
        whenNew(Point.class).withAnyArguments().thenReturn(point);

        wm  = mock(WindowManager.class);
        cacheDir = mock(File.class);
        activity = mock(Activity.class);
        display = mock(Display.class);

        doReturn(wm).when(activity).getWindowManager();
        doReturn(cacheDir).when(activity).getCacheDir();
        doReturn(display).when(wm).getDefaultDisplay();
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                point.x = 30;
                point.y = 40;
                return null;
            }
        }).when(display).getRealSize(point);

        return spy(new AppFeedbackContext(activity));
    }

    @Test
    public void getCacheDir() throws Exception {
        AppFeedbackContext context = initAppFeedbackContext();
        assertEquals(cacheDir, context.getCacheDir());
    }

    @Test
    public void getScreenWidth() throws Exception {
        AppFeedbackContext context = initAppFeedbackContext();
        assertEquals(30, context.getScreenWidth());
    }

    @Test
    public void getScreenHeight() throws Exception {
        AppFeedbackContext context = initAppFeedbackContext();
        assertEquals(40, context.getScreenHeight());
    }

    @Test
    @TargetApi(21)
    public void setupVirtualDisplay() throws Exception {
        AppFeedbackContext appFeedbackContext = initAppFeedbackContext();
        MediaProjection mockMediaProjection = mock(MediaProjection.class);
        Context mockContext = mock(Context.class);
        Resources mockResources = mock(Resources.class);
        DisplayMetrics mockDisplayMetrics = mock(DisplayMetrics.class);

        mockDisplayMetrics.densityDpi = 10;

        doReturn(mockMediaProjection).when(appFeedbackContext, "getMediaProjection", Mockito.any());
        doReturn(mockDisplayMetrics).when(mockResources).getDisplayMetrics();
        doReturn(mockResources).when(mockContext).getResources();

        appFeedbackContext.setupVirtualDisplay(mockContext, "name", null, null);

        verify(mockMediaProjection).createVirtualDisplay("name", 30, 40, 10, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR|DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, null, null, null);
    }
}
