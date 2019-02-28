package jp.co.yahoo.appfeedback.constants;

import android.content.Context;

import jp.co.yahoo.appfeedback.R;

/**
 * 開発環境を切り替えるために使用<br>
 * Hostを直接使うのではなく、AppFeedback.dev(String) or alpha() or production()の使用を推奨<br>
 * (releaseパッケージにはHostのインターフェースがないため)
 *
 * Created by taicsuzu on 2016/09/20.
 */
class Host {
    private static ENV env = ENV.EXTERNAL;
    private static int devInstanceNum = 0;

    /**
     * 環境の設定
     * @param env 環境
     */
    public static void set(ENV env) {
        Host.env = env;
    }

    /**
     * 開発環境のURLを設定
     * @param instanceNum Development環境のインスタンス番号 0 ~ 4
     */
    public static void setDevInstanceNum(int instanceNum) {
        Host.devInstanceNum = instanceNum;
    }

    /**
     * 環境の種類
     */
    public enum ENV {

        DEV {
            @Override
            public String getHost(Context context) {
                return context.getString(R.string.appfeedback_api_dev);
            }
        },

        ALPHA {
            @Override
            public String getHost(Context context) {
                return context.getString(R.string.appfeedback_api_alpha);
            }
        },

        INTERNAL {
            @Override
            public String getHost(Context context) {
                return context.getString(R.string.appfeedback_api_internal);
            }
        },

        EXTERNAL {
            @Override
            public String getHost(Context context) {
                return context.getString(R.string.appfeedback_api_external);
            }
        };

        abstract public String getHost(Context context);
    }

    /**
     * 現在の環境の取得
     * @param context Context
     * @return 現在の環境
     */
    public static String HOST(Context context) {
        String url = env.getHost(context);

        if (Host.env == ENV.DEV) {
            url = String.format(url, devInstanceNum);
        }

        return url;
    }
}
