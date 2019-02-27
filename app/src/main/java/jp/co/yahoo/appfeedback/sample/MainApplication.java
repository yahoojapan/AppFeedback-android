package jp.co.yahoo.appfeedback.sample;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by taicsuzu on 2017/06/21.
 */

public class MainApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }

        LeakCanary.install(this);
    }
}
