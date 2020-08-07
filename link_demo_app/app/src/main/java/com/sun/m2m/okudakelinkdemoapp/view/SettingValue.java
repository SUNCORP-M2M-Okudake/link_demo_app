/**
 * Copyright © 2020 SUNCORPORATION All rights reserved.
 *
 * Creation Date : July 14th, 2020
 */

package com.sun.m2m.okudakelinkdemoapp.view;

import android.widget.AdapterView;

/**
 * ListViewに表示する設定名と設定値を格納
 */
public class SettingValue {
    /** ListViewに表示する設定名 */
    public String title;
    /** ListViewに表示する設定の単位 */
    public String unit;
    /** ListViewに表示するSpinnerの項目リスト */
    public String[] listItems;
    /** Spinnerの選択項目Index */
    public int selectedItemIndex;
    /** Spinnerの項目選択時のコールバック */
    public AdapterView.OnItemSelectedListener onItemSelectedListener;

    /**
     * コンストラクタ
     * @param title 設定名
     * @param unit 設定の単位
     * @param listItems Spinnerの選択項目
     * @param selectedItemIndex Spinnerの選択項目Index
     * @param onItemSelectedListener Spinnerの項目選択時のコールバック
     */
    public SettingValue(String title, String unit, String[] listItems, int selectedItemIndex,
                        AdapterView.OnItemSelectedListener onItemSelectedListener) {
        this.title = title;
        this.unit = unit;
        this.listItems = listItems;
        this.selectedItemIndex = selectedItemIndex;
        this.onItemSelectedListener = onItemSelectedListener;
    }
}
