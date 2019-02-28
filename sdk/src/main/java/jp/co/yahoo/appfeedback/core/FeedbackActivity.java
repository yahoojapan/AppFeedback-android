package jp.co.yahoo.appfeedback.core;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import jp.co.yahoo.appfeedback.R;
import jp.co.yahoo.appfeedback.core.api.APIHandler;
import jp.co.yahoo.appfeedback.core.api.PostSlack;
import jp.co.yahoo.appfeedback.utils.PreferencesWrapper;

import static jp.co.yahoo.appfeedback.R.id.edit_view;

/**
 * フィードバック送信画面
 *
 * Created by taicsuzu on 2016/09/15.
 */
public class FeedbackActivity extends Activity {
    private class OutOfMemoryMediaContent extends Exception{}

    private static final int SELECT_SCREEN_SHOT = 1010;
    private static final int EDIT_SCREEN_SHOT = 2020;

    // アカウント名を管理
    // 送信時に保存して、次回から入力を省略できる
    private PreferencesWrapper preferencesWrapper;

    private EditText title, comment, username;
    private ImageView screenshotImage;
    private VideoView screenVideoView;
    private TextView emptyScreenshot;
    private ImageButton removeScreenshot;
    private FrameLayout editButton;

    private ImageView editedImage;

    // スクリーンショットのBitmap
    // nullの時はスクリーンショットはない
    private Bitmap screenshotBitmap;
    // 画面の動画キャプチャ(.mp4)へのファイルパス
    // nullの時は動画はない
    private String screenVideoFilePath;

    // screenshotはここから取得する
    private ScreenShot screenShot;

    // 描画されたものを表示する
    private DrawView drawView;
    private Bitmap drawBitmap;

    // 動画キャプチャはここから取得する
    private ScreenRecorder screenRecorder;

    private boolean sending = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.appfeedback_activity_feedback);

        preferencesWrapper = PreferencesWrapper.getInstance(this);

        drawView = DrawView.getInstance(this);

        emptyScreenshot = (TextView) findViewById(R.id.appfeedback_feedback_empty_screenshot);
        removeScreenshot = (ImageButton) findViewById(R.id.appfeedback_feedback_remove_image);
        screenshotImage = (ImageView) findViewById(R.id.appfeedback_feedback_image);
        screenVideoView = (VideoView) findViewById(R.id.appfeedback_feedback_video_view);
        editButton = (FrameLayout) findViewById(R.id.appfeedback_image_editing_mode);
        editedImage = (ImageView)findViewById(edit_view);

        title = (EditText) findViewById(R.id.appfeedback_feedback_title);
        comment = (EditText) findViewById(R.id.appfeedback_feedback_comment);
        username = (EditText) findViewById(R.id.appfeedback_feedback_username);
        username.setText(preferencesWrapper.getId());

        if (Build.VERSION.SDK_INT >= 21) {
            screenShot = ScreenShot.getInstance();
            screenRecorder = ScreenRecorder.getInstance();

            if (screenRecorder.canRecord(this)) {
                findViewById(R.id.appfeedback_feedback_recording_mode).setVisibility(View.VISIBLE);
            } else {
                // 動画キャプチャを取ることができない時はボタンを非表示にする
                findViewById(R.id.appfeedback_feedback_recording_mode).setVisibility(View.INVISIBLE);
            }
        } else {
            findViewById(R.id.appfeedback_feedback_recording_mode).setVisibility(View.INVISIBLE);
        }

        // 適切なスクリーンショットを設定する
        setScreenshot();
        editedImage.setImageBitmap(drawBitmap);
    }

    /**
     * screenRecorderとscreenShotからデータを取得する
     */
    private void setScreenshot() {
        if(Build.VERSION.SDK_INT >= 21) {
            if(screenRecorder.hasData()) {
                // 動画キャプチャがある時は.mp4ファイルへのパスを設定
                screenVideoFilePath = screenRecorder.getDataFilePath();
            }else{
                // スクリーンショットを取得
                // ただし、screenShot.getBitmap()はnullを返す時もある
                screenshotBitmap = screenShot.getBitmap();
            }
        }else{
            screenshotBitmap = null;
        }

        // 表示を切り替える
        updateScreenshot();
    }

    /**
     * 現在の状態(動画キャプチャとスクリーンショットは存在するか)に応じて表示を変える
     */
    private void updateScreenshot() {

        if(screenVideoFilePath != null) {
            // 動画キャプチャが存在している
            screenVideoView.setVisibility(View.VISIBLE);
            screenVideoView.setVideoPath(screenVideoFilePath);
            screenVideoView.start();
            screenVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setLooping(true);
                }
            });

            screenshotImage.setImageDrawable(null);
            emptyScreenshot.setVisibility(View.INVISIBLE);
            removeScreenshot.setVisibility(View.VISIBLE);
            editedImage.setVisibility(View.INVISIBLE);
            editButton.setVisibility(View.INVISIBLE);

        }else if(screenshotBitmap != null) {
            // スクリーンショットが存在している
            screenVideoView.setVisibility(View.INVISIBLE);
            screenshotImage.setImageBitmap(screenshotBitmap);
            emptyScreenshot.setVisibility(View.INVISIBLE);
            removeScreenshot.setVisibility(View.VISIBLE);
            editButton.setVisibility(View.VISIBLE);
        }else{
            // 動画キャプチャもスクリーンショットも存在しない
            screenshotImage.setImageDrawable(null);
            screenVideoView.setVisibility(View.INVISIBLE);
            emptyScreenshot.setVisibility(View.VISIBLE);
            removeScreenshot.setVisibility(View.INVISIBLE);
            editedImage.setVisibility(View.INVISIBLE);
            editButton.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * スクリーンショットを削除
     * @param view View
     */
    public void removeScreenshot(View view) {
        if(screenVideoFilePath != null) {
            screenVideoView.pause();
            screenVideoFilePath = null;
        }

        if(screenshotBitmap != null) {
            screenshotBitmap.recycle();
            screenshotBitmap = null;
        }

        updateScreenshot();
    }

    /**
     * FeedbackActivityを閉じる
     * @param view View
     */
    public void close(View view) {
        if(sending)
            return;
        this.drawBitmap = null;
        this.finish();
    }

    /**
     * フィードバックを送信
     * @param view View
     */
    public void send(View view) {

        if(sending)
            return;

        sending = true;

        final String _title = title.getText().toString();
        final String _comment = comment.getText().toString();
        final String _username = username.getText().toString();

        // 全ての入力項目は必須
        if(_title.length() == 0) {
            // タイトルが入力されていない
            Toast.makeText(this, getString(R.string.appfeedback_feedback_empty_title), Toast.LENGTH_LONG).show();
            sending = false;
            return;
        }else if(_comment.length() == 0) {
            // コメントが入力されていない
            Toast.makeText(this, getString(R.string.appfeedback_feedback_empty_comment), Toast.LENGTH_LONG).show();
            sending = false;
            return;
        }else if(username.length() == 0) {
            // アカウント名が入力されていない
            Toast.makeText(this, getString(R.string.appfeedback_feedback_empty_id), Toast.LENGTH_LONG).show();
            sending = false;
            return;
        }else{

            findViewById(R.id.appfeedback_feedback_progressbar).setVisibility(View.VISIBLE);

            // 現在入力中のIDを保存
            preferencesWrapper.setId(_username);

            // キーボードを隠す
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

            postMessage(_title, _comment, _username);
        }
    }

    private void postMessage(String _title, String _comment, String _username) {

        try {

            FeedbackContent content = new FeedbackContent();
            content.title = _title;
            content.message = _comment;
            content.account = _username;

            // スクショあり
            if (drawBitmap != null || screenshotBitmap != null) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                // 強調線あり画像がある
                if (drawBitmap != null) {
                    drawBitmap = Bitmap.createScaledBitmap(drawBitmap, screenshotBitmap.getWidth(), screenshotBitmap.getHeight(), true);
                    screenshotBitmap = screenshotBitmap.copy(Bitmap.Config.ARGB_8888, true);
                    Canvas canvas = new Canvas(screenshotBitmap);
                    canvas.drawBitmap(drawBitmap, 0, 0, null);
                }

                // 上も含め、スクショが存在する
                if (screenshotBitmap != null) {
                    screenshotBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                }

                content.screenshot = byteArrayOutputStream.toByteArray();
            }

            // 動画あり
            else if (screenVideoFilePath != null) {
                File file = new File(screenVideoFilePath);

                FileInputStream fileIn = new FileInputStream(file);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                byte[] buffer = new byte[4096];
                int read;

                while ((read = fileIn.read(buffer, 0, buffer.length)) > 0) {
                    byteArrayOutputStream.write(buffer, 0, read);
                }

                fileIn.close();

                content.screenshot = byteArrayOutputStream.toByteArray();
            }

            new PostSlack(AppFeedback.getSlackexApiUrl(), this, AppFeedback.getSlackChannel(), AppFeedback.getToken(), content).executeAsync(new SlackAPIHandler(this));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 通信終了
     * ダイアログを消してフラグをfalseに
     */
    private void finishPosting() {
        findViewById(R.id.appfeedback_feedback_progressbar).setVisibility(View.INVISIBLE);
        sending = false;
    }

    /**
     * キャプチャ開始ボタンをタップしたときに呼ばれる
     * @param view View
     */
    public void startRecording(View view) {

        if(preferencesWrapper.getRecordConfirmed()) {
            // 確認ダイアログが必要ない
            startRecording();
        }else{
            // 確認ダイアログが必要
            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.appfeedback_dialog_start_recording);
            dialog.setCancelable(true);
            dialog.show();

            // 開始ボタン
            dialog.findViewById(R.id.appfeedback_dialog_recording_start).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    startRecording();
                }
            });

            // キャンセルボタン
            dialog.findViewById(R.id.appfeedback_dialog_recording_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            // 今後確認が必要か
            final CheckBox confirm = ((CheckBox)dialog.findViewById(R.id.appfeedback_dialog_record_confirm));
            confirm.setChecked(false);
            confirm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    preferencesWrapper.setRecordConfirmed(isChecked);
                }
            });
        }
    }

    /**
     * キャプチャを開始する<b>
     * 開始と同時にActivityを閉じる
     */
    private void startRecording() {
        if(screenRecorder != null && screenRecorder.canRecord(this)) {
            screenRecorder.startRecording(this);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        // progress barがあれば非表示にする
        findViewById(R.id.appfeedback_feedback_progressbar).setVisibility(View.INVISIBLE);

        // screenShotが設定されていれば解除する
        if(screenshotImage != null)
            screenshotImage.setImageDrawable(null);

        if(screenshotBitmap != null) {
            try {
                // Bitmapをfreeする
                screenshotBitmap.recycle();
            }catch (Exception e) {}
        }

        if(drawBitmap != null) {
            try {
                // Bitmapをfreeする
                drawBitmap.recycle();
            }catch (Exception e) {}
        }

        DrawView.getInstance(this).finish();

        if(Build.VERSION.SDK_INT >= 21) {
            // レコード中でなければレコード済みのデータをcleanする
            if (!screenRecorder.isRecording()) {
                screenRecorder.clean();
            }
        }

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //　スクリーンショットを選択した時にここにくる
        if(requestCode == SELECT_SCREEN_SHOT) {

            if(resultCode != RESULT_OK) {
                return;
            }

            Uri imageUri = data == null ? null : data.getData();

            if(imageUri == null) {
                return;
            }

            try {
                byte[] imageBytes = getBytes(imageUri);
                // 正常に選択されていたらscreenshotBitmapをセットする
                screenshotBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                // 動画がある場合はなくす
                screenVideoFilePath = null;
                // プレビューのアップデート
                updateScreenshot();
                // スクリーンショット画像の更新
                ScreenShot.getInstance().setScreenShotBitmap(screenshotBitmap);

                // 枠線描画の情報を初期化する
                DrawView.getInstance(this).finish();
                this.editedImage.setImageBitmap(null);
            } catch (OutOfMemoryMediaContent outOfMemoryMediaContent) {
                Toast.makeText(this, "画像のサイズが大きすぎます", Toast.LENGTH_SHORT).show();
                outOfMemoryMediaContent.printStackTrace();
            } catch (IOException e) {
                Toast.makeText(this, "画像の取得にエラーが発生しました", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else if(requestCode == EDIT_SCREEN_SHOT) {
            editedImage.setVisibility(View.VISIBLE);
            drawBitmap = DrawView.getInstance(this).getBitmap();
            editedImage.setImageBitmap(drawBitmap);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * スクリーンショットをギャラリーから選択する
     * @param view View
     */
    public void selectScreenshot(View view) {
        // パーミッションの確認
        if(Build.VERSION.SDK_INT >= 21) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "設定からストレージの読み込みを許可してください", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
        }

        startActivityForResult(intent, SELECT_SCREEN_SHOT);
    }

    /**
     * スクリーンショットを編集する
     * @param view View
     */
    public void startEditing(View view) {
        if(screenshotBitmap != null) {
            Intent intent = new Intent(getApplication(), DrawActivity.class);
            startActivityForResult(intent, EDIT_SCREEN_SHOT);
        }
    }

    /**
     * 画像データのcontent uriからbyte[]に変換する
     * OutOfMemoryやIOExceptionはそのまま投げる
     * @param contentUri 画像のcontent uri
     * @return byte[]
     * @throws OutOfMemoryMediaContent メモリ不足時
     * @throws IOException 画像の取得失敗時
     */
    public byte[] getBytes(Uri contentUri) throws OutOfMemoryMediaContent, IOException {

        try {
            InputStream is = getContentResolver().openInputStream(contentUri);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int read;
            byte[] data = new byte[16384];

            while ((read = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, read);
            }

            buffer.flush();
            return buffer.toByteArray();
        } catch (OutOfMemoryError e) {
            throw new OutOfMemoryMediaContent();
        }
    }

    class SlackAPIHandler extends APIHandler {

        protected SlackAPIHandler(Context context) {
            super(context);
        }

        @Override
        public void handleJson(int apiResult, int statusCode, @Nullable JSONObject json) {
            try {
                if (apiResult == SUCCESS && json != null && !json.isNull("ok")) {
                    if (json.getBoolean("ok")) {
                        showSlackChannel(this.getSlackChannelNameFromResponse(json));
                    } else if (json.getString("error") != null) {
                        showSlackError(json.getString("error"));
                    } else {
                        showInternalServerError(statusCode);
                    }
                } else if (400 <= statusCode && statusCode < 500 && json != null && !json.isNull("message")) {
                    String errorMessage = json.getString("message");
                    showStandardError(errorMessage);
                } else {
                    showInternalServerError(statusCode);
                }
            }catch (JSONException e) {
                e.printStackTrace();
                showInternalServerError(statusCode);
            }
        }

        private String getSlackChannelNameFromResponse(JSONObject json) {
            JSONObject file = json.optJSONObject("file");
            if (file == null) {
                return null;
            }

            JSONObject shares = file.optJSONObject("shares");
            if (shares == null) {
                return null;
            }

            JSONObject pub = shares.optJSONObject("public");
            if (pub != null) {
                JSONArray channels = pub.optJSONArray(pub.keys().next());
                if (channels.length() < 1) {
                    return null;
                }

                JSONObject channel = channels.optJSONObject(0);
                if (channel == null) {
                    return null;
                }

                String channelName = channel.optString("channel_name");
                return channelName;
            }

            if (shares.has("private")) {
                return "private channel";
            }

            return null;
        }

        private void showSlackChannel(String channelName) {
            Toast.makeText(
                    FeedbackActivity.this,
                    getString(R.string.appfeedback_feedback_slack_channel, channelName),
                    Toast.LENGTH_LONG).show();
            finish();
        }

        private void showStandardError(String errorMessage) {
            Toast.makeText(
                    FeedbackActivity.this,
                    errorMessage,
                    Toast.LENGTH_LONG).show();
            finishPosting();
        }

        private void showSlackError(String message) {
            Toast.makeText(
                    FeedbackActivity.this,
                    getString(R.string.appfeedback_feedback_slack_error, message),
                    Toast.LENGTH_LONG).show();
            finishPosting();
        }

        private void showInternalServerError(int statusCode) {
            Toast.makeText(
                    FeedbackActivity.this,
                    getString(R.string.appfeedback_feedback_internal_server_error, statusCode),
                    Toast.LENGTH_LONG).show();
            finishPosting();
        }

    }
}
