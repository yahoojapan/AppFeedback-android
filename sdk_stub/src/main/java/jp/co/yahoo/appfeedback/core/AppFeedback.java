package jp.co.yahoo.appfeedback.core;

import android.app.Activity;
import android.content.Intent;

/**
 * Created by taicsuzu on 2016/09/15.
 */
public class AppFeedback {

    /**
     * AppFeedback SDKを利用しないために空のAPIを用意している
     * この関数は機能しない
     * @param activity SDKを初期化するActivity
     * @param slackChannel 投稿先のSlack Channel
     */
    public static void start(Activity activity, String slackChannel) {
    }

    /**
     * AppFeedback SDKを利用しないために空のAPIを用意している
     * この関数は機能しない
     * @param activity SDKを初期化するActivity
     * @param token Slack Token
     * @param slackChannel 投稿先の Channel ID
     */
    public static void start(Activity activity, String token, String slackChannel) {
    }

    /**
     * Set slack api url
     */
    public static String getSlackApiUrl() {
        return "";
    }

    /**
     * Set slack api url
     */
    public static void setSlackApiUrl(String apiUrl) { }
}
