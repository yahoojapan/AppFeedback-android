package jp.co.yahoo.appfeedback.core;

import android.app.Activity;
import android.os.Bundle;

import jp.co.yahoo.appfeedback.utils.PreferencesWrapper;

/**
 * Notification(statusバーに表示されているもの)で反応するクラス
 * PreferenceWrapperを通じてモード(表示・非表示)の切り替えを行う
 *
 * Created by taicsuzu on 2016/10/24.
 */

public class ClipIntent extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferencesWrapper.getInstance(this).switchShowClip();
        finish();
    }
}
