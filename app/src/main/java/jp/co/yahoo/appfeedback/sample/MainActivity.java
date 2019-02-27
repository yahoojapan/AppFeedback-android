package jp.co.yahoo.appfeedback.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import jp.co.yahoo.appfeedback.core.AppFeedback;

public class MainActivity extends AppCompatActivity {

    private static final String SLACK_CHANNEL = "CEJ4RQF6D";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Externalで起動するパターン
        AppFeedback.start(this,
                           BuildConfig.APP_FEEDBACK_SDK_SLACK_TOKEN,
                           SLACK_CHANNEL);

        findViewById(R.id.main_second).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SecondActivity.class));
            }
        });

        findViewById(R.id.main_web).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, WebViewActivity.class));
            }
        });

        findViewById(R.id.error).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                throw new RuntimeException();
            }
        });
    }
}
