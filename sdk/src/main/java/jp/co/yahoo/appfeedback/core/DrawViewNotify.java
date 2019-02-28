package jp.co.yahoo.appfeedback.core;

import java.util.EventListener;

/**
 * DrawViewで発生したスワイプイベントをDrawActivityに通知するためのインターフェース
 *
 * Created by tsando on 2017/08/23.
 */
public interface DrawViewNotify extends EventListener {
    /**
     * スワイプが終了し、描画情報が更新された
     */
    public void drawViewUpdate();
}
