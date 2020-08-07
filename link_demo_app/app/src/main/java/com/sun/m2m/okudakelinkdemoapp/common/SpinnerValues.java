/**
 * Copyright © 2020 SUNCORPORATION All rights reserved.
 *
 * Creation Date : July 14th, 2020
 */

package com.sun.m2m.okudakelinkdemoapp.common;

import com.sun.m2m.okudakelinkdemoapp.okudake.LedValue;

/**
 * Spinnerにセットする値
 */
public class SpinnerValues {

    /**
     * データ取得方法
     */
    public enum ReadDataWay {
        NONE,
        READ,
        NOTIFICATION
    }

    /**
     * ビーコン送信間隔のSpinnerの項目
     */
    public static final String[] BEACON_TRANSMITTING_INTERVALS = new String[]{
            "0.1", "0.5", "0.8", "1", "2"
    };

    /**
     * ビーコン送信間隔のSpinnerの項目の値
     */
    public static final int[] BEACON_TRANSMITTING_INTERVAL_VALUES = new int[]{
            160, 800, 1280, 1600, 3200
    };

    /**
     * ビーコン更新間隔のSpinnerの項目
     */
    public static final String[] BEACON_UPDATE_INTERVALS = new String[]{
            "1", "4", "5", "10", "20", "30", "40", "50", "60"
    };

    /**
     * ビーコン更新間隔のSpinnerの項目の値
     */
    public static final int[] BEACON_UPDATE_INTERVAL_VALUES = new int[]{
            1, 4, 5, 10, 20, 30, 40, 50, 60
    };

    /**
     * データ取得方法のSpinnerの項目
     */
    public static final String[] READ_DATA_WAYS = new String[]{
            "Read", "Notification"
    };

    /**
     * データ取得方法のSpinnerの項目の値
     */
    public static final ReadDataWay[] READ_DATA_WAY_VALUES = new ReadDataWay[]{
            ReadDataWay.READ, ReadDataWay.NOTIFICATION
    };

    /**
     * データ取得間隔のSpinnerの項目
     */
    public static final String[] READ_DATA_INTERVALS = new String[]{
            "なし", "1", "5", "10", "20", "30"
    };

    /**
     * データ取得間隔のSpinnerの項目の値
     */
    public static final int[] READ_DATA_INTERVAL_VALUES = new int[]{
            0, 1, 5, 10, 20, 30
    };

    /**
     * 送信出力のSpinnerの項目
     */
    public static final String[] TX_POWER_LEVELS = new String[]{
            "-20", "-10", "0", "7"
    };

    /**
     * 送信出力のSpinnerの項目の値
     */
    public static final int[] TX_POWER_LEVEL_VALUES = new int[]{
            -200, -100, 0, 70
    };

    /**
     * 自動切断のSpinnerの項目
     */
    public static final String[] IDLE_TIMEOUTS = new String[]{
            "なし", "1", "10", "20", "30", "60", "120", "180"
    };

    /**
     * 自動切断のSpinnerの項目の値
     */
    public static final int[] IDLE_TIMEOUT_VALUES = new int[]{
            0, 1, 10, 20, 30, 60, 120, 180
    };

    /**
     * センサー更新間隔のSpinnerの項目
     */
    public static final String[] SENSOR_UPDATE_INTERVALS = new String[]{
            "0.5", "1", "2", "5", "10", "20", "30", "60"
    };

    /**
     * センサー更新間隔のSpinnerの項目の値
     */
    public static final int[] SENSOR_UPDATE_INTERVAL_VALUES = new int[]{
            500, 1000, 2000, 5000, 10000, 20000, 30000, 60000
    };

    /**
     * LED状態のSpinnerの項目
     */
    public static final String[] LED_STATUS = new String[]{
            "消灯", "点滅", "点灯"
    };

    /**
     * LED状態のSpinnerの項目の値の値
     */
    public static final int[] LED_STATUS_VALUES = new int[]{
            LedValue.LED_STATUS_OFF, LedValue.LED_STATUS_FLUSH, LedValue.LED_STATUS_ON
    };

    /**
     * IFTTTデータのSpinnerの項目
     */
    public static final String[] IFTTT_TEXTS = new String[]{
            "なし", "端末名", "温度", "湿度", "照度", "加速度"
    };

    /** IFTTTデータ　なしのSpinnerでのIndex */
    public static final int IFTTT_NOTHING_INDEX = 0;
    /** IFTTTデータ　端末名のSpinnerでのIndex */
    public static final int IFTTT_DEVICE_NAME_INDEX = 1;
    /** IFTTTデータ　温度のSpinnerでのIndex */
    public static final int IFTTT_TEMPERATURE_INDEX = 2;
    /** IFTTTデータ　湿度のSpinnerでのIndex */
    public static final int IFTTT_HUMIDITY_INDEX = 3;
    /** IFTTTデータ　照度のSpinnerでのIndex */
    public static final int IFTTT_ILLUMINANCE_INDEX = 4;
    /** IFTTTデータ　加速度のSpinnerでのIndex */
    public static final int IFTTT_ACCELERATION_INDEX = 5;

    /**
     * IFTTTデータのSpinnerの項目の値
     */
    public static final int[] IFTTT_VALUES = new int[]{
            IFTTT_NOTHING_INDEX,
            IFTTT_DEVICE_NAME_INDEX,
            IFTTT_TEMPERATURE_INDEX,
            IFTTT_HUMIDITY_INDEX,
            IFTTT_ILLUMINANCE_INDEX,
            IFTTT_ACCELERATION_INDEX
    };
}
