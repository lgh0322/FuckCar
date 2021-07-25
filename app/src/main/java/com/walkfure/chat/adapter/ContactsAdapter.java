package com.walkfure.chat.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.walkfure.chat.R;
import com.walkfure.chat.bean.ContactsBean;
import com.walkfure.chat.data.PersonalInfo;
import com.walkfure.chat.service.SocketService;
import com.walkfure.chat.view.CircleImageView;
import com.walkfure.chat.view.SwipeRecycler;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder> {
    private List<ContactsBean> ContactsList;
    private Context mContext;
    private RecyclerView rv;
    private int unreadSum = 0;
    OnItemClickListener itemClickListener;


    public ContactsAdapter(Context context, RecyclerView rv) {
        mContext = context;
        this.rv = rv;
    }


    public class ContactsViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public TextView time;
        public TextView content;
        public CircleImageView imageView;
        public TextView redText;

        public ContactsViewHolder(View v) {
            super(v);
            textView = v.findViewById(R.id.tv_name);
            imageView = v.findViewById(R.id.item_iv_avatar);
            content = v.findViewById(R.id.tv_content);
            redText = v.findViewById(R.id.red_text);
            time = v.findViewById(R.id.tv_time);
        }
    }

    public void setList(List<ContactsBean> beanList) {
        this.ContactsList = beanList;
        notifyDataSetChanged();
    }

    public void ReceiveMsg(String phone, String content) {
        for (int k = 0; k < ContactsList.size(); k++) {
            ContactsBean contactsBean = ContactsList.get(k);
            if (contactsBean.getPhone().equals(phone)) {
                ContactsList.get(k).setMsg(content);
                ContactsList.get(k).setUnRead(contactsBean.getUnRead() + 1);
                ContactsList.get(k).setTime("");
                break;
            }
        }

        //--------------------------------------------------update mainactivity bottom unread textview
        unreadSum = 0;
        for (int k = 0; k < ContactsList.size(); k++) {
            unreadSum = unreadSum + ContactsList.get(k).getUnRead();
        }
        Intent intent = new Intent(SocketService.updateMainActivityUnread);
        intent.putExtra("unread", unreadSum);
        mContext.sendBroadcast(intent);

        notifyDataSetChanged();
    }

    public void ReceiveMsgMeSend(String phone, String content) {
        for (int k = 0; k < ContactsList.size(); k++) {
            ContactsBean contactsBean = ContactsList.get(k);
            if (contactsBean.getPhone().equals(phone)) {
                ContactsList.get(k).setMsg(content);
                ContactsList.get(k).setUnRead(0);
                break;
            }
        }

        //--------------------------------------------------update mainactivity bottom unread textview
        unreadSum = 0;
        for (int k = 0; k < ContactsList.size(); k++) {
            unreadSum = unreadSum + ContactsList.get(k).getUnRead();
        }
        Intent intent = new Intent(SocketService.updateMainActivityUnread);
        intent.putExtra("unread", unreadSum);
        mContext.sendBroadcast(intent);
        notifyDataSetChanged();
    }

    public void SetRedTextZero(String phone) {
        for (int k = 0; k < ContactsList.size(); k++) {
            ContactsBean contactsBean = ContactsList.get(k);
            if (contactsBean.getPhone().equals(phone)) {
                ContactsList.get(k).setUnRead(0);
                break;
            }
        }

        //--------------------------------------------------update mainactivity bottom unread textview
        unreadSum = 0;
        for (int k = 0; k < ContactsList.size(); k++) {
            unreadSum = unreadSum + ContactsList.get(k).getUnRead();
        }
        Intent intent = new Intent(SocketService.updateMainActivityUnread);
        intent.putExtra("unread", unreadSum);
        mContext.sendBroadcast(intent);
        notifyDataSetChanged();
    }

    public void clearList() {
        this.ContactsList = new ArrayList<>();
        notifyDataSetChanged();
    }

    public ContactsAdapter.ContactsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_contacts, rv, false);
        ContactsAdapter.ContactsViewHolder vh = new ContactsViewHolder(view);
        return vh;
    }


    public void onBindViewHolder(ContactsViewHolder holder, int position) {
        ContactsBean bean = ContactsList.get(position);
        holder.textView.setText(bean.getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContactsAdapter.this.itemClickListener.onItemClick(bean.getPhone());
            }
        });
        if (bean.getUnRead() > 0) {
            holder.redText.setVisibility(View.VISIBLE);
            holder.redText.setText(String.valueOf(bean.getUnRead()));
            if (bean.getUnRead() > 99) {
                holder.redText.setText("99");
            }
        } else {
            holder.redText.setVisibility(View.INVISIBLE);
        }

        holder.content.setText(bean.getMsg());
        holder.time.setText(bean.getTime());
        Glide.with(mContext).load(SocketService.filePath + bean.getAvatar())
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .centerCrop().into(holder.imageView);


    }

    public int getItemCount() {
        return this.ContactsList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(String phone);
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}
