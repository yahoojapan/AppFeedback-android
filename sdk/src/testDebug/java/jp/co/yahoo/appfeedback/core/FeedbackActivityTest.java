package jp.co.yahoo.appfeedback.core;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.asm.tree.analysis.Frame;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import jp.co.yahoo.appfeedback.R;
import jp.co.yahoo.appfeedback.TestUtil;
import jp.co.yahoo.appfeedback.utils.PreferencesWrapper;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.verifyPrivate;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import static org.powermock.reflect.Whitebox.getInternalState;
import static org.powermock.reflect.Whitebox.invokeMethod;
import static org.powermock.reflect.Whitebox.setInternalState;

/**
 * Created by taicsuzu on 2017/05/02.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({FeedbackActivity.class, ScreenRecorder.class})
public class FeedbackActivityTest {

    ScreenRecorder mockRecorder(boolean hasData) throws Exception {
        SparseIntArray array = mock(SparseIntArray.class);
        doNothing().when(array).append(anyInt(), anyInt());
        whenNew(SparseIntArray.class).withAnyArguments().thenReturn(array);

        ScreenRecorder recorder = mock(ScreenRecorder.class);

        if(hasData) {
            doReturn(true).when(recorder).hasData();
            doReturn("dummy_path").when(recorder).getDataFilePath();
        }else{
            doReturn(false).when(recorder).hasData();
        }

        return recorder;
    }

    ScreenShot mockShot(boolean hasData, Bitmap bitmap) throws Exception {
        ScreenShot screenShot = mock(ScreenShot.class);

        if (hasData) {
            doReturn(bitmap).when(screenShot).getBitmap();
        } else {
            doReturn(null).when(screenShot).getBitmap();
        }

        return screenShot;
    }

    @Test
    public void setScreenshot_動画キャプチャあり() throws Exception {
        TestUtil.setFinalStatic(Build.VERSION.class.getDeclaredField("SDK_INT"), 23);

        ScreenRecorder mockRecorder = mockRecorder(true);

        FeedbackActivity activity = spy(new FeedbackActivity());
        doNothing().when(activity, "updateScreenshot");
        setInternalState(activity, "screenRecorder", mockRecorder);

        invokeMethod(activity, "setScreenshot");

        assertEquals(getInternalState(activity, "screenVideoFilePath"), "dummy_path");
        assertEquals(getInternalState(activity, "screenshotBitmap"), null);
    }

    @Test
    public void setScreenshot_動画キャプチャなしスクショあり() throws Exception {
        TestUtil.setFinalStatic(Build.VERSION.class.getDeclaredField("SDK_INT"), 23);

        Bitmap bitmap = mock(Bitmap.class);

        ScreenRecorder mockRecorder = mockRecorder(false);
        ScreenShot mockShot = mockShot(true, bitmap);

        FeedbackActivity activity = spy(new FeedbackActivity());
        doNothing().when(activity, "updateScreenshot");
        setInternalState(activity, "screenRecorder", mockRecorder);
        setInternalState(activity, "screenShot", mockShot);

        invokeMethod(activity, "setScreenshot");

        assertEquals(getInternalState(activity, "screenVideoFilePath"), null);
        assertEquals(getInternalState(activity, "screenshotBitmap"), bitmap);
    }

    @Test
    public void setScreenshot_動画キャプチャなしスクショなし() throws Exception {
        TestUtil.setFinalStatic(Build.VERSION.class.getDeclaredField("SDK_INT"), 23);

        ScreenRecorder mockRecorder = mockRecorder(false);
        ScreenShot mockShot = mockShot(false, null);

        FeedbackActivity activity = spy(new FeedbackActivity());
        doNothing().when(activity, "updateScreenshot");
        setInternalState(activity, "screenRecorder", mockRecorder);
        setInternalState(activity, "screenShot", mockShot);

        invokeMethod(activity, "setScreenshot");

        assertEquals(getInternalState(activity, "screenVideoFilePath"), null);
        assertEquals(getInternalState(activity, "screenshotBitmap"), null);
    }

    @Test
    public void setScreenshot_APIレベルが低くスクショが設定されない() throws Exception {
        TestUtil.setFinalStatic(Build.VERSION.class.getDeclaredField("SDK_INT"), 10);

        FeedbackActivity activity = spy(new FeedbackActivity());
        doNothing().when(activity, "updateScreenshot");

        invokeMethod(activity, "setScreenshot");

        assertEquals(getInternalState(activity, "screenVideoFilePath"), null);
        assertEquals(getInternalState(activity, "screenshotBitmap"), null);
    }

    VideoView screenVideoView;
    TextView emptyScreenshot;
    ImageButton removeScreenshot;
    ImageView screenshotImage;
    ImageView editedImage;
    FrameLayout editButton;

    void setState(FeedbackActivity activity, String videoPath, Bitmap screenShot) {
        screenVideoView = mock(VideoView.class);
        emptyScreenshot = mock(TextView.class);
        removeScreenshot = mock(ImageButton.class);
        screenshotImage = mock(ImageView.class);
        editedImage = mock(ImageView.class);
        editButton = mock(FrameLayout.class);

        setInternalState(activity, "screenVideoView", screenVideoView);
        setInternalState(activity, "screenshotImage", screenshotImage);
        setInternalState(activity, "removeScreenshot", removeScreenshot);
        setInternalState(activity, "emptyScreenshot", emptyScreenshot);
        setInternalState(activity, "editedImage", editedImage);
        setInternalState(activity, "editButton", editButton);
        setInternalState(activity, "screenVideoFilePath", videoPath);
        setInternalState(activity, "screenshotBitmap", screenShot);
    }

    @Test
    public void updateScreenshot_動画キャプチャあり() throws Exception {
        FeedbackActivity activity = spy(new FeedbackActivity());
        setState(activity, "dummy_path", null);

        invokeMethod(activity, "updateScreenshot");

        verify(screenVideoView).setVisibility(View.VISIBLE);
        verify(emptyScreenshot).setVisibility(View.INVISIBLE);
        verify(removeScreenshot).setVisibility(View.VISIBLE);
        verify(editedImage).setVisibility(View.INVISIBLE);
        verify(editButton).setVisibility(View.INVISIBLE);
        verify(screenshotImage).setImageDrawable(null);

    }

    @Test
    public void updateScreenshot_動画キャプチャなしスクショあり() throws Exception {
        Bitmap bitmap = mock(Bitmap.class);

        FeedbackActivity activity = spy(new FeedbackActivity());
        setState(activity, null, bitmap);

        invokeMethod(activity, "updateScreenshot");

        verify(screenVideoView).setVisibility(View.INVISIBLE);
        verify(emptyScreenshot).setVisibility(View.INVISIBLE);
        verify(removeScreenshot).setVisibility(View.VISIBLE);
        verify(editButton).setVisibility(View.VISIBLE);
        verify(screenshotImage).setImageBitmap(bitmap);
    }

    @Test
    public void updateScreenshot_動画キャプチャなしスクショなし() throws Exception {
        FeedbackActivity activity = spy(new FeedbackActivity());
        setState(activity, null, null);

        invokeMethod(activity, "updateScreenshot");

        verify(screenVideoView).setVisibility(View.INVISIBLE);
        verify(emptyScreenshot).setVisibility(View.VISIBLE);
        verify(removeScreenshot).setVisibility(View.INVISIBLE);
        verify(editedImage).setVisibility(View.INVISIBLE);
        verify(editButton).setVisibility(View.INVISIBLE);
        verify(screenshotImage).setImageDrawable(null);
    }

    @Test
    public void removeScreenshot_動画キャプチャあり() throws Exception {
        FeedbackActivity activity = spy(new FeedbackActivity());
        doNothing().when(activity, "updateScreenshot");
        setState(activity, "dummy_path", null);

        invokeMethod(activity, "removeScreenshot", any());

        verify(screenVideoView).pause();
    }

    @Test
    public void removeScreenshot_スクショあり() throws Exception {
        FeedbackActivity activity = spy(new FeedbackActivity());
        doNothing().when(activity, "updateScreenshot");

        Bitmap bitmap = mock(Bitmap.class);

        setState(activity, null, bitmap);

        invokeMethod(activity, "removeScreenshot", any());

        verify(bitmap).recycle();
    }

    @Test
    public void startRecording_確認が必要() throws Exception {
        FeedbackActivity activity = spy(new FeedbackActivity());

        View okButton, cancelButton;
        CheckBox check;

        okButton = mock(View.class);
        cancelButton = mock(View.class);
        check = mock(CheckBox.class);

        Dialog dialog = mock(Dialog.class);

        doReturn(okButton).when(dialog).findViewById(R.id.appfeedback_dialog_recording_start);
        doReturn(cancelButton).when(dialog).findViewById(R.id.appfeedback_dialog_recording_cancel);
        doReturn(check).when(dialog).findViewById(R.id.appfeedback_dialog_record_confirm);

        whenNew(Dialog.class).withAnyArguments().thenReturn(dialog);

        PreferencesWrapper pw = mock(PreferencesWrapper.class);
        // 確認が必要
        doReturn(false).when(pw).getRecordConfirmed();

        setInternalState(activity, "preferencesWrapper", pw);

        activity.startRecording(null);

        verify(dialog).show();
    }

    @Test
    public void startRecording_確認は必要ない() throws Exception {
        FeedbackActivity activity = spy(new FeedbackActivity());
        doNothing().when(activity, "startRecording");

        PreferencesWrapper pw = mock(PreferencesWrapper.class);
        // 確認が必要
        doReturn(true).when(pw).getRecordConfirmed();

        setInternalState(activity, "preferencesWrapper", pw);

        activity.startRecording(null);

        verifyPrivate(activity).invoke("startRecording");
    }
}
