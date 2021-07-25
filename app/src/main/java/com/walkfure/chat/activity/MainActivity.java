package com.walkfure.chat.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.walkfure.chat.adapter.MainFragmentAdapter;
import com.walkfure.chat.R;
import com.walkfure.chat.data.PersonalInfo;
import com.walkfure.chat.fragment.CarFragment;
import com.walkfure.chat.fragment.ContactsFragment;
import com.walkfure.chat.fragment.MyFragment;
import com.walkfure.chat.service.SocketService;
import com.walkfure.chat.view.NoScrollViewPager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;
import me.weyye.hipermission.PermissionItem;

public class MainActivity extends AppCompatActivity {
    private Unbinder unbinder;
    public static MainActivity mainActivity;
    @BindView(R.id.image_layout_0)
    LinearLayout image_layout_0;
    @BindView(R.id.image_layout_1)
    LinearLayout image_layout_1;
    @BindView(R.id.image_layout_4)
    LinearLayout image_layout_4;

    @BindView(R.id.image_main_0)
    ImageView image_main_0;
    @BindView(R.id.image_main_1)
    ImageView image_main_1;

    @BindView(R.id.image_main_4)
    ImageView image_main_4;
    @BindView(R.id.image_text_0)
    TextView image_text_0;
    @BindView(R.id.image_text_1)
    TextView image_text_1;
    @BindView(R.id.image_text_4)
    TextView image_text_4;

    @BindView(R.id.red_text)
    TextView unreadTV;

    LinearLayout[] layouts;
    ImageView[] images;
    TextView[] mTextViews;
    @BindView(R.id.fl_content)
    public NoScrollViewPager fl_content;


    BroadcastReceiver broadcastReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = this;
        //---------------------------------------------------------poccessAvatar;
        AskPermission();
        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (action.equals(SocketService.LoginSuccessUpdate)) {
//                        sendBroadcast(new Intent(SocketService.LoginSuccessUploadPhone));
                    } else if (action.equals(SocketService.updateMainActivityUnread)) {
                        int unread = intent.getIntExtra("unread", 0);
                        if (unread > 0) {
                            unreadTV.setVisibility(View.VISIBLE);
                            unreadTV.setText(String.valueOf(unread));
                        } else {
                            unreadTV.setVisibility(View.INVISIBLE);
                        }
                    }

                }
            };
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(SocketService.updateMainActivityUnread);
            intentFilter.addAction(SocketService.LoginSuccessUpdate);
            registerReceiver(broadcastReceiver, intentFilter);

        }
        if (LoginActivity.loginActivity != null) {
            LoginActivity.loginActivity.finish();
            LoginActivity.loginActivity = null;
        }
        System.out.println("里的水库附近开了"+MainActivity.this.getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()).getAbsolutePath());
    }

    private void AskPermission() {
        List<PermissionItem> permissionItems = new ArrayList<PermissionItem>();
        permissionItems.add(new PermissionItem(Manifest.permission.CAMERA, "相机", R.drawable.permission_ic_camera));
        permissionItems.add(new PermissionItem(Manifest.permission.RECORD_AUDIO, "录音", R.drawable.permission_ic_micro_phone));
        permissionItems.add(new PermissionItem(Manifest.permission.READ_PHONE_STATE, "手机", R.drawable.permission_ic_phone));
        permissionItems.add(new PermissionItem(Manifest.permission.ACCESS_COARSE_LOCATION, "位置信息", R.drawable.permission_ic_storage));
        permissionItems.add(new PermissionItem(Manifest.permission.ACCESS_FINE_LOCATION, "定位", R.drawable.permission_ic_micro_phone));
        permissionItems.add(new PermissionItem(Manifest.permission.WRITE_EXTERNAL_STORAGE, "写入", R.drawable.permission_ic_phone));
        permissionItems.add(new PermissionItem(Manifest.permission.READ_EXTERNAL_STORAGE, "读取", R.drawable.permission_ic_phone));


        HiPermission.create(this).permissions(permissionItems)
                .checkMutiPermission(new PermissionCallback() {
                    @Override
                    public void onClose() {

                    }

                    @Override
                    public void onFinish() {
                        if (SPUtils.getInstance().getString(PersonalInfo.Avatar) == null) {

                        }
                        sendBroadcast(new Intent(SocketService.LoginSuccessUploadPhone));

                        setContentView(R.layout.activity_main);
                        unbinder = ButterKnife.bind(MainActivity.this);
                        layouts = new LinearLayout[]{image_layout_0, image_layout_1, image_layout_4};
                        images = new ImageView[]{image_main_0, image_main_1, image_main_4};
                        mTextViews = new TextView[]{image_text_0, image_text_1, image_text_4};
                        for (int a = 0; a < layouts.length; a++) {
                            layouts[a].setOnClickListener(new OnTabClickListener(a));
                        }
                        List<Fragment> fragments = new ArrayList<>();
                        fragments.add(CarFragment.newInstance());
                        fragments.add(ContactsFragment.newInstance());
                        fragments.add(MyFragment.newInstance());
                        MainFragmentAdapter mainFragmentAdapter = new MainFragmentAdapter(getSupportFragmentManager(), fragments);
                        fl_content.setAdapter(mainFragmentAdapter);
                        fl_content.addOnPageChangeListener(new PageChange());
                        fl_content.setOffscreenPageLimit(3);
                        fl_content.setNoScroll(true);
                        bottomTabClickState(0);

                    }

                    @Override
                    public void onDeny(String permission, int position) {

                    }

                    @Override
                    public void onGuarantee(String permission, int position) {

                    }
                });
    }

    @Override
    protected void onDestroy() {

        if (unbinder != null)
            unbinder.unbind();

        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
            broadcastReceiver.clearAbortBroadcast();
            broadcastReceiver = null;
        }
        mainActivity = null;
        super.onDestroy();
    }

    private void bottomTabClickState(int index) {
        fl_content.setCurrentItem(index);
        for (int a = 0; a < layouts.length; a++) {
            images[a].setSelected(a == index);
            mTextViews[a].setTextColor(a == index ? Color.parseColor("#255BB5") : Color.parseColor("#434343"));
        }
    }

    private void bottomTabClickState2(int index) {
        for (int a = 0; a < layouts.length; a++) {
            images[a].setSelected(a == index);
            mTextViews[a].setTextColor(a == index ? Color.parseColor("#255BB5") : Color.parseColor("#434343"));
        }
    }

    class OnTabClickListener implements View.OnClickListener {
        int index;

        public OnTabClickListener(int index) {
            this.index = index;
        }

        @Override
        public void onClick(View v) {
            bottomTabClickState(index);
        }

    }


    class PageChange implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int arg0) {
            bottomTabClickState2(arg0);
        }

    }
}
