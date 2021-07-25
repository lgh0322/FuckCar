package com.walkfure.chat.fragment.setting;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.walkfure.chat.R;

/**
 * Created by Administrator on 2016/7/8.
 */
public class SettingCarFragment extends Fragment {
    Switch aSwitch;
    Context mContext;

    public SettingCarFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_setting_car, container, false);
        mContext = getActivity();
        aSwitch = view.findViewById(R.id.s2);
        aSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("stick");
                intent.putExtra("ga", aSwitch.isChecked());
                mContext.sendBroadcast(intent);
            }
        });
        return view;
    }
}