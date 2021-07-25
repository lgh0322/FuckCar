package com.walkfure.chat.fragment.setting;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.walkfure.chat.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/7/8.
 */
public class SettingCotrolerFragment extends Fragment {
    Spinner control_spin;
    Context mContext;
    List<String> mList = new ArrayList<>();

    public SettingCotrolerFragment() {

    }

    class spinnerAdapter extends BaseAdapter {
        public spinnerAdapter() {
            mList.clear();
            mList.add("左手油门");
            mList.add("右手油门");
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            convertView = layoutInflater.inflate(R.layout.item_control_spinner, null);
            if (convertView != null) {
                TextView textView = convertView.findViewById(R.id.control_spinner_tv);
                textView.setText(mList.get(position));
            }
            return convertView;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_setting_controler, container, false);
        control_spin = view.findViewById(R.id.control_spinner);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        control_spin.setAdapter(new spinnerAdapter());
    }
}