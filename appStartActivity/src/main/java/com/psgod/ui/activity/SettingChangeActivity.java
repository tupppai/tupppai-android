package com.psgod.ui.activity;

import android.content.Context;
import android.os.Bundle;

import com.psgod.R;
import com.psgod.ui.widget.ActionBar;

/**
 * Created by pires on 16/1/20.
 */
public class SettingChangeActivity extends PSGodBaseActivity {
    private static final String TAG = SettingChangeActivity.class.getSimpleName();
    private Context mContext;
    private ActionBar mActionBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_change);

        mActionBar = (ActionBar) this.findViewById(R.id.actionbar);
    }
}
