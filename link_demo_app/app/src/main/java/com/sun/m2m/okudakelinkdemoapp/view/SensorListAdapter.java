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
import android.widget.BaseExpandableListAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import com.sun.m2m.okudakelinkdemoapp.R;

/**
 * センサーデータを表示するExpandableListView用のAdapter
 */
public class SensorListAdapter extends BaseExpandableListAdapter {
    /** アプリのContext */
    private Context context;
    /** LayoutInflater */
    private LayoutInflater layoutInflater;
    /** ListViewに表示するセンサー */
    private List<SensorValue> sensorList;
    /** ListViewの各センサーに表示する設定 */
    private List<List<SettingValue>> settingList;

    /**
     * コンストラクタ
     * @param context アプリのContext
     * @param sensorValues ListViewに表示するセンサー
     * @param settingValues ListViewの各センサーに表示する設定
     */
    public SensorListAdapter(Context context, List<SensorValue> sensorValues,
                             List<List<SettingValue>> settingValues) {
        this.context = context;
        this.layoutInflater =
                (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.sensorList = sensorValues;
        this.settingList = settingValues;
    }

    /**
     * ListViewに表示するセンサーの値をセットする
     * @param idx 値を入れるセンサーのListView上でのIndex
     * @param value センサーの値
     */
    public void setSensorValue(int idx, String value) {
        if ((idx >= 0) && (idx < sensorList.size())) {
            sensorList.get(idx).value = value;
        }
    }

    /**
     * センサー毎の設定Spinnerの選択項目Indexをセットする
     * @param groupIdx 値を入れるセンサーのListView上でのIndex
     * @param childIdx センサーの子要素（値を入れる設定の行）のIndex
     * @param settingIdx 設定Spinnerの選択項目Index
     */
    public void setSettingValue(int groupIdx, int childIdx, int settingIdx) {
        if ((groupIdx >= 0) && (groupIdx < settingList.size())) {
            List<SettingValue> lst = settingList.get(groupIdx);
            if ((childIdx >= 0) && (childIdx < lst.size())) {
                SettingValue settingValue = lst.get(childIdx);
                if ((settingIdx >= 0) && (settingIdx < settingValue.listItems.length)) {
                    settingValue.selectedItemIndex = settingIdx;
                }
            }
        }
    }

    /**
     * センサー毎の設定Spinnerの選択項目Indexを返す
     * @param groupIdx 値を入れるセンサーのListView上でのIndex
     * @param childIdx センサーの子要素（値を入れる設定の行）のIndex
     * @return
     */
    public int getSettingValue(int groupIdx, int childIdx) {
        if ((groupIdx >= 0) && (groupIdx < settingList.size())) {
            List<SettingValue> lst = settingList.get(groupIdx);
            if ((childIdx >= 0) && (childIdx < lst.size())) {
                SettingValue settingValue = lst.get(childIdx);
                return settingValue.selectedItemIndex;
            }
        }
        return -1;
    }

    /**
     * ListViewの行数を返す
     * @return ListViewの行数
     */
    @Override
    public int getGroupCount() {
        return sensorList.size();
    }

    /**
     * ListView各行の子要素の行数を返す
     * @param idx ListViewの行のIndex
     * @return ListViewの各号の子要素の行数
     */
    @Override
    public int getChildrenCount(int idx) {
        if ((idx >= 0) && (idx < settingList.size())) {
            return settingList.get(idx).size();
        } else {
            return 0;
        }
    }

    /**
     * ListViewの行要素を返す
     * @param idx ListViewの行のIndex
     * @return ListViewの行要素
     */
    @Override
    public Object getGroup(int idx) {
        if ((idx >= 0) && (idx < sensorList.size())) {
            return sensorList.get(idx);
        } else {
            return null;
        }
    }

    /**
     * ListView各行の子要素を返す
     * @param groupIdx ListViewの行のIndex
     * @param childIdx ListViewの子要素のIndex
     * @return ListViewの子要素
     */
    @Override
    public Object getChild(int groupIdx, int childIdx) {
        if ((groupIdx >= 0) && (groupIdx < settingList.size())) {
            if ((childIdx >= 0) && (childIdx < settingList.get(groupIdx).size())) {
                return settingList.get(groupIdx).get(childIdx);
            }
        }
        return null;
    }

    /**
     * ListViewの行要素のIDを返す
     * @param idx ListViewの行のIndex
     * @return ID
     */
    @Override
    public long getGroupId(int idx) {
        return 0;
    }

    /**
     * ListView各行の子要素のIDを返す
     * @param groupIdx ListViewの行のIndex
     * @param childIdx ListViewの子要素のIndex
     * @return ID
     */
    @Override
    public long getChildId(int groupIdx, int childIdx) {
        return 0;
    }

    /**
     * has stable ids
     * @return has stable
     */
    @Override
    public boolean hasStableIds() {
        return false;
    }

    /**
     * ListViewの行要素のViewを生成して返す
     * @param idx ListViewの行のIndex
     * @param b 行が展開しているかどうか
     * @param view 行要素のView
     * @param viewGroup ListView
     * @return 行要素のView
     */
    @Override
    public View getGroupView(int idx, boolean b, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = layoutInflater.inflate(R.layout.list_group_item, viewGroup, false);
        }

        SensorValue sensorValue = sensorList.get(idx);

        TextView titleTextView = view.findViewById(R.id.group_title);
        titleTextView.setText(sensorValue.title);

        TextView valueTextView = view.findViewById(R.id.group_text);
        valueTextView.setText(sensorValue.value);

        return view;
    }

    /**
     * ListViewの行要素の子要素Viewを生成して返す
     * @param groupIdx ListViewの行のIndex
     * @param childIdx ListViewの子要素のIndex
     * @param b 行が展開しているかどうか
     * @param view 行要素のView
     * @param viewGroup ListView
     * @return 行要素のView
     */
    @Override
    public View getChildView(int groupIdx, int childIdx, boolean b, View view,
                             ViewGroup viewGroup) {
        if (view == null) {
            view = layoutInflater.inflate(R.layout.list_setting_item, viewGroup, false);
        }

        SettingValue settingValue = settingList.get(groupIdx).get(childIdx);

        TextView settingTitleTextView = view.findViewById(R.id.setting_title);
        settingTitleTextView.setText(settingValue.title);

        TextView settingUnitTextView = view.findViewById(R.id.setting_unit);
        settingUnitTextView.setText(settingValue.unit);

        Spinner settingSpinner = view.findViewById(R.id.setting_spinner);
        if (settingValue.title.length() > 0) {
            settingSpinner.setVisibility(View.VISIBLE);
            ArrayAdapter<String> settingAdapter =
                    new ArrayAdapter(context, R.layout.spinner_text_item, settingValue.listItems);
            settingAdapter.setDropDownViewResource(R.layout.spinner_text_item);
            settingSpinner.setAdapter(settingAdapter);
            settingSpinner.setSelection(settingValue.selectedItemIndex, false);
            settingSpinner.setOnItemSelectedListener(settingValue.onItemSelectedListener);
            ViewGroup.LayoutParams params = settingSpinner.getLayoutParams();
            params.width = (int)(150.0 * context.getResources().getDisplayMetrics().scaledDensity);
            settingSpinner.setLayoutParams(params);
        } else {
            settingTitleTextView.setText("この項目の設定はありません");
            settingSpinner.setVisibility(View.INVISIBLE);
            ViewGroup.LayoutParams params = settingSpinner.getLayoutParams();
            params.width = (int)(10.0 * context.getResources().getDisplayMetrics().scaledDensity);
            settingSpinner.setLayoutParams(params);
        }

        return view;
    }

    /**
     * 子要素の選択の許可／不許可を返す
     * @param groupIdx ListViewの行のIndex
     * @param childIdx ListViewの子要素のIndex
     * @return 子要素の選択の許可／不許可
     */
    @Override
    public boolean isChildSelectable(int groupIdx, int childIdx) {
        return true;
    }
}
