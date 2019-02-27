package jp.co.yahoo.appfeedback.core;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.util.SparseIntArray;
import android.view.Surface;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;

import jp.co.yahoo.appfeedback.TestUtil;
import jp.co.yahoo.appfeedback.utils.SecCounter;
import jp.co.yahoo.appfeedback.views.TapViewParent;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import static org.powermock.reflect.internal.WhiteboxImpl.getInternalState;
import static org.powermock.reflect.internal.WhiteboxImpl.setInternalState;

/**
 * Created by taicsuzu on 2017/05/02.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({ScreenRecorder.class, AppFeedback.class, ContextCompat.class, SecCounter.class})
public class ScreenRecorderTest {

    void setupSparseIntArray() throws Exception {
        SparseIntArray array = mock(SparseIntArray.class);
        doNothing().when(array).append(anyInt(), anyInt());
        whenNew(SparseIntArray.class).withAnyArguments().thenReturn(array);
    }

    ScreenRecorder mockRecorder(Context context, int apiLevel, boolean canUseMP, int checkPermission, boolean isRecording) throws Exception {
        TestUtil.setFinalStatic(Build.VERSION.class.getDeclaredField("SDK_INT"), apiLevel);

        setupSparseIntArray();

        ScreenRecorder screenRecorder = spy(new ScreenRecorder());

        mockStatic(AppFeedback.class);
        doReturn(canUseMP).when(AppFeedback.class, "canUseMediaProjectionAPI");

        mockStatic(ContextCompat.class);
        doReturn(checkPermission).when(ContextCompat.class, "checkSelfPermission", context, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        setInternalState(screenRecorder, "isRecording", isRecording);

        return screenRecorder;
    }

    @Test
    public void canRecord_録画可能_パーミッション獲得済み() throws Exception {
        Context context = mock(Context.class);
        ScreenRecorder screenRecorder = mockRecorder(context, 23, true, PackageManager.PERMISSION_GRANTED, false);
        assertEquals(true, screenRecorder.canRecord(context));
    }

    @Test
    public void canRecord_録画可能_パーミッション必要なし() throws Exception {
        Context context = mock(Context.class);
        ScreenRecorder screenRecorder = mockRecorder(context, 21, true, PackageManager.PERMISSION_DENIED, false);
        assertEquals(true, screenRecorder.canRecord(context));
    }

    @Test
    public void canRecord_MediaProjection利用不可_APILEVELが低いため() throws Exception {
        Context context = mock(Context.class);
        ScreenRecorder screenRecorder = mockRecorder(context, 15, true, PackageManager.PERMISSION_GRANTED, false);
        assertEquals(false, screenRecorder.canRecord(context));
    }

    @Test
    public void canRecord_MediaProjection利用不可_拒否されたため() throws Exception {
        Context context = mock(Context.class);
        ScreenRecorder screenRecorder = mockRecorder(context, 23, false, PackageManager.PERMISSION_GRANTED, false);
        assertEquals(false, screenRecorder.canRecord(context));
    }

    @Test
    public void canRecord_MediaProjection利用不可_Write権限がないため() throws Exception {
        Context context = mock(Context.class);
        ScreenRecorder screenRecorder = mockRecorder(context, 23, true, PackageManager.PERMISSION_DENIED, false);
        assertEquals(false, screenRecorder.canRecord(context));
    }

    @Test
    public void canRecord_MediaProjection利用不可_録画中のため() throws Exception {
        Context context = mock(Context.class);
        ScreenRecorder screenRecorder = mockRecorder(context, 23, true, PackageManager.PERMISSION_GRANTED, true);
        assertEquals(false, screenRecorder.canRecord(context));
    }

    HandlerThread thread;

    @TargetApi(21)
    private ScreenRecorder setupScreenRecorderForStartRecording() throws Exception{

        TestUtil.setFinalStatic(Build.VERSION.class.getDeclaredField("SDK_INT"), 23);

        VirtualDisplay display = mock(VirtualDisplay.class);

        AppFeedbackContext appFeedbackContext = mock(AppFeedbackContext.class);
        doReturn(display).when(appFeedbackContext).setupVirtualDisplay((Context) any(), anyString(), (Surface)any(), (Handler)any());

        Looper looper = mock(Looper.class);

        thread = mock(HandlerThread.class);
        doReturn(looper).when(thread).getLooper();
        whenNew(HandlerThread.class).withAnyArguments().thenReturn(thread);

        Handler handler = mock(Handler.class);
        whenNew(Handler.class).withAnyArguments().thenReturn(handler);

        MediaRecorder mediaRecorder = mock(MediaRecorder.class);
        doReturn(null).when(mediaRecorder).getSurface();

        setupSparseIntArray();

        ScreenRecorder screenRecorder = spy(new ScreenRecorder());
        doReturn(true).when(screenRecorder).canRecord((Context) any());
        doReturn(true).when(screenRecorder, "prepareMediaRecorder", (Context) any());
        setInternalState(screenRecorder, "appFeedbackContext", appFeedbackContext);
        setInternalState(screenRecorder, "mediaRecorder", mediaRecorder);

        return screenRecorder;
    }

    @Test
    public void startRecording_正常に終了() throws Exception {
        ScreenRecorder screenRecorder = setupScreenRecorderForStartRecording();
        screenRecorder.startRecording(null);

        verify(thread).start();
        assertEquals(false, getInternalState(screenRecorder, "hasData"));
    }

    @TargetApi(21)
    @Test
    public void stopRecording_正常に終了() throws Exception {
        TestUtil.setFinalStatic(Build.VERSION.class.getDeclaredField("SDK_INT"), 23);

        SecCounter secCounter = mock(SecCounter.class);

        mockStatic(SecCounter.class);
        doReturn(secCounter).when(SecCounter.class, "getInstance");

        MediaRecorder mediaRecorder = mock(MediaRecorder.class);
        VirtualDisplay display = mock(VirtualDisplay.class);
        HandlerThread thread = mock(HandlerThread.class);

        setupSparseIntArray();

        ScreenRecorder screenRecorder = spy(new ScreenRecorder());
        setInternalState(screenRecorder, "mediaRecorder", mediaRecorder);
        setInternalState(screenRecorder, "virtualDisplay", display);
        setInternalState(screenRecorder, "handlerThread", thread);

        screenRecorder.stopRecording();

        verify(mediaRecorder).stop();
        verify(mediaRecorder).reset();
        verify(display).release();
        verify(thread).quitSafely();

        assertEquals(true, getInternalState(screenRecorder, "hasData"));
        assertEquals(false, getInternalState(screenRecorder, "isRecording"));
    }

    @TargetApi(21)
    @Test
    public void stopRecording_ディレイが終了するよりも前に録画終了() throws Exception {
        TestUtil.setFinalStatic(Build.VERSION.class.getDeclaredField("SDK_INT"), 23);

        SecCounter secCounter = mock(SecCounter.class);

        mockStatic(SecCounter.class);
        doReturn(secCounter).when(SecCounter.class, "getInstance");

        MediaRecorder mediaRecorder = mock(MediaRecorder.class);
        VirtualDisplay display = mock(VirtualDisplay.class);
        HandlerThread thread = mock(HandlerThread.class);

        doThrow(new IllegalStateException()).when(mediaRecorder).stop();

        setupSparseIntArray();

        ScreenRecorder screenRecorder = spy(new ScreenRecorder());
        setInternalState(screenRecorder, "mediaRecorder", mediaRecorder);
        setInternalState(screenRecorder, "virtualDisplay", display);
        setInternalState(screenRecorder, "handlerThread", thread);

        screenRecorder.stopRecording();

        verify(mediaRecorder).stop();
        verify(mediaRecorder).reset();
        verify(display).release();
        verify(thread).quitSafely();

        assertEquals(false, getInternalState(screenRecorder, "hasData"));
        assertEquals(false, getInternalState(screenRecorder, "isRecording"));
    }

    @Test
    public void clean() throws Exception {
        File file = mock(File.class);
        whenNew(File.class).withAnyArguments().thenReturn(file);

        setupSparseIntArray();

        ScreenRecorder screenRecorder = spy(new ScreenRecorder());
        setInternalState(screenRecorder, "dataFilePath", "dummy_path");
        screenRecorder.clean();

        verify(file).delete();
        assertEquals(false, getInternalState(screenRecorder, "hasData"));
    }
}
