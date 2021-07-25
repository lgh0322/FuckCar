package com.walkfure.chat.fragment;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.walkfure.chat.R;
import com.walkfure.chat.activity.ChatActivity;
import com.walkfure.chat.adapter.ContactsAdapter;


import com.walkfure.chat.bean.ContactsBean;
import com.walkfure.chat.data.PersonalInfo;
import com.walkfure.chat.service.BluetoothLeService;
import com.walkfure.chat.service.SocketService;
import com.walkfure.chat.view.CircleImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class ContactsFragment extends BaseFragment implements ContactsAdapter.OnItemClickListener {
    FriendListAdapter friendListAdapter;
    PopupWindow pwindow;
    RelativeLayout newF;
    ListView lv;
    RelativeLayout rl;
    PopupWindow popupWindow;
    @BindView(R.id.iv_right)
    ImageView add;
    @BindView(R.id.red_text)
    TextView red;
    List<ContactsBean> contactsBeanList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ContactsAdapter contactsAdapter;
    private BroadcastReceiver updateReceiver;
    public static JSONArray contactsJsonArray;

    public int getLayoutId() {
        return R.layout.fragment_contacts;
    }

    public static ContactsFragment newInstance() {

        Bundle args = new Bundle();

        ContactsFragment fragment = new ContactsFragment();
        fragment.setArguments(args);

        return fragment;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        newF = view.findViewById(R.id.newF);
        recyclerView = getActivity().findViewById(R.id.r2);
        contactsAdapter = new ContactsAdapter(mContext, recyclerView);
        contactsAdapter.setList(new ArrayList<ContactsBean>());
        contactsAdapter.setOnItemClickListener(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setAdapter(contactsAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);
        View pop = View.inflate(mContext, R.layout.add_pop_windows, null);
        popupWindow = new PopupWindow(pop, ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setOutsideTouchable(true);//设置点击外部区域可以取消popupWindow
        TextView a = pop.findViewById(R.id.a1);
        newF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                red.setVisibility(View.INVISIBLE);
                pwindow.setFocusable(true);
                pwindow.setOutsideTouchable(true);
                pwindow.showAsDropDown(newF);
                pwindow.update();
            }
        });

        a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText inputServer = new EditText(mContext);
                inputServer.setGravity(Gravity.CENTER);
                inputServer.setInputType(InputType.TYPE_CLASS_NUMBER);
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                builder.setTitle("输入对方手机号").setIcon(android.R.drawable.ic_dialog_info).setView(inputServer)
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                builder.setIcon(R.mipmap.ic_launcher);
                builder.setPositiveButton("查找", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text = inputServer.getText().toString();
                        if (text.length() > 0) {
                            Intent intent = new Intent(SocketService.searchAFriend);
                            intent.putExtra("msg", text);
                            mContext.sendBroadcast(intent);
                        }
                        dialog.dismiss();

                    }
                });
                builder.show();
                popupWindow.dismiss();
            }
        });

        TextView a2 = pop.findViewById(R.id.a2);
        a2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtils.showShort("请输入账号");
                popupWindow.dismiss();
            }
        });


        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.showAsDropDown(add, -10, 0, Gravity.END);//设置popupWindow显示,并且告诉它显示在那个View下面

            }
        });
    }


    @Override
    public void onItemClick(String phone) {
        Intent intent = new Intent(mContext, ChatActivity.class);
        intent.putExtra("phone", phone);
        contactsAdapter.SetRedTextZero(phone);
        mContext.startActivity(intent);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SocketService.UpdateContactsData);
        intentFilter.addAction(SocketService.searchAFriendResult);
        intentFilter.addAction(SocketService.addFriendRequest);
        intentFilter.addAction(SocketService.ChatReceive);
        intentFilter.addAction(SocketService.setRedTextZero);
        intentFilter.addAction(SocketService.MsgMeSend);
        if (updateReceiver == null) {
            updateReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (action == null) {
                        return;
                    }
                    if (action.equals(SocketService.MsgMeSend)) {
                        String phone = intent.getStringExtra("phone");
                        String content = intent.getStringExtra("content");
                        contactsAdapter.ReceiveMsgMeSend(phone, content);
                    } else if (action.equals(SocketService.setRedTextZero)) {
                        String phone = intent.getStringExtra("phone");
                        contactsAdapter.SetRedTextZero(phone);
                    } else if (action.equals(SocketService.ChatReceive)) {
                        String from = intent.getStringExtra("from");
                        String content = intent.getStringExtra("content");
                        contactsAdapter.ReceiveMsg(from, content);

                    } else if (action.equals(SocketService.addFriendRequest)) {
                        String data = intent.getStringExtra(SocketService.addFriendRequest);
                        red.setVisibility(View.VISIBLE);
                        ToastUtils.showShort(data);
                        if (data == null) {
                            return;
                        }
                        friendListAdapter.addDevice(data);

                    } else if (action.equals(SocketService.searchAFriendResult)) {
                        String data = intent.getStringExtra(SocketService.searchAFriendResult);
                        if (data == null) {
                            return;
                        }
                        if (data.length() == 0) {
                            return;
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(data);
                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            View contentview = LayoutInflater.from(mContext).inflate(R.layout.item_add_friend_return, null);
                            CircleImageView circleImageView = contentview.findViewById(R.id.item_iv_avatar);
                            TextView textView = contentview.findViewById(R.id.tv_name);
                            String img = jsonObject.getString("avatar");
                            String name = jsonObject.getString("name");
                            String phone = jsonObject.getString("id");
                            textView.setText(name);
                            if (img.length() != 0) {
                                Glide.with(mContext).load(SocketService.ImgUrl + img)
                                        .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                                        .centerCrop().into(circleImageView);
                            }


                            builder.setTitle("查找结果").setView(contentview)
                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });

                            builder.setPositiveButton("添加", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent1 = new Intent(SocketService.addFriendEnter);
                                    intent1.putExtra("msg", phone);
                                    mContext.sendBroadcast(intent1);

                                    dialog.dismiss();

                                }
                            });
                            builder.show();


                        } catch (JSONException e) {

                        }
                    } else if (action.equals(SocketService.UpdateContactsData)) {
                        String data = intent.getStringExtra(SocketService.UpdateContactsData);
                        try {
                            JSONArray a = new JSONArray(data);
                            contactsJsonArray = a;
                            if (a.length() > 0) {
                                contactsBeanList.clear();
                                for (int k = 0; k < a.length(); k++) {
                                    JSONObject b = new JSONObject(a.get(k).toString());
                                    ContactsBean bean = new ContactsBean();
                                    bean.setAvatar(b.getString("avatar"));
                                    bean.setName(b.getString("name"));
                                    bean.setPhone(b.getString("id"));
                                    contactsBeanList.add(bean);
                                    SocketService.contactsInfo.put(b.getString("id"), bean);
                                }
                                contactsAdapter.setList(contactsBeanList);
                            } else {


                                contactsAdapter.clearList();
                            }

                        } catch (JSONException e) {

                        }

                    }

                }
            };
            mContext.registerReceiver(updateReceiver, intentFilter);
        }

        friendListAdapter = new FriendListAdapter();
        friendListAdapter.notifyDataSetChanged();


        View contentview = LayoutInflater.from(mContext).inflate(R.layout.new_friend_list, null);
        rl = contentview.findViewById(R.id.rl);
        lv = contentview.findViewById(R.id.lv);


        lv.setAdapter(friendListAdapter);
        pwindow = new PopupWindow(contentview, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        pwindow.setOutsideTouchable(true);
        rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pwindow.dismiss();
            }
        });

        try {
            SocketService.mLockLoginMsg.notify();
        } catch (Exception ee) {

        }

        SocketService.Created = true;
    }


    public void onDestroy() {
        if (updateReceiver != null) {
            getActivity().unregisterReceiver(updateReceiver);
            updateReceiver.clearAbortBroadcast();
            updateReceiver = null;
        }
        super.onDestroy();
    }


    private class FriendListAdapter extends BaseAdapter {
        private ArrayList<String> mLeDevices;

        private LayoutInflater mInflator;

        public FriendListAdapter() {
            super();
            mLeDevices = new ArrayList<String>();
            mInflator = getLayoutInflater();
        }

        public void addDevice(String device) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public String getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public String getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }


        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = mInflator.inflate(R.layout.item_new_friend, null);
            CircleImageView circleImageView = view.findViewById(R.id.item_iv_avatar);
            TextView textView = view.findViewById(R.id.tv_name);
            String phone = "";
            try {
                JSONObject jsonObject = new JSONObject(mLeDevices.get(i));
                textView.setText(jsonObject.getString("name"));
                phone = jsonObject.getString("id");
                String url = jsonObject.getString("avatar");

                if (url.length() != 0) {
                    Glide.with(mContext).load(SocketService.ImgUrl + url)
                            .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                            .centerCrop().into(circleImageView);
                }


            } catch (JSONException e) {

            }


            Button button = view.findViewById(R.id.ga);
            String finalPhone = phone;
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SocketService.addFriendPermit);
                    intent.putExtra("msg", finalPhone);
                    mContext.sendBroadcast(intent);
                    mLeDevices.remove(i);
                    pwindow.dismiss();
                }
            });


            return view;
        }


    }


}
