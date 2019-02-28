package jp.co.yahoo.appfeedback.core.api;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.widget.Toast;

import org.json.JSONObject;

import jp.co.yahoo.appfeedback.R;

/**
 * APIで発行したリクエストを処理するHandlerを継承した抽象クラス
 * エラーメッセージをToastで表示するなどの共通処理が入っている
 *
 * Created by taicsuzu on 2017/04/20.
 */
abstract public class APIHandler extends Handler {
    protected static final int SUCCESS = API.SUCCESS, FAILURE = API.FAILURE;
    protected Context context;

    /**
     * 結果のJSONObjectを処理する関数
     * @param apiResult 処理が正常に完了かつステータスコードが200の時だけここがSUCCESSになる
     * @param statusCode ステータスコード
     * @param json 結果のJSONObject
     */
    abstract public void handleJson(int apiResult, int statusCode, @Nullable JSONObject json);

    protected APIHandler(Context context) {
        this.context = context;
    }

    @Override
    public void handleMessage(Message msg) {
        handleJson(msg.what, msg.arg1, (JSONObject)msg.obj);
    }
}
