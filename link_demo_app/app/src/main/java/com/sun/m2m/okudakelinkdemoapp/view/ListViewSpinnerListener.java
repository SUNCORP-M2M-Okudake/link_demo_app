/**
 * Copyright © 2020 SUNCORPORATION All rights reserved.
 *
 * Creation Date : July 14th, 2020
 */

package com.sun.m2m.okudakelinkdemoapp.view;

import android.view.View;
import android.widget.AdapterView;

/**
 * ListViewの中にあるSpinnerのEvent Listener
 */
public abstract class ListViewSpinnerListener implements AdapterView.OnItemSelectedListener {

    /** ListViewの行番号 */
    protected int listIdx;

    /**
     * コンストラクタ
     * @param listIdx ListViewの行番号
     */
    public ListViewSpinnerListener(int listIdx) {
        this.listIdx = listIdx;
    }

    /**
     * Spinnerの項目が選択された時のコールバック
     * @param adapterView Spinner
     * @param view 選択された項目のView
     * @param position Spinnerで選択された項目のIndex
     * @param id 選択された項目のID
     */
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {

    }

    /**
     * Spinnerから選択した項目が消えた時のコールバック
     * @param adapterView Spinner
     */
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
