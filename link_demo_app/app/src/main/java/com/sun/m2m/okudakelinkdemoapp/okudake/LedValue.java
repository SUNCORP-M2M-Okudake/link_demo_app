/**
 * Copyright © 2020 SUNCORPORATION All rights reserved.
 *
 * Creation Date : July 14th, 2020
 */

package com.sun.m2m.okudakelinkdemoapp.okudake;

/**
 * LED状態データ
 */
public class LedValue {
    /** LED消灯の設定値 */
    public static final byte LED_STATUS_OFF = 0;
    /** LED点滅の設定値 */
    public static final byte LED_STATUS_FLUSH = 1;
    /** LED点灯の設定値 */
    public static final byte LED_STATUS_ON = 2;

    /** データ取得時刻 */
    public long latestDataTime;
    /** LED状態 */
    public byte status;

    /**
     * コンストラクタ
     */
    public LedValue() {
        resetValue();
    }

    /**
     * 保持しているデータのリセット
     */
    public void resetValue() {
        latestDataTime = 0;
        status = LED_STATUS_OFF;
    }

    /**
     * オブジェクトのコピー
     * @return コピーしたオブジェクト
     */
    public LedValue clone() {
        LedValue cloneObj = new LedValue();
        cloneObj.latestDataTime = latestDataTime;
        cloneObj.status = status;
        return cloneObj;
    }

    /**
     * 受信したLED状態をセット
     * @param data　受信したデータ
     */
    public void setValue(byte[] data) {
        if (data.length == 1) {
            latestDataTime = System.currentTimeMillis();

            switch (data[0]) {
                case LED_STATUS_OFF:
                    status = LED_STATUS_OFF;
                    break;
                case LED_STATUS_FLUSH:
                    status = LED_STATUS_FLUSH;
                    break;
                case LED_STATUS_ON:
                    status = LED_STATUS_ON;
                    break;
            }
        }
    }
}
