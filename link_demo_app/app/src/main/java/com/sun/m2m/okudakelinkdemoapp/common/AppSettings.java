/**
 * Copyright © 2020 SUNCORPORATION All rights reserved.
 *
 * Creation Date : July 14th, 2020
 */

package com.sun.m2m.okudakelinkdemoapp.common;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * アプリの設定をファイルから読み込み／ファイルに書き込みするクラス
 */
public class AppSettings {
    /** ログ出力時のタグ */
    private static final String TAG = "AppSettings";
    /** 設定ファイル名 */
    private static final String settingFileName = "AppSettings";

    /** IFTTTのKeyを設定ファイルに書き込むときのKey */
    private static final String SETTING_KEY_IFTTT_KEY = "ifttt_key";
    /** IFTTTのEventNameを設定ファイルに書き込むときのKey */
    private static final String SETTING_KEY_IFTTT_EVENTNAME = "ifttt_eventname";
    /** IFTTTのデータアップロード間隔を設定ファイルに書き込むときのKey */
    private static final String SETTING_KEY_IFTTT_UPLOADINTERVAL = "ifttt_uploadinterval";
    /** IFTTTのValue1を設定ファイルに書き込むときのKey */
    private static final String SETTING_KEY_IFTTT_VALUE1 = "ifttt_value1";
    /** IFTTTのValue2を設定ファイルに書き込むときのKey */
    private static final String SETTING_KEY_IFTTT_VALUE2 = "ifttt_value2";
    /** IFTTTのValue3を設定ファイルに書き込むときのKey */
    private static final String SETTING_KEY_IFTTT_VALUE3 = "ifttt_value3";

    /** IFTTTのKey */
    public static String iftttKey;
    /** IFTTTのEventName */
    public static String iftttEventName;
    /** IFTTTのデータアップロード間隔（秒） */
    public static int iftttUploadInterval;
    /** IFTTTのValue1に入れるデータの種類 */
    public static int iftttValue1;
    /** IFTTTのValue2に入れるデータの種類  */
    public static int iftttValue2;
    /** IFTTTのValue3に入れるデータの種類  */
    public static int iftttValue3;


    /**
     * 設定ファイルを読み込む
     * ファイルが存在しない場合は設定初期値でファイルを作成する
     */
    public static void loadSettings(Context context) {
        iftttKey = "";
        iftttEventName = "";
        iftttUploadInterval = 10;
        iftttValue1 = 0;
        iftttValue2 = 1;
        iftttValue3 = 2;

        File settingFile = new File(context.getFilesDir(), settingFileName);
        if (!settingFile.exists()) {
            saveSettings(context);
        } else {
            Log.d(TAG, String.format("load setting file.  %s", settingFile.getAbsoluteFile()));
            FileInputStream is = null;
            BufferedReader br = null;
            try {
                is = new FileInputStream(settingFile.getAbsolutePath());
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                String str;
                while((str = br.readLine()) != null){
                    Log.d(TAG, str);
                    if (str.indexOf("=")>=0) {
                        String[] vals = str.split("=");

                        if (vals.length == 2) {
                            if (vals[0].equals(SETTING_KEY_IFTTT_KEY)) {
                                iftttKey = vals[1];
                            } else if (vals[0].equals(SETTING_KEY_IFTTT_EVENTNAME)) {
                                iftttEventName = vals[1];
                            } else if (vals[0].equals(SETTING_KEY_IFTTT_UPLOADINTERVAL)) {
                                iftttUploadInterval = Integer.parseInt(vals[1]);
                            } else if (vals[0].equals(SETTING_KEY_IFTTT_VALUE1)) {
                                iftttValue1 = Integer.parseInt(vals[1]);
                            } else if (vals[0].equals(SETTING_KEY_IFTTT_VALUE2)) {
                                iftttValue2 = Integer.parseInt(vals[1]);
                            } else if (vals[0].equals(SETTING_KEY_IFTTT_VALUE3)) {
                                iftttValue3 = Integer.parseInt(vals[1]);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try { is.close(); }
                    catch (Exception ex){}
                }
            }
        }

    }

    /**
     * 設定をファイルに書き込む
     */
    public static void saveSettings(Context context) {
        File settingFile = new File(context.getFilesDir(), settingFileName);
        if (settingFile.exists()) {
            settingFile.delete();
        }

        OutputStream os = null;
        BufferedWriter bw;
        try {
            Log.d(TAG, String.format("save setting file.  %s", settingFile.getAbsoluteFile()));
            os = new FileOutputStream(settingFile.getAbsolutePath());
            bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            bw.write(String.format("%s=%s", SETTING_KEY_IFTTT_KEY, iftttKey));
            bw.newLine();
            bw.write(String.format("%s=%s", SETTING_KEY_IFTTT_EVENTNAME, iftttEventName));
            bw.newLine();
            bw.write(String.format("%s=%s", SETTING_KEY_IFTTT_UPLOADINTERVAL, iftttUploadInterval));
            bw.newLine();
            bw.write(String.format("%s=%s", SETTING_KEY_IFTTT_VALUE1, iftttValue1));
            bw.newLine();
            bw.write(String.format("%s=%s", SETTING_KEY_IFTTT_VALUE2, iftttValue2));
            bw.newLine();
            bw.write(String.format("%s=%s", SETTING_KEY_IFTTT_VALUE3, iftttValue3));
            bw.newLine();
            bw.flush();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try { os.close(); }
                catch (Exception ex){}
            }
        }
    }
}
