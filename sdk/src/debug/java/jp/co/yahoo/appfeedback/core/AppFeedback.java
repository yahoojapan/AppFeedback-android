package jp.co.yahoo.appfeedback.core;

import android.app.Activity;
import android.content.Intent;

import jp.co.yahoo.appfeedback.constants.Host;

/**
 * ユーザーが使用するインターフェース<br>
 *  - AppFeedbackのIDとTokenの保持<br>
 *  - AppFeedbackContextの保持
 *
 * Created by taicsuzu on 2016/09/15.
 */

public class AppFeedback {
    private static final String TAG = "AppFeedbackSDK";

    private static AppFeedbackContext appFeedbackContext = null;

    private static String slackApiUrl = "https://slack.com/api";
    private static String token = null;
    private static String slackChannel = null;

    private static boolean canUseMediaProjectionAPI = true;
    private static Intent mediaProjectionData = null;

    /**
     * AppFeedbackSDKを初期化するための関数<br>
     * eventIdを指定しないので、internalで起動
     * @param activity SDKを初期化するActivity
     * @param slackChannel 投稿先のSlack Channel
     */
    public static void start(Activity activity, String slackChannel) {
        start(activity, null, slackChannel);
    }

    /**
     * AppFeedbackSDKを初期化するための関数<br>
     * アプリが最初に立ち上げるActivity内で呼び出す<br>
     * 詳しいドキュメントは<a href="http://cptl.corp.yahoo.co.jp/pages/viewpage.action?pageId=1175959217">ここに</a>
     * @param activity SDKを初期化するActivity
     * @param token Slack Token
     * @param slackChannel 投稿先の Channel ID
     */
    public static void start(Activity activity, String token, String slackChannel) {

        if(appFeedbackContext != null) {
            // アプリケーション起動時以外のタイミングでは呼び出されてもスルーする
            return;
        }

        if (slackChannel == null || slackChannel.length() == 0) {
            // パラメータが足りなければ起動しない
            return;
        }

        AppFeedback.token = token;
        AppFeedback.slackChannel = slackChannel;

        appFeedbackContext = new AppFeedbackContext(activity);

        // パーミッションの確認を行う
        AppFeedbackActivity.launch(activity);
    }

    /**
     * Set slack api url
     */
    public static String getSlackexApiUrl() {
        return AppFeedback.slackApiUrl;
    }

    /**
     * Set slack api url
     */
    public static void setSlackexApiUrl(String apiUrl) {
        AppFeedback.slackApiUrl = apiUrl;
    }

    /**
     * MediaProjectionの状態をセットする
     * @param mediaProjectionData MediaProjectionData
     * @param canUseMediaProjectionAPI MediaProjectionを利用可能か
     */
    static void setMediaProjection(Intent mediaProjectionData, boolean canUseMediaProjectionAPI) {
        AppFeedback.mediaProjectionData = mediaProjectionData;
        AppFeedback.canUseMediaProjectionAPI = canUseMediaProjectionAPI;
    }

    /**
     * AppFeedbackContextを返す
     * @return AppFeedbackContext
     */
    static AppFeedbackContext getAppFeedbackContext() {
        return appFeedbackContext;
    }

    /**
     * AppFeedback Tokenを返す
     * @return token
     */
    static String getToken() {
        return AppFeedback.token;
    }

    /**
     * slackChennelを返す
     * @return slackChannel
     */
    static String getSlackChannel() {
        return AppFeedback.slackChannel;
    }

    /**
     * MediaProjectionAPIが使用できるのかを返す
     * @return canUseMediaProjectionAPI
     */
    static boolean canUseMediaProjectionAPI() {
        return canUseMediaProjectionAPI;
    }

    /**
     * MediaProjectionのパーミッションのデータを返す
     * @return mediaProjectionData;
     */
    static Intent getMediaProjectionData() {
        return mediaProjectionData;
    }

    //コンストラクタは捨てる
    private AppFeedback() {}

    /**
     * idとtokenをリセットする
     * 今はテストに使用している
     */
    static void reset() {
        token = null;
        slackChannel = null;
        appFeedbackContext = null;
    }
}
