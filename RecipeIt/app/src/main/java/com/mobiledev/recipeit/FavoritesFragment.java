package com.mobiledev.recipeit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.mobiledev.recipeit.Adapters.ChatHistoryAdapter;
import com.mobiledev.recipeit.Helpers.ChatHistoryManager;
import com.mobiledev.recipeit.Models.ChatHistory;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {

    private RecyclerView favoritesRecyclerView;
    private ChatHistoryAdapter favoritesAdapter;
    private List<ChatHistory> favoriteMessages = new ArrayList<>();
    private ChatHistoryManager historyManager;
    private TextView emptyView;

    public FavoritesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize UI components
        favoritesRecyclerView = view.findViewById(R.id.favoritesRecyclerView);
        emptyView = view.findViewById(R.id.emptyFavoritesView);
        
        // Initialize history manager
        historyManager = new ChatHistoryManager(getContext(), FirebaseAuth.getInstance().getCurrentUser());
        
        // Set up RecyclerView
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        favoritesAdapter = new ChatHistoryAdapter(getContext(), favoriteMessages);
        favoritesRecyclerView.setAdapter(favoritesAdapter);
        
        // Handle un-favorite action
        favoritesAdapter.setOnFavoriteClickListener((position, isFavorite) -> {
            if (!isFavorite) {
                // Remove from favorites if un-starred
                ChatHistory removedMessage = favoriteMessages.get(position);
                favoriteMessages.remove(position);
                favoritesAdapter.notifyItemRemoved(position);
                
                // Save updated favorites
                historyManager.saveFavorites(favoriteMessages);
                
                // Get all chat history to update the unfavorited message there too
                List<ChatHistory> allHistory = historyManager.loadHistory();
                for (ChatHistory message : allHistory) {
                    if (message.getMessage().equals(removedMessage.getMessage()) && 
                            message.getCreatedAt().equals(removedMessage.getCreatedAt())) {
                        message.setFavorite(false);
                    }
                }
                historyManager.saveHistory(allHistory);
                
                // Update empty state visibility
                updateEmptyViewVisibility();
            }
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }
    
    private void loadFavorites() {
        // Clear current list and load favorites from storage
        favoriteMessages.clear();
        favoriteMessages.addAll(historyManager.loadFavorites());
        favoritesAdapter.notifyDataSetChanged();
        
        // Update empty state visibility
        updateEmptyViewVisibility();
    }
    
    private void updateEmptyViewVisibility() {
        if (favoriteMessages.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            favoritesRecyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            favoritesRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}