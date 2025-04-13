package com.mobiledev.recipeit.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobiledev.recipeit.Models.ChatHistory;
import com.mobiledev.recipeit.R;

import java.time.format.DateTimeFormatter;
import java.util.List;

import io.noties.markwon.Markwon;

public class ChatHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final int VIEW_TYPE_SERVER = 0;
    private static final int VIEW_TYPE_USER = 1;

    private final Markwon markwon;
    private final List<ChatHistory> chatHistories;

    public ChatHistoryAdapter(Context context, List<ChatHistory> chatHistories) {
        this.chatHistories = chatHistories;
        this.markwon = Markwon.create(context);
    }

    @Override
    public int getItemViewType(int position) {
        return chatHistories.get(position).isServerMessage() ? VIEW_TYPE_SERVER : VIEW_TYPE_USER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SERVER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_server, parent, false);
            return new ServerMessageViewHolder(this.markwon, view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_user, parent, false);
            return new UserMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatHistory chat = chatHistories.get(position);
        if (holder instanceof ServerMessageViewHolder) {
            ((ServerMessageViewHolder) holder).bind(chat);
        } else {
            ((UserMessageViewHolder) holder).bind(chat);
        }
    }

    @Override
    public int getItemCount() {
        return chatHistories.size();
    }

    static class ServerMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage, textTimestamp;
        Markwon markwon;

        ServerMessageViewHolder(Markwon markwon, View itemView) {
            super(itemView);
            this.markwon = markwon;
            textMessage = itemView.findViewById(R.id.textMessage);
            textTimestamp = itemView.findViewById(R.id.textTimestamp);
        }

        void bind(ChatHistory chat) {
            // textMessage.setText(chat.getMessage());
            markwon.setMarkdown(textMessage, chat.getMessage());
            textTimestamp.setText(chat.getCreatedAt().format(formatter));
        }
    }

    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage, textTimestamp;

        UserMessageViewHolder(View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.textMessage);
            textTimestamp = itemView.findViewById(R.id.textTimestamp);
        }

        void bind(ChatHistory chat) {
            textMessage.setText(chat.getMessage());
            textTimestamp.setText(chat.getCreatedAt().format(formatter));
        }
    }
}