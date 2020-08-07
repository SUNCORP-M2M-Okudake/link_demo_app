/**
 * Copyright © 2020 SUNCORPORATION All rights reserved.
 *
 * Creation Date : July 14th, 2020
 */

package com.sun.m2m.okudakelinkdemoapp.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.sun.m2m.okudakelinkdemoapp.R;
import com.sun.m2m.okudakelinkdemoapp.common.AppSettings;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

/**
 * IFTTT設定画面
 */
public class IftttSettingActivity extends AppCompatActivity {

    /** 背景Layout */
    private LinearLayout baseLayout;
    /** IFTTT KeyのEditText */
    private EditText iftttKeyEditText;
    /** IFTTT EventNameのEditText */
    private EditText iftttEventEditText;
    /** IFTTT アップロード間隔のSpinner */
    private Spinner iftttUploadIntervalSpinner;

    /** IFTTT アップロード間隔のSpinnerの選択項目 */
    private final Integer[] uploadIntervalValues = new Integer[]{5, 10, 20, 30, 60};

    /**
     * onCreate
     * @param savedInstanceState SavedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ifttt_setting);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("IFTTT設定");
        actionBar.setDisplayHomeAsUpEnabled(true);

        baseLayout = findViewById(R.id.base_layout);
        iftttKeyEditText = findViewById(R.id.ifttt_key_edittext);
        iftttEventEditText = findViewById(R.id.ifttt_event_edittext);
        iftttUploadIntervalSpinner = findViewById(R.id.ifttt_upload_interval_spinner);

        baseLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                InputMethodManager inputManager = (InputMethodManager)getSystemService(
                        getApplicationContext().INPUT_METHOD_SERVICE
                );
                inputManager.hideSoftInputFromWindow(
                        view.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS
                );
                return false;
            }
        });

        ArrayAdapter<Integer> uploadIntervalArrayAdapter = new ArrayAdapter<>(
                getApplicationContext(),
                android.R.layout.simple_spinner_dropdown_item,
                uploadIntervalValues
        );
        iftttUploadIntervalSpinner.setAdapter(uploadIntervalArrayAdapter);

    }

    /**
     * onResume
     * Activity表示時のコールバック
     */
    @Override
    protected void onResume() {
        super.onResume();

        AppSettings.loadSettings(getApplicationContext());

        iftttKeyEditText.setText(AppSettings.iftttKey);
        iftttEventEditText.setText(AppSettings.iftttEventName);
        iftttUploadIntervalSpinner.setSelection(0, false);

        for (int i=0; i<uploadIntervalValues.length; i++) {
            if (uploadIntervalValues[i] == AppSettings.iftttUploadInterval) {
                iftttUploadIntervalSpinner.setSelection(i, false);
                break;
            }
        }
    }

    /**
     * onSupportNavigateUp
     * ActionBarの矢印ボタンで前画面に戻る
     * @return
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish();

        return super.onSupportNavigateUp();
    }

    /**
     * onPause
     * Activity非表示時のコールバック
     */
    @Override
    protected void onPause() {
        saveSettings();

        super.onPause();
    }

    /**
     * onDestroy
     * Activity破棄時のコールバック
     */
    @Override
    protected void onDestroy() {
        saveSettings();

        super.onDestroy();
    }

    /**
     * IFTTT設定の保存
     */
    private void saveSettings() {
        AppSettings.iftttKey = iftttKeyEditText.getText().toString();
        AppSettings.iftttEventName = iftttEventEditText.getText().toString();
        AppSettings.iftttUploadInterval =
                uploadIntervalValues[iftttUploadIntervalSpinner.getSelectedItemPosition()];

        AppSettings.saveSettings(getApplicationContext());

    }
}
