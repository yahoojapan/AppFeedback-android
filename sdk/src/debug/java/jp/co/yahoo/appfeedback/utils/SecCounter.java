package jp.co.yahoo.appfeedback.utils;

import android.os.Handler;
import android.os.Message;

/**
 * 録画の秒数をカウントするオブジェクト<br>
 * カウントの開始・停止はScreenRecorderクラスからのみ行う
 *
 * Created by taicsuzu on 2017/04/24.
 */
public class SecCounter {
    private final static int MAX = 300;
    private Thread thread;
    private Handler handler;
    private int startTime;
    private boolean isCounting = false;

    private static SecCounter secCounter;

    public static SecCounter getInstance() {
        if(secCounter == null) {
            secCounter = new SecCounter();
        }

        return secCounter;
    }

    /**
     * カウント開始
     */
    public void startCount() {
        // 開始時間を覚えておく
        startTime = (int)System.currentTimeMillis();
        isCounting = true;
        this.thread = new Thread(runnable);
        this.thread.start();
    }

    /**
     * カウント停止
     */
    public void stopCount() {
        if(thread.isAlive()) {
            isCounting = false;
        }
    }

    /**
     * カウントしているかbooleanで返す
     * @return boolean(trueならカウント中)
     */
    public boolean isCounting() {
        return thread.isAlive();
    }

    /**
     * カウントを始めた時刻を返す
     * @return int(カウント開始時間kaisijikann)
     */
    public int startTime() {
        return startTime;
    }

    /**
     * カウントの最大値を返す
     * @return MAX
     */
    public int getMax() {
        return MAX;
    }

    /**
     * HandlerをSecCounterにアタッチする<br>
     * これによりSecCounterから今何秒かを受け取ることができる
     * @param handler
     */
    public void attachHandler(Handler handler) {
        this.handler = handler;
    }

    /**
     * カウントするRunnable
     */
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

            // 1 ~ MAXを投げる
            for(int i = 1 ; i <= MAX && isCounting ; i ++) {

                try {
                    // 0.1秒 sleep
                    // 100 * 300 = 10000(30秒)
                    Thread.sleep(100);

                    if(handler != null) {
                        Message msg = new Message();
                        msg.what = startTime;
                        msg.arg1 = i;
                        handler.sendMessage(msg);
                    }
                } catch (InterruptedException e) {
                    isCounting = false;
                    Thread.currentThread().interrupt();
                }
            }
        }
    };
}
