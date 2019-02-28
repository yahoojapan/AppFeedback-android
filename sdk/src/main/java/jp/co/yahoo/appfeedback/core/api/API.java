package jp.co.yahoo.appfeedback.core.api;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

/**
 * APIにPOSTするための抽象クラス
 * 通信にはHttpURLConnectionを使用
 * ジェネリクスTはOutputStreamに書き込むために入力オブジェクト
 * フィードバックをPOSTするときはJSONObject、VideoをUploadするときはFileになる
 *
 * Created by taicsuzu on 2016/09/16.
 */
abstract public class API {
    static final int SUCCESS = 0, FAILURE = -1;

    protected Context context;

    API(Context context) {
        this.context = context;
    }

    /**
     * HttpURLConnectionのOutputStreamに書き込む
     * @param out connectionのOutputStream
     * @param t 入力オブジェクト
     * @throws IOException IOに異常発生時
     */
    abstract protected void writeToOutputStream(OutputStream out) throws IOException;

    /**
     * ヘッダーなどのオプションを設定する
     * @param connection 接続するHttpURLConnection
     */
    abstract protected void setOptions(HttpURLConnection connection);

    /**
     * リクエスト先のURLを返す
     * @return リクエストURL
     */
    abstract protected String requestUrl();

    /**
     * リクエストを実行
     * APIHandlerに結果を返す
     * @param handler APIHandler
     * @param t 入力オブジェクト
     */
    @SuppressLint("StaticFieldLeak")
    public void executeAsync(final APIHandler handler) {

        new AsyncTask<Void, Integer, JSONObject>() {

            @Override
            protected JSONObject doInBackground(Void... contents) {
                InputStream in = null;
                HttpsURLConnection connection = null;

                try {
                    SSLContext sslContext = SSLContext.getInstance("TLS");
                    sslContext.init(null, null, new SecureRandom());

                    URL url = new URL(requestUrl());

                    connection = (HttpsURLConnection)url.openConnection();
                    connection.setSSLSocketFactory(sslContext.getSocketFactory());
                    connection.setRequestMethod("POST");
                    connection.setInstanceFollowRedirects(false);
                    setOptions(connection);

                    OutputStream outputStream = connection.getOutputStream();
                    writeToOutputStream(outputStream);

                    int code = connection.getResponseCode();

                    JSONObject resultJson = new JSONObject();

                    if(code == 200) {
                        in = new BufferedInputStream(connection.getInputStream());
                        String result = readStream(in);
                        resultJson.put("result", new JSONObject(result));
                    }

                    resultJson.put("code", code);

                    return resultJson;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (KeyManagementException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (in != null) {
                            in.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(JSONObject json) {

                try {
                    if(json != null) {

                        Message msg = new Message();

                        int code = json.getInt("code");

                        if(code == 200) {
                            msg.what = SUCCESS;
                            msg.obj = json.getJSONObject("result");
                        }else{
                            msg.what = FAILURE;
                        }

                        msg.arg1 = code;

                        if(handler != null)
                            handler.sendMessage(msg);

                    }else{

                        Message msg = new Message();
                        msg.what = FAILURE;
                        msg.obj = null;

                        if(handler != null)
                            handler.sendMessage(msg);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }.execute();
    }

    /**
     * 結果を読み込んでStringに変換
     * @param in InputStream
     * @return 結果のString
     * @throws IOException IOに異常発生時
     */
    private String readStream(InputStream in) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int length;

        while ((length = in.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }

        return result.toString("UTF-8");
    }
}
