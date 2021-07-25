package com.walkfure.chat.fragment.setting;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.walkfure.chat.R;

/**
 * Created by Administrator on 2016/7/8.
 */
public class SettingVideoFragment extends Fragment implements View.OnClickListener {
    Button x1;
    Button x2;
    Context mContext;


    public SettingVideoFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_setting_video, container, false);
        x1 = view.findViewById(R.id.a11);
        x2 = view.findViewById(R.id.a22);
        x1.setOnClickListener(this);
        x2.setOnClickListener(this);
        x2.setTextColor(Color.WHITE);
        x2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.button_circle_shape));
        x1.setTextColor(Color.BLACK);
        x1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.button_circle_shape_white));
        return view;
    }

    @Override
    public void onClick(View v) {
        int da = v.getId();
        switch (da) {
            case R.id.a11:
                x1.setTextColor(Color.WHITE);
                x1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.button_circle_shape));
                x2.setTextColor(Color.BLACK);
                x2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.button_circle_shape_white));

                break;
            case R.id.a22:
                x2.setTextColor(Color.WHITE);
                x2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.button_circle_shape));
                x1.setTextColor(Color.BLACK);
                x1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.button_circle_shape_white));

                break;
        }
    }
}