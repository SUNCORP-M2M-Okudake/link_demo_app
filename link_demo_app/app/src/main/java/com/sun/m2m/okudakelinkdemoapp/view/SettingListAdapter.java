/**
 * Copyright © 2020 SUNCORPORATION All rights reserved.
 *
 * Creation Date : July 14th, 2020
 */

package com.sun.m2m.okudakelinkdemoapp.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import com.sun.m2m.okudakelinkdemoapp.R;

/**
 * 設定ListView用のAdapter
 */
public class SettingListAdapter extends BaseAdapter {
    /** アプリのContext */
    private Context context;
    /** LayoutInflater */
    private LayoutInflater layoutInflater;
    /** ListViewに表示する設定 */
    private List<SettingValue> settingList;

    /**
     * コンストラクタ
     * @param context アプリのContext
     * @param settingValues ListViewに表示する設定
     */
    public SettingListAdapter(Context context, List<SettingValue> settingValues) {
        this.context = context;
        this.layoutInflater =
                (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        settingList = settingValues;
    }

    /**
     * 設定Spinnerの指定したIndexの項目を選択する
     * @param idx ListViewの行のIndex
     * @param selectedIdx Spinnerで選択する項目のIndex
     */
    public void setSetting(int idx, int selectedIdx) {
        if ((idx >= 0) && (idx < settingList.size())) {
            SettingValue settingValue = settingList.get(idx);
            if ((selectedIdx >= 0) && (selectedIdx <= settingValue.listItems.length)) {
                settingValue.selectedItemIndex = selectedIdx;
            }
        }
    }

    /**
     * 設定Spinnerの選択されている項目のIndexを返す
     * @param idx ListViewの行のIndex
     * @return Spinnerで選択されている項目のIndex
     */
    public int getSetting(int idx) {
        if ((idx >= 0) && (idx < settingList.size())) {
            SettingValue settingValue = settingList.get(idx);
            return settingValue.selectedItemIndex;
        }
        return -1;
    }

    /**
     * ListViewの行数を返す
     * @return ListViewの行数
     */
    @Override
    public int getCount() {
        return settingList.size();
    }

    /**
     * ListViewの行要素を返す
     * @param idx ListViewの行のIndex
     * @return ListViewの行要素
     */
    @Override
    public Object getItem(int idx) {
        if ((idx >= 0) && (idx < settingList.size())) {
            return settingList.get(idx);
        } else {
            return null;
        }
    }

    /**
     * ListViewの行要素のIDを返す
     * @param idx ListViewの行のIndex
     * @return ID
     */
    @Override
    public long getItemId(int idx) {
        return 0;
    }

    /**
     * ListViewの行のViewを返す
     * @param idx ListViewの行のIndex
     * @param view ListViewの行のView
     * @param viewGroup ListView
     * @return ListViewの行のView
     */
    @Override
    public View getView(int idx, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = layoutInflater.inflate(R.layout.list_setting_item, viewGroup, false);
        }

        SettingValue settingValue = settingList.get(idx);

        TextView settingTitleTextView = view.findViewById(R.id.setting_title);
        settingTitleTextView.setText(settingValue.title);

        TextView settingUnitTextView = view.findViewById(R.id.setting_unit);
        settingUnitTextView.setText(settingValue.unit);

        Spinner settingSpinner = view.findViewById(R.id.setting_spinner);
        ArrayAdapter<String> settingAdapter =
                new ArrayAdapter(context, R.layout.spinner_text_item, settingValue.listItems);
        settingAdapter.setDropDownViewResource(R.layout.spinner_text_item);
        settingSpinner.setAdapter(settingAdapter);
        settingSpinner.setSelection(settingValue.selectedItemIndex, false);

        settingSpinner.setOnItemSelectedListener(settingValue.onItemSelectedListener);

        return view;
    }
}
