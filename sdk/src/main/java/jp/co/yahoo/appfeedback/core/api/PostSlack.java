package jp.co.yahoo.appfeedback.core.api;

import android.content.Context;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import jp.co.yahoo.appfeedback.core.FeedbackContent;
import jp.co.yahoo.appfeedback.utils.SDKInfo;

/**
 * Slackにフィードバックを送信するリクエスト
 *
 */

class PostSlack extends API {
    private final String apiUrl;
    private final String channel;
    private final String token;
    private final FeedbackContent content;

    public PostSlack(String apiUrl, Context context, String channel, String token, FeedbackContent content) {
        super(context);

        this.apiUrl = apiUrl;
        this.channel = channel;
        this.token= token;
        this.content = content;
    }

    @Override
    protected void writeToOutputStream(OutputStream out) throws IOException {
        OutputStream outputStream = new BufferedOutputStream(out);
        outputStream.write(this.generateBody().makePostData());
        outputStream.flush();
        outputStream.close();
    }

    @Override
    protected void setOptions(HttpURLConnection connection) {
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + MultipartFrom.BOUNDARY);
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("Cache-Control", "no-cache");
        connection.setRequestProperty("Authorization", "Bearer " + this.token);
    }

    protected MultipartFrom generateBody() {
        MultipartFrom multipartFrom = new MultipartFrom();

        multipartFrom.entryText("channels", this.channel);
        multipartFrom.entryText("initial_comment", this.generateComment());

        if (this.content.screenshot != null) {
            multipartFrom.entryText("media_type", "image");
            multipartFrom.entryBinary("file", this.content.screenshot, "image/jpeg");
            multipartFrom.entryText("filename", "ScreenShot.png");
        }
        else if (this.content.screencapture != null) {
            multipartFrom.entryText("media_type", "image");
            multipartFrom.entryBinary("file", this.content.screencapture, "video/mp4");
            multipartFrom.entryText("filename", "ScreenCapture.mp4");
        }

        return multipartFrom;
    }

    @Override
    protected String requestUrl() {
        return this.apiUrl + "/files.upload";
    }

    private String generateComment() {
        StringBuffer comment = new StringBuffer();

        comment.append(this.content.title);
        comment.append("\n");

        comment.append("by @" + this.content.account);
        comment.append("\n");

        comment.append("```");

        comment.append("[Message]\n");
        comment.append(this.content.message);
        comment.append("\n\n");

        comment.append("[App Title]\n");
        comment.append(SDKInfo.getAppName(this.context));
        comment.append("\n\n");

        comment.append("[App Version]\n");
        comment.append("Version Code: " + SDKInfo.getVersionCode(this.context) + "\n");
        comment.append("Version Name: " + SDKInfo.getVersionName(this.context) + "\n");
        comment.append("\n");

        comment.append("[Device]\n");
        comment.append("Model:Api " + SDKInfo.getDeviceModel() + "\n");
        comment.append("Version: " + SDKInfo.getDeviceVersion() + "\n");

        comment.append("```");

        return comment.toString();
    }
}
