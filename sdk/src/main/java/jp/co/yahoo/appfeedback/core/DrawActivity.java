package jp.co.yahoo.appfeedback.core;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import jp.co.yahoo.appfeedback.R;
import jp.co.yahoo.appfeedback.utils.PreferencesWrapper;

import static jp.co.yahoo.appfeedback.R.id.imageView;


/**
 * 画像編集画面
 *
 * Created by tsando on 2017/08/15.
 */
public class DrawActivity extends FragmentActivity implements DrawViewNotify {
    private LinearLayout rootLayout;
    private LinearLayout drawViewLayout;
    private ImageView editImage;
    private Button clearButton;
    private Bitmap originalImageBitmap;
    private ScreenShot originalScreenShot;
    private PreferencesWrapper preferencesWrapper;

    private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener;

    /**
     * 初期化処理
     * レンダリング終了のイベントを検知するインナークラスを定義し、その中でDrawViewを動的に生成している
     * DrawViewはスクリーンショットのViewにぴったり重なるように表示する必要があり、スクリーンショットのViewサイズは
     * レンダリング終了後でないと確定しないため
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.appfeedback_activity_draw);

        // フィールドの初期化
        rootLayout = (LinearLayout) findViewById(R.id.rootLayout);
        originalScreenShot = ScreenShot.getInstance();
        originalImageBitmap = originalScreenShot.getBitmap();
        editImage = (ImageView)findViewById(imageView);
        editImage.setImageBitmap(originalImageBitmap);
        drawViewLayout = (LinearLayout)findViewById(R.id.drawViewLayout);
        clearButton = (Button)findViewById(R.id.clearButton);

        // 描画途中の状態で再開した場合はクリアボタンをオンにする
        if(DrawView.getInstance(this).isDurringDrawing()) {
            clearButton.setEnabled(true);
        } else {
            clearButton.setEnabled(false);
        }

        // DrawViewにお絵描きされた時のリスナーを登録
        DrawView.getInstance(this).setListener(this);

        // レンダリング終了を検知するイベントリスナーを登録
        final FragmentActivity activity = this;
        onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            // 実際に表示されているスクリーンショットのサイズと、drawViewのサイズを揃える
            @Override
            public void onGlobalLayout() {
                Bitmap bitmap = originalScreenShot.getBitmap();
                double aspectRatio = this.getAspectRatio(bitmap);
                LinearLayout.LayoutParams drawViewLayoutParams;

                // スクリーンショットが縦向きか横向きかによって処理が異なる
                int bitmapWidth = bitmap.getWidth();
                int bitmapHeight = bitmap.getHeight();
                if(bitmapWidth <= bitmapHeight) {  // 縦向きの場合drawViewの横幅を縮小する
                    double imageWidth = (double)editImage.getHeight() * aspectRatio;
                    drawViewLayoutParams = new LinearLayout.LayoutParams(
                            (int)imageWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
                } else {  // 横向きの場合drawViewの縦幅を縮小する
                    double imageHeight = (double)editImage.getWidth() / aspectRatio;
                    drawViewLayoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, (int)imageHeight);
                }

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    rootLayout.getViewTreeObserver().removeGlobalOnLayoutListener(onGlobalLayoutListener);
                } else {
                    rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
                }

                DrawView.getInstance(activity).setLayoutParams(drawViewLayoutParams);
                drawViewLayout.addView(DrawView.getInstance(activity));
            }

            // bitmap画像のアスペクト比を計算する (横幅/縦幅)
            private double getAspectRatio(Bitmap bitmap) {
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();

                return (double)width / (double)height;
            }
        };
        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);

        preferencesWrapper = PreferencesWrapper.getInstance(this);
        if(!preferencesWrapper.getDrawConfirmed()) {
            // 使い方ダイアログが必要
            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.appfeedback_dialog_draw_tutorial);
            dialog.setCancelable(true);
            dialog.show();

            // 閉じるボタン
            dialog.findViewById(R.id.appfeedback_dialog_draw_tutorial_close).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            // 今後表示が必要か
            final CheckBox confirm = ((CheckBox) dialog.findViewById(R.id.appfeedback_dialog_draw_confirm));
            confirm.setChecked(false);
            confirm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    preferencesWrapper.setDrawConfirmed(isChecked);
                }
            });
        }
    }

    /**
     * Activityの終了処理
     */
    @Override
    protected void onDestroy() {
        originalImageBitmap = null;
        DrawView.getInstance(this).removeListener();
        super.onDestroy();
    }

    /**
     * 閉じるボタンを押した時の処理
     * @param view
     */
    public void close(View view) {
        DrawView.getInstance(this).reset();
        drawViewLayout.removeView(DrawView.getInstance(this));
        finish();
    }

    /**
     * 保存ボタンを押した時の処理
     * @param view
     */
    public void save(View view) {
        DrawView.getInstance(this).save();
        drawViewLayout.removeView(DrawView.getInstance(this));
        finish();
    }

    /**
     * クリアボタンを押した時の処理
     * @param view
     */
    public void clear(View view) {
        DrawView.getInstance(this).clearDrawRects();
        clearButton.setEnabled(false);
        return;
    }

    /**
     * drawViewの描画情報に更新があった場合クリアボタンを有効にする
     */
    public void drawViewUpdate() {
        clearButton.setEnabled(true);
    }
}
