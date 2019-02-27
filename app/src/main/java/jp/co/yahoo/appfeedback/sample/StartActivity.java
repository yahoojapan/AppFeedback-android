package jp.co.yahoo.appfeedback.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by taicsuzu on 2016/10/13.
 */

public class StartActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivityForResult(new Intent(StartActivity.this, MainActivity.class), 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }
}
