/**
 * Copyright © 2020 SUNCORPORATION All rights reserved.
 *
 * Creation Date : July 14th, 2020
 */

package com.sun.m2m.okudakelinkdemoapp.view;

/**
 * ListViewに表示するセンサー名とセンサー値を格納
 */
public class SensorValue {
    /** ListViewに表示するセンサー名） */
    public String title;
    /** ListViewに表示するセンサー値 */
    public String value;

    /**
     * コンストラクタ
     * @param title センサー名
     * @param value センサー値
     */
    public SensorValue(String title, String value) {
        this.title = title;
        this.value = value;
    }
}
