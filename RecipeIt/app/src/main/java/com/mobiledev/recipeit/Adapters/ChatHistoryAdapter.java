package com.mobiledev.recipeit.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
    private OnFavoriteClickListener favoriteClickListener;

    public interface OnFavoriteClickListener {
        void onFavoriteClick(int position, boolean isFavorite);
    }

    public ChatHistoryAdapter(Context context, List<ChatHistory> chatHistories) {
        this.chatHistories = chatHistories;
        this.markwon = Markwon.create(context);
    }

    public void setOnFavoriteClickListener(OnFavoriteClickListener listener) {
        this.favoriteClickListener = listener;
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
            ((ServerMessageViewHolder) holder).bind(chat, position);
        } else {
            ((UserMessageViewHolder) holder).bind(chat, position);
        }
    }

    @Override
    public int getItemCount() {
        return chatHistories.size();
    }

    class ServerMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage, textTimestamp;
        ImageView favoriteIcon;
        Markwon markwon;

        ServerMessageViewHolder(Markwon markwon, View itemView) {
            super(itemView);
            this.markwon = markwon;
            textMessage = itemView.findViewById(R.id.textMessage);
            textTimestamp = itemView.findViewById(R.id.textTimestamp);
            favoriteIcon = itemView.findViewById(R.id.favoriteIcon);
        }

        void bind(ChatHistory chat, int position) {
            markwon.setMarkdown(textMessage, chat.getMessage());
            textTimestamp.setText(chat.getCreatedAt().format(formatter));
            
            // Set the favorite icon state
            updateFavoriteIcon(favoriteIcon, chat.isFavorite());
            
            // Set click listener for the favorite icon
            favoriteIcon.setOnClickListener(v -> {
                boolean newFavoriteState = !chat.isFavorite();
                chat.setFavorite(newFavoriteState);
                updateFavoriteIcon(favoriteIcon, newFavoriteState);
                
                if (favoriteClickListener != null) {
                    favoriteClickListener.onFavoriteClick(position, newFavoriteState);
                }
            });
        }
        
        private void updateFavoriteIcon(ImageView imageView, boolean isFavorite) {
            imageView.setImageResource(isFavorite ? 
                    R.drawable.ic_star_filled : 
                    R.drawable.ic_star_outline);
        }
    }

    class UserMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage, textTimestamp;
        ImageView favoriteIcon;

        UserMessageViewHolder(View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.textMessage);
            textTimestamp = itemView.findViewById(R.id.textTimestamp);
            favoriteIcon = itemView.findViewById(R.id.favoriteIcon);
        }

        void bind(ChatHistory chat, int position) {
            textMessage.setText(chat.getMessage());
            textTimestamp.setText(chat.getCreatedAt().format(formatter));
            
            // Set the favorite icon state
            updateFavoriteIcon(favoriteIcon, chat.isFavorite());
            
            // Set click listener for the favorite icon
            favoriteIcon.setOnClickListener(v -> {
                boolean newFavoriteState = !chat.isFavorite();
                chat.setFavorite(newFavoriteState);
                updateFavoriteIcon(favoriteIcon, newFavoriteState);
                
                if (favoriteClickListener != null) {
                    favoriteClickListener.onFavoriteClick(position, newFavoriteState);
                }
            });
        }
        
        private void updateFavoriteIcon(ImageView imageView, boolean isFavorite) {
            imageView.setImageResource(isFavorite ? 
                    R.drawable.ic_star_filled : 
                    R.drawable.ic_star_outline);
        }
    }
}