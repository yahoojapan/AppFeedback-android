package jp.co.yahoo.appfeedback.core;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.nio.ByteBuffer;

import jp.co.yahoo.appfeedback.TestUtil;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import static org.powermock.reflect.Whitebox.getInternalState;
import static org.powermock.reflect.Whitebox.setInternalState;

/**
 * Created by taicsuzu on 2017/05/02.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({ScreenShot.class, ImageReader.class, AppFeedback.class, Bitmap.class})
public class ScreenShotTest {

    void setupAppFeedbackContext() throws Exception {
        AppFeedbackContext appFeedbackContext = mock(AppFeedbackContext.class);
        mockStatic(AppFeedback.class);
        doReturn(appFeedbackContext).when(AppFeedback.class, "getAppFeedbackContext");
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Test
    public void start() throws Exception {
        Handler handler = mock(Handler.class);
        whenNew(Handler.class).withAnyArguments().thenReturn(handler);

        HandlerThread thread = mock(HandlerThread.class);
        whenNew(HandlerThread.class).withAnyArguments().thenReturn(thread);

        ImageReader reader = mock(ImageReader.class);

        mockStatic(ImageReader.class);
        doReturn(reader).when(ImageReader.class, "newInstance", anyInt(), anyInt(), eq(PixelFormat.RGBA_8888), eq(2));

        setupAppFeedbackContext();

        ScreenShot screenShot = spy(new ScreenShot());
        screenShot.start(null);

        assertEquals(true, getInternalState(screenShot, "isRunning"));
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Test
    public void stop() throws Exception {
        VirtualDisplay virtualDisplay = mock(VirtualDisplay.class);
        HandlerThread handlerThread = mock(HandlerThread.class);
        ImageReader imageReader = mock(ImageReader.class);

        setupAppFeedbackContext();

        ScreenShot screenShot = spy(new ScreenShot());
        setInternalState(screenShot, "virtualDisplay", virtualDisplay);
        setInternalState(screenShot, "handlerThread", handlerThread);
        setInternalState(screenShot, "imageReader", imageReader);

        screenShot.stop();

        verify(virtualDisplay).release();
        verify(handlerThread).quitSafely();
        verify(imageReader).close();

        assertEquals(false, getInternalState(screenShot, "isRunning"));
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Test
    public void onImageAvailable_正常() throws Exception {
        ImageReader imageReader = mock(ImageReader.class);
        Image image = mock(Image.class);
        Image.Plane plane = mock(Image.Plane.class);

        ByteBuffer byteBuffer = mock(ByteBuffer.class);

        doReturn(image).when(imageReader).acquireLatestImage();
        doReturn(new Image.Plane[]{plane}).when(image).getPlanes();
        doReturn(byteBuffer).when(plane).getBuffer();

        setupAppFeedbackContext();

        ScreenShot screenShot = spy(new ScreenShot());
        setInternalState(screenShot, "isRunning", true);
        screenShot.onImageAvailable(imageReader);

        verify(image).getPlanes();
        verify(plane).getBuffer();
        verify(image).close();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Test
    public void onImageAvailable_実行中じゃない() throws Exception {
        ImageReader imageReader = mock(ImageReader.class);
        Image image = mock(Image.class);
        Image.Plane plane = mock(Image.Plane.class);

        ByteBuffer byteBuffer = mock(ByteBuffer.class);

        doReturn(image).when(imageReader).acquireLatestImage();
        doReturn(new Image.Plane[]{plane}).when(image).getPlanes();
        doReturn(byteBuffer).when(plane).getBuffer();

        setupAppFeedbackContext();

        ScreenShot screenShot = spy(new ScreenShot());
        setInternalState(screenShot, "isRunning", false);
        screenShot.onImageAvailable(imageReader);

        verify(image, never()).getPlanes();
        verify(plane, never()).getBuffer();
        verify(image).close();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Test
    public void saveScreenShot_正常() throws Exception {
        TestUtil.setFinalStatic(Build.VERSION.class.getDeclaredField("SDK_INT"), 23);

        setupAppFeedbackContext();
        doReturn(true).when(AppFeedback.class, "canUseMediaProjectionAPI");

        Bitmap bitmap = mock(Bitmap.class);
        mockStatic(Bitmap.class);
        doReturn(bitmap).when(Bitmap.class, "createBitmap", anyInt(), anyInt(), eq(Bitmap.Config.ARGB_8888));

        ImageReader imageReader = mock(ImageReader.class);

        ScreenShot.ScreenShotData screenShotData = mock(ScreenShot.ScreenShotData.class);
        screenShotData.pixelStride = 10;
        doReturn(screenShotData).when(screenShotData).clone();

        ScreenShot screenShot = spy(new ScreenShot());
        setInternalState(ScreenShot.class, "ssd", screenShotData);
        setInternalState(screenShot, "imageReader", imageReader);

        assertEquals(true, screenShot.saveScreenShot());
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Test
    public void saveScreenShot_MediaProjectionが使用不可() throws Exception {
        TestUtil.setFinalStatic(Build.VERSION.class.getDeclaredField("SDK_INT"), 23);

        setupAppFeedbackContext();
        doReturn(false).when(AppFeedback.class, "canUseMediaProjectionAPI");

        ScreenShot screenShot = spy(new ScreenShot());

        assertEquals(false, screenShot.saveScreenShot());
    }

    @Test
    public void getBitmap_正常に取得() throws Exception {
        TestUtil.setFinalStatic(Build.VERSION.class.getDeclaredField("SDK_INT"), 23);

        Bitmap bitmap = mock(Bitmap.class);
        doReturn(false).when(bitmap).isRecycled();

        setupAppFeedbackContext();
        doReturn(true).when(AppFeedback.class, "canUseMediaProjectionAPI");

        ScreenShot screenShot = spy(new ScreenShot());
        setInternalState(screenShot, "screenShotBitmap", bitmap);

        assertEquals(bitmap, screenShot.getBitmap());
    }
}
