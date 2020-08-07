/**
 * Copyright © 2020 SUNCORPORATION All rights reserved.
 *
 * Creation Date : July 14th, 2020
 */

package com.sun.m2m.okudakelinkdemoapp.okudake;

import java.nio.ByteBuffer;

/**
 * 加速度センサーデータ
 */
public class AccelerometerValue {
    /** データ更新間隔の下限値（ミリ秒） */
    public static final int MIN_PERIOD = 100;
    /** データ更新間隔の上限値（ミリ秒） */
    public static final int MAX_PERIOD = 60000;

    /** データ取得時刻 */
    public long latestDataTime;
    /**
     * 加速度
     * [0] ... X軸の加速度
     * [1] ... Y軸の加速度
     * [2] ... Z軸の加速度
     */
    public double[] acceleration;
    /** センサーの有効／無効 */
    public boolean enable;
    /** データ更新間隔 */
    public int period;

    /**
     * コンストラクタ
     */
    public AccelerometerValue() {
        resetValue();
    }

    /**
     * 保持しているデータのリセット
     */
    public void resetValue() {
        latestDataTime = 0;
        acceleration = new double[3];
        enable = false;
        period = 100;
    }

    /**
     * オブジェクトのコピー
     * @return コピーしたオブジェクト
     */
    public AccelerometerValue clone() {
        AccelerometerValue cloneObj = new AccelerometerValue();
        cloneObj.latestDataTime = latestDataTime;
        System.arraycopy(acceleration, 0, cloneObj.acceleration, 0, acceleration.length);
        cloneObj.enable = enable;
        cloneObj.period = period;
        return cloneObj;
    }

    /**
     * 受信した加速度データをセット
     * @param data　受信したデータ
     */
    public void setValue(byte[] data) {
        if (data.length == 6) {
            latestDataTime = System.currentTimeMillis();

            ByteBuffer byteBuffer = ByteBuffer.wrap(data);
            byteBuffer.position(0);
            byte[] bytes = new byte[2];
            for (int i=0; i<3; i++) {
                byteBuffer.get(bytes);
                acceleration[i] = ((double) Utility.bytesToShort(bytes) * 3.9 * 9.8) / 1000.0;
            }
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
