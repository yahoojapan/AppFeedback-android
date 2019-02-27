package jp.co.yahoo.appfeedback.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.LinkedList;

/**
 * 描画機能を持つカスタムビュー
 * ユーザのスワイプ操作を検知して強調枠線の情報を保存・表示する
 *
 * Created by tsando on 2017/08/15.
 */
public class DrawView extends View {
    // 枠線の太さ
    private static final int STROKE_WIDTH = 5;
    // 枠線の描画情報
    private LinkedList<RectF> drawRects = new LinkedList<RectF>();
    private LinkedList<RectF> drawCache = new LinkedList<RectF>();
    private Paint paint;
    // スワイプ中の座標を記憶する
    private PointF startPoint;
    private PointF currentPoint;
    // Activityに描画を通知するリスナー
    private DrawViewNotify drawViewNotify;
    // クラスをシングルトンにする
    private static DrawView drawView;
    private static Bitmap bitmapCahce;

    /**
     * シングルトンのインスタンスを得る
     * @param context
     * @return
     */
    public static DrawView getInstance(Context context) {
        if(drawView == null) {
            drawView = new DrawView(context.getApplicationContext());
        }
        return drawView;
    }

    /**
     * コンストラクタ
     * @param context
     */
    private DrawView(Context context) {
        this(context, null);
    }

    /**
     * コンストラクタ
     * @param context
     * @param attr
     */
    private DrawView(Context context, AttributeSet attr) {
        super(context,attr);

        // 描画情報のキャッシュを取るように設定
        this.setDrawingCacheEnabled(true);

        // フィールドの初期化
        this.paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(this.STROKE_WIDTH);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
    }

    /**
     * 描画命令に応じてdrawRectsに格納された強調枠線を描画する
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // スワイプ中の枠線を描画
        if(this.currentPoint != null) {
            RectF rectF = this.calcRectFArgument(this.startPoint, this.currentPoint);
            canvas.drawRect(rectF, paint);
        }

        // スワイプ済みの枠線を描画
        for(RectF drawRect: drawRects) {
            canvas.drawRect(drawRect, paint);
        }
    }

    /**
     * 二つのPointF変数を渡すと、RectF変数を生成して返す
     * @param startPoint PointF
     * @param endPoint PointF
     * @return RectF
     */
    private RectF calcRectFArgument(PointF startPoint, PointF endPoint) {
        float left;
        float right;
        float top;
        float bottom;

        if(startPoint.x < endPoint.x) {
            left = startPoint.x;
            right = endPoint.x;
        } else {
            left = endPoint.x;
            right = startPoint.x;
        }
        if(startPoint.y < endPoint.y) {
            top = startPoint.y;
            bottom = endPoint.y;
        } else {
            top = endPoint.y;
            bottom = startPoint.y;
        }

        return new RectF(left, top, right, bottom);
    }

    /**
     * ユーザーのタッチアクションに応じて強調枠線の保存処理を行う
     * @param e MotionEvent
     * @return boolean
     */
    public boolean onTouchEvent(MotionEvent e) {
        switch(e.getAction()) {
            // タッチオン
            case MotionEvent.ACTION_DOWN:
                this.startPoint = new PointF(e.getX(), e.getY());
                break;
            // スワイプ
            case MotionEvent.ACTION_MOVE:
                this.currentPoint = new PointF(e.getX(), e.getY());
                invalidate();
                break;
            // タッチアウト
            case MotionEvent.ACTION_UP:
                if(this.currentPoint != null) {
                    RectF rectF = this.calcRectFArgument(this.startPoint, this.currentPoint);
                    this.drawRects.add(rectF);
                    this.startPoint = null;
                    this.currentPoint = null;
                    invalidate();
                    // 描画情報が更新されたことを通知
                    this.drawViewNotify.drawViewUpdate();
                }
                break;
            default:
                break;
        }

        return true;
    }

    /**
     * drawViewの終了処理
     * インスタンス静的を削除する
     */
    public void finish() {
        drawView = null;
        bitmapCahce = null;
        this.setDrawingCacheEnabled(false);
    }

    /**
     * クリアボタンが押されたときの動作
     */
    public void clearDrawRects() {
        drawRects.clear();
        invalidate();
    }

    /**
     * 保存ボタンが押されたときの動作
     */
    public void save() {
        drawCache.clear();
        drawCache.addAll(drawRects);
        setBitmap();
    }

    /**
     * 描画途中の情報をリセットする
     */
    public void reset() {
        drawRects.clear();
        drawRects.addAll(drawCache);
    }

    /**
     * 描画内容をBitmap形式で返す
     * @return Bitmap
     */
    public Bitmap getBitmap() {
        return bitmapCahce;
    }

    /**
     * キャッシュを保存する
     */
    private void setBitmap() {
        Bitmap bitmap = this.getDrawingCache();
        if(bitmap != null) {
            bitmapCahce = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        }
    }

    /**
     * 描画中のデータがあるかどうかを返す
     * @return Boolean
     */
    public boolean isDurringDrawing() {
        if(drawRects != null && drawRects.toArray().length != 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * イベントリスナの登録
     * @param listener (DrawActivityのインスタンスが渡される)
     */
    public void setListener(DrawViewNotify listener) {
        this.drawViewNotify = listener;
    }

    /**
     * イベントリスナの削除
     */
    public void removeListener() {
        this.drawViewNotify = null;
    }
}
