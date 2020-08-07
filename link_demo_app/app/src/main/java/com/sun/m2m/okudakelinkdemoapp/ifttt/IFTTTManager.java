/**
 * Copyright © 2020 SUNCORPORATION All rights reserved.
 *
 * Creation Date : July 14th, 2020
 */

package com.sun.m2m.okudakelinkdemoapp.ifttt;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import com.sun.m2m.okudakelinkdemoapp.R;

/**
 * IFTTTへのデータアップロードを行うクラス
 */
public class IFTTTManager {
    /** ログ出力のTag */
    private final String TAG = "IFTTTManager";

    /** setValueで指定するValue1のIndex */
    public static final int IFTTT_VALUE1_INDEX = 0;
    /** setValueで指定するValue2のIndex */
    public static final int IFTTT_VALUE2_INDEX = 1;
    /** setValueで指定するValue3のIndex */
    public static final int IFTTT_VALUE3_INDEX = 2;

    /** アプリのContext */
    private Context context;
    /** IFTTTデータアップロード時のKey */
    private String key;
    /** IFTTTデータアップロード時のEventName */
    private String eventName;
    /** IFTTTデータアップロードURL */
    private String iftttUrl = "https://maker.ifttt.com/trigger/%s/with/key/%s";
    /** アップロードするデータ */
    private String[] iftttValues;
    /** 定期的にデータアップロードするかどうか */
    private boolean posting;
    /** 定期的にデータアップロードするときの間隔（秒） */
    private int postingIntervalSec;

    /**
     * コンストラクタ
     * @param context　アプリのContext
     * @param key　IFTTTデータアップロードするときのKey
     * @param eventName　IFTTTデータアップロードするときのEventName
     */
    public IFTTTManager(Context context, String key, String eventName) {
        this.context = context;
        this.key = key;
        this.eventName = eventName;
        if ((key != null) && (eventName != null)
                && (key.trim().length() > 0) && (eventName.trim().length() > 0)) {
            this.iftttUrl = String.format(this.iftttUrl, this.eventName, this.key);
        } else {
            this.iftttUrl = null;
        }
        this.iftttValues = new String[3];
        this.posting = false;
        this.postingIntervalSec = 10;
    }

    /**
     * 定期的にデータアップロード中かどうか
     * @return 定期的にデータアップロードしている時はTrue
     */
    public boolean isPosting() {
        return posting;
    }

    /**
     * 定期的にデータアップロードするときの間隔（秒）
     * @param sec データアップロードする間隔を1～3600で指定する
     */
    public void setPostingInterval(int sec) {
        if ((sec >= 1) && (sec <= 3600)) {
            postingIntervalSec = sec;
        }
    }

    /**
     * アップロードするデータをセット
     * 定期的にデータアップロードする場合に使用する
     * @param index データのIndex（0～2）
     * @param value アップロードするデータ
     */
    public void setValue(int index, String value) {
        if ((index >= 0) && (index < iftttValues.length)) {
            iftttValues[index] = value;
        }
    }

    /**
     * アップロードするデータをセット
     * 定期的にデータアップロードする場合に使用する
     * @param value1 アップロードするデータ１
     * @param value2 アップロードするデータ２
     * @param value3 アップロードするデータ３
     */
    public void setValues(String value1, String value2, String value3) {
        iftttValues[0] = value1;
        iftttValues[1] = value2;
        iftttValues[2] = value3;
    }

    /**
     * 定期的なデータアップロードを開始
     */
    public void startPosting() {
        if (iftttUrl != null) {
            posting = true;
            postingHandler.postDelayed(postingRunnable, 1000);
        }
    }

    /**
     * 定期的なデータアップロードを終了
     */
    public void stopPosting() {
        posting = false;
    }

    /**
     * 定期的にデータアップロードするためのHandler
     */
    private Handler postingHandler = new Handler();

    /**
     * 定期的にデータアップロードするためのRunnable
     */
    private Runnable postingRunnable = new Runnable() {
        @Override
        public void run() {
            postingHandler.removeCallbacks(postingRunnable);

            if (posting) {
                post(iftttValues);

                postingHandler.postDelayed(postingRunnable, postingIntervalSec*1000);
            }
        }
    };

    /**
     * IFTTTにデータをアップロードする
     * @param values　アップロードするデータ
     */
    public void post(final String[] values) {
        if ((values != null) && (values.length > 0) && (iftttUrl != null)) {

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {

                    HttpsURLConnection httpsURLConnection = null;
                    try {

                        JSONObject jsonObject = new JSONObject();
                        for (int i=0; i<3; i++) {
                            if (i < values.length) {
                                jsonObject.put(String.format("value%d", i+1), values[i]);
                            } else {
                                jsonObject.put(String.format("value%d", i+1), null);
                            }
                        }

                        URL url = new URL(iftttUrl);
                        httpsURLConnection = (HttpsURLConnection) url.openConnection();

                        httpsURLConnection.setRequestMethod("POST");
                        httpsURLConnection.setInstanceFollowRedirects(false);
                        httpsURLConnection.setDoOutput(true);
                        httpsURLConnection.setRequestProperty(
                                "Content-Type", "application/json; charset=UTF-8"
                        );
                        httpsURLConnection.setConnectTimeout(10000);

                        SSLContext sslContext = loadCertFile();
                        if (sslContext != null) {
                            httpsURLConnection.setSSLSocketFactory(sslContext.getSocketFactory());
                        }

                        OutputStreamWriter osw = new OutputStreamWriter(
                                httpsURLConnection.getOutputStream(),
                                "UTF-8"
                        );
                        osw.write(jsonObject.toString());
                        osw.flush();
                        osw.close();

                        int status = httpsURLConnection.getResponseCode();
                        Log.d(TAG, String.format("HTTP POST status = %d", status));

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        httpsURLConnection.disconnect();
                    }

                }
            });
            thread.start();
        }
    }

    /**
     * 公開鍵ファイルの読込
     * @return SSLContext
     */
    private SSLContext loadCertFile() {
        SSLContext sslContext = null;

        try {
            InputStream caInput =
                    new BufferedInputStream(context.getResources().openRawResource(R.raw.cert));
            Certificate ca;
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            try {
                ca = cf.generateCertificate(caInput);
                System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
            } finally {
                caInput.close();
            }

            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            sslContext = SSLContext.getInstance("TLSv1.3");
            sslContext.init(null, tmf.getTrustManagers(), null);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return sslContext;
        }

    }
}
