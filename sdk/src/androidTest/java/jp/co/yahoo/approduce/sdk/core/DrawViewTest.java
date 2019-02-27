package jp.co.yahoo.appfeedback.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;


/**
 * Created by tsando on 2017/08/26.
 */
@RunWith(AndroidJUnit4.class)
public class DrawViewTest {
    @Before
    public void setUp() throws Exception {
        // 静的変数の初期化
        Field field = DrawView.class.getDeclaredField("drawView");
        field.setAccessible(true);
        field.set(null, null);
        field = DrawView.class.getDeclaredField("bitmapCahce");
        field.setAccessible(true);
        field.set(null, null);
    }

    @Test
    public void getInstanceAtNotExistInstance() throws Exception {
        DrawView drawView = DrawView.getInstance(InstrumentationRegistry.getContext());
        assertNotNull(drawView);

        // 初期化フィールドのテスト
        Field field = DrawView.class.getDeclaredField("paint");
        field.setAccessible(true);
        Paint paint = (Paint)field.get(drawView);
        assertEquals(Paint.Style.STROKE, paint.getStyle());
    }

    @Test
    public void getInstanceAtExistInstance() throws Exception {
        // 定義済みのDrawView.drawViewを取ってくるためにgetInstanceを二度実行する
        DrawView drawView = DrawView.getInstance(InstrumentationRegistry.getContext());
        drawView = DrawView.getInstance(InstrumentationRegistry.getContext());
        assertNotNull(drawView);

        // 初期化フィールドのテスト
        Field field = DrawView.class.getDeclaredField("paint");
        field.setAccessible(true);
        Paint paint = (Paint)field.get(drawView);
        assertEquals(Paint.Style.STROKE, paint.getStyle());
    }

    @Test
    public void finish() throws Exception {
        DrawView drawView = this.createInstance();

        drawView.finish();
        Field field = DrawView.class.getDeclaredField("drawView");
        field.setAccessible(true);
        assertNull((DrawView)field.get(drawView));
        field = DrawView.class.getDeclaredField("bitmapCahce");
        field.setAccessible(true);
        assertNull((Bitmap)field.get(drawView));
    }

    @Test
    public void clearDrawRects() throws Exception {
        DrawView drawView = this.createInstance();

        Field field = DrawView.class.getDeclaredField("drawRects");
        field.setAccessible(true);
        ((LinkedList<RectF>)field.get(drawView)).add(new RectF(1, 1, 2, 2));
        drawView.clearDrawRects();
        assertEquals(0, ((LinkedList<RectF>)field.get(drawView)).size());
    }

    @Test
    public void save() throws Exception {
        DrawView drawView = this.createInstance();

        Field field = DrawView.class.getDeclaredField("drawCache");
        field.setAccessible(true);
        LinkedList<RectF> drawCache = (LinkedList<RectF>)field.get(drawView);
        drawCache.add(new RectF(1, 1, 2, 2));

        field = DrawView.class.getDeclaredField("drawRects");
        field.setAccessible(true);
        LinkedList<RectF> drawRects = (LinkedList<RectF>)field.get(drawView);
        drawRects.add(new RectF(2, 2, 3, 3));
        drawRects.add(new RectF(3, 3, 4, 4));

        drawView.save();
        assertTrue(Arrays.equals(drawCache.toArray(), drawRects.toArray()));
    }

    @Test
    public void reset() throws Exception {
        DrawView drawView = this.createInstance();

        Field field = DrawView.class.getDeclaredField("drawCache");
        field.setAccessible(true);
        LinkedList<RectF> drawCache = (LinkedList<RectF>)field.get(drawView);
        drawCache.add(new RectF(1, 1, 2, 2));

        field = DrawView.class.getDeclaredField("drawRects");
        field.setAccessible(true);
        LinkedList<RectF> drawRects = (LinkedList<RectF>)field.get(drawView);
        drawRects.add(new RectF(2, 2, 3, 3));
        drawRects.add(new RectF(3, 3, 4, 4));

        drawView.reset();
        assertTrue(Arrays.equals(drawCache.toArray(), drawRects.toArray()));
    }

    @Test
    public void getBitmap() throws Exception {
        DrawView drawView = this.createInstance();
        assertNull(drawView.getBitmap());

        Field field = DrawView.class.getDeclaredField("bitmapCahce");
        field.setAccessible(true);
        field.set(drawView, Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888));
        assertNotNull(drawView.getBitmap());
    }

    @Test
    public void setBitmapAtExistDrawingCache() throws Exception {
        // getDrawingCacheは描画イベントがおこらないとNULLを返すのでスタブ化する
        // PowerMockitoとandroidruntimeの相性が悪いため、Mockitoのスパイを使う
        DrawView drawView = spy(this.createInstance());
        doReturn(Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)).when(drawView).getDrawingCache();

        Method method = DrawView.class.getDeclaredMethod("setBitmap");
        method.setAccessible(true);
        method.invoke(drawView);

        Field field = DrawView.class.getDeclaredField("bitmapCahce");
        field.setAccessible(true);
        assertNotNull((Bitmap)field.get(drawView));
    }

    @Test
    public void setBitmapAtNotExistDrawingCache() throws Exception {
        DrawView drawView = this.createInstance();

        Method method = DrawView.class.getDeclaredMethod("setBitmap");
        method.setAccessible(true);
        method.invoke(drawView);

        Field field = DrawView.class.getDeclaredField("bitmapCahce");
        field.setAccessible(true);
        assertNull((Bitmap)field.get(drawView));
    }

    @Test
    public void isDurringDrawing() throws Exception {
        DrawView drawView = this.createInstance();

        assertFalse(drawView.isDurringDrawing());

        Field field = DrawView.class.getDeclaredField("drawRects");
        field.setAccessible(true);
        ((LinkedList<RectF>)field.get(drawView)).add(new RectF(1, 1, 2, 2));
        assertTrue(drawView.isDurringDrawing());
    }

    @Test
    public void setListener() throws Exception {
        DrawView drawView = this.createInstance();

        Field field = DrawView.class.getDeclaredField("drawViewNotify");
        field.setAccessible(true);
        assertNull((DrawViewNotify)field.get(drawView));

        DrawActivity drawActivity = mock(DrawActivity.class);
        drawView.setListener(drawActivity);
        assertNotNull((DrawViewNotify)field.get(drawView));
    }

    @Test
    public void removeListener() throws Exception {
        DrawView drawView = this.createInstance();

        Field field = DrawView.class.getDeclaredField("drawViewNotify");
        field.setAccessible(true);
        field.set(drawView, mock(DrawActivity.class));

        drawView.removeListener();
        assertNull((DrawViewNotify)field.get(drawView));
    }

    @Test
    public void calcRectFArgument() throws Exception {
        DrawView drawView = this.createInstance();
        Method method = DrawView.class.getDeclaredMethod("calcRectFArgument", PointF.class, PointF.class);
        method.setAccessible(true);

        // 長方形の位置情報
        final float RECT_LEFT = 10.0F;
        final float RECT_TOP = 10.0F;
        final float RECT_RIGHT = 100.0F;
        final float RECT_BUTTOM = 100.0F;

        // いずれのテストケースでも次の形で生成したRectF変数が返される
        RectF expected = new RectF(RECT_LEFT, RECT_TOP, RECT_RIGHT, RECT_BUTTOM);

        // 想定されるテストケース(スワイプした長方形の始点と終点の組み合わせ)
        final float[][] testCases = {
                { RECT_LEFT, RECT_TOP, RECT_RIGHT, RECT_BUTTOM },  // 始点:左上, 終点:右下
                { RECT_RIGHT, RECT_BUTTOM, RECT_LEFT, RECT_TOP },  // 始点:右下, 終点:左上
                { RECT_LEFT, RECT_BUTTOM, RECT_RIGHT, RECT_TOP },  // 始点:左下, 終点:右上
                { RECT_RIGHT, RECT_TOP, RECT_LEFT, RECT_BUTTOM }  // 始点:右上, 終点:左下
        };

        // 全てのテストケースをassertする
        for (float[] testCase: testCases) {
            PointF start = new PointF(testCase[0], testCase[1]);
            PointF end = new PointF(testCase[2], testCase[3]);
            RectF actual = (RectF)method.invoke(drawView, start, end);
            assertEquals(expected, actual);
        }
    }

    /**
     * privateコンストラクタからインスタンスを生成して返す
     * @return drawView DrawView
     */
    @NonNull
    private DrawView createInstance() throws Exception {
        Class<?>[] params = {Context.class};
        Constructor<DrawView> constructor = DrawView.class.getDeclaredConstructor(params);
        constructor.setAccessible(true);
        return constructor.newInstance(InstrumentationRegistry.getContext());
    }
}