/**
 * Copyright © 2020 SUNCORPORATION All rights reserved.
 *
 * Creation Date : July 14th, 2020
 */

package com.sun.m2m.okudakelinkdemoapp.okudake;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 温湿度センサーデータ
 */
public class ThermohygrometerValue {
    /** データ更新間隔の下限値（ミリ秒） */
    public static final int MIN_PERIOD = 100;
    /** データ更新間隔の上限値（ミリ秒） */
    public static final int MAX_PERIOD = 60000;

    /** データ取得時刻 */
    public long latestDataTime;
    /** 温度 */
    public double temperature;
    /** 湿度 */
    public double humidity;
    /** センサーの有効／無効 */
    public boolean enable;
    /** データ更新間隔 */
    public int period;

    /**
     * コンストラクタ
     */
    public ThermohygrometerValue() {
        resetValue();
    }

    /**
     * 保持しているデータのリセット
     */
    public void resetValue() {
        latestDataTime = 0;
        temperature = 0;
        humidity = 0;
        enable = false;
        period = 200;
    }

    /**
     * オブジェクトのコピー
     * @return コピーしたオブジェクト
     */
    public ThermohygrometerValue clone() {
        ThermohygrometerValue cloneObj = new ThermohygrometerValue();
        cloneObj.latestDataTime = latestDataTime;
        cloneObj.temperature = temperature;
        cloneObj.humidity = humidity;
        cloneObj.enable = enable;
        cloneObj.period = period;
        return cloneObj;
    }

    /**
     * 受信した温湿度データをセット
     * @param data　受信したデータ
     */
    public void setValue(byte[] data) {
        if (data.length == 4) {
            latestDataTime = System.currentTimeMillis();

            byte[] tmpBytes = {data[0], data[1], 0, 0, data[2], data[3], 0, 0, };
            ByteBuffer byteBuffer = ByteBuffer.wrap(tmpBytes);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

            int rhCode = byteBuffer.getInt();
            humidity = 125.0 * (double)rhCode / 65536.0 - 6.0;

            int tempCode = byteBuffer.getInt();
            temperature = 175.72 * (double)tempCode / 65536.0 - 46.85;
        }
    }

    /**
     * 受信したセンサーの有効／無効状態をセット
     * @param data　受信したデータ
     */
    public void setEnable(byte[] data) {
        if (data.length == 1) {
            if (data[0] == 0) {
                enable = false;
            } else if (data[0] == 1) {
                enable = true;
            }
        }
    }

    /**
     * 受信したデータ更新間隔をセット
     * @param data　受信したデータ
     */
    public void setPeriod(byte[] data) {
        if (data.length == 2) {
            int newPeriod = Utility.bytesToInt(data);
            if ((newPeriod >= MIN_PERIOD) && (newPeriod <= MAX_PERIOD)) {
                period = newPeriod;
            }
        }
    }
}
