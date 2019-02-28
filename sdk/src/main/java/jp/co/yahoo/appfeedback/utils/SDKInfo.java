package jp.co.yahoo.appfeedback.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

/**
 * デバイスとアプリのバージョンなどを返すクラス
 *
 * Created by taicsuzu on 2016/09/16.
 */
public class SDKInfo {
    /**
     * アプリ名の取得
     * @param context Context
     * @return アプリ名
     */
    public static String getAppName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

    /**
     * バージョン名の取得
     * @param context Context
     * @return バージョン名
     */
    public static String getVersionName(Context context) {
        PackageManager pm = context.getPackageManager();
        String versionName = "";
        try{
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = packageInfo.versionName;
        }catch(PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    /**
     * バージョンコードの取得
     * @param context Context
     * @return バージョンコード
     */
    public static int getVersionCode(Context context) {
        PackageManager pm = context.getPackageManager();
        int versionCode = 0;
        try{
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            versionCode = packageInfo.versionCode;
        }catch(PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * デバイスモデル名の取得
     * @return デバイスモデル名
     */
    public static String getDeviceModel() {
        return Build.MANUFACTURER + " " + Build.MODEL;
    }

    /**
     * デバイスのバージョンの取得
     * @return デバイスのバージョン
     */
    public static String getDeviceVersion() {
        return Build.VERSION.RELEASE;
    }

    // デバイスのバージョン(コードネーム)

    /**
     * デバイスのバージョンコードネームの取得
     * @return デバイスのバージョンコードネーム
     */
    public static String getDeviceVersionCodeName() {
        return Build.VERSION.CODENAME;
    }

    /**
     * SDKのバージョンの取得
     * @return SDKのバージョン
     */
    public static int getSDKVersion() {
        return Build.VERSION.SDK_INT;
    }
}
