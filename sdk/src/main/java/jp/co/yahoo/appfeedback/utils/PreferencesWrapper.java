package jp.co.yahoo.appfeedback.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 基本的な設定を保存するクラス<br>
 *  - Clipの表示・非表示<br>
 *  - アカウント名<br>
 *  - 録画ボタンタップ後に確認するかどうか
 *
 * Created by taicsuzu on 2016/09/21.
 */
class PreferencesWrapper {
    private static final String DB  = "id_manager";
    private static final String ID_KEY = "id";
    private static final String RECORD_CONFIRMED_KEY = "record_confirm";
    private static final String DRAW_CONFIRMED_KEY = "draw_confirm";
    private static final String SHOW_CLIP_KEY = "show_clip";
    private static final String SHOW_TAP_VIEW = "show_tap_view";
    private SharedPreferences pref;
    private static PreferencesWrapper PreferencesWrapper;

    private PreferencesWrapper(Context context) {
        this.pref = context.getSharedPreferences(DB, Context.MODE_PRIVATE);
    }

    public static PreferencesWrapper getInstance(Context context) {
        if(PreferencesWrapper == null) {
            PreferencesWrapper = new PreferencesWrapper(context);
        }

        return PreferencesWrapper;
    }

    /**
     * Clipの表示・非表示を切り替える
     */
    public void switchShowClip() {
        pref.edit().putBoolean(SHOW_CLIP_KEY, !showClip()).apply();
    }

    /**
     * Clipの表示・非表示を返す
     * @return boolean(trueなら表示)
     */
    public boolean showClip() {
        return pref.getBoolean(SHOW_CLIP_KEY, true);
    }

    /**
     * IDの保存
     * @param id アカウント名
     */
    public void setId(String id) {
        pref.edit().putString(ID_KEY, id).apply();
    }

    /**
     * IDの取得
     * @return アカウント名
     */
    public String getId() {
        return pref.getString(ID_KEY, "");
    }

    /**
     * 録画開始時の確認をするかどうかを設定
     */
    public void setRecordConfirmed(boolean confirm) {
        pref.edit().putBoolean(RECORD_CONFIRMED_KEY, confirm).apply();
    }

    /**
     * 録画開始時の確認をするかどうか
     * @return boolean(falseなら確認が必要)
     */
    public boolean getRecordConfirmed() {
        return pref.getBoolean(RECORD_CONFIRMED_KEY, false);
    }

    /**
     * お絵かき機能の使い方を表示するかどうかを設定
     * @param confirm boolean
     */
    public void setDrawConfirmed(boolean confirm) {
        pref.edit().putBoolean(DRAW_CONFIRMED_KEY, confirm).apply();
    }

    /**
     * お絵かき機能の使い方の表示をするかどうか
     * @return boolean(falseなら表示が必要)
     */
    public boolean getDrawConfirmed() {
        return pref.getBoolean(DRAW_CONFIRMED_KEY, false);
    }
}
