package com.walkfure.chat.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.walkfure.chat.R;
import com.walkfure.chat.data.PersonalInfo;
import com.walkfure.chat.service.SocketService;
import com.walkfure.chat.utils.FileUtils;
import com.walkfure.chat.view.CircleImageView;
import com.zxy.tiny.Tiny;
import com.zxy.tiny.callback.FileWithBitmapCallback;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.OnClick;

import static android.view.Gravity.CENTER;

public class MyFragment extends BaseFragment {

    private BroadcastReceiver updateReceiver;

    private AlertDialog.Builder builder_video;
    private AlertDialog dialog_video;
    private LayoutInflater inflater_video;
    private View layout_video;
    clickInfo cliInfo = new clickInfo();
    private TextView takeVideoTV;
    private TextView chooseVideoTV;
    private TextView cancelVideoTV;
    FileUtils fileUtils;
    private static int RequestSinglePhoto = 2;
    @BindView(R.id.bt_logout)
    TextView logout;
    @BindView(R.id.change_info)
    RelativeLayout change_info;
    @BindView(R.id.iv_head)
    CircleImageView iv_head;
    @BindView(R.id.tv_username)
    TextView tv_username;

    public int getLayoutId() {
        return R.layout.fragment_my;
    }

    public static MyFragment newInstance() {

        Bundle args = new Bundle();

        MyFragment fragment = new MyFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onDestroy() {
        if (updateReceiver != null) {
            mContext.unregisterReceiver(updateReceiver);
            updateReceiver.clearAbortBroadcast();
            updateReceiver = null;
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fileUtils = new FileUtils(mContext);

        //-------------------------------------------------register Receiver
        if (updateReceiver == null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(SocketService.updatePersonalAvatar);
            intentFilter.addAction(SocketService.updatePersonalName);
            intentFilter.addAction(SocketService.updatePersonalInfo);
            intentFilter.addAction(SocketService.downloadPersonalAvatarSucceed);
            updateReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (action.equals(SocketService.updatePersonalAvatar)) {
                        Glide.with(mContext).load(SPUtils.getInstance().getString(PersonalInfo.AvatarPath))
                                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                                .centerCrop().into(iv_head);

                    } else if (action.equals(SocketService.updatePersonalName)) {
                        tv_username.setText(SPUtils.getInstance().getString(PersonalInfo.UserName));
                    } else if (action.equals(SocketService.updatePersonalInfo)) {
                        String info = intent.getStringExtra(SocketService.updatePersonalInfo);
                        try {
                            JSONObject inf = new JSONObject(info);
                            String name = inf.getString("name");
                            String objectKey = inf.getString("avatar");
                            tv_username.setText(name);
                            final Intent intentX = new Intent(SocketService.checkObjectKey);
                            intentX.putExtra(SocketService.checkObjectKey, objectKey);
                            mContext.sendBroadcast(intentX);

                        } catch (JSONException e) {

                        }
                    } else if (action.equals(SocketService.downloadPersonalAvatarSucceed)) {
                        Glide.with(mContext).load(SPUtils.getInstance().getString(PersonalInfo.AvatarPath))
                                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                                .centerCrop().into(iv_head);
                    }
                }
            };
            mContext.registerReceiver(updateReceiver, intentFilter);
        }

    }

    @OnClick({R.id.bt_logout, R.id.change_info})
    public void onViewClicked(View view) {
        if (view.getId() == R.id.bt_logout) {
        } else if (view.getId() == R.id.change_info) {
            viewInit_change();
        }

    }


    public void viewInit_change() {

        builder_video = new AlertDialog.Builder(mContext);//创建对话框
        inflater_video = getLayoutInflater();
        layout_video = inflater_video.inflate(R.layout.dialog_change_info, null);//获取自定义布局
        builder_video.setView(layout_video);//设置对话框的布局
        dialog_video = builder_video.create();//生成最终的对话框
        dialog_video.show();//显示对话框

        takeVideoTV = layout_video.findViewById(R.id.videoGraph);
        chooseVideoTV = layout_video.findViewById(R.id.video);
        cancelVideoTV = layout_video.findViewById(R.id.cancel_video);
        //设置监听
        takeVideoTV.setOnClickListener(cliInfo);
        chooseVideoTV.setOnClickListener(cliInfo);
        cancelVideoTV.setOnClickListener(cliInfo);
    }

    class clickInfo implements View.OnClickListener {
        public void onClick(View v) {
            if (v.getId() == R.id.videoGraph) {
                final EditText inputServer = new EditText(mContext);
                inputServer.setGravity(CENTER);
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                builder.setTitle("输入新昵称").setIcon(android.R.drawable.ic_dialog_info).setView(inputServer)
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                builder.setIcon(R.mipmap.ic_launcher);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text = inputServer.getText().toString();
                        if (text.length() > 0) {
                            final Intent yes = new Intent(SocketService.ChangeName);
                            yes.putExtra("name", text);
                            mContext.sendBroadcast(yes);
                            ToastUtils.showShort("修改成功");
                            SPUtils.getInstance().put(PersonalInfo.UserName, text);
                            mContext.sendBroadcast(new Intent(SocketService.updatePersonalName));
                        }

                    }
                });
                builder.show();


            } else if (v.getId() == R.id.video) {
                dialog_video.dismiss();
                startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), RequestSinglePhoto);
            }

            dialog_video.dismiss();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestSinglePhoto
                && resultCode == Activity.RESULT_OK && null != data) {
            try {
                Uri selectedImage = data.getData();
                String filePath = fileUtils.getPath(selectedImage);
                Tiny.FileCompressOptions options = new Tiny.FileCompressOptions();
                Tiny.getInstance().source(filePath).asFile().withOptions(options).compress(new FileWithBitmapCallback() {
                    @Override
                    public void callback(boolean isSuccess, Bitmap bitmap, String outfile, Throwable t) {
                        final Intent intent = new Intent(SocketService.uploadAvatar);
                        intent.putExtra("path", outfile);
                        mContext.sendBroadcast(intent);
                        SPUtils.getInstance().put(PersonalInfo.AvatarPath, outfile);
                        mContext.sendBroadcast(new Intent(SocketService.updatePersonalAvatar));
                    }
                });
            } catch (Exception e) {

            }


        }

    }


}
