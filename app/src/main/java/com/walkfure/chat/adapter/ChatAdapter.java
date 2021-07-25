package com.walkfure.chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.SPUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.walkfure.chat.R;
import com.walkfure.chat.bean.ChatBean;
import com.walkfure.chat.data.PersonalInfo;
import com.walkfure.chat.holder.ChatTextReceiveHolder;
import com.walkfure.chat.holder.ChatTextSendHolder;
import com.walkfure.chat.service.SocketService;
import com.walkfure.chat.view.SwipeRecycler;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ChatBean> chatList = new ArrayList<>();
    private Context mContext;
    private RecyclerView rv;
    OnItemClickListener itemClickListener;

    public ChatAdapter(Context context, RecyclerView rv) {
        mContext = context;
        this.rv = rv;
    }


    public void setList(List<ChatBean> chatList) {
        this.chatList.addAll(0, chatList);
        notifyDataSetChanged();
    }

    public void setData(ChatBean bean) {
        this.chatList.add(bean);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return chatList.get(position).getChatType();
    }


    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        RecyclerView.ViewHolder viewHolder = null;
        switch (viewType) {
            case 0:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_chat_send_text, parent, false);
                viewHolder = new ChatTextSendHolder(view);
                break;
            case 1:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_chat_receive_text, parent, false);
                viewHolder = new ChatTextReceiveHolder(view);
                break;
        }
        return viewHolder;
    }


    private void showContent(RecyclerView.ViewHolder holder, ChatBean bean) {
        switch (holder.getItemViewType()) {
            case 0:
                ((ChatTextSendHolder) holder).tv_content.setText(bean.getChatMessage());
                Glide.with(mContext).load(SPUtils.getInstance().getString(PersonalInfo.AvatarPath))
                        .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                        .centerCrop().into(((ChatTextSendHolder) holder).iv_icon);

                break;
            case 1:
                ((ChatTextReceiveHolder) holder).tv_content.setText(bean.getChatMessage());
                Glide.with(mContext).load(SocketService.filePath + bean.getImg())
                        .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                        .centerCrop().into(((ChatTextReceiveHolder) holder).iv_icon);
                break;
        }

    }


    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChatBean chatBean = chatList.get(position);
        showContent(holder, chatBean);


    }

    public int getItemCount() {
        return this.chatList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int a);
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}
